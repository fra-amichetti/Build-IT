package com.buildit.backend.gestioneDocumentazione;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot.resolve("tecnici"));
            Files.createDirectories(this.uploadRoot.resolve("contabili"));
        } catch (IOException e) {
            throw new RuntimeException("Impossibile creare le directory di upload", e);
        }
    }

    // Salva il file e restituisce l'URL relativo per servirlo.
    public String salva(MultipartFile file, String sottocartella) throws IOException {
        String nomeOriginale = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        int dot = nomeOriginale.lastIndexOf('.');
        String ext = dot >= 0 ? nomeOriginale.substring(dot).toLowerCase() : "";
        String nomeSalvato = UUID.randomUUID() + ext;
        Path destinazione = uploadRoot.resolve(sottocartella).resolve(nomeSalvato);
        Files.copy(file.getInputStream(), destinazione, StandardCopyOption.REPLACE_EXISTING);
        return "/api/files/" + sottocartella + "/" + nomeSalvato;
    }

    public Resource carica(String sottocartella, String nomefile) throws IOException {
        Path filePath = uploadRoot.resolve(sottocartella).resolve(nomefile).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) throw new IOException("File non trovato: " + nomefile);
        return resource;
    }
}
