-- Senhas: admin123 / operador123 / socorrista123 (BCrypt)
insert into usuarios(login, senha, perfil) values
('admin', '$2a$10$1aZtdxim/4l9jelzZ1MMiuny3iTn2gR8CS3s8Fpmy.wxklTySH/AS', 'ADMIN'),
('operador', '$2a$10$5oZZaciugNMOztqbf9BVaeqC9sUHrpmW7gFBi6k0L2eogOvN5kkTW', 'OPERADOR'),
('socorrista', '$2a$10$uQwACri27d3ula0lF3bDV.jpKd1s100r1rmXO62DCmf.jcRfGg.Xi', 'SOCORRISTA');

-- Cruzamentos da demonstracao (coordenadas aproximadas do corredor da Av. Paulista, Sao Paulo)
insert into cruzamentos(codigo, nome, latitude, longitude, status_conexao, modo_operacao, ativo) values
('cruzamento-01', 'Av. Paulista x R. da Consolação', -23.555800, -46.662000, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-02', 'Av. Paulista x R. Augusta', -23.557970, -46.659578, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-03', 'Av. Paulista x Al. Pamplona', -23.560450, -46.656810, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-04', 'Av. Paulista x Al. Campinas', -23.562775, -46.654215, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-05', 'Av. Paulista x Al. Joaquim Eugênio de Lima', -23.564480, -46.652312, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-06', 'Av. Paulista x Av. Brigadeiro Luís Antônio', -23.566960, -46.649544, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-07', 'Av. Paulista x R. Teixeira da Silva', -23.569130, -46.647122, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-13', 'Av. Rebouças x Av. Brasil', -23.570300, -46.672500, 'OFFLINE', 'ADAPTATIVO', 1),
('cruzamento-14', 'Av. Brasil x R. Augusta', -23.573800, -46.665700, 'OFFLINE', 'CICLO_FIXO', 1);

insert into ambulancias(codigo, placa, base, ativo) values
('USA-01', 'SAM1A01', 'SAMU - Base Centro', 1),
('USA-02', 'SAM2B02', 'SAMU - Base Pinheiros', 1);
