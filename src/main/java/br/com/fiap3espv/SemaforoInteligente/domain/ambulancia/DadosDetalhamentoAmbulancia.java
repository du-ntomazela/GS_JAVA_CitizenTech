package br.com.fiap3espv.SemaforoInteligente.domain.ambulancia;

public record DadosDetalhamentoAmbulancia(
        Long id,
        String codigo,
        String placa,
        String base,
        boolean ativo) {
    public DadosDetalhamentoAmbulancia(Ambulancia ambulancia) {
        this(
                ambulancia.getId(),
                ambulancia.getCodigo(),
                ambulancia.getPlaca(),
                ambulancia.getBase(),
                ambulancia.isAtivo()
        );
    }
}
