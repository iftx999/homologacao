package com.example.homologacao.Repository;

import com.example.homologacao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}