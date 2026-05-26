package com.example.homologacao.Service;

import com.example.homologacao.Repository.GrupoUsuarioMembroRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.IchoHistoricoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoHistorico;
import com.example.homologacao.model.Modulo;
import com.example.homologacao.model.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;


@Service
public class IchoService {

    private final IchoRepository repository;
    private final IchoHistoricoRepository historicoRepository;
    private final UserRepository userRepository;
    private final ImplantacaoService implantacaoService;
    private final ModuloRepository moduloRepository;
    private final GrupoUsuarioMembroRepository membroRepository;

    private static final Map<StatusIcho, EnumSet<StatusIcho>> TRANSICOES_PERMITIDAS =
            new EnumMap<>(StatusIcho.class);

    static {
        TRANSICOES_PERMITIDAS.put(
                StatusIcho.NAO_TESTADO,
                EnumSet.of(StatusIcho.EM_TESTE, StatusIcho.PENDENTE, StatusIcho.FALHA, StatusIcho.OK)
        );
        TRANSICOES_PERMITIDAS.put(
                StatusIcho.EM_TESTE,
                EnumSet.of(StatusIcho.PENDENTE, StatusIcho.FALHA, StatusIcho.OK)
        );
        TRANSICOES_PERMITIDAS.put(
                StatusIcho.PENDENTE,
                EnumSet.of(StatusIcho.EM_TESTE, StatusIcho.FALHA, StatusIcho.OK)
        );
        TRANSICOES_PERMITIDAS.put(
                StatusIcho.FALHA,
                EnumSet.of(StatusIcho.EM_CORRECAO, StatusIcho.RETESTE, StatusIcho.PENDENTE, StatusIcho.OK)
        );
        TRANSICOES_PERMITIDAS.put(
                StatusIcho.EM_CORRECAO,
                EnumSet.of(StatusIcho.RETESTE, StatusIcho.EM_TESTE, StatusIcho.PENDENTE)
        );
        TRANSICOES_PERMITIDAS.put(
                StatusIcho.RETESTE,
                EnumSet.of(StatusIcho.EM_TESTE, StatusIcho.PENDENTE, StatusIcho.FALHA, StatusIcho.OK)
        );
        TRANSICOES_PERMITIDAS.put(
                StatusIcho.OK,
                EnumSet.of(StatusIcho.EM_TESTE, StatusIcho.PENDENTE, StatusIcho.FALHA)
        );
    }

    public IchoService(IchoRepository repository,
                       IchoHistoricoRepository historicoRepository,
                       UserRepository userRepository,
                       ImplantacaoService implantacaoService,
                       ModuloRepository moduloRepository,
                       GrupoUsuarioMembroRepository membroRepository) {
        this.repository = repository;
        this.historicoRepository = historicoRepository;
        this.userRepository = userRepository;
        this.implantacaoService = implantacaoService;
        this.moduloRepository = moduloRepository;
        this.membroRepository = membroRepository;
    }

    @Transactional
    public Icho criar(Icho icho) {
        Modulo modulo = resolverModulo(icho.getModulo());
        icho.setModulo(modulo);
        aplicarResponsavelDoPayload(icho, icho, modulo);
        icho.setStatus(StatusIcho.NAO_TESTADO);
        icho.setDataTeste(null);

        Icho salvo = repository.save(icho);
        registrarHistorico(salvo, null, StatusIcho.NAO_TESTADO, salvo.getTestadoPor(), "ICHO criada");
        return salvo;
    }

    @Transactional
    public Icho atualizarStatus(Long id, StatusIcho status, String usuario) {
        return atualizarStatus(id, status, usuario, null);
    }

    @Transactional
    public Icho iniciarTeste(Long id, String usuario, String observacao) {
        return atualizarStatus(id, StatusIcho.EM_TESTE, usuario, observacao);
    }

    @Transactional
    public Icho reprovar(Long id, String usuario, String observacao) {
        return atualizarStatus(id, StatusIcho.FALHA, usuario, observacao);
    }

    @Transactional
    public Icho registrarCorrecao(Long id, String usuario, String observacao) {
        return atualizarStatus(id, StatusIcho.EM_CORRECAO, usuario, observacao);
    }

    @Transactional
    public Icho retestar(Long id, String usuario, String observacao) {
        return atualizarStatus(id, StatusIcho.RETESTE, usuario, observacao);
    }

    @Transactional
    public Icho finalizar(Long id, String usuario, String observacao) {
        return atualizarStatus(id, StatusIcho.OK, usuario, observacao);
    }

    @Transactional
    public Icho atualizarStatus(Long id, StatusIcho status, String usuario, String observacao) {
        return atualizarStatus(id, status, usuario, observacao, null);
    }

    @Transactional
    public Icho atualizarStatus(Long id,
                                StatusIcho status,
                                String usuario,
                                String observacao,
                                Long testadoPorUsuarioId) {
        Icho icho = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ICHO não encontrado"));

        StatusIcho statusAnterior = icho.getStatus();
        validarTransicao(statusAnterior, status);

        Usuario usuarioTeste = resolverUsuarioTeste(testadoPorUsuarioId, icho);
        String usuarioResponsavel = usuarioTeste == null
                ? resolverUsuarioResponsavel(usuario)
                : usuarioTeste.getUsername();
        icho.setStatus(status);
        icho.setTestadoPor(usuarioResponsavel);
        icho.setTestadoPorUsuario(usuarioTeste);
        icho.setDataTeste(LocalDate.now());
        icho.setObservacao(observacao);

        Icho salvo = repository.save(icho);
        if (statusAnterior != status) {
            registrarHistorico(salvo, statusAnterior, status, usuarioResponsavel, observacao);
            reavaliarCicloVidaImplantacao(salvo);
        } else {
            registrarHistoricoInicialSeNecessario(salvo, status, usuarioResponsavel, observacao);
        }

        return salvo;
    }

    @Transactional
    public Icho atualizar(Long id, Icho payload) {
        Icho icho = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ICHO não encontrado"));

        StatusIcho statusAnterior = icho.getStatus();
        StatusIcho novoStatus = payload.getStatus();
        Modulo modulo = payload.getModulo() != null ? resolverModulo(payload.getModulo()) : icho.getModulo();

        if (novoStatus != null) {
            validarTransicao(statusAnterior, novoStatus);
            icho.setStatus(novoStatus);
        }

        if (payload.getTitulo() != null) {
            icho.setTitulo(payload.getTitulo());
        }
        if (payload.getDescricao() != null) {
            icho.setDescricao(payload.getDescricao());
        }
        if (payload.getModulo() != null) {
            icho.setModulo(modulo);
        }
        if (payload.getDataTeste() != null) {
            icho.setDataTeste(payload.getDataTeste());
        } else if (novoStatus != null && statusAnterior != novoStatus) {
            icho.setDataTeste(LocalDate.now());
        }
        aplicarResponsavelDoPayload(icho, payload, modulo);
        if (payload.getObservacao() != null) {
            icho.setObservacao(payload.getObservacao());
        }

        String usuarioResponsavel = resolverUsuarioResponsavel(
                payload.getTestadoPor() != null ? payload.getTestadoPor() : icho.getTestadoPor()
        );

        Icho salvo = repository.save(icho);
        if (novoStatus != null && statusAnterior != novoStatus) {
            registrarHistorico(salvo, statusAnterior, novoStatus, usuarioResponsavel, payload.getObservacao());
            reavaliarCicloVidaImplantacao(salvo);
        } else if (novoStatus != null) {
            registrarHistoricoInicialSeNecessario(salvo, novoStatus, usuarioResponsavel, payload.getObservacao());
        }

        return salvo;
    }

    public List<Icho> listarPendentes() {
        return repository.buscarPendentesComGoLiveEstourado(
                List.of(
                                StatusIcho.NAO_TESTADO,
                                StatusIcho.EM_TESTE,
                                StatusIcho.PENDENTE,
                                StatusIcho.FALHA,
                                StatusIcho.EM_CORRECAO,
                                StatusIcho.RETESTE
                        )
                        .stream()
                        .map(StatusIcho::name)
                        .toList()
        );
    }

    public List<Icho> buscarPorModulo(Long moduloId) {
        return repository.findByModuloId(moduloId);
    }

    public List<IchoHistorico> listarHistorico(Long ichoId) {
        if (!repository.existsById(ichoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ICHO não encontrado");
        }

        return historicoRepository.findByIchoIdOrderByDataAlteracaoAsc(ichoId);
    }

    private void validarTransicao(StatusIcho statusAtual, StatusIcho novoStatus) {
        if (statusAtual == null) {
            if (novoStatus != StatusIcho.NAO_TESTADO) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A criação da ICHO deve iniciar em NAO_TESTADO"
                );
            }
            return;
        }

        if (statusAtual == novoStatus) {
            return;
        }

        EnumSet<StatusIcho> destinosPermitidos = TRANSICOES_PERMITIDAS.get(statusAtual);
        if (destinosPermitidos == null || !destinosPermitidos.contains(novoStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transição de status inválida: %s -> %s".formatted(statusAtual, novoStatus)
            );
        }
    }

    private void registrarHistorico(Icho icho,
                                    StatusIcho statusAnterior,
                                    StatusIcho novoStatus,
                                    String usuarioResponsavel,
                                    String observacao) {
        IchoHistorico historico = new IchoHistorico();
        historico.setIcho(icho);
        historico.setStatusAnterior(statusAnterior);
        historico.setNovoStatus(novoStatus);
        historico.setObservacao(observacao);
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setUsuarioNome(usuarioResponsavel);
        historico.setUsuario(buscarUsuarioSeExistir(usuarioResponsavel));
        historicoRepository.save(historico);
    }

    private void registrarHistoricoInicialSeNecessario(Icho icho,
                                                       StatusIcho status,
                                                       String usuarioResponsavel,
                                                       String observacao) {
        if (icho.getId() == null || historicoRepository.existsByIchoId(icho.getId())) {
            return;
        }

        registrarHistorico(icho, null, status, usuarioResponsavel, observacao);
    }

    private Usuario buscarUsuarioSeExistir(String usuarioResponsavel) {
        if (usuarioResponsavel == null || usuarioResponsavel.isBlank()) {
            return null;
        }

        return userRepository.findByUsername(usuarioResponsavel).orElse(null);
    }

    private void aplicarResponsavelDoPayload(Icho destino, Icho payload, Modulo modulo) {
        Long usuarioId = payload.getTestadoPorUsuarioId();
        if (usuarioId != null) {
            Usuario usuario = resolverUsuarioTeste(usuarioId, modulo);
            destino.setTestadoPorUsuario(usuario);
            destino.setTestadoPor(usuario.getUsername());
            return;
        }

        if (payload.getTestadoPor() == null) {
            return;
        }

        destino.setTestadoPor(payload.getTestadoPor());
        userRepository.findByUsername(payload.getTestadoPor())
                .filter(usuario -> pertenceAImplantacao(usuario.getId(), obterImplantacaoId(modulo)))
                .ifPresent(destino::setTestadoPorUsuario);
    }

    private Usuario resolverUsuarioTeste(Long usuarioId, Icho icho) {
        if (usuarioId == null) {
            return null;
        }
        return resolverUsuarioTeste(usuarioId, icho.getModulo());
    }

    private Usuario resolverUsuarioTeste(Long usuarioId, Modulo modulo) {
        Usuario usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário responsável pelo teste não encontrado"));

        Long implantacaoId = obterImplantacaoId(modulo);
        if (!pertenceAImplantacao(usuarioId, implantacaoId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usuário responsável pelo teste não pertence à implantação"
            );
        }

        return usuario;
    }

    private boolean pertenceAImplantacao(Long usuarioId, Long implantacaoId) {
        return usuarioId != null
                && implantacaoId != null
                && membroRepository.existsByGrupoImplantacaoIdAndUsuarioId(implantacaoId, usuarioId);
    }

    private Long obterImplantacaoId(Modulo modulo) {
        if (modulo == null || modulo.getImplantacao() == null) {
            return null;
        }
        return modulo.getImplantacao().getId();
    }

    private Modulo resolverModulo(Modulo modulo) {
        if (modulo == null || modulo.getId() == null) {
            return modulo;
        }

        return moduloRepository.findById(modulo.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo não encontrado"));
    }

    private void reavaliarCicloVidaImplantacao(Icho icho) {
        if (icho.getModulo() == null || icho.getModulo().getImplantacao() == null) {
            return;
        }

        Long implantacaoId = icho.getModulo().getImplantacao().getId();
        if (implantacaoId != null) {
            implantacaoService.avaliarStatus(implantacaoId);
        }
    }

    private String resolverUsuarioResponsavel(String usuarioInformado) {
        if (usuarioInformado != null && !usuarioInformado.isBlank()) {
            return usuarioInformado;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }

        return "sistema";
    }
}
