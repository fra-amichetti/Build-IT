package com.buildit.backend.autenticazione;

import com.buildit.backend.dominio.Cliente;
import com.buildit.backend.dominio.Utente;
import com.buildit.backend.log.BruteForceProtectionService;
import com.buildit.backend.log.EsitoOperazione;
import com.buildit.backend.log.Logger;
import com.buildit.backend.log.TipoOperazione;
import com.buildit.backend.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AutenticazioneController {

    @Autowired private UtenteRepository         utenteRepository;
    @Autowired private PasswordEncoder          passwordEncoder;
    @Autowired private Logger                   logger;
    @Autowired private BruteForceProtectionService bruteForce;

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        // 1. Brute-force: verifica blocco prima di tutto
        if (bruteForce.isBloccato(email)) {
            long secondi = bruteForce.secondiRimanenti(email);
            logger.log(email, TipoOperazione.LOGIN_BLOCCATO,
                "Account temporaneamente bloccato — " + secondi + " secondi rimanenti " +
                "(3 tentativi falliti consecutivi)",
                EsitoOperazione.FALLITO);
            return ResponseEntity.status(429).body(Map.of(
                "errore", "Troppi tentativi falliti. Riprova tra " + secondi + " secondi."));
        }

        // 2. Utente non trovato
        Optional<Utente> opt = utenteRepository.findByEmail(email);
        if (opt.isEmpty()) {
            bruteForce.registraFallimento(email);
            logger.log(email, TipoOperazione.LOGIN_FALLITO,
                "Tentativo " + bruteForce.getNumeroTentativi(email) +
                "/3 — utente non trovato", EsitoOperazione.FALLITO);
            return ResponseEntity.status(401).body(Map.of("errore", "Utente non registrato"));
        }

        // 3. Password errata
        if (!passwordEncoder.matches(password, opt.get().getHashPassword())) {
            bruteForce.registraFallimento(email);
            logger.log(email, TipoOperazione.LOGIN_FALLITO,
                "Tentativo " + bruteForce.getNumeroTentativi(email) +
                "/3 — password errata", EsitoOperazione.FALLITO);
            return ResponseEntity.status(401).body(Map.of("errore", "Password non corretta"));
        }

        // 4. Login riuscito
        bruteForce.reset(email);
        Utente utente = opt.get();
        logger.log(email, TipoOperazione.LOGIN_OK,
            "Login effettuato con successo (ruolo: " + utente.getRuolo() + ")",
            EsitoOperazione.SUCCESSO);

        return ResponseEntity.ok(Map.of(
            "id",      utente.getId(),
            "nome",    utente.getNome(),
            "cognome", utente.getCognome(),
            "email",   utente.getEmail(),
            "ruolo",   utente.getRuolo()
        ));
    }

    // ── POST /api/auth/register ───────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");
        String nome     = body.get("nome");
        String cognome  = body.get("cognome");

        // Regex: ≥8 char, maiuscola, minuscola, cifra, uno dei simboli !?.@/
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!?.@/]).{8,}$";
        if (password == null || !password.matches(passwordRegex)) {
            return ResponseEntity.status(400).body(Map.of("errore",
                "La password deve essere di almeno 8 caratteri e contenere " +
                "una maiuscola, una minuscola, un numero e un carattere speciale tra !?.@/"));
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

        logger.log(email, TipoOperazione.REGISTRAZIONE,
            "Nuovo cliente registrato: " + nome + " " + cognome,
            EsitoOperazione.SUCCESSO);

        return ResponseEntity.ok(Map.of("messaggio", "Registrazione completata"));
    }
}
