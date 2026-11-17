# Tech Challenge — Fase 4

**Rascunho para o PDF do portal. Não enviar com os campos pendentes.**

## Participantes

Mateus Borgonovi Francisco Drago - RM370507.

## Repositórios e vídeo

- OS: https://github.com/Mateusborg98/mecanica-os-service
- Billing: https://github.com/Mateusborg98/mecanica-billing-service
- Execution: https://github.com/Mateusborg98/mecanica-execution-service
- Vídeo público ou não listado, até 15 minutos: **INSERIR APÓS GRAVAÇÃO**.

## Arquitetura e justificativa

A aplicação foi dividida conforme as responsabilidades do atendimento. OS mantém abertura, acompanhamento, histórico e entrega. Execution concentra diagnóstico, estoque, reservas e reparo. Billing concentra orçamento, decisão, envio de notificações e pagamento. A separação permite implantar os serviços de forma independente e evita compartilhamento de tabelas.

Java 21 e Spring Boot uniformizam o desenvolvimento. PostgreSQL atende OS e Execution, incluindo transações locais de estoque. MongoDB armazena documentos e registros de processamento do Billing. RabbitMQ integra os serviços de forma assíncrona. APIs REST atendem as operações síncronas. O Billing utiliza Mercado Pago e SMTP para as integrações externas.

Inserir no PDF o diagrama de serviços e bancos de [arquitetura da Fase 4](architecture-fase-4.md). A topologia AWS foi implantada no EKS us-east-1; o CD do OS foi validado em 17/11/2026.

## Saga Pattern

Adotou-se coreografia: cada participante confirma sua operação local e comunica os resultados por eventos. Outbox, confirmação de publicação, idempotência e DLQ permitem recuperação sem uma transação distribuída entre bancos. Recusa ou cancelamento anterior ao início compensa reservas e cancela o atendimento. Falhas definitivas aguardam correção e reprocessamento ou cancelamento explícito. Depois do reparo físico, a recuperação segue adiante para cobrança e pagamento; não existe desfazimento físico nem estorno automático.

## Validação e limites

Testes unitários, de integração e BDD cobrem o fluxo distribuído e compensações. JaCoCo exige pelo menos 80% por serviço e PMD verifica qualidade no CI. O BDD utiliza provedor financeiro simulado. Recebimento SMTP local confirmado em 16/11/2026. CD do OS concluido em 17/11/2026. A compra externa de teste Mercado Pago permanece pendente no chamado WCS-59172. Consulte a [matriz atualizada](matriz-requisitos-fase-4.md) para os links finais de CI/CD e observabilidade.
