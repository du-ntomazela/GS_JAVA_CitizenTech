# 🚦 Semáforo Inteligente — Backend Java

API REST em **Spring Boot** que é o "backend central" do Semáforo Inteligente: recebe a telemetria dos cruzamentos via **MQTT**, decide o tempo de verde de cada fase, permite a intervenção manual da central de trânsito e orquestra a **onda verde para ambulâncias**.

Projeto da **Global Solution FIAP 2026.2 — Desafio CitizenTech**, disciplina **Arquitetura Orientada a Serviços (SOA) e Web Services**.

## Integrantes

| Nome | RM |
|---|---|
| Eduardo Tomazela | RM556807 |
| Luiz Henrique Silva | RM555235 |
| Léo Masago | RM557768 |

## O problema e a solução (CitizenTech)

Nas grandes metrópoles a maioria dos semáforos trabalha em **ciclos fixos**: o verde dura o mesmo às 3h e às 18h. Isso gera congestionamento, poluição e, principalmente, **ambulâncias presas no trânsito**.

Na solução, cada cruzamento tem **um** dispositivo de borda (Raspberry Pi) que conta filas por via e publica só os números via MQTT (o vídeo nunca sai do cruzamento). Este backend:

1. **assina** `cidade/+/sensores` e `cidade/+/status`, grava o histórico e mantém o status online/offline de cada cruzamento;
2. **calcula** um novo plano (tempo de verde norte-sul e leste-oeste) proporcional à demanda e publica em `cidade/{cruzamento}/comandos`;
3. **expõe** a API REST usada pelo painel da central (C#) e pelo app da ambulância (mobile);
4. **orquestra emergências**: recebe o GPS da ambulância, abre o verde no eixo dela quando está a até 300 m do cruzamento (o eixo oposto passa pelo amarelo antes), e devolve o cruzamento ao ciclo normal depois da passagem.

> Princípio de segurança: o app **nunca** fala com o semáforo. Tudo passa pelo backend, que autentica a ambulância (JWT) e valida se ela está mesmo na rota declarada.

## Tecnologias

- Java 25 · Spring Boot 4.0.4 (Web MVC, Data JPA, Validation, Security)
- MySQL + **Flyway** (migrações versionadas)
- Spring Security com **JWT** (auth0 java-jwt) e BCrypt
- **SpringDoc OpenAPI** (Swagger UI)
- **Eclipse Paho** (cliente MQTT, QoS 1)
- Lombok · Maven
- Testes: JUnit 5, Mockito, AssertJ, MockMvc e H2 (banco em memória)

## Arquitetura

```
br.com.fiap3espv.SemaforoInteligente
├── controller/            endpoints REST
├── domain/
│   ├── cruzamento/        entidade Cruzamento, modos de operação, status de conexão, DTOs
│   ├── telemetria/        LeituraSensor (fila e fluxo por via) e pacote recebido dos sensores
│   ├── plano/             PlanoSemaforico, CalculadoraPlanoSemaforico, ComandoSemaforo, PublicadorComandos
│   ├── ambulancia/        entidade Ambulancia e DTOs
│   ├── emergencia/        CentralDeEmergencias (onda verde), CalculadoraRota, rota da emergência
│   │   └── validacao/     regras de abertura e regras anti-fraude da posição (rota e velocidade)
│   ├── localizacao/       objeto de valor Coordenada e cálculos geográficos (Haversine, projeção na rota)
│   └── usuario/           Usuario (UserDetails), perfis e autenticação
├── infra/
│   ├── security/          SecurityConfig, filtro e serviço de token JWT
│   ├── mqtt/              cliente, assinante e publicador MQTT (ou publicador de log sem broker)
│   ├── exception/         tratamento global de erros (@RestControllerAdvice)
│   └── documentation/     configuração do Swagger/OpenAPI
└── service/               regras de aplicação (cruzamentos, telemetria, planos, ambulâncias, usuários, monitor de conexão)
```

As entidades só expõem getters; mudanças de estado passam por métodos de domínio (`liberar`, `restaurar`, `alterarModo`, `encerrar`…) e a API conversa apenas por DTOs (`record`). Regras de negócio que variam ficam em validadores injetados como lista (`List<ValidadorAberturaEmergencia>`, `List<ValidadorPosicaoEmergencia>`), no mesmo padrão do agendamento do projeto Auto Escola.

## Perfis de acesso

| Perfil | Quem é | Pode |
|---|---|---|
| `ADMIN` | equipe técnica | tudo, inclusive cadastros e usuários |
| `OPERADOR` | operadora da central de trânsito | ver cruzamentos, telemetria e planos, mudar o modo, intervir manualmente, encerrar emergências |
| `SOCORRISTA` | motorista/socorrista da ambulância | abrir emergência, enviar GPS e encerrar a própria emergência |

## Endpoints

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/login` | público | Retorna o token JWT (60 min) |
| GET | `/health-check` | público | Verificação da API |
| POST/GET/DELETE | `/usuario` | ADMIN | Cadastro, listagem e exclusão de usuários |
| POST/PUT/DELETE | `/cruzamento` | ADMIN | Cadastro, atualização e exclusão lógica de cruzamentos |
| GET | `/cruzamento?status=OFFLINE` | todos | Mapa ao vivo (filtro opcional por status) |
| GET | `/cruzamento/{id}` | todos | Detalhe do cruzamento |
| PATCH | `/cruzamento/{id}/modo` | ADMIN, OPERADOR | `ADAPTATIVO` ou `CICLO_FIXO` |
| POST | `/cruzamento/{id}/leitura` | ADMIN, OPERADOR | Mesmo processamento do MQTT, via HTTP (testes/demo) |
| GET | `/cruzamento/{id}/leitura` | ADMIN, OPERADOR | Histórico de leituras |
| GET | `/cruzamento/{id}/plano` | ADMIN, OPERADOR | Plano em vigor |
| GET | `/cruzamento/{id}/plano/historico` | ADMIN, OPERADOR | Histórico de planos |
| POST | `/cruzamento/{id}/plano` | ADMIN, OPERADOR | Intervenção manual (modo `MANUAL`) |
| CRUD | `/ambulancia` | ADMIN (leitura: todos) | Frota de ambulâncias |
| POST | `/emergencia` | ADMIN, SOCORRISTA | Abre a emergência e devolve a rota com os cruzamentos |
| POST | `/emergencia/{id}/posicao` | ADMIN, SOCORRISTA | GPS a cada 1–2 s; devolve os cruzamentos já liberados |
| POST | `/emergencia/{id}/encerramento` | ADMIN, OPERADOR, SOCORRISTA | Encerra, restaura os cruzamentos e grava o tempo de viagem |
| GET | `/emergencia`, `/emergencia/{id}` | ADMIN, OPERADOR (detalhe: todos) | Histórico de emergências |

A documentação completa e interativa fica em **http://localhost:8080/swagger-ui.html** (o JSON em `/v3/api-docs`). Faça login, copie o `tokenJWT` e use o botão **Authorize**.

### Regras de negócio principais

- **Plano adaptativo:** o verde do ciclo (120 s) é dividido entre os eixos na proporção de `fila + 0,5 × fluxo`, com verde mínimo de 15 s por eixo e amarelo de 3 s. Cruzamentos em `MANUAL`, `CICLO_FIXO` ou `EMERGENCIA` só têm as leituras gravadas.
- **Offline:** a mensagem `offline` (Last Will) ou 60 s sem dados marcam o cruzamento como `OFFLINE`; o dispositivo, sozinho, volta ao ciclo fixo.
- **Rota da emergência:** cruzamentos ativos a até 150 m do trajeto origem → destino, na ordem de passagem. (Simplificação da demo: corredor em linha reta; em produção a rota viria de um serviço de roteamento.)
- **Onda verde:** a 300 m o backend publica `LIBERAR_EMERGENCIA` com o eixo da ambulância; 30 m depois do cruzamento publica `RESTAURAR`.
- **Anti-fraude:** posição a mais de 500 m da rota declarada ou deslocamento acima de 180 km/h é rejeitado; só o socorrista que abriu a emergência (ou um ADMIN) envia posições; uma ambulância só pode ter uma emergência ativa.

### Contrato MQTT

| Tópico | Quem publica | Exemplo |
|---|---|---|
| `cidade/{cruzamento}/sensores` | Raspberry | `{"timestamp":"2026-11-08T18:00:00","vias":[{"via":"NORTE","fila":12,"fluxo_por_minuto":20}, …]}` |
| `cidade/{cruzamento}/status` | Raspberry (Last Will) | `online` / `offline` |
| `cidade/{cruzamento}/comandos` | Backend | `{"tipo":"PLANO","modo":"ADAPTATIVO","verde_norte_sul":77,"verde_leste_oeste":37,"amarelo":3}` · `{"tipo":"LIBERAR_EMERGENCIA","eixo_prioritario":"LESTE_OESTE","amarelo":3,"id_emergencia":2}` · `{"tipo":"RESTAURAR","modo":"ADAPTATIVO"}` |

Todas as mensagens usam **QoS 1**. Para TLS use `MQTT_BROKER_URL=ssl://broker:8883` com o truststore/keystore da JVM configurados.

## Como rodar

1. Tenha **Java 25** e **MySQL** rodando (o banco `semaforointeligente` é criado automaticamente).
2. Ajuste usuário/senha do MySQL em `src/main/resources/application.properties`, se necessário.
3. Rode (o Flyway aplica as migrações na inicialização):
   ```bash
   ./mvnw spring-boot:run
   ```
4. A API sobe em `http://localhost:8080` e o Swagger em `http://localhost:8080/swagger-ui.html`.

Por padrão o MQTT fica **desligado** e os comandos aparecem no log, para a API subir sem broker. Para ligar:

```bash
# broker local (ex.: Mosquitto) ou HiveMQ público
MQTT_ENABLED=true MQTT_BROKER_URL=tcp://localhost:1883 ./mvnw spring-boot:run

# em outro terminal, simule um cruzamento (um script por cruzamento)
pip install paho-mqtt
python scripts/sensor_simulado.py cruzamento-13
```

### Usuários de demonstração (migração V7)

| Login | Senha | Perfil |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `operador` | `operador123` | OPERADOR |
| `socorrista` | `socorrista123` | SOCORRISTA |

A V7 também cria 9 cruzamentos (7 no corredor da Av. Paulista, com coordenadas aproximadas) e 2 ambulâncias.

## Migrações (Flyway)

| Versão | Descrição |
|---|---|
| V1 | Tabela `usuarios` (com perfil) |
| V2 | Tabela `cruzamentos` |
| V3 | Tabela `leituras_sensores` + índice por cruzamento/data |
| V4 | Tabela `planos_semaforicos` |
| V5 | Tabela `ambulancias` |
| V6 | Tabelas `emergencias` e `emergencias_cruzamentos` |
| V7 | Dados iniciais (usuários, cruzamentos e ambulâncias) |

## Testes automatizados

```bash
./mvnw test
```

35 testes, sem precisar de MySQL nem de broker:

- **Unitários:** cálculos geográficos, calculadora de plano, cálculo de rota, `CentralDeEmergencias` (liberação, restauração, anti-fraude, permissão), `TelemetriaService` e o assinante MQTT (Mockito).
- **Integração** (`SemaforoInteligenteApplicationTests`): sobe a aplicação inteira com H2 em modo MySQL, **executa as migrações do Flyway** e percorre login, controle de acesso, Swagger, telemetria, intervenção manual e o fluxo completo da onda verde via MockMvc.

## Evidências

- [`docs/evidencias/swagger.png`](docs/evidencias/swagger.png): documentação gerada automaticamente.
- [`docs/evidencias/fluxo_onda_verde.md`](docs/evidencias/fluxo_onda_verde.md): execução real com broker MQTT: o simulador recebendo planos, o alerta de offline e uma ambulância percorrendo a Av. Paulista com os 7 cruzamentos ficando verdes um a um.
