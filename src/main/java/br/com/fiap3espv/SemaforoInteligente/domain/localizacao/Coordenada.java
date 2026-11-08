package br.com.fiap3espv.SemaforoInteligente.domain.localizacao;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Coordenada {
    private Double latitude;
    private Double longitude;

    public Coordenada(DadosCoordenada dados) {
        this.latitude = dados.latitude();
        this.longitude = dados.longitude();
    }

    public void atualizarInformacoes(DadosCoordenada dados) {
        if (dados.latitude() != null) {
            this.latitude = dados.latitude();
        }
        if (dados.longitude() != null) {
            this.longitude = dados.longitude();
        }
    }
}
