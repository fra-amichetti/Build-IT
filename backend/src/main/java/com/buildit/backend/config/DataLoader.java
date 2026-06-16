package com.buildit.backend.config;

import com.buildit.backend.dominio.Amministratore;
import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.DocumentoTecnico;
import com.buildit.backend.dominio.FaseLavorativa;
import com.buildit.backend.dominio.Fattura;
import com.buildit.backend.dominio.Preventivo;
import com.buildit.backend.dominio.Squadra;
import com.buildit.backend.dominio.Specializzazione;
import com.buildit.backend.dominio.StatoCantiere;
import com.buildit.backend.dominio.StatoFase;
import com.buildit.backend.dominio.StatoFattura;
import com.buildit.backend.repository.CantiereRepository;
import com.buildit.backend.repository.DocumentoContabileRepository;
import com.buildit.backend.repository.DocumentoTecnicoRepository;
import com.buildit.backend.repository.FaseLavorativaRepository;
import com.buildit.backend.repository.SquadraRepository;
import com.buildit.backend.repository.UtenteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(UtenteRepository utenteRepository,
                                      PasswordEncoder passwordEncoder,
                                      CantiereRepository cantiereRepository,
                                      FaseLavorativaRepository faseLavorativaRepository,
                                      SquadraRepository squadraRepository,
                                      DocumentoTecnicoRepository documentoTecnicoRepository,
                                      DocumentoContabileRepository documentoContabileRepository) {
        return args -> {

            // ── 1. AMMINISTRATORE ────────────────────────────────────────────────────
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

            // ── 2. CANTIERI ──────────────────────────────────────────────────────────
            if (cantiereRepository.findAll().isEmpty()) {
                Cantiere c1 = new Cantiere();
                c1.setNome("Residenza");
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

            // ── 3. SQUADRE ───────────────────────────────────────────────────────────
            if (squadraRepository.findAll().isEmpty()) {
                Squadra sq1 = new Squadra();
                sq1.setNome("Squadra Alpha");
                sq1.setSpecializzazione(Specializzazione.MURATORI);
                sq1.setNumeroComponenti(5);
                sq1.setNomeReferente("Mario Rossi");
                squadraRepository.save(sq1);

                Squadra sq2 = new Squadra();
                sq2.setNome("Squadra Beta");
                sq2.setSpecializzazione(Specializzazione.ELETTRICISTI);
                sq2.setNumeroComponenti(3);
                sq2.setNomeReferente("Luigi Bianchi");
                squadraRepository.save(sq2);

                Squadra sq3 = new Squadra();
                sq3.setNome("Squadra Gamma");
                sq3.setSpecializzazione(Specializzazione.IDRAULICI);
                sq3.setNumeroComponenti(4);
                sq3.setNomeReferente("Anna Verdi");
                squadraRepository.save(sq3);

                Squadra sq4 = new Squadra();
                sq4.setNome("Squadra Delta");
                sq4.setSpecializzazione(Specializzazione.CARPENTIERI);
                sq4.setNumeroComponenti(6);
                sq4.setNomeReferente("Carlo Neri");
                squadraRepository.save(sq4);

                System.out.println("Squadre di test create!");
            }

            // ── 4. FASI LAVORATIVE ───────────────────────────────────────────────────
            // Questo blocco viene eseguito solo se il DB delle fasi è vuoto.
            // Le squadre devono già esistere (create al passo 3).
            if (faseLavorativaRepository.findAll().isEmpty()) {
                List<Cantiere> cantieri = cantiereRepository.findAll();
                List<Squadra> squadre = squadraRepository.findAll();

                Optional<Cantiere> cResidenza = cantieri.stream()
                        .filter(c -> "Residenza".equals(c.getNome())).findFirst();
                Optional<Cantiere> cPalazzo = cantieri.stream()
                        .filter(c -> "Palazzo Medici".equals(c.getNome())).findFirst();
                Optional<Cantiere> cVilla = cantieri.stream()
                        .filter(c -> "Villa Serena".equals(c.getNome())).findFirst();
                Optional<Cantiere> cCentro = cantieri.stream()
                        .filter(c -> "Centro Commerciale Nord".equals(c.getNome())).findFirst();

                Optional<Squadra> sAlpha = squadre.stream()
                        .filter(s -> "Squadra Alpha".equals(s.getNome())).findFirst();
                Optional<Squadra> sBeta = squadre.stream()
                        .filter(s -> "Squadra Beta".equals(s.getNome())).findFirst();
                Optional<Squadra> sGamma = squadre.stream()
                        .filter(s -> "Squadra Gamma".equals(s.getNome())).findFirst();
                Optional<Squadra> sDelta = squadre.stream()
                        .filter(s -> "Squadra Delta".equals(s.getNome())).findFirst();

                // Residenza (IN_CORSO) — 4 fasi
                if (cResidenza.isPresent()) {
                    Cantiere c = cResidenza.get();

                    FaseLavorativa f1 = new FaseLavorativa();
                    f1.setNome("Fondamenta");
                    f1.setDescrizione("Scavo e getto delle fondamenta");
                    f1.setDataInizioPrevista(LocalDate.of(2025, 1, 15));
                    f1.setDataFinePrevista(LocalDate.of(2025, 3, 15));
                    f1.setDataInizioEffettiva(LocalDate.of(2025, 1, 20));
                    f1.setDataFineEffettiva(LocalDate.of(2025, 3, 10));
                    f1.setStato(StatoFase.TERMINATA);
                    f1.setCantiere(c);
                    sAlpha.ifPresent(f1::setSquadra);
                    faseLavorativaRepository.save(f1);

                    FaseLavorativa f2 = new FaseLavorativa();
                    f2.setNome("Struttura portante");
                    f2.setDescrizione("Costruzione muri portanti e solai");
                    f2.setDataInizioPrevista(LocalDate.of(2025, 3, 16));
                    f2.setDataFinePrevista(LocalDate.of(2025, 7, 31));
                    f2.setDataInizioEffettiva(LocalDate.of(2025, 3, 16));
                    f2.setStato(StatoFase.IN_CORSO);
                    f2.setCantiere(c);
                    sAlpha.ifPresent(f2::setSquadra);
                    faseLavorativaRepository.save(f2);

                    FaseLavorativa f3 = new FaseLavorativa();
                    f3.setNome("Impianti");
                    f3.setDescrizione("Impianti elettrici e idraulici");
                    f3.setDataInizioPrevista(LocalDate.of(2025, 8, 1));
                    f3.setDataFinePrevista(LocalDate.of(2025, 10, 31));
                    f3.setStato(StatoFase.PIANIFICATA);
                    f3.setCantiere(c);
                    sBeta.ifPresent(f3::setSquadra);
                    faseLavorativaRepository.save(f3);

                    FaseLavorativa f4 = new FaseLavorativa();
                    f4.setNome("Finiture interne");
                    f4.setDescrizione("Intonaci, pavimentazioni e rifinitura degli interni");
                    f4.setDataInizioPrevista(LocalDate.of(2025, 11, 1));
                    f4.setDataFinePrevista(LocalDate.of(2025, 12, 20));
                    f4.setStato(StatoFase.PIANIFICATA);
                    f4.setCantiere(c);
                    sDelta.ifPresent(f4::setSquadra);
                    faseLavorativaRepository.save(f4);
                }

                // Palazzo Medici (IN_RITARDO) — 3 fasi
                if (cPalazzo.isPresent()) {
                    Cantiere c = cPalazzo.get();

                    FaseLavorativa f5 = new FaseLavorativa();
                    f5.setNome("Fondamenta");
                    f5.setDescrizione("Scavo e getto delle fondamenta");
                    f5.setDataInizioPrevista(LocalDate.of(2025, 3, 5));
                    f5.setDataFinePrevista(LocalDate.of(2025, 4, 30));
                    f5.setDataInizioEffettiva(LocalDate.of(2025, 3, 5));
                    f5.setDataFineEffettiva(LocalDate.of(2025, 5, 10));
                    f5.setStato(StatoFase.TERMINATA);
                    f5.setCantiere(c);
                    sBeta.ifPresent(f5::setSquadra);
                    faseLavorativaRepository.save(f5);

                    FaseLavorativa f6 = new FaseLavorativa();
                    f6.setNome("Struttura portante");
                    f6.setDescrizione("Costruzione muri portanti e solai");
                    f6.setDataInizioPrevista(LocalDate.of(2025, 5, 1));
                    f6.setDataFinePrevista(LocalDate.of(2025, 6, 30));
                    f6.setDataInizioEffettiva(LocalDate.of(2025, 5, 15));
                    f6.setStato(StatoFase.IN_CORSO);
                    f6.setCantiere(c);
                    sAlpha.ifPresent(f6::setSquadra);
                    faseLavorativaRepository.save(f6);

                    FaseLavorativa f7 = new FaseLavorativa();
                    f7.setNome("Impianti e finiture");
                    f7.setDescrizione("Impianti, intonaci e pavimentazioni");
                    f7.setDataInizioPrevista(LocalDate.of(2025, 7, 1));
                    f7.setDataFinePrevista(LocalDate.of(2025, 9, 30));
                    f7.setStato(StatoFase.PIANIFICATA);
                    f7.setCantiere(c);
                    sGamma.ifPresent(f7::setSquadra);
                    faseLavorativaRepository.save(f7);
                }

                // Villa Serena (PIANIFICATO) — 3 fasi tutte pianificate
                if (cVilla.isPresent()) {
                    Cantiere c = cVilla.get();

                    FaseLavorativa f8 = new FaseLavorativa();
                    f8.setNome("Demolizioni e scavi");
                    f8.setDescrizione("Demolizione delle strutture esistenti e scavi");
                    f8.setDataInizioPrevista(LocalDate.of(2025, 9, 1));
                    f8.setDataFinePrevista(LocalDate.of(2025, 10, 31));
                    f8.setStato(StatoFase.PIANIFICATA);
                    f8.setCantiere(c);
                    sDelta.ifPresent(f8::setSquadra);
                    faseLavorativaRepository.save(f8);

                    FaseLavorativa f9 = new FaseLavorativa();
                    f9.setNome("Fondamenta e struttura");
                    f9.setDescrizione("Getto delle fondamenta e struttura portante");
                    f9.setDataInizioPrevista(LocalDate.of(2025, 11, 1));
                    f9.setDataFinePrevista(LocalDate.of(2026, 3, 31));
                    f9.setStato(StatoFase.PIANIFICATA);
                    f9.setCantiere(c);
                    sAlpha.ifPresent(f9::setSquadra);
                    faseLavorativaRepository.save(f9);

                    FaseLavorativa f10 = new FaseLavorativa();
                    f10.setNome("Finiture e impiantistica");
                    f10.setDescrizione("Impianti, finiture interne ed esterne");
                    f10.setDataInizioPrevista(LocalDate.of(2026, 4, 1));
                    f10.setDataFinePrevista(LocalDate.of(2026, 6, 30));
                    f10.setStato(StatoFase.PIANIFICATA);
                    f10.setCantiere(c);
                    sBeta.ifPresent(f10::setSquadra);
                    faseLavorativaRepository.save(f10);
                }

                // Centro Commerciale Nord (TERMINATO) — 4 fasi tutte terminate
                if (cCentro.isPresent()) {
                    Cantiere c = cCentro.get();

                    FaseLavorativa f11 = new FaseLavorativa();
                    f11.setNome("Fondamenta");
                    f11.setDescrizione("Scavo e getto delle fondamenta");
                    f11.setDataInizioPrevista(LocalDate.of(2024, 1, 10));
                    f11.setDataFinePrevista(LocalDate.of(2024, 3, 31));
                    f11.setDataInizioEffettiva(LocalDate.of(2024, 1, 10));
                    f11.setDataFineEffettiva(LocalDate.of(2024, 3, 28));
                    f11.setStato(StatoFase.TERMINATA);
                    f11.setCantiere(c);
                    sAlpha.ifPresent(f11::setSquadra);
                    faseLavorativaRepository.save(f11);

                    FaseLavorativa f12 = new FaseLavorativa();
                    f12.setNome("Struttura in acciaio");
                    f12.setDescrizione("Montaggio della struttura portante in acciaio");
                    f12.setDataInizioPrevista(LocalDate.of(2024, 4, 1));
                    f12.setDataFinePrevista(LocalDate.of(2024, 7, 31));
                    f12.setDataInizioEffettiva(LocalDate.of(2024, 4, 2));
                    f12.setDataFineEffettiva(LocalDate.of(2024, 7, 25));
                    f12.setStato(StatoFase.TERMINATA);
                    f12.setCantiere(c);
                    sDelta.ifPresent(f12::setSquadra);
                    faseLavorativaRepository.save(f12);

                    FaseLavorativa f13 = new FaseLavorativa();
                    f13.setNome("Impianti");
                    f13.setDescrizione("Impianti elettrici, idraulici e HVAC");
                    f13.setDataInizioPrevista(LocalDate.of(2024, 8, 1));
                    f13.setDataFinePrevista(LocalDate.of(2024, 10, 31));
                    f13.setDataInizioEffettiva(LocalDate.of(2024, 8, 1));
                    f13.setDataFineEffettiva(LocalDate.of(2024, 10, 28));
                    f13.setStato(StatoFase.TERMINATA);
                    f13.setCantiere(c);
                    sGamma.ifPresent(f13::setSquadra);
                    faseLavorativaRepository.save(f13);

                    FaseLavorativa f14 = new FaseLavorativa();
                    f14.setNome("Finiture e collaudo");
                    f14.setDescrizione("Pavimentazioni, finiture e collaudo finale");
                    f14.setDataInizioPrevista(LocalDate.of(2024, 11, 1));
                    f14.setDataFinePrevista(LocalDate.of(2024, 12, 20));
                    f14.setDataInizioEffettiva(LocalDate.of(2024, 11, 1));
                    f14.setDataFineEffettiva(LocalDate.of(2024, 12, 18));
                    f14.setStato(StatoFase.TERMINATA);
                    f14.setCantiere(c);
                    sBeta.ifPresent(f14::setSquadra);
                    faseLavorativaRepository.save(f14);
                }

                System.out.println("Fasi lavorative di test create!");
            }

            // ── 5. DOCUMENTI ─────────────────────────────────────────────────────────
            if (documentoTecnicoRepository.findAll().isEmpty()) {
                Optional<Cantiere> cDoc = cantiereRepository.findAll().stream()
                        .filter(c -> "Residenza".equals(c.getNome())).findFirst();

                if (cDoc.isPresent()) {
                    Cantiere c = cDoc.get();

                    DocumentoTecnico d1 = new DocumentoTecnico();
                    d1.setNome("Pianta piano terra");
                    d1.setTipologia("pianta");
                    d1.setFileUrl("https://example.com/pianta.pdf");
                    d1.setData(LocalDate.of(2025, 1, 20));
                    d1.setCantiere(c);
                    documentoTecnicoRepository.save(d1);

                    DocumentoTecnico d2 = new DocumentoTecnico();
                    d2.setNome("Permesso di costruzione");
                    d2.setTipologia("prospetto");
                    d2.setFileUrl("https://example.com/permesso.pdf");
                    d2.setData(LocalDate.of(2025, 1, 10));
                    d2.setCantiere(c);
                    documentoTecnicoRepository.save(d2);

                    Fattura fat1 = new Fattura();
                    fat1.setNome("Fattura acconto lavori");
                    fat1.setImporto(15000.0);
                    fat1.setFileUrl("https://example.com/fattura1.pdf");
                    fat1.setData(LocalDate.of(2025, 2, 1));
                    fat1.setStatoPagamento(StatoFattura.SALDATO);
                    fat1.setCantiere(c);
                    documentoContabileRepository.save(fat1);

                    Fattura fat2 = new Fattura();
                    fat2.setNome("Fattura SAL 1");
                    fat2.setImporto(25000.0);
                    fat2.setFileUrl("https://example.com/fattura2.pdf");
                    fat2.setData(LocalDate.of(2025, 4, 15));
                    fat2.setStatoPagamento(StatoFattura.DA_SALDARE);
                    fat2.setCantiere(c);
                    documentoContabileRepository.save(fat2);

                    Preventivo prev1 = new Preventivo();
                    prev1.setNome("Preventivo impianto elettrico");
                    prev1.setImporto(8000.0);
                    prev1.setFileUrl("https://example.com/preventivo1.pdf");
                    prev1.setData(LocalDate.of(2025, 1, 5));
                    prev1.setCantiere(c);
                    documentoContabileRepository.save(prev1);

                    System.out.println("Documenti di test creati!");
                }
            }
        };
    }
}
