package com.buildit.backend.gestioneCantieri;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.repository.CantiereRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class TestCantiereControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CantiereRepository cantiereRepository;

    @BeforeEach
    public void cleanUp() {
        cantiereRepository.deleteAll();
    }

    @Test
    public void testAggiungiCantiere() throws Exception {
        String json = """
            {
              "nome": "Cantiere Test",
              "indirizzo": "Via Test 1",
              "dataInizioPrevista": "2026-07-01",
              "dataFinePrevista": "2026-12-31",
              "emailCliente": "test@email.it"
            }
            """;

        mockMvc.perform(post("/api/cantieri")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Cantiere Test"))
            .andExpect(jsonPath("$.stato").value("PIANIFICATO"));
    }

    @Test
    public void testAggiungiCantiereDataFineNonValida() throws Exception {
        String json = """
            {
              "nome": "Cantiere Invalido",
              "indirizzo": "Via Test 2",
              "dataInizioPrevista": "2026-12-31",
              "dataFinePrevista": "2026-01-01"
            }
            """;

        mockMvc.perform(post("/api/cantieri")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testIniziaLavoriCantiere() throws Exception {
        Cantiere c = new Cantiere();
        c.setNome("Cantiere da avviare");
        c.setIndirizzo("Via Test 3");
        c.setDataInizioPrevista(LocalDate.now());
        c.setDataFinePrevista(LocalDate.now().plusMonths(3));
        c.setStato(StatoCantiere.PIANIFICATO);
        c = cantiereRepository.save(c);

        mockMvc.perform(put("/api/cantieri/" + c.getId() + "/avvia"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stato").value("IN_CORSO"));
    }

    @Test
    public void testTerminaCantierePianificatoFallisce() throws Exception {
        Cantiere c = new Cantiere();
        c.setNome("Cantiere mai avviato");
        c.setIndirizzo("Via Test 4");
        c.setDataInizioPrevista(LocalDate.now());
        c.setDataFinePrevista(LocalDate.now().plusMonths(3));
        c.setStato(StatoCantiere.PIANIFICATO);
        c = cantiereRepository.save(c);

        mockMvc.perform(put("/api/cantieri/" + c.getId() + "/termina"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetElencoCantieri() throws Exception {
        Cantiere c = new Cantiere();
        c.setNome("Cantiere Lista");
        c.setIndirizzo("Via Test 5");
        c.setDataInizioPrevista(LocalDate.now());
        c.setDataFinePrevista(LocalDate.now().plusMonths(3));
        c.setStato(StatoCantiere.PIANIFICATO);
        cantiereRepository.save(c);

        mockMvc.perform(get("/api/cantieri"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Cantiere Lista"));
    }

    @Test
    public void testCantiereNonTrovato() throws Exception {
        mockMvc.perform(get("/api/cantieri/99999"))
            .andExpect(status().isNotFound());
    }
}