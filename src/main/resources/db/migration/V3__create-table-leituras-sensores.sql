create table leituras_sensores(
    id bigint not null auto_increment,
    id_cruzamento bigint not null,
    via varchar(10) not null,
    fila int not null,
    fluxo_por_minuto int not null,
    data_hora datetime not null,

    primary key(id),
    constraint fk_leituras_id_cruzamento foreign key(id_cruzamento) references cruzamentos(id)
);

create index idx_leituras_cruzamento_data on leituras_sensores(id_cruzamento, data_hora);
