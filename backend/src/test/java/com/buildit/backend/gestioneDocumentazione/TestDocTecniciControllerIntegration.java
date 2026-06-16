package com.buildit.backend.gestioneDocumentazione;

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

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.DocumentoTecnicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_doctecn",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class TestDocTecniciControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CantiereRepository cantiereRepository;

    @Autowired
    private DocumentoTecnicoRepository documentoTecnicoRepository;

    private Cantiere cantiere;

    @BeforeEach
    public void setUp() {
        documentoTecnicoRepository.deleteAll();
        cantiereRepository.deleteAll();

        cantiere = new Cantiere();
        cantiere.setNome("Cantiere Doc Tecnici");
        cantiere.setIndirizzo("Via Test");
        cantiere.setDataInizioPrevista(LocalDate.now());
        cantiere.setDataFinePrevista(LocalDate.now().plusMonths(2));
        cantiere.setStato(StatoCantiere.IN_CORSO);
        cantiere = cantiereRepository.save(cantiere);
    }

    @Test
    public void testAggiungiDocumentoTecnico() throws Exception {
        String json = """
            {
              "nome": "Pianta Piano Terra",
              "tipologia": "PIANTA",
              "data": "2026-06-15",
              "fileUrl": "/documents/pianta.pdf"
            }
            """;

        mockMvc.perform(post("/api/cantieri/" + cantiere.getId() + "/documenti-tecnici")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Pianta Piano Terra"));
    }

    @Test
    public void testEliminaDocumentoTecnicoNonEsistente() throws Exception {
        mockMvc.perform(delete("/api/cantieri/" + cantiere.getId() + "/documenti-tecnici/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testCantiereNonTrovato() throws Exception {
        mockMvc.perform(get("/api/cantieri/99999/documenti-tecnici"))
            .andExpect(status().isNotFound());
    }
}