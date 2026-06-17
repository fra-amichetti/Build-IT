package com.buildit.backend.log;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Endpoint REST per la visualizzazione del log di sistema.
 * Accessibile SOLO dall'Amministratore (controllo via header X-User-Role).
 */
@RestController
@RequestMapping("/api/log")
@CrossOrigin(origins = "http://localhost:5173")
public class LogController {

    private final Logger logger;

    public LogController(Logger logger) {
        this.logger = logger;
    }

    /**
     * GET /api/log
     * Parametri di filtro (tutti opzionali):
     *   ?email=     → filtro parziale sull'email utente
     *   ?operazione= → filtro esatto sul tipo operazione (es. LOGIN_FALLITO)
     *   ?da=        → data inizio nel formato yyyy-MM-dd
     *   ?a=         → data fine nel formato yyyy-MM-dd
     */
    @GetMapping
    public ResponseEntity<?> getLog(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String operazione,
            @RequestParam(required = false) String da,
            @RequestParam(required = false) String a,
            @RequestHeader(value = "X-User-Role", required = false) String ruolo) {

        if (!"AMMINISTRATORE".equals(ruolo)) {
            return ResponseEntity.status(403)
                .body(Map.of("errore", "Accesso negato: solo gli Amministratori possono visualizzare il log"));
        }

        TipoOperazione op   = parseOperazione(operazione);
        LocalDate      dataDA = parseData(da);
        LocalDate      dataA  = parseData(a);

        boolean hasFiltri = email != null || op != null || dataDA != null || dataA != null;
        List<LogEntry> risultati = hasFiltri
            ? logger.filtra(email, op, dataDA, dataA)
            : logger.getAll();

        return ResponseEntity.ok(risultati);
    }

    /** GET /api/log/sospetti — restituisce solo le voci con esito SOSPETTO */
    @GetMapping("/sospetti")
    public ResponseEntity<?> getAccessiSospetti(
            @RequestHeader(value = "X-User-Role", required = false) String ruolo) {

        if (!"AMMINISTRATORE".equals(ruolo)) {
            return ResponseEntity.status(403).body(Map.of("errore", "Accesso negato"));
        }
        return ResponseEntity.ok(logger.getAccessiSospetti());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private TipoOperazione parseOperazione(String s) {
        if (s == null || s.isBlank()) return null;
        try { return TipoOperazione.valueOf(s); }
        catch (IllegalArgumentException e) { return null; }
    }

    private LocalDate parseData(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }
}
