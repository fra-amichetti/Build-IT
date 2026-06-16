package com.buildit.backend.gestioneFase;

import com.buildit.backend.dominio.FaseLavorativa;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaseControllerTest {

    @Mock private FaseLavorativaRepository faseLavorativaRepository;
    @Mock private SquadraRepository squadraRepository;

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

        Map<String, String> body = Map.of("squadraId", "7");

        ResponseEntity<?> risposta = controller.modificaFase(10L, body);

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

        Map<String, String> body = Map.of("squadraId", "7");

        ResponseEntity<?> risposta = controller.modificaFase(10L, body);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void modificaFase_consenteSenzaCambioSquadra() {
        FaseLavorativa fase = fasePianificata(10L);
        when(faseLavorativaRepository.findById(10L)).thenReturn(Optional.of(fase));
        when(faseLavorativaRepository.save(any())).thenReturn(fase);

        Map<String, String> body = Map.of("nome", "Nuovo nome");

        ResponseEntity<?> risposta = controller.modificaFase(10L, body);

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

    // ── utility ───────────────────────────────────────────────────────────────

    private static FaseLavorativa fasePianificata(Long id) {
        return fase(StatoFase.PIANIFICATA, id);
    }

    private static FaseLavorativa fase(StatoFase stato, Long id) {
        FaseLavorativa f = new FaseLavorativa();
        f.setId(id);
        f.setNome("Fase test");
        f.setDataInizioPrevista(LocalDate.of(2025, 3, 1));
        f.setDataFinePrevista(LocalDate.of(2025, 5, 31));
        f.setStato(stato);
        return f;
    }

    @SuppressWarnings("unchecked")
    private static String errore(ResponseEntity<?> r) {
        return ((Map<String, String>) r.getBody()).get("errore");
    }
}
