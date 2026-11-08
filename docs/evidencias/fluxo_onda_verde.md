# Evidência: fluxo da onda verde (execução real contra a API, MQTT ligado)

Legenda: AGUA = aguardando, LIBE = verde aberto para a ambulância, REST = voltou ao ciclo normal

```
POST /emergencia -> 201  id=2  cruzamentos na rota=7
  posição inicial           01:LIBE  02:AGUA  03:AGUA  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao fora da rota -> 400  Posição informada está 1901 m fora da rota declarada!
POST posicao 01/34 -> 200  01:LIBE  02:AGUA  03:AGUA  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao 02/34 -> 200  01:LIBE  02:AGUA  03:AGUA  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao 04/34 -> 200  01:REST  02:LIBE  03:AGUA  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao 06/34 -> 200  01:REST  02:LIBE  03:AGUA  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao 08/34 -> 200  01:REST  02:REST  03:AGUA  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao 10/34 -> 200  01:REST  02:REST  03:LIBE  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao 12/34 -> 200  01:REST  02:REST  03:LIBE  04:AGUA  05:AGUA  06:AGUA  07:AGUA
POST posicao 14/34 -> 200  01:REST  02:REST  03:REST  04:LIBE  05:AGUA  06:AGUA  07:AGUA
POST posicao 16/34 -> 200  01:REST  02:REST  03:REST  04:LIBE  05:AGUA  06:AGUA  07:AGUA
POST posicao 18/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:LIBE  06:AGUA  07:AGUA
POST posicao 20/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:LIBE  06:AGUA  07:AGUA
POST posicao 22/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:REST  06:LIBE  07:AGUA
POST posicao 24/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:REST  06:LIBE  07:AGUA
POST posicao 26/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:REST  06:REST  07:LIBE
POST posicao 28/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:REST  06:REST  07:LIBE
POST posicao 30/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:REST  06:REST  07:REST
POST posicao 32/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:REST  06:REST  07:REST
POST posicao 34/34 -> 200  01:REST  02:REST  03:REST  04:REST  05:REST  06:REST  07:REST
POST /emergencia/2/encerramento -> 200  status=ENCERRADA  tempo=38s  semaforos_liberados=7
```

## Log do backend (comandos publicados no broker MQTT)

As três primeiras mensagens PLANO vêm do `scripts/sensor_simulado.py cruzamento-13`; o ALERTA é o Last Will do simulador ao ser encerrado.
```
2026-11-08T10:13:15.061 Assinando cidade/+/sensores e cidade/+/status
2026-11-08T10:13:30.725 Comando PLANO publicado em cidade/cruzamento-13/comandos
2026-11-08T10:13:32.524 Comando PLANO publicado em cidade/cruzamento-13/comandos
2026-11-08T10:13:34.523 Comando PLANO publicado em cidade/cruzamento-13/comandos
2026-11-08T10:13:36.515 ALERTA: cruzamento cruzamento-13 ficou offline e voltou ao ciclo fixo local
2026-11-08T10:14:05.386 Comando LIBERAR_EMERGENCIA publicado em cidade/cruzamento-01/comandos
2026-11-08T10:14:08.751 Comando RESTAURAR publicado em cidade/cruzamento-01/comandos
2026-11-08T10:14:09.871 Comando LIBERAR_EMERGENCIA publicado em cidade/cruzamento-02/comandos
2026-11-08T10:14:14.347 Comando RESTAURAR publicado em cidade/cruzamento-02/comandos
2026-11-08T10:14:15.468 Comando LIBERAR_EMERGENCIA publicado em cidade/cruzamento-03/comandos
2026-11-08T10:14:19.932 Comando RESTAURAR publicado em cidade/cruzamento-03/comandos
2026-11-08T10:14:19.934 Comando LIBERAR_EMERGENCIA publicado em cidade/cruzamento-04/comandos
2026-11-08T10:14:24.415 Comando LIBERAR_EMERGENCIA publicado em cidade/cruzamento-05/comandos
2026-11-08T10:14:25.535 Comando RESTAURAR publicado em cidade/cruzamento-04/comandos
2026-11-08T10:14:28.881 Comando RESTAURAR publicado em cidade/cruzamento-05/comandos
2026-11-08T10:14:30.000 Comando LIBERAR_EMERGENCIA publicado em cidade/cruzamento-06/comandos
2026-11-08T10:14:34.463 Comando RESTAURAR publicado em cidade/cruzamento-06/comandos
2026-11-08T10:14:34.464 Comando LIBERAR_EMERGENCIA publicado em cidade/cruzamento-07/comandos
2026-11-08T10:14:38.923 Comando RESTAURAR publicado em cidade/cruzamento-07/comandos
```
