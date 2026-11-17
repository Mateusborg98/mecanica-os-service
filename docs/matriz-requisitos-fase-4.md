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
| CI/CD independente | Workflows por repositório | OS CD concluido em 17/11; Billing e Execution em validacao final |
| Main protegida | PR obrigatório e checks automáticos configurados | Configurado; integracoes seguem por PR com checks obrigatorios |
| Observabilidade da Fase 3 | Logs, métricas, correlação e configuração Datadog | Evidência em AWS/Datadog pendente |
| Documentação e contratos | Arquitetura atual e collections Postman nos serviços | Documentação preparada; collections são roteiro manual, não prova de chamadas executadas |
| Vídeo até 15 minutos | Roteiro de demonstração | Gravação/upload e link pendentes |
| PDF do portal | Texto-base com estrutura de entrega | Faltam identificação dos participantes, link do vídeo e evidências externas finais |

## CI e cobertura

As porcentagens são das validações já registradas, não uma nova medição feita durante esta revisão documental. Consulte os artefatos da execução correspondente antes da entrega final.

| Serviço | Commit validado | Cobertura de linhas registrada | CI |
|---|---|---|---|
| OS | `21fb0c541adc75da95a125ee1b1889f37b23e860` | 93,07% | [Execução](https://github.com/Mateusborg98/mecanica-os-service/actions/runs/42662648525) |
| Billing | `4ba6caa9379d6771eef092b7663f2bafb5766a80` | 89,94% | [Execução](https://github.com/Mateusborg98/mecanica-billing-service/actions/runs/42662614822) |
| Execution | `e6849e04bcd5d2aac895a27c52659ca90f5c3096` | 87,98% | [Execução e BDD](https://github.com/Mateusborg98/mecanica-execution-service/actions/runs/42662910263) |


## Validacao externa

OS CD concluido: https://github.com/Mateusborg98/mecanica-os-service/actions/runs/44090636491 . Dois cenarios AWS repetidos com sucesso em 17/11: aprovacao ate cobranca pendente e recusa com compensacao. Pagamento Mercado Pago permanece pendente (WCS-59172); nao marcar pagamento manualmente. SMTP recebido localmente em 16/11; envio AWS depende de configuracao propria. Video e seu link so podem ser concluidos apos gravacao.
