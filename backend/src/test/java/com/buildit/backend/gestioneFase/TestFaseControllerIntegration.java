package com.buildit.backend.gestioneFase;


import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Specializzazione;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_fase",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class TestFaseControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FaseLavorativaRepository faseLavorativaRepository;

    @Autowired
    private SquadraRepository squadraRepository;

    @Autowired
    private CantiereRepository cantiereRepository;

    private Cantiere cantiere;
    private Squadra squadra;

    @BeforeEach
    public void setUp() {
        faseLavorativaRepository.deleteAll();
        squadraRepository.deleteAll();
        cantiereRepository.deleteAll();

        cantiere = new Cantiere();
        cantiere.setNome("Cantiere per fasi");
        cantiere.setIndirizzo("Via Test");
        cantiere.setDataInizioPrevista(LocalDate.now());
        cantiere.setDataFinePrevista(LocalDate.now().plusMonths(3));
        cantiere.setStato(StatoCantiere.IN_CORSO);
        cantiere = cantiereRepository.save(cantiere);

        squadra = new Squadra();
        squadra.setNome("Squadra A");
        squadra.setNumeroComponenti(4);
        squadra.setNomeReferente("Referente A");
        squadra.setSpecializzazione(Specializzazione.MURATORI);
        squadra = squadraRepository.save(squadra);
    }

    private FaseLavorativa creaFase(StatoFase stato) {
        FaseLavorativa fase = new FaseLavorativa();
        fase.setNome("Fase Test");
        fase.setDataInizioPrevista(LocalDate.now());
        fase.setDataFinePrevista(LocalDate.now().plusDays(15));
        fase.setStato(stato);
        fase.setCantiere(cantiere);
        fase.setSquadra(squadra);
        return faseLavorativaRepository.save(fase);
    }

    @Test
    public void testAvviaFasePianificata() throws Exception {
        FaseLavorativa fase = creaFase(StatoFase.PIANIFICATA);

        mockMvc.perform(put("/api/fasi/" + fase.getId() + "/avvia"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stato").value("IN_CORSO"));
    }

    @Test
    public void testAvviaFaseGiaInCorsoFallisce() throws Exception {
        FaseLavorativa fase = creaFase(StatoFase.IN_CORSO);

        mockMvc.perform(put("/api/fasi/" + fase.getId() + "/avvia"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testTerminaFase() throws Exception {
        FaseLavorativa fase = creaFase(StatoFase.IN_CORSO);

        mockMvc.perform(put("/api/fasi/" + fase.getId() + "/termina"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stato").value("TERMINATA"));
    }

    @Test
    public void testModificaFaseTerminataFallisce() throws Exception {
        FaseLavorativa fase = creaFase(StatoFase.TERMINATA);

        String json = """
            { "descrizione": "Nuova descrizione" }
            """;

        mockMvc.perform(put("/api/fasi/" + fase.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testAssegnaSquadra() throws Exception {
        FaseLavorativa fase = creaFase(StatoFase.PIANIFICATA);

        Squadra nuovaSquadra = new Squadra();
        nuovaSquadra.setNome("Squadra B");
        nuovaSquadra.setNumeroComponenti(2);
        nuovaSquadra.setNomeReferente("Referente B");
        nuovaSquadra.setSpecializzazione(Specializzazione.IDRAULICI);
        nuovaSquadra = squadraRepository.save(nuovaSquadra);

        String json = "{ \"squadraId\": \"" + nuovaSquadra.getId() + "\" }";

        mockMvc.perform(put("/api/fasi/" + fase.getId() + "/assegna-squadra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());
    }
}