create table planos_semaforicos(
    id bigint not null auto_increment,
    id_cruzamento bigint not null,
    verde_norte_sul int not null,
    verde_leste_oeste int not null,
    amarelo int not null,
    origem varchar(20) not null,
    observacao varchar(255),
    data_hora datetime not null,

    primary key(id),
    constraint fk_planos_id_cruzamento foreign key(id_cruzamento) references cruzamentos(id)
);
