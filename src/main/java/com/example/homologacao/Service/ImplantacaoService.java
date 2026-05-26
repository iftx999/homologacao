package com.example.homologacao.Service;

import com.example.homologacao.Repository.GrupoUsuarioMembroRepository;
import com.example.homologacao.Repository.GrupoUsuarioRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.dto.UsuarioImplantacaoResponse;
import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.GrupoUsuarioMembro;
import com.example.homologacao.model.Enum.StatusImplantacao;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.Implantacao;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImplantacaoService {

    private static final Logger logger = LoggerFactory.getLogger(ImplantacaoService.class);

    private final ImplantacaoRepository implantacaoRepository;
    private final IchoRepository ichoRepository;
    private final ModuloRepository moduloRepository;
    private final GrupoUsuarioRepository grupoUsuarioRepository;
    private final GrupoUsuarioMembroRepository membroRepository;
    private final EmailService emailService;
    private final PermissaoService permissaoService;
    private final String destinatariosAlertaImplantacao;

    public ImplantacaoService(ImplantacaoRepository implantacaoRepository,
                              IchoRepository ichoRepository,
                              ModuloRepository moduloRepository,
                              GrupoUsuarioRepository grupoUsuarioRepository,
                              GrupoUsuarioMembroRepository membroRepository,
                              EmailService emailService,
                              PermissaoService permissaoService,
                              @Value("${app.email.alertas.implantacao.destinatarios:}") String destinatariosAlertaImplantacao) {
        this.implantacaoRepository = implantacaoRepository;
        this.ichoRepository = ichoRepository;
        this.moduloRepository = moduloRepository;
        this.grupoUsuarioRepository = grupoUsuarioRepository;
        this.membroRepository = membroRepository;
        this.emailService = emailService;
        this.permissaoService = permissaoService;
        this.destinatariosAlertaImplantacao = destinatariosAlertaImplantacao;
    }

    // =========================
    // CRUD
    // =========================

    public Implantacao criar(Implantacao implantacao) {
        permissaoService.exigirPermissaoGeral(RecursoSistema.IMPLANTACOES, AcaoPermissao.CRIAR);
        implantacao.setStatus(StatusImplantacao.EM_ANDAMENTO);
        return implantacaoRepository.save(implantacao);
    }

    public Implantacao buscarPorId(Long id) {
        permissaoService.exigirPermissaoNaImplantacao(id, RecursoSistema.IMPLANTACOES, AcaoPermissao.LER);
        return implantacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));
    }

    public List<Implantacao> listar() {
        List<Long> idsPermitidos = permissaoService.buscarImplantacaoIdsPermitidas(
                RecursoSistema.IMPLANTACOES,
                AcaoPermissao.LER
        );

        if (idsPermitidos == null) {
            return implantacaoRepository.findAll();
        }

        if (idsPermitidos.isEmpty()) {
            return List.of();
        }

        return implantacaoRepository.findAllById(idsPermitidos);
    }

    public List<UsuarioImplantacaoResponse> listarUsuariosDisponiveisParaTeste(Long implantacaoId) {
        permissaoService.exigirPermissaoNaImplantacao(
                implantacaoId,
                RecursoSistema.IMPLANTACOES,
                AcaoPermissao.LER
        );

        if (!implantacaoRepository.existsById(implantacaoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Implantação não encontrada");
        }

        Map<Long, UsuarioImplantacaoResponse> usuariosPorId = new LinkedHashMap<>();
        for (GrupoUsuarioMembro membro : membroRepository.findByGrupoImplantacaoId(implantacaoId)) {
            if (membro.getUsuario() == null || membro.getUsuario().getId() == null) {
                continue;
            }
            usuariosPorId.putIfAbsent(membro.getUsuario().getId(), UsuarioImplantacaoResponse.fromMembro(membro));
        }

        return usuariosPorId.values().stream()
                .sorted(Comparator.comparing(this::nomeOrdenacao, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Implantacao atualizar(Long id, Implantacao payload) {
        permissaoService.exigirPermissaoNaImplantacao(id, RecursoSistema.IMPLANTACOES, AcaoPermissao.ALTERAR);

        Implantacao implantacao = implantacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));

        implantacao.setNome(payload.getNome());
        implantacao.setDataGoLive(payload.getDataGoLive());
        implantacao.setObservacao(payload.getObservacao());

        return implantacaoRepository.save(implantacao);
    }

    @Transactional
    public void deletar(Long id) {
        permissaoService.exigirPermissaoNaImplantacao(id, RecursoSistema.IMPLANTACOES, AcaoPermissao.DELETAR);

        Implantacao implantacao = implantacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));

        if (ichoRepository.existsByModuloImplantacaoId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Implantação possui ICHOs cadastrados e não pode ser deletada"
            );
        }

        grupoUsuarioRepository.deleteAll(grupoUsuarioRepository.findByImplantacaoId(id));
        moduloRepository.deleteAll(moduloRepository.findByImplantacaoId(id));
        implantacaoRepository.delete(implantacao);
    }

    // =========================
    // STATUS
    // =========================

    public StatusImplantacao avaliarStatus(Long implantacaoId) {

        permissaoService.exigirPermissaoNaImplantacao(implantacaoId, RecursoSistema.IMPLANTACOES, AcaoPermissao.LER);

        Implantacao implantacao = implantacaoRepository.findById(implantacaoId)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));

        List<Icho> ichos = ichoRepository.findByModuloImplantacaoId(implantacaoId);

        boolean existeFalha = ichos.stream()
                .anyMatch(i -> i.getStatus() == StatusIcho.FALHA);

        boolean existePendencia = ichos.stream()
                .anyMatch(i -> i.getStatus() != StatusIcho.OK);

        if (existeFalha) {
            atualizarStatus(implantacao, StatusImplantacao.REPROVADA);
            return StatusImplantacao.REPROVADA;
        }

        if (existePendencia) {
            atualizarStatus(implantacao, StatusImplantacao.EM_HOMOLOGACAO);
            return StatusImplantacao.EM_HOMOLOGACAO;
        }

        atualizarStatus(implantacao, StatusImplantacao.APROVADA);
        return StatusImplantacao.APROVADA;
    }

    private void atualizarStatus(Implantacao implantacao, StatusImplantacao status) {
        implantacao.setStatus(status);
        implantacaoRepository.save(implantacao);
    }

    // =========================
    // GO LIVE CHECK
    // =========================

    public boolean possuiPendenciasAposGoLive(Long implantacaoId) {

        permissaoService.exigirPermissaoNaImplantacao(implantacaoId, RecursoSistema.IMPLANTACOES, AcaoPermissao.LER);

        Implantacao imp = implantacaoRepository.findById(implantacaoId)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));

        if (LocalDate.now().isBefore(imp.getDataGoLive())) {
            return false;
        }

        return ichoRepository.existsByModuloImplantacaoIdAndStatusIn(
                implantacaoId,
                statusComPendenciaAposGoLive()
        );
    }

    // =========================
    // VALIDACAO (SCHEDULER)
    // =========================

    public void validarImplantacoes() {

        List<Implantacao> implantacoes =
                implantacaoRepository.findByStatus(StatusImplantacao.EM_HOMOLOGACAO);

        for (Implantacao implantacao : implantacoes) {

            if (LocalDate.now().isBefore(implantacao.getDataGoLive())) {
                continue;
            }

            boolean possuiPendencias =
                    ichoRepository.existsByModuloImplantacaoIdAndStatusIn(
                            implantacao.getId(),
                            statusComPendenciaAposGoLive()
                    );

            if (possuiPendencias) {
                implantacao.setStatus(StatusImplantacao.REPROVADA);
                enviarEmailPendencia(implantacao);
            } else {
                implantacao.setStatus(StatusImplantacao.APROVADA);
            }

            implantacaoRepository.save(implantacao);
        }
    }

    private List<StatusIcho> statusComPendenciaAposGoLive() {
        return List.of(
                StatusIcho.NAO_TESTADO,
                StatusIcho.EM_TESTE,
                StatusIcho.PENDENTE,
                StatusIcho.FALHA,
                StatusIcho.EM_CORRECAO,
                StatusIcho.RETESTE
        );
    }

    private String nomeOrdenacao(UsuarioImplantacaoResponse usuario) {
        if (usuario.nome() != null && !usuario.nome().isBlank()) {
            return usuario.nome();
        }
        if (usuario.username() != null && !usuario.username().isBlank()) {
            return usuario.username();
        }
        return "";
    }

    // =========================
    // EMAIL
    // =========================

    private void enviarEmailPendencia(Implantacao implantacao) {

        String corpo = """
                A implantação "%s" não foi homologada.

                Data do Go Live: %s

                Existem ICHOs pendentes de validação.

                Favor verificar o sistema.
                """.formatted(
                implantacao.getNome(),
                implantacao.getDataGoLive()
        );

        try {
            emailService.enviarEmail(
                    destinatariosAlertaImplantacao,
                    "Implantacao nao homologada",
                    corpo
            );
        } catch (IllegalArgumentException | MailException e) {
            logger.error("Falha ao enviar alerta da implantacao {}", implantacao.getId(), e);
        }
    }
}
