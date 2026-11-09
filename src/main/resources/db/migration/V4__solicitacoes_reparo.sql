CREATE TABLE solicitacoes_reparo (
    ordem_id UUID PRIMARY KEY REFERENCES ordem_de_servico_jpa_entity(id),
    reparo_id UUID NOT NULL UNIQUE,
    ocorrido_em TIMESTAMP WITH TIME ZONE NOT NULL,
    publicado_em TIMESTAMP WITH TIME ZONE,
    tentativa_em TIMESTAMP WITH TIME ZONE NOT NULL,
    lease_ate TIMESTAMP WITH TIME ZONE,
    lease_token UUID,
    tentativas INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_solicitacoes_reparo_pendentes ON solicitacoes_reparo(publicado_em, tentativa_em);
