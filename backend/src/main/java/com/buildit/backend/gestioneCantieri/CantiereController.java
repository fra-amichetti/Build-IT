package com.buildit.backend.gestioneCantieri;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.repository.CantiereRepository;

// import com.buildit.backend.dominio.StatoCantiere;

@RestController
@RequestMapping("/api/cantieri")
@CrossOrigin(origins = "http://localhost:5173")
public class CantiereController {

    @Autowired
    private CantiereRepository cantiereRepository;

    // GET /api/cantieri/{id} — dettaglio cantiere
    @GetMapping("/{id}")
    public ResponseEntity<?> getDettagliCantiere(@PathVariable Long id) {
        Optional<Cantiere> cantiere = cantiereRepository.findById(id);
        if (cantiere.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("errore", "Cantiere non trovato"));
        }
        return ResponseEntity.ok(cantiere.get());
    }

    // PUT /api/cantieri/{id} — modifica cantiere (solo nome, indirizzo, emailCliente)
    @PutMapping("/{id}")
    public ResponseEntity<?> modificaCantiere(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        Optional<Cantiere> opt = cantiereRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("errore", "Cantiere non trovato"));
        }

        Cantiere cantiere = opt.get();

        /*
        if (cantiere.getStato() == StatoCantiere.TERMINATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Un cantiere terminato non può essere modificato"));
        }
        */
        if ("TERMINATO".equals(cantiere.getStato())) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Un cantiere terminato non può essere modificato"));
        }

        if (body.containsKey("nome") && !body.get("nome").isBlank()) {
            cantiere.setNome(body.get("nome"));
        }
        if (body.containsKey("indirizzo") && !body.get("indirizzo").isBlank()) {
            cantiere.setIndirizzo(body.get("indirizzo"));
        }
        if (body.containsKey("emailCliente")) {
            cantiere.setEmailCliente(body.get("emailCliente"));
        }

        return ResponseEntity.ok(cantiereRepository.save(cantiere));
    }

    // PUT /api/cantieri/{id}/avvia — avvia cantiere
    @PutMapping("/{id}/avvia")
    public ResponseEntity<?> iniziaLavoriCantiere(@PathVariable Long id) {
        Optional<Cantiere> opt = cantiereRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("errore", "Cantiere non trovato"));
        }

        Cantiere cantiere = opt.get();

        /*
        if (cantiere.getStato() != StatoCantiere.PIANIFICATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Solo un cantiere pianificato può essere avviato"));
        }
        */
        if (!"PIANIFICATO".equals(cantiere.getStato())) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Solo un cantiere pianificato può essere avviato"));
        }

        cantiere.iniziaLavori();
        return ResponseEntity.ok(cantiereRepository.save(cantiere));
    }

    // PUT /api/cantieri/{id}/termina — termina cantiere
    @PutMapping("/{id}/termina")
    public ResponseEntity<?> terminaCantiere(@PathVariable Long id) {
        Optional<Cantiere> opt = cantiereRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("errore", "Cantiere non trovato"));
        }

        Cantiere cantiere = opt.get();

        /*
        if (cantiere.getStato() == StatoCantiere.TERMINATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Il cantiere è già terminato"));
        }
        if (cantiere.getStato() == StatoCantiere.PIANIFICATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Un cantiere pianificato non può essere terminato direttamente"));
        }
        */
        if ("TERMINATO".equals(cantiere.getStato())) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Il cantiere è già terminato"));
        }
        if ("PIANIFICATO".equals(cantiere.getStato())) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Un cantiere pianificato non può essere terminato direttamente"));
        }

        cantiere.terminaCantiere();
        return ResponseEntity.ok(cantiereRepository.save(cantiere));
    }
}