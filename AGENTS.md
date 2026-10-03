# Guia de agentes do Investment Manager

O Investment Manager ingere notas de corretagem brasileiras e eventos corporativos, transforma-os em eventos de portfólio e impactos de posição e recompõe posições e resultados realizados.

## Mapa de trabalho

- Stack: Java 21, Maven, Spring Boot, MongoDB, RabbitMQ e MinIO; frontend React/Vite.
- Módulos do reator Maven do backend: `commons`, `tradingnote`, `portfolioevent`, `assetposition` e o módulo executável de composição `app`.
- Leia [`docs/architecture.md`](docs/architecture.md) antes de alterar um fluxo ou limite de módulo.
- Especificações de funcionalidades ficam em `specs/<NNN-nome-da-funcionalidade>/`; a política do projeto está em [`.specify/memory/constitution.md`](.specify/memory/constitution.md). No Codex, use as skills oficiais `$speckit-constitution`, `$speckit-specify`, `$speckit-clarify`, `$speckit-plan`, `$speckit-tasks`, `$speckit-implement` e `$speckit-converge`.
- Decisões arquiteturais ficam em [`docs/adr/`](docs/adr/); as orientações para avaliações probabilísticas estão em [`evals/README.md`](evals/README.md).

## Regras para alterações

Preserve a separação entre domínio e infraestrutura e o comportamento financeiro determinístico. Faça a menor alteração justificada por uma spec, adicione critérios de aceitação verificáveis e teste código produzido por agentes exatamente como código produzido por pessoas. Registre escolhas arquiteturais duradouras e relevantes como ADRs. Nunca enfraqueça testes para fazer uma alteração passar.

## Verificações oficiais

Execute a partir da raiz do repositório:

```bash
mvn -f backend/pom.xml clean verify
npm --prefix frontend run build
```

Use `docker compose up -d` somente quando uma tarefa exigir os serviços locais MongoDB, RabbitMQ ou MinIO. Para trabalho no parser, consulte `tradingnote`; para eventos e regras, `portfolioevent`; para cálculos e projeções, `assetposition`; para composição e configuração de infraestrutura, `app` e `docker-compose.yml`.
