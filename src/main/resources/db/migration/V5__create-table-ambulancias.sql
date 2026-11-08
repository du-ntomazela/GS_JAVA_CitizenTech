create table ambulancias(
    id bigint not null auto_increment,
    codigo varchar(20) not null unique,
    placa varchar(7) not null unique,
    base varchar(100) not null,
    ativo tinyint default 1,

    primary key(id)
);
