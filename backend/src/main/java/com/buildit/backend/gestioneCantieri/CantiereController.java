package com.buildit.backend.gestioneCantieri;

import com.buildit.backend.dominio.*;
import com.buildit.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cantieri")
@CrossOrigin(origins = "http://localhost:5173")
public class CantiereController {

    private final CantiereRepository       cantiereRepository;
    private final FaseLavorativaRepository faseLavorativaRepository;
    private final SquadraRepository        squadraRepository;

    public CantiereController(CantiereRepository cantiereRepository,
                               FaseLavorativaRepository faseLavorativaRepository,
                               SquadraRepository squadraRepository) {
        this.cantiereRepository       = cantiereRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
        this.squadraRepository        = squadraRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDettagliCantiere(@PathVariable Long id) {
        Optional<Cantiere> opt = cantiereRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        Cantiere cantiere = opt.get();
        if (cantiere.verificaRitardo()) {
            cantiere.setStato(StatoCantiere.IN_RITARDO);
            cantiereRepository.save(cantiere);
        }
        return ResponseEntity.ok(cantiere);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificaCantiere(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        Optional<Cantiere> opt = cantiereRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        Cantiere cantiere = opt.get();
        if (cantiere.getStato() == StatoCantiere.TERMINATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Un cantiere terminato non può essere modificato"));
        }
        if (body.containsKey("nome") && !body.get("nome").isBlank())
            cantiere.setNome(body.get("nome"));
        if (body.containsKey("indirizzo") && !body.get("indirizzo").isBlank())
            cantiere.setIndirizzo(body.get("indirizzo"));
        if (body.containsKey("emailCliente"))
            cantiere.setEmailCliente(body.get("emailCliente"));
        return ResponseEntity.ok(cantiereRepository.save(cantiere));
    }

    @PutMapping("/{id}/avvia")
    public ResponseEntity<?> iniziaLavoriCantiere(@PathVariable Long id) {
        Optional<Cantiere> opt = cantiereRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        Cantiere cantiere = opt.get();
        if (cantiere.getStato() != StatoCantiere.PIANIFICATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Solo un cantiere pianificato può essere avviato"));
        }
        cantiere.iniziaLavori();
        return ResponseEntity.ok(cantiereRepository.save(cantiere));
    }

    @PutMapping("/{id}/termina")
    public ResponseEntity<?> terminaCantiere(@PathVariable Long id) {
        Optional<Cantiere> opt = cantiereRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        Cantiere cantiere = opt.get();
        if (cantiere.getStato() == StatoCantiere.TERMINATO) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Il cantiere è già terminato"));
        }
        if (cantiere.getStato() == StatoCantiere.PIANIFICATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Un cantiere pianificato non può essere terminato direttamente"));
        }

        long fasiNonTerminate = faseLavorativaRepository.findByCantiereId(id).stream()
            .filter(f -> f.getStato() != StatoFase.TERMINATA).count();
        if (fasiNonTerminate > 0) {
            return ResponseEntity.badRequest().body(Map.of("errore",
                "Impossibile terminare il cantiere: " + fasiNonTerminate +
                " fase/i non ancora terminate. Terminare tutte le fasi prima di chiudere il cantiere."));
        }

        cantiere.terminaCantiere();
        return ResponseEntity.ok(cantiereRepository.save(cantiere));
    }

    @GetMapping("/{id}/fasi")
    public ResponseEntity<?> getFasi(@PathVariable Long id) {
        if (!cantiereRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        return ResponseEntity.ok(faseLavorativaRepository.findByCantiereId(id));
    }

    @PostMapping("/{id}/fasi")
    public ResponseEntity<?> aggiungiFase(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        Optional<Cantiere> optCantiere = cantiereRepository.findById(id);
        if (optCantiere.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        Cantiere cantiere = optCantiere.get();
        if (cantiere.getStato() == StatoCantiere.TERMINATO) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Non si possono aggiungere fasi a un cantiere terminato"));
        }

        LocalDate inizio = LocalDate.parse(body.get("dataInizioPrevista"));
        LocalDate fine   = LocalDate.parse(body.get("dataFinePrevista"));

        String squadraIdStr = body.get("squadraId");
        if (squadraIdStr != null && !squadraIdStr.isBlank()) {
            Long squadraId = Long.parseLong(squadraIdStr);
            boolean overlap = !faseLavorativaRepository
                .findOverlappingBySquadra(squadraId, -1L, inizio, fine).isEmpty();
            if (overlap) {
                return ResponseEntity.badRequest().body(Map.of("errore",
                    "La squadra selezionata è già impegnata in un'altra fase che si sovrappone a questo periodo"));
            }
        }

        FaseLavorativa fase = new FaseLavorativa();
        fase.setNome(body.get("nome"));
        fase.setDescrizione(body.get("descrizione"));
        fase.setDataInizioPrevista(inizio);
        fase.setDataFinePrevista(fine);
        fase.setStato(StatoFase.PIANIFICATA);
        fase.setCantiere(cantiere);
        if (squadraIdStr != null && !squadraIdStr.isBlank()) {
            squadraRepository.findById(Long.parseLong(squadraIdStr)).ifPresent(fase::setSquadra);
        }

        return ResponseEntity.ok(faseLavorativaRepository.save(fase));
    }
}
