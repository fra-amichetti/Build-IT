package com.buildit.backend.gestioneDocumentazione;

import com.buildit.backend.dominio.*;
import com.buildit.backend.repository.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cantieri/{cantiereId}/documenti-tecnici")
@CrossOrigin(origins = "http://localhost:5173")
public class DocTecniciController {

    private final DocumentoTecnicoRepository documentoTecnicoRepository;
    private final CantiereRepository cantiereRepository;
    private final FaseLavorativaRepository faseLavorativaRepository;
    private final FileStorageService fileStorageService;

    public DocTecniciController(DocumentoTecnicoRepository documentoTecnicoRepository,
                                 CantiereRepository cantiereRepository,
                                 FaseLavorativaRepository faseLavorativaRepository,
                                 FileStorageService fileStorageService) {
        this.documentoTecnicoRepository = documentoTecnicoRepository;
        this.cantiereRepository = cantiereRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<?> getDocumentiTecnici(@PathVariable Long cantiereId) {
        if (!cantiereRepository.existsById(cantiereId)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        List<DocumentoTecnico> documenti = documentoTecnicoRepository.findByCantiereId(cantiereId);
        return ResponseEntity.ok(documenti);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> aggiungiDocumentoTecnico(
            @PathVariable Long cantiereId,
            @RequestParam String nome,
            @RequestParam(required = false) String tipologia,
            @RequestParam MultipartFile file,
            @RequestParam String data,
            @RequestParam(required = false) String faseId) {

        // Validazioni di campo
        if (nome == null || nome.isBlank())
            return ResponseEntity.badRequest().body(Map.of("errore", "Il nome è obbligatorio"));
        if (nome.length() > 32)
            return ResponseEntity.badRequest().body(Map.of("errore", "Il nome non può superare 32 caratteri"));
        if (tipologia != null && tipologia.length() > 100)
            return ResponseEntity.badRequest().body(Map.of("errore", "La tipologia non può superare 100 caratteri"));

        // Validazione estensione (polimorfismo)
        DocumentoTecnico validatore = new DocumentoTecnico();
        if (!validatore.validaEstensione(file.getOriginalFilename()))
            return ResponseEntity.badRequest().body(Map.of("errore",
                    "Formato non supportato. Usa .pdf, .jpg o .png"));

        Optional<Cantiere> optCantiere = cantiereRepository.findById(cantiereId);
        if (optCantiere.isEmpty())
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));

        String fileUrl;
        try {
            fileUrl = fileStorageService.salva(file, "tecnici");
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("errore", "Errore nel salvataggio del file"));
        }

        DocumentoTecnico doc = new DocumentoTecnico();
        doc.setNome(nome);
        doc.setTipologia(tipologia != null ? tipologia : "");
        doc.setFileUrl(fileUrl);
        doc.setData(LocalDate.parse(data));
        doc.setCantiere(optCantiere.get());

        if (faseId != null && !faseId.isBlank())
            faseLavorativaRepository.findById(Long.parseLong(faseId)).ifPresent(doc::setFase);

        return ResponseEntity.ok(documentoTecnicoRepository.save(doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaDocumentoTecnico(@PathVariable Long cantiereId,
                                                      @PathVariable Long id) {
        if (!documentoTecnicoRepository.existsById(id))
            return ResponseEntity.status(404).body(Map.of("errore", "Documento non trovato"));
        documentoTecnicoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("messaggio", "Documento eliminato"));
    }
}
