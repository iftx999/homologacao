package Service;

import Model.Enum.StatusIcho;
import Model.Enum.StatusImplantacao;
import Model.Icho;
import Model.Implantacao;
import Repository.IchoRepository;
import Repository.ImplantacaoRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ImplantacaoService {

    private final ImplantacaoRepository implantacaoRepository;
    private final IchoRepository ichoRepository;
    private final EmailService emailService;

    public ImplantacaoService(ImplantacaoRepository implantacaoRepository,
                              IchoRepository ichoRepository, EmailService emailService) {
        this.implantacaoRepository = implantacaoRepository;
        this.ichoRepository = ichoRepository;
        this.emailService = emailService;
    }

    public Implantacao criar(Implantacao implantacao) {
        implantacao.setStatus(StatusImplantacao.EM_ANDAMENTO);
        return implantacaoRepository.save(implantacao);
    }

    public Implantacao buscarPorId(Long id) {
        return implantacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));
    }

    public StatusImplantacao avaliarStatus(Long implantacaoId) {

        List<Icho> ichos = ichoRepository.findByImplantacaoId(implantacaoId);

        boolean existeFalha = ichos.stream()
                .anyMatch(i -> i.getStatus() == StatusIcho.FALHA);

        boolean existePendente = ichos.stream()
                .anyMatch(i -> i.getStatus() != StatusIcho.OK);

        if (existeFalha) {
            atualizarStatus(implantacaoId, StatusImplantacao.REPROVADA);
            return StatusImplantacao.REPROVADA;
        }

        if (existePendente) {
            atualizarStatus(implantacaoId, StatusImplantacao.EM_HOMOLOGACAO);
            return StatusImplantacao.EM_HOMOLOGACAO;
        }

        atualizarStatus(implantacaoId, StatusImplantacao.APROVADA);
        return StatusImplantacao.APROVADA;
    }

    private void atualizarStatus(Long id, StatusImplantacao status) {
        Implantacao impl = buscarPorId(id);
        impl.setStatus(status);
        implantacaoRepository.save(impl);
    }

    public boolean possuiPendenciasAposGoLive(Long implantacaoId) {
        Implantacao imp = buscarPorId(implantacaoId);

        if (LocalDate.now().isBefore(imp.getDataGoLive())) {
            return false;
        }

        return ichoRepository.findByImplantacaoId(implantacaoId)
                .stream()
                .anyMatch(i -> i.getStatus() != StatusIcho.OK);
    }
    public void validarImplantacoes() {

        List<Implantacao> implantacoes =
                implantacaoRepository.findByStatus(StatusImplantacao.EM_HOMOLOGACAO);

        for (Implantacao implantacao : implantacoes) {

            // Se ainda não chegou no go-live → ignora
            if (LocalDate.now().isBefore(implantacao.getDataGoLive())) {
                continue;
            }

            boolean possuiPendencias =
                    ichoRepository.existsByImplantacaoIdAndStatusIn(
                            implantacao.getId(),
                            List.of(StatusIcho.NAO_TESTADO, StatusIcho.PENDENTE)
                    );

            if (possuiPendencias) {
                implantacao.setStatus(StatusImplantacao.REPROVADA);
            } else {
                implantacao.setStatus(StatusImplantacao.APROVADA);
            }

            implantacaoRepository.save(implantacao);
        }
    }

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