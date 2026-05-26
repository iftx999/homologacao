package com.example.homologacao.Controller;


import com.example.homologacao.Service.IchoService;
import com.example.homologacao.Service.IchoEvidenciaService;
import com.example.homologacao.dto.IchoEvidenciaResponse;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoHistorico;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ichos")
@CrossOrigin
public class IchoController {

    private final IchoService service;
    private final IchoEvidenciaService evidenciaService;

    public IchoController(IchoService service,
                          IchoEvidenciaService evidenciaService) {
        this.service = service;
        this.evidenciaService = evidenciaService;
    }

    @PostMapping
    public ResponseEntity<Icho> criar(@RequestBody Icho icho) {
        return ResponseEntity.ok(service.criar(icho));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Icho> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusIcho status,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) Long testadoPorUsuarioId,
            @RequestParam(required = false) String observacao) {

        return ResponseEntity.ok(service.atualizarStatus(id, status, usuario, observacao, testadoPorUsuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Icho> atualizar(
            @PathVariable Long id,
            @RequestBody Icho payload) {

        return ResponseEntity.ok(service.atualizar(id, payload));
    }


    @GetMapping("/pendentes")
    public ResponseEntity<List<Icho>> listarPendentes() {
        return ResponseEntity.ok(service.listarPendentes());
    }

    @GetMapping("/modulo/{moduloId}")
    public List<Icho> buscarPorModulo(@PathVariable Long moduloId) {
        return service.buscarPorModulo(moduloId);
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<IchoHistorico>> listarHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarHistorico(id));
    }

    @PostMapping(value = "/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IchoEvidenciaResponse> adicionarEvidencia(
            @PathVariable Long id,
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam(required = false) String descricao) {

        return ResponseEntity.ok(evidenciaService.adicionar(id, arquivo, descricao));
    }

    @GetMapping("/{id}/evidencias")
    public ResponseEntity<List<IchoEvidenciaResponse>> listarEvidencias(@PathVariable Long id) {
        return ResponseEntity.ok(evidenciaService.listar(id));
    }

    @GetMapping("/evidencias/{evidenciaId}/arquivo")
    public ResponseEntity<Resource> baixarEvidencia(@PathVariable Long evidenciaId) {
        IchoEvidenciaService.ArquivoEvidencia arquivo = evidenciaService.carregarArquivo(evidenciaId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.tipoConteudo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(arquivo.nomeArquivo())
                        .build()
                        .toString())
                .body(arquivo.resource());
    }

    @DeleteMapping("/evidencias/{evidenciaId}")
    public ResponseEntity<Void> removerEvidencia(@PathVariable Long evidenciaId) {
        evidenciaService.remover(evidenciaId);
        return ResponseEntity.noContent().build();
    }

}
