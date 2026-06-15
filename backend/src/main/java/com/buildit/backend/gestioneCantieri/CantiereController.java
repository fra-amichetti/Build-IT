package com.buildit.backend.gestioneCantieri;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;

@RestController
@RequestMapping("/api/cantieri")
@CrossOrigin(origins = "http://localhost:5173")
public class CantiereController {

    private final CantiereRepository cantiereRepository;
    private final FaseLavorativaRepository faseLavorativaRepository;
    private final SquadraRepository squadraRepository;

    public CantiereController(CantiereRepository cantiereRepository,
                               FaseLavorativaRepository faseLavorativaRepository,
                               SquadraRepository squadraRepository) {
        this.cantiereRepository = cantiereRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
        this.squadraRepository = squadraRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDettagliCantiere(@PathVariable Long id) {
        Optional<Cantiere> cantiere = cantiereRepository.findById(id);
        if (cantiere.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        return ResponseEntity.ok(cantiere.get());
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
            return ResponseEntity.badRequest().body(Map.of("errore", "Un cantiere terminato non può essere modificato"));
        }
        if (body.containsKey("nome") && !body.get("nome").isBlank()) cantiere.setNome(body.get("nome"));
        if (body.containsKey("indirizzo") && !body.get("indirizzo").isBlank()) cantiere.setIndirizzo(body.get("indirizzo"));
        if (body.containsKey("emailCliente")) cantiere.setEmailCliente(body.get("emailCliente"));
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
            return ResponseEntity.badRequest().body(Map.of("errore", "Solo un cantiere pianificato può essere avviato"));
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
            return ResponseEntity.badRequest().body(Map.of("errore", "Un cantiere pianificato non può essere terminato direttamente"));
        }
        cantiere.terminaCantiere();
        return ResponseEntity.ok(cantiereRepository.save(cantiere));
    }

    @GetMapping("/{id}/fasi")
    public ResponseEntity<?> getFasi(@PathVariable Long id) {
        if (!cantiereRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("errore", "Cantiere non trovato"));
        }
        List<FaseLavorativa> fasi = faseLavorativaRepository.findByCantiereId(id);
        return ResponseEntity.ok(fasi);
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
            return ResponseEntity.badRequest().body(Map.of("errore", "Non si possono aggiungere fasi a un cantiere terminato"));
        }
        FaseLavorativa fase = new FaseLavorativa();
        fase.setNome(body.get("nome"));
        fase.setDescrizione(body.get("descrizione"));
        fase.setDataInizioPrevista(LocalDate.parse(body.get("dataInizioPrevista")));
        fase.setDataFinePrevista(LocalDate.parse(body.get("dataFinePrevista")));
        fase.setStato(StatoFase.PIANIFICATA);
        fase.setCantiere(cantiere);
        if (body.get("squadraId") != null && !body.get("squadraId").isBlank()) {
            squadraRepository.findById(Long.parseLong(body.get("squadraId")))
                .ifPresent(fase::setSquadra);
        }
        return ResponseEntity.ok(faseLavorativaRepository.save(fase));
    }
}