package com.buildit.backend.dominio;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestCantiere {

    private Cantiere cantiere;

    @BeforeEach
    public void setUp() {
        cantiere = new Cantiere();
        cantiere.setNome("Ristrutturazione Villa");
        cantiere.setIndirizzo("Via Roma 10");
        cantiere.setDataInizioPrevista(LocalDate.now().minusDays(1));
        cantiere.setDataFinePrevista(LocalDate.now().minusDays(1).plusMonths(1));
        cantiere.setEmailCliente("cliente@email.it");
        cantiere.setStato(StatoCantiere.PIANIFICATO);
    }

    @Test
    public void testGetterSetter() {
        assertEquals("Ristrutturazione Villa", cantiere.getNome());
        assertEquals("PIANIFICATO", cantiere.getStato());

        cantiere.setNome("Ristrutturazione Villa - Lotto 2");
        assertEquals("Ristrutturazione Villa - Lotto 2", cantiere.getNome());
    }

    @Test
    public void testIniziaLavori() {
        assertEquals("PIANIFICATO", cantiere.getStato());

        cantiere.iniziaLavori();

        assertEquals("IN_CORSO", cantiere.getStato());
        assertEquals(LocalDate.now(), cantiere.getDataInizioEffettiva());
    }

    @Test
    public void testTerminaCantiere() {
        cantiere.iniziaLavori();
        assertEquals("IN_CORSO", cantiere.getStato());

        cantiere.terminaCantiere();

        assertEquals("TERMINATO", cantiere.getStato());
        assertEquals(LocalDate.now(), cantiere.getDataFineEffettiva());
    }

    @Test
    public void testVerificaRitardo() {
        // dataFinePrevista è già passata (ieri + 1 mese fa, quindi nel passato se oggi è dopo)
        cantiere.setDataFinePrevista(LocalDate.now().minusDays(1));
        cantiere.iniziaLavori();

        assertTrue(cantiere.verificaRitardo());
    }

    @Test
    public void testNonInRitardoSeFuturo() {
        cantiere.setDataFinePrevista(LocalDate.now().plusMonths(1));
        cantiere.iniziaLavori();

        assertFalse(cantiere.verificaRitardo());
    }
}