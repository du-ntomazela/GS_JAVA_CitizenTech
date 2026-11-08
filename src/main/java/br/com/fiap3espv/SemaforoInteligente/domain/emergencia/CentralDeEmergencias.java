package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.Ambulancia;
import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.AmbulanciaNotFoundException;
import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.AmbulanciaRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.ModoOperacao;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao.ValidadorAberturaEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao.ValidadorPosicaoEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Geolocalizacao;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.CalculadoraPlanoSemaforico;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.ComandoSemaforo;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.PublicadorComandos;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.Perfil;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquestra a onda verde: abre a emergencia, calcula os cruzamentos da rota,
 * acompanha o GPS da ambulancia e libera/restaura cada cruzamento via MQTT.
 * O app nunca fala direto com o semaforo: tudo passa por aqui.
 */
@Service
public class CentralDeEmergencias {
    /** Depois de passar o cruzamento por esta distancia, ele volta ao ciclo normal. */
    private static final double MARGEM_PASSAGEM_METROS = 30;

    private final EmergenciaRepository repository;
    private final AmbulanciaRepository ambulanciaRepository;
    private final CruzamentoRepository cruzamentoRepository;
    private final CalculadoraRota calculadoraRota;
    private final CalculadoraPlanoSemaforico calculadoraPlano;
    private final PublicadorComandos publicador;
    private final List<ValidadorAberturaEmergencia> validadoresAbertura;
    private final List<ValidadorPosicaoEmergencia> validadoresPosicao;
    private final double raioLiberacaoMetros;

    public CentralDeEmergencias(EmergenciaRepository repository,
                                AmbulanciaRepository ambulanciaRepository,
                                CruzamentoRepository cruzamentoRepository,
                                CalculadoraRota calculadoraRota,
                                CalculadoraPlanoSemaforico calculadoraPlano,
                                PublicadorComandos publicador,
                                List<ValidadorAberturaEmergencia> validadoresAbertura,
                                List<ValidadorPosicaoEmergencia> validadoresPosicao,
                                @Value("${emergencia.raio-liberacao-metros:300}") double raioLiberacaoMetros) {
        this.repository = repository;
        this.ambulanciaRepository = ambulanciaRepository;
        this.cruzamentoRepository = cruzamentoRepository;
        this.calculadoraRota = calculadoraRota;
        this.calculadoraPlano = calculadoraPlano;
        this.publicador = publicador;
        this.validadoresAbertura = validadoresAbertura;
        this.validadoresPosicao = validadoresPosicao;
        this.raioLiberacaoMetros = raioLiberacaoMetros;
    }

    @Transactional
    public DetalhamentoEmergencia abrir(DadosAberturaEmergencia dados, Usuario socorrista) {
        if (!ambulanciaRepository.existsById(dados.idAmbulancia())) {
            throw new AmbulanciaNotFoundException("ID da ambulância informada não existe!");
        }

        validadoresAbertura.forEach(v -> v.validar(dados));

        Ambulancia ambulancia = ambulanciaRepository.getReferenceById(dados.idAmbulancia());
        Emergencia emergencia = new Emergencia(ambulancia, socorrista.getLogin(), dados);

        List<CalculadoraRota.CruzamentoNaRota> rota = calculadoraRota.calcular(
                emergencia.getOrigem(), emergencia.getDestino(), cruzamentoRepository.findAllByAtivoTrue());
        for (int i = 0; i < rota.size(); i++) {
            CalculadoraRota.CruzamentoNaRota item = rota.get(i);
            emergencia.adicionarNaRota(new EmergenciaCruzamento(
                    emergencia, item.cruzamento(), i + 1, item.projecao().distanciaNaRota(), item.eixo()));
        }

        repository.save(emergencia);
        processarPosicao(emergencia, emergencia.getOrigem());
        return new DetalhamentoEmergencia(emergencia);
    }

    /** Recebe o GPS do app (a cada 1-2 s) e devolve a rota com os cruzamentos ja liberados. */
    @Transactional
    public DetalhamentoEmergencia atualizarPosicao(Long idEmergencia, DadosCoordenada dados, Usuario usuario) {
        Emergencia emergencia = buscarAtiva(idEmergencia);
        validarResponsavel(emergencia, usuario);

        Coordenada posicao = new Coordenada(dados);
        LocalDateTime agora = LocalDateTime.now();
        validadoresPosicao.forEach(v -> v.validar(emergencia, posicao, agora));

        emergencia.registrarPosicao(posicao, agora);
        processarPosicao(emergencia, posicao);
        return new DetalhamentoEmergencia(emergencia);
    }

    @Transactional
    public DetalhamentoEmergencia encerrar(Long idEmergencia, Usuario usuario) {
        Emergencia emergencia = buscarAtiva(idEmergencia);
        if (usuario.getPerfil() == Perfil.SOCORRISTA) {
            validarResponsavel(emergencia, usuario);
        }

        emergencia.getRota().stream()
                .filter(EmergenciaCruzamento::isLiberado)
                .forEach(item -> restaurar(emergencia, item));
        emergencia.encerrar();
        return new DetalhamentoEmergencia(emergencia);
    }

    @Transactional(readOnly = true)
    public DetalhamentoEmergencia detalhar(Long idEmergencia) {
        return new DetalhamentoEmergencia(buscar(idEmergencia));
    }

    @Transactional(readOnly = true)
    public Page<DetalhamentoEmergencia> listar(StatusEmergencia status, Pageable pageable) {
        Page<Emergencia> pagina = status == null
                ? repository.findAllByOrderByDataInicioDesc(pageable)
                : repository.findAllByStatusOrderByDataInicioDesc(status, pageable);
        return pagina.map(DetalhamentoEmergencia::new);
    }

    private void processarPosicao(Emergencia emergencia, Coordenada posicao) {
        double percorrido = Geolocalizacao.projetar(emergencia.getOrigem(), emergencia.getDestino(), posicao).distanciaNaRota();

        for (EmergenciaCruzamento item : emergencia.getRota()) {
            boolean jaPassou = percorrido > item.getDistanciaNaRota() + MARGEM_PASSAGEM_METROS;

            if (item.isLiberado() && jaPassou) {
                restaurar(emergencia, item);
            } else if (item.isAguardando() && !jaPassou) {
                double distancia = Geolocalizacao.distanciaMetros(posicao, item.getCruzamento().getLocalizacao());
                if (distancia <= raioLiberacaoMetros) {
                    liberar(emergencia, item);
                }
            }
        }
    }

    private void liberar(Emergencia emergencia, EmergenciaCruzamento item) {
        Cruzamento cruzamento = item.getCruzamento();
        item.liberar();
        cruzamento.alterarModo(ModoOperacao.EMERGENCIA);
        publicador.publicar(ComandoSemaforo.liberarEmergencia(
                cruzamento.getCodigo(), item.getEixo(), calculadoraPlano.getAmarelo(), emergencia.getId()));
    }

    private void restaurar(Emergencia emergencia, EmergenciaCruzamento item) {
        Cruzamento cruzamento = item.getCruzamento();
        item.restaurar();
        cruzamento.alterarModo(ModoOperacao.ADAPTATIVO);
        publicador.publicar(ComandoSemaforo.restaurar(cruzamento.getCodigo(), ModoOperacao.ADAPTATIVO, emergencia.getId()));
    }

    private void validarResponsavel(Emergencia emergencia, Usuario usuario) {
        if (usuario.getPerfil() != Perfil.ADMIN && !emergencia.getSocorrista().equals(usuario.getLogin())) {
            throw new AcessoNegadoException("Somente o socorrista que abriu a emergência pode atualizá-la!");
        }
    }

    private Emergencia buscarAtiva(Long id) {
        Emergencia emergencia = buscar(id);
        if (!emergencia.isAtiva()) {
            throw new ValidacaoException("Emergência já foi encerrada!");
        }
        return emergencia;
    }

    private Emergencia buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EmergenciaNotFoundException("ID da emergência informada não existe!"));
    }
}
