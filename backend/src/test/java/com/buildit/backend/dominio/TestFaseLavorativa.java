package com.buildit.backend.dominio;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestFaseLavorativa {

    private FaseLavorativa fase;

    @BeforeEach
    public void setUp() {
        fase = new FaseLavorativa();
        fase.setNome("Demolizione Pareti");
        fase.setDataInizioPrevista(LocalDate.of(2026, 9, 1));
        fase.setDataFinePrevista(LocalDate.of(2026, 9, 15));
        fase.setStato(StatoFase.PIANIFICATA);
    }

    @Test
    public void testAvviaETerminaFase() {
        assertEquals(StatoFase.PIANIFICATA, fase.getStato());

        fase.avviaFase();
        assertEquals(StatoFase.IN_CORSO, fase.getStato());
        assertEquals(LocalDate.now(), fase.getDataInizioEffettiva());

        fase.terminaFase();
        assertEquals(StatoFase.TERMINATA, fase.getStato());
        assertEquals(LocalDate.now(), fase.getDataFineEffettiva());
    }
}


