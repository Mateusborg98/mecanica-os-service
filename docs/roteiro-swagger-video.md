# Ensaio do video da Fase 4

Participante: Mateus Borgonovi Francisco Drago - RM370507.

## Preparacao

SMTP sera demonstrado no ambiente local ja validado. No Swagger AWS, rotas de notificacao nao aparecem quando NOTIFICACOES_ENABLED=false; o webhook tambem depende da configuracao habilitada. Nao habilite SMTP na AWS sem credenciais proprias.

No PowerShell 7, na pasta mecanica-execution-service, execute `./scripts/abrir-swagger-aws.ps1 -CopiarToken`. O script abre tres abas por port-forward local: OS 19081, Billing 19082 e Execution 19083. Mantenha o terminal aberto; Enter encerra os encaminhamentos criados. O token fica no clipboard, vale 15 minutos e deve ser colado no Authorize de cada Swagger antes de gravar. Nao filme JWT, senhas, Secrets ou cookies. Reinicie o script para renovar o token.

Use registros novos em cada ensaio. Cadastre cliente, operador e veiculo no OS. Use CPF valido de teste, placa ainda nao cadastrada e matricula nova. O Swagger apresenta o schema de cada corpo. Guarde placa e matricula; nao e preciso consultar banco.

## Fluxo aprovado (5 minutos)

| Ordem | Swagger | Operacao | O que guardar/mostrar |
|---|---|---|---|
| 1 | OS | POST /ordens-servico | Corpo com placa e matriculaOperador; guardar id como ordemId |
| 2 | OS | GET /ordens-servico/{id}/reparo | Aguardar publicado=true; guardar reparoId |
| 3 | Execution | POST /servicos | codigo unico, descricao, preco 50; guardar id |
| 4 | Execution | POST /reparos/{id}/diagnostico | Usar corpo abaixo; guardar orcamentoId da resposta |
| 5 | Billing | GET /orcamentos/{id} | Aguardar criacao assincrona; inicialmente pode retornar 404 |
| 6 | Billing | POST /orcamentos/{id}/notificacao | destinatario com e-mail seu; depende de SMTP configurado no ambiente |
| 7 | Billing | GET /orcamentos/{id}/notificacao | Mostrar ENVIADO e o e-mail recebido; nao confundir envio local com AWS |
| 8 | Billing | POST /orcamentos/{id}/aprovar | Ou abrir o link de aprovacao recebido por e-mail |
| 9 | Execution | GET /reparos/fila | Reparo passa a NA_FILA |
| 10 | Execution | POST /reparos/{id}/iniciar | Consultar acompanhamento OS: EM_EXECUCAO |
| 11 | Execution | POST /reparos/{id}/concluir | Consultar acompanhamento OS: FINALIZADA |
| 12 | Billing | GET /cobrancas/{orcamentoId} | PENDENTE, valor 50 |
| 13 | OS | POST /ordens-servico/{id}/entregar | Antes de pagar: 422 |
| 14 | Billing | POST /cobrancas/{orcamentoId}/checkout | Abrir exatamente checkoutUrl retornado |
| 15 | Mercado Pago | Compra com cartao de teste | Janela anonima sem login; fluxo Orders confirmado pelo suporte |
| 16 | Billing / OS | Consultar cobranca e historico | Somente apos webhook validado: pagamento confirmado e entrega liberada |
| 17 | OS | POST /ordens-servico/{id}/entregar | Sucesso apenas com reparo concluido e pagamento confirmado |

Diagnostico (substitua referenciaId pelo id do servico):

```json
{"observacoes":"Revisao para demonstracao","itens":[{"tipo":"SERVICO","referenciaId":"ID-DO-SERVICO","quantidade":1}]}
```

Acompanhe os eventos em GET /ordens-servico/{id}/historico e o status em GET /ordens-servico/acompanhamento/{id}. Nao altere status manualmente para pular participantes da Saga.

## Compensacao por recusa (2 minutos)

Abra outra OS. No Execution, cadastre peca com SKU unico, descricao e preco 25.50. Adicione 5 unidades em POST /pecas/{id}/entradas com movimentoId UUID novo e quantidade=5. Registre diagnostico com tipo=PECA, referenciaId=id da peca e quantidade=2. Reserve em POST /reparos/{id}/reserva:

```json
{"itens":[{"pecaId":"ID-DA-PECA","quantidade":2}]}
```

Mostre GET /pecas/{id}: disponivel=3. Recuse o orcamento no Billing. Aguarde reparo CANCELADO, reserva LIBERADA, OS CANCELADA e disponivel=5. Repetir a recusa retorna 400 e nao devolve pecas novamente. Mostre a sequencia no historico da OS.

## Falha tecnica e recuperacao (2 minutos)

Use o ambiente isolado do BDD, sem derrubar RabbitMQ da AWS durante o fluxo principal. No Execution, rode `./mvnw.cmd -B -f tests/fluxo-distribuido/pom.xml verify` e mostre o relatorio Cucumber em tests/fluxo-distribuido/target/cucumber. O teste exercita os servicos reais em containers e um provedor de pagamento simulado. Explique a indisponibilidade, persistencia na outbox, reentrega e idempotencia. O provedor simulado nao comprova compra real no Mercado Pago. As revisoes dos tres repositorios devem corresponder aos SHAs registrados pelo CI distribuido.

## CI/CD (2 minutos)

Abra a execucao de CI do mesmo commit implantado: testes, JaCoCo >=80%, PMD e manifests. Depois mostre CD verde, Publish immutable commit image, Deploy verified image e Remove temporary runner access. A imagem usa o SHA do commit. Mostre rollout concluido e pods Ready. Nao rode Terraform durante a gravacao.

## Observabilidade (2 minutos)

No Datadog, filtre env:fase4 e os servicos mecanica-os-service, mecanica-execution-service e mecanica-billing-service. Abra APM/Traces e selecione o intervalo do ensaio. Mostre spans HTTP e RabbitMQ e a ligacao produtor/consumidor quando presente. Em Logs, busque o ordemId/reparoId ou correlationId do ensaio. Mostre a correlacao log/trace e metricas de requests/erros/latencia. Logs separados por servico ou agentes Running, isoladamente, nao comprovam rastreamento distribuido.

## Limite externo conhecido

Mercado Pago WCS-59172: checkout_url abre, mas compra foi rejeitada com invalid_users_types_error. Nao usar sandbox_init_point da preferencia no fluxo Orders. Ate pagamento externo confirmado, apresente o teste automatizado como simulado e declare a pendencia; nao afirme fluxo real completo nem marque cobranca como paga no banco. SMTP foi recebido localmente em 16/11; o ambiente AWS precisa de configuracao SMTP propria para repetir esse envio.

Reserve 1 minuto para arquitetura e justificativa; total planejado 14 minutos. Grave apenas apos ensaiar. Depois insira a URL publica/nao listada no PDF de entrega.