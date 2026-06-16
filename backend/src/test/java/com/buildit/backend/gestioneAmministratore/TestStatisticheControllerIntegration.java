package com.buildit.backend.gestioneAmministratore;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.Fattura;
import com.buildit.backend.dominio.Preventivo;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFattura;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.DocumentoContabileRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb2",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class TestStatisticheControllerIntegration {

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
        cantiere.setNome("Cantiere Statistiche");
        cantiere.setIndirizzo("Via Test");
        cantiere.setDataInizioPrevista(LocalDate.now());
        cantiere.setDataFinePrevista(LocalDate.now().plusMonths(2));
        cantiere.setStato(StatoCantiere.IN_CORSO);
        cantiere = cantiereRepository.save(cantiere);
    }

    @Test
    public void testStatisticheCoerenzaFatturato() throws Exception {
        Fattura fatturaSaldata = new Fattura();
        fatturaSaldata.setNome("Fattura 1");
        fatturaSaldata.setImporto(10000.00);
        fatturaSaldata.setData(LocalDate.now());
        fatturaSaldata.setFileUrl("f1.pdf");
        fatturaSaldata.setCantiere(cantiere);
        fatturaSaldata.setStatoPagamento(StatoFattura.SALDATO);
        documentoContabileRepository.save(fatturaSaldata);

        Fattura fatturaDaSaldare = new Fattura();
        fatturaDaSaldare.setNome("Fattura 2");
        fatturaDaSaldare.setImporto(5000.00);
        fatturaDaSaldare.setData(LocalDate.now());
        fatturaDaSaldare.setFileUrl("f2.pdf");
        fatturaDaSaldare.setCantiere(cantiere);
        fatturaDaSaldare.setStatoPagamento(StatoFattura.DA_SALDARE);
        documentoContabileRepository.save(fatturaDaSaldare);

        mockMvc.perform(get("/api/statistiche"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fatturatoTotale").value(15000.00))
            .andExpect(jsonPath("$.fatturatoIncassato").value(10000.00))
            .andExpect(jsonPath("$.saldoDaIncassare").value(5000.00));
    }

    @Test
    public void testPreventivoNonContaNelFatturato() throws Exception {
        Preventivo preventivo = new Preventivo();
        preventivo.setNome("Preventivo");
        preventivo.setImporto(100000.00);
        preventivo.setData(LocalDate.now());
        preventivo.setFileUrl("prev.pdf");
        preventivo.setCantiere(cantiere);
        documentoContabileRepository.save(preventivo);

        mockMvc.perform(get("/api/statistiche"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fatturatoTotale").value(0.0));
    }

    @Test
    public void testContatoriCantieriNonNegativi() throws Exception {
        mockMvc.perform(get("/api/statistiche"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numeroCantieriAttivi").value(1))
            .andExpect(jsonPath("$.numeroCantieriInRitardo").value(0))
            .andExpect(jsonPath("$.numeroCantieriTerminati").value(0));
    }
}