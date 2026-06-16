package com.buildit.backend.dominio;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestFattura {

    private Fattura fattura;

    @BeforeEach
    public void setUp() {
        fattura = new Fattura();
        fattura.setNome("Fattura Fondamenta");
        fattura.setImporto(15000.00);
        fattura.setData(LocalDate.of(2026, 3, 31));
        fattura.setFileUrl("fattura001.pdf");
        fattura.setStatoPagamento(StatoFattura.DA_SALDARE);
    }

    @Test
    public void testGetterSetter() {
        assertEquals(15000.00, fattura.getImporto(), 0.01);
        assertEquals(StatoFattura.DA_SALDARE, fattura.getStatoPagamento());

        fattura.setStatoPagamento(StatoFattura.SALDATO);
        assertEquals(StatoFattura.SALDATO, fattura.getStatoPagamento());
    }
}
