# Futuros evals de componentes probabilísticos

Este diretório reserva um único local para datasets, runners e resumos de resultados caso componentes probabilísticos sejam introduzidos. Nenhum framework de eval, dataset, chamada a provider ou integração com models está incluído neste momento.

Unit tests verificam unidades determinísticas rapidamente e devem usar mocks nas fronteiras de models e providers para que o comportamento de negócio, a validação e o tratamento de falhas permaneçam reproduzíveis. Integration tests verificam a colaboração entre componentes reais da aplicação e contratos de infraestrutura. Evals respondem a outra pergunta: quão bem uma implementação probabilística se comporta em exemplos representativos nos quais as saídas podem variar.

Evals futuros podem executar models reais sobre datasets representativos e versionados e compará-los a um baseline determinístico existente ou revisado por pessoas. As medições relevantes podem incluir precisão da tarefa, falhas de schema ou reconciliação, confiabilidade entre execuções, latency, tokens de entrada e saída e cost estimado ou real. A spec da funcionalidade deve escolher limites proporcionais ao risco, em vez de considerar uma única pontuação universal como suficiente.

Execute a suíte de evals relevante quando um prompt, output schema, model ou provider, etapa de pré-processamento ou comportamento esperado mudar. Não armazene credenciais nem documentos sensíveis de corretagem no repositório. Mantenha a execução de evals separada do ciclo padrão de unit tests até que suas dependências, cost e reprodutibilidade justifiquem automação.
