# Entrega da Fase 4 — fechamento em 17/11/2026

Atualização: 11/11/2026. Implementação e validação local não equivalem a implantação e evidência externa. AWS Academy: orçamento informado de US$ 50, us-east-1; sessão expirada, avanço sem AWS solicitado pelo responsável.

## Implementado e verificado

- [x] OS, Billing e Execution em repositórios próprios, com PostgreSQL para OS/Execution e MongoDB para Billing; nenhum acesso cruzado aos bancos.
- [x] Abertura, progresso, consulta e histórico da OS; orçamento, aprovação/recusa, checkout/webhook e pagamento antes da entrega.
- [x] Fila administrativa ordenada dos reparos aprovados, diagnóstico, reservas, início e conclusão.
- [x] Envio SMTP persistente e página pública de decisão; testes com servidor SMTP local e MongoDB real. Recebimento confirmado em 10/11; nova tentativa de 11/11 pendente por autenticação SMTP.
- [x] RabbitMQ, outbox, idempotência, DLQ e ferramenta Java de reprocessamento auditável.
- [x] Compensação antes da execução, incluindo cancelamento antes da criação do reparo e proteção contra solicitação tardia.
- [x] Política explícita de recuperação para frente após execução física; não há estorno/chargeback automático.
- [x] Testes e cobertura acima de 80% por serviço; PMD como análise estática no CI.
- [x] CI independente e BDD distribuído automatizado no Execution; três checks obrigatórios no Execution e dois em OS/Billing.
- [x] Main protegida por PR, checks obrigatórios, branch atualizada e conversas resolvidas. Sem aprovação adicional obrigatória por ser projeto individual.
- [x] Manifests próprios e pipelines CD para Kubernetes; infraestrutura compartilhada documentada.
- [x] Inventário inicial AWS e estimativa de custo realizados; a topologia preparada usa EKS e um nó, sem NAT/RDS/LB. Confirmar saldo e inventário novamente antes de provisionar.

## Validar antes da entrega

- [x] CI das alterações de 09/11 aprovada; commits e links na matriz de requisitos.
- [x] BDD distribuído com três cenários aprovado, incluindo falha do broker e cancelamento anterior à criação.
- [ ] Revalidar SMTP: um recebimento confirmado em 10/11; tentativa de 11/11 adiada após erro Gmail 535.
- [ ] PENDENTE, adiado pelo responsável em 11/11 — compra real de teste Mercado Pago: webhook, consulta ao provedor, cobrança paga e entrega liberada. Roteiro em Billing/docs/roteiro-pagamento-real.md.
- [ ] Renovar sessão AWS Academy e atualizar credenciais temporárias nos ambientes GitHub, sem versionar valores.
- [ ] Confirmar acesso Datadog e provisionar o ambiente com orçamento/duração controlados.
- [ ] Demonstrar deploy automatizado independente dos três serviços em Kubernetes.
- [ ] Verificar saúde, autenticação, fluxo completo, falha/compensação e recuperação no ambiente entregue.
- [ ] Capturar logs, métricas e correlação no Datadog.

## Pacote final

- [x] Arquitetura da Fase 4 e collections manuais das operações principais preparadas, incluindo SMTP, fila e limites de compensação.
- [x] Requisitos e entregáveis conferidos contra o PDF; matriz separa código, evidências e pendências.
- [ ] Conferir links dos repositórios, instruções e OpenAPI.
- [ ] Preparar vídeo de até 15 minutos e verificar acesso.
- [ ] Preparar PDF com participantes, links e evidências.
- [ ] Remover dados sensíveis das evidências e encerrar recursos AWS após demonstração.

Referências: `infra/fase4/README.md`, [política da Saga](politica-falhas-saga.md), [cancelamento](cancelamento-integrado.md). Documentos com data registram validações daquele momento; não substituem os relatórios dos commits finais.
## Documentação de fechamento

- [Arquitetura atual](architecture-fase-4.md).
- [Matriz de requisitos e evidências](matriz-requisitos-fase-4.md).
- [Roteiro do vídeo](roteiro-demonstracao-fase-4.md).
- [Texto-base do PDF, ainda incompleto](texto-base-entrega-fase-4.md).

## Avanco em 13/11/2026

[Primeira implantacao AWS](validacao-deploy-2026-11-13.md): tres APIs Ready, bancos e broker saudaveis, Datadog recebendo logs e traces. Deploy inicial executado localmente; CD GitHub e fluxo externo continuam pendentes. Cluster agora usa dois t3.large.
