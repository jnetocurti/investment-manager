# Futuras avaliações probabilísticas

Este diretório reserva um único local para datasets, executores e resumos de resultados caso componentes probabilísticos sejam introduzidos. Nenhum framework de avaliação, dataset, chamada a provider ou integração com modelos está incluído neste momento.

Testes unitários verificam unidades determinísticas rapidamente e devem usar mocks nas fronteiras de modelos e providers para que o comportamento de negócio, a validação e o tratamento de falhas permaneçam reproduzíveis. Testes de integração verificam a colaboração entre componentes reais da aplicação e contratos de infraestrutura. Evals respondem a outra pergunta: quão bem uma implementação probabilística se comporta em exemplos representativos nos quais as saídas podem variar.

Evals futuros podem executar modelos reais sobre datasets representativos e versionados e compará-los a um baseline determinístico existente ou revisado por pessoas. As medições relevantes podem incluir precisão da tarefa, falhas de schema ou reconciliação, confiabilidade entre execuções, latência, tokens de entrada e saída e custo estimado ou real. A spec da funcionalidade deve escolher limites proporcionais ao risco, em vez de considerar uma única pontuação universal como suficiente.

Execute a suíte de evals relevante quando um prompt, schema de saída, modelo ou provider, etapa de pré-processamento ou comportamento esperado mudar. Não armazene credenciais nem documentos sensíveis de corretagem no repositório. Mantenha a execução de evals separada do ciclo padrão de testes unitários até que suas dependências, custo e reprodutibilidade justifiquem automação.
