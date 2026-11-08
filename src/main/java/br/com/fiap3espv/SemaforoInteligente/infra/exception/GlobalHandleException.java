package br.com.fiap3espv.SemaforoInteligente.infra.exception;

import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.AmbulanciaNotFoundException;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoNotFoundException;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.AcessoNegadoException;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.EmergenciaNotFoundException;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.UsuarioNotFoundException;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.ValidacaoUsuarioException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalHandleException {
    @ExceptionHandler({
            EntityNotFoundException.class,
            CruzamentoNotFoundException.class,
            AmbulanciaNotFoundException.class,
            EmergenciaNotFoundException.class,
            UsuarioNotFoundException.class
    })
    public ResponseEntity<DadosErro> handleNotFoundException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DadosErro(ex.getMessage()));
    }

    @ExceptionHandler({ValidacaoException.class, ValidacaoUsuarioException.class})
    public ResponseEntity<DadosErro> handleValidacaoException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DadosErro(ex.getMessage()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<DadosErro> handleAcessoNegadoException(AcessoNegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new DadosErro(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<DadosErro> handleBadCredentialsException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DadosErro("Login ou senha inválidos!"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DadosErro> handleMensagemInvalida() {
        return ResponseEntity.badRequest().body(new DadosErro("Corpo da requisição inválido ou mal formatado!"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosCampoInvalido>> handleBadRequestException(MethodArgumentNotValidException ex) {
        List<FieldError> erros = ex.getFieldErrors();
        return ResponseEntity.badRequest().body(erros.stream().map(DadosCampoInvalido::new).toList());
    }

    private record DadosErro(String message) {
    }

    private record DadosCampoInvalido(String field, String message) {
        public DadosCampoInvalido(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}
