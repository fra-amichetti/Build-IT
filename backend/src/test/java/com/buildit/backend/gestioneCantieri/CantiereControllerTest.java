package com.buildit.backend.gestioneCantieri;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.log.Logger;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CantiereControllerTest {

    @Mock private CantiereRepository       cantiereRepository;
    @Mock private FaseLavorativaRepository faseLavorativaRepository;
    @Mock private SquadraRepository        squadraRepository;
    @Mock private Logger                   logger;

    private CantiereController controller;

    private static final String EMAIL = "admin@buildit.it";

    @BeforeEach
    void setUp() {
        controller = new CantiereController(cantiereRepository, faseLavorativaRepository, squadraRepository, logger);
    }

    // ── terminaCantiere ────────────────────────────────────────────────────────

    @Test
    void terminaCantiere_bloccaSeFasiNonTerminate() {
        Cantiere cantiere = cantiereInCorso(1L);
        when(cantiereRepository.findById(1L)).thenReturn(Optional.of(cantiere));

        FaseLavorativa fasiInCorso = fase(StatoFase.IN_CORSO);
        when(faseLavorativaRepository.findByCantiereId(1L)).thenReturn(List.of(fasiInCorso));

        ResponseEntity<?> risposta = controller.terminaCantiere(1L, EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).contains("fase");
        verify(cantiereRepository, never()).save(any());
    }

    @Test
    void terminaCantiere_bloccaSeFasiPianificate() {
        Cantiere cantiere = cantiereInCorso(1L);
        when(cantiereRepository.findById(1L)).thenReturn(Optional.of(cantiere));

        FaseLavorativa fasePianificata = fase(StatoFase.PIANIFICATA);
        FaseLavorativa faseTerminata   = fase(StatoFase.TERMINATA);
        when(faseLavorativaRepository.findByCantiereId(1L)).thenReturn(List.of(fasePianificata, faseTerminata));

        ResponseEntity<?> risposta = controller.terminaCantiere(1L, EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void terminaCantiere_consenteSeTutteLeFasiSonoTerminate() {
        Cantiere cantiere = cantiereInCorso(1L);
        when(cantiereRepository.findById(1L)).thenReturn(Optional.of(cantiere));
        when(faseLavorativaRepository.findByCantiereId(1L)).thenReturn(List.of(fase(StatoFase.TERMINATA)));
        when(cantiereRepository.save(any())).thenReturn(cantiere);

        ResponseEntity<?> risposta = controller.terminaCantiere(1L, EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void terminaCantiere_consenteSeNessunFase() {
        Cantiere cantiere = cantiereInCorso(1L);
        when(cantiereRepository.findById(1L)).thenReturn(Optional.of(cantiere));
        when(faseLavorativaRepository.findByCantiereId(1L)).thenReturn(Collections.emptyList());
        when(cantiereRepository.save(any())).thenReturn(cantiere);

        ResponseEntity<?> risposta = controller.terminaCantiere(1L, EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── aggiungiFase ──────────────────────────────────────────────────────────

    @Test
    void aggiungiFase_bloccaSeSquadraHaSovrapposizione() {
        Cantiere cantiere = cantiereInCorso(1L);
        when(cantiereRepository.findById(1L)).thenReturn(Optional.of(cantiere));

        FaseLavorativa conflitto = fase(StatoFase.IN_CORSO);
        when(faseLavorativaRepository.findOverlappingBySquadra(eq(5L), eq(-1L),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(conflitto));

        Map<String, String> body = Map.of(
                "nome", "Nuova fase",
                "descrizione", "",
                "dataInizioPrevista", "2025-03-01",
                "dataFinePrevista", "2025-05-31",
                "squadraId", "5"
        );

        ResponseEntity<?> risposta = controller.aggiungiFase(1L, body, EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).contains("squadra");
        verify(faseLavorativaRepository, never()).save(any());
    }

    @Test
    void aggiungiFase_consenteSeSquadraNonHaSovrapposizione() {
        Cantiere cantiere = cantiereInCorso(1L);
        when(cantiereRepository.findById(1L)).thenReturn(Optional.of(cantiere));
        when(faseLavorativaRepository.findOverlappingBySquadra(anyLong(), eq(-1L),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(squadraRepository.findById(anyLong())).thenReturn(Optional.empty());
        FaseLavorativa salvata = fase(StatoFase.PIANIFICATA);
        when(faseLavorativaRepository.save(any())).thenReturn(salvata);

        Map<String, String> body = Map.of(
                "nome", "Nuova fase",
                "descrizione", "",
                "dataInizioPrevista", "2025-03-01",
                "dataFinePrevista", "2025-05-31",
                "squadraId", "5"
        );

        ResponseEntity<?> risposta = controller.aggiungiFase(1L, body, EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void aggiungiFase_consenteSenzaSquadra() {
        Cantiere cantiere = cantiereInCorso(1L);
        when(cantiereRepository.findById(1L)).thenReturn(Optional.of(cantiere));
        FaseLavorativa salvata = fase(StatoFase.PIANIFICATA);
        when(faseLavorativaRepository.save(any())).thenReturn(salvata);

        Map<String, String> body = Map.of(
                "nome", "Nuova fase",
                "descrizione", "",
                "dataInizioPrevista", "2025-03-01",
                "dataFinePrevista", "2025-05-31"
        );

        ResponseEntity<?> risposta = controller.aggiungiFase(1L, body, EMAIL);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(faseLavorativaRepository, never()).findOverlappingBySquadra(anyLong(), anyLong(), any(), any());
    }

    // ── utility ───────────────────────────────────────────────────────────────

    private static Cantiere cantiereInCorso(Long id) {
        Cantiere c = new Cantiere();
        c.setId(id);
        c.setNome("Test");
        c.setIndirizzo("Via Test");
        c.setDataInizioPrevista(LocalDate.of(2025, 1, 1));
        c.setDataFinePrevista(LocalDate.of(2025, 12, 31));
        c.setStato(StatoCantiere.IN_CORSO);
        return c;
    }

    private static FaseLavorativa fase(StatoFase stato) {
        FaseLavorativa f = new FaseLavorativa();
        f.setNome("Fase test");
        f.setDataInizioPrevista(LocalDate.of(2025, 1, 1));
        f.setDataFinePrevista(LocalDate.of(2025, 6, 30));
        f.setStato(stato);
        return f;
    }

    @SuppressWarnings("unchecked")
    private static String errore(ResponseEntity<?> r) {
        return ((Map<String, String>) r.getBody()).get("errore");
    }
}
