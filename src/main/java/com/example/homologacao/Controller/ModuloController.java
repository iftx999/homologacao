package com.example.homologacao.Controller;

import com.example.homologacao.Service.ModuloService;
import com.example.homologacao.model.Modulo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modulos")
@CrossOrigin
public class ModuloController {

    private final ModuloService service;

    public ModuloController(ModuloService service) {
        this.service = service;
    }

    // ➕ criar módulo
    @PostMapping
    public ResponseEntity<Modulo> criar(@RequestBody Modulo modulo) {
        return ResponseEntity.ok(service.salvar(modulo));
    }

    // 📋 listar todos
    @GetMapping
    public ResponseEntity<List<Modulo>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    // 🔎 buscar por id  ⭐⭐⭐ ESTE FALTAVA
    @GetMapping("/{id}")
    public ResponseEntity<Modulo> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
