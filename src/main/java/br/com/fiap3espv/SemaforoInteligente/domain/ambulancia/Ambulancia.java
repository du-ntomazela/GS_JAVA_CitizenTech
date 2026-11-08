package br.com.fiap3espv.SemaforoInteligente.domain.ambulancia;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Ambulancia")
@Table(name = "ambulancias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Ambulancia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String placa;
    private String base;
    private boolean ativo = true;

    public Ambulancia(DadosCadastroAmbulancia dados) {
        this.codigo = dados.codigo();
        this.placa = dados.placa();
        this.base = dados.base();
        this.ativo = true;
    }

    public void atualizarInformacoes(DadosAtualizacaoAmbulancia dados) {
        if (dados.base() != null && !dados.base().isBlank()) {
            this.base = dados.base();
        }
    }

    public void excluir() {
        this.ativo = false;
    }
}
