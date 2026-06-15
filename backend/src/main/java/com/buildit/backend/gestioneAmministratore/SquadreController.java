package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.Specializzazione;
import com.buildit.backend.repository.SquadraRepository;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.dominio.StatoFase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/squadre")
@CrossOrigin(origins = "http://localhost:5173")
public class SquadreController {

    private final SquadraRepository squadraRepository;
    private final FaseLavorativaRepository faseLavorativaRepository;

    public SquadreController(SquadraRepository squadraRepository,
                              FaseLavorativaRepository faseLavorativaRepository) {
        this.squadraRepository = squadraRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
    }

    @GetMapping
    public ResponseEntity<?> getSquadre() {
        List<Squadra> squadre = squadraRepository.findAll();
        return ResponseEntity.ok(squadre);
    }

    @PostMapping
    public ResponseEntity<?> aggiungiSquadra(@RequestBody Map<String, String> body) {
        String nome = body.get("nome");
        String specializzazioneStr = body.get("specializzazione");
        String numComponentiStr = body.get("numeroComponenti");
        String nomeReferente = body.get("nomeReferente");

        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Il nome è obbligatorio"));
        }

        Squadra squadra = new Squadra();
        squadra.setNome(nome);
        squadra.setNomeReferente(nomeReferente);
        squadra.setNumeroComponenti(Integer.parseInt(numComponentiStr));
        squadra.setSpecializzazione(Specializzazione.valueOf(specializzazioneStr));

        return ResponseEntity.ok(squadraRepository.save(squadra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaSquadra(@PathVariable Long id) {
        Optional<Squadra> opt = squadraRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Squadra non trovata"));
        }

        boolean hasFasiAttive = faseLavorativaRepository.findAll().stream()
            .anyMatch(f -> f.getSquadra() != null 
                && f.getSquadra().getId().equals(id)
                && f.getStato() != StatoFase.TERMINATA);

        if (hasFasiAttive) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Squadra impegnata in fasi attive"));
        }

        squadraRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("messaggio", "Squadra eliminata"));
    }
}