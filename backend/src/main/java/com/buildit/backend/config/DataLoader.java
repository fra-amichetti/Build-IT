package com.buildit.backend.config;

import com.buildit.backend.dominio.Amministratore;
import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.repository.UtenteRepository;

import java.time.LocalDate;
import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.repository.CantiereRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.Specializzazione;
import com.buildit.backend.repository.SquadraRepository;

import java.util.Optional;
@Configuration
public class DataLoader {

    @Bean
 public CommandLineRunner loadData(UtenteRepository utenteRepository,
                                  PasswordEncoder passwordEncoder,
                                  CantiereRepository cantiereRepository,
                                  FaseLavorativaRepository faseLavorativaRepository,
                                  SquadraRepository squadraRepository) {
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
// Fasi di test
if (faseLavorativaRepository.findAll().isEmpty()) {
    Optional<Cantiere> c1 = cantiereRepository.findById(1L);
    Optional<Cantiere> c2 = cantiereRepository.findById(2L);

    if (c1.isPresent()) {
        FaseLavorativa f1 = new FaseLavorativa();
        f1.setNome("Fondamenta");
        f1.setDescrizione("Scavo e getto delle fondamenta");
        f1.setDataInizioPrevista(LocalDate.of(2025, 1, 15));
        f1.setDataFinePrevista(LocalDate.of(2025, 3, 15));
        f1.setDataInizioEffettiva(LocalDate.of(2025, 1, 20));
        f1.setDataFineEffettiva(LocalDate.of(2025, 3, 10));
        f1.setStato(StatoFase.TERMINATA);
        f1.setCantiere(c1.get());
        faseLavorativaRepository.save(f1);

        FaseLavorativa f2 = new FaseLavorativa();
        f2.setNome("Struttura portante");
        f2.setDescrizione("Costruzione muri e solai");
        f2.setDataInizioPrevista(LocalDate.of(2025, 3, 16));
        f2.setDataFinePrevista(LocalDate.of(2025, 7, 31));
        f2.setDataInizioEffettiva(LocalDate.of(2025, 3, 16));
        f2.setStato(StatoFase.IN_CORSO);
        f2.setCantiere(c1.get());
        faseLavorativaRepository.save(f2);

        FaseLavorativa f3 = new FaseLavorativa();
        f3.setNome("Impianti");
        f3.setDescrizione("Impianti elettrici e idraulici");
        f3.setDataInizioPrevista(LocalDate.of(2025, 8, 1));
        f3.setDataFinePrevista(LocalDate.of(2025, 10, 31));
        f3.setStato(StatoFase.PIANIFICATA);
        f3.setCantiere(c1.get());
        faseLavorativaRepository.save(f3);

        System.out.println("Fasi cantiere 1 create!");
    }

    if (c2.isPresent()) {
        FaseLavorativa f4 = new FaseLavorativa();
        f4.setNome("Fondamenta");
        f4.setDescrizione("Scavo e getto delle fondamenta");
        f4.setDataInizioPrevista(LocalDate.of(2025, 3, 5));
        f4.setDataFinePrevista(LocalDate.of(2025, 5, 31));
        f4.setDataInizioEffettiva(LocalDate.of(2025, 3, 5));
        f4.setStato(StatoFase.IN_CORSO);
        f4.setCantiere(c2.get());
        faseLavorativaRepository.save(f4);

        System.out.println("Fasi cantiere 2 create!");
    }
}// Squadre di test
if (squadraRepository.findAll().isEmpty()) {
    Squadra s1 = new Squadra();
    s1.setNome("Squadra Alpha");
    s1.setSpecializzazione(Specializzazione.MURATORI);
    s1.setNumeroComponenti(5);
    s1.setNomeReferente("Mario Rossi");
    squadraRepository.save(s1);

    Squadra s2 = new Squadra();
    s2.setNome("Squadra Beta");
    s2.setSpecializzazione(Specializzazione.ELETTRICISTI);
    s2.setNumeroComponenti(3);
    s2.setNomeReferente("Luigi Bianchi");
    squadraRepository.save(s2);

    Squadra s3 = new Squadra();
    s3.setNome("Squadra Gamma");
    s3.setSpecializzazione(Specializzazione.IDRAULICI);
    s3.setNumeroComponenti(4);
    s3.setNomeReferente("Anna Verdi");
    squadraRepository.save(s3);

    Squadra s4 = new Squadra();
    s4.setNome("Squadra Delta");
    s4.setSpecializzazione(Specializzazione.CARPENTIERI);
    s4.setNumeroComponenti(6);
    s4.setNomeReferente("Carlo Neri");
    squadraRepository.save(s4);

    System.out.println("Squadre di test create!");
}
        };
    }
}