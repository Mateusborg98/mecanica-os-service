CREATE TABLE conclusoes_reparo_recebidas (
    event_id UUID PRIMARY KEY,
    ordem_id UUID NOT NULL UNIQUE REFERENCES ordem_de_servico_jpa_entity(id),
    reparo_id UUID NOT NULL UNIQUE,
    orcamento_id UUID NOT NULL UNIQUE,
    ocorrido_em TIMESTAMP WITH TIME ZONE NOT NULL
);
