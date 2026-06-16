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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.Fattura;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFattura;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.DocumentoContabileRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb_doccontab",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class TestDocContabiliControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CantiereRepository cantiereRepository;

    @Autowired
    private DocumentoContabileRepository documentoContabileRepository;

    private Cantiere cantiere;

    @BeforeEach
    public void setUp() {
        documentoContabileRepository.deleteAll();
        cantiereRepository.deleteAll();

        cantiere = new Cantiere();
        cantiere.setNome("Cantiere Doc Contabili");
        cantiere.setIndirizzo("Via Test");
        cantiere.setDataInizioPrevista(LocalDate.now());
        cantiere.setDataFinePrevista(LocalDate.now().plusMonths(2));
        cantiere.setStato(StatoCantiere.IN_CORSO);
        cantiere = cantiereRepository.save(cantiere);
    }

    @Test
    public void testAggiungiFattura() throws Exception {
        String json = """
            {
              "nome": "Fattura Lavori",
              "tipo": "Fattura",
              "importo": "5000.0",
              "data": "2026-06-15",
              "fileUrl": "/documents/fattura.pdf"
            }
            """;

        mockMvc.perform(post("/api/cantieri/" + cantiere.getId() + "/documenti-contabili")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Fattura Lavori"))
            .andExpect(jsonPath("$.statoPagamento").value("DA_SALDARE"));
    }

    @Test
    public void testAggiungiPreventivo() throws Exception {
        String json = """
            {
              "nome": "Preventivo Iniziale",
              "tipo": "Preventivo",
              "importo": "80000.0",
              "data": "2026-01-10",
              "fileUrl": "/documents/preventivo.pdf"
            }
            """;

        mockMvc.perform(post("/api/cantieri/" + cantiere.getId() + "/documenti-contabili")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Preventivo Iniziale"));
    }

    @Test
    public void testSaldaFattura() throws Exception {
        Fattura fattura = new Fattura();
        fattura.setNome("Fattura da saldare");
        fattura.setImporto(3000.0);
        fattura.setData(LocalDate.now());
        fattura.setFileUrl("/documents/f.pdf");
        fattura.setCantiere(cantiere);
        fattura.setStatoPagamento(StatoFattura.DA_SALDARE);
        fattura = (Fattura) documentoContabileRepository.save(fattura);

        mockMvc.perform(put("/api/cantieri/" + cantiere.getId() + "/documenti-contabili/" + fattura.getId() + "/salda"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statoPagamento").value("SALDATO"));
    }

    @Test
    public void testDocumentoContabileCantiereNonTrovato() throws Exception {
        mockMvc.perform(get("/api/cantieri/99999/documenti-contabili"))
            .andExpect(status().isNotFound());
    }
}