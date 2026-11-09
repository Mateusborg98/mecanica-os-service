# Validação da preparação de infraestrutura — 06/11/2026

## Escopo

Manifests dos três serviços, plataforma compartilhada, Terraform EKS/ECR, valores Datadog e workflows CI/CD. Esta evidência não representa implantação em AWS.

## Resultados

| Verificação | Resultado |
|---|---|
| AWS STS | Sessão Academy válida em us-east-1 |
| Inventário inicial consultado | Sem EKS, EC2, RDS, NAT Gateway, ELBv2 ou EBS; VPC padrão e LabRole disponíveis |
| Terraform 1.15.8 fmt/validate | Aprovados; provider AWS 6.72.0 fixado no lock file |
| Terraform plan | 13 adições, zero alterações, zero destruições; sem apply |
| Kustomize + kubeconform 0.7.0, schemas Kubernetes 1.36.0 | 25 recursos válidos, zero erros, zero ignorados |
| actionlint 1.7.7 | Seis workflows CI/CD válidos |
| Helm 3.19.0, checksum SHA256 verificado | Chart Datadog 3.269.1 renderizado offline após download |
| Schemas do Datadog renderizado | 26 recursos válidos, zero erros, zero ignorados |
| Git diff --check | Sem erros nos três repositórios |

O plano usa uma allowlist fictícia de documentação para não liberar o cluster acidentalmente. Não é um plano de implantação aprovado: substituir pelo CIDR real e resolver a saída do runner antes de gerar o plano final.

O chart Datadog está configurado sem Operator, CSI Datadog, CRDs de instrumentação ou autoscaling de workloads; usa Agent, Cluster Agent e injeção Java por anotação. A observabilidade ainda precisa ser comprovada no cluster e na conta Datadog.

## Limites e próximos passos

- Schemas validam estrutura; não comprovam inicialização das aplicações, probes, permissões AWS de criação, provisionamento EBS, compatibilidade do admission controller ou conectividade entre serviços.
- CI/CD ainda não executados remotamente. Configurar Environment/Secrets e proteção de main em cada repositório.
- Secrets reais, emissor JWT e credenciais Mercado Pago/Datadog ainda precisam ser configurados e conferidos.
- Nenhuma imagem publicada, recurso AWS criado, banco migrado ou volume removido.
- Não houve alteração de código Java nesta etapa; as suítes Java não foram repetidas. A próxima validação integrada deve incluir rollout Kubernetes, persistência após reinício e o fluxo distribuído completo.
- Custos são estimativas do README da infraestrutura, sem garantia de teto automático. Encerramento deve conferir também volumes retidos e imagens ECR.
