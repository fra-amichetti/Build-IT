package com.buildit.backend.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SquadraTest {

    private Squadra squadra;

    @BeforeEach
    void setUp() {
        squadra = new Squadra();
        squadra.setNome("Squadra Muratori");
        squadra.setSpecializzazione(Specializzazione.MURATORI);
        squadra.setNumeroComponenti(5);
        squadra.setNomeReferente("Giovanni Bianchi");
        squadra.setFasiDelCantiere(new ArrayList<>());
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    @Test
    void getterSetter_campiBase() {
        assertEquals("Squadra Muratori", squadra.getNome());
        assertEquals(Specializzazione.MURATORI, squadra.getSpecializzazione());
        assertEquals(5, squadra.getNumeroComponenti());
        assertEquals("Giovanni Bianchi", squadra.getNomeReferente());
    }

    // ── isDisponibile ─────────────────────────────────────────────────────────

    @Test
    void isDisponibile_ritornaTrue_seListaFasiNull() {
        squadra.setFasiDelCantiere(null);
        assertTrue(squadra.isDisponibile(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
    }

    @Test
    void isDisponibile_ritornaTrue_seListaFasiVuota() {
        assertTrue(squadra.isDisponibile(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
    }

    @Test
    void isDisponibile_ritornaTrue_seFaseEsistenteETerminata() {
        // Le fasi TERMINATA non bloccano la disponibilità
        squadra.setFasiDelCantiere(List.of(
                faseCon(StatoFase.TERMINATA, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))));
        assertTrue(squadra.isDisponibile(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
    }

    @Test
    void isDisponibile_ritornaFalse_seOverlapParziale_SinistraConIN_CORSO() {
        // Fase: 10-20 set; Richiesta: 1-15 set → overlap
        squadra.setFasiDelCantiere(List.of(
                faseCon(StatoFase.IN_CORSO, LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 20))));
        assertFalse(squadra.isDisponibile(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15)));
    }

    @Test
    void isDisponibile_ritornaFalse_seOverlapParziale_DestraConPIANIFICATA() {
        // Fase: 1-10 set; Richiesta: 5-20 set → overlap
        squadra.setFasiDelCantiere(List.of(
                faseCon(StatoFase.PIANIFICATA, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10))));
        assertFalse(squadra.isDisponibile(
                LocalDate.of(2026, 9, 5), LocalDate.of(2026, 9, 20)));
    }

    @Test
    void isDisponibile_ritornaFalse_seFaseInternaAlPeriodoRichiesto() {
        // Fase: 5-15 set; Richiesta: 1-30 set → overlap totale
        squadra.setFasiDelCantiere(List.of(
                faseCon(StatoFase.PIANIFICATA, LocalDate.of(2026, 9, 5), LocalDate.of(2026, 9, 15))));
        assertFalse(squadra.isDisponibile(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
    }

    @Test
    void isDisponibile_ritornaTrue_sePeriodoSuccessivo_SenzaOverlap() {
        // Fase: 1-10 set; Richiesta: 11-20 set → nessun overlap
        squadra.setFasiDelCantiere(List.of(
                faseCon(StatoFase.PIANIFICATA, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10))));
        assertTrue(squadra.isDisponibile(
                LocalDate.of(2026, 9, 11), LocalDate.of(2026, 9, 20)));
    }

    @Test
    void isDisponibile_ritornaTrue_sePeriodoPrecedente_SenzaOverlap() {
        // Fase: 20-30 set; Richiesta: 1-10 set → nessun overlap
        squadra.setFasiDelCantiere(List.of(
                faseCon(StatoFase.IN_CORSO, LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 30))));
        assertTrue(squadra.isDisponibile(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10)));
    }

    // ── utility ──────────────────────────────────────────────────────────────

    private static FaseLavorativa faseCon(StatoFase stato, LocalDate inizio, LocalDate fine) {
        FaseLavorativa f = new FaseLavorativa();
        f.setNome("Fase test");
        f.setDataInizioPrevista(inizio);
        f.setDataFinePrevista(fine);
        f.setStato(stato);
        return f;
    }
}
