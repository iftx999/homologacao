package com.example.homologacao.Controller;


import com.example.homologacao.Service.ImplantacaoService;
import com.example.homologacao.Service.RelatorioImplantacaoPdfService;
import com.example.homologacao.dto.UsuarioImplantacaoResponse;
import com.example.homologacao.model.Enum.StatusImplantacao;
import com.example.homologacao.model.Implantacao;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/implantacoes")
@CrossOrigin
public class ImplantacaoController {

    private final ImplantacaoService service;
    private final RelatorioImplantacaoPdfService relatorioPdfService;

    public ImplantacaoController(ImplantacaoService service,
                                 RelatorioImplantacaoPdfService relatorioPdfService) {
        this.service = service;
        this.relatorioPdfService = relatorioPdfService;
    }

    @PostMapping
    public ResponseEntity<Implantacao> criar(@RequestBody Implantacao implantacao) {
        return ResponseEntity.ok(service.criar(implantacao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Implantacao> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<StatusImplantacao> status(@PathVariable Long id) {
        return ResponseEntity.ok(service.avaliarStatus(id));
    }

    @GetMapping("/{id}/pendencias")
    public ResponseEntity<Boolean> possuiPendencias(@PathVariable Long id) {
        return ResponseEntity.ok(service.possuiPendenciasAposGoLive(id));
    }

    @GetMapping("/{id}/usuarios")
    public ResponseEntity<List<UsuarioImplantacaoResponse>> listarUsuariosDisponiveisParaTeste(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarUsuariosDisponiveisParaTeste(id));
    }

    @GetMapping(value = "/{id}/relatorio.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarRelatorio(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"implantacao-" + id + "-relatorio.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(relatorioPdfService.gerar(id));
    }

    @GetMapping
    public ResponseEntity<List<Implantacao>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Implantacao> atualizar(@PathVariable Long id,
                                                 @RequestBody Implantacao implantacao) {
        return ResponseEntity.ok(service.atualizar(id, implantacao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
