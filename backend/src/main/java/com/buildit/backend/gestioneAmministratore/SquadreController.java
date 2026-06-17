package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.Specializzazione;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.log.EsitoOperazione;
import com.buildit.backend.log.Logger;
import com.buildit.backend.log.TipoOperazione;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/squadre")
@CrossOrigin(origins = "http://localhost:5173")
public class SquadreController {

    private final SquadraRepository        squadraRepository;
    private final FaseLavorativaRepository faseLavorativaRepository;
    private final Logger                   logger;

    public SquadreController(SquadraRepository squadraRepository,
                              FaseLavorativaRepository faseLavorativaRepository,
                              Logger logger) {
        this.squadraRepository        = squadraRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
        this.logger                   = logger;
    }

    @GetMapping
    public ResponseEntity<?> getSquadre() {
        return ResponseEntity.ok(squadraRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> aggiungiSquadra(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String email) {

        String nome               = body.get("nome");
        String specializzazioneStr = body.get("specializzazione");
        String numComponentiStr   = body.get("numeroComponenti");
        String nomeReferente      = body.get("nomeReferente");

        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Il nome è obbligatorio"));
        }

        Squadra squadra = new Squadra();
        squadra.setNome(nome);
        squadra.setNomeReferente(nomeReferente);
        squadra.setNumeroComponenti(Integer.parseInt(numComponentiStr));
        squadra.setSpecializzazione(Specializzazione.valueOf(specializzazioneStr));

        Squadra salvata = squadraRepository.save(squadra);
        logger.log(email, TipoOperazione.CREA_SQUADRA,
            "Squadra creata: '" + salvata.getNome() + "' — " + salvata.getSpecializzazione(),
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(salvata);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaSquadra(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String email) {

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

        String nomeSquadra = opt.get().getNome();
        squadraRepository.deleteById(id);
        logger.log(email, TipoOperazione.ELIMINA_SQUADRA,
            "Squadra eliminata: '" + nomeSquadra + "' (id=" + id + ")",
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(Map.of("messaggio", "Squadra eliminata"));
    }
}
