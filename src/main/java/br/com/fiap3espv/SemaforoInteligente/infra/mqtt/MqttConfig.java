package br.com.fiap3espv.SemaforoInteligente.infra.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente MQTT do backend. Para TLS use uma URL ssl:// (ex.: ssl://broker:8883) e configure
 * o truststore/keystore da JVM com o certificado do broker e o certificado do backend.
 */
@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttConfig {
    @Bean(destroyMethod = "close")
    public MqttClient mqttClient(
            @Value("${mqtt.broker-url:tcp://localhost:1883}") String brokerUrl,
            @Value("${mqtt.client-id:semaforo-backend}") String clientId,
            @Value("${mqtt.username:}") String username,
            @Value("${mqtt.password:}") String password) throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setConnectionTimeout(10);
        if (!username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        client.connect(options);
        return client;
    }
}
