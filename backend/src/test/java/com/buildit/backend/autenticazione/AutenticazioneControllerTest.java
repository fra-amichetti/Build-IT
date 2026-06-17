package com.buildit.backend.autenticazione;

import com.buildit.backend.dominio.Amministratore;
import com.buildit.backend.dominio.Utente;
import com.buildit.backend.repository.UtenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticazioneControllerTest {

    @Mock private UtenteRepository utenteRepository;
    @Mock private PasswordEncoder  passwordEncoder;

    @InjectMocks
    private AutenticazioneController controller;

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_ok_ritornaCredenziali() {
        Amministratore admin = amministratore("admin@buildit.it", "hash");
        when(utenteRepository.findByEmail("admin@buildit.it")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("Password1!", "hash")).thenReturn(true);

        ResponseEntity<?> risposta = controller.login(body("email", "admin@buildit.it",
                                                           "password", "Password1!"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?,?> body = (Map<?,?>) risposta.getBody();
        assertThat(body.get("email")).isEqualTo("admin@buildit.it");
        assertThat(body.get("ruolo")).isEqualTo("AMMINISTRATORE");
    }

    @Test
    void login_401_seUtenteNonTrovato() {
        when(utenteRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> risposta = controller.login(body("email", "nessuno@test.it",
                                                           "password", "qualsiasi"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(errore(risposta)).contains("registrato");
    }

    @Test
    void login_401_sePasswordErrata() {
        Amministratore admin = amministratore("admin@buildit.it", "hash");
        when(utenteRepository.findByEmail("admin@buildit.it")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> risposta = controller.login(body("email", "admin@buildit.it",
                                                           "password", "Sbagliata1!"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(errore(risposta)).contains("Password");
    }

    // ── register: validazione regex password ──────────────────────────────────

    @Test
    void register_ok_conPasswordValida() {
        when(utenteRepository.existsByEmail(anyString())).thenReturn(false);
        when(utenteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> risposta = controller.register(body(
                "nome", "Marco", "cognome", "Rossi",
                "email", "marco@test.it", "password", "Password1!"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest(name = "password non valida: \"{0}\"")
    @ValueSource(strings = {
            "short1!",          // troppo corta (< 8 char)
            "password1!",       // manca maiuscola
            "PASSWORD1!",       // manca minuscola
            "Password!!",       // manca cifra
            "Password1",        // manca carattere speciale
            "Password1#",       // carattere speciale non ammesso (#)
            ""                  // vuota
    })
    void register_400_sePasswordNonRispettaRegex(String pwd) {
        ResponseEntity<?> risposta = controller.register(body(
                "nome", "Test", "cognome", "User",
                "email", "test@test.it", "password", pwd));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).contains("password");
        verify(utenteRepository, never()).save(any());
    }

    @ParameterizedTest(name = "carattere speciale ammesso: \"{0}\"")
    @ValueSource(strings = {"Password1!", "Password1?", "Password1.", "Password1@", "Password1/"})
    void register_ok_tuttiCaratteriSpecialiAmmessi(String pwd) {
        when(utenteRepository.existsByEmail(anyString())).thenReturn(false);
        when(utenteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> risposta = controller.register(body(
                "nome", "Test", "cognome", "User",
                "email", "test@test.it", "password", pwd));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void register_400_seEmailGiaRegistrata() {
        when(utenteRepository.existsByEmail("dup@test.it")).thenReturn(true);

        ResponseEntity<?> risposta = controller.register(body(
                "nome", "Dup", "cognome", "User",
                "email", "dup@test.it", "password", "Password1!"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).contains("registrata");
        verify(utenteRepository, never()).save(any());
    }

    // ── utility ───────────────────────────────────────────────────────────────

    private static Amministratore amministratore(String email, String hash) {
        Amministratore a = new Amministratore();
        a.setId(1L);
        a.setNome("Admin");
        a.setCognome("Test");
        a.setEmail(email);
        a.setHashPassword(hash);
        return a;
    }

    private static Map<String, String> body(String... kvPairs) {
        Map<String, String> map = new java.util.HashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            map.put(kvPairs[i], kvPairs[i + 1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static String errore(ResponseEntity<?> r) {
        return ((Map<String, String>) r.getBody()).get("errore");
    }
}
