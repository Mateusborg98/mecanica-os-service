# Entrega da Fase 4 — plano de fechamento

Este checklist distingue implementação local de evidência de entrega. O arquivo delivery-checklist.md registra a Fase 3 e não comprova a entrega da Fase 4. Prazo de trabalho informado: 17/11/2026, a reconfirmar. Saldo confirmado pelo usuário: US$ 50, região us-east-1.

## Base validada localmente

- [x] Serviços OS, Billing e Execution separados, com persistência própria.
- [x] Fluxo assíncrono com outbox, idempotência, compensação e reprocessamento documentados.
- [x] Operações administrativas protegidas; links do cliente públicos conforme decisão de produto.
- [x] Cobertura de linhas acima de 80% e PMD sem violações nas últimas verificações locais.
- [x] BDD distribuído com provedor simulado passou em 05/11/2026.

Evidências: validacao-permissoes-2026-11-05.md nesta pasta; mensageria-publicacao-confirmada.md no Billing e Execution registra os builds posteriores. A última refatoração de publishers ainda precisa entrar na próxima execução distribuída.

## 1. Preparar entrega sem credenciais

- [x] Adaptar os manifests herdados da OS e criar os de Billing e Execution (validação local de schemas; implantação ainda pendente).
- [x] Definir configuração, Secrets, probes, recursos, serviços e estratégia de persistência de cada componente. Criar os Secrets reais e validar em EKS permanece pendente.
- [x] Preparar publicação de imagens e CD independente após sucesso da CI; execução remota ainda pendente.
- [ ] Integrar a execução do BDD distribuído à automação com acesso aos três repositórios.
- [ ] Revisar diagramas, READMEs e roteiro de demonstração conforme os contratos atuais.

Atualização: os três serviços possuem manifests e workflow CD para ECR/EKS. A CI também valida os manifests; a OS valida Terraform. A preparação local não comprova execução remota, provisionamento, permissões de criação ou deploy. Consulte infra/fase4/README.md e docs/validacao-infra-fase4-2026-11-06.md.

## 2. Validar destino e custo

- [ ] Confirmar saldo, região, permissões e duração da sessão do AWS Academy.
- [ ] Inventariar recursos existentes antes de decidir reaproveitamento.
- [ ] Estimar custo da topologia Kubernetes, armazenamento, rede e observabilidade dentro do saldo disponível.
- [ ] Configurar credenciais localmente e nos ambientes do GitHub, sem incluí-las no código ou documentação.
- [ ] Confirmar acesso e configuração do Datadog.

Não provisionar antes de verificar permissões e custo. A topologia final depende desses dados; os artefatos herdados não são aprovação automática para recriar EKS/RDS.

## 3. Produzir evidências finais

- [ ] Executar CI remota dos três serviços e confirmar checks obrigatórios e proteção de main.
- [ ] Demonstrar deploy automatizado e independente em Kubernetes.
- [ ] Validar saúde, autenticação e fluxo completo no ambiente de entrega.
- [ ] Executar novamente o BDD distribuído após as últimas alterações.
- [ ] Concluir compra de teste no Mercado Pago: webhook, consulta ao provedor, cobrança paga e entrega liberada. Usar o roteiro do Billing docs/roteiro-pagamento-real.md.
- [ ] Demonstrar falha da Saga, compensação aplicável e reprocessamento sem efeitos duplicados.
- [ ] Capturar logs, métricas e correlação entre serviços no Datadog.

## 4. Pacote final

- [ ] Conferir todos os itens diretamente contra o PDF do enunciado.
- [ ] Consolidar diagramas e justificativa da Saga, incluindo limites conhecidos.
- [ ] Conferir instruções de execução, Swagger/OpenAPI e links dos repositórios.
- [ ] Preparar vídeo de até 15 minutos e verificar acesso ao link.
- [ ] Preparar PDF com participantes, links e evidências exigidas.
- [ ] Revisar ausência de credenciais nas evidências e planejar encerramento dos recursos após a apresentação.

## Melhoria técnica ainda pendente

Os workers do Execution mantêm transações abertas durante confirmação do RabbitMQ. Avaliar reserva temporária de eventos e publicação fora da transação, com testes de concorrência e recuperação. Essa melhoria não substitui as validações de entrega acima.