package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.Dipendente;
import com.buildit.backend.repository.UtenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DipendentiControllerTest {

    @Mock private UtenteRepository utenteRepository;
    @Mock private PasswordEncoder  passwordEncoder;

    @InjectMocks
    private DipendentiController controller;

    // ── aggiungiDipendente ────────────────────────────────────────────────────

    @Test
    void aggiungiDipendente_ok_conEmailNuova() {
        when(utenteRepository.existsByEmail("paolo@buildit.it")).thenReturn(false);
        when(utenteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        ResponseEntity<?> risposta = controller.aggiungiDipendente(Map.of(
                "nome", "Paolo", "cognome", "Verdi",
                "email", "paolo@buildit.it", "password", "Password1!",
                "incarico", "Elettricista"
        ));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(utenteRepository).save(any());
    }

    @Test
    void aggiungiDipendente_400_seEmailGiaRegistrata() {
        when(utenteRepository.existsByEmail("dup@buildit.it")).thenReturn(true);

        ResponseEntity<?> risposta = controller.aggiungiDipendente(Map.of(
                "nome", "Marco", "cognome", "Rossi",
                "email", "dup@buildit.it", "password", "Password2!",
                "incarico", "Idraulico"
        ));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).contains("registrata");
        verify(utenteRepository, never()).save(any());
    }

    // ── getDipendenti ─────────────────────────────────────────────────────────

    @Test
    void getDipendenti_ritornaListaVuotaSeNessunUtente() {
        when(utenteRepository.findAll()).thenReturn(List.of());

        ResponseEntity<?> risposta = controller.getDipendenti();

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) risposta.getBody()).isEmpty();
    }

    @Test
    void getDipendenti_filtraSoloDipendenti() {
        Dipendente dip = new Dipendente();
        dip.setId(1L);
        dip.setNome("Paolo");
        dip.setCognome("Verdi");
        dip.setEmail("paolo@buildit.it");
        dip.setIncarico("Muratore");

        com.buildit.backend.dominio.Amministratore admin =
                new com.buildit.backend.dominio.Amministratore();
        admin.setId(2L);
        admin.setNome("Admin");
        admin.setCognome("Boss");
        admin.setEmail("admin@buildit.it");

        when(utenteRepository.findAll()).thenReturn(List.of(dip, admin));

        ResponseEntity<?> risposta = controller.getDipendenti();

        List<?> body = (List<?>) risposta.getBody();
        assertThat(body).hasSize(1);
        Map<?,?> dipMap = (Map<?,?>) body.get(0);
        assertThat(dipMap.get("email")).isEqualTo("paolo@buildit.it");
    }

    @Test
    void getDipendenti_includeIncaricoVuotoSeNull() {
        Dipendente dip = new Dipendente();
        dip.setId(1L);
        dip.setNome("Luca");
        dip.setCognome("Neri");
        dip.setEmail("luca@buildit.it");
        dip.setIncarico(null);

        when(utenteRepository.findAll()).thenReturn(List.of(dip));

        ResponseEntity<?> risposta = controller.getDipendenti();

        List<?> body = (List<?>) risposta.getBody();
        Map<?,?> dipMap = (Map<?,?>) body.get(0);
        assertThat(dipMap.get("incarico")).isEqualTo("");
    }

    // ── utility ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static String errore(ResponseEntity<?> r) {
        return ((Map<String, String>) r.getBody()).get("errore");
    }
}
