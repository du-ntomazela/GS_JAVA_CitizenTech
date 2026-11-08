package br.com.fiap3espv.SemaforoInteligente.domain.usuario;

public record DadosDetalhamentoUsuario(
        Long id,
        String login,
        Perfil perfil) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getPerfil()
        );
    }
}
