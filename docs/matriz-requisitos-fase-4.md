# Matriz de requisitos e evidências — 17/11/2026

Conferida contra `12SOAT - Fase 4 - Tech challenge.pdf`. Implementação, teste local e validação externa são estados diferentes. Esta matriz não declara a entrega concluída.

| Requisito | Evidência disponível | Situação / próxima comprovação |
|---|---|---|
| Três microsserviços independentes | Repositórios OS, Billing e Execution, Dockerfiles e manifests próprios | Implementado; infraestrutura compartilhada explicitada na arquitetura |
| Banco próprio; SQL e NoSQL | PostgreSQL OS/Execution, MongoDB Billing | Implementado; integração automatizada com bancos reais |
| OS: abertura, status e histórico | Controllers e testes do OS; docs de abertura/progresso/histórico | Validado localmente e no CI |
| Billing: orçamento e envio | Orçamento, decisão pública e outbox SMTP | Recebimento local confirmado em 16/11; configuracao TLS e diagnostico de falhas revisados |
| Mercado Pago e atualização após pagamento | Checkout, webhook, consulta ao provedor, evento para OS | Link criado; compra de teste ponta a ponta permanece PENDENTE |
| Execution: fila, diagnóstico e reparos | Fila ordenada, estoque, reservas e conclusão | Validado em testes; comunica progresso à OS |
| REST, mensageria e isolamento dos bancos | APIs, RabbitMQ, outbox/inbox | Implementado; sem leitura cruzada dos bancos |
| Saga e compensação | Recusa libera estoque; cancelamento anterior à criação; testes com broker indisponível | Validado nos cenários automatizados; após execução física há recuperação para frente, sem estorno automático |
| Unitários, BDD e cobertura mínima 80% | JaCoCo, testes de cada serviço e três cenários distribuídos | CI aprovado nos commits abaixo; pagamento do BDD é simulado |
| SonarQube ou similar | PMD obrigatório em Maven verify/CI | Implementado; relatórios disponibilizados pelo CI |
| CI/CD independente | Workflows por repositório | Tres CD concluidos em 17/11; ver validacao-final-2026-11-17.md |
| Main protegida | PR obrigatório e checks automáticos configurados | Configurado; integracoes seguem por PR com checks obrigatorios |
| Observabilidade da Fase 3 | Logs, métricas, correlação e configuração Datadog | Coleta de logs/traces confirmada nos agentes; visualizacao do painel para video pendente |
| Documentação e contratos | Arquitetura atual e collections Postman nos serviços | Documentação preparada; collections são roteiro manual, não prova de chamadas executadas |
| Vídeo até 15 minutos | Roteiro de demonstração | Gravação/upload e link pendentes |
| PDF do portal | Texto-base com estrutura de entrega | Participante identificado e PDF preparado; falta link do video e conclusao das evidencias externas |

## CI e cobertura

Porcentagens conferidas em 17/11 diretamente nos jacoco.xml baixados dos artefatos dos CIs abaixo. O BDD distribuido usa os commits OS d5518bc e Billing fe0d4d4 fixados no workflow do Execution.

| Serviço | Commit validado | Cobertura de linhas registrada | CI |
|---|---|---|---|
| OS | `d5518bc6e9393b7fa5aec447ac50a75d2046f77a` | 93,13% | [Execução](https://github.com/Mateusborg98/mecanica-os-service/actions/runs/44092179490) |
| Billing | `fe0d4d4b70bb7b6d74e75b9766f5c9d953f24466` | 89,57% | [Execução](https://github.com/Mateusborg98/mecanica-billing-service/actions/runs/44092005684) |
| Execution | `3742d651b347326da60fa12ad050380027fff44b` | 87,14% | [Execução e BDD](https://github.com/Mateusborg98/mecanica-execution-service/actions/runs/44092211619) |


## Validacao externa

OS CD concluido: https://github.com/Mateusborg98/mecanica-os-service/actions/runs/44090636491 . Dois cenarios AWS repetidos com sucesso em 17/11: aprovacao ate cobranca pendente e recusa com compensacao. Pagamento Mercado Pago permanece pendente (WCS-59172); nao marcar pagamento manualmente. SMTP recebido localmente em 16/11; envio AWS depende de configuracao propria. Video e seu link so podem ser concluidos apos gravacao.

Em 17/11, a sessao Academy encerrou e retornou explicit deny voc-cancel-cred; o CD Billing [44131509282](https://github.com/Mateusborg98/mecanica-billing-service/actions/runs/44131509282) ficou bloqueado antes da publicacao. Renovar credenciais e reexecutar o job; isso nao invalida os testes aprovados. Billing e Execution tiveram os PRs integrados. O Swagger dos dois foi validado localmente por HTTP 200, contrato OpenAPI 3.1, rotas publicas sem bearer e rotas administrativas retornando 401 sem JWT.

## Fechamento dos deploys

As tres reexecucoes terminaram com sucesso apos renovar a sessao. Swaggers e cenarios distribuidos validados na AWS. Consulte [evidencias finais](validacao-final-2026-11-17.md). O paragrafo sobre voc-cancel-cred registra a falha anterior, ja superada.
