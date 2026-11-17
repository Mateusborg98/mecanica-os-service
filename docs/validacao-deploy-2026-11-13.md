# Validacao de implantacao — 13/11/2026

## Implantacao inicial concluida

OS, Billing e Execution implantados no EKS mecanica-fase4, us-east-1, com imagens no ECR. Build de copias isoladas dos commits aprovados na CI; alteracoes locais de documentacao nao foram incorporadas nas imagens. Nao houve merge de PR.

| API | Commit da imagem |
|---|---|
| OS | 21fb0c541adc75da95a125ee1b1889f37b23e860 |
| Billing | 4ba6caa9379d6771eef092b7663f2bafb5766a80 |
| Execution | e6849e04bcd5d2aac895a27c52659ca90f5c3096 |

Os testes desses commits foram aprovados no GitHub antes da construcao. O Dockerfile empacota com testes desabilitados; nao se trata de uma nova execucao da suite. As imagens usam tags imutaveis por SHA.

## Evidencias observadas

- Tres APIs Ready; HTTP /actuator/health/readiness retornou UP em cada uma.
- PostgreSQL OS, PostgreSQL Execution, MongoDB e RabbitMQ 1/1 Running.
- Acesso administrativo sem JWT bloqueado: OS 403, Billing 401, Execution 401.
- Datadog Cluster Agent e agentes nos dois nos Running; agente no no das APIs registrou traces/spans recebidos, LogsSent maior que zero, RetryQueueSize zero e nenhuma falha de submissao no momento da consulta.
- A visualizacao no painel e a correlacao de um atendimento completo ainda precisam ser demonstradas; chamadas de health nao comprovam Saga ou pagamento ponta a ponta.

## Ajustes realizados

O no unico reservava 1400m de CPU antes das APIs; as tres APIs acrescentam 750m, excedendo os cerca de 1930m alocaveis. Node group ajustado via Terraform para dois t3.large, ambos permitidos pelo Academy. Nenhum banco ou volume foi substituido. Isso aumenta custo; revisar saldo e encerrar o ambiente quando as evidencias forem capturadas.

Corrigido o script configurar-secrets-apis.ps1 para remover aspas externas do arquivo env da chave publica JWT. Os tres Secrets foram atualizados somente nesse campo. O rollout inicial da OS falhou com a chave entre aspas; depois da correcao os novos pods iniciaram sem reinicios na verificacao.

A chave publica e do emissor local de demonstracao (mecanica-auth); nao constitui validacao da autenticacao Lambda da Fase 3. Credenciais de banco/broker vieram dos Secrets existentes; Mercado Pago foi cadastrado pelo responsavel. Nenhuma chave foi versionada.

## Pendencias

Esta implantacao foi comandada localmente, com commits aprovados na CI. Ainda NAO comprova o CD remoto independente do GitHub. Resolver acesso dos runners ao endpoint EKS restrito, atualizar credenciais temporarias dos environments e concluir revisao/merge antes da demonstracao de CD.

Continuam pendentes: fluxo integrado em AWS com JWT, pagamento Mercado Pago, SMTP, evidencias visuais Datadog, video e PDF final. APIs usam Services internos; nao foi criado endpoint publico. Usar port-forward para validacao.