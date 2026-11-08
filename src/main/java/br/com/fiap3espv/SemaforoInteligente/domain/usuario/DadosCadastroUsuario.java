package br.com.fiap3espv.SemaforoInteligente.domain.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
        @NotBlank
        String login,

        @NotBlank
        @Size(min = 6)
        String senha,

        @NotNull
        Perfil perfil) {
}
