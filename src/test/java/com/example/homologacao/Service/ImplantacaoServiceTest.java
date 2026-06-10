package com.example.homologacao.Service;

import com.example.homologacao.Repository.GrupoUsuarioMembroRepository;
import com.example.homologacao.Repository.GrupoUsuarioRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.dto.UsuarioImplantacaoResponse;
import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.GrupoUsuario;
import com.example.homologacao.model.GrupoUsuarioMembro;
import com.example.homologacao.model.Enum.StatusImplantacao;
import com.example.homologacao.model.Implantacao;
import com.example.homologacao.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImplantacaoServiceTest {

    @Mock
    private ImplantacaoRepository implantacaoRepository;

    @Mock
    private IchoRepository ichoRepository;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private GrupoUsuarioRepository grupoUsuarioRepository;

    @Mock
    private GrupoUsuarioMembroRepository membroRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PermissaoService permissaoService;

    @Test
    void reprovaImplantacaoComPendenciaEEnviaAlerta() {
        Implantacao implantacao = emHomologacaoComGoLiveVencido();
        when(implantacaoRepository.findByStatus(StatusImplantacao.EM_HOMOLOGACAO))
                .thenReturn(List.of(implantacao));
        when(ichoRepository.existsByModuloImplantacaoIdAndStatusIn(eq(implantacao.getId()), anyList()))
                .thenReturn(true);

        criarService().validarImplantacoes();

        assertThat(implantacao.getStatus()).isEqualTo(StatusImplantacao.REPROVADA);
        verify(emailService).enviarEmail(
                "ti@empresa.com,qa@empresa.com",
                "Implantacao nao homologada",
                """
                A implantação "ERP" não foi homologada.

                Data do Go Live: 2026-05-20

                Existem ICHOs pendentes de validação.

                Favor verificar o sistema.
                """
        );
        verify(implantacaoRepository).save(implantacao);
    }

    @Test
    void mantemAtualizacaoDeStatusQuandoAlertaFalha() {
        Implantacao implantacao = emHomologacaoComGoLiveVencido();
        when(implantacaoRepository.findByStatus(StatusImplantacao.EM_HOMOLOGACAO))
                .thenReturn(List.of(implantacao));
        when(ichoRepository.existsByModuloImplantacaoIdAndStatusIn(eq(implantacao.getId()), anyList()))
                .thenReturn(true);
        doThrow(new MailSendException("smtp indisponivel"))
                .when(emailService)
                .enviarEmail(
                        "ti@empresa.com,qa@empresa.com",
                        "Implantacao nao homologada",
                        """
                        A implantação "ERP" não foi homologada.

                        Data do Go Live: 2026-05-20

                        Existem ICHOs pendentes de validação.

                        Favor verificar o sistema.
                        """
                );

        criarService().validarImplantacoes();

        assertThat(implantacao.getStatus()).isEqualTo(StatusImplantacao.REPROVADA);
        verify(implantacaoRepository).save(implantacao);
    }

    @Test
    void listaUsuariosDisponiveisParaTesteSemDuplicarEOrdenados() {
        when(implantacaoRepository.existsById(42L)).thenReturn(true);

        GrupoUsuario grupo = new GrupoUsuario();
        grupo.setImplantacao(emHomologacaoComGoLiveVencido());

        Usuario maria = usuario(2L, "maria", "Maria Silva", "maria@email.com");
        Usuario joao = usuario(1L, "joao", "João Silva", "joao@email.com");

        when(membroRepository.findByGrupoImplantacaoId(42L)).thenReturn(List.of(
                membro(grupo, maria, "Analista"),
                membro(grupo, joao, "Técnico"),
                membro(grupo, joao, "QA")
        ));

        List<UsuarioImplantacaoResponse> usuarios = criarService().listarUsuariosDisponiveisParaTeste(42L);

        assertThat(usuarios)
                .extracting(UsuarioImplantacaoResponse::usuarioId)
                .containsExactly(1L, 2L);
        assertThat(usuarios.get(0).username()).isEqualTo("joao");
        assertThat(usuarios.get(0).funcao()).isEqualTo("Técnico");
        verify(permissaoService).exigirPermissaoNaImplantacao(
                42L,
                com.example.homologacao.model.Enum.RecursoSistema.IMPLANTACOES,
                com.example.homologacao.model.Enum.AcaoPermissao.LER
        );
    }

    @Test
    void finalizaImplantacaoSemPendenciasAposGoLive() {
        Implantacao implantacao = emHomologacaoComGoLiveVencido();
        implantacao.setStatus(StatusImplantacao.APROVADA);
        when(implantacaoRepository.findById(42L)).thenReturn(Optional.of(implantacao));
        when(ichoRepository.existsByModuloImplantacaoIdAndStatusIn(eq(42L), anyList()))
                .thenReturn(false);
        when(implantacaoRepository.save(implantacao)).thenReturn(implantacao);

        Implantacao finalizada = criarService().finalizar(42L);

        assertThat(finalizada.getStatus()).isEqualTo(StatusImplantacao.FINALIZADA);
        verify(permissaoService).exigirPermissaoNaImplantacao(
                42L,
                RecursoSistema.IMPLANTACOES,
                AcaoPermissao.ALTERAR
        );
        verify(implantacaoRepository).save(implantacao);
    }

    @Test
    void naoFinalizaImplantacaoComPendencias() {
        Implantacao implantacao = emHomologacaoComGoLiveVencido();
        implantacao.setStatus(StatusImplantacao.APROVADA);
        when(implantacaoRepository.findById(42L)).thenReturn(Optional.of(implantacao));
        when(ichoRepository.existsByModuloImplantacaoIdAndStatusIn(eq(42L), anyList()))
                .thenReturn(true);

        assertThatThrownBy(() -> criarService().finalizar(42L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("ICHOs pendentes");

        assertThat(implantacao.getStatus()).isEqualTo(StatusImplantacao.EM_HOMOLOGACAO);
        verify(implantacaoRepository).save(implantacao);
    }

    private ImplantacaoService criarService() {
        return new ImplantacaoService(
                implantacaoRepository,
                ichoRepository,
                moduloRepository,
                grupoUsuarioRepository,
                membroRepository,
                emailService,
                permissaoService,
                "ti@empresa.com,qa@empresa.com"
        );
    }

    private Implantacao emHomologacaoComGoLiveVencido() {
        return new Implantacao(
                42L,
                "ERP",
                LocalDate.of(2026, 5, 20),
                StatusImplantacao.EM_HOMOLOGACAO,
                null
        );
    }

    private Usuario usuario(Long id, String username, String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setNome(nome);
        usuario.setEmail(email);
        return usuario;
    }

    private GrupoUsuarioMembro membro(GrupoUsuario grupo, Usuario usuario, String funcao) {
        GrupoUsuarioMembro membro = new GrupoUsuarioMembro();
        membro.setGrupo(grupo);
        membro.setUsuario(usuario);
        membro.setFuncao(funcao);
        return membro;
    }
}
