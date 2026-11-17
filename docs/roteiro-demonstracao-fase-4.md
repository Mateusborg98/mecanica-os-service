# Roteiro de demonstração — até 15 minutos

Preparado em 11/11/2026. Reservar cerca de 14 minutos; fazer a gravação final depois das validações externas. Não apresentar o provedor simulado do BDD como pagamento no Mercado Pago.

| Tempo | Demonstração | Evidência a mostrar |
|---|---|---|
| 0:00–1:30 | Divisão dos três serviços e bancos; RabbitMQ e coreografia | Diagrama atual e três repositórios |
| 1:30–3:00 | CI, cobertura, PMD e deploy automatizado | Execução concluída do CD de pelo menos um serviço, SHA da imagem e rollout saudável |
| 3:00–7:30 | Abrir OS, diagnosticar, enviar orçamento, aprovar, consultar fila, iniciar/concluir reparo | Respostas das APIs, link recebido e histórico da OS |
| 7:30–9:30 | Mostrar entrega bloqueada; pagar no ambiente de teste; entregar | HTTP 422 anterior, pagamento confirmado pelo provedor, webhook processado e entrega posterior |
| 9:30–12:00 | Recusar outro orçamento com reserva e mostrar compensação | Estoque antes/depois; cancelamento dos participantes; evidência do BDD de indisponibilidade/reentrega |
| 12:00–14:00 | Observabilidade e limites | Logs/métricas/traces no Datadog com correlação do atendimento; política de falhas |

## Preparação

1. Concluir os itens externos da matriz; deixar abas e relatórios abertos sem credenciais visíveis.
2. Usar uma OS nova para o fluxo aprovado e outra para recusa. Não reutilizar registros cancelados ou pagos.
3. Gravar o CD já concluído e explicar seu gatilho; não consumir o vídeo inteiro esperando build.
4. Executar testes de indisponibilidade isoladamente: o roteiro de Saga para o RabbitMQ temporariamente.
5. Capturar a cobertura por serviço nos artefatos do mesmo commit apresentado.
6. Publicar vídeo público ou não listado e conferir acesso sem login. Inserir link e participantes no PDF.

## Checklist de evidências

- [ ] CI/CD do commit apresentado e rollout Kubernetes.
- [ ] E-mail do orçamento, decisão e progressão pelos três serviços.
- [ ] Pagamento de teste Mercado Pago e webhook, com dados sensíveis ocultos.
- [ ] Bloqueio da entrega sem pagamento e liberação após confirmação.
- [ ] Compensação do estoque e idempotência, com recuperação após falha.
- [ ] Logs, métricas e rastreamento distribuído no Datadog.
- [ ] Vídeo acessível, até 15 minutos, e PDF revisado.
