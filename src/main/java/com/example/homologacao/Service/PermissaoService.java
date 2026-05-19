package com.example.homologacao.Service;

import com.example.homologacao.Repository.GrupoUsuarioPermissaoRepository;
import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PermissaoService {

    private final GrupoUsuarioPermissaoRepository permissaoRepository;
    private final UserRepository userRepository;

    public PermissaoService(GrupoUsuarioPermissaoRepository permissaoRepository,
                            UserRepository userRepository) {
        this.permissaoRepository = permissaoRepository;
        this.userRepository = userRepository;
    }

    public void exigirPermissaoGeral(RecursoSistema recurso, AcaoPermissao acao) {
        String username = usernameAtual();

        if (usuarioAtualAdmin(username)) {
            return;
        }

        if (!permissaoRepository.existsPermissaoGeral(username, recurso, acoesAceitas(acao))) {
            negarAcesso(recurso, acao);
        }
    }

    public void exigirPermissaoNaImplantacao(Long implantacaoId, RecursoSistema recurso, AcaoPermissao acao) {
        String username = usernameAtual();

        if (usuarioAtualAdmin(username)) {
            return;
        }

        boolean permitido = permissaoRepository.existsPermissaoNaImplantacao(
                implantacaoId,
                username,
                recurso,
                acoesAceitas(acao)
        );

        if (!permitido) {
            negarAcesso(recurso, acao);
        }
    }

    public List<Long> buscarImplantacaoIdsPermitidas(RecursoSistema recurso, AcaoPermissao acao) {
        String username = usernameAtual();

        if (usuarioAtualAdmin(username)) {
            return null;
        }

        return permissaoRepository.findImplantacaoIdsPermitidas(username, recurso, acoesAceitas(acao));
    }

    private List<AcaoPermissao> acoesAceitas(AcaoPermissao acao) {
        return acao == AcaoPermissao.FULL
                ? List.of(AcaoPermissao.FULL)
                : List.of(acao, AcaoPermissao.FULL);
    }

    private boolean usuarioAtualAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(Usuario::getRole)
                .map(role -> role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ROLE_ADMIN"))
                .orElse(false);
    }

    private String usernameAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return authentication.getName();
    }

    private void negarAcesso(RecursoSistema recurso, AcaoPermissao acao) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuário não possui permissão " + acao + " para " + recurso
        );
    }
}
