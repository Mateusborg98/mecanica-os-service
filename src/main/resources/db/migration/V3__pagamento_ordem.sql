CREATE TABLE pagamento_ordem (
    event_id UUID PRIMARY KEY,
    cobranca_id UUID NOT NULL UNIQUE,
    ordem_id UUID NOT NULL UNIQUE REFERENCES ordem_de_servico_jpa_entity(id),
    reparo_id UUID NOT NULL UNIQUE,
    valor NUMERIC(38,2) NOT NULL CHECK (valor > 0),
    moeda VARCHAR(3) NOT NULL CHECK (moeda = 'BRL'),
    confirmado_em TIMESTAMP WITH TIME ZONE NOT NULL
);
