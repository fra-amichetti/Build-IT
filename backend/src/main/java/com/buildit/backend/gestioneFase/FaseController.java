package com.buildit.backend.gestioneFase;

import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fasi")
@CrossOrigin(origins = "http://localhost:5173")
public class FaseController {

    private final FaseLavorativaRepository faseLavorativaRepository;
    private final SquadraRepository squadraRepository;

    public FaseController(FaseLavorativaRepository faseLavorativaRepository,
                          SquadraRepository squadraRepository) {
        this.faseLavorativaRepository = faseLavorativaRepository;
        this.squadraRepository = squadraRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDettagliFase(@PathVariable Long id) {
        Optional<FaseLavorativa> fase = faseLavorativaRepository.findById(id);
        if (fase.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        return ResponseEntity.ok(fase.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificaFase(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        Optional<FaseLavorativa> opt = faseLavorativaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        FaseLavorativa fase = opt.get();
        if (fase.getStato() == StatoFase.TERMINATA) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Una fase terminata non può essere modificata"));
        }
        if (body.containsKey("nome") && !body.get("nome").isBlank()) {
    fase.setNome(body.get("nome"));
}
        if (body.containsKey("descrizione")) fase.setDescrizione(body.get("descrizione"));
        if (body.containsKey("dataFinePrevista") && !body.get("dataFinePrevista").isBlank()) {
            fase.setDataFinePrevista(LocalDate.parse(body.get("dataFinePrevista")));
        }
        if (body.containsKey("squadraId") && !body.get("squadraId").isBlank()) {
            squadraRepository.findById(Long.parseLong(body.get("squadraId")))
                .ifPresent(fase::setSquadra);
        }
        return ResponseEntity.ok(faseLavorativaRepository.save(fase));
    }

    @PutMapping("/{id}/avvia")
    public ResponseEntity<?> avviaFase(@PathVariable Long id) {
        Optional<FaseLavorativa> opt = faseLavorativaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        FaseLavorativa fase = opt.get();
        if (fase.getStato() != StatoFase.PIANIFICATA) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Solo una fase pianificata può essere avviata"));
        }
        fase.avviaFase();
        return ResponseEntity.ok(faseLavorativaRepository.save(fase));
    }

    @PutMapping("/{id}/termina")
    public ResponseEntity<?> terminaFase(@PathVariable Long id) {
        Optional<FaseLavorativa> opt = faseLavorativaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        FaseLavorativa fase = opt.get();
        if (fase.getStato() == StatoFase.TERMINATA) {
            return ResponseEntity.badRequest().body(Map.of("errore", "La fase è già terminata"));
        }
        fase.terminaFase();
        return ResponseEntity.ok(faseLavorativaRepository.save(fase));
    }

    @PutMapping("/{id}/assegna-squadra")
    public ResponseEntity<?> assegnaSquadra(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        Optional<FaseLavorativa> opt = faseLavorativaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        FaseLavorativa fase = opt.get();
        Long squadraId = Long.parseLong(body.get("squadraId"));
        Optional<Squadra> squadra = squadraRepository.findById(squadraId);
        if (squadra.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Squadra non trovata"));
        }
        fase.setSquadra(squadra.get());
        return ResponseEntity.ok(faseLavorativaRepository.save(fase));
    }
}