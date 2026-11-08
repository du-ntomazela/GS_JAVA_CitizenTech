package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoNotFoundException;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.ModoOperacao;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.StatusConexao;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.CalculadoraPlanoSemaforico;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.ComandoSemaforo;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.PlanoSemaforicoRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.PublicadorComandos;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.TipoComando;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosLeituraVia;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosPacoteSensores;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.LeituraSensorRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.Via;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static br.com.fiap3espv.SemaforoInteligente.domain.emergencia.CalculadoraRotaTest.cruzamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetriaServiceTest {
    @Mock
    private CruzamentoRepository cruzamentoRepository;
    @Mock
    private LeituraSensorRepository leituraRepository;
    @Mock
    private PlanoSemaforicoRepository planoRepository;
    @Mock
    private PublicadorComandos publicador;

    private TelemetriaService service;
    private Cruzamento cruzamento;

    private final DadosPacoteSensores pacote = new DadosPacoteSensores(null, List.of(
            new DadosLeituraVia(Via.NORTE, 20, 15),
            new DadosLeituraVia(Via.SUL, 18, 12),
            new DadosLeituraVia(Via.LESTE, 4, 3),
            new DadosLeituraVia(Via.OESTE, 2, 2)));

    @BeforeEach
    void setUp() {
        service = new TelemetriaService(cruzamentoRepository, leituraRepository, planoRepository,
                new CalculadoraPlanoSemaforico(120, 15, 3), publicador);
        cruzamento = cruzamento("cruzamento-13", -23.5703, -46.6725);
    }

    @Test
    @DisplayName("Pacote de cruzamento adaptativo grava as leituras, marca ONLINE e publica um novo plano")
    void cruzamentoAdaptativoRecebeNovoPlano() {
        when(cruzamentoRepository.findByCodigoAndAtivoTrue("cruzamento-13")).thenReturn(Optional.of(cruzamento));

        DadosProcessamentoTelemetria resultado = service.registrarPacote("cruzamento-13", pacote);

        assertThat(resultado.leiturasRegistradas()).isEqualTo(4);
        assertThat(resultado.planoAplicado()).isNotNull();
        assertThat(resultado.planoAplicado().verdeNorteSul()).isGreaterThan(resultado.planoAplicado().verdeLesteOeste());
        assertThat(cruzamento.getStatusConexao()).isEqualTo(StatusConexao.ONLINE);
        verify(leituraRepository).saveAll(any());
        verify(planoRepository).save(any());

        ArgumentCaptor<ComandoSemaforo> comando = ArgumentCaptor.forClass(ComandoSemaforo.class);
        verify(publicador).publicar(comando.capture());
        assertThat(comando.getValue().tipo()).isEqualTo(TipoComando.PLANO);
        assertThat(comando.getValue().cruzamento()).isEqualTo("cruzamento-13");
    }

    @Test
    @DisplayName("Cruzamento em modo manual apenas grava as leituras, sem sobrescrever o plano da operadora")
    void cruzamentoManualNaoRecebePlano() {
        cruzamento.alterarModo(ModoOperacao.MANUAL);
        when(cruzamentoRepository.findByCodigoAndAtivoTrue("cruzamento-13")).thenReturn(Optional.of(cruzamento));

        DadosProcessamentoTelemetria resultado = service.registrarPacote("cruzamento-13", pacote);

        assertThat(resultado.planoAplicado()).isNull();
        verify(leituraRepository).saveAll(any());
        verifyNoInteractions(planoRepository, publicador);
    }

    @Test
    @DisplayName("Pacote de cruzamento desconhecido é rejeitado")
    void cruzamentoDesconhecido() {
        when(cruzamentoRepository.findByCodigoAndAtivoTrue("cruzamento-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarPacote("cruzamento-99", pacote))
                .isInstanceOf(CruzamentoNotFoundException.class);
        verifyNoInteractions(leituraRepository, publicador);
    }

    @Test
    @DisplayName("Mensagem offline (Last Will) marca o cruzamento como OFFLINE")
    void statusOffline() {
        cruzamento.marcarOnline();
        when(cruzamentoRepository.findByCodigoAndAtivoTrue("cruzamento-13")).thenReturn(Optional.of(cruzamento));

        service.atualizarStatus("cruzamento-13", "offline");

        assertThat(cruzamento.getStatusConexao()).isEqualTo(StatusConexao.OFFLINE);
    }
}
