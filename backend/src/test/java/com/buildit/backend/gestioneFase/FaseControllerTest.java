package com.buildit.backend.gestioneFase;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFase;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaseControllerTest {

    @Mock private FaseLavorativaRepository faseLavorativaRepository;
    @Mock private SquadraRepository        squadraRepository;

    private FaseController controller;

    @BeforeEach
    void setUp() {
        controller = new FaseController(faseLavorativaRepository, squadraRepository);
    }

    // ── modificaFase – controllo squadra ─────────────────────────────────────

    @Test
    void modificaFase_bloccaSeNuovaSquadraHaSovrapposizione() {
        FaseLavorativa fase = fasePianificata(10L);
        when(faseLavorativaRepository.findById(10L)).thenReturn(Optional.of(fase));

        FaseLavorativa conflitto = fasePianificata(99L);
        when(faseLavorativaRepository.findOverlappingBySquadra(
                anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(conflitto));

        ResponseEntity<?> risposta = controller.modificaFase(10L, Map.of("squadraId", "7"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).contains("squadra");
        verify(faseLavorativaRepository, never()).save(any());
    }

    @Test
    void modificaFase_consenteSeNuovaSquadraNonHaSovrapposizione() {
        FaseLavorativa fase = fasePianificata(10L);
        when(faseLavorativaRepository.findById(10L)).thenReturn(Optional.of(fase));
        when(faseLavorativaRepository.findOverlappingBySquadra(
                anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(squadraRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(faseLavorativaRepository.save(any())).thenReturn(fase);

        ResponseEntity<?> risposta = controller.modificaFase(10L, Map.of("squadraId", "7"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void modificaFase_consenteSenzaCambioSquadra() {
        FaseLavorativa fase = fasePianificata(10L);
        when(faseLavorativaRepository.findById(10L)).thenReturn(Optional.of(fase));
        when(faseLavorativaRepository.save(any())).thenReturn(fase);

        ResponseEntity<?> risposta = controller.modificaFase(10L, Map.of("nome", "Nuovo nome"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(faseLavorativaRepository, never()).findOverlappingBySquadra(anyLong(), anyLong(), any(), any());
    }

    @Test
    void modificaFase_bloccaSeGiaTerminata() {
        FaseLavorativa fase = fase(StatoFase.TERMINATA, 10L);
        when(faseLavorativaRepository.findById(10L)).thenReturn(Optional.of(fase));

        ResponseEntity<?> risposta = controller.modificaFase(10L, Map.of("nome", "Test"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(faseLavorativaRepository, never()).save(any());
    }

    // ── getDettagliFase ───────────────────────────────────────────────────────

    @Test
    void getDettagliFase_ok_seEsiste() {
        FaseLavorativa fase = fasePianificata(10L);
        when(faseLavorativaRepository.findById(10L)).thenReturn(Optional.of(fase));

        ResponseEntity<?> risposta = controller.getDettagliFase(10L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(risposta.getBody()).isEqualTo(fase);
    }

    @Test
    void getDettagliFase_404_seNonTrovata() {
        when(faseLavorativaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> risposta = controller.getDettagliFase(99L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── avviaFase ─────────────────────────────────────────────────────────────

    @Test
    void avviaFase_ok_seFasePianificata() {
        FaseLavorativa fase = fasePianificata(20L);
        when(faseLavorativaRepository.findById(20L)).thenReturn(Optional.of(fase));
        when(faseLavorativaRepository.save(any())).thenReturn(fase);

        ResponseEntity<?> risposta = controller.avviaFase(20L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fase.getStato()).isEqualTo(StatoFase.IN_CORSO);
        assertThat(fase.getDataInizioEffettiva()).isEqualTo(LocalDate.now());
    }

    @Test
    void avviaFase_400_seFaseNonPianificata() {
        FaseLavorativa fase = fase(StatoFase.IN_CORSO, 20L);
        when(faseLavorativaRepository.findById(20L)).thenReturn(Optional.of(fase));

        ResponseEntity<?> risposta = controller.avviaFase(20L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("pianificata");
        verify(faseLavorativaRepository, never()).save(any());
    }

    @Test
    void avviaFase_404_seNonTrovata() {
        when(faseLavorativaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> risposta = controller.avviaFase(99L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void avviaFase_400_seCantierePianificato() {
        FaseLavorativa fase = fase(StatoFase.PIANIFICATA, 20L, StatoCantiere.PIANIFICATO);
        when(faseLavorativaRepository.findById(20L)).thenReturn(Optional.of(fase));

        ResponseEntity<?> risposta = controller.avviaFase(20L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("cantiere");
        verify(faseLavorativaRepository, never()).save(any());
    }

    // ── terminaFase ───────────────────────────────────────────────────────────

    @Test
    void terminaFase_ok_seFaseInCorso() {
        FaseLavorativa fase = fase(StatoFase.IN_CORSO, 30L);
        when(faseLavorativaRepository.findById(30L)).thenReturn(Optional.of(fase));
        when(faseLavorativaRepository.save(any())).thenReturn(fase);

        ResponseEntity<?> risposta = controller.terminaFase(30L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fase.getStato()).isEqualTo(StatoFase.TERMINATA);
        assertThat(fase.getDataFineEffettiva()).isEqualTo(LocalDate.now());
    }

    @Test
    void terminaFase_400_seGiaTerminata() {
        FaseLavorativa fase = fase(StatoFase.TERMINATA, 30L);
        when(faseLavorativaRepository.findById(30L)).thenReturn(Optional.of(fase));

        ResponseEntity<?> risposta = controller.terminaFase(30L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("terminata");
        verify(faseLavorativaRepository, never()).save(any());
    }

    @Test
    void terminaFase_404_seNonTrovata() {
        when(faseLavorativaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> risposta = controller.terminaFase(99L);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── assegnaSquadra ────────────────────────────────────────────────────────

    @Test
    void assegnaSquadra_ok_seEntrambiEsistono() {
        FaseLavorativa fase = fasePianificata(40L);
        Squadra squadra = new Squadra();
        squadra.setId(5L);
        squadra.setNome("Squadra Alfa");

        when(faseLavorativaRepository.findById(40L)).thenReturn(Optional.of(fase));
        when(squadraRepository.findById(5L)).thenReturn(Optional.of(squadra));
        when(faseLavorativaRepository.save(any())).thenReturn(fase);

        ResponseEntity<?> risposta = controller.assegnaSquadra(40L, Map.of("squadraId", "5"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fase.getSquadra()).isEqualTo(squadra);
    }

    @Test
    void assegnaSquadra_404_seFaseNonTrovata() {
        when(faseLavorativaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> risposta = controller.assegnaSquadra(99L, Map.of("squadraId", "5"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(faseLavorativaRepository, never()).save(any());
    }

    @Test
    void assegnaSquadra_404_seSquadraNonTrovata() {
        FaseLavorativa fase = fasePianificata(40L);
        when(faseLavorativaRepository.findById(40L)).thenReturn(Optional.of(fase));
        when(squadraRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> risposta = controller.assegnaSquadra(40L, Map.of("squadraId", "99"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(faseLavorativaRepository, never()).save(any());
    }

    // ── utility ───────────────────────────────────────────────────────────────

    private static FaseLavorativa fasePianificata(Long id) {
        return fase(StatoFase.PIANIFICATA, id);
    }

    private static FaseLavorativa fase(StatoFase stato, Long id) {
        return fase(stato, id, StatoCantiere.IN_CORSO);
    }

    private static FaseLavorativa fase(StatoFase statoFase, Long id, StatoCantiere statoCantiere) {
        FaseLavorativa f = new FaseLavorativa();
        f.setId(id);
        f.setNome("Fase test");
        f.setDataInizioPrevista(LocalDate.of(2025, 3, 1));
        f.setDataFinePrevista(LocalDate.of(2025, 5, 31));
        f.setStato(statoFase);
        Cantiere c = new Cantiere();
        c.setStato(statoCantiere);
        f.setCantiere(c);
        return f;
    }

    @SuppressWarnings("unchecked")
    private static String errore(ResponseEntity<?> r) {
        return ((Map<String, String>) r.getBody()).get("errore");
    }
}
