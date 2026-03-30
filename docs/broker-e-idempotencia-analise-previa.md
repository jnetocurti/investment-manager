# Análise prévia — evolução de broker e idempotência no Investment Manager

Data: 2026-03-30

## 1) Diagnóstico do modelo atual

### 1.1 Representação de broker no fluxo
- O `PortfolioEvent` hoje carrega `brokerName`, `brokerDocument` e `brokerKey` no agregado de domínio. Isso mistura identidade + apresentação no mesmo objeto de evento. 
- A fábrica `fromOperation` calcula `brokerKey` via `BrokerIdentityResolver`.
- `SubscriptionService` repete o cálculo e também usa `brokerName`/`brokerDocument` no evento.

### 1.2 Persistência e idempotência
- `portfolio_events` persiste os três campos (`brokerName`, `brokerDocument`, `brokerKey`).
- Idempotência hoje é parcial:
  - por `sourceReferenceId` (no fluxo trading note);
  - por índice único **somente para SUBSCRIPTION** (`eventType + assetName + assetType + brokerKey + eventDate`).
- A API de repositório também explicita essa especialização (`existsSubscriptionByBusinessKey`).

### 1.3 Projeção e cadastro implícito de corretora
- `asset_positions` já funciona como read model e mantém `brokerKey`, `brokerName` e `brokerDocument`.
- Existe cadastro implícito em `brokers` (`BrokerRegistryDocument`) com `brokerKey` único + aliases (`knownNames`/`knownDocuments`).
- A composição da posição depende de busca de impactos por **aliases de nome/documento** (`$or` com `brokerDocument` ou `brokerName`), não por identificador estável de broker.

### 1.4 Mensageria / acoplamento
- O fluxo ponta a ponta transporta `brokerName` e `brokerDocument`:
  - `TradingNoteCreatedEvent` → `TradingNoteMessage` → `CreatePortfolioEventsCommand`;
  - `PositionImpactEvent`/mensagem de impacto também carrega nome/documento;
  - `AssetPositionService` recebe nome/documento para resolver `brokerKey` e aliases.

### 1.5 Metadata
- `PortfolioEventMetadata` é minimalista (ex.: `subscriptionTicker`) e não participa da idempotência hoje.

## 2) Principais problemas identificados

1. **Identidade de broker é instável e sem referência canônica persistida no evento**: `brokerKey` atual é derivado por heurística (alias + normalização) e não é o ID de `brokers`.
2. **Duplicidade conceitual**: eventos e impactos persistem dados descritivos (nome/documento), embora isso seja necessidade de leitura/apresentação.
3. **Idempotência fragmentada**: regra forte em subscription, regra fraca em trading note, sem contrato unificado para `PortfolioEvent`.
4. **Acoplamento da composição de posição a aliases textuais**: consulta por nome/documento aumenta risco de falso positivo/falso negativo.
5. **Race condition potencial**: checagens `exists...` + `saveAll` podem concorrer; hoje a proteção real está só em índices específicos.
6. **Cadastro implícito distribuído na lógica de posição**: broker é “descoberto” tardiamente no `assetposition`, não no ponto de entrada do evento.

## 3) Crítica da proposta (o que pode dar errado)

1. **Trocar direto para `brokerId` sem etapa de resolução explícita no pipeline** pode quebrar criação de evento quando broker ainda não estiver registrado na execução concorrente.
2. **Generalização excessiva da idempotência por `eventType` com muitas estratégias** pode virar over-engineering e esconder regra de negócio simples.
3. **Remover nome/documento dos eventos sem ajustar impactos e consumers** quebra cálculo de posição, pois hoje a query depende de aliases.
4. **Acoplamento indevido ao Mongo `_id` no domínio**: usar string crua de infra em todo lugar enfraquece clareza de modelo.
5. **Misturar metadata com identidade idempotente** pode tornar o sistema frágil (metadados são mais suscetíveis a evolução semântica).

## 4) Melhor estratégia de evolução

### Diretriz de desenho
- Separar explicitamente:
  - **identidade**: `brokerId` (canônico);
  - **apresentação**: `brokerName` e `brokerDocument` apenas em `asset_positions`.

### Estratégia incremental (sem retrocompatibilidade)
1. Introduzir resolução/cadastro implícito de broker **antes da criação de `PortfolioEvent`** (em caso de trading note e subscription).
2. Propagar `brokerId` por todo pipeline de evento e impacto.
3. Remover nome/documento de `PortfolioEvent` e `PositionImpactEvent` + documentos Mongo/mensagens associadas.
4. Ajustar `assetposition` para consultar impactos por `brokerId` (não mais aliases).
5. Manter `brokers` como fonte de enriquecimento descritivo para gravar snapshot em `asset_positions`.

## 5) Estratégia recomendada para idempotência genérica de PortfolioEvent

### 5.1 Conceito
- Criar uma **idempotent key derivada do evento** (campo explícito `idempotencyKey`) persistida em `portfolio_events` com índice único.
- A chave deve combinar:
  - `eventType`;
  - `eventSource`;
  - `assetName` + `assetType`;
  - `brokerId`;
  - `eventDate`;
  - `sourceReferenceId` (quando existir);
  - parte específica mínima por tipo (ex.: `metadata.subscriptionTicker` para subscription/conversion se necessário).

### 5.2 Governança da regra
- Regra de composição deve viver no **domínio de portfolioevent** (policy/factory de chave), não no repositório.
- Repositório apenas aplica persistência e traduz `DuplicateKeyException`.

### 5.3 Concorrência
- Manter checagem prévia opcional para resposta amigável, mas tratar índice único como autoridade final (evita race condition).

### 5.4 Evitar over-engineering
- Não criar hierarquia complexa por `eventType` de início.
- Começar com composição única e **uma tabela de campos opcionais por tipo** (switch simples), evoluindo só quando houver novo tipo com regra realmente distinta.

## 6) Estratégia recomendada para uso de brokerId

1. **`BrokerKey` ainda faz sentido?**
   - Como conceito de domínio, sim, mas agora deve representar identidade canônica (`BrokerId`) e não alias heurístico.
2. **VO ou string?**
   - Manter como Value Object leve (ex.: `BrokerId`) para proteger invariantes e reduzir vazamento de infra.
3. **Acoplamento com Mongo**
   - Não expor semântica de `_id` no domínio; tratar como identificador opaco.
4. **Ponto de resolução**
   - Resolver/cadastrar broker no início do caso de uso de criação de evento e passar só `brokerId` adiante.

## 7) Estratégia recomendada para `asset_positions` como única fonte descritiva

1. Faz sentido: `asset_positions` já é read model consolidada.
2. `brokerName` e `brokerDocument` devem ser **snapshot** no momento do recálculo da posição (não lookup dinâmico em toda leitura).
3. Consistência:
   - sempre recalcular/enriquecer com `brokers.currentName/currentDocument` ao persistir posição;
   - manter índice único por (`assetName`,`assetType`,`brokerId`).
4. Trade-off:
   - snapshot pode ficar desatualizado frente ao cadastro de broker, mas é aceitável para read model e auditabilidade da visão gerada.

## 8) Sequência ideal de implementação (passo a passo)

1. Mapear todos os pontos de entrada que criam `PortfolioEvent` (trading note + subscription) e introduzir resolução/cadastro implícito de broker retornando `brokerId`.
2. Evoluir modelo de domínio de `PortfolioEvent` para usar `brokerId` e remover `brokerName`/`brokerDocument`.
3. Evoluir persistência `portfolio_events` (document, mapper, repo, índices) para `brokerId` + `idempotencyKey` única.
4. Generalizar `PortfolioEventRepositoryPort` removendo método específico de subscription e adotando contrato idempotente único.
5. Evoluir `PositionImpactEvent`/document/mensagem para transportar `brokerId` (sem nome/documento).
6. Ajustar query de impactos (`position_impact_events`) para filtro por `brokerId`.
7. Ajustar `AssetPositionService` para operar por `brokerId` e enriquecer `brokerName`/`brokerDocument` a partir de `brokers`.
8. Garantir persistência final de `asset_positions` com `brokerId`,`brokerName`,`brokerDocument`.
9. Revisar testes unitários e de integração para novo contrato ponta a ponta.

## 9) Pontos do código a inspecionar antes de implementar

- `portfolioevent/domain/model/PortfolioEvent`
- `portfolioevent/domain/service/PortfolioEventService`
- `portfolioevent/domain/service/SubscriptionService`
- `portfolioevent/domain/model/PortfolioEventMetadata`
- `portfolioevent/domain/port/in/CreatePortfolioEventsCommand`
- `portfolioevent/domain/port/in/CreateSubscriptionCommand`
- `portfolioevent/domain/port/out/PortfolioEventRepositoryPort`
- `portfolioevent/adapter/out/persistence/PortfolioEventDocument`
- `portfolioevent/adapter/out/persistence/PortfolioEventDocumentMapper`
- `portfolioevent/adapter/out/persistence/PortfolioEventPersistenceAdapter`
- `portfolioevent/adapter/out/persistence/PortfolioEventMongoRepository`
- `portfolioevent/domain/model/PositionImpactEvent`
- `portfolioevent/domain/service/PositionImpactGenerationService`
- `portfolioevent/adapter/out/persistence/impact/PositionImpactEventDocument`
- `portfolioevent/adapter/out/persistence/impact/PositionImpactEventDocumentMapper`
- `portfolioevent/adapter/out/persistence/impact/PositionImpactEventMongoRepository`
- `portfolioevent/domain/service/impact/*` (translators)
- `tradingnote/domain/model/TradingNoteCreatedEvent`
- `portfolioevent/adapter/in/messaging/TradingNoteMessage`
- `portfolioevent/adapter/in/messaging/TradingNoteCreatedListener`
- `assetposition/adapter/in/messaging/PositionImpactCreatedMessage`
- `assetposition/adapter/in/messaging/PortfolioEventProcessedListener`
- `assetposition/domain/service/AssetPositionService`
- `assetposition/domain/model/AssetPosition`
- `assetposition/adapter/out/persistence/AssetPositionDocument`
- `assetposition/domain/model/BrokerRegistry`
- `assetposition/adapter/out/persistence/BrokerRegistryDocument`
- `assetposition/adapter/out/query/PositionImpactQueryAdapter`
- `commons/domain/model/BrokerIdentityResolver`

## Ambiguidades de negócio (interpretação segura)

1. **Subscription conversion**: assumir que conversão é idempotente por vínculo com o evento de subscrição + `eventType`.
2. **Escopo de unicidade de broker implícito**: assumir unicidade global de corretora por critério canônico definido no registro `brokers` (não por ativo/conta).
3. **Metadata na idempotência**: assumir que metadata só entra na chave quando for parte da identidade de negócio do tipo de evento.
