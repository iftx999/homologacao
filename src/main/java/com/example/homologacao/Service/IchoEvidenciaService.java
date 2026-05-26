package com.example.homologacao.Service;

import com.example.homologacao.Repository.IchoEvidenciaRepository;
import com.example.homologacao.Repository.IchoRepository;
import com.example.homologacao.dto.IchoEvidenciaResponse;
import com.example.homologacao.model.Icho;
import com.example.homologacao.model.IchoEvidencia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class IchoEvidenciaService {

    private final IchoEvidenciaRepository evidenciaRepository;
    private final IchoRepository ichoRepository;
    private final Path storageRoot;
    private final long tamanhoMaximoBytes;
    private final Set<String> tiposConteudoPermitidos;

    public IchoEvidenciaService(IchoEvidenciaRepository evidenciaRepository,
                                IchoRepository ichoRepository,
                                @Value("${app.icho.evidencias.storage-dir:uploads/icho-evidencias}") String storageDir,
                                @Value("${app.icho.evidencias.max-size-bytes:10485760}") long tamanhoMaximoBytes,
                                @Value("${app.icho.evidencias.allowed-content-types:image/png,image/jpeg,image/webp,application/pdf}") String tiposConteudoPermitidos) {
        this.evidenciaRepository = evidenciaRepository;
        this.ichoRepository = ichoRepository;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
        this.tiposConteudoPermitidos = Arrays.stream(tiposConteudoPermitidos.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(StringUtils::hasText)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Transactional
    public IchoEvidenciaResponse adicionar(Long ichoId, MultipartFile arquivo, String descricao) {
        validarArquivo(arquivo);

        Icho icho = ichoRepository.findById(ichoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ICHO não encontrado"));

        String nomeOriginal = limparNomeArquivo(arquivo.getOriginalFilename());
        String nomeArmazenado = UUID.randomUUID() + "-" + nomeOriginal;
        Path destino = storageRoot.resolve(nomeArmazenado).normalize();

        if (!destino.startsWith(storageRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de arquivo inválido");
        }

        try {
            Files.createDirectories(storageRoot);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao salvar evidência", e);
        }

        IchoEvidencia evidencia = new IchoEvidencia();
        evidencia.setIcho(icho);
        evidencia.setNomeArquivoOriginal(nomeOriginal);
        evidencia.setNomeArquivoArmazenado(nomeArmazenado);
        evidencia.setTipoConteudo(arquivo.getContentType().toLowerCase());
        evidencia.setTamanhoBytes(arquivo.getSize());
        evidencia.setDescricao(normalizarDescricao(descricao));
        evidencia.setCriadoPor(resolverUsuarioAtual());
        evidencia.setCriadoEm(LocalDateTime.now());

        return IchoEvidenciaResponse.fromEntity(evidenciaRepository.save(evidencia));
    }

    @Transactional(readOnly = true)
    public List<IchoEvidenciaResponse> listar(Long ichoId) {
        if (!ichoRepository.existsById(ichoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ICHO não encontrado");
        }

        return evidenciaRepository.findByIchoIdOrderByCriadoEmDesc(ichoId).stream()
                .map(IchoEvidenciaResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArquivoEvidencia carregarArquivo(Long evidenciaId) {
        IchoEvidencia evidencia = buscarEvidencia(evidenciaId);
        Path caminho = storageRoot.resolve(evidencia.getNomeArquivoArmazenado()).normalize();

        if (!caminho.startsWith(storageRoot) || !Files.exists(caminho)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo da evidência não encontrado");
        }

        try {
            Resource resource = new UrlResource(caminho.toUri());
            return new ArquivoEvidencia(
                    resource,
                    evidencia.getNomeArquivoOriginal(),
                    evidencia.getTipoConteudo()
            );
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao carregar evidência", e);
        }
    }

    @Transactional
    public void remover(Long evidenciaId) {
        IchoEvidencia evidencia = buscarEvidencia(evidenciaId);
        Path caminho = storageRoot.resolve(evidencia.getNomeArquivoArmazenado()).normalize();

        evidenciaRepository.delete(evidencia);

        try {
            if (caminho.startsWith(storageRoot)) {
                Files.deleteIfExists(caminho);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao remover arquivo da evidência", e);
        }
    }

    private IchoEvidencia buscarEvidencia(Long evidenciaId) {
        return evidenciaRepository.findById(evidenciaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evidência não encontrada"));
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo da evidência deve ser informado");
        }

        String tipoConteudo = arquivo.getContentType();
        if (arquivo.getSize() > tamanhoMaximoBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo da evidência excede o tamanho máximo permitido");
        }

        if (tipoConteudo == null || !tiposConteudoPermitidos.contains(tipoConteudo.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de arquivo de evidência não permitido");
        }
    }

    private String limparNomeArquivo(String nomeArquivo) {
        String nomeLimpo = StringUtils.cleanPath(nomeArquivo == null ? "evidencia" : nomeArquivo);
        if (!StringUtils.hasText(nomeLimpo) || nomeLimpo.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de arquivo inválido");
        }
        return nomeLimpo;
    }

    private String normalizarDescricao(String descricao) {
        if (!StringUtils.hasText(descricao)) {
            return null;
        }
        return descricao.trim();
    }

    private String resolverUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "sistema";
    }

    public record ArquivoEvidencia(Resource resource, String nomeArquivo, String tipoConteudo) {
    }
}
