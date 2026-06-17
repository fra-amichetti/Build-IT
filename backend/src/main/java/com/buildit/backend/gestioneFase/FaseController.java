package com.buildit.backend.gestioneFase;

import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.log.EsitoOperazione;
import com.buildit.backend.log.Logger;
import com.buildit.backend.log.TipoOperazione;
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
    private final SquadraRepository        squadraRepository;
    private final Logger                   logger;

    public FaseController(FaseLavorativaRepository faseLavorativaRepository,
                          SquadraRepository squadraRepository,
                          Logger logger) {
        this.faseLavorativaRepository = faseLavorativaRepository;
        this.squadraRepository        = squadraRepository;
        this.logger                   = logger;
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
    public ResponseEntity<?> modificaFase(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String email) {

        Optional<FaseLavorativa> opt = faseLavorativaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        FaseLavorativa fase = opt.get();
        if (fase.getStato() == StatoFase.TERMINATA) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Una fase terminata non può essere modificata"));
        }
        if (body.containsKey("nome") && !body.get("nome").isBlank())
            fase.setNome(body.get("nome"));
        if (body.containsKey("descrizione"))
            fase.setDescrizione(body.get("descrizione"));

        LocalDate nuovaFine = (body.containsKey("dataFinePrevista") && !body.get("dataFinePrevista").isBlank())
            ? LocalDate.parse(body.get("dataFinePrevista"))
            : fase.getDataFinePrevista();

        if (body.containsKey("squadraId") && !body.get("squadraId").isBlank()) {
            Long squadraId = Long.parseLong(body.get("squadraId"));
            boolean overlap = !faseLavorativaRepository
                .findOverlappingBySquadra(squadraId, fase.getId(), fase.getDataInizioPrevista(), nuovaFine)
                .isEmpty();
            if (overlap) {
                return ResponseEntity.badRequest().body(Map.of("errore",
                    "La squadra selezionata è già impegnata in un'altra fase che si sovrappone a questo periodo"));
            }
            squadraRepository.findById(squadraId).ifPresent(fase::setSquadra);
        }

        fase.setDataFinePrevista(nuovaFine);
        FaseLavorativa salvata = faseLavorativaRepository.save(fase);
        logger.log(email, TipoOperazione.MODIFICA_FASE,
            "Fase modificata: '" + salvata.getNome() + "' (id=" + id + ")",
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(salvata);
    }

    @PutMapping("/{id}/avvia")
    public ResponseEntity<?> avviaFase(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String email) {

        Optional<FaseLavorativa> opt = faseLavorativaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        FaseLavorativa fase = opt.get();
        if (fase.getStato() != StatoFase.PIANIFICATA) {
            return ResponseEntity.badRequest().body(Map.of("errore", "Solo una fase pianificata può essere avviata"));
        }
        fase.avviaFase();
        FaseLavorativa salvata = faseLavorativaRepository.save(fase);
        logger.log(email, TipoOperazione.AVVIA_FASE,
            "Fase avviata: '" + salvata.getNome() + "' (id=" + id + ")",
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(salvata);
    }

    @PutMapping("/{id}/termina")
    public ResponseEntity<?> terminaFase(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String email) {

        Optional<FaseLavorativa> opt = faseLavorativaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("errore", "Fase non trovata"));
        }
        FaseLavorativa fase = opt.get();
        if (fase.getStato() == StatoFase.TERMINATA) {
            return ResponseEntity.badRequest().body(Map.of("errore", "La fase è già terminata"));
        }
        fase.terminaFase();
        FaseLavorativa salvata = faseLavorativaRepository.save(fase);
        logger.log(email, TipoOperazione.TERMINA_FASE,
            "Fase terminata: '" + salvata.getNome() + "' (id=" + id + ")",
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(salvata);
    }

    @PutMapping("/{id}/assegna-squadra")
    public ResponseEntity<?> assegnaSquadra(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String email) {

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
        FaseLavorativa salvata = faseLavorativaRepository.save(fase);
        logger.log(email, TipoOperazione.ASSEGNA_SQUADRA,
            "Squadra '" + squadra.get().getNome() + "' assegnata alla fase '" + salvata.getNome() + "'",
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(salvata);
    }
}
