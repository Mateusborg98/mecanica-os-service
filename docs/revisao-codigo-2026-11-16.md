# Revisao dos tres servicos - 16/11/2026

## Correcoes

- OS: removidos PlacaMapper e OrdemDeServicoResponseResumo, sem consumidores. Removidos os conversores de listas/response sem uso do VeiculoMapper e os conversores redundantes de OrdemDeServicoPresentationMapper. A persistencia de veiculo deixa de depender de DTO de apresentacao.
- Billing: consulta de notificacao inclui ultimoErro e proximaTentativa. Falhas de autenticacao SMTP, transporte e processamento sao diferenciadas sem expor mensagem bruta ou credenciais; logs indicam o ID do orcamento e o reagendamento.
- Billing: propriedades JavaMail de autenticacao, STARTTLS e SSL mapeadas explicitamente das variaveis de sessao; UTF-8 definido. Teste com a autoconfiguracao Spring confirma as propriedades efetivas.
- Launcher Billing: nao carrega arquivo de credenciais SMTP. Verifica variaveis obrigatorias quando habilitado e informa claramente quando SMTP esta desabilitado. Clean antes da inicializacao evita reaproveitar classes antigas da IDE.
- Execution CI: teste distribuido referencia a correcao de abertura administrativa da OS ja publicada (738cb59).
- Documentacao: removidos tres planos antigos de retomada/pendencias e atualizadas as referencias ao estado da AWS e da separacao dos servicos. Migrations, contratos v1/v2 ativos e evidencias historicas foram preservados.

## Verificacao

Executada em copias isoladas com PostgreSQL e MongoDB locais, sem usar credenciais de producao:

| Servico | Testes | Falhas / erros / ignorados | Cobertura de linhas |
| --- | ---: | --- | ---: |
| OS | 116 | 0 / 0 / 0 | 93,13% |
| Billing | 187 | 0 / 0 / 0 | 90,04% |
| Execution | 104 | 0 / 0 / 0 | 87,98% |

Maven clean verify e PMD aprovados nos tres servicos. Sintaxe do launcher PowerShell validada. Referencias Markdown locais verificadas. CPD com limiar padrao de 100 tokens nao encontrou duplicacoes na OS/Billing. No Execution apontou um fragmento de 8 linhas dos workers de inicio/conclusao: eventos e tabelas distintos compartilham o mesmo padrao de publicacao transacional. Mantido por clareza, evitando uma abstracao generica apenas para satisfazer a metrica. Componentes Spring nao foram removidos apenas por nao terem referencias Java diretas.

SMTP: testes locais cobrem rejeicao 535 pelo protocolo, falha visivel no estado persistido, recuperacao com novo worker, mensagem enviada da fila MongoDB ao servidor SMTP local, destinatario/link, concorrencia e lease. Nenhum e-mail externo foi enviado por estes testes.

## Limites e proximo passo

As alteracoes desta revisao estao nos arquivos locais; ainda nao foram implantadas na AWS nem comprovadas por novo CI remoto. Os testes nao comprovam recebimento no Gmail, pagamento real, webhook externo ou CD remoto. Para SMTP real seguir o roteiro atualizado em mecanica-billing-service/docs/envio-orcamento-smtp.md: configurar variaveis no mesmo terminal do Billing, reiniciar, solicitar envio administrativo e consultar o estado. ENVIADO significa aceite SMTP, nao chegada na caixa de entrada.

Persistem as limitacoes conhecidas: SMTP pode duplicar envio se houver queda entre aceite remoto e confirmacao no MongoDB; URL publica temporaria deve continuar acessivel; a configuracao de credenciais depende do ambiente do processo Java.