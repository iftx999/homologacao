package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.dto.DashboardResumoResponse;
import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Enum.StatusImplantacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ImplantacaoRepository implantacaoRepository;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private IchoRepository ichoRepository;

    @Mock
    private PermissaoService permissaoService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void adminVeTodosOsDados() {
        when(permissaoService.buscarImplantacaoIdsPermitidas(RecursoSistema.IMPLANTACOES, AcaoPermissao.LER))
                .thenReturn(null);
        when(implantacaoRepository.count()).thenReturn(10L);
        when(moduloRepository.count()).thenReturn(35L);
        when(ichoRepository.count()).thenReturn(120L);
        when(ichoRepository.countByStatus(StatusIcho.PENDENTE)).thenReturn(12L);
        when(ichoRepository.countByStatus(StatusIcho.FALHA)).thenReturn(5L);
        when(ichoRepository.countPorStatus()).thenReturn(List.<Object[]>of(
                new Object[]{StatusIcho.OK, 80L},
                new Object[]{StatusIcho.PENDENTE, 12L},
                new Object[]{StatusIcho.FALHA, 5L},
                new Object[]{StatusIcho.NAO_TESTADO, 23L}
        ));
        when(implantacaoRepository.countPorStatus()).thenReturn(List.<Object[]>of(
                new Object[]{StatusImplantacao.EM_ANDAMENTO, 6L},
                new Object[]{StatusImplantacao.FINALIZADA, 3L},
                new Object[]{StatusImplantacao.CANCELADA, 1L}
        ));

        DashboardResumoResponse response = dashboardService.resumo();

        assertThat(response.getCards().getTotalImplantacoes()).isEqualTo(10L);
        assertThat(response.getCards().getTotalModulos()).isEqualTo(35L);
        assertThat(response.getCards().getTotalIchos()).isEqualTo(120L);
        assertThat(response.getCards().getImplantacoesEmAndamento()).isEqualTo(6L);
        assertThat(response.getCards().getImplantacoesFinalizadas()).isEqualTo(3L);
        assertThat(response.getCards().getImplantacoesCanceladas()).isEqualTo(1L);
        assertThat(response.getCards().getIchosPendentes()).isEqualTo(12L);
        assertThat(response.getCards().getIchosComFalha()).isEqualTo(5L);
        assertThat(response.getIchosPorStatus()).containsEntry("OK", 80L);
        assertThat(response.getImplantacoesPorStatus()).containsEntry("FINALIZADA", 3L);
    }

    @Test
    void usuarioComumVeApenasImplantacoesPermitidas() {
        List<Long> idsPermitidos = List.of(1L, 2L);
        when(permissaoService.buscarImplantacaoIdsPermitidas(RecursoSistema.IMPLANTACOES, AcaoPermissao.LER))
                .thenReturn(idsPermitidos);
        when(implantacaoRepository.countByIdIn(idsPermitidos)).thenReturn(2L);
        when(moduloRepository.countByImplantacaoIdIn(idsPermitidos)).thenReturn(4L);
        when(ichoRepository.countByModuloImplantacaoIdIn(idsPermitidos)).thenReturn(9L);
        when(ichoRepository.countByModuloImplantacaoIdInAndStatus(idsPermitidos, StatusIcho.PENDENTE)).thenReturn(2L);
        when(ichoRepository.countByModuloImplantacaoIdInAndStatus(idsPermitidos, StatusIcho.FALHA)).thenReturn(1L);
        when(ichoRepository.countPorStatusByImplantacaoIdIn(idsPermitidos)).thenReturn(List.<Object[]>of(
                new Object[]{StatusIcho.OK, 6L},
                new Object[]{StatusIcho.PENDENTE, 2L},
                new Object[]{StatusIcho.FALHA, 1L}
        ));
        when(implantacaoRepository.countPorStatusByIdIn(idsPermitidos)).thenReturn(List.<Object[]>of(
                new Object[]{StatusImplantacao.EM_ANDAMENTO, 2L}
        ));

        DashboardResumoResponse response = dashboardService.resumo();

        assertThat(response.getCards().getTotalImplantacoes()).isEqualTo(2L);
        assertThat(response.getCards().getTotalModulos()).isEqualTo(4L);
        assertThat(response.getCards().getTotalIchos()).isEqualTo(9L);
        assertThat(response.getCards().getImplantacoesEmAndamento()).isEqualTo(2L);
        assertThat(response.getIchosPorStatus()).containsEntry("OK", 6L);
        assertThat(response.getIchosPorStatus()).containsEntry("NAO_TESTADO", 0L);

        verify(implantacaoRepository).countByIdIn(idsPermitidos);
        verify(moduloRepository).countByImplantacaoIdIn(idsPermitidos);
        verify(ichoRepository).countByModuloImplantacaoIdIn(idsPermitidos);
    }

    @Test
    void statusSemDadosRetornamZero() {
        when(permissaoService.buscarImplantacaoIdsPermitidas(RecursoSistema.IMPLANTACOES, AcaoPermissao.LER))
                .thenReturn(null);
        when(implantacaoRepository.count()).thenReturn(1L);
        when(moduloRepository.count()).thenReturn(1L);
        when(ichoRepository.count()).thenReturn(1L);
        when(ichoRepository.countByStatus(StatusIcho.PENDENTE)).thenReturn(0L);
        when(ichoRepository.countByStatus(StatusIcho.FALHA)).thenReturn(0L);
        when(ichoRepository.countPorStatus()).thenReturn(List.<Object[]>of(
                new Object[]{StatusIcho.OK, 1L}
        ));
        when(implantacaoRepository.countPorStatus()).thenReturn(List.<Object[]>of(
                new Object[]{StatusImplantacao.EM_ANDAMENTO, 1L}
        ));

        DashboardResumoResponse response = dashboardService.resumo();

        assertThat(response.getIchosPorStatus()).containsEntry("PENDENTE", 0L);
        assertThat(response.getIchosPorStatus()).containsEntry("FALHA", 0L);
        assertThat(response.getIchosPorStatus()).containsEntry("NAO_TESTADO", 0L);
        assertThat(response.getImplantacoesPorStatus()).containsEntry("FINALIZADA", 0L);
        assertThat(response.getImplantacoesPorStatus()).containsEntry("CANCELADA", 0L);
    }

    @Test
    void usuarioSemPermissaoRecebeContadoresZerados() {
        when(permissaoService.buscarImplantacaoIdsPermitidas(RecursoSistema.IMPLANTACOES, AcaoPermissao.LER))
                .thenReturn(List.of());

        DashboardResumoResponse response = dashboardService.resumo();

        assertThat(response.getCards().getTotalImplantacoes()).isZero();
        assertThat(response.getCards().getTotalModulos()).isZero();
        assertThat(response.getCards().getTotalIchos()).isZero();
        assertThat(response.getIchosPorStatus()).containsEntry("OK", 0L);
        assertThat(response.getImplantacoesPorStatus()).containsEntry("EM_ANDAMENTO", 0L);
        verifyNoInteractions(implantacaoRepository, moduloRepository, ichoRepository);
    }
}
