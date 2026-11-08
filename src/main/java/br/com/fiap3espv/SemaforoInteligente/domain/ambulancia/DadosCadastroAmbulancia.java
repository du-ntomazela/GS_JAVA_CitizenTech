package br.com.fiap3espv.SemaforoInteligente.domain.ambulancia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroAmbulancia(
        @NotBlank
        @Pattern(regexp = "[A-Z0-9-]{3,20}")
        String codigo,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}", message = "deve seguir o padrão de placa brasileira (ex: ABC1D23)")
        String placa,

        @NotBlank
        String base) {
}
