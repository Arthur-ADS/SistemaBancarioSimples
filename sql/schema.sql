-- ATENÇÃO: com Hibernate, as tabelas são criadas/atualizadas AUTOMATICAMENTE
-- (hibernate.hbm2ddl.auto=update no persistence.xml), inclusive o banco
-- (createDatabaseIfNotExist=true na URL). Não é preciso rodar este script.
-- Ele é apenas uma referência equivalente ao que o Hibernate gera.

CREATE DATABASE IF NOT EXISTS banco_poo_jpa;
USE banco_poo_jpa;

-- Cliente + Endereco (@Embedded: as colunas do endereço ficam na mesma tabela)
CREATE TABLE cliente (
    cpf         VARCHAR(11)  PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    logradouro  VARCHAR(100),
    cidade      VARCHAR(60),
    uf          VARCHAR(2),
    cep         VARCHAR(8)
);

-- Conta (herança SINGLE_TABLE: Corrente e Poupança na mesma tabela, "tipo" é o discriminador)
CREATE TABLE conta (
    numero       VARCHAR(4)    PRIMARY KEY,
    tipo         VARCHAR(10)   NOT NULL,
    cliente_cpf  VARCHAR(11)   NOT NULL,
    saldo        DECIMAL(15,2) NOT NULL,
    status       ENUM('ATIVA','BLOQUEADA','ENCERRADA') NOT NULL,   -- @Enumerated(STRING)
    FOREIGN KEY (cliente_cpf) REFERENCES cliente(cpf)                -- Cliente 1:N Conta
);

-- Movimentacao (Conta 1:N Movimentacao, gravada em cascata ao salvar a conta)
CREATE TABLE movimentacao (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    conta_numero  VARCHAR(4)    NOT NULL,
    tipo          ENUM('DEPOSITO','SAQUE','TRANSFERENCIA_ENVIADA','TRANSFERENCIA_RECEBIDA','TARIFA','RENDIMENTO') NOT NULL,
    valor         DECIMAL(15,2) NOT NULL,
    dataHora      DATETIME(6)   NOT NULL,
    FOREIGN KEY (conta_numero) REFERENCES conta(numero)
);

CREATE TABLE servico (
    codigo        VARCHAR(20)   PRIMARY KEY,
    nome          VARCHAR(60)   NOT NULL,
    tarifaMensal  DECIMAL(10,2) NOT NULL
);

-- Tabela intermediária do @ManyToMany (definida por @JoinTable em Conta; sem classe Java)
CREATE TABLE conta_servico (
    conta_numero    VARCHAR(4)  NOT NULL,
    servico_codigo  VARCHAR(20) NOT NULL,
    PRIMARY KEY (conta_numero, servico_codigo),
    FOREIGN KEY (conta_numero)   REFERENCES conta(numero),
    FOREIGN KEY (servico_codigo) REFERENCES servico(codigo)
);
