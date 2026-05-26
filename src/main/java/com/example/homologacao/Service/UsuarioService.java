package com.example.homologacao.Service;

import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario criarUsuario(Usuario usuario) {
        if (usuario.getRole() != null && !usuario.getRole().isBlank()
                && !usuario.getRole().equalsIgnoreCase("USER")
                && !usuario.getRole().equalsIgnoreCase("ROLE_USER")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é permitido definir perfil administrativo no cadastro público"
            );
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRole("USER");

        return repository.save(usuario);
    }

    public Usuario buscarPorUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
