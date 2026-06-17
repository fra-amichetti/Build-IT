package com.buildit.backend.gestioneAmministratore;

import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Specializzazione;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.log.Logger;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SquadreControllerTest {

    @Mock private SquadraRepository       squadraRepository;
    @Mock private FaseLavorativaRepository faseLavorativaRepository;
    @Mock private Logger                  logger;

    private SquadreController controller;

    private static final String EMAIL_ADMIN = "admin@buildit.it";

    @BeforeEach
    void setUp() {
        controller = new SquadreController(squadraRepository, faseLavorativaRepository, logger);
    }

    // ── getSquadre ────────────────────────────────────────────────────────────

    @Test
    void getSquadre_ritornaListaDalRepository() {
        Squadra s = squadra(1L, "Squadra Alfa");
        when(squadraRepository.findAll()).thenReturn(List.of(s));

        ResponseEntity<?> risposta = controller.getSquadre();

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) risposta.getBody()).hasSize(1);
    }

    // ── aggiungiSquadra ───────────────────────────────────────────────────────

    @Test
    void aggiungiSquadra_ok_conDatiValidi() {
        Squadra salvata = squadra(1L, "Squadra Beta");
        when(squadraRepository.save(any())).thenReturn(salvata);

        ResponseEntity<?> risposta = controller.aggiungiSquadra(Map.of(
                "nome", "Squadra Beta",
                "specializzazione", "MURATORI",
                "numeroComponenti", "4",
                "nomeReferente", "Luca Verdi"
        ), EMAIL_ADMIN);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(squadraRepository).save(any());
    }

    @Test
    void aggiungiSquadra_400_seNomeMancante() {
        ResponseEntity<?> risposta = controller.aggiungiSquadra(Map.of(
                "nome", "",
                "specializzazione", "ELETTRICISTI",
                "numeroComponenti", "3",
                "nomeReferente", "Test"
        ), EMAIL_ADMIN);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("nome");
        verify(squadraRepository, never()).save(any());
    }

    // ── eliminaSquadra ────────────────────────────────────────────────────────

    @Test
    void eliminaSquadra_404_seNonTrovata() {
        when(squadraRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<?> risposta = controller.eliminaSquadra(99L, EMAIL_ADMIN);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(squadraRepository, never()).deleteById(any());
    }

    @Test
    void eliminaSquadra_400_seAssegnataAFaseAttiva() {
        Squadra s = squadra(1L, "Squadra Gamma");
        when(squadraRepository.findById(1L)).thenReturn(Optional.of(s));

        FaseLavorativa faseAttiva = fase(1L, StatoFase.IN_CORSO);
        faseAttiva.setSquadra(s);
        when(faseLavorativaRepository.findAll()).thenReturn(List.of(faseAttiva));

        ResponseEntity<?> risposta = controller.eliminaSquadra(1L, EMAIL_ADMIN);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errore(risposta)).containsIgnoringCase("attive");
        verify(squadraRepository, never()).deleteById(any());
    }

    @Test
    void eliminaSquadra_400_seAssegnataAFasePianificata() {
        Squadra s = squadra(2L, "Squadra Delta");
        when(squadraRepository.findById(2L)).thenReturn(Optional.of(s));

        FaseLavorativa fasePian = fase(2L, StatoFase.PIANIFICATA);
        fasePian.setSquadra(s);
        when(faseLavorativaRepository.findAll()).thenReturn(List.of(fasePian));

        ResponseEntity<?> risposta = controller.eliminaSquadra(2L, EMAIL_ADMIN);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void eliminaSquadra_ok_seNessunFaseAttiva() {
        Squadra s = squadra(3L, "Squadra Libera");
        when(squadraRepository.findById(3L)).thenReturn(Optional.of(s));
        when(faseLavorativaRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> risposta = controller.eliminaSquadra(3L, EMAIL_ADMIN);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(squadraRepository).deleteById(3L);
    }

    @Test
    void eliminaSquadra_ok_seHaSoloFasiTerminate() {
        Squadra s = squadra(4L, "Squadra Veterana");
        when(squadraRepository.findById(4L)).thenReturn(Optional.of(s));

        FaseLavorativa faseTerminata = fase(4L, StatoFase.TERMINATA);
        faseTerminata.setSquadra(s);
        when(faseLavorativaRepository.findAll()).thenReturn(List.of(faseTerminata));

        ResponseEntity<?> risposta = controller.eliminaSquadra(4L, EMAIL_ADMIN);

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(squadraRepository).deleteById(4L);
    }

    // ── utility ───────────────────────────────────────────────────────────────

    private static Squadra squadra(Long id, String nome) {
        Squadra s = new Squadra();
        s.setId(id);
        s.setNome(nome);
        s.setSpecializzazione(Specializzazione.MURATORI);
        s.setNumeroComponenti(3);
        s.setNomeReferente("Referente Test");
        return s;
    }

    private static FaseLavorativa fase(Long id, StatoFase stato) {
        FaseLavorativa f = new FaseLavorativa();
        f.setId(id);
        f.setNome("Fase test");
        f.setDataInizioPrevista(LocalDate.of(2026, 1, 1));
        f.setDataFinePrevista(LocalDate.of(2026, 6, 30));
        f.setStato(stato);
        return f;
    }

    @SuppressWarnings("unchecked")
    private static String errore(ResponseEntity<?> r) {
        return ((Map<String, String>) r.getBody()).get("errore");
    }
}
