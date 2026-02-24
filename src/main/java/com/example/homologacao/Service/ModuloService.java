package com.example.homologacao.Service;

import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.model.Modulo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Modulo> buscarPorId(Long id) {
        return repository.findById(id);
    }
    public List<Modulo> listarPorImplantacao(Long id) {
        return repository.findByImplantacaoId(id);
    }
}
