package Scheduler;

import Service.ImplantacaoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ImplantacaoScheduler {

    private final ImplantacaoService implantacaoService;

    public ImplantacaoScheduler(ImplantacaoService implantacaoService) {
        this.implantacaoService = implantacaoService;
    }

    // Executa todo dia às 08:00
    @Scheduled(cron = "0 0 8 * * ?")
    public void validarImplantacoes() {
        implantacaoService.validarImplantacoes();
    }
}
