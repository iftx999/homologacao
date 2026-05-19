package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.dto.DashboardResumoResponse;
import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.Enum.StatusIcho;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ImplantacaoRepository implantacaoRepository;
    private final ModuloRepository moduloRepository;
    private final IchoRepository ichoRepository;
    private final PermissaoService permissaoService;

    public DashboardService(ImplantacaoRepository implantacaoRepository,
                            ModuloRepository moduloRepository,
                            IchoRepository ichoRepository,
                            PermissaoService permissaoService) {
        this.implantacaoRepository = implantacaoRepository;
        this.moduloRepository = moduloRepository;
        this.ichoRepository = ichoRepository;
        this.permissaoService = permissaoService;
    }

    public DashboardResumoResponse resumo() {
        List<Long> implantacaoIdsPermitidas = permissaoService.buscarImplantacaoIdsPermitidas(
                RecursoSistema.IMPLANTACOES,
                AcaoPermissao.LER
        );

        if (implantacaoIdsPermitidas != null && implantacaoIdsPermitidas.isEmpty()) {
            return montarRespostaVazia();
        }

        boolean admin = implantacaoIdsPermitidas == null;
        Map<StatusIcho, Long> ichosPorStatus = buscarIchosPorStatus(admin, implantacaoIdsPermitidas);
        Map<Model.Enum.StatusImplantacao, Long> implantacoesPorStatus =
                buscarImplantacoesPorStatus(admin, implantacaoIdsPermitidas);

        long totalImplantacoes = admin
                ? implantacaoRepository.count()
                : implantacaoRepository.countByIdIn(implantacaoIdsPermitidas);
        long totalModulos = admin
                ? moduloRepository.count()
                : moduloRepository.countByImplantacaoIdIn(implantacaoIdsPermitidas);
        long totalIchos = admin
                ? ichoRepository.count()
                : ichoRepository.countByModuloImplantacaoIdIn(implantacaoIdsPermitidas);

        long ichosPendentes = admin
                ? ichoRepository.countByStatus(StatusIcho.PENDENTE)
                : ichoRepository.countByModuloImplantacaoIdInAndStatus(implantacaoIdsPermitidas, StatusIcho.PENDENTE);
        long ichosComFalha = admin
                ? ichoRepository.countByStatus(StatusIcho.FALHA)
                : ichoRepository.countByModuloImplantacaoIdInAndStatus(implantacaoIdsPermitidas, StatusIcho.FALHA);

        DashboardResumoResponse.Cards cards = new DashboardResumoResponse.Cards(
                totalImplantacoes,
                totalModulos,
                totalIchos,
                implantacoesPorStatus.getOrDefault(Model.Enum.StatusImplantacao.EM_ANDAMENTO, 0L),
                implantacoesPorStatus.getOrDefault(Model.Enum.StatusImplantacao.FINALIZADA, 0L),
                implantacoesPorStatus.getOrDefault(Model.Enum.StatusImplantacao.CANCELADA, 0L),
                ichosPendentes,
                ichosComFalha
        );

        return new DashboardResumoResponse(
                cards,
                montarMapaIchos(ichosPorStatus),
                montarMapaImplantacoes(implantacoesPorStatus)
        );
    }

    private DashboardResumoResponse montarRespostaVazia() {
        DashboardResumoResponse.Cards cards = new DashboardResumoResponse.Cards(0, 0, 0, 0, 0, 0, 0, 0);
        return new DashboardResumoResponse(
                cards,
                montarMapaIchos(new EnumMap<>(StatusIcho.class)),
                montarMapaImplantacoes(new EnumMap<>(Model.Enum.StatusImplantacao.class))
        );
    }

    private Map<StatusIcho, Long> buscarIchosPorStatus(boolean admin, List<Long> implantacaoIdsPermitidas) {
        List<Object[]> linhas = admin
                ? ichoRepository.countPorStatus()
                : ichoRepository.countPorStatusByImplantacaoIdIn(implantacaoIdsPermitidas);

        Map<StatusIcho, Long> resultado = new EnumMap<>(StatusIcho.class);
        for (Object[] linha : linhas) {
            resultado.put((StatusIcho) linha[0], (Long) linha[1]);
        }
        return resultado;
    }

    private Map<Model.Enum.StatusImplantacao, Long> buscarImplantacoesPorStatus(
            boolean admin,
            List<Long> implantacaoIdsPermitidas) {
        List<Object[]> linhas = admin
                ? implantacaoRepository.countPorStatus()
                : implantacaoRepository.countPorStatusByIdIn(implantacaoIdsPermitidas);

        Map<Model.Enum.StatusImplantacao, Long> resultado =
                new EnumMap<>(Model.Enum.StatusImplantacao.class);
        for (Object[] linha : linhas) {
            resultado.put((Model.Enum.StatusImplantacao) linha[0], (Long) linha[1]);
        }
        return resultado;
    }

    private Map<String, Long> montarMapaIchos(Map<StatusIcho, Long> contagens) {
        Map<String, Long> resposta = new LinkedHashMap<>();
        resposta.put(StatusIcho.OK.name(), contagens.getOrDefault(StatusIcho.OK, 0L));
        resposta.put(StatusIcho.PENDENTE.name(), contagens.getOrDefault(StatusIcho.PENDENTE, 0L));
        resposta.put(StatusIcho.FALHA.name(), contagens.getOrDefault(StatusIcho.FALHA, 0L));
        resposta.put(StatusIcho.NAO_TESTADO.name(), contagens.getOrDefault(StatusIcho.NAO_TESTADO, 0L));
        return resposta;
    }

    private Map<String, Long> montarMapaImplantacoes(Map<Model.Enum.StatusImplantacao, Long> contagens) {
        Map<String, Long> resposta = new LinkedHashMap<>();
        resposta.put(
                Model.Enum.StatusImplantacao.EM_ANDAMENTO.name(),
                contagens.getOrDefault(Model.Enum.StatusImplantacao.EM_ANDAMENTO, 0L)
        );
        resposta.put(
                Model.Enum.StatusImplantacao.FINALIZADA.name(),
                contagens.getOrDefault(Model.Enum.StatusImplantacao.FINALIZADA, 0L)
        );
        resposta.put(
                Model.Enum.StatusImplantacao.CANCELADA.name(),
                contagens.getOrDefault(Model.Enum.StatusImplantacao.CANCELADA, 0L)
        );
        return resposta;
    }
}
