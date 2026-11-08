package br.com.fiap3espv.SemaforoInteligente.infra.mqtt;

import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosPacoteSensores;
import br.com.fiap3espv.SemaforoInteligente.service.TelemetriaService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/** Assina os topicos dos cruzamentos e repassa os dados para a camada de servico. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttAssinante {
    private final MqttClient client;
    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final TelemetriaService telemetriaService;

    @Value("${mqtt.qos:1}")
    private int qos;

    @EventListener(ApplicationReadyEvent.class)
    public void assinar() throws MqttException {
        client.subscribe(TopicosMqtt.TODOS_SENSORES, qos, this::processarMensagem);
        client.subscribe(TopicosMqtt.TODOS_STATUS, qos, this::processarMensagem);
        log.info("Assinando {} e {}", TopicosMqtt.TODOS_SENSORES, TopicosMqtt.TODOS_STATUS);
    }

    void processarMensagem(String topico, MqttMessage mensagem) {
        try {
            String codigo = TopicosMqtt.codigoCruzamento(topico);
            String conteudo = new String(mensagem.getPayload(), StandardCharsets.UTF_8);

            switch (TopicosMqtt.tipo(topico)) {
                case "sensores" -> {
                    DadosPacoteSensores pacote = jsonMapper.readValue(conteudo, DadosPacoteSensores.class);
                    Set<ConstraintViolation<DadosPacoteSensores>> erros = validator.validate(pacote);
                    if (!erros.isEmpty()) {
                        log.warn("Pacote inválido de {} descartado: {}", codigo, erros);
                        return;
                    }
                    telemetriaService.registrarPacote(codigo, pacote);
                }
                case "status" -> telemetriaService.atualizarStatus(codigo, conteudo.replace("\"", "").trim());
                default -> log.debug("Tópico ignorado: {}", topico);
            }
        } catch (Exception e) {
            // Uma mensagem ruim nunca derruba a assinatura dos demais cruzamentos.
            log.error("Erro ao processar mensagem de {}: {}", topico, e.getMessage());
        }
    }
}
