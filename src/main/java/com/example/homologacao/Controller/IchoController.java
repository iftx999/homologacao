package com.example.homologacao.Controller;


import com.example.homologacao.Service.IchoService;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoHistorico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ichos")
@CrossOrigin
public class IchoController {

    private final IchoService service;

    public IchoController(IchoService service) {
        this.service = service;
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
            @RequestParam(required = false) String observacao) {

        return ResponseEntity.ok(service.atualizarStatus(id, status, usuario, observacao));
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

}
