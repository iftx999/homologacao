package com.example.homologacao.Controller;


import com.example.homologacao.Service.ImplantacaoService;
import com.example.homologacao.model.Implantacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/implantacoes")
@CrossOrigin
public class ImplantacaoController {

    private final ImplantacaoService service;

    public ImplantacaoController(ImplantacaoService service) {
        this.service = service;
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
    public ResponseEntity<Model.Enum.StatusImplantacao> status(@PathVariable Long id) {
        return ResponseEntity.ok(service.avaliarStatus(id));
    }

    @GetMapping("/{id}/pendencias")
    public ResponseEntity<Boolean> possuiPendencias(@PathVariable Long id) {
        return ResponseEntity.ok(service.possuiPendenciasAposGoLive(id));
    }

    @GetMapping
    public ResponseEntity<List<Implantacao>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}