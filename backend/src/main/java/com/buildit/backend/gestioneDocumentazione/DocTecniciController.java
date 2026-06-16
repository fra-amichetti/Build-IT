package com.buildit.backend.gestioneDocumentazione;

import com.buildit.backend.dominio.*;
import com.buildit.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    public DocTecniciController(DocumentoTecnicoRepository documentoTecnicoRepository,
                                 CantiereRepository cantiereRepository,
                                 FaseLavorativaRepository faseLavorativaRepository) {
        this.documentoTecnicoRepository = documentoTecnicoRepository;
        this.cantiereRepository = cantiereRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDocumentiTecnici(@PathVariable Long cantiereId) {
        if (!cantiereRepository.existsById(cantiereId)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        List<DocumentoTecnico> documenti = documentoTecnicoRepository.findByCantiereId(cantiereId);
        return ResponseEntity.ok(documenti);
    }

    @PostMapping
    public ResponseEntity<?> aggiungiDocumentoTecnico(@PathVariable Long cantiereId,
                                                       @RequestBody Map<String, String> body) {
        Optional<Cantiere> optCantiere = cantiereRepository.findById(cantiereId);
        if (optCantiere.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }

        DocumentoTecnico doc = new DocumentoTecnico();
        doc.setNome(body.get("nome"));
        doc.setFileUrl(body.get("fileUrl"));
        doc.setData(LocalDate.parse(body.get("data")));
       doc.setTipologia(body.get("tipologia"));
        doc.setCantiere(optCantiere.get());

        if (body.get("faseId") != null && !body.get("faseId").isBlank()) {
            faseLavorativaRepository.findById(Long.parseLong(body.get("faseId")))
                .ifPresent(doc::setFase);
        }

        return ResponseEntity.ok(documentoTecnicoRepository.save(doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaDocumentoTecnico(@PathVariable Long cantiereId,
                                                      @PathVariable Long id) {
        if (!documentoTecnicoRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Documento non trovato"));
        }
        documentoTecnicoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("messaggio", "Documento eliminato"));
    }
}
