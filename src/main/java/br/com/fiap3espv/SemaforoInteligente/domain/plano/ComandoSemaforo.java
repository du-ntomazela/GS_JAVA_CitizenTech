package br.com.fiap3espv.SemaforoInteligente.domain.plano;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.ModoOperacao;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Eixo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/** Mensagem publicada em cidade/{cruzamento}/comandos. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComandoSemaforo(
        TipoComando tipo,
        String cruzamento,
        ModoOperacao modo,

        @JsonProperty("verde_norte_sul")
        Integer verdeNorteSul,

        @JsonProperty("verde_leste_oeste")
        Integer verdeLesteOeste,

        Integer amarelo,

        @JsonProperty("eixo_prioritario")
        Eixo eixoPrioritario,

        @JsonProperty("id_emergencia")
        Long idEmergencia,

        @JsonProperty("data_hora")
        LocalDateTime dataHora) {

    public static ComandoSemaforo plano(PlanoSemaforico plano, ModoOperacao modo) {
        return new ComandoSemaforo(TipoComando.PLANO, plano.getCruzamento().getCodigo(), modo,
                plano.getVerdeNorteSul(), plano.getVerdeLesteOeste(), plano.getAmarelo(),
                null, null, LocalDateTime.now());
    }

    public static ComandoSemaforo liberarEmergencia(String codigoCruzamento, Eixo eixo, int amarelo, Long idEmergencia) {
        return new ComandoSemaforo(TipoComando.LIBERAR_EMERGENCIA, codigoCruzamento, ModoOperacao.EMERGENCIA,
                null, null, amarelo, eixo, idEmergencia, LocalDateTime.now());
    }

    public static ComandoSemaforo restaurar(String codigoCruzamento, ModoOperacao modo, Long idEmergencia) {
        return new ComandoSemaforo(TipoComando.RESTAURAR, codigoCruzamento, modo,
                null, null, null, null, idEmergencia, LocalDateTime.now());
    }
}
