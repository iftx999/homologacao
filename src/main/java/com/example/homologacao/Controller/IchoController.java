package com.example.homologacao.Controller;


import com.example.homologacao.Service.IchoService;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
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
            @RequestParam String usuario) {

        return ResponseEntity.ok(service.atualizarStatus(id, status, usuario));
    }


    @GetMapping("/pendentes")
    public ResponseEntity<List<Icho>> listarPendentes() {
        return ResponseEntity.ok(service.listarPendentes());
    }

    @GetMapping("/modulo/{moduloId}")
    public List<Icho> buscarPorModulo(@PathVariable Long moduloId) {
        return service.buscarPorModulo(moduloId);
    }

}


