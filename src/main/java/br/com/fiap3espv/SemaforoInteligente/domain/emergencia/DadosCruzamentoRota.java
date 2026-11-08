package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.StatusConexao;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Eixo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record DadosCruzamentoRota(
        int ordem,

        @JsonProperty("id_cruzamento")
        Long idCruzamento,

        String codigo,
        String nome,
        Double latitude,
        Double longitude,

        @JsonProperty("distancia_na_rota_metros")
        long distanciaNaRota,

        @JsonProperty("eixo_liberado")
        Eixo eixo,

        SituacaoCruzamentoRota situacao,

        @JsonProperty("status_conexao")
        StatusConexao statusConexao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        @JsonProperty("data_liberacao")
        LocalDateTime dataLiberacao) {
    public DadosCruzamentoRota(EmergenciaCruzamento item) {
        this(
                item.getOrdem(),
                item.getCruzamento().getId(),
                item.getCruzamento().getCodigo(),
                item.getCruzamento().getNome(),
                item.getCruzamento().getLocalizacao().getLatitude(),
                item.getCruzamento().getLocalizacao().getLongitude(),
                Math.round(item.getDistanciaNaRota()),
                item.getEixo(),
                item.getSituacao(),
                item.getCruzamento().getStatusConexao(),
                item.getDataLiberacao()
        );
    }
}
