CREATE TABLE progresso_os (
    ordem_id UUID PRIMARY KEY REFERENCES ordem_de_servico_jpa_entity(id),
    reparo_id UUID NOT NULL UNIQUE,
    diagnostico_em TIMESTAMP WITH TIME ZONE,
    orcamento_id UUID UNIQUE,
    decisao VARCHAR(16) CHECK (decisao IN ('APROVADO','RECUSADO')),
    valor NUMERIC(38,2),
    moeda VARCHAR(3),
    decisao_em TIMESTAMP WITH TIME ZONE,
    inicio_em TIMESTAMP WITH TIME ZONE
);
CREATE TABLE eventos_progresso_os (
    tipo VARCHAR(32) NOT NULL,
    event_id UUID NOT NULL,
    ordem_id UUID NOT NULL REFERENCES ordem_de_servico_jpa_entity(id),
    conteudo TEXT NOT NULL,
    PRIMARY KEY(tipo,event_id)
);
