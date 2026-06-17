package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.Dipendente;
import com.buildit.backend.dominio.Utente;
import com.buildit.backend.log.EsitoOperazione;
import com.buildit.backend.log.Logger;
import com.buildit.backend.log.TipoOperazione;
import com.buildit.backend.repository.UtenteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dipendenti")
@CrossOrigin(origins = "http://localhost:5173")
public class DipendentiController {

    private final UtenteRepository  utenteRepository;
    private final PasswordEncoder   passwordEncoder;
    private final Logger            logger;

    public DipendentiController(UtenteRepository utenteRepository,
                                 PasswordEncoder passwordEncoder,
                                 Logger logger) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder  = passwordEncoder;
        this.logger           = logger;
    }

    @PostMapping
    public ResponseEntity<?> aggiungiDipendente(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Email", required = false, defaultValue = "SCONOSCIUTO") String adminEmail) {

        String email = body.get("email");

        if (utenteRepository.existsByEmail(email)) {
            return ResponseEntity.status(400).body(Map.of("errore", "Email già registrata"));
        }

        Dipendente dipendente = new Dipendente();
        dipendente.setNome(body.get("nome"));
        dipendente.setCognome(body.get("cognome"));
        dipendente.setEmail(email);
        dipendente.setHashPassword(passwordEncoder.encode(body.get("password")));
        dipendente.setIncarico(body.get("incarico"));
        utenteRepository.save(dipendente);

        logger.log(adminEmail, TipoOperazione.CREA_DIPENDENTE,
            "Dipendente aggiunto: " + dipendente.getNome() + " " + dipendente.getCognome() +
            " (" + email + ") — incarico: " + dipendente.getIncarico(),
            EsitoOperazione.SUCCESSO);
        return ResponseEntity.ok(Map.of("messaggio", "Dipendente aggiunto"));
    }

    @GetMapping
    public ResponseEntity<?> getDipendenti() {
        List<Utente> tutti = utenteRepository.findAll();
        List<Map<String, Object>> dipendenti = tutti.stream()
            .filter(u -> u instanceof Dipendente)
            .map(u -> {
                Dipendente d = (Dipendente) u;
                return Map.<String, Object>of(
                    "id",       d.getId(),
                    "nome",     d.getNome(),
                    "cognome",  d.getCognome(),
                    "email",    d.getEmail(),
                    "incarico", d.getIncarico() != null ? d.getIncarico() : ""
                );
            })
            .toList();
        return ResponseEntity.ok(dipendenti);
    }
}
