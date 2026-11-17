# Arquitetura da Fase 3

## Visão de componentes

```mermaid
flowchart LR
    Client[Cliente / Swagger / Postman]

    subgraph AWS[Amazon Web Services - us-east-1]
        Gateway[AWS API Gateway HTTP API]
        Lambda[Lambda Auth - Java 21]
        ECR[Amazon ECR]
        LB[Elastic Load Balancer]
        subgraph EKS[Amazon EKS]
            API[Spring Boot API]
            HPA[HPA]
            MetricsServer[Metrics Server]
            Agent[Datadog Agent]
        end
        RDS[(Amazon RDS PostgreSQL)]
        S3[(S3 - Terraform states)]
    end

    GitHub[GitHub Actions] -->|Terraform| S3
    GitHub -->|imagem| ECR
    GitHub -->|deploy| EKS
    Client --> Gateway
    Gateway -->|POST /auth| Lambda
    Gateway -->|demais rotas| LB
    LB --> API
    Lambda --> RDS
    API --> RDS
    ECR --> API
    MetricsServer --> HPA
    HPA --> API
    API -->|métricas, logs e traces| Agent
    Agent --> Datadog[Datadog SaaS]
```

O API Gateway é o ponto de entrada. A Lambda valida CPF/CNPJ, consulta o cliente no RDS e assina um JWT com a chave privada RSA. A aplicação conhece somente a chave pública e valida assinatura, emissor e expiração. O banco é externo ao EKS e suas migrations são executadas pelo Flyway.

## Sequência de autenticação

```mermaid
sequenceDiagram
    actor Cliente
    participant APIGW as API Gateway
    participant Lambda as Lambda Auth
    participant RDS as PostgreSQL RDS
    Cliente->>APIGW: POST /auth {cpfCnpj}
    APIGW->>Lambda: Evento HTTP
    Lambda->>Lambda: Valida CPF/CNPJ
    Lambda->>RDS: Consulta cliente e status
    RDS-->>Lambda: Cliente ativo
    Lambda->>Lambda: Assina JWT RS256
    Lambda-->>APIGW: 200 + accessToken
    APIGW-->>Cliente: JWT
```

## Sequência de abertura de ordem

```mermaid
sequenceDiagram
    actor Cliente
    participant APIGW as API Gateway
    participant API as API no EKS
    participant RDS as PostgreSQL RDS
    Cliente->>APIGW: POST /ordens-servico + Bearer JWT
    APIGW->>API: Encaminha requisição
    API->>API: Valida JWT RS256
    API->>RDS: Valida cadastros e persiste OS
    RDS-->>API: Ordem criada
    API-->>APIGW: 201 Created
    APIGW-->>Cliente: Dados da ordem
```

## Modelo relacional

```mermaid
erDiagram
    CLIENTE ||--o{ VEICULO : possui
    CLIENTE ||--o{ ORDEM_DE_SERVICO : solicita
    VEICULO ||--o{ ORDEM_DE_SERVICO : recebe
    OPERADOR ||--o{ ORDEM_DE_SERVICO : atende
    ORDEM_DE_SERVICO ||--o{ PECA_ORDEM : contem
    PECA ||--o{ PECA_ORDEM : compoe
    ORDEM_DE_SERVICO ||--o{ SERVICO_ORDEM : contem
    SERVICO ||--o{ SERVICO_ORDEM : compoe
    PECA ||--|| ESTOQUE : possui
```

O PostgreSQL foi escolhido por oferecer transações ACID, integridade referencial, índices e bom suporte no RDS. As tabelas associativas preservam quantidade, preço cobrado e datas da execução, evitando que alterações futuras no catálogo modifiquem o histórico da ordem. A justificativa formal, as cardinalidades e os ajustes realizados estão em [relational-model.md](relational-model.md).

Os artefatos editáveis de descoberta que originaram esses fluxos estão em [domain/](domain/README.md).

## Observabilidade

- Spring Boot Actuator fornece healthchecks e métricas HTTP/JVM.
- `/actuator/prometheus` expõe métricas para coleta pelo Datadog OpenMetrics.
- Logs são emitidos em JSON e possuem `correlationId` recebido ou gerado por requisição.
- O tracer Java é injetado pelo Datadog Admission Controller e correlaciona
  spans HTTP/JDBC com os logs.
- O HPA utiliza CPU e memória para escalar os pods.
- Dashboards e alertas do Datadog devem cobrir latência, uptime, recursos do Kubernetes, erros de integração, volume de ordens e duração dos status.
