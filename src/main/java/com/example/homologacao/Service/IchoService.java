package Service;

import Model.Enum.StatusIcho;
import Model.Icho;
import Model.Implantacao;
import Repository.IchoRepository;
import Repository.ImplantacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class IchoService {

    private final IchoRepository repository;

    private final ImplantacaoRepository implantacaoRepository;

    private final IchoRepository ichoRepository;

    public IchoService(IchoRepository repository, ImplantacaoRepository implantacaoRepository, IchoRepository ichoRepository) {
        this.repository = repository;
        this.implantacaoRepository = implantacaoRepository;
        this.ichoRepository = ichoRepository;
    }

    public Icho criar(Icho icho) {
        icho.setStatus(StatusIcho.NAO_TESTADO);
        return repository.save(icho);
    }

    public Icho atualizarStatus(Long id, StatusIcho status, String usuario) {
        Icho icho = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ICHO não encontrado"));

        icho.setStatus(status);
        icho.setTestadoPor(usuario);
        icho.setDataTeste(LocalDate.now());

        return repository.save(icho);
    }

    public List<Icho> listarPorImplantacao(Long implantacaoId) {
        return repository.findByImplantacaoId(implantacaoId);
    }

    public List<Icho> listarPendentes() {
        return repository.buscarPendentesComGoLiveEstourado();
    }

    public boolean implantacaoEstaValida(Long implantacaoId) {

        Implantacao implantacao = implantacaoRepository.findById(implantacaoId)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));

        // Se ainda não chegou no go-live, não bloqueia
        if (LocalDate.now().isBefore(implantacao.getDataGoLive())) {
            return true;
        }

        // Verifica se existe ICHO não testado
        return ichoRepository.findByImplantacaoId(implantacaoId)
                .stream()
                .noneMatch(i ->
                        i.getStatus() == StatusIcho.NAO_TESTADO ||
                                i.getStatus() == StatusIcho.PENDENTE
                );
    }
}
