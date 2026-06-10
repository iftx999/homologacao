package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoEvidenciaRepository;
import com.example.homologacao.Repository.IchoHistoricoRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.Repository.ModuloRepository;
import com.example.homologacao.dto.AnaliseIAResponse;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoEvidencia;
import com.example.homologacao.model.IchoHistorico;
import com.example.homologacao.model.Implantacao;
import com.example.homologacao.model.Modulo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssistenteIAService {

    private static final String SYSTEM_PROMPT = """
            Voce e um assistente especializado em gestao de implantacoes de sistemas, homologacao, go-live e controle de ICHOs.
            Analise apenas os dados fornecidos no contexto.
            Nao invente informacoes.
            Se alguma informacao estiver ausente, informe a limitacao.
            Responda sempre em portugues do Brasil.
            Seja objetivo, pratico e orientado a tomada de decisao.
            Classifique riscos e pendencias por severidade.
            Priorize itens que possam impactar go-live, homologacao, falhas, pendencias sem evidencia e modulos criticos.
            Retorne somente JSON valido no formato solicitado, sem markdown e sem texto fora do JSON.
            """;

    private final ImplantacaoRepository implantacaoRepository;
    private final ModuloRepository moduloRepository;
    private final IchoRepository ichoRepository;
    private final IchoHistoricoRepository historicoRepository;
    private final IchoEvidenciaRepository evidenciaRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public AssistenteIAService(ImplantacaoRepository implantacaoRepository,
                               ModuloRepository moduloRepository,
                               IchoRepository ichoRepository,
                               IchoHistoricoRepository historicoRepository,
                               IchoEvidenciaRepository evidenciaRepository,
                               ObjectMapper objectMapper,
                               @Value("${app.ia.api-key:}") String apiKey,
                               @Value("${app.ia.api-url:https://api.openai.com/v1/chat/completions}") String apiUrl,
                               @Value("${app.ia.model:gpt-4o-mini}") String model) {
        this.implantacaoRepository = implantacaoRepository;
        this.moduloRepository = moduloRepository;
        this.ichoRepository = ichoRepository;
        this.historicoRepository = historicoRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    @Transactional(readOnly = true)
    public AnaliseIAResponse analisarImplantacao(Long implantacaoId) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chave da IA nao configurada no backend"
            );
        }

        Map<String, Object> contexto = montarContexto(implantacaoId);
        String promptUsuario = montarPromptUsuario(contexto);
        String resposta = chamarProvedor(promptUsuario);

        return normalizarResposta(parseAnalise(resposta));
    }

    private Map<String, Object> montarContexto(Long implantacaoId) {
        Implantacao implantacao = implantacaoRepository.findById(implantacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Implantacao nao encontrada"));

        List<Modulo> modulos = moduloRepository.findByImplantacaoId(implantacaoId);
        List<Icho> ichos = ichoRepository.findByModuloImplantacaoId(implantacaoId);
        List<Long> ichoIds = ichos.stream()
                .map(Icho::getId)
                .toList();

        Map<Long, List<IchoHistorico>> historicosPorIcho = ichoIds.isEmpty()
                ? Map.of()
                : historicoRepository.findByIchoIdInOrderByDataAlteracaoAsc(ichoIds).stream()
                        .filter(historico -> historico.getIcho() != null && historico.getIcho().getId() != null)
                        .collect(Collectors.groupingBy(historico -> historico.getIcho().getId()));

        Map<Long, List<IchoEvidencia>> evidenciasPorIcho = ichoIds.isEmpty()
                ? Map.of()
                : evidenciaRepository.findByIchoIdInOrderByCriadoEmDesc(ichoIds).stream()
                        .filter(evidencia -> evidencia.getIcho() != null && evidencia.getIcho().getId() != null)
                        .collect(Collectors.groupingBy(evidencia -> evidencia.getIcho().getId()));

        Map<String, Long> ichosPorStatus = ichos.stream()
                .collect(Collectors.groupingBy(icho -> icho.getStatus().name(), Collectors.counting()));

        Map<Long, List<Icho>> ichosPorModulo = ichos.stream()
                .filter(icho -> icho.getModulo() != null && icho.getModulo().getId() != null)
                .collect(Collectors.groupingBy(icho -> icho.getModulo().getId()));

        List<Map<String, Object>> modulosContexto = modulos.stream()
                .map(modulo -> montarModuloContexto(
                        modulo,
                        ichosPorModulo.getOrDefault(modulo.getId(), List.of()),
                        historicosPorIcho,
                        evidenciasPorIcho
                ))
                .toList();

        Map<String, Object> implantacaoContexto = new LinkedHashMap<>();
        implantacaoContexto.put("id", implantacao.getId());
        implantacaoContexto.put("nome", valorOuNulo(implantacao.getNome()));
        implantacaoContexto.put("status", implantacao.getStatus() == null ? null : implantacao.getStatus().name());
        implantacaoContexto.put("dataGoLive", implantacao.getDataGoLive());
        implantacaoContexto.put("observacao", valorOuNulo(implantacao.getObservacao()));

        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("implantacao", implantacaoContexto);
        contexto.put("quantidadeModulos", modulos.size());
        contexto.put("quantidadeIchos", ichos.size());
        contexto.put("ichosPorStatus", ichosPorStatus);
        contexto.put("modulos", modulosContexto);
        return contexto;
    }

    private Map<String, Object> montarModuloContexto(Modulo modulo,
                                                     List<Icho> ichos,
                                                     Map<Long, List<IchoHistorico>> historicosPorIcho,
                                                     Map<Long, List<IchoEvidencia>> evidenciasPorIcho) {
        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("id", modulo.getId());
        contexto.put("nome", valorOuNulo(modulo.getNome()));
        contexto.put("descricao", valorOuNulo(modulo.getDescricao()));
        contexto.put("ichos", ichos.stream()
                .map(icho -> montarIchoContexto(
                        icho,
                        historicosPorIcho.getOrDefault(icho.getId(), List.of()),
                        evidenciasPorIcho.getOrDefault(icho.getId(), List.of())
                ))
                .toList());
        return contexto;
    }

    private Map<String, Object> montarIchoContexto(Icho icho,
                                                   List<IchoHistorico> historicos,
                                                   List<IchoEvidencia> evidencias) {
        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("id", icho.getId());
        contexto.put("titulo", valorOuNulo(icho.getTitulo()));
        contexto.put("descricao", valorOuNulo(icho.getDescricao()));
        contexto.put("status", icho.getStatus() == null ? null : icho.getStatus().name());
        contexto.put("dataTeste", icho.getDataTeste());
        contexto.put("testadoPor", valorOuNulo(icho.getTestadoPor()));
        contexto.put("testadoPorNome", valorOuNulo(icho.getTestadoPorNome()));
        contexto.put("observacao", valorOuNulo(icho.getObservacao()));
        contexto.put("historicoStatus", historicos.stream()
                .map(this::montarHistoricoContexto)
                .toList());
        contexto.put("evidencias", evidencias.stream()
                .map(this::montarEvidenciaContexto)
                .toList());
        return contexto;
    }

    private Map<String, Object> montarHistoricoContexto(IchoHistorico historico) {
        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("statusAnterior", historico.getStatusAnterior() == null ? null : historico.getStatusAnterior().name());
        contexto.put("novoStatus", historico.getNovoStatus() == null ? null : historico.getNovoStatus().name());
        contexto.put("observacao", valorOuNulo(historico.getObservacao()));
        contexto.put("usuarioNome", valorOuNulo(historico.getUsuarioNome()));
        contexto.put("dataAlteracao", historico.getDataAlteracao());
        return contexto;
    }

    private Map<String, Object> montarEvidenciaContexto(IchoEvidencia evidencia) {
        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("id", evidencia.getId());
        contexto.put("nomeArquivoOriginal", valorOuNulo(evidencia.getNomeArquivoOriginal()));
        contexto.put("tipoConteudo", valorOuNulo(evidencia.getTipoConteudo()));
        contexto.put("tamanhoBytes", evidencia.getTamanhoBytes());
        contexto.put("descricao", valorOuNulo(evidencia.getDescricao()));
        contexto.put("criadoPor", valorOuNulo(evidencia.getCriadoPor()));
        contexto.put("criadoEm", evidencia.getCriadoEm());
        return contexto;
    }

    private String montarPromptUsuario(Map<String, Object> contexto) {
        try {
            return """
                    Analise a implantacao abaixo e retorne o diagnostico no formato JSON definido.

                    Dados da implantacao:
                    %s

                    Considere:
                    - Status geral da implantacao.
                    - Data de go-live.
                    - Quantidade de modulos.
                    - ICHOs por status.
                    - ICHOs com falha.
                    - ICHOs pendentes.
                    - ICHOs nao testados.
                    - Observacoes.
                    - Evidencias.
                    - Historico de mudancas de status, quando existir.
                    - Possiveis riscos para o go-live.
                    - Proximas acoes recomendadas.
                    """.formatted(objectMapper.writeValueAsString(contexto));
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao montar contexto da IA", e);
        }
    }

    private String chamarProvedor(String promptUsuario) {
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", promptUsuario)
                )
        );

        try {
            return restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao chamar provedor de IA", e);
        }
    }

    private AnaliseIAResponse parseAnalise(String respostaProvedor) {
        try {
            JsonNode root = objectMapper.readTree(respostaProvedor);
            String conteudo = root.path("choices").path(0).path("message").path("content").asText(null);
            if (!StringUtils.hasText(conteudo)) {
                conteudo = respostaProvedor;
            }
            return objectMapper.readValue(conteudo, AnaliseIAResponse.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Resposta invalida do provedor de IA", e);
        }
    }

    private AnaliseIAResponse normalizarResposta(AnaliseIAResponse resposta) {
        return new AnaliseIAResponse(
                StringUtils.hasText(resposta.resumoExecutivo()) ? resposta.resumoExecutivo() : "Nao ha informacao suficiente para gerar um resumo.",
                resposta.nivelRisco() == null ? AnaliseIAResponse.NivelRiscoIA.MEDIO : resposta.nivelRisco(),
                listaOuVazia(resposta.riscos()),
                listaOuVazia(resposta.pendencias()),
                listaOuVazia(resposta.planoAcao()),
                listaOuVazia(resposta.perguntasSugeridas())
        );
    }

    private <T> List<T> listaOuVazia(List<T> lista) {
        return lista == null ? new ArrayList<>() : lista;
    }

    private String valorOuNulo(String valor) {
        return StringUtils.hasText(valor) ? valor : null;
    }
}
