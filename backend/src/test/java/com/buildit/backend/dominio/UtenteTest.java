package com.buildit.backend.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtenteTest {

    // ── Amministratore ────────────────────────────────────────────────────────

    @Test
    void amministratore_getRuolo_ritornaAMMINISTRATORE() {
        assertEquals("AMMINISTRATORE", new Amministratore().getRuolo());
    }

    @Test
    void amministratore_getterSetter_campiBase() {
        Amministratore amm = new Amministratore();
        amm.setNome("Mario");
        amm.setCognome("Rossi");
        amm.setEmail("mario.rossi@buildit.it");
        amm.setHashPassword("hash_bcrypt");
        amm.setNomeAzienda("BuildIT S.r.l.");

        assertEquals("Mario", amm.getNome());
        assertEquals("Rossi", amm.getCognome());
        assertEquals("mario.rossi@buildit.it", amm.getEmail());
        assertEquals("hash_bcrypt", amm.getHashPassword());
        assertEquals("BuildIT S.r.l.", amm.getNomeAzienda());
    }

    // ── Cliente ───────────────────────────────────────────────────────────────

    @Test
    void cliente_getRuolo_ritornaCLIENTE() {
        assertEquals("CLIENTE", new Cliente().getRuolo());
    }

    @Test
    void cliente_getterSetter_campiBase() {
        Cliente c = new Cliente();
        c.setNome("Luisa");
        c.setCognome("Verdi");
        c.setEmail("luisa@clienti.it");

        assertEquals("Luisa", c.getNome());
        assertEquals("Verdi", c.getCognome());
        assertEquals("luisa@clienti.it", c.getEmail());
    }

    // ── Dipendente ────────────────────────────────────────────────────────────

    @Test
    void dipendente_getRuolo_ritornaDIPENDENTE() {
        assertEquals("DIPENDENTE", new Dipendente().getRuolo());
    }

    @Test
    void dipendente_getterSetter_inclusiIncarico() {
        Dipendente d = new Dipendente();
        d.setNome("Paolo");
        d.setCognome("Bianchi");
        d.setEmail("paolo@buildit.it");
        d.setIncarico("Capo cantiere");

        assertEquals("Paolo", d.getNome());
        assertEquals("Bianchi", d.getCognome());
        assertEquals("Capo cantiere", d.getIncarico());
    }

    // ── Polimorfismo ──────────────────────────────────────────────────────────

    @Test
    void polimorfismo_getRuolo_diversoPerOgniSottoclasse() {
        Utente admin = new Amministratore();
        Utente cliente = new Cliente();
        Utente dipendente = new Dipendente();

        assertNotEquals(admin.getRuolo(), cliente.getRuolo());
        assertNotEquals(admin.getRuolo(), dipendente.getRuolo());
        assertNotEquals(cliente.getRuolo(), dipendente.getRuolo());
    }

    @Test
    void polimorfismo_istanzaDiUtente_perOgniTipo() {
        assertTrue(new Amministratore() instanceof Utente);
        assertTrue(new Cliente() instanceof Utente);
        assertTrue(new Dipendente() instanceof Utente);
    }
}
