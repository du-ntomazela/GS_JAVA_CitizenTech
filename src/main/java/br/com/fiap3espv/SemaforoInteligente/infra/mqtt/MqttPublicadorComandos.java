package br.com.fiap3espv.SemaforoInteligente.infra.mqtt;

import br.com.fiap3espv.SemaforoInteligente.domain.plano.ComandoSemaforo;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.PublicadorComandos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttPublicadorComandos implements PublicadorComandos {
    private final MqttClient client;
    private final JsonMapper jsonMapper;

    @Value("${mqtt.qos:1}")
    private int qos;

    @Override
    public void publicar(ComandoSemaforo comando) {
        String topico = TopicosMqtt.comandos(comando.cruzamento());
        try {
            MqttMessage mensagem = new MqttMessage(jsonMapper.writeValueAsString(comando).getBytes(StandardCharsets.UTF_8));
            mensagem.setQos(qos);
            client.publish(topico, mensagem);
            log.info("Comando {} publicado em {}", comando.tipo(), topico);
        } catch (MqttException e) {
            log.error("Falha ao publicar comando em {}: {}", topico, e.getMessage());
        }
    }
}
