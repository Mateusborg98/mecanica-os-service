# RFC 0001 - AWS e PostgreSQL gerenciado

## Proposta

Usar AWS API Gateway, Lambda Java 21, Amazon EKS, Amazon RDS PostgreSQL e Amazon ECR. Provisionar RDS e EKS com Terraform em repositórios separados.

## Motivação

A AWS Academy disponibiliza os serviços necessários no mesmo ambiente. PostgreSQL é compatível com o modelo relacional existente, oferece integridade e transações, e no RDS transfere tarefas operacionais de banco para um serviço gerenciado.

## Alternativas consideradas

- Banco dentro do Kubernetes: mais simples localmente, mas não atende ao requisito de banco gerenciado.
- JWT simétrico: exige compartilhar o segredo de assinatura com a API.
- Cluster local Kind: útil para desenvolvimento, mas não representa deploy automático na nuvem.
