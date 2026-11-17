# Validacao final - 17/11/2026

## Deploy automatizado

Os tres jobs de CD finalizaram com sucesso, incluindo publicacao de imagem, rollout e remocao do IPv4 temporario do runner. O endpoint EKS voltou ao CIDR original de administracao. Os deployments estavam Ready=1.

| Servico | Commit implantado e validado | CD |
|---|---|---|
| OS | 5427097bf0b4800233580d38e808ddc1cb2ce20c | https://github.com/Mateusborg98/mecanica-os-service/actions/runs/44133946897 |
| Billing | 8064df738c53ce39e5b8e97e68f02aafe17cf041 | https://github.com/Mateusborg98/mecanica-billing-service/actions/runs/44131509282 |
| Execution | b3a230d16f87b11b2a9549241b3b21f23aa99e2f | https://github.com/Mateusborg98/mecanica-execution-service/actions/runs/44133432511 |

As tentativas anteriores bloqueadas por voc-cancel-cred foram reexecutadas apos renovacao da sessao Academy. Nao foi necessario enfraquecer autenticacao nem expor o cluster para toda a internet.

## APIs e fluxo distribuido

Os tres Swaggers retornaram HTTP 200: OS com 16 paths, Billing com 8 e Execution com 14. O diagnostico do Execution expoe orcamentoId para continuar o fluxo manual no Billing. As rotas condicionais dependem das funcionalidades habilitadas; SMTP permanece local para a demonstracao.

O validador Java executou os dois cenarios com APIs, bancos e RabbitMQ reais na AWS:

- Aprovacao: abertura, diagnostico, reserva, orcamento aprovado, fila, inicio, conclusao, cobranca PENDENTE e entrega rejeitada com 422.
- Recusa: reparo e OS cancelados, reserva LIBERADA, estoque restituido de 3 para 5 e segunda recusa rejeitada sem duplicar devolucao.

## Observabilidade

Agentes Datadog receberam traces e spans Java e enviaram payloads ao intake APM. O agente de logs apresentou envio ativo e zero erros de submissao. Logs estruturados da aplicacao apresentam dd.trace_id, dd.span_id e correlationId. Para o video, abrir APM/Traces com env:fase4 e os tres services; selecionar o intervalo do ensaio. A verificacao visual da cadeia no painel ainda deve ser feita: a automacao do Edge nao foi autorizada.

## Limites

Nao houve pagamento externo nem envio SMTP nesta execucao automatizada. SMTP local foi confirmado pelo usuario em 16/11. Mercado Pago WCS-59172 permanece pendente; o BDD usa provedor simulado e nao deve ser apresentado como compra real. O PDF esta preparado com Mateus Borgonovi Francisco Drago - RM370507 e aguarda a URL do video.
## Identificadores do ensaio

Horario UTC: 2026-11-17T19:31:05.543874300Z. Correlation ID: `c40b1954-ac3d-417f-b5c7-1eaa220360f4`.

- aprovado-aguardando-pagamento: OS `a6003986-3a60-4410-99fe-85330de1f9a0`, reparo `ef34e611-04b9-4ae4-8487-ad9406d7d686`, orcamento `bf36187e-3dd5-35e9-8179-aecb1378cb61`.

- recusado-compensado: OS `32231044-2c54-4d7f-9564-97311a8cac49`, reparo `d434fe10-d494-473e-b892-40be49ba2f4a`, orcamento `16037c17-82e9-3dd4-9322-116bd8cd6d5e`.
