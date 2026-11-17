# Arquitetura da Fase 4

Revisão: 16/11/2026. O desenho lógico representa o código atual. O deploy local e dois cenários integrados na AWS foram validados; veja [evidências](validacao-integrada-aws-2026-11-16.md). CD remoto do OS validado em 17/11; SMTP local recebido em 16/11; pagamento externo pendente no WCS-59172. A [arquitetura da Fase 3](architecture.md) permanece como histórico.

## Contexto

O cliente acompanha a OS e decide o orçamento por rotas públicas. O operador usa JWT para administrar o atendimento. O Billing integra o Mercado Pago e envia o link de aprovação por SMTP. Um checkout criado não comprova pagamento.

```mermaid
flowchart LR
  Cliente -->|Acompanhamento e aprovação| Sistema[Sistema da oficina]
  Operador -->|Operações administrativas com JWT| Sistema
  Sistema -->|Checkout e consulta de pagamento| MP[Mercado Pago]
  MP -->|Webhook assinado| Sistema
  Sistema -->|Link do orçamento| SMTP[Servidor SMTP]
  SMTP -->|E-mail| Cliente
```

## Serviços e bancos

```mermaid
flowchart LR
  Cliente -->|HTTP público| OS[OS Service]
  Cliente -->|HTTP público| Billing[Billing Service]
  Operador -->|HTTP com JWT| OS
  Operador -->|HTTP com JWT| Billing
  Operador -->|HTTP com JWT| Execution[Execution Service]
  OS --> PGOS[(PostgreSQL OS)]
  Billing --> Mongo[(MongoDB Billing)]
  Execution --> PGEX[(PostgreSQL Execution)]
  OS <-->|Eventos e confirmações| Rabbit[RabbitMQ]
  Billing <-->|Eventos e confirmações| Rabbit
  Execution <-->|Eventos e confirmações| Rabbit
  Billing -->|HTTPS| MP[Mercado Pago]
  MP -->|Webhook HTTPS| Billing
  Billing -->|SMTP com TLS| SMTP[Servidor de e-mail]
```

| Serviço | Responsabilidade e justificativa |
|---|---|
| OS | Cadastro de clientes, veículos e operadores; abertura, acompanhamento, histórico e entrega. Mantém a visão do atendimento sem assumir execução ou cobrança. |
| Execution | Diagnóstico, serviços, peças, reservas, fila e reparo. PostgreSQL permite transações locais para estoque e compensações. |
| Billing | Orçamento, decisão, notificações, cobrança e integração financeira. MongoDB mantém os documentos e os registros de processamento; cada operação usa as garantias implementadas, sem presumir transação distribuída. |

Cada serviço tem código, Dockerfile, manifests e pipeline próprios. Nenhum lê o banco de outro. RabbitMQ e cluster são infraestrutura compartilhada; os bancos e volumes são separados. Java 21/Spring Boot uniformizam implementação e operação; RabbitMQ desacopla a evolução dos estados.

## Componentes

Controllers e consumidores traduzem entradas para casos de uso. Casos de uso aplicam regras de domínio e usam contratos de persistência, publicação e integração. Adaptadores implementam esses contratos. JWT protege operações administrativas; a configuração de segurança preserva as rotas públicas previstas no contrato.

Outbox retoma publicações; consumidores idempotentes evitam repetir efeitos. A entrega de mensagens é pelo menos uma vez. DLQ exige diagnóstico e reprocessamento auditável. O envio SMTP também pode repetir após uma falha entre a aceitação pelo servidor e a confirmação no banco.

## Saga por coreografia

```mermaid
sequenceDiagram
  participant O as OS
  participant E as Execution
  participant B as Billing
  participant C as Cliente
  participant M as Mercado Pago
  O->>E: Solicitação de reparo via RabbitMQ
  E->>B: Diagnóstico via RabbitMQ
  B-->>C: Link do orçamento por SMTP
  C->>B: Aprovação pública
  B->>E: Decisão via RabbitMQ
  B->>O: Decisão e valor via RabbitMQ
  Note over E: Fila, início e conclusão do reparo
  E->>O: Progresso e conclusão via RabbitMQ
  E->>B: Conclusão via RabbitMQ
  C->>B: Solicitar checkout
  B->>M: Criar ordem
  M-->>B: Webhook após pagamento
  B->>M: Consultar e validar pagamento
  B->>O: Confirmação via RabbitMQ
  Note over O: Entrega somente após conclusão e pagamento
```

A escolha evita um coordenador central e mantém cada transação no serviço responsável. Estados podem divergir temporariamente até o processamento das mensagens. Recusa/cancelamento anterior ao início libera reservas; falhas temporárias são repetidas; falhas definitivas permanecem pendentes para correção ou cancelamento explícito. Depois do reparo físico usa-se recuperação para frente. Não há estorno/chargeback automático. Consulte [Saga](saga.md) e [política de falhas](politica-falhas-saga.md).

## Implantação preparada

```mermaid
flowchart TB
  GH[GitHub Actions por serviço] -->|Build, testes e qualidade| ECR[ECR por serviço]
  GH -->|Deploy do SHA validado| APIs
  subgraph AWS[AWS Academy us-east-1 - implantação pendente]
    subgraph EKS[EKS compartilhado]
      APIs[Deployments OS, Billing e Execution]
      APIs --> Dados[PostgreSQL OS, PostgreSQL Execution e MongoDB]
      APIs <--> MQ[RabbitMQ]
      Dados --> Volumes[Volumes EBS independentes]
      MQ --> Volumes
      DD[Agente Datadog]
      APIs --> DD
    end
  end
  ECR -->|Imagens imutáveis| APIs
  DD --> Datadog[Datadog externo]
```

Topologia temporária de demonstração: um nó, Services internos, acesso por port-forward, sem alta disponibilidade. ConfigMap e Secrets fornecem configuração; valores sensíveis não ficam no repositório. Provisionamento, CD real e rastreamento Datadog ainda precisam de evidência. Detalhes e custos datados: [infraestrutura](../infra/fase4/README.md).
