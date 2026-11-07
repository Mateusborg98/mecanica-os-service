# Ambiente de demonstração — Fase 4

Destino: AWS Academy, `us-east-1`, saldo informado de US$ 50. Esta pasta é a infraestrutura compartilhada dos três serviços, mantida no repositório OS para não criar um quarto repositório de aplicação. Os manifests e pipelines de cada API ficam no seu próprio repositório.

## Topologia proposta

- EKS 1.36 em suporte padrão, um Managed Node Group com um `t3.xlarge` (4 vCPU, 16 GiB), capacidade fixa de um nó.
- VPC e sub-redes públicas padrão preexistentes. Endpoint Kubernetes autenticado, com allowlist de CIDRs; endpoint privado também habilitado. Não há regras de entrada pública para as APIs ou bancos.
- Sem NAT Gateway, RDS ou Load Balancer. Os Services são internos. Acesso de demonstração por `kubectl port-forward` em localhost.
- PostgreSQL da OS, PostgreSQL do Execution, MongoDB do Billing e RabbitMQ em StatefulSets independentes, um volume EBS gp3 criptografado de 10 GiB para cada um.
- EBS CSI e metrics-server como add-ons; ECR separado por serviço, tags imutáveis por SHA do commit.
- Datadog instalado separadamente com Secret preexistente. Cada API tem identificação própria, logs e injeção do agente Java.

É um ambiente temporário de demonstração, sem alta disponibilidade. Uma falha no único nó interrompe todos os serviços até a recuperação. Volumes de dados persistem fora do disco do nó; isso não substitui backup. O nó permanece na mesma zona para permitir remontar os volumes EBS em uma substituição. Não atualizar o node group durante a gravação: uma atualização gerenciada pode iniciar capacidade temporária extra.

## Estimativa de custo

Premissas: Linux On-Demand em us-east-1, um nó, um IPv4 público, 70 GiB gp3 (30 do nó + 40 de dados), sem IOPS/throughput adicionais. Valores de referência consultados em 06/11/2026; revalidar antes de aplicar.

| Item | Referência | Aproximação por hora |
|---|---:|---:|
| EKS em suporte padrão | US$ 0,10/h | US$ 0,1000 |
| t3.xlarge | US$ 0,1664/h | US$ 0,1664 |
| Um IPv4 público | US$ 0,005/h | US$ 0,0050 |
| 70 GiB gp3 | US$ 0,08/GB-mês, mês aproximado de 730 h | US$ 0,0077 |
| Total base estimado | | **US$ 0,2791/h** |

48 horas: aproximadamente US$ 13,40. 72 horas: US$ 20,09. 120 horas: US$ 33,49. **Planejar até 72 horas de ambiente criado**, reservar até US$ 10 para armazenamento de imagens, transferência, variações e tentativas, mantendo aproximadamente US$ 20 de margem do saldo informado. É estimativa, não limite automático de cobrança.

Não inclui assinatura/ingestão do Datadog, tráfego de saída, ECR, snapshots, recursos anteriores ou capacidade transitória durante manutenção. A disponibilidade do Datadog depende da conta/plano do usuário. O modo T3 Standard evita cobrança por créditos excedentes, mas pode limitar CPU em carga sustentada. O HPA escala pods, não nós; validar consumo real e pods Pending antes de gravar.

Fontes oficiais: [EKS](https://aws.amazon.com/eks/pricing/), [EC2 T3](https://aws.amazon.com/ec2/instance-types/t3/), [IPv4](https://aws.amazon.com/vpc/pricing/), [EBS](https://aws.amazon.com/ebs/pricing/).

## Terraform: preparação e plano

Executar nesta pasta, com credenciais temporárias válidas e Terraform >= 1.10. Não copiar state de outra fase. O state é local e ignorado pelo Git; mantenha uma cópia protegida durante todo o ciclo de vida. Não executar apply em outra máquina sem transferir esse state de forma segura.

1. Copiar `terraform.tfvars.example` para `terraform.tfvars` (ignorado).
2. Informar o ARN IAM da role operadora, não o ARN STS da sessão. O laboratório consultado usa a role `voclabs`.
3. Substituir o CIDR de documentação pelo IP público real do operador com `/32`. Para CD, permitir também a saída do runner. Runners hospedados do GitHub têm IP variável: decidir entre runner com saída controlada ou acesso público autenticado mais amplo, explicitamente, antes do primeiro deploy. O exemplo não libera acesso global.
4. Executar:

```powershell
terraform init
terraform fmt -check
terraform validate
terraform plan -out=lab.tfplan
```

Revisar o plano e o custo antes de `terraform apply lab.tfplan`. A preparação dos arquivos não cria recursos. Confirmar que não há substituição/destruição de recursos existentes. O plano local de validação usa CIDR fictício e não deve ser aplicado.

O laboratório reutiliza `LabRole` para EKS e nó; não cria ou amplia políticas IAM. O EBS CSI usa as credenciais da role do nó via IMDSv2 (hop limit 2). Isso depende das permissões preexistentes do laboratório e é uma concessão de isolamento: pods com acesso ao IMDS podem herdar a role do nó. Para produção, usar identidade dedicada do CSI e bloquear IMDS para aplicações. `terraform plan` não comprova que o Academy autoriza todas as operações de criação ou que o driver consegue provisionar discos; validar isso no primeiro deploy.

## Bootstrap dos dados e Secrets

Após o cluster estar ativo:

```powershell
aws eks update-kubeconfig --name mecanica-fase4 --region us-east-1
kubectl apply -f platform/namespace.yaml
kubectl get nodes
kubectl -n kube-system get pods
```

Criar os Secrets abaixo em `mecanica-fase4` a partir de arquivos locais protegidos, fora do Git. Usar senhas próprias do ambiente, sem reutilizar as senhas do Compose. Não imprimir os valores em logs, comandos salvos ou vídeos.

| Secret | Chaves obrigatórias |
|---|---|
| postgres-os-secret | POSTGRES_PASSWORD |
| postgres-execution-secret | POSTGRES_PASSWORD |
| mongodb-secret | MONGO_INITDB_ROOT_USERNAME, MONGO_INITDB_ROOT_PASSWORD |
| rabbitmq-secret | RABBITMQ_DEFAULT_USER, RABBITMQ_DEFAULT_PASS |
| mecanica-os-service-secret | SPRING_DATASOURCE_PASSWORD, JWT_PUBLIC_KEY, RABBITMQ_USERNAME, RABBITMQ_PASSWORD |
| mecanica-execution-service-secret | DATABASE_PASSWORD, JWT_PUBLIC_KEY, RABBITMQ_USERNAME, RABBITMQ_PASSWORD |
| mecanica-billing-service-secret | MONGODB_URI, JWT_PUBLIC_KEY, RABBITMQ_USERNAME, RABBITMQ_PASSWORD, MERCADO_PAGO_ACCESS_TOKEN, MERCADO_PAGO_WEBHOOK_SECRET |

As senhas PostgreSQL de cada API devem corresponder ao seu banco; as credenciais RabbitMQ devem corresponder ao broker. A URI MongoDB deve apontar para `mongodb:27017/mecanica_billing?authSource=admin`, com usuário e senha codificados para URI. Neste laboratório, a credencial administrativa Mongo é exclusiva do banco Billing; antes de produção, criar usuário de aplicação com permissões restritas.

`JWT_PUBLIC_KEY` deve ser a chave pública do emissor real, igual nos três serviços. Para arquivo env, usar PEM em uma linha com `\n` literais (suportado pelo loader). `JWT_ISSUER` nos ConfigMaps deve coincidir com o emissor. Esta infraestrutura não provisiona um serviço de login: o emissor herdado da Fase 3 ainda precisa ser conferido para os papéis CLIENTE, OPERADOR e ADMINISTRADOR.

Exemplo de carregamento, com arquivo previamente criado e protegido:

```powershell
kubectl -n mecanica-fase4 create secret generic postgres-os-secret --from-env-file=C:\caminho-local-protegido\postgres-os.env
```

Repetir para cada Secret usando os nomes e chaves da tabela. Não executar novamente `create` sobre um Secret existente; planejar atualização e rotação. Mudar o Secret não altera automaticamente a senha de um banco já inicializado em PVC. Os Secrets Kubernetes precisam de controle de acesso; base64 não é criptografia.

Depois:

```powershell
kubectl apply -k platform
kubectl -n mecanica-fase4 rollout status statefulset/postgres-os --timeout=5m
kubectl -n mecanica-fase4 rollout status statefulset/postgres-execution --timeout=5m
kubectl -n mecanica-fase4 rollout status statefulset/mongodb --timeout=5m
kubectl -n mecanica-fase4 rollout status statefulset/rabbitmq --timeout=5m
kubectl -n mecanica-fase4 get pvc,pods
```

Não prosseguir se PVCs estiverem Pending; verificar permissões do CSI e disponibilidade de zona. Imagens de bancos usam as mesmas famílias do Compose; fixar digests das imagens verificadas antes da gravação para reproduzir o ambiente.

## CD independente por serviço

Cada repositório possui `.github/workflows/cd.yml`, acionado pelo sucesso da sua CI em um push na `main`. O workflow usa exatamente o SHA validado, publica a imagem no ECR e espera o rollout da sua API. Não executa Terraform nem recria bancos. Reexecutar uma entrega do mesmo SHA reutiliza a tag imutável existente.

Antes de habilitar a entrega:

- Criar o GitHub Environment `fase4` em cada repositório e configurar `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN` como Secrets do ambiente. Credenciais Academy expiram e precisam ser atualizadas antes da execução.
- Exigir PR e check da CI na proteção da `main`. Conferir políticas do Environment conforme a conta GitHub.
- Usar a mesma role com access entry do cluster, ou criar uma access entry própria explicitamente para o deploy.
- Garantir que a allowlist do endpoint EKS permita a saída do runner e que bancos/Secrets estejam prontos.
- Conferir os três repositórios ECR criados pelo Terraform.

Os workflows estão preparados, mas só uma execução remota com imagem publicada e rollout bem-sucedido comprova o CD. O runner instala kubectl 1.36.0 e confere seu SHA256. As credenciais temporárias são utilizadas apenas durante o job; não criar chaves IAM permanentes para contornar as limitações do Academy.

## Datadog e acesso de demonstração

Instalar o chart oficial Datadog com Helm após criar um Secret `datadog-secret`, chave `api-key`, no namespace `datadog`. Conferir o `site` da conta no arquivo de valores e usar o chart 3.269.1, renderizado e validado localmente nesta etapa:

```powershell
helm repo add datadog https://helm.datadoghq.com
helm repo update
$chartVersion = '3.269.1'
helm upgrade --install datadog-agent datadog/datadog --version $chartVersion --namespace datadog --create-namespace -f datadog-values.yaml --wait --timeout 10m
```

Instalar antes das APIs para a injeção do agente Java ocorrer na criação dos pods. Os logs são coletados apenas nos containers anotados. A integração oferece infraestrutura/logs/APM; dashboards de negócio e correlação ponta a ponta ainda precisam ser verificados no Datadog. [Instalação oficial](https://docs.datadoghq.com/containers/kubernetes/installation/) e [injeção Java](https://docs.datadoghq.com/tracing/guide/tutorial-enable-java-admission-controller/).

Após os três deploys, usar terminais separados:

```powershell
kubectl -n mecanica-fase4 port-forward service/mecanica-os-service 8081:8081
kubectl -n mecanica-fase4 port-forward service/mecanica-billing-service 8082:8082
kubectl -n mecanica-fase4 port-forward service/mecanica-execution-service 8083:8083
```

Essas portas devem estar livres de APIs locais. O webhook externo exige HTTPS: o proxy restrito já existente no Billing pode encaminhar para o port-forward de 8082, expondo apenas `/webhooks/mercado-pago`. Configurar uma URL nova no provedor e mantê-la ativa durante o teste. Não executar o launcher local de Billing junto ao port-forward, pois ele ocuparia a mesma porta e poderia direcionar o teste ao banco local. Links do checkout e compra real continuam pendentes de validação pelo usuário.

## Encerramento e preservação

EKS cobra enquanto o cluster existir, mesmo sem pods. Encerrar a sessão Academy ou parar o nó não comprova encerramento de todas as cobranças.

1. Capturar evidências e exportar os dados necessários antes da desmontagem.
2. Inventariar PVCs/PVs e seus IDs EBS. A StorageClass usa `Retain`: exclusão de namespace/PVC não apaga os discos. Os discos de dados foram criados pelo Kubernetes e não pertencem ao state Terraform.
3. Revisar `terraform plan -destroy` com o mesmo state. ECR tem `force_delete=false`; repositórios com imagens exigem uma decisão explícita sobre preservar ou excluir as imagens antes de destruir.
4. Executar a desmontagem somente quando autorizada, e depois conferir EKS, EC2, EBS, ECR, IPs e quaisquer outros recursos criados. Volumes retidos continuam cobrando até a exclusão explícita após backup/validação.

Não há comando automático que apague volumes, imagens ou dados nesta entrega. Reavaliar o saldo diariamente enquanto o ambiente estiver criado.

## Evidência desta preparação

Consulte [validação local de infraestrutura](../../docs/validacao-infra-fase4-2026-11-06.md). Nenhum apply Terraform, instalação Helm, publicação de imagem ou deploy remoto foi executado nesta etapa.
