package com.example.homologacao.Service;

import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario criarUsuario(Usuario usuario) {
        boolean primeiroUsuario = repository.count() == 0;

        if (usuario.getRole() != null && !usuario.getRole().isBlank()
                && !usuario.getRole().equalsIgnoreCase("USER")
                && !usuario.getRole().equalsIgnoreCase("ROLE_USER")
                && !primeiroUsuario) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido definir perfil administrativo no cadastro público"
            );
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRole(primeiroUsuario ? "ADMIN" : "USER");

        return repository.save(usuario);
    }

    @Transactional
    public void garantirAdministradorInicial(String username) {
        if (repository.count() != 1) {
            return;
        }

        repository.findByUsername(username)
                .filter(usuario -> !isAdmin(usuario.getRole()))
                .ifPresent(usuario -> {
                    usuario.setRole("ADMIN");
                    repository.save(usuario);
                });
    }

    public Usuario buscarPorUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private boolean isAdmin(String role) {
        return role != null
                && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ROLE_ADMIN"));
    }
}
