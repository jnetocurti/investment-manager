# Constituição do Investment Manager

## Princípios fundamentais

### I. Correção financeira e rastreabilidade
A correção financeira e um caminho auditável desde o documento ou comando de origem até eventos, impactos e projeções DEVEM ter prioridade sobre a velocidade de entrega. Alterações relevantes DEVEM preservar referências de origem, identificadores determinísticos e evidências suficientes para explicar um resultado calculado.

### II. Determinismo nas fronteiras financeiras
Cálculos, reconciliações, apurações e regras formalizáveis DEVEM permanecer determinísticos quando apropriado. Saídas probabilísticas NÃO DEVEM contornar schemas, invariantes, reconciliação, autorização ou outras validações determinísticas proporcionais ao seu risco financeiro e operacional.

### III. Capacidades probabilísticas orientadas por evidências
Componentes probabilísticos PODEM complementar ou substituir implementações existentes somente quando oferecerem benefício mensurável. Seus requisitos DEVEM contemplar precisão, confiabilidade, latência e custo. Prefira o modelo menos complexo e de menor custo que satisfaça limites explícitos; mantenha modelos e providers substituíveis quando isso não criar complexidade artificial.

### IV. Arquitetura centrada no domínio e orientada por necessidades
As políticas de domínio DEVEM permanecer separadas dos detalhes de infraestrutura por meio dos ports e adapters existentes. Tecnologias, dependências, serviços e abstrações DEVEM ser introduzidos para resolver um requisito concreto, não em busca de flexibilidade especulativa. Preserve os limites entre módulos, exceto quando uma mudança planejada apresentar razões verificáveis para alterá-los.

### V. Entrega verificável
Toda alteração relevante DEVE definir critérios de sucesso observáveis. Código produzido ou modificado por agentes está sujeito às mesmas revisões, testes, verificações determinísticas e etapas de avaliação que qualquer outro código. Testes unitários e de integração continuam sendo o padrão para comportamento determinístico; trabalho que afete modelos também DEVE definir evals representativos e limites proporcionais ao risco.

## Registros de engenharia

O trabalho em funcionalidades segue `specify -> plan -> tasks -> implement -> converge`, regido por esta constituição; atualize a constituição separadamente quando princípios duradouros mudarem. Specs DEVEM distinguir o comportamento exigido das escolhas de implementação e incluir critérios de verificação. Planos DEVEM informar módulos afetados, limites de domínio, compatibilidade de dados e mensageria, testes e, quando aplicável, medidas de avaliação.

Registre uma decisão arquitetural relevante como ADR quando preservar seu contexto e suas consequências beneficiar trabalhos futuros. Não crie justificativas retroativas sem evidências. Mantenha a documentação arquitetural alinhada ao código e à configuração observáveis.

## Governança

Esta constituição prevalece sobre orientações locais de desenvolvimento conflitantes. Emendas exigem motivo documentado, revisão das specs, templates e documentações afetadas, alteração de versão semântica e nova data de emenda. A conformidade DEVE ser verificada durante o planejamento e novamente antes da convergência. Exceções DEVEM estar explícitas no plano, ter escopo delimitado e ser acompanhadas por uma justificativa de correção ou aceitação.

**Versão**: 1.0.0 | **Ratificada em**: 2026-10-03 | **Última emenda**: 2026-10-03
