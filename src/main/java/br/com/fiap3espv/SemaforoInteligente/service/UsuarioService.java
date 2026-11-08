package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.usuario.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DadosDetalhamentoUsuario cadastrarUsuario(DadosCadastroUsuario dados) {
        if (repository.existsByLogin(dados.login())) {
            throw new ValidacaoUsuarioException("Já existe um usuário cadastrado com este login!");
        }
        String senhaCriptografada = passwordEncoder.encode(dados.senha());
        Usuario usuario = new Usuario(dados, senhaCriptografada);
        repository.save(usuario);
        return new DadosDetalhamentoUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoUsuario> listarUsuarios(Pageable pageable) {
        return repository.findAll(pageable).map(DadosDetalhamentoUsuario::new);
    }

    @Transactional
    public void excluirUsuario(Long id) {
        if (!repository.existsById(id)) {
            throw new UsuarioNotFoundException("ID do usuário informado não existe!");
        }
        repository.deleteById(id);
    }
}
