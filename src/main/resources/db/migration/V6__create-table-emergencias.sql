create table emergencias(
    id bigint not null auto_increment,
    id_ambulancia bigint not null,
    socorrista varchar(100) not null,
    origem_latitude double not null,
    origem_longitude double not null,
    destino_latitude double not null,
    destino_longitude double not null,
    descricao_destino varchar(150) not null,
    status varchar(15) not null,
    data_inicio datetime not null,
    data_fim datetime,
    ultima_latitude double,
    ultima_longitude double,
    ultima_atualizacao datetime,

    primary key(id),
    constraint fk_emergencias_id_ambulancia foreign key(id_ambulancia) references ambulancias(id)
);

create table emergencias_cruzamentos(
    id bigint not null auto_increment,
    id_emergencia bigint not null,
    id_cruzamento bigint not null,
    ordem int not null,
    distancia_na_rota double not null,
    eixo varchar(15) not null,
    situacao varchar(15) not null,
    data_liberacao datetime,
    data_restauracao datetime,

    primary key(id),
    constraint fk_emerg_cruz_id_emergencia foreign key(id_emergencia) references emergencias(id),
    constraint fk_emerg_cruz_id_cruzamento foreign key(id_cruzamento) references cruzamentos(id)
);
