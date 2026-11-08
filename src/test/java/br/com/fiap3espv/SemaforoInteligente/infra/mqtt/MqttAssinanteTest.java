package br.com.fiap3espv.SemaforoInteligente.infra.mqtt;

import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosPacoteSensores;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.Via;
import br.com.fiap3espv.SemaforoInteligente.service.TelemetriaService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MqttAssinanteTest {
    private final TelemetriaService telemetriaService = mock(TelemetriaService.class);
    private MqttAssinante assinante;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        assinante = new MqttAssinante(mock(MqttClient.class), JsonMapper.builder().build(), validator, telemetriaService);
    }

    private MqttMessage mensagem(String conteudo) {
        return new MqttMessage(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Pacote publicado em cidade/{cruzamento}/sensores é convertido e enviado ao serviço")
    void processaPacoteDeSensores() {
        assinante.processarMensagem("cidade/cruzamento-13/sensores", mensagem("""
                {"timestamp":"2026-11-08T18:00:00",
                 "vias":[{"via":"NORTE","fila":12,"fluxo_por_minuto":20},
                         {"via":"LESTE","fila":3,"fluxo_por_minuto":6}]}"""));

        ArgumentCaptor<DadosPacoteSensores> pacote = ArgumentCaptor.forClass(DadosPacoteSensores.class);
        verify(telemetriaService).registrarPacote(eq("cruzamento-13"), pacote.capture());
        assertThat(pacote.getValue().vias()).hasSize(2);
        assertThat(pacote.getValue().vias().getFirst().via()).isEqualTo(Via.NORTE);
        assertThat(pacote.getValue().vias().getFirst().fluxoPorMinuto()).isEqualTo(20);
        assertThat(pacote.getValue().timestamp().getHour()).isEqualTo(18);
    }

    @Test
    @DisplayName("Mensagem de status (Last Will) atualiza a conexão do cruzamento")
    void processaStatus() {
        assinante.processarMensagem("cidade/cruzamento-13/status", mensagem("offline"));

        verify(telemetriaService).atualizarStatus("cruzamento-13", "offline");
    }

    @Test
    @DisplayName("Pacote inválido ou mal formatado é descartado sem derrubar a assinatura")
    void descartaPacoteInvalido() {
        assinante.processarMensagem("cidade/cruzamento-13/sensores", mensagem("{\"vias\":[{\"via\":\"NORTE\",\"fila\":-5,\"fluxo_por_minuto\":1}]}"));
        assinante.processarMensagem("cidade/cruzamento-13/sensores", mensagem("isto nao e json"));
        assinante.processarMensagem("topico/errado", mensagem("{}"));

        verify(telemetriaService, never()).registrarPacote(anyString(), any(DadosPacoteSensores.class));
    }
}
