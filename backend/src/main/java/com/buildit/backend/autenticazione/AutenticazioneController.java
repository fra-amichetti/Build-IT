package com.buildit.backend.autenticazione;

import com.buildit.backend.dominio.Utente;
import com.buildit.backend.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AutenticazioneController {

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Optional<Utente> utente = utenteRepository.findByEmail(email);

        if (utente.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("errore", "Utente non registrato"));
        }

        if (!passwordEncoder.matches(password, utente.get().getHashPassword())) {
            return ResponseEntity.status(401).body(Map.of("errore", "Password non corretta"));
        }

        return ResponseEntity.ok(Map.of(
            "id", utente.get().getId(),
            "nome", utente.get().getNome(),
            "cognome", utente.get().getCognome(),
            "email", utente.get().getEmail(),
            "ruolo", utente.get().getRuolo()
        ));
    }
}