package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.*;
import com.buildit.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CantiereRepository cantiereRepository;

    @Autowired
    private SquadraRepository squadraRepository;

    @Autowired
    private FaseLavorativaRepository faseLavorativaRepository;

    @Autowired
    private DocumentoContabileRepository documentoContabileRepository;

    @GetMapping
    public ResponseEntity<?> getStatistiche() {

        // ── Indicatori economici (solo Fatture, non Preventivi) ──
        List<DocumentoContabile> tuttiIDocumenti = documentoContabileRepository.findAll();

        List<Fattura> fatture = tuttiIDocumenti.stream()
            .filter(d -> d instanceof Fattura)
            .map(d -> (Fattura) d)
            .collect(Collectors.toList());

        double fatturatoTotale = fatture.stream()
            .mapToDouble(Fattura::getImporto)
            .sum();

        double fatturatoIncassato = fatture.stream()
            .filter(f -> f.getStatoPagamento() == StatoFattura.SALDATO)
            .mapToDouble(Fattura::getImporto)
            .sum();

        double saldoDaIncassare = fatturatoTotale - fatturatoIncassato;

        // ── Indicatori operativi ──
        List<Cantiere> tuttiICantieri = cantiereRepository.findAll();

        long numeroCantieriAttivi = tuttiICantieri.stream()
            .filter(c -> "IN_CORSO".equals(c.getStato()))
            .count();

        long numeroCantieriInRitardo = tuttiICantieri.stream()
            .filter(c -> "IN_RITARDO".equals(c.getStato()))
            .count();

        long numeroCantieriTerminati = tuttiICantieri.stream()
            .filter(c -> "TERMINATO".equals(c.getStato()))
            .count();

        // Squadre attualmente impiegate: hanno almeno una fase non terminata
        List<FaseLavorativa> tutteLeFasi = faseLavorativaRepository.findAll();

        List<Map<String, Object>> squadreImpiegate = squadraRepository.findAll().stream()
            .filter(squadra -> tutteLeFasi.stream().anyMatch(fase ->
                fase.getSquadra() != null
                && fase.getSquadra().getId().equals(squadra.getId())
                && fase.getStato() != StatoFase.TERMINATA
            ))
            .map(squadra -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", squadra.getId());
                m.put("nome", squadra.getNome());
                m.put("specializzazione", squadra.getSpecializzazione());
                return m;
            })
            .collect(Collectors.toList());

        // ── Risposta ──
        Map<String, Object> risultato = new HashMap<>();
        risultato.put("fatturatoTotale", fatturatoTotale);
        risultato.put("fatturatoIncassato", fatturatoIncassato);
        risultato.put("saldoDaIncassare", saldoDaIncassare);
        risultato.put("numeroCantieriAttivi", numeroCantieriAttivi);
        risultato.put("numeroCantieriInRitardo", numeroCantieriInRitardo);
        risultato.put("numeroCantieriTerminati", numeroCantieriTerminati);
        risultato.put("squadreImpiegate", squadreImpiegate);

        return ResponseEntity.ok(risultato);
    }

    // Statistiche per singolo cantiere (fatturato e saldo di un cantiere specifico)
    @GetMapping("/cantiere/{idCantiere}")
    public ResponseEntity<?> getStatisticheCantiere(@PathVariable Long idCantiere) {

        List<DocumentoContabile> docCantiere = documentoContabileRepository.findByCantiereId(idCantiere);

        double fatturatoCantiere = docCantiere.stream()
            .filter(d -> d instanceof Fattura)
            .mapToDouble(DocumentoContabile::getImporto)
            .sum();

        double saldoCantiere = docCantiere.stream()
            .filter(d -> d instanceof Fattura && ((Fattura) d).getStatoPagamento() == StatoFattura.DA_SALDARE)
            .mapToDouble(DocumentoContabile::getImporto)
            .sum();

        Map<String, Object> risultato = new HashMap<>();
        risultato.put("fatturatoCantiere", fatturatoCantiere);
        risultato.put("saldoCantiere", saldoCantiere);

        return ResponseEntity.ok(risultato);
    }
}