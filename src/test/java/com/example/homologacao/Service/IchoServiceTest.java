package com.example.homologacao.Service;

import com.example.homologacao.Repository.GrupoUsuarioMembroRepository;
import com.example.homologacao.Repository.IchoHistoricoRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.Implantacao;
import com.example.homologacao.model.Modulo;
import com.example.homologacao.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IchoServiceTest {

    @Mock
    private IchoRepository repository;

    @Mock
    private IchoHistoricoRepository historicoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImplantacaoService implantacaoService;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private GrupoUsuarioMembroRepository membroRepository;

    @Test
    void atualizarStatusComResponsavelPorIdValidaMembroDaImplantacao() {
        Icho icho = icho(10L, StatusIcho.NAO_TESTADO);
        Usuario usuario = usuario(1L, "joao", "João Silva");

        when(repository.findById(10L)).thenReturn(Optional.of(icho));
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(membroRepository.existsByGrupoImplantacaoIdAndUsuarioId(42L, 1L)).thenReturn(true);
        when(repository.save(any(Icho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Icho salvo = criarService().atualizarStatus(10L, StatusIcho.EM_TESTE, null, "Início", 1L);

        assertThat(salvo.getTestadoPor()).isEqualTo("joao");
        assertThat(salvo.getTestadoPorUsuarioId()).isEqualTo(1L);
        assertThat(salvo.getTestadoPorNome()).isEqualTo("João Silva");

        ArgumentCaptor<Icho> captor = ArgumentCaptor.forClass(Icho.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTestadoPorUsuario()).isSameAs(usuario);
    }

    @Test
    void rejeitaResponsavelPorIdForaDaImplantacao() {
        Icho icho = icho(10L, StatusIcho.NAO_TESTADO);
        Usuario usuario = usuario(1L, "joao", "João Silva");

        when(repository.findById(10L)).thenReturn(Optional.of(icho));
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(membroRepository.existsByGrupoImplantacaoIdAndUsuarioId(42L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> criarService().atualizarStatus(10L, StatusIcho.EM_TESTE, null, null, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não pertence à implantação");
    }

    private IchoService criarService() {
        return new IchoService(
                repository,
                historicoRepository,
                userRepository,
                implantacaoService,
                moduloRepository,
                membroRepository
        );
    }

    private Icho icho(Long id, StatusIcho status) {
        Implantacao implantacao = new Implantacao();
        implantacao.setId(42L);

        Modulo modulo = new Modulo();
        modulo.setId(5L);
        modulo.setImplantacao(implantacao);

        Icho icho = new Icho();
        icho.setId(id);
        icho.setTitulo("Validar login");
        icho.setStatus(status);
        icho.setModulo(modulo);
        return icho;
    }

    private Usuario usuario(Long id, String username, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setNome(nome);
        return usuario;
    }
}
