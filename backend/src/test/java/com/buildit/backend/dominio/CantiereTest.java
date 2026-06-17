package com.buildit.backend.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CantiereTest {

    private Cantiere cantiere;

    @BeforeEach
    void setUp() {
        cantiere = new Cantiere();
        cantiere.setNome("Ristrutturazione Villa");
        cantiere.setIndirizzo("Via Roma 10, Milano");
        cantiere.setDataInizioPrevista(LocalDate.of(2026, 1, 1));
        cantiere.setDataFinePrevista(LocalDate.of(2026, 12, 31));
        cantiere.setEmailCliente("cliente@email.it");
        cantiere.setStato(StatoCantiere.PIANIFICATO);
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    @Test
    void getterSetter_campiBase() {
        assertEquals("Ristrutturazione Villa", cantiere.getNome());
        assertEquals("Via Roma 10, Milano", cantiere.getIndirizzo());
        assertEquals("cliente@email.it", cantiere.getEmailCliente());
        assertEquals(StatoCantiere.PIANIFICATO, cantiere.getStato());
        assertEquals(LocalDate.of(2026, 1, 1), cantiere.getDataInizioPrevista());
        assertEquals(LocalDate.of(2026, 12, 31), cantiere.getDataFinePrevista());
        assertNull(cantiere.getDataInizioEffettiva());
        assertNull(cantiere.getDataFineEffettiva());
    }

    @Test
    void setNome_aggiornaNome() {
        cantiere.setNome("Villa Aggiornata - Lotto 2");
        assertEquals("Villa Aggiornata - Lotto 2", cantiere.getNome());
    }

    // ── iniziaLavori ─────────────────────────────────────────────────────────

    @Test
    void iniziaLavori_cambiaStatoInIN_CORSO() {
        cantiere.iniziaLavori();
        assertEquals(StatoCantiere.IN_CORSO, cantiere.getStato());
    }

    @Test
    void iniziaLavori_impostaDataInizioEffettivaAdOggi() {
        cantiere.iniziaLavori();
        assertEquals(LocalDate.now(), cantiere.getDataInizioEffettiva());
    }

    @Test
    void iniziaLavori_nonImpostaDataFineEffettiva() {
        cantiere.iniziaLavori();
        assertNull(cantiere.getDataFineEffettiva());
    }

    // ── terminaCantiere ──────────────────────────────────────────────────────

    @Test
    void terminaCantiere_cambiaStatoInTERMINATO() {
        cantiere.iniziaLavori();
        cantiere.terminaCantiere();
        assertEquals(StatoCantiere.TERMINATO, cantiere.getStato());
    }

    @Test
    void terminaCantiere_impostaDataFineEffettivaAdOggi() {
        cantiere.iniziaLavori();
        cantiere.terminaCantiere();
        assertEquals(LocalDate.now(), cantiere.getDataFineEffettiva());
    }

    @Test
    void terminaCantiere_preservaDataInizioEffettiva() {
        cantiere.iniziaLavori();
        LocalDate inizioEffettivo = cantiere.getDataInizioEffettiva();
        cantiere.terminaCantiere();
        assertEquals(inizioEffettivo, cantiere.getDataInizioEffettiva());
    }

    // ── verificaRitardo ──────────────────────────────────────────────────────

    @Test
    void verificaRitardo_ritornaFalse_seStatoPIANIFICATO() {
        assertFalse(cantiere.verificaRitardo());
    }

    @Test
    void verificaRitardo_ritornaFalse_seStatoTERMINATO() {
        cantiere.iniziaLavori();
        cantiere.terminaCantiere();
        assertFalse(cantiere.verificaRitardo());
    }

    @Test
    void verificaRitardo_ritornaFalse_seDataFineNonSuperata() {
        cantiere.iniziaLavori();
        cantiere.setDataFinePrevista(LocalDate.now().plusDays(30));
        assertFalse(cantiere.verificaRitardo());
    }

    @Test
    void verificaRitardo_ritornaFalse_seDataFineUgualeAdOggi() {
        cantiere.iniziaLavori();
        cantiere.setDataFinePrevista(LocalDate.now());
        // isAfter(oggi) = false → non ancora in ritardo
        assertFalse(cantiere.verificaRitardo());
    }

    @Test
    void verificaRitardo_ritornaTrueSeDataFineSuperataEStatoIN_CORSO() {
        cantiere.iniziaLavori();
        cantiere.setDataFinePrevista(LocalDate.now().minusDays(1));
        assertTrue(cantiere.verificaRitardo());
    }
}
