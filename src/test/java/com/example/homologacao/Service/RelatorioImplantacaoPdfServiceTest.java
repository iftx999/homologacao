package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoEvidenciaRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Enum.StatusImplantacao;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoEvidencia;
import com.example.homologacao.model.Implantacao;
import com.example.homologacao.model.Modulo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioImplantacaoPdfServiceTest {

    @Mock
    private ImplantacaoService implantacaoService;

    @Mock
    private ModuloRepository moduloRepository;

    @Mock
    private IchoRepository ichoRepository;

    @Mock
    private IchoEvidenciaRepository evidenciaRepository;

    @InjectMocks
    private RelatorioImplantacaoPdfService service;

    @Test
    void geraPdfDaImplantacaoComDadosPermitidos() {
        Implantacao implantacao = new Implantacao(
                5L,
                "Virada ERP",
                LocalDate.of(2026, 6, 10),
                StatusImplantacao.EM_HOMOLOGACAO,
                "Janela homologada"
        );
        Modulo modulo = new Modulo();
        modulo.setId(8L);
        modulo.setNome("Financeiro");
        modulo.setDescricao("Contas a pagar");

        Icho icho = new Icho(
                13L,
                "Validar remessa",
                "Arquivo bancario",
                modulo,
                StatusIcho.OK,
                LocalDate.of(2026, 5, 20),
                "ana",
                "Fluxo validado"
        );

        when(implantacaoService.buscarPorId(5L)).thenReturn(implantacao);
        when(moduloRepository.findByImplantacaoId(5L)).thenReturn(List.of(modulo));
        when(ichoRepository.findByModuloImplantacaoId(5L)).thenReturn(List.of(icho));
        IchoEvidencia evidencia = new IchoEvidencia();
        evidencia.setId(21L);
        evidencia.setIcho(icho);
        evidencia.setNomeArquivoOriginal("evidencia.pdf");
        evidencia.setTipoConteudo("application/pdf");
        evidencia.setTamanhoBytes(2048L);
        evidencia.setCriadoPor("ana");
        when(evidenciaRepository.findByIchoIdInOrderByCriadoEmDesc(List.of(13L))).thenReturn(List.of(evidencia));

        byte[] pdf = service.gerar(5L);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        verify(implantacaoService).buscarPorId(5L);
        verify(moduloRepository).findByImplantacaoId(5L);
        verify(ichoRepository).findByModuloImplantacaoId(5L);
        verify(evidenciaRepository).findByIchoIdInOrderByCriadoEmDesc(List.of(13L));
    }
}
