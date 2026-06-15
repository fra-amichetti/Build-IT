package com.buildit.backend.config;

import com.buildit.backend.dominio.Amministratore;
import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.repository.UtenteRepository;

import java.time.LocalDate;
import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.repository.CantiereRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
   public CommandLineRunner loadData(UtenteRepository utenteRepository,
                                  PasswordEncoder passwordEncoder,
                                  CantiereRepository cantiereRepository) {
        return args -> {
            if (!utenteRepository.existsByEmail("admin1@buildit.it")) {
                Amministratore admin = new Amministratore();
                admin.setNome("Marco");
                admin.setCognome("Rossi");
                admin.setEmail("admin1@buildit.it");
                admin.setHashPassword(passwordEncoder.encode("Admin1234!"));
                admin.setNomeAzienda("BuildIT2 Srl");
                utenteRepository.save(admin);
                System.out.println("Admin creato!");
            }
            // Cantieri di test
if(cantiereRepository.findAll().isEmpty()){
    Cantiere c1 = new Cantiere();
    c1.setNome("Residenza Aurora");
    c1.setIndirizzo("Via Roma 12, Milano");
    c1.setDataInizioPrevista(LocalDate.of(2025, 1, 10));
    c1.setDataFinePrevista(LocalDate.of(2025, 12, 31));
    c1.setEmailCliente("mario.conti@email.it");
   c1.setStato(StatoCantiere.IN_CORSO);
    c1.setDataInizioEffettiva(LocalDate.of(2025, 1, 15));
    cantiereRepository.save(c1);

    Cantiere c2 = new Cantiere();
    c2.setNome("Palazzo Medici");
    c2.setIndirizzo("Corso Buenos Aires 45, Milano");
    c2.setDataInizioPrevista(LocalDate.of(2025, 3, 1));
    c2.setDataFinePrevista(LocalDate.of(2025, 6, 30));
    c2.setEmailCliente("luigi.ferrari@email.it");
   c2.setStato(StatoCantiere.IN_RITARDO);
    c2.setDataInizioEffettiva(LocalDate.of(2025, 3, 5));
    cantiereRepository.save(c2);

    Cantiere c3 = new Cantiere();
    c3.setNome("Villa Serena");
    c3.setIndirizzo("Via Garibaldi 8, Bergamo");
    c3.setDataInizioPrevista(LocalDate.of(2025, 9, 1));
    c3.setDataFinePrevista(LocalDate.of(2026, 6, 30));
    c3.setEmailCliente("mario.conti@email.it");
c3.setStato(StatoCantiere.PIANIFICATO);
    cantiereRepository.save(c3);

    Cantiere c4 = new Cantiere();
    c4.setNome("Centro Commerciale Nord");
    c4.setIndirizzo("Via Industriale 100, Monza");
    c4.setDataInizioPrevista(LocalDate.of(2024, 1, 1));
    c4.setDataFinePrevista(LocalDate.of(2024, 12, 31));
    c4.setEmailCliente("anna.bianchi@email.it");
   c4.setStato(StatoCantiere.TERMINATO);
    c4.setDataInizioEffettiva(LocalDate.of(2024, 1, 10));
    c4.setDataFineEffettiva(LocalDate.of(2024, 12, 20));
    cantiereRepository.save(c4);

    System.out.println("Cantieri di test creati!");
}
        };
    }
}