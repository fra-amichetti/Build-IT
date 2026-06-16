package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.*;
import com.buildit.backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistiche")
@CrossOrigin(origins = "http://localhost:5173")
public class StatisticheController {

    private final CantiereRepository cantiereRepository;
    private final SquadraRepository squadraRepository;
    private final FaseLavorativaRepository faseLavorativaRepository;
    private final DocumentoContabileRepository documentoContabileRepository;

    public StatisticheController(CantiereRepository cantiereRepository,
                                  SquadraRepository squadraRepository,
                                  FaseLavorativaRepository faseLavorativaRepository,
                                  DocumentoContabileRepository documentoContabileRepository) {
        this.cantiereRepository = cantiereRepository;
        this.squadraRepository = squadraRepository;
        this.faseLavorativaRepository = faseLavorativaRepository;
        this.documentoContabileRepository = documentoContabileRepository;
    }

    @GetMapping
    public ResponseEntity<?> getStatistiche() {
        List<DocumentoContabile> tuttiIDocumenti = documentoContabileRepository.findAll();

        List<Fattura> fatture = tuttiIDocumenti.stream()
            .filter(d -> d instanceof Fattura)
            .map(d -> (Fattura) d)
            .collect(Collectors.toList());

        double fatturatoTotale = fatture.stream().mapToDouble(Fattura::getImporto).sum();
        double fatturatoIncassato = fatture.stream()
            .filter(f -> f.getStatoPagamento() == StatoFattura.SALDATO)
            .mapToDouble(Fattura::getImporto).sum();

        List<Cantiere> tuttiICantieri = cantiereRepository.findAll();
        long numeroCantieriAttivi = tuttiICantieri.stream()
            .filter(c -> c.getStato() == StatoCantiere.IN_CORSO || c.getStato() == StatoCantiere.IN_RITARDO)
            .count();
        long numeroCantieriInRitardo = tuttiICantieri.stream()
            .filter(c -> c.getStato() == StatoCantiere.IN_RITARDO).count();
        long numeroCantieriTerminati = tuttiICantieri.stream()
            .filter(c -> c.getStato() == StatoCantiere.TERMINATO).count();

        List<FaseLavorativa> tutteLeFasi = faseLavorativaRepository.findAll();
        List<Map<String, Object>> squadreImpiegate = squadraRepository.findAll().stream()
            .filter(squadra -> tutteLeFasi.stream().anyMatch(fase ->
                fase.getSquadra() != null
                && fase.getSquadra().getId().equals(squadra.getId())
                && fase.getStato() != StatoFase.TERMINATA))
            .map(squadra -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", squadra.getId());
                m.put("nome", squadra.getNome());
                m.put("specializzazione", squadra.getSpecializzazione());
                return m;
            })
            .collect(Collectors.toList());

        Map<String, Object> risultato = new HashMap<>();
        risultato.put("fatturatoTotale", fatturatoTotale);
        risultato.put("fatturatoIncassato", fatturatoIncassato);
        risultato.put("saldoDaIncassare", fatturatoTotale - fatturatoIncassato);
        risultato.put("numeroCantieriAttivi", numeroCantieriAttivi);
        risultato.put("numeroCantieriInRitardo", numeroCantieriInRitardo);
        risultato.put("numeroCantieriTerminati", numeroCantieriTerminati);
        risultato.put("squadreImpiegate", squadreImpiegate);

        return ResponseEntity.ok(risultato);
    }

    @GetMapping("/cantiere/{idCantiere}")
    public ResponseEntity<?> getStatisticheCantiere(@PathVariable Long idCantiere) {
        List<DocumentoContabile> docCantiere = documentoContabileRepository.findByCantiereId(idCantiere);

        double fatturatoCantiere = docCantiere.stream()
            .filter(d -> d instanceof Fattura)
            .mapToDouble(DocumentoContabile::getImporto).sum();

        double saldoCantiere = docCantiere.stream()
            .filter(d -> d instanceof Fattura && ((Fattura) d).getStatoPagamento() == StatoFattura.DA_SALDARE)
            .mapToDouble(DocumentoContabile::getImporto).sum();

        Map<String, Object> risultato = new HashMap<>();
        risultato.put("fatturatoCantiere", fatturatoCantiere);
        risultato.put("saldoCantiere", saldoCantiere);

        return ResponseEntity.ok(risultato);
    }
}