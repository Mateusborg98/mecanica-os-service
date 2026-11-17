# Validacao integrada AWS - 16/11/2026

Cluster mecanica-fase4, us-east-1. Dois nos t3.large e bancos persistentes recuperados apos reinicio do Learner Lab.

OS implantado: 738cb593feb52c658a180d175f179f0510a0759f. Correcao de abertura administrativa para o proprietario do veiculo, mantendo a identidade do cliente nas operacoes do proprio cliente. Testes e CI aprovados. ECR publicado e rollout confirmado. Deploy local; ainda nao comprova CD remoto.

Validador Java: mecanica-execution-service/tools/validacao-aws. Execucao final: codigo 0.

- Aprovacao: OS, diagnostico, reserva, orcamento, fila, inicio, conclusao, cobranca pendente de R$ 51 e bloqueio de entrega sem pagamento (422).
- Recusa: cancelamento do reparo e OS, liberacao da reserva e estoque de 3 para 5. Segunda decisao rejeitada (400), sem duplicar devolucao. Nenhuma cobranca criada (404).
- Primeira execucao: expectativa incorreta do validador para repeticao da recusa. Corrigida conforme contrato; ambos os cenarios repetidos com sucesso.

Limites: nao valida pagamento Mercado Pago, webhook financeiro, SMTP, login Lambda, falha do broker na AWS nem CD pelo GitHub. JWT de demonstracao em memoria. Registros ficticios preservados. Port-forwards encerrados automaticamente.

## Resultado

```json
{
  "validadoEm" : "2026-11-16T12:04:52.472638700Z",
  "pagamentoRealValidado" : false,
  "smtpValidado" : false,
  "correlationId" : "8e5c4163-d5c9-4e99-a1c9-58d52b68aff3",
  "cenarios" : [ {
    "reparoId" : "9d738dfc-c47b-4150-aa3d-de0cc54bceb0",
    "cenario" : "aprovado-aguardando-pagamento",
    "ordemId" : "7fae1c57-11a4-4bff-b565-c8c3d922da6a",
    "orcamentoId" : "11b7a1e2-59d3-3384-9d16-86d57a509a65",
    "pecaId" : "dfe1393c-e584-436a-8b6b-fef9b2ae8741"
  }, {
    "reparoId" : "ef923a7f-547a-4acc-be0a-2c6d380dfe90",
    "cenario" : "recusado-compensado",
    "ordemId" : "51351749-3de8-4270-a59f-e82d92b70047",
    "orcamentoId" : "e8452b28-738d-316b-9d74-10fda12f8e92",
    "pecaId" : "2413d12a-47ab-402b-a119-18209b4163b7"
  } ]
}
```
