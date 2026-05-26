package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoEvidenciaRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.dto.IchoEvidenciaResponse;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoEvidencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IchoEvidenciaServiceTest {

    @TempDir
    Path storageDir;

    @Test
    void salvaImagemComoEvidenciaDaIcho() throws Exception {
        IchoEvidenciaRepository evidenciaRepository = mock(IchoEvidenciaRepository.class);
        IchoRepository ichoRepository = mock(IchoRepository.class);
        IchoEvidenciaService service = new IchoEvidenciaService(
                evidenciaRepository,
                ichoRepository,
                storageDir.toString(),
                10_485_760,
                "image/png,image/jpeg,image/webp,application/pdf"
        );

        Icho icho = new Icho();
        icho.setId(10L);
        when(ichoRepository.findById(10L)).thenReturn(Optional.of(icho));
        when(evidenciaRepository.save(org.mockito.ArgumentMatchers.any(IchoEvidencia.class)))
                .thenAnswer(invocation -> {
                    IchoEvidencia evidencia = invocation.getArgument(0);
                    evidencia.setId(99L);
                    return evidencia;
                });

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "tela.png",
                "image/png",
                "conteudo".getBytes()
        );

        IchoEvidenciaResponse response = service.adicionar(10L, arquivo, "Tela validada");

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.ichoId()).isEqualTo(10L);
        assertThat(response.nomeArquivo()).isEqualTo("tela.png");
        assertThat(response.tipoConteudo()).isEqualTo("image/png");
        assertThat(response.descricao()).isEqualTo("Tela validada");
        assertThat(response.urlDownload()).isEqualTo("/api/ichos/evidencias/99/arquivo");

        ArgumentCaptor<IchoEvidencia> captor = ArgumentCaptor.forClass(IchoEvidencia.class);
        verify(evidenciaRepository).save(captor.capture());
        Path arquivoSalvo = storageDir.resolve(captor.getValue().getNomeArquivoArmazenado());
        assertThat(Files.exists(arquivoSalvo)).isTrue();
        assertThat(Files.readString(arquivoSalvo)).isEqualTo("conteudo");
    }

    @Test
    void rejeitaArquivoQueNaoEImagem() {
        IchoEvidenciaRepository evidenciaRepository = mock(IchoEvidenciaRepository.class);
        IchoRepository ichoRepository = mock(IchoRepository.class);
        IchoEvidenciaService service = new IchoEvidenciaService(
                evidenciaRepository,
                ichoRepository,
                storageDir.toString(),
                10_485_760,
                "image/png,image/jpeg,image/webp,application/pdf"
        );

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "relatorio.txt",
                "text/plain",
                "texto".getBytes()
        );

        assertThatThrownBy(() -> service.adicionar(10L, arquivo, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tipo de arquivo de evidência não permitido");
    }

    @Test
    void rejeitaArquivoAcimaDoLimiteConfigurado() {
        IchoEvidenciaRepository evidenciaRepository = mock(IchoEvidenciaRepository.class);
        IchoRepository ichoRepository = mock(IchoRepository.class);
        IchoEvidenciaService service = new IchoEvidenciaService(
                evidenciaRepository,
                ichoRepository,
                storageDir.toString(),
                3,
                "image/png"
        );

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "tela.png",
                "image/png",
                "conteudo".getBytes()
        );

        assertThatThrownBy(() -> service.adicionar(10L, arquivo, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("excede o tamanho máximo permitido");
    }
}
