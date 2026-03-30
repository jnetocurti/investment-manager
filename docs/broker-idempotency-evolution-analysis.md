# Análise prévia — evolução de broker e idempotência de PortfolioEvent

## Escopo
Documento de análise crítica do estado atual para orientar implementação futura sem retrocompatibilidade legada.

## Diagnóstico do desenho atual
- `PortfolioEvent` persiste `brokerName`, `brokerDocument` e `brokerKey` ao mesmo tempo, misturando identidade com atributos de apresentação no próprio evento factual.
- A geração de `brokerKey` hoje depende de `BrokerIdentityResolver`, com alias hardcoded (`CLEAR`, `NUINVEST`) e fallback por documento/nome normalizado.
- Idempotência de `portfolio_events` está parcial: existe por `sourceReferenceId` (trading note e conversão) e índice único **somente** para `SUBSCRIPTION` por chave de negócio (`eventType`, `assetName`, `assetType`, `brokerKey`, `eventDate`).
- `position_impact_events` não usa `brokerKey`; usa `brokerName` e `brokerDocument` para query por aliases e para cálculo de posição.
- `asset_positions` já funciona como read model consolidada contendo `brokerKey` + dados descritivos (`brokerName`, `brokerDocument`).
- Cadastro implícito de broker já existe em `brokers` com unicidade por `brokerKey` e manutenção de aliases conhecidos (`knownNames`, `knownDocuments`).

## Problemas principais
1. Redundância e acoplamento semântico em eventos
   - Evento de domínio (factual) carrega também dados descritivos de UI (`brokerName/document`), gerando duplicação em `portfolio_events` e `position_impact_events`.
2. Identidade de broker frágil
   - `brokerKey` atual não é ID persistente do Mongo; depende de regra de alias in-memory.
3. Idempotência assimétrica
   - Regras espalhadas e especializadas para subscription (`existsSubscriptionByBusinessKey` + índice parcial), sem estratégia única por `PortfolioEvent`.
4. Risco de condição de corrida
   - Há checagens `exists*` prévias no serviço e reforço parcial em índice único; fluxo geral depende de janela entre check e write.
5. Acoplamento cross-module por atributos descritivos
   - Asset position consulta impacts por `brokerDocument`/`brokerName` (alias query), não por identificador estável.

## Crítica da proposta e riscos
- **Risco de quebra de fluxo ponta a ponta** ao remover nome/documento dos eventos sem antes trocar o critério de query de impacts e cálculo de posição para `brokerId`.
- **Risco de over-engineering** ao tentar generalizar idempotência com estratégia excessivamente plugável por tipo de evento; a base atual sugere solução simples com chave derivada + política por tipo mínima.
- **Risco de vazamento de infraestrutura** se `ObjectId` do Mongo for usado cru no domínio; preferir `BrokerId` como VO/string opaca no domínio.
- **Risco de duplicidade de broker** se cadastro implícito continuar baseado em chave não canônica e sem normalização consistente antes do upsert.

## Estratégia recomendada (alto nível)
1. Separar claramente identidade vs apresentação:
   - Eventos (`portfolio_events` e `position_impact_events`) ficam com `brokerId` (e opcionalmente `brokerKey` apenas transitório durante refactor, depois remover).
   - `asset_positions` mantém `brokerId`, `brokerName`, `brokerDocument`.
2. Tornar o cadastro implícito de broker a origem da identidade:
   - Resolver/canonicalizar entrada → localizar/criar broker em `brokers` → devolver `brokerId` para o fluxo.
3. Generalizar idempotência no contexto de `PortfolioEvent`:
   - Chave idempotente derivada por evento (`idempotencyKey`) + índice único no banco.
   - Política de composição por tipo de evento, porém sem framework complexo.
4. Ajustar projeções e mensageria para trafegar identidade (`brokerId`) em vez de dados descritivos.

## Recomendação para idempotência genérica de PortfolioEvent
- Definir uma **IdempotencyKeyFactory** de domínio/aplicação com:
  - Parte comum: `eventType`, `eventSource`, `assetName`, `assetType`, `brokerId`, `eventDate`.
  - Parte específica por tipo apenas quando necessário (ex.: metadados relevantes em SUBSCRIPTION).
- Persistir `idempotencyKey` em `portfolio_events` com índice único global.
- Regra principal: tentativa de insert direto e tratamento de `DuplicateKeyException` como duplicado idempotente (evita race condition do check-before-write).
- Manter `sourceReferenceId` para rastreabilidade/origem, não como regra geral de idempotência.

## Recomendação para brokerId
- `BrokerKey` pode ser aposentado gradualmente como identidade de negócio principal.
- No domínio, usar `brokerId` como identificador opaco (VO leve) para não acoplar a Mongo internamente.
- Resolver broker na borda de aplicação antes de construir `PortfolioEvent`.

## Recomendação para asset_positions
- Permanecer como read model com snapshot descritivo (`brokerName/document`) + identidade (`brokerId`).
- Garantir consistência por atualização sempre a partir do registro atual de `brokers` no momento da recomputação.
- Assumir sem ambiguidade que nome/documento em `asset_positions` são **snapshot de leitura** (não fonte de verdade).

## Sequência ideal de implementação (incremental)
1. Introduzir `brokerId` no domínio de `PortfolioEvent` e em persistência.
2. Introduzir resolução/cadastro implícito de broker por serviço dedicado (find-or-create) retornando `brokerId`.
3. Propagar `brokerId` para `PositionImpactEvent` e mensageria de impacto.
4. Alterar queries de impacto para usar `brokerId` (eliminar alias query por nome/documento).
5. Atualizar `AssetPositionService` para calcular por `brokerId` e enriquecer descrição via `brokers`.
6. Implementar `idempotencyKey` genérica de `PortfolioEvent` + índice único.
7. Remover `brokerName/document` de `portfolio_events` e `position_impact_events`.
8. Manter apenas `asset_positions` com `brokerId` + `brokerName` + `brokerDocument`.
9. Limpar artefatos antigos (`existsSubscriptionByBusinessKey`, `BrokerIdentityResolver` hardcoded se perder utilidade).

## Ambiguidades de negócio a confirmar
- Para cada `eventType`, quais campos distinguem eventos legítimos diferentes no mesmo dia/ativo/corretora?
- `metadata` deve compor idempotência apenas para alguns tipos (ex.: subscriptionTicker) ou para todos via hash canônico?
- Em caso de broker com mudança cadastral, `asset_positions` deve refletir sempre o cadastro mais recente (snapshot atualizado) ou manter histórico textual por snapshot apenas?
