# ADR 0003 - Comunicação síncrona HTTP

- Status: aceito
- Contexto: autenticação e operações da oficina precisam responder ao usuário
  durante a mesma interação e o escopo não exige mensageria.
- Decisão: utilizar comunicação HTTP/JSON síncrona, com o API Gateway como
  entrada única e integração proxy para Lambda e Load Balancer do EKS.
- Consequências: o contrato é simples de demonstrar no Swagger/Postman e erros
  são devolvidos imediatamente. A disponibilidade do fluxo depende dos
  componentes chamados; timeouts, healthchecks e monitoramento são necessários.
  Eventos assíncronos poderão ser introduzidos futuramente para notificações,
  sem alterar o contrato principal desta fase.
