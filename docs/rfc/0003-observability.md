# RFC 0003 - Observabilidade com Datadog

- Status: aceita
- Responsável: Mateus Borgonovi Francisco
- Escopo: Tech Challenge Fase 3

## Contexto

É necessário observar latência, disponibilidade, recursos do Kubernetes,
falhas nas ordens de serviço e indicadores operacionais, além de correlacionar
logs e requisições durante a demonstração.

## Proposta

Instalar o Datadog Agent no EKS por Helm. A API publica healthchecks pelo
Actuator, métricas no formato Prometheus/OpenMetrics e logs JSON. O
`X-Correlation-ID` identifica uma requisição nos logs. O Admission Controller
injeta o tracer Java e envia spans ao Trace Agent por socket Unix. Dashboards e
monitores são provisionados por Terraform.

## Alternativas consideradas

- New Relic: atende ao requisito, mas exigiria outra integração e novos painéis.
- Prometheus e Grafana autogerenciados: evitariam SaaS, porém criariam cargas e
  armazenamento adicionais no cluster acadêmico.
- Somente CloudWatch: cobre recursos AWS, mas exigiria maior trabalho para
  consolidar métricas da aplicação, logs e APM em uma única experiência.

## Consequências

- Métricas, logs e traces ficam acessíveis em uma única ferramenta.
- API Key e App Key permanecem nos GitHub Environments.
- A amostragem de traces é 100% no ambiente acadêmico de baixo volume.
- O Agent consome parte dos recursos do único nó do laboratório.
- Dados sensíveis, JWTs e senhas não devem ser enviados à plataforma.

## Critérios de aceitação

- Dashboard apresenta volume de OS, duração por etapa, latência e recursos.
- Monitores cobrem indisponibilidade, falhas de processamento, latência e CPU.
- Logs JSON apresentam `correlationId`.
- O Trace Explorer apresenta spans HTTP e de acesso ao PostgreSQL.
