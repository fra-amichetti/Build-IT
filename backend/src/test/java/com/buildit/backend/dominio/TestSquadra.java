package com.buildit.backend.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;




public class TestSquadra {

    private Squadra squadra;

    @BeforeEach
    public void setUp() {
        squadra = new Squadra();
        squadra.setNome("Squadra Muratori");
        squadra.setSpecializzazione(Specializzazione.MURATORI);
        squadra.setNumeroComponenti(5);
        squadra.setNomeReferente("Giovanni Bianchi");
        squadra.setFasiDelCantiere(new ArrayList<>());
    }

    @Test
    public void testIsDisponibileLibero() {
        assertTrue(squadra.isDisponibile(
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 5)
        ));
    }

    @Test
    public void testIsDisponibileOccupato() {
        FaseLavorativa fase = new FaseLavorativa();
        fase.setNome("Demolizione Pareti");
        fase.setDataInizioPrevista(LocalDate.of(2026, 9, 1));
        fase.setDataFinePrevista(LocalDate.of(2026, 9, 15));
        fase.setStato(StatoFase.PIANIFICATA);
        fase.setSquadra(squadra);

        List<FaseLavorativa> fasi = new ArrayList<>();
        fasi.add(fase);
        squadra.setFasiDelCantiere(fasi);

        assertFalse(squadra.isDisponibile(
            LocalDate.of(2026, 9, 10),
            LocalDate.of(2026, 9, 20)
        ));
    }

    @Test
    public void testIsDisponibileFaseTerminataNonConta() {
        FaseLavorativa fase = new FaseLavorativa();
        fase.setDataInizioPrevista(LocalDate.of(2026, 9, 1));
        fase.setDataFinePrevista(LocalDate.of(2026, 9, 15));
        fase.setStato(StatoFase.TERMINATA);

        List<FaseLavorativa> fasi = new ArrayList<>();
        fasi.add(fase);
        squadra.setFasiDelCantiere(fasi);

        // Anche se le date si sovrappongono, la fase è terminata quindi è disponibile
        assertTrue(squadra.isDisponibile(
            LocalDate.of(2026, 9, 10),
            LocalDate.of(2026, 9, 20)
        ));
    }
}