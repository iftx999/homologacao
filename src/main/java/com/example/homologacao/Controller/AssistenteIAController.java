package com.example.homologacao.Controller;

import com.example.homologacao.Service.AssistenteIAService;
import com.example.homologacao.dto.AnaliseIAResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ia")
@CrossOrigin
public class AssistenteIAController {

    private final AssistenteIAService assistenteIAService;

    public AssistenteIAController(AssistenteIAService assistenteIAService) {
        this.assistenteIAService = assistenteIAService;
    }

    @PostMapping("/implantacoes/{implantacaoId}/analise")
    public ResponseEntity<AnaliseIAResponse> analisarImplantacao(@PathVariable Long implantacaoId) {
        return ResponseEntity.ok(assistenteIAService.analisarImplantacao(implantacaoId));
    }
}
