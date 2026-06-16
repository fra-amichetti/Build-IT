package com.buildit.backend.autenticazione;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildit.backend.dominio.Cliente;
import com.buildit.backend.dominio.Utente;
import com.buildit.backend.repository.UtenteRepository;

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
    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    String password = body.get("password");
    String nome = body.get("nome");
    String cognome = body.get("cognome");

    String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!?.@/]).{8,}$";
        
        if (password == null || !password.matches(passwordRegex)) {
            return ResponseEntity.status(400).body(Map.of("errore", 
                "La password deve essere di almeno 8 caratteri, contenere una maiuscola, una minuscola, un numero e un carattere speciale tra !?.@/"));
        }

    if (utenteRepository.existsByEmail(email)) {
        return ResponseEntity.status(400).body(Map.of("errore", "Email già registrata"));
    }

    Cliente cliente = new Cliente();
    cliente.setNome(nome);
    cliente.setCognome(cognome);
    cliente.setEmail(email);
    cliente.setHashPassword(passwordEncoder.encode(password));
    utenteRepository.save(cliente);

    return ResponseEntity.ok(Map.of("messaggio", "Registrazione completata"));
}
}