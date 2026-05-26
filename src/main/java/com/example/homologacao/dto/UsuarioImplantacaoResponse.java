package com.example.homologacao.dto;

import com.example.homologacao.model.GrupoUsuarioMembro;
import com.example.homologacao.model.Usuario;

public record UsuarioImplantacaoResponse(
        Long usuarioId,
        String username,
        String nome,
        String email,
        String funcao
) {

    public static UsuarioImplantacaoResponse fromMembro(GrupoUsuarioMembro membro) {
        Usuario usuario = membro.getUsuario();
        return new UsuarioImplantacaoResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getEmail(),
                membro.getFuncao()
        );
    }
}
