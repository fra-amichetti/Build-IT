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
@RequestMapping("/api/cantieri/{cantiereId}/documenti-contabili")
@CrossOrigin(origins = "http://localhost:5173")
public class DocContabiliController {

    private final DocumentoContabileRepository documentoContabileRepository;
    private final CantiereRepository cantiereRepository;
    private final FaseLavorativaRepository faseLavorativaRepository;

    public DocContabiliController(DocumentoContabileRepository documentoContabileRepository,
                                   CantiereRepository cantiereRepository,
                                   FaseLavorativaRepository faseLavorativaRepository) {
        this.documentoContabileRepository = documentoContabileRepository;
        this.cantiereRepository = cantiereRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDocumentiContabili(@PathVariable Long cantiereId) {
        if (!cantiereRepository.existsById(cantiereId)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        List<DocumentoContabile> documenti = documentoContabileRepository.findByCantiereId(cantiereId);
        return ResponseEntity.ok(documenti);
    }

    @PostMapping
    public ResponseEntity<?> aggiungiDocumentoContabile(@PathVariable Long cantiereId,
                                                         @RequestBody Map<String, String> body) {
        Optional<Cantiere> optCantiere = cantiereRepository.findById(cantiereId);
        if (optCantiere.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }

        String tipo = body.get("tipo");
        DocumentoContabile doc;

        if ("Fattura".equals(tipo)) {
            Fattura fattura = new Fattura();
            fattura.setStatoPagamento(StatoFattura.DA_SALDARE);
            doc = fattura;
        } else {
            doc = new Preventivo();
        }

        doc.setNome(body.get("nome"));
        doc.setFileUrl(body.get("fileUrl"));
       doc.setData(LocalDate.parse(body.get("data")));
        doc.setImporto(Double.parseDouble(body.get("importo")));
        doc.setCantiere(optCantiere.get());

        if (body.get("faseId") != null && !body.get("faseId").isBlank()) {
            faseLavorativaRepository.findById(Long.parseLong(body.get("faseId")))
                .ifPresent(doc::setFase);
        }

        return ResponseEntity.ok(documentoContabileRepository.save(doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaDocumentoContabile(@PathVariable Long cantiereId,
                                                        @PathVariable Long id) {
        if (!documentoContabileRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Documento non trovato"));
        }
        documentoContabileRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("messaggio", "Documento eliminato"));
    }

    @PutMapping("/{id}/salda")
    public ResponseEntity<?> saldaFattura(@PathVariable Long cantiereId,
                                           @PathVariable Long id) {
        Optional<DocumentoContabile> opt = documentoContabileRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Documento non trovato"));
        }
        DocumentoContabile doc = opt.get();
        if (!(doc instanceof Fattura)) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Solo le fatture possono essere saldate"));
        }
        ((Fattura) doc).setStatoPagamento(StatoFattura.SALDATO);
        return ResponseEntity.ok(documentoContabileRepository.save(doc));
    }
}