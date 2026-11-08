package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.Ambulancia;
import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.AmbulanciaRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.ModoOperacao;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao.ValidadorPosicaoDentroDaRota;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao.ValidadorVelocidadePlausivel;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Eixo;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.CalculadoraPlanoSemaforico;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.ComandoSemaforo;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.PublicadorComandos;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.TipoComando;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.Perfil;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static br.com.fiap3espv.SemaforoInteligente.domain.emergencia.CalculadoraRotaTest.cruzamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CentralDeEmergenciasTest {
    @Mock
    private EmergenciaRepository repository;
    @Mock
    private AmbulanciaRepository ambulanciaRepository;
    @Mock
    private CruzamentoRepository cruzamentoRepository;
    @Mock
    private PublicadorComandos publicador;

    private CentralDeEmergencias central;

    private final Usuario socorrista = new Usuario(1L, "socorrista", "hash", Perfil.SOCORRISTA);
    private final Ambulancia ambulancia = new Ambulancia(1L, "USA-01", "SAM1A01", "Base Centro", true);

    private final Cruzamento augusta = cruzamento("cruzamento-02", -23.557970, -46.659578);
    private final Cruzamento campinas = cruzamento("cruzamento-04", -23.562775, -46.654215);
    private final Cruzamento brigadeiro = cruzamento("cruzamento-06", -23.566960, -46.649544);

    private final DadosAberturaEmergencia abertura = new DadosAberturaEmergencia(
            1L,
            new DadosCoordenada(-23.5558, -46.6620),
            new DadosCoordenada(-23.5713, -46.6447),
            "Hospital - Av. Paulista");

    @BeforeEach
    void setUp() {
        central = new CentralDeEmergencias(
                repository,
                ambulanciaRepository,
                cruzamentoRepository,
                new CalculadoraRota(150),
                new CalculadoraPlanoSemaforico(120, 15, 3),
                publicador,
                List.of(),
                List.of(new ValidadorPosicaoDentroDaRota(500), new ValidadorVelocidadePlausivel(180)),
                300);
    }

    private Emergencia abrirEmergencia() {
        when(ambulanciaRepository.existsById(1L)).thenReturn(true);
        when(ambulanciaRepository.getReferenceById(1L)).thenReturn(ambulancia);
        when(cruzamentoRepository.findAllByAtivoTrue()).thenReturn(List.of(brigadeiro, augusta, campinas));
        ArgumentCaptor<Emergencia> captor = ArgumentCaptor.forClass(Emergencia.class);
        when(repository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        central.abrir(abertura, socorrista);
        Emergencia emergencia = captor.getValue();
        lenient().when(repository.findById(any())).thenReturn(Optional.of(emergencia));
        return emergencia;
    }

    /** Simula que a ultima posicao foi enviada ha um minuto, para nao cair na regra de velocidade. */
    private void voltarRelogio(Emergencia emergencia) {
        emergencia.registrarPosicao(emergencia.getUltimaPosicao(), LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @DisplayName("Abrir emergência monta a rota ordenada com os cruzamentos do corredor")
    void abrirMontaRota() {
        Emergencia emergencia = abrirEmergencia();

        assertThat(emergencia.getRota()).extracting(r -> r.getCruzamento().getCodigo())
                .containsExactly("cruzamento-02", "cruzamento-04", "cruzamento-06");
        assertThat(emergencia.getRota()).allMatch(EmergenciaCruzamento::isAguardando);
        assertThat(emergencia.getSocorrista()).isEqualTo("socorrista");
        verifyNoInteractions(publicador);
    }

    @Test
    @DisplayName("Ambulância a menos de 300 m do cruzamento abre o verde no eixo dela")
    void liberaCruzamentoAoSeAproximar() {
        Emergencia emergencia = abrirEmergencia();
        voltarRelogio(emergencia);

        central.atualizarPosicao(1L, new DadosCoordenada(-23.5570, -46.6607), socorrista);

        EmergenciaCruzamento itemAugusta = emergencia.getRota().get(0);
        assertThat(itemAugusta.isLiberado()).isTrue();
        assertThat(augusta.getModoOperacao()).isEqualTo(ModoOperacao.EMERGENCIA);
        assertThat(emergencia.getRota().get(1).isAguardando()).isTrue();

        ArgumentCaptor<ComandoSemaforo> comando = ArgumentCaptor.forClass(ComandoSemaforo.class);
        verify(publicador).publicar(comando.capture());
        assertThat(comando.getValue().tipo()).isEqualTo(TipoComando.LIBERAR_EMERGENCIA);
        assertThat(comando.getValue().cruzamento()).isEqualTo("cruzamento-02");
        assertThat(comando.getValue().eixoPrioritario()).isEqualTo(itemAugusta.getEixo());
        assertThat(comando.getValue().amarelo()).isEqualTo(3);
    }

    @Test
    @DisplayName("Depois que a ambulância passa, o cruzamento volta ao ciclo normal e o próximo é liberado")
    void restauraCruzamentoDepoisDaPassagem() {
        Emergencia emergencia = abrirEmergencia();
        voltarRelogio(emergencia);
        central.atualizarPosicao(1L, new DadosCoordenada(-23.5570, -46.6607), socorrista);
        voltarRelogio(emergencia);

        central.atualizarPosicao(1L, new DadosCoordenada(-23.5615, -46.6556), socorrista);

        assertThat(emergencia.getRota().get(0).getSituacao()).isEqualTo(SituacaoCruzamentoRota.RESTAURADO);
        assertThat(augusta.getModoOperacao()).isEqualTo(ModoOperacao.ADAPTATIVO);
        assertThat(emergencia.getRota().get(1).isLiberado()).isTrue();
        assertThat(emergencia.getSemaforosLiberados()).isEqualTo(2);
    }

    @Test
    @DisplayName("Posição longe da rota declarada é rejeitada (anti-fraude)")
    void rejeitaPosicaoForaDaRota() {
        Emergencia emergencia = abrirEmergencia();
        voltarRelogio(emergencia);

        assertThatThrownBy(() -> central.atualizarPosicao(1L, new DadosCoordenada(-23.5703, -46.6725), socorrista))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("fora da rota");
        verifyNoInteractions(publicador);
    }

    @Test
    @DisplayName("Salto de posição impossível para um veículo é rejeitado (GPS falsificado)")
    void rejeitaVelocidadeImpossivel() {
        abrirEmergencia();

        assertThatThrownBy(() -> central.atualizarPosicao(1L, new DadosCoordenada(-23.5669, -46.6495), socorrista))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("velocidade");
    }

    @Test
    @DisplayName("Outro socorrista não pode enviar posição de uma emergência que não abriu")
    void bloqueiaOutroSocorrista() {
        abrirEmergencia();
        Usuario outro = new Usuario(2L, "outro", "hash", Perfil.SOCORRISTA);

        assertThatThrownBy(() -> central.atualizarPosicao(1L, new DadosCoordenada(-23.5560, -46.6618), outro))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    @DisplayName("Encerrar a emergência devolve os cruzamentos liberados ao ciclo normal")
    void encerrarRestauraCruzamentos() {
        Emergencia emergencia = abrirEmergencia();
        voltarRelogio(emergencia);
        central.atualizarPosicao(1L, new DadosCoordenada(-23.5570, -46.6607), socorrista);

        DetalhamentoEmergencia resultado = central.encerrar(1L, socorrista);

        assertThat(resultado.status()).isEqualTo(StatusEmergencia.ENCERRADA);
        assertThat(resultado.semaforosLiberados()).isEqualTo(1);
        assertThat(resultado.tempoViagemSegundos()).isNotNull();
        assertThat(augusta.getModoOperacao()).isEqualTo(ModoOperacao.ADAPTATIVO);
        verify(publicador, times(2)).publicar(any());
    }

    @Test
    @DisplayName("Emergência encerrada não aceita novas posições")
    void naoAtualizaEmergenciaEncerrada() {
        abrirEmergencia();
        central.encerrar(1L, socorrista);

        assertThatThrownBy(() -> central.atualizarPosicao(1L, new DadosCoordenada(-23.5560, -46.6618), socorrista))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Rota da Av. Paulista usa a fase do eixo predominante do trajeto")
    void eixoDaRota() {
        Emergencia emergencia = abrirEmergencia();
        Eixo eixo = emergencia.getRota().get(0).getEixo();
        assertThat(emergencia.getRota()).allMatch(r -> r.getEixo() == eixo);
        assertThat(new Coordenada(-23.5558, -46.6620)).isEqualTo(emergencia.getOrigem());
    }
}
