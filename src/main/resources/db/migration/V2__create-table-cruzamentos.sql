create table cruzamentos(
    id bigint not null auto_increment,
    codigo varchar(50) not null unique,
    nome varchar(150) not null,
    latitude double not null,
    longitude double not null,
    status_conexao varchar(10) not null,
    modo_operacao varchar(20) not null,
    ultima_comunicacao datetime,
    ativo tinyint default 1,

    primary key(id)
);
