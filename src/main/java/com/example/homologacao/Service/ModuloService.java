package Service;

import Model.Modulo;
import Repository.ModuloRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuloService {

    private final ModuloRepository repository;

    public ModuloService(ModuloRepository repository) {
        this.repository = repository;
    }

    public Modulo salvar(Modulo modulo) {
        return repository.save(modulo);
    }

    public List<Modulo> listarTodos() {
        return repository.findAll();
    }
}
