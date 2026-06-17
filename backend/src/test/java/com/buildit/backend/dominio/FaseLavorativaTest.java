package com.buildit.backend.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FaseLavorativaTest {

    private FaseLavorativa fase;

    @BeforeEach
    void setUp() {
        fase = new FaseLavorativa();
        fase.setNome("Demolizione Pareti");
        fase.setDescrizione("Rimozione tramezzi interni");
        fase.setDataInizioPrevista(LocalDate.of(2026, 9, 1));
        fase.setDataFinePrevista(LocalDate.of(2026, 9, 30));
        fase.setStato(StatoFase.PIANIFICATA);
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    @Test
    void getterSetter_campiBase() {
        assertEquals("Demolizione Pareti", fase.getNome());
        assertEquals("Rimozione tramezzi interni", fase.getDescrizione());
        assertEquals(StatoFase.PIANIFICATA, fase.getStato());
        assertEquals(LocalDate.of(2026, 9, 1), fase.getDataInizioPrevista());
        assertEquals(LocalDate.of(2026, 9, 30), fase.getDataFinePrevista());
        assertNull(fase.getDataInizioEffettiva());
        assertNull(fase.getDataFineEffettiva());
    }

    @Test
    void setCantiere_associaERestituisceCantiere() {
        Cantiere cantiere = new Cantiere();
        cantiere.setNome("Palazzo Test");
        fase.setCantiere(cantiere);
        assertEquals(cantiere, fase.getCantiere());
    }

    @Test
    void setSquadra_associaERestituisceSquadra() {
        Squadra squadra = new Squadra();
        squadra.setNome("Squadra Muratori");
        fase.setSquadra(squadra);
        assertEquals(squadra, fase.getSquadra());
    }

    // ── avviaFase ────────────────────────────────────────────────────────────

    @Test
    void avviaFase_cambiaStatoInIN_CORSO() {
        fase.avviaFase();
        assertEquals(StatoFase.IN_CORSO, fase.getStato());
    }

    @Test
    void avviaFase_impostaDataInizioEffettivaAdOggi() {
        fase.avviaFase();
        assertEquals(LocalDate.now(), fase.getDataInizioEffettiva());
    }

    @Test
    void avviaFase_nonImpostaDataFineEffettiva() {
        fase.avviaFase();
        assertNull(fase.getDataFineEffettiva());
    }

    // ── terminaFase ──────────────────────────────────────────────────────────

    @Test
    void terminaFase_cambiaStatoInTERMINATA() {
        fase.avviaFase();
        fase.terminaFase();
        assertEquals(StatoFase.TERMINATA, fase.getStato());
    }

    @Test
    void terminaFase_impostaDataFineEffettivaAdOggi() {
        fase.avviaFase();
        fase.terminaFase();
        assertEquals(LocalDate.now(), fase.getDataFineEffettiva());
    }

    @Test
    void terminaFase_preservaDataInizioEffettiva() {
        fase.avviaFase();
        LocalDate inizioEffettivo = fase.getDataInizioEffettiva();
        fase.terminaFase();
        assertEquals(inizioEffettivo, fase.getDataInizioEffettiva());
    }

    @Test
    void transizione_completaPIANIFICATA_IN_CORSO_TERMINATA() {
        assertEquals(StatoFase.PIANIFICATA, fase.getStato());
        fase.avviaFase();
        assertEquals(StatoFase.IN_CORSO, fase.getStato());
        fase.terminaFase();
        assertEquals(StatoFase.TERMINATA, fase.getStato());
    }
}
