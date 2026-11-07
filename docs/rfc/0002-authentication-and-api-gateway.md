# RFC 0002 - Autenticação serverless e API Gateway

- Status: aceita
- Responsável: Mateus Borgonovi Francisco
- Escopo: Tech Challenge Fase 3

## Contexto

A solução precisa autenticar clientes por CPF ou CNPJ, proteger as rotas da
oficina e manter um único ponto público de entrada. A aplicação principal não
deve receber a chave privada usada para emitir tokens.

## Proposta

Utilizar o AWS API Gateway HTTP API como entrada pública. A rota `POST /auth`
invoca uma Lambda Java 21, que valida o documento, consulta o cliente ativo no
RDS e emite um JWT RS256 com validade curta. As demais rotas são encaminhadas
ao Load Balancer da API no EKS. O Spring Security valida assinatura, emissor,
expiração e papel do token usando somente a chave pública.

## Alternativas consideradas

- Autenticação dentro da API principal: acoplaria emissão e consumo do token e
  aumentaria o alcance da chave privada.
- JWT HS256: exigiria compartilhar o mesmo segredo entre emissor e consumidor.
- Sessão armazenada no banco: adicionaria estado e consultas em todas as
  requisições protegidas.
- API Gateway REST API: oferece mais recursos, porém o HTTP API atende ao
  roteamento necessário com menor complexidade para o laboratório.

## Consequências

- A chave privada permanece restrita à Lambda e a API recebe apenas a pública.
- O token é stateless e expira em 900 segundos.
- O CPF/CNPJ não é incluído no JWT; o `sub` contém o UUID interno do cliente.
- A autorização fina continua sendo responsabilidade da aplicação.
- A rotação do par RSA exige atualizar os secrets da Lambda e da API.

## Critérios de aceitação

- Documento inválido, ausente ou cliente inativo não produz token.
- Uma rota protegida sem token retorna `401` ou `403`.
- Um token válido permite consumir as APIs protegidas.
- Logs não exibem documento, token ou chave privada.
