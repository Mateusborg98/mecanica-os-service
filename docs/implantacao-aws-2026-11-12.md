# Implantacao AWS — 12/11/2026

Saldo informado pelo responsavel antes da criacao: US$ 50. Implantacao autorizada nesta sessao.

## Resultado verificado

- EKS `mecanica-fase4` 1.36 criado em us-east-1; acesso Kubernetes validado.
- Node group `demonstracao`: um t3.large, no Ready.
- EBS CSI e metrics-server instalados; pods dos add-ons Running.
- Tres repositorios ECR com tags imutaveis e politicas de retencao criados.
- PostgreSQL OS, PostgreSQL Execution, MongoDB e RabbitMQ: quatro pods 1/1 Running. RabbitMQ teve um reinicio inicial e ficou Ready na verificacao seguinte.
- Quatro PVCs Bound, cada um com 10 GiB gp3, com retencao Retain.
- Secrets de bancos/broker gerados em memoria e criados no cluster, sem senhas em arquivos ou logs.
- Namespace datadog criado; Secret e agente ainda pendentes de cadastro pelo responsavel.
- APIs e CD remoto ainda nao implantados; nao confundir plataforma pronta com fluxo de negocio validado.

## Correcao da restricao Academy

A primeira tentativa de node group com t3.xlarge foi recusada. As instrucoes fornecidas pelo responsavel permitem tamanhos ate large. Corrigido main.tf para t3.large; novo plano: 3 adicoes, nenhuma alteracao ou destruicao. Apply concluido com sucesso, incluindo os dois add-ons.

A LabRole existente foi preservada: a criacao do no e dos volumes funcionou apos corrigir o tamanho, sem alteracoes em IAM. As roles dedicadas do laboratorio foram inventariadas, mas nao substituidas no cluster existente.

## Retomada e custos

Preservar state e parametros locais ignorados pelo Git. Cluster, no e volumes ja consomem credito. A estimativa antiga de xlarge no README esta identificada como historica; revalidar duracao e consumo no Academy. O no atual tem menos memoria; observar recursos antes de implantar as tres APIs e Datadog.

Executar configurar-datadog-cluster.ps1 no terminal que contem DD_API_KEY; instalar o chart somente depois. Ainda faltam Secrets das APIs, emissor JWT, credenciais externas, acesso do runner ao endpoint restrito, CI/CD e evidencias Datadog. Encerramento deve revisar Terraform e volumes retidos; parar o Lab nao comprova fim das cobrancas.
## Datadog instalado

Chart datadog/datadog 3.269.1 instalado via Helm, release datadog-agent, namespace datadog, site datadoghq.com. Secret fornecido pelo responsavel sem exposicao da chave.

Verificacao: Cluster Agent 1/1 Running e Agent 3/3 Running, sem reinicios. Status do Agent informa API Key valida e RetryQueueSize 0. Consumo observado do no neste momento: cerca de 22% de memoria e 23% de CPU; nao representa teste de carga com as APIs.

Logs/traces de negocio e visualizacao no painel ainda precisam de validacao apos implantacao das aplicacoes. Funcionalidades opcionais desabilitadas (process collector/system probe/installer) nao sao requisitos deste ambiente. Cluster Agent possui uma replica, conforme topologia temporaria sem alta disponibilidade.
