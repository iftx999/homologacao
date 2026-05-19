package com.example.homologacao.Controller;

import com.example.homologacao.Service.GrupoUsuarioService;
import com.example.homologacao.dto.AdicionarMembroGrupoUsuarioRequest;
import com.example.homologacao.dto.ConfigurarPermissoesGrupoUsuarioRequest;
import com.example.homologacao.dto.CriarGrupoUsuarioRequest;
import com.example.homologacao.dto.GrupoUsuarioResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grupos-usuarios")
@CrossOrigin
public class GrupoUsuarioController {

    private final GrupoUsuarioService service;

    public GrupoUsuarioController(GrupoUsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GrupoUsuarioResponse> criar(@Valid @RequestBody CriarGrupoUsuarioRequest request) {
        return ResponseEntity.ok(service.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<GrupoUsuarioResponse>> listar(@RequestParam(required = false) Long implantacaoId) {
        return ResponseEntity.ok(service.listar(implantacaoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoUsuarioResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<GrupoUsuarioResponse> adicionarMembro(@PathVariable Long id,
                                                                @Valid @RequestBody AdicionarMembroGrupoUsuarioRequest request) {
        return ResponseEntity.ok(service.adicionarMembro(id, request));
    }

    @PatchMapping("/{id}/membros/{usuarioId}/funcao")
    public ResponseEntity<GrupoUsuarioResponse> atualizarFuncao(@PathVariable Long id,
                                                                @PathVariable Long usuarioId,
                                                                @Valid @RequestBody AtualizarFuncaoRequest request) {
        return ResponseEntity.ok(service.atualizarFuncao(id, usuarioId, request.getFuncao()));
    }

    @DeleteMapping("/{id}/membros/{usuarioId}")
    public ResponseEntity<Void> removerMembro(@PathVariable Long id, @PathVariable Long usuarioId) {
        service.removerMembro(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/permissoes")
    public ResponseEntity<GrupoUsuarioResponse> configurarPermissoes(
            @PathVariable Long id,
            @Valid @RequestBody ConfigurarPermissoesGrupoUsuarioRequest request) {
        return ResponseEntity.ok(service.configurarPermissoes(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerGrupo(@PathVariable Long id) {
        service.removerGrupo(id);
        return ResponseEntity.noContent().build();
    }

    public static class AtualizarFuncaoRequest {

        @NotBlank
        private String funcao;

        public String getFuncao() {
            return funcao;
        }

        public void setFuncao(String funcao) {
            this.funcao = funcao;
        }
    }
}
