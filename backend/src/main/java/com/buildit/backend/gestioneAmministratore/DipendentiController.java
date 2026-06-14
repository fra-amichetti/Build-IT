package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.Dipendente;
import com.buildit.backend.dominio.Utente;
import com.buildit.backend.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dipendenti")
@CrossOrigin(origins = "http://localhost:5173")
public class DipendentiController {

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> aggiungiDipendente(@RequestBody Map<String, String> body) {
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
                "id", d.getId(),
                "nome", d.getNome(),
                "cognome", d.getCognome(),
                "email", d.getEmail(),
                "incarico", d.getIncarico() != null ? d.getIncarico() : ""
            );
        })
        .toList();
    return ResponseEntity.ok(dipendenti);
}
}