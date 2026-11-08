package br.com.fiap3espv.SemaforoInteligente.infra.mqtt;

import br.com.fiap3espv.SemaforoInteligente.domain.plano.ComandoSemaforo;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.PublicadorComandos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Usado quando mqtt.enabled=false: a API funciona sem broker e os comandos aparecem no log. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "false", matchIfMissing = true)
public class LogPublicadorComandos implements PublicadorComandos {
    private final JsonMapper jsonMapper;

    @Override
    public void publicar(ComandoSemaforo comando) {
        log.info("[MQTT desligado] {} <- {}", TopicosMqtt.comandos(comando.cruzamento()), jsonMapper.writeValueAsString(comando));
    }
}
