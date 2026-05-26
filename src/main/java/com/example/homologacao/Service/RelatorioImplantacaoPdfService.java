package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoEvidenciaRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoEvidencia;
import com.example.homologacao.model.Implantacao;
import com.example.homologacao.model.Modulo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RelatorioImplantacaoPdfService {

    private static final float MARGEM = 48;
    private static final float TAMANHO_TEXTO = 9;
    private static final float ALTURA_LINHA = 13;
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ImplantacaoService implantacaoService;
    private final ModuloRepository moduloRepository;
    private final IchoRepository ichoRepository;
    private final IchoEvidenciaRepository evidenciaRepository;

    public RelatorioImplantacaoPdfService(ImplantacaoService implantacaoService,
                                          ModuloRepository moduloRepository,
                                          IchoRepository ichoRepository,
                                          IchoEvidenciaRepository evidenciaRepository) {
        this.implantacaoService = implantacaoService;
        this.moduloRepository = moduloRepository;
        this.ichoRepository = ichoRepository;
        this.evidenciaRepository = evidenciaRepository;
    }

    public byte[] gerar(Long implantacaoId) {
        Implantacao implantacao = implantacaoService.buscarPorId(implantacaoId);
        List<Modulo> modulos = moduloRepository.findByImplantacaoId(implantacaoId);
        List<Icho> ichos = ichoRepository.findByModuloImplantacaoId(implantacaoId);
        Map<Long, List<IchoEvidencia>> evidenciasPorIcho = buscarEvidenciasPorIcho(ichos);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            EscritorPdf escritor = new EscritorPdf(document);
            escreverCabecalho(escritor, implantacao, modulos, ichos, evidenciasPorIcho);
            escreverResumo(escritor, ichos, evidenciasPorIcho);
            escreverDetalhes(escritor, modulos, ichos, evidenciasPorIcho);
            escritor.fechar();
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel gerar o relatorio PDF", e);
        }
    }

    private void escreverCabecalho(EscritorPdf escritor,
                                   Implantacao implantacao,
                                   List<Modulo> modulos,
                                   List<Icho> ichos,
                                   Map<Long, List<IchoEvidencia>> evidenciasPorIcho) throws IOException {
        escritor.titulo("Relatorio de Homologacao");
        escritor.texto("Implantacao: " + texto(implantacao.getNome()));
        escritor.texto("Status: " + texto(implantacao.getStatus()));
        escritor.texto("Go live: " + data(implantacao.getDataGoLive()));
        escritor.texto("Modulos: " + modulos.size()
                + " | ICHOs: " + ichos.size()
                + " | Evidencias: " + totalEvidencias(evidenciasPorIcho));
        escritor.texto("Observacao: " + texto(implantacao.getObservacao()));
        escritor.espaco();
    }

    private void escreverResumo(EscritorPdf escritor,
                                List<Icho> ichos,
                                Map<Long, List<IchoEvidencia>> evidenciasPorIcho) throws IOException {
        Map<StatusIcho, Long> contagens = new EnumMap<>(StatusIcho.class);
        for (Icho icho : ichos) {
            contagens.merge(icho.getStatus(), 1L, Long::sum);
        }

        escritor.subtitulo("Resumo por status");
        for (StatusIcho status : StatusIcho.values()) {
            escritor.texto(status.name() + ": " + contagens.getOrDefault(status, 0L));
        }
        escritor.texto("ICHOs com evidencia: " + evidenciasPorIcho.keySet().size());
        escritor.espaco();
    }

    private void escreverDetalhes(EscritorPdf escritor,
                                  List<Modulo> modulos,
                                  List<Icho> ichos,
                                  Map<Long, List<IchoEvidencia>> evidenciasPorIcho) throws IOException {
        escritor.subtitulo("Detalhamento");
        if (modulos.isEmpty()) {
            escritor.texto("Nenhum modulo cadastrado.");
            return;
        }

        for (Modulo modulo : modulos) {
            escritor.linhaModulo("Modulo " + texto(modulo.getNome()) + ": " + texto(modulo.getDescricao()));
            List<Icho> ichosDoModulo = ichos.stream()
                    .filter(icho -> icho.getModulo() != null
                            && Objects.equals(modulo.getId(), icho.getModulo().getId()))
                    .toList();

            if (ichosDoModulo.isEmpty()) {
                escritor.texto("  Nenhum ICHO cadastrado.");
                continue;
            }

            for (Icho icho : ichosDoModulo) {
                escritor.texto("  [" + texto(icho.getStatus()) + "] " + texto(icho.getTitulo()));
                escritor.texto("    Testado por: " + texto(icho.getTestadoPor())
                        + " | Data: " + data(icho.getDataTeste()));
                if (icho.getObservacao() != null && !icho.getObservacao().isBlank()) {
                    escritor.texto("    Observacao: " + texto(icho.getObservacao()));
                }
                escreverEvidencias(escritor, evidenciasPorIcho.getOrDefault(icho.getId(), List.of()));
            }
        }
    }

    private void escreverEvidencias(EscritorPdf escritor, List<IchoEvidencia> evidencias) throws IOException {
        if (evidencias.isEmpty()) {
            escritor.texto("    Evidencias: nenhuma");
            return;
        }

        escritor.texto("    Evidencias:");
        for (IchoEvidencia evidencia : evidencias) {
            escritor.texto("      - " + texto(evidencia.getNomeArquivoOriginal())
                    + " (" + texto(evidencia.getTipoConteudo())
                    + ", " + formatarBytes(evidencia.getTamanhoBytes()) + ")"
                    + " por " + texto(evidencia.getCriadoPor()));
            if (evidencia.getDescricao() != null && !evidencia.getDescricao().isBlank()) {
                escritor.texto("        Descricao: " + texto(evidencia.getDescricao()));
            }
        }
    }

    private Map<Long, List<IchoEvidencia>> buscarEvidenciasPorIcho(List<Icho> ichos) {
        List<Long> ids = ichos.stream()
                .map(Icho::getId)
                .filter(Objects::nonNull)
                .toList();

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return evidenciaRepository.findByIchoIdInOrderByCriadoEmDesc(ids).stream()
                .filter(evidencia -> evidencia.getIcho() != null && evidencia.getIcho().getId() != null)
                .collect(Collectors.groupingBy(evidencia -> evidencia.getIcho().getId()));
    }

    private long totalEvidencias(Map<Long, List<IchoEvidencia>> evidenciasPorIcho) {
        return evidenciasPorIcho.values().stream()
                .mapToLong(List::size)
                .sum();
    }

    private String formatarBytes(Long bytes) {
        if (bytes == null) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0);
        }
        return String.format(java.util.Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0);
    }

    private String texto(Object valor) {
        return valor == null || valor.toString().isBlank() ? "-" : valor.toString();
    }

    private String data(LocalDate data) {
        return data == null ? "-" : DATA_BR.format(data);
    }

    private static class EscritorPdf {

        private final PDDocument document;
        private final PDType1Font fonte = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font fonteNegrito = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;

        EscritorPdf(PDDocument document) throws IOException {
            this.document = document;
            novaPagina();
        }

        void titulo(String texto) throws IOException {
            linha(texto, fonteNegrito, 16);
            espaco();
        }

        void subtitulo(String texto) throws IOException {
            linha(texto, fonteNegrito, 11);
        }

        void linhaModulo(String texto) throws IOException {
            espaco();
            linha(texto, fonteNegrito, 10);
        }

        void texto(String texto) throws IOException {
            for (String linha : quebrar(texto, 104)) {
                linha(linha, fonte, TAMANHO_TEXTO);
            }
        }

        void espaco() throws IOException {
            y -= ALTURA_LINHA / 2;
            garantirEspaco();
        }

        void fechar() throws IOException {
            stream.close();
        }

        private void linha(String texto, PDType1Font fonteLinha, float tamanho) throws IOException {
            garantirEspaco();
            stream.beginText();
            stream.setFont(fonteLinha, tamanho);
            stream.newLineAtOffset(MARGEM, y);
            stream.showText(normalizar(texto));
            stream.endText();
            y -= ALTURA_LINHA;
        }

        private void garantirEspaco() throws IOException {
            if (y > MARGEM) {
                return;
            }
            stream.close();
            novaPagina();
        }

        private void novaPagina() throws IOException {
            PDPage pagina = new PDPage(PDRectangle.A4);
            document.addPage(pagina);
            stream = new PDPageContentStream(document, pagina);
            y = pagina.getMediaBox().getHeight() - MARGEM;
        }

        private List<String> quebrar(String texto, int limite) {
            if (texto.length() <= limite) {
                return List.of(texto);
            }

            String[] palavras = texto.split("\\s+");
            java.util.ArrayList<String> linhas = new java.util.ArrayList<>();
            StringBuilder linha = new StringBuilder();
            for (String palavra : palavras) {
                if (linha.length() > 0 && linha.length() + palavra.length() + 1 > limite) {
                    linhas.add(linha.toString());
                    linha.setLength(0);
                }
                if (linha.length() > 0) {
                    linha.append(' ');
                }
                linha.append(palavra);
            }
            if (linha.length() > 0) {
                linhas.add(linha.toString());
            }
            return linhas;
        }

        private String normalizar(String texto) {
            return texto.replace("\n", " ")
                    .replace("\r", " ")
                    .replaceAll("[^\\x20-\\x7E]", "?");
        }
    }
}
