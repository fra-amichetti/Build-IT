package com.buildit.backend.gestioneCantieri;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.log.Logger;
import com.buildit.backend.repository.CantiereRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaCantieriControllerTest {

    @Mock private CantiereRepository cantiereRepository;
    @Mock private Logger             logger;

    private ListaCantieriController controller;

    private static final String EMAIL = "admin@buildit.it";

    @BeforeEach
    void setUp() {
        controller = new ListaCantieriController(cantiereRepository, logger);
    }

    // ── getElencoCantieri ─────────────────────────────────────────────────────

    @Test
    void getElencoCantieri_ritornaListaVuotaSeNessunCantiere() {
        when(cantiereRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> risposta = controller.getElencoCantieri();

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) risposta.getBody()).isEmpty();
    }

    @Test
    void getElencoCantieri_aggiornaStatoInRitardoEPersiste() {
        Cantiere scaduto = cantiereCon(StatoCantiere.IN_CORSO, LocalDate.now().minusDays(1));
        when(cantiereRepository.findAll()).thenReturn(List.of(scaduto));
        when(cantiereRepository.save(any())).thenReturn(scaduto);

        controller.getElencoCantieri();

        verify(cantiereRepository).save(scaduto);
        assertThat(scaduto.getStato()).isEqualTo(StatoCantiere.IN_RITARDO);
    }

    @Test
    void getElencoCantieri_nonPersisteSeCantierePianificato() {
        Cantiere pianificato = cantiereCon(StatoCantiere.PIANIFICATO, LocalDate.now().plusDays(30));
        when(cantiereRepository.findAll()).thenReturn(List.of(pianificato));

        controller.getElencoCantieri();

        verify(cantiereRepository, never()).save(any());
    }

    // ── aggiungiCantiere ──────────────────────────────────────────────────────

    @Test
    void aggiungiCantiere_ok_conDatiValidi() {
        Cantiere salvato = cantiereCon(StatoCantiere.PIANIFICATO, LocalDate.now().plusMonths(6));
        when(cantiereRepository.save(any())).thenReturn(salvato);

        ResponseEntity<?> risposta = controller.aggiungiCantiere(Map.of(
                "nome", "Nuovo Palazzo",
                "indirizzo", "Via Test 1",
                "dataInizioPrevista", "2026-07-01",
                "dataFinePrevista", "2026-12-31",
                "emailCliente", "cliente@test.it"
        ), EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cantiereRepository).save(any());
    }

    @Test
    void aggiungiCantiere_400_seNomeMancante() {
        ResponseEntity<?> risposta = controller.aggiungiCantiere(Map.of(
                "nome", "",
                "indirizzo", "Via Test 1",
                "dataInizioPrevista", "2026-07-01",
                "dataFinePrevista", "2026-12-31"
        ), EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("nome");
        verify(cantiereRepository, never()).save(any());
    }

    @Test
    void aggiungiCantiere_400_seIndirizzoMancante() {
        ResponseEntity<?> risposta = controller.aggiungiCantiere(Map.of(
                "nome", "Palazzo",
                "indirizzo", "",
                "dataInizioPrevista", "2026-07-01",
                "dataFinePrevista", "2026-12-31"
        ), EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("indirizzo");
    }

    @Test
    void aggiungiCantiere_400_seDataFineAntecedente() {
        ResponseEntity<?> risposta = controller.aggiungiCantiere(Map.of(
                "nome", "Palazzo",
                "indirizzo", "Via Test 1",
                "dataInizioPrevista", "2026-12-31",
                "dataFinePrevista", "2026-01-01"
        ), EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("fine");
    }

    @Test
    void aggiungiCantiere_400_seDataFineUgualeAInizio() {
        ResponseEntity<?> risposta = controller.aggiungiCantiere(Map.of(
                "nome", "Palazzo",
                "indirizzo", "Via Test 1",
                "dataInizioPrevista", "2026-07-01",
                "dataFinePrevista", "2026-07-01"
        ), EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── controllaScadenzaCantieri (job schedulato) ────────────────────────────

    @Test
    void controllaScadenzaCantieri_aggiornaCantieriInRitardo() {
        Cantiere inCorso = cantiereCon(StatoCantiere.IN_CORSO, LocalDate.now().minusDays(5));
        when(cantiereRepository.findByStato(StatoCantiere.IN_CORSO)).thenReturn(List.of(inCorso));
        when(cantiereRepository.save(any())).thenReturn(inCorso);

        controller.controllaScadenzaCantieri();

        verify(cantiereRepository).save(inCorso);
        assertThat(inCorso.getStato()).isEqualTo(StatoCantiere.IN_RITARDO);
    }

    @Test
    void controllaScadenzaCantieri_nonModificaCantieriNonScaduti() {
        Cantiere inCorso = cantiereCon(StatoCantiere.IN_CORSO, LocalDate.now().plusDays(30));
        when(cantiereRepository.findByStato(StatoCantiere.IN_CORSO)).thenReturn(List.of(inCorso));

        controller.controllaScadenzaCantieri();

        verify(cantiereRepository, never()).save(any());
    }

    // ── utility ───────────────────────────────────────────────────────────────

    private static Cantiere cantiereCon(StatoCantiere stato, LocalDate dataFinePrevista) {
        Cantiere c = new Cantiere();
        c.setNome("Cantiere Test");
        c.setIndirizzo("Via Test 1");
        c.setDataInizioPrevista(LocalDate.of(2026, 1, 1));
        c.setDataFinePrevista(dataFinePrevista);
        c.setStato(stato);
        return c;
    }

    @SuppressWarnings("unchecked")
    private static String errore(ResponseEntity<?> r) {
        return ((Map<String, String>) r.getBody()).get("errore");
    }
}
