package com.example.homologacao.Service;

import com.example.homologacao.Repository.GrupoUsuarioRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.Implantacao;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ImplantacaoService {

    private final ImplantacaoRepository implantacaoRepository;
    private final IchoRepository ichoRepository;
    private final ModuloRepository moduloRepository;
    private final GrupoUsuarioRepository grupoUsuarioRepository;
    private final EmailService emailService;
    private final PermissaoService permissaoService;

    public ImplantacaoService(ImplantacaoRepository implantacaoRepository,
                              IchoRepository ichoRepository,
                              ModuloRepository moduloRepository,
                              GrupoUsuarioRepository grupoUsuarioRepository,
                              EmailService emailService,
                              PermissaoService permissaoService) {
        this.implantacaoRepository = implantacaoRepository;
        this.ichoRepository = ichoRepository;
        this.moduloRepository = moduloRepository;
        this.grupoUsuarioRepository = grupoUsuarioRepository;
        this.emailService = emailService;
        this.permissaoService = permissaoService;
    }

    // =========================
    // CRUD
    // =========================

    public Implantacao criar(Implantacao implantacao) {
        permissaoService.exigirPermissaoGeral(RecursoSistema.IMPLANTACOES, AcaoPermissao.CRIAR);
        implantacao.setStatus(Model.Enum.StatusImplantacao.EM_ANDAMENTO);
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

    public Model.Enum.StatusImplantacao avaliarStatus(Long implantacaoId) {

        permissaoService.exigirPermissaoNaImplantacao(implantacaoId, RecursoSistema.IMPLANTACOES, AcaoPermissao.LER);

        Implantacao implantacao = implantacaoRepository.findById(implantacaoId)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));

        List<Icho> ichos = ichoRepository.findByModuloImplantacaoId(implantacaoId);

        boolean existeFalha = ichos.stream()
                .anyMatch(i -> i.getStatus() == StatusIcho.FALHA);

        boolean existePendencia = ichos.stream()
                .anyMatch(i -> i.getStatus() != StatusIcho.OK);

        if (existeFalha) {
            atualizarStatus(implantacao, Model.Enum.StatusImplantacao.REPROVADA);
            return Model.Enum.StatusImplantacao.REPROVADA;
        }

        if (existePendencia) {
            atualizarStatus(implantacao, Model.Enum.StatusImplantacao.EM_HOMOLOGACAO);
            return Model.Enum.StatusImplantacao.EM_HOMOLOGACAO;
        }

        atualizarStatus(implantacao, Model.Enum.StatusImplantacao.APROVADA);
        return Model.Enum.StatusImplantacao.APROVADA;
    }

    private void atualizarStatus(Implantacao implantacao, Model.Enum.StatusImplantacao status) {
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
                implantacaoRepository.findByStatus(Model.Enum.StatusImplantacao.EM_HOMOLOGACAO);

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
                implantacao.setStatus(Model.Enum.StatusImplantacao.REPROVADA);
            } else {
                implantacao.setStatus(Model.Enum.StatusImplantacao.APROVADA);
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

        emailService.enviarEmail(
                "ti@empresa.com",
                "🚨 Implantação NÃO homologada",
                corpo
        );
    }
}
