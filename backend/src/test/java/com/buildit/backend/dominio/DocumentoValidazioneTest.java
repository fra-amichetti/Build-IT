package com.buildit.backend.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa il metodo polimorfico validaEstensione() su tutta la gerarchia Documento.
 */
class DocumentoValidazioneTest {

    // ── DocumentoTecnico: formati ammessi (pdf, jpg, png) ────────────────────

    @ParameterizedTest(name = "DocumentoTecnico accetta \"{0}\"")
    @ValueSource(strings = {"relazione.pdf", "foto.jpg", "pianta.png",
                             "RELAZIONE.PDF", "FOTO.JPG", "PIANTA.PNG"})
    void documentoTecnico_validaEstensione_accettaPdfJpgPng(String nomeFile) {
        assertTrue(new DocumentoTecnico().validaEstensione(nomeFile));
    }

    @ParameterizedTest(name = "DocumentoTecnico rifiuta \"{0}\"")
    @ValueSource(strings = {"documento.docx", "foglio.xlsx", "archivio.zip",
                             "video.mp4", "file.txt", "immagine.gif"})
    void documentoTecnico_validaEstensione_rifiutaAltriFormati(String nomeFile) {
        assertFalse(new DocumentoTecnico().validaEstensione(nomeFile));
    }

    @Test
    void documentoTecnico_validaEstensione_ritornaFalse_seNomeNull() {
        assertFalse(new DocumentoTecnico().validaEstensione(null));
    }

    @Test
    void documentoTecnico_getterSetter_tipologia() {
        DocumentoTecnico doc = new DocumentoTecnico();
        doc.setTipologia("Pianta piano primo");
        assertEquals("Pianta piano primo", doc.getTipologia());
    }

    // ── Fattura: solo PDF ────────────────────────────────────────────────────

    @Test
    void fattura_validaEstensione_accettaSoloPdf() {
        Fattura f = new Fattura();
        assertTrue(f.validaEstensione("fattura.pdf"));
    }

    @ParameterizedTest(name = "Fattura rifiuta \"{0}\"")
    @ValueSource(strings = {"fattura.jpg", "fattura.png", "fattura.docx", "fattura.xlsx"})
    void fattura_validaEstensione_rifiutaNonPdf(String nomeFile) {
        assertFalse(new Fattura().validaEstensione(nomeFile));
    }

    @Test
    void fattura_validaEstensione_caseInsensitive() {
        Fattura f = new Fattura();
        assertTrue(f.validaEstensione("FATTURA.PDF"));
        assertTrue(f.validaEstensione("Fattura.Pdf"));
    }

    @Test
    void fattura_validaEstensione_ritornaFalse_seNomeNull() {
        assertFalse(new Fattura().validaEstensione(null));
    }

    @Test
    void fattura_getterSetter_importoEStatoPagamento() {
        Fattura f = new Fattura();
        f.setImporto(15000.00);
        f.setStatoPagamento(StatoFattura.DA_SALDARE);

        assertEquals(15000.00, f.getImporto(), 0.001);
        assertEquals(StatoFattura.DA_SALDARE, f.getStatoPagamento());

        f.setStatoPagamento(StatoFattura.SALDATO);
        assertEquals(StatoFattura.SALDATO, f.getStatoPagamento());
    }

    // ── Preventivo: solo PDF (ereditato da DocumentoContabile) ───────────────

    @Test
    void preventivo_validaEstensione_accettaSoloPdf() {
        assertTrue(new Preventivo().validaEstensione("preventivo.pdf"));
    }

    @ParameterizedTest(name = "Preventivo rifiuta \"{0}\"")
    @ValueSource(strings = {"preventivo.jpg", "preventivo.xlsx", "preventivo.docx"})
    void preventivo_validaEstensione_rifiutaNonPdf(String nomeFile) {
        assertFalse(new Preventivo().validaEstensione(nomeFile));
    }

    @Test
    void preventivo_validaEstensione_ritornaFalse_seNomeNull() {
        assertFalse(new Preventivo().validaEstensione(null));
    }

    // ── Polimorfismo ──────────────────────────────────────────────────────────

    @Test
    void polimorfismo_jpgAccettatoDaTecnico_RifiutatoDaContabile() {
        Documento tecnico = new DocumentoTecnico();
        Documento fattura  = new Fattura();

        assertTrue(tecnico.validaEstensione("foto.jpg"));
        assertFalse(fattura.validaEstensione("foto.jpg"));
    }

    @Test
    void polimorfismo_pdfAccettatoDaEntrambi() {
        Documento tecnico = new DocumentoTecnico();
        Documento fattura  = new Fattura();

        assertTrue(tecnico.validaEstensione("doc.pdf"));
        assertTrue(fattura.validaEstensione("doc.pdf"));
    }

    @Test
    void polimorfismo_fatturaEPreventivoComportansiUguali() {
        Documento fattura    = new Fattura();
        Documento preventivo = new Preventivo();

        assertTrue(fattura.validaEstensione("doc.pdf"));
        assertTrue(preventivo.validaEstensione("doc.pdf"));
        assertFalse(fattura.validaEstensione("doc.jpg"));
        assertFalse(preventivo.validaEstensione("doc.jpg"));
    }
}
