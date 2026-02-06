package Controller;

import Model.Enum.StatusImplantacao;
import Model.Implantacao;
import Service.ImplantacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<StatusImplantacao> status(@PathVariable Long id) {
        return ResponseEntity.ok(service.avaliarStatus(id));
    }

    @GetMapping("/{id}/pendencias")
    public ResponseEntity<Boolean> possuiPendencias(@PathVariable Long id) {
        return ResponseEntity.ok(service.possuiPendenciasAposGoLive(id));
    }
}