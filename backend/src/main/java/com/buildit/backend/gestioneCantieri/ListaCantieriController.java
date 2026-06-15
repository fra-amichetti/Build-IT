package com.buildit.backend.gestioneCantieri;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.repository.CantiereRepository;

import com.buildit.backend.dominio.StatoCantiere;

@RestController
@RequestMapping("/api/cantieri")
@CrossOrigin(origins = "http://localhost:5173")
public class ListaCantieriController {

    @Autowired
    private CantiereRepository cantiereRepository;

    // GET /api/cantieri — tutti i cantieri (admin/dipendente)
    @GetMapping
    public ResponseEntity<?> getElencoCantieri() {
        List<Cantiere> cantieri = cantiereRepository.findAll();
        return ResponseEntity.ok(cantieri);
    }

    // GET /api/cantieri?email=... — cantieri filtrati per cliente
    @GetMapping(params = "email")
    public ResponseEntity<?> getElencoCantieriByCliente(@RequestParam String email) {
        List<Cantiere> cantieri = cantiereRepository.findByEmailCliente(email);
        return ResponseEntity.ok(cantieri);
    }

    // POST /api/cantieri — crea nuovo cantiere
    @PostMapping
    public ResponseEntity<?> aggiungiCantiere(@RequestBody Map<String, String> body) {
        String nome = body.get("nome");
        String indirizzo = body.get("indirizzo");
        String dataInizioPrevistaStr = body.get("dataInizioPrevista");
        String dataFinePrevistaStr = body.get("dataFinePrevista");
        String emailCliente = body.get("emailCliente");

        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Il nome è obbligatorio"));
        }
        if (indirizzo == null || indirizzo.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "L'indirizzo è obbligatorio"));
        }
        if (dataInizioPrevistaStr == null || dataFinePrevistaStr == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "Le date sono obbligatorie"));
        }

        LocalDate dataInizio = LocalDate.parse(dataInizioPrevistaStr);
        LocalDate dataFine = LocalDate.parse(dataFinePrevistaStr);

        if (!dataFine.isAfter(dataInizio)) {
            return ResponseEntity.badRequest()
                .body(Map.of("errore", "La data di fine deve essere successiva alla data di inizio"));
        }

        Cantiere cantiere = new Cantiere();
        cantiere.setNome(nome);
        cantiere.setIndirizzo(indirizzo);
        cantiere.setDataInizioPrevista(dataInizio);
        cantiere.setDataFinePrevista(dataFine);
        cantiere.setEmailCliente(emailCliente);

        cantiere.setStato(StatoCantiere.PIANIFICATO);
        

        Cantiere salvato = cantiereRepository.save(cantiere);
        return ResponseEntity.ok(salvato);
    }

    // Controllo scadenze automatico ogni giorno a mezzanotte
    @Scheduled(cron = "0 0 0 * * *")
    public void controllaScadenzaCantieri() {
        List<Cantiere> inCorso = cantiereRepository.findByStato("IN_CORSO");
        //List<Cantiere> inCorso = cantiereRepository.findByStato(StatoCantiere.IN_CORSO);
        for (Cantiere c : inCorso) {
            if (c.verificaRitardo()) {
                c.setStato(StatoCantiere.IN_RITARDO);
               
                cantiereRepository.save(c);
            }
        }
        System.out.println("Controllo scadenze completato: " + LocalDate.now());
    }
}