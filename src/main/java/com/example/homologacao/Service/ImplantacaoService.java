package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.Implantacao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ImplantacaoService {

    private final ImplantacaoRepository implantacaoRepository;
    private final IchoRepository ichoRepository;
    private final EmailService emailService;

    public ImplantacaoService(ImplantacaoRepository implantacaoRepository,
                              IchoRepository ichoRepository,
                              EmailService emailService) {
        this.implantacaoRepository = implantacaoRepository;
        this.ichoRepository = ichoRepository;
        this.emailService = emailService;
    }

    // =========================
    // CRUD
    // =========================

    public Implantacao criar(Implantacao implantacao) {
        implantacao.setStatus(Model.Enum.StatusImplantacao.EM_ANDAMENTO);
        return implantacaoRepository.save(implantacao);
    }

    public Implantacao buscarPorId(Long id) {
        return implantacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));
    }

    public List<Implantacao> listar() {
        return implantacaoRepository.findAll();
    }

    // =========================
    // STATUS
    // =========================

    public Model.Enum.StatusImplantacao avaliarStatus(Long implantacaoId) {

        Implantacao implantacao = buscarPorId(implantacaoId);

        List<Icho> ichos = ichoRepository.findByModuloId(implantacaoId);

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

        Implantacao imp = buscarPorId(implantacaoId);

        if (LocalDate.now().isBefore(imp.getDataGoLive())) {
            return false;
        }

        return ichoRepository.existsByModuloIdAndStatusIn(
                implantacaoId,
                List.of(StatusIcho.NAO_TESTADO, StatusIcho.PENDENTE)
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
                    ichoRepository.existsByModuloIdAndStatusIn(
                            implantacao.getId(),
                            List.of(StatusIcho.NAO_TESTADO, StatusIcho.PENDENTE)
                    );

            if (possuiPendencias) {
                implantacao.setStatus(Model.Enum.StatusImplantacao.REPROVADA);
            } else {
                implantacao.setStatus(Model.Enum.StatusImplantacao.APROVADA);
            }

            implantacaoRepository.save(implantacao);
        }
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