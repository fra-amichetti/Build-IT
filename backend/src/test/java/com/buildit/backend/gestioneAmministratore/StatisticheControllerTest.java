package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.*;
import com.buildit.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticheControllerTest {

    @Mock private CantiereRepository            cantiereRepository;
    @Mock private SquadraRepository             squadraRepository;
    @Mock private FaseLavorativaRepository      faseLavorativaRepository;
    @Mock private DocumentoContabileRepository  documentoContabileRepository;

    private StatisticheController controller;

    @BeforeEach
    void setUp() {
        controller = new StatisticheController(
                cantiereRepository, squadraRepository,
                faseLavorativaRepository, documentoContabileRepository);
    }

    // ── Fatturato ─────────────────────────────────────────────────────────────

    @Test
    void getStatistiche_fatturatoTotale_ugualeIncassatoPiuSaldo() {
        Fattura saldato   = fattura(10000.0, StatoFattura.SALDATO);
        Fattura daSaldare = fattura(5000.0,  StatoFattura.DA_SALDARE);
        stubRepositoriVuoti();
        when(documentoContabileRepository.findAll()).thenReturn(List.of(saldato, daSaldare));

        Map<String, Object> body = getBody();

        double totale    = (double) body.get("fatturatoTotale");
        double incassato = (double) body.get("fatturatoIncassato");
        double saldo     = (double) body.get("saldoDaIncassare");

        assertThat(totale).isEqualTo(15000.0, withPrecision(0.01));
        assertThat(incassato).isEqualTo(10000.0, withPrecision(0.01));
        assertThat(saldo).isEqualTo(5000.0, withPrecision(0.01));
        assertThat(totale).isEqualTo(incassato + saldo, withPrecision(0.01));
    }

    @Test
    void getStatistiche_valoriNonNegativi_seRepositoriVuoti() {
        stubRepositoriVuoti();
        when(documentoContabileRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> body = getBody();

        assertThat((double) body.get("fatturatoTotale")).isGreaterThanOrEqualTo(0.0);
        assertThat((double) body.get("fatturatoIncassato")).isGreaterThanOrEqualTo(0.0);
        assertThat((double) body.get("saldoDaIncassare")).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void getStatistiche_preventivi_nonContriboisconoAlFatturato() {
        // Un Preventivo non è una Fattura → non deve essere sommato
        Preventivo prev = new Preventivo();
        prev.setImporto(8000.0);
        stubRepositoriVuoti();
        when(documentoContabileRepository.findAll()).thenReturn(List.of(prev));

        Map<String, Object> body = getBody();

        assertThat((double) body.get("fatturatoTotale")).isEqualTo(0.0, withPrecision(0.01));
    }

    // ── Contatori cantieri ────────────────────────────────────────────────────

    @Test
    void getStatistiche_contatoriCantieri_coerentiConStato() {
        Cantiere inCorso    = cantiereCon(StatoCantiere.IN_CORSO);
        Cantiere inRitardo  = cantiereCon(StatoCantiere.IN_RITARDO);
        Cantiere terminato  = cantiereCon(StatoCantiere.TERMINATO);
        Cantiere pianificato= cantiereCon(StatoCantiere.PIANIFICATO);

        when(cantiereRepository.findAll()).thenReturn(List.of(inCorso, inRitardo, terminato, pianificato));
        when(squadraRepository.findAll()).thenReturn(Collections.emptyList());
        when(faseLavorativaRepository.findAll()).thenReturn(Collections.emptyList());
        when(documentoContabileRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> body = getBody();

        // Attivi = IN_CORSO + IN_RITARDO = 2
        assertThat((long) body.get("numeroCantieriAttivi")).isEqualTo(2L);
        assertThat((long) body.get("numeroCantieriInRitardo")).isEqualTo(1L);
        assertThat((long) body.get("numeroCantieriTerminati")).isEqualTo(1L);
    }

    @Test
    void getStatistiche_contatoriNonNegativi_seNessunCantiere() {
        stubRepositoriVuoti();
        when(documentoContabileRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> body = getBody();

        assertThat((long) body.get("numeroCantieriAttivi")).isGreaterThanOrEqualTo(0L);
        assertThat((long) body.get("numeroCantieriInRitardo")).isGreaterThanOrEqualTo(0L);
        assertThat((long) body.get("numeroCantieriTerminati")).isGreaterThanOrEqualTo(0L);
    }

    // ── Squadre impiegate ─────────────────────────────────────────────────────

    @Test
    void getStatistiche_squadreImpiegate_includesoloConFasiAttive() {
        Squadra s = new Squadra();
        s.setId(1L);
        s.setNome("Squadra Alfa");
        s.setSpecializzazione(Specializzazione.MURATORI);

        FaseLavorativa faseAttiva = new FaseLavorativa();
        faseAttiva.setStato(StatoFase.IN_CORSO);
        faseAttiva.setSquadra(s);

        when(cantiereRepository.findAll()).thenReturn(Collections.emptyList());
        when(squadraRepository.findAll()).thenReturn(List.of(s));
        when(faseLavorativaRepository.findAll()).thenReturn(List.of(faseAttiva));
        when(documentoContabileRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> body = getBody();

        List<?> squadreImpiegate = (List<?>) body.get("squadreImpiegate");
        assertThat(squadreImpiegate).hasSize(1);
    }

    // ── getStatisticheCantiere ────────────────────────────────────────────────

    @Test
    void getStatisticheCantiere_ritornaFatturatoDiQuelCantiere() {
        Fattura f = fattura(3000.0, StatoFattura.DA_SALDARE);
        when(documentoContabileRepository.findByCantiereId(42L)).thenReturn(List.of(f));

        ResponseEntity<?> risposta = controller.getStatisticheCantiere(42L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?,?> body = (Map<?,?>) risposta.getBody();
        assertThat((double) body.get("fatturatoCantiere")).isEqualTo(3000.0, withPrecision(0.01));
        assertThat((double) body.get("saldoCantiere")).isEqualTo(3000.0, withPrecision(0.01));
    }

    // ── utility ───────────────────────────────────────────────────────────────

    private static Fattura fattura(double importo, StatoFattura stato) {
        Fattura f = new Fattura();
        f.setImporto(importo);
        f.setStatoPagamento(stato);
        return f;
    }

    private static Cantiere cantiereCon(StatoCantiere stato) {
        Cantiere c = new Cantiere();
        c.setNome("Test");
        c.setIndirizzo("Via Test");
        c.setDataInizioPrevista(LocalDate.of(2026, 1, 1));
        c.setDataFinePrevista(LocalDate.of(2026, 12, 31));
        c.setStato(stato);
        return c;
    }

    private void stubRepositoriVuoti() {
        when(cantiereRepository.findAll()).thenReturn(Collections.emptyList());
        when(squadraRepository.findAll()).thenReturn(Collections.emptyList());
        when(faseLavorativaRepository.findAll()).thenReturn(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBody() {
        ResponseEntity<?> risposta = controller.getStatistiche();
        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (Map<String, Object>) risposta.getBody();
    }

    private static org.assertj.core.data.Offset<Double> withPrecision(double delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }
}
