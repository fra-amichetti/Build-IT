package com.buildit.backend.gestioneDocumentazione;

import com.buildit.backend.dominio.*;
import com.buildit.backend.log.EsitoOperazione;
import com.buildit.backend.log.Logger;
import com.buildit.backend.log.TipoOperazione;
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
@RequestMapping("/api/cantieri/{cantiereId}/documenti-contabili")
@CrossOrigin(origins = "http://localhost:5173")
public class DocContabiliController {

    private final DocumentoContabileRepository documentoContabileRepository;
    private final CantiereRepository           cantiereRepository;
    private final FaseLavorativaRepository     faseLavorativaRepository;
    private final FileStorageService           fileStorageService;
    private final Logger                       logger;

    public DocContabiliController(DocumentoContabileRepository documentoContabileRepository,
                                   CantiereRepository cantiereRepository,
                                   FaseLavorativaRepository faseLavorativaRepository,
                                   FileStorageService fileStorageService,
                                   Logger logger) {
        this.documentoContabileRepository = documentoContabileRepository;
        this.cantiereRepository           = cantiereRepository;
        this.faseLavorativaRepository     = faseLavorativaRepository;
        this.fileStorageService           = fileStorageService;
        this.logger                       = logger;
    }

    @GetMapping
    public ResponseEntity<?> getDocumentiContabili(@PathVariable Long cantiereId) {
        if (!cantiereRepository.existsById(cantiereId)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        List<DocumentoContabile> documenti = documentoContabileRepository.findByCantiereId(cantiereId);
        return ResponseEntity.ok(documenti);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> aggiungiDocumentoContabile(
            @PathVariable Long cantiereId,
            @RequestParam String nome,
            @RequestParam String tipo,
            @RequestParam String importo,
            @RequestParam MultipartFile file,
            @RequestParam String data,
            @RequestParam(required = false) String faseId,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String email) {

        if (nome == null || nome.isBlank())
            return ResponseEntity.badRequest().body(Map.of("errore", "Il nome è obbligatorio"));
        if (nome.length() > 32)
            return ResponseEntity.badRequest().body(Map.of("errore", "Il nome non può superare 32 caratteri"));

        double importoNum;
        try {
            importoNum = Double.parseDouble(importo);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Importo non valido"));
        }
        if (importoNum <= 0)
            return ResponseEntity.badRequest().body(Map.of("errore", "L'importo deve essere maggiore di 0"));

        DocumentoContabile validatore = "Fattura".equals(tipo) ? new Fattura() : new Preventivo();
        if (!validatore.validaEstensione(file.getOriginalFilename()))
            return ResponseEntity.badRequest().body(Map.of("errore",
                "Formato non supportato. I documenti contabili devono essere in formato .pdf"));

        Optional<Cantiere> optCantiere = cantiereRepository.findById(cantiereId);
        if (optCantiere.isEmpty())
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));

        String fileUrl;
        try {
            fileUrl = fileStorageService.salva(file, "contabili");
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("errore", "Errore nel salvataggio del file"));
        }

        DocumentoContabile doc;
        if ("Fattura".equals(tipo)) {
            Fattura fattura = new Fattura();
            fattura.setStatoPagamento(StatoFattura.DA_SALDARE);
            doc = fattura;
        } else {
            doc = new Preventivo();
        }

        doc.setNome(nome);
        doc.setImporto(importoNum);
        doc.setFileUrl(fileUrl);
        doc.setData(LocalDate.parse(data));
        doc.setCantiere(optCantiere.get());

        if (faseId != null && !faseId.isBlank())
            faseLavorativaRepository.findById(Long.parseLong(faseId)).ifPresent(doc::setFase);

        DocumentoContabile salvato = documentoContabileRepository.save(doc);
        logger.log(email, TipoOperazione.CARICA_DOCUMENTO_CONTABILE,
            tipo + " '" + salvato.getNome() + "' (€" + importoNum + ") caricata nel cantiere id=" + cantiereId,
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(salvato);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaDocumentoContabile(@PathVariable Long cantiereId,
                                                        @PathVariable Long id) {
        if (!documentoContabileRepository.existsById(id))
            return ResponseEntity.status(404).body(Map.of("errore", "Documento non trovato"));
        documentoContabileRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("messaggio", "Documento eliminato"));
    }

    @PutMapping("/{id}/salda")
    public ResponseEntity<?> saldaFattura(@PathVariable Long cantiereId,
                                           @PathVariable Long id) {
        Optional<DocumentoContabile> opt = documentoContabileRepository.findById(id);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("errore", "Documento non trovato"));
        DocumentoContabile doc = opt.get();
        if (!(doc instanceof Fattura))
            return ResponseEntity.badRequest().body(Map.of("errore", "Solo le fatture possono essere saldate"));
        ((Fattura) doc).setStatoPagamento(StatoFattura.SALDATO);
        return ResponseEntity.ok(documentoContabileRepository.save(doc));
    }
}
