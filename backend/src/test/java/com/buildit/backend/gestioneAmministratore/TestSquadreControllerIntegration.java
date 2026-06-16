package com.buildit.backend.gestioneAmministratore;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Specializzazione;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_squadre",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class TestSquadreControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SquadraRepository squadraRepository;

    @Autowired
    private FaseLavorativaRepository faseLavorativaRepository;

    @BeforeEach
    public void cleanUp() {
        faseLavorativaRepository.deleteAll();
        squadraRepository.deleteAll();
    }

    @Test
    public void testAggiungiSquadra() throws Exception {
        String json = """
            {
              "nome": "Squadra Test",
              "specializzazione": "MURATORI",
              "numeroComponenti": "3",
              "nomeReferente": "Referente Test"
            }
            """;

        mockMvc.perform(post("/api/squadre")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Squadra Test"));
    }

    @Test
    public void testEliminaSquadraDisponibile() throws Exception {
        Squadra squadra = new Squadra();
        squadra.setNome("Squadra Libera");
        squadra.setNumeroComponenti(3);
        squadra.setNomeReferente("Referente");
        squadra.setSpecializzazione(Specializzazione.MURATORI);
        squadra = squadraRepository.save(squadra);

        mockMvc.perform(delete("/api/squadre/" + squadra.getId()))
            .andExpect(status().isOk());
    }

    @Test
    public void testEliminaSquadraAssegnataAFaseAttiva() throws Exception {
        Squadra squadra = new Squadra();
        squadra.setNome("Squadra Occupata");
        squadra.setNumeroComponenti(4);
        squadra.setNomeReferente("Referente");
        squadra.setSpecializzazione(Specializzazione.IDRAULICI);
        squadra = squadraRepository.save(squadra);

        FaseLavorativa fase = new FaseLavorativa();
        fase.setNome("Fase attiva");
        fase.setDataInizioPrevista(LocalDate.now());
        fase.setDataFinePrevista(LocalDate.now().plusDays(10));
        fase.setStato(StatoFase.PIANIFICATA);
        fase.setSquadra(squadra);
        faseLavorativaRepository.save(fase);

        mockMvc.perform(delete("/api/squadre/" + squadra.getId()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errore").value("Squadra impegnata in fasi attive"));
    }

    @Test
    public void testGetSquadre() throws Exception {
        Squadra squadra = new Squadra();
        squadra.setNome("Squadra Lista");
        squadra.setNumeroComponenti(5);
        squadra.setNomeReferente("Referente");
        squadra.setSpecializzazione(Specializzazione.ELETTRICISTI);
        squadraRepository.save(squadra);

        mockMvc.perform(get("/api/squadre"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Squadra Lista"));
    }
}