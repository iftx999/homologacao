package com.example.homologacao.Service;

import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario criarUsuario(Usuario usuario) {

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return repository.save(usuario);
    }

    public Usuario buscarPorUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
