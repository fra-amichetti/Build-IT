package com.buildit.backend.config;

import com.buildit.backend.dominio.Amministratore;
import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.DocumentoContabile;
import com.buildit.backend.dominio.DocumentoTecnico;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.DocumentoContabileRepository;
import com.buildit.backend.repository.DocumentoTecnicoRepository;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;
import com.buildit.backend.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataLoaderTest {

    private static final String ADMIN_EMAIL = "admin1@buildit.it";

    @Mock private UtenteRepository utenteRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CantiereRepository cantiereRepository;
    @Mock private FaseLavorativaRepository faseLavorativaRepository;
    @Mock private SquadraRepository squadraRepository;
    @Mock private DocumentoTecnicoRepository documentoTecnicoRepository;
    @Mock private DocumentoContabileRepository documentoContabileRepository;

    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader();

        // Default: tutto vuoto → tutti i blocchi di seeding si attivano.
        when(utenteRepository.existsByEmail(anyString())).thenReturn(false);
        when(cantiereRepository.findAll()).thenReturn(Collections.emptyList());
        when(squadraRepository.findAll()).thenReturn(Collections.emptyList());
        when(faseLavorativaRepository.findAll()).thenReturn(Collections.emptyList());
        when(documentoTecnicoRepository.findAll()).thenReturn(Collections.emptyList());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
    }

    private CommandLineRunner runner() {
        return dataLoader.loadData(
                utenteRepository,
                passwordEncoder,
                cantiereRepository,
                faseLavorativaRepository,
                squadraRepository,
                documentoTecnicoRepository,
                documentoContabileRepository);
    }

    // ── AMMINISTRATORE ────────────────────────────────────────────────────────

    @Test
    void loadData_creaLAmministratore_quandoNonEsiste() throws Exception {
        runner().run();

        verify(passwordEncoder).encode("Admin1234!");
        verify(utenteRepository).save(any(Amministratore.class));
    }

    @Test
    void loadData_nonCreaLAmministratore_quandoGiaEsiste() throws Exception {
        when(utenteRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(true);

        runner().run();

        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ── CANTIERI ──────────────────────────────────────────────────────────────

    @Test
    void loadData_creaQuattroCantieri_quandoRepositoryVuoto() throws Exception {
        runner().run();

        verify(cantiereRepository, times(4)).save(any(Cantiere.class));
    }

    @Test
    void loadData_nonCreaCantieri_quandoGiaPresenti() throws Exception {
        Cantiere esistente = new Cantiere();
        esistente.setNome("Cantiere esistente");
        when(cantiereRepository.findAll()).thenReturn(List.of(esistente));

        runner().run();

        verify(cantiereRepository, never()).save(any(Cantiere.class));
    }

    // ── SQUADRE ───────────────────────────────────────────────────────────────

    @Test
    void loadData_creaQuattroSquadre_quandoRepositoryVuoto() throws Exception {
        runner().run();

        verify(squadraRepository, times(4)).save(any(Squadra.class));
    }

    @Test
    void loadData_nonCreaSquadre_quandoGiaPresenti() throws Exception {
        Squadra esistente = new Squadra();
        esistente.setNome("Squadra esistente");
        when(squadraRepository.findAll()).thenReturn(List.of(esistente));

        runner().run();

        verify(squadraRepository, never()).save(any(Squadra.class));
    }

    // ── FASI LAVORATIVE ───────────────────────────────────────────────────────

    @Test
    void loadData_creaQuattordiciFasi_quandoEsistonoTuttiICantieriELeSquadre() throws Exception {
        Cantiere residenza = cantiere("Residenza");
        Cantiere palazzo = cantiere("Palazzo Medici");
        Cantiere villa = cantiere("Villa Serena");
        Cantiere centro = cantiere("Centro Commerciale Nord");

        Squadra alpha = squadra("Squadra Alpha");
        Squadra beta = squadra("Squadra Beta");
        Squadra gamma = squadra("Squadra Gamma");
        Squadra delta = squadra("Squadra Delta");

        when(cantiereRepository.findAll()).thenReturn(List.of(residenza, palazzo, villa, centro));
        when(squadraRepository.findAll()).thenReturn(List.of(alpha, beta, gamma, delta));

        runner().run();

        // Residenza 4 + Palazzo Medici 3 + Villa Serena 3 + Centro Commerciale Nord 4 = 14
        verify(faseLavorativaRepository, times(14)).save(any(FaseLavorativa.class));
    }

    @Test
    void loadData_nonCreaFasi_quandoGiaPresenti() throws Exception {
        when(faseLavorativaRepository.findAll()).thenReturn(List.of(new FaseLavorativa()));

        runner().run();

        verify(faseLavorativaRepository, never()).save(any(FaseLavorativa.class));
    }

    // ── DOCUMENTI ─────────────────────────────────────────────────────────────

    @Test
    void loadData_creaIDocumenti_quandoEsisteIlCantiereResidenza() throws Exception {
        Cantiere residenza = cantiere("Residenza");
        when(cantiereRepository.findAll()).thenReturn(List.of(residenza));

        runner().run();

        // 2 documenti tecnici e 3 documenti contabili (2 fatture + 1 preventivo).
        verify(documentoTecnicoRepository, times(2)).save(any(DocumentoTecnico.class));
        verify(documentoContabileRepository, times(3)).save(any(DocumentoContabile.class));
    }

    @Test
    void loadData_nonCreaDocumenti_quandoNonEsisteIlCantiereResidenza() throws Exception {
        Cantiere altro = cantiere("Altro Cantiere");
        when(cantiereRepository.findAll()).thenReturn(List.of(altro));

        runner().run();

        verify(documentoTecnicoRepository, never()).save(any());
        verify(documentoContabileRepository, never()).save(any());
    }

    // ── UTILITY ───────────────────────────────────────────────────────────────

    @Test
    void loadData_restituisceUnRunnerNonNullo() {
        assertThat(runner()).isNotNull();
    }

    private static Cantiere cantiere(String nome) {
        Cantiere c = new Cantiere();
        c.setNome(nome);
        return c;
    }

    private static Squadra squadra(String nome) {
        Squadra s = new Squadra();
        s.setNome(nome);
        return s;
    }
}
