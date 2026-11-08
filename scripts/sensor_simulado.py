"""
Simula o dispositivo de borda (Raspberry Pi) de UM cruzamento, com as suas quatro vias.
Publica filas/fluxo em cidade/{cruzamento}/sensores, avisa online/offline em cidade/{cruzamento}/status
(com Last Will) e imprime os comandos recebidos do backend em cidade/{cruzamento}/comandos.

Uso:  pip install paho-mqtt
      python sensor_simulado.py cruzamento-13 [--broker localhost] [--porta 1883] [--intervalo 5] [--vezes 0]
Obs.: os numeros sao sinteticos, so para demonstrar a integracao com o backend (nao sao usados no modelo de ML).
"""
import argparse
import json
import random
import time
from datetime import datetime

import paho.mqtt.client as mqtt


def fator_horario(hora: int) -> float:
    """Picos as 8h e as 18h."""
    return 0.3 + max(0.0, 1 - abs(hora - 8) / 3) + max(0.0, 1 - abs(hora - 18) / 3)


def gerar_pacote() -> dict:
    agora = datetime.now()
    f = fator_horario(agora.hour)
    vias = []
    for via, peso in (("NORTE", 1.0), ("SUL", 0.9), ("LESTE", 0.5), ("OESTE", 0.4)):
        fila = max(0, int(random.gauss(25 * f * peso, 4)))
        fluxo = max(0, int(random.gauss(20 * f * peso, 3)))
        vias.append({"via": via, "fila": fila, "fluxo_por_minuto": fluxo})
    return {"timestamp": agora.replace(microsecond=0).isoformat(), "vias": vias}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("cruzamento")
    parser.add_argument("--broker", default="localhost")
    parser.add_argument("--porta", type=int, default=1883)
    parser.add_argument("--intervalo", type=float, default=5)
    parser.add_argument("--vezes", type=int, default=0, help="0 = infinito")
    args = parser.parse_args()

    base = f"cidade/{args.cruzamento}"
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=f"borda-{args.cruzamento}")
    client.will_set(f"{base}/status", "offline", qos=1, retain=True)
    client.on_message = lambda c, u, msg: print(f"<- {msg.topic}: {msg.payload.decode()}")
    client.connect(args.broker, args.porta)
    client.subscribe(f"{base}/comandos", qos=1)
    client.loop_start()
    client.publish(f"{base}/status", "online", qos=1, retain=True)

    enviados = 0
    try:
        while args.vezes == 0 or enviados < args.vezes:
            pacote = gerar_pacote()
            client.publish(f"{base}/sensores", json.dumps(pacote), qos=1)
            print(f"-> {base}/sensores: {pacote}")
            enviados += 1
            time.sleep(args.intervalo)
    finally:
        client.publish(f"{base}/status", "offline", qos=1, retain=True).wait_for_publish()
        client.loop_stop()
        client.disconnect()


if __name__ == "__main__":
    main()
