package com.buildit.backend.gestioneAmministratore;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buildit.backend.repository.UtenteRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_dip",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class TestDipendentiControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @BeforeEach
    public void cleanUp() {
        utenteRepository.deleteAll();
    }

    @Test
    public void testAggiungiDipendente() throws Exception {
        String json = """
            {
              "nome": "Paolo",
              "cognome": "Verdi",
              "email": "paolo@buildit.it",
              "password": "Password1!",
              "incarico": "Operaio"
            }
            """;

        mockMvc.perform(post("/api/dipendenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.messaggio").value("Dipendente aggiunto"));
    }

    @Test
    public void testAggiungiDipendenteEmailDuplicata() throws Exception {
        String json = """
            {
              "nome": "Paolo",
              "cognome": "Verdi",
              "email": "dup@buildit.it",
              "password": "Password1!",
              "incarico": "Operaio"
            }
            """;

        mockMvc.perform(post("/api/dipendenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());

        String json2 = """
            {
              "nome": "Marco",
              "cognome": "Rossi",
              "email": "dup@buildit.it",
              "password": "Password2!",
              "incarico": "Operaio"
            }
            """;

        mockMvc.perform(post("/api/dipendenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errore").value("Email già registrata"));
    }

    @Test
    public void testGetDipendenti() throws Exception {
        String json = """
            {
              "nome": "Anna",
              "cognome": "Bianchi",
              "email": "anna@buildit.it",
              "password": "Password1!",
              "incarico": "Geometra"
            }
            """;
        mockMvc.perform(post("/api/dipendenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        mockMvc.perform(get("/api/dipendenti"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("anna@buildit.it"));
    }
}