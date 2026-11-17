# Pagamento e entrega da OS

O Billing publica `PagamentoConfirmado` v1 no exchange direto `mecanica.events`,
rota `pagamento.confirmado.v1`. A OS consome `os.pagamento.v1` quando
`MESSAGING_ENABLED=true`; configure RABBITMQ_HOST/PORT/USERNAME/PASSWORD.
Sem consumo habilitado, a entrega permanece bloqueada até existir confirmação persistida.

A migration V3 cria `pagamento_ordem`. O consumidor bloqueia a linha da OS,
valida sua existência e o valor integral em BRL e grava o comprovante em transação.
Reentregas idênticas são aceitas sem duplicação; eventos conflitantes são rejeitados.
O ACK ocorre depois do retorno da transação. Erros de infraestrutura têm três
tentativas; mensagens rejeitadas seguem para `os.pagamento.v1.dlq` via `mecanica.dlx`.
Corrija a causa antes de republicar a mensagem original; não altere IDs ou valores.

A entrega exige pagamento integral e status FINALIZADA. A verificação e a alteração
da OS participam da mesma transação, mantendo o bloqueio até o commit.
Não há endpoint público para marcar uma OS como paga.

## Limites e validação restante

- O evento deve vir do Billing em um broker de acesso restrito; não é prova de pagamento
  recebida diretamente do navegador ou do Mercado Pago.
- A OS ainda não persiste a associação prévia com o reparo/orçamento do Execution.
  Valida o hash de cobrança por reparo, o ID da OS e o valor; a associação completa
  e a sincronização automática da finalização precisam ser implementadas.
- OS desconhecida ou valor incompatível vai para DLQ e exige correção/reprocessamento.
- Os IDs de demonstração anteriores não representam necessariamente uma OS existente.
- Validar o fluxo completo com PostgreSQL, RabbitMQ e pagamento de teste no Mercado Pago
  antes de considerar a integração pronta para entrega. Estornos não estão cobertos.

## Verificação local em 27/10/2026

Maven clean verify: 114 testes, sem falhas, erros ou testes ignorados; cobertura de linhas de 98,07%. Os testes novos cobrem a persistência em H2 (modo PostgreSQL), concorrência, rollback, duplicidade, contrato JSON e a regra de entrega. Não foi executado neste lote o fluxo integrado com PostgreSQL, RabbitMQ e Mercado Pago.
