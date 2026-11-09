CREATE TABLE cliente_jpa_entity (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(255) NOT NULL UNIQUE,
    contato VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    ativo BOOLEAN NOT NULL,
    data_inativacao TIMESTAMP
);

CREATE TABLE operador_jpa_entity (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    matricula INTEGER NOT NULL UNIQUE,
    cargo VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    contato VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL,
    data_inativacao TIMESTAMP
);

CREATE TABLE peca_jpa_entity (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    marca VARCHAR(255) NOT NULL,
    preco NUMERIC(19, 2) NOT NULL,
    ativo BOOLEAN NOT NULL,
    data_inativacao TIMESTAMP
);

CREATE TABLE servico_jpa_entity (
    id UUID PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    preco NUMERIC(19, 2) NOT NULL,
    ativo BOOLEAN NOT NULL,
    data_inativacao TIMESTAMP
);

CREATE TABLE veiculo_jpa_entity (
    id UUID PRIMARY KEY,
    placa VARCHAR(255) NOT NULL UNIQUE,
    marca VARCHAR(255) NOT NULL,
    modelo VARCHAR(255) NOT NULL,
    ano INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL,
    data_inativacao TIMESTAMP,
    cliente_jpa_entity_id UUID NOT NULL,
    CONSTRAINT fk_veiculo_cliente
        FOREIGN KEY (cliente_jpa_entity_id)
        REFERENCES cliente_jpa_entity (id)
);

CREATE TABLE estoque_jpa_entity (
    id UUID PRIMARY KEY,
    peca_id UUID UNIQUE,
    quantidade INTEGER NOT NULL,
    versao BIGINT,
    CONSTRAINT fk_estoque_peca
        FOREIGN KEY (peca_id)
        REFERENCES peca_jpa_entity (id)
);

CREATE TABLE ordem_de_servico_jpa_entity (
    id UUID PRIMARY KEY,
    cliente_jpa_entity_id UUID,
    veiculo_jpa_entity_id UUID,
    operador_jpa_entity_id UUID,
    status VARCHAR(255),
    dt_criacao TIMESTAMP,
    dt_inicio_os TIMESTAMP,
    dt_fim_os TIMESTAMP,
    valor_total_os NUMERIC(38, 2),
    CONSTRAINT fk_ordem_servico_cliente
        FOREIGN KEY (cliente_jpa_entity_id)
        REFERENCES cliente_jpa_entity (id),
    CONSTRAINT fk_ordem_servico_veiculo
        FOREIGN KEY (veiculo_jpa_entity_id)
        REFERENCES veiculo_jpa_entity (id),
    CONSTRAINT fk_ordem_servico_operador
        FOREIGN KEY (operador_jpa_entity_id)
        REFERENCES operador_jpa_entity (id)
);

CREATE TABLE peca_ordem_de_servico_jpa_entity (
    id UUID PRIMARY KEY,
    ordem_servico_id UUID,
    peca_id UUID,
    quantidade INTEGER NOT NULL,
    valor_unitario NUMERIC(38, 2) NOT NULL,
    CONSTRAINT fk_peca_ordem_ordem_servico
        FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_de_servico_jpa_entity (id),
    CONSTRAINT fk_peca_ordem_peca
        FOREIGN KEY (peca_id)
        REFERENCES peca_jpa_entity (id)
);

CREATE TABLE servico_ordem_de_servico_jpa_entity (
    id UUID PRIMARY KEY,
    ordem_servico_id UUID NOT NULL,
    servico_id UUID NOT NULL,
    status VARCHAR(255),
    dt_inicio TIMESTAMP,
    dt_fim TIMESTAMP,
    valor_cobrado NUMERIC(38, 2) NOT NULL,
    CONSTRAINT fk_servico_ordem_ordem_servico
        FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_de_servico_jpa_entity (id),
    CONSTRAINT fk_servico_ordem_servico
        FOREIGN KEY (servico_id)
        REFERENCES servico_jpa_entity (id)
);

CREATE INDEX idx_veiculo_cliente
    ON veiculo_jpa_entity (cliente_jpa_entity_id);

CREATE INDEX idx_ordem_servico_cliente
    ON ordem_de_servico_jpa_entity (cliente_jpa_entity_id);

CREATE INDEX idx_ordem_servico_status
    ON ordem_de_servico_jpa_entity (status);
