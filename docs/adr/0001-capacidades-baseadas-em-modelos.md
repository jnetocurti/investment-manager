# ADR 0001: Introduzir capacidades baseadas em modelos de forma incremental

- **Status:** Aceito
- **Data:** 2026-10-03

## Contexto

Os fluxos de negócio atuais são predominantemente determinísticos: extração de notas por expressões regulares, validação e reconciliação explícitas, tradução tipada de eventos e reaplicação ordenada de impactos. Algumas tarefas futuras podem se beneficiar de modelos probabilísticos, mas ainda não existe integração com modelos nem evidência que sustente uma substituição específica.

## Decisão

Introduzir capacidades probabilísticas somente em incrementos delimitados e com benefício mensurável. Uma implementação baseada em modelos pode complementar ou substituir uma implementação existente quando uma avaliação representativa demonstrar que ela atende a critérios explícitos de precisão, confiabilidade, latência e custo.

Manter validações determinísticas nas fronteiras de risco adequadas, especialmente para totais financeiros, schemas, invariantes e reconciliação. Preferir o modelo menos complexo e de menor custo que atenda aos critérios. Manter providers e modelos substituíveis quando razoável; evitar abstrações cujo único efeito seja criar complexidade artificial.

Usar os ports e adapters existentes quando forem adequados. A adição dessas capacidades não exige redesenhar toda a arquitetura.

## Consequências

- Cada funcionalidade que afete modelos deve definir critérios mensuráveis de aceitação e avaliação antes da implementação.
- A saída de modelos é tratada como não confiável em proporção ao risco financeiro e operacional.
- As decisões podem comparar o baseline determinístico atual, uma solução híbrida e uma substituição, em vez de presumir um único caminho de migração.
- Novas infraestruturas e dependências de providers exigem uma necessidade concreta; decisões subsequentes relevantes podem receber seu próprio ADR quando o contexto futuro for conhecido.
