package com.buildit.backend.dominio;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDocumento {

    private Cantiere cantiere;

    @BeforeEach
    public void setUp() {
        cantiere = new Cantiere();
        cantiere.setNome("Cantiere Test");
    }

    @Test
    public void testDocumentoTecnico() {
        DocumentoTecnico doc = new DocumentoTecnico();
        doc.setNome("Pianta Piano Terra");
        doc.setData(LocalDate.now());
        doc.setFileUrl("/documents/pianta.pdf");
        doc.setCantiere(cantiere);
        doc.setTipologia("pianta");

        assertEquals("Pianta Piano Terra", doc.getNome());
        assertEquals("pianta", doc.getTipologia());
        assertEquals(cantiere, doc.getCantiere());
        assertNull(doc.getFase());
    }

    @Test
    public void testPreventivoNonContribuisceStatisticheLogica() {
        // Verifica solo che il Preventivo si comporti come DocumentoContabile
        Preventivo preventivo = new Preventivo();
        preventivo.setNome("Preventivo Iniziale");
        preventivo.setImporto(50000.00);
        preventivo.setCantiere(cantiere);

        assertEquals(50000.00, preventivo.getImporto(), 0.01);
        assertTrue(preventivo instanceof DocumentoContabile);
        assertNotEquals(Fattura.class, preventivo.getClass());
    }
}