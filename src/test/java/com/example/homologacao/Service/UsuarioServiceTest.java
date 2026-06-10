package com.example.homologacao.Service;

import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void primeiroUsuarioCriadoRecebePerfilAdmin() {
        Usuario usuario = usuario("admin", "senha");
        when(repository.count()).thenReturn(0L);
        when(passwordEncoder.encode("senha")).thenReturn("senha-criptografada");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario salvo = criarService().criarUsuario(usuario);

        assertThat(salvo.getRole()).isEqualTo("ADMIN");
        assertThat(salvo.getPassword()).isEqualTo("senha-criptografada");
        verify(repository).save(usuario);
    }

    @Test
    void usuarioPosteriorNaoPodeDefinirPerfilAdminNoCadastroPublico() {
        Usuario usuario = usuario("maria", "senha");
        usuario.setRole("ADMIN");
        when(repository.count()).thenReturn(1L);

        assertThatThrownBy(() -> criarService().criarUsuario(usuario))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("perfil administrativo");
    }

    @Test
    void loginPromoveUsuarioUnicoExistenteParaAdmin() {
        Usuario usuario = usuario("admin", "senha");
        usuario.setRole("USER");
        when(repository.count()).thenReturn(1L);
        when(repository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        criarService().garantirAdministradorInicial("admin");

        assertThat(usuario.getRole()).isEqualTo("ADMIN");
        verify(repository).save(usuario);
    }

    private UsuarioService criarService() {
        return new UsuarioService(repository, passwordEncoder);
    }

    private Usuario usuario(String username, String password) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password);
        return usuario;
    }
}
