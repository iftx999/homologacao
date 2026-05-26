package com.example.homologacao.Scheduler;

import com.example.homologacao.Service.ImplantacaoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ImplantacaoScheduler {

    private final ImplantacaoService implantacaoService;

    public ImplantacaoScheduler(ImplantacaoService implantacaoService) {
        this.implantacaoService = implantacaoService;
    }

    // Executa todo dia as 08:00 por padrao.
    @Scheduled(cron = "${app.implantacao.validacao.cron:0 0 8 * * ?}",
            zone = "${app.implantacao.validacao.zone:America/Sao_Paulo}")
    public void validarImplantacoes() {
        implantacaoService.validarImplantacoes();
    }
}
