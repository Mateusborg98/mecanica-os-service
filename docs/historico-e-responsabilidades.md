# Responsabilidades e histórico da OS

A OS mantém clientes, veículos, operadores, abertura do atendimento, acompanhamento e entrega. Execution mantém catálogo de peças/serviços, diagnóstico, reserva/consumo do estoque e execução. Billing mantém orçamento, decisão, cobrança e pagamento.

## Abertura

`POST /ordens-servico` recebe somente `placa` e `matriculaOperador`. O cliente vem do `sub` do JWT. O caso de uso verifica se o veículo pertence a esse cliente antes de qualquer gravação. Se o vínculo não existir, retorna HTTP 422 e não cria OS, solicitação de reparo ou notificação. A mesma transação grava a OS, a solicitação de reparo na outbox e o evento ABERTURA. Itens e preços são definidos no diagnóstico do Execution.

Os controllers e casos de uso antigos de catálogo, estoque, alteração de itens, aprovação e execução foram removidos da OS. Os eventos dos outros serviços atualizam a projeção de acompanhamento; a OS não recalcula orçamento nem executa reparo localmente.

## Histórico

`GET /ordens-servico/{id}/historico?apos=0` exige JWT e retorna até 100 registros. Para a próxima página, informe a última `sequencia` no parâmetro `apos`.

Cada registro contém tipo, identificador do evento, origem, estado anterior/atual, data de ocorrência e data de registro. A sequência representa a ordem de recebimento na OS; eventos distribuídos podem chegar fora da ordem de ocorrência. Pagamento registra confirmação, sem criar uma transição artificial do status da OS.

Tipos: ABERTURA, DIAGNOSTICO, DECISAO, INICIO, CONCLUSAO, CANCELAMENTO, PAGAMENTO e ENTREGA. O registro ocorre na mesma transação local que a alteração correspondente. A chave única `(ordem_id, tipo, evento_id)` evita duplicar histórico na reentrega. O consumidor continua validando a correlação e rejeitando eventos conflitantes.

V7 cria a tabela e seus índices. V8 registra `MARCO_LEGADO` para ordens preexistentes, com o estado conhecido e `ocorridoEm` nulo. Esse marco não reconstrói transições anteriores nem inventa datas.

## Migração sem perda de dados

As migrations antigas continuam imutáveis. As tabelas legadas de catálogo/estoque permanecem no PostgreSQL para preservar dados anteriores, mas não têm mais entidades, repositórios ou rotas no serviço. A remoção física dessas tabelas deve ocorrer somente depois de uma migração de dados conferida e com backup.

## Entrega

`POST /ordens-servico/{id}/entregar` exige conclusão e pagamento confirmados, correlacionados ao mesmo reparo/orçamento e valor aprovado. A conclusão isolada não libera entrega. Alteração da OS e registro ENTREGA são confirmados juntos.

## Verificação

`mvnw.cmd clean verify` executa testes, cobertura mínima de 80% e PMD. Os testes de histórico verificam paginação, duplicatas, eventos recebidos fora da ordem temporal, rollback, inexistência da OS e marco legado. Os testes de rotas verificam autenticação e ausência das responsabilidades removidas.
