package com.buildit.backend.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TestUtente {

    @Test
    public void testClienteGetRuolo() {
        Cliente cliente = new Cliente();
        cliente.setNome("Mario");
        cliente.setCognome("Rossi");
        cliente.setEmail("mario@email.it");
        cliente.setHashPassword("hash123");

        assertEquals("CLIENTE", cliente.getRuolo());
        assertEquals("Mario", cliente.getNome());
        assertEquals("Rossi", cliente.getCognome());
    }

    @Test
    public void testDipendenteGetRuolo() {
        Dipendente dipendente = new Dipendente();
        dipendente.setNome("Luigi");
        dipendente.setIncarico("Capo cantiere");

        assertEquals("DIPENDENTE", dipendente.getRuolo());
        assertEquals("Capo cantiere", dipendente.getIncarico());
    }

    @Test
    public void testAmministratoreGetRuolo() {
        Amministratore admin = new Amministratore();
        admin.setNome("Marco");
        admin.setNomeAzienda("BuildIT Srl");

        assertEquals("AMMINISTRATORE", admin.getRuolo());
        assertEquals("BuildIT Srl", admin.getNomeAzienda());
    }
}