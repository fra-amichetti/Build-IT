package com.buildit.backend.config;

import com.buildit.backend.dominio.*;
import com.buildit.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Popola il database con dati di prototipo che coprono tutte le casistiche:
 *   - 1 Amministratore, 3 Clienti (con account), 4 Dipendenti
 *   - 5 Cantieri: PIANIFICATO · IN_CORSO · IN_CORSO · IN_RITARDO · TERMINATO
 *   - 5 Squadre (tutte le specializzazioni)
 *   - Fasi in tutti gli stati: PIANIFICATA · IN_CORSO · TERMINATA
 *   - Documenti tecnici (tutte le 6 tipologie) e contabili (fatture + preventivi)
 */
@Configuration
public class DataLoader {

    private static final String PDF_SAMPLE = "https://www.w3.org/WAI/WCAG21/Techniques/pdf/sample.pdf";

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
            if (!utenteRepository.existsByEmail("admin@buildit.it")) {
                Amministratore admin = new Amministratore();
                admin.setNome("Marco");
                admin.setCognome("Rossi");
                admin.setEmail("admin@buildit.it");
                admin.setHashPassword(passwordEncoder.encode("Admin1234!"));
                admin.setNomeAzienda("BuildIT Srl");
                utenteRepository.save(admin);
            }

            // ── 2. CLIENTI ───────────────────────────────────────────────────────────
            // Ogni cliente può autenticarsi e vedere i propri cantieri.
            if (!utenteRepository.existsByEmail("mario.conti@email.it")) {
                Cliente c = new Cliente();
                c.setNome("Mario"); c.setCognome("Conti");
                c.setEmail("mario.conti@email.it");
                c.setHashPassword(passwordEncoder.encode("Cliente1!"));
                utenteRepository.save(c);
            }
            if (!utenteRepository.existsByEmail("luigi.ferrari@email.it")) {
                Cliente c = new Cliente();
                c.setNome("Luigi"); c.setCognome("Ferrari");
                c.setEmail("luigi.ferrari@email.it");
                c.setHashPassword(passwordEncoder.encode("Cliente1!"));
                utenteRepository.save(c);
            }
            if (!utenteRepository.existsByEmail("anna.bianchi@email.it")) {
                Cliente c = new Cliente();
                c.setNome("Anna"); c.setCognome("Bianchi");
                c.setEmail("anna.bianchi@email.it");
                c.setHashPassword(passwordEncoder.encode("Cliente1!"));
                utenteRepository.save(c);
            }

            // ── 3. DIPENDENTI ────────────────────────────────────────────────────────
            if (!utenteRepository.existsByEmail("luca.esposito@buildit.it")) {
                Dipendente d = new Dipendente();
                d.setNome("Luca"); d.setCognome("Esposito");
                d.setEmail("luca.esposito@buildit.it");
                d.setHashPassword(passwordEncoder.encode("Dip123456!"));
                d.setIncarico("Capocantiere");
                utenteRepository.save(d);
            }
            if (!utenteRepository.existsByEmail("sara.marino@buildit.it")) {
                Dipendente d = new Dipendente();
                d.setNome("Sara"); d.setCognome("Marino");
                d.setEmail("sara.marino@buildit.it");
                d.setHashPassword(passwordEncoder.encode("Dip123456!"));
                d.setIncarico("Geometra");
                utenteRepository.save(d);
            }
            if (!utenteRepository.existsByEmail("paolo.greco@buildit.it")) {
                Dipendente d = new Dipendente();
                d.setNome("Paolo"); d.setCognome("Greco");
                d.setEmail("paolo.greco@buildit.it");
                d.setHashPassword(passwordEncoder.encode("Dip123456!"));
                d.setIncarico("Direttore Tecnico");
                utenteRepository.save(d);
            }
            if (!utenteRepository.existsByEmail("elena.ricci@buildit.it")) {
                Dipendente d = new Dipendente();
                d.setNome("Elena"); d.setCognome("Ricci");
                d.setEmail("elena.ricci@buildit.it");
                d.setHashPassword(passwordEncoder.encode("Dip123456!"));
                d.setIncarico("Contabile");
                utenteRepository.save(d);
            }

            // ── 4. SQUADRE ───────────────────────────────────────────────────────────
            if (squadraRepository.findAll().isEmpty()) {
                squadraRepository.save(squadra("Squadra Alpha",   Specializzazione.MURATORI,     5, "Mario Fontana"));
                squadraRepository.save(squadra("Squadra Beta",    Specializzazione.ELETTRICISTI,  3, "Luigi Bianchi"));
                squadraRepository.save(squadra("Squadra Gamma",   Specializzazione.IDRAULICI,     4, "Anna Verdi"));
                squadraRepository.save(squadra("Squadra Delta",   Specializzazione.CARPENTIERI,   6, "Carlo Neri"));
                squadraRepository.save(squadra("Squadra Epsilon", Specializzazione.MURATORI,      4, "Giulia Serra"));
            }

            // ── 5. CANTIERI ──────────────────────────────────────────────────────────
            if (cantiereRepository.findAll().isEmpty()) {

                // PIANIFICATO — lavori non ancora iniziati
                Cantiere cVilla = new Cantiere();
                cVilla.setNome("Villa Serena");
                cVilla.setIndirizzo("Via Garibaldi 8, Bergamo");
                cVilla.setDataInizioPrevista(LocalDate.of(2026, 9, 1));
                cVilla.setDataFinePrevista(LocalDate.of(2027, 6, 30));
                cVilla.setEmailCliente("mario.conti@email.it");
                cVilla.setStato(StatoCantiere.PIANIFICATO);
                cantiereRepository.save(cVilla);

                // IN_CORSO — avanzamento regolare
                Cantiere cResidenza = new Cantiere();
                cResidenza.setNome("Residenza La Pace");
                cResidenza.setIndirizzo("Via Roma 12, Milano");
                cResidenza.setDataInizioPrevista(LocalDate.of(2025, 1, 10));
                cResidenza.setDataFinePrevista(LocalDate.of(2025, 12, 31));
                cResidenza.setEmailCliente("mario.conti@email.it");
                cResidenza.setStato(StatoCantiere.IN_CORSO);
                cResidenza.setDataInizioEffettiva(LocalDate.of(2025, 1, 15));
                cantiereRepository.save(cResidenza);

                // IN_CORSO — secondo cantiere attivo, cliente diverso
                Cantiere cTorre = new Cantiere();
                cTorre.setNome("Torre Uffici Milano");
                cTorre.setIndirizzo("Piazza Duca d'Aosta 6, Milano");
                cTorre.setDataInizioPrevista(LocalDate.of(2025, 4, 1));
                cTorre.setDataFinePrevista(LocalDate.of(2026, 3, 31));
                cTorre.setEmailCliente("luigi.ferrari@email.it");
                cTorre.setStato(StatoCantiere.IN_CORSO);
                cTorre.setDataInizioEffettiva(LocalDate.of(2025, 4, 7));
                cantiereRepository.save(cTorre);

                // IN_RITARDO — scaduto senza completamento
                Cantiere cPalazzo = new Cantiere();
                cPalazzo.setNome("Palazzo Medici");
                cPalazzo.setIndirizzo("Corso Buenos Aires 45, Milano");
                cPalazzo.setDataInizioPrevista(LocalDate.of(2025, 3, 1));
                cPalazzo.setDataFinePrevista(LocalDate.of(2025, 6, 30));
                cPalazzo.setEmailCliente("luigi.ferrari@email.it");
                cPalazzo.setStato(StatoCantiere.IN_RITARDO);
                cPalazzo.setDataInizioEffettiva(LocalDate.of(2025, 3, 5));
                cantiereRepository.save(cPalazzo);

                // TERMINATO — completato con successo
                Cantiere cCentro = new Cantiere();
                cCentro.setNome("Centro Commerciale Nord");
                cCentro.setIndirizzo("Via Industriale 100, Monza");
                cCentro.setDataInizioPrevista(LocalDate.of(2024, 1, 1));
                cCentro.setDataFinePrevista(LocalDate.of(2024, 12, 31));
                cCentro.setEmailCliente("anna.bianchi@email.it");
                cCentro.setStato(StatoCantiere.TERMINATO);
                cCentro.setDataInizioEffettiva(LocalDate.of(2024, 1, 10));
                cCentro.setDataFineEffettiva(LocalDate.of(2024, 12, 20));
                cantiereRepository.save(cCentro);

                System.out.println("Cantieri creati.");
            }

            // ── 6. FASI LAVORATIVE ───────────────────────────────────────────────────
            if (faseLavorativaRepository.findAll().isEmpty()) {
                List<Cantiere> cantieri = cantiereRepository.findAll();
                List<Squadra>  squadre  = squadraRepository.findAll();

                Cantiere cVilla     = find(cantieri, "Villa Serena");
                Cantiere cResidenza = find(cantieri, "Residenza La Pace");
                Cantiere cTorre     = find(cantieri, "Torre Uffici Milano");
                Cantiere cPalazzo   = find(cantieri, "Palazzo Medici");
                Cantiere cCentro    = find(cantieri, "Centro Commerciale Nord");

                Squadra sAlpha   = findS(squadre, "Squadra Alpha");
                Squadra sBeta    = findS(squadre, "Squadra Beta");
                Squadra sGamma   = findS(squadre, "Squadra Gamma");
                Squadra sDelta   = findS(squadre, "Squadra Delta");
                Squadra sEpsilon = findS(squadre, "Squadra Epsilon");

                // ── Villa Serena (PIANIFICATO) — 3 fasi tutte pianificate ────────────
                if (cVilla != null) {
                    fase(faseLavorativaRepository, "Demolizioni e scavi",
                         "Demolizione strutture esistenti e scavi preparatori",
                         LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31),
                         null, null, StatoFase.PIANIFICATA, cVilla, sDelta);

                    fase(faseLavorativaRepository, "Fondamenta e struttura",
                         "Getto delle fondamenta e costruzione struttura portante",
                         LocalDate.of(2026, 11, 1), LocalDate.of(2027, 3, 31),
                         null, null, StatoFase.PIANIFICATA, cVilla, sAlpha);

                    fase(faseLavorativaRepository, "Finiture e impiantistica",
                         "Impianti, finiture interne ed esterne",
                         LocalDate.of(2027, 4, 1), LocalDate.of(2027, 6, 30),
                         null, null, StatoFase.PIANIFICATA, cVilla, sBeta);
                }

                // ── Residenza La Pace (IN_CORSO) — 4 fasi: T · IC · P · P ───────────
                if (cResidenza != null) {
                    fase(faseLavorativaRepository, "Fondamenta",
                         "Scavo e getto delle fondamenta in c.a.",
                         LocalDate.of(2025, 1, 15), LocalDate.of(2025, 3, 15),
                         LocalDate.of(2025, 1, 20), LocalDate.of(2025, 3, 10),
                         StatoFase.TERMINATA, cResidenza, sAlpha);

                    fase(faseLavorativaRepository, "Struttura portante",
                         "Costruzione muri portanti e solai intermedi",
                         LocalDate.of(2025, 3, 16), LocalDate.of(2025, 7, 31),
                         LocalDate.of(2025, 3, 16), null,
                         StatoFase.IN_CORSO, cResidenza, sEpsilon);

                    fase(faseLavorativaRepository, "Impianti elettrici e idraulici",
                         "Posa impianti elettrici, idrosanitari e termici",
                         LocalDate.of(2025, 8, 1), LocalDate.of(2025, 10, 31),
                         null, null, StatoFase.PIANIFICATA, cResidenza, sBeta);

                    fase(faseLavorativaRepository, "Finiture interne",
                         "Intonaci, pavimentazioni, infissi e rifinitura ambienti",
                         LocalDate.of(2025, 11, 1), LocalDate.of(2025, 12, 20),
                         null, null, StatoFase.PIANIFICATA, cResidenza, sDelta);
                }

                // ── Torre Uffici Milano (IN_CORSO) — 4 fasi: T · T · IC · P ─────────
                if (cTorre != null) {
                    fase(faseLavorativaRepository, "Scavi e fondazioni",
                         "Scavi profondi e fondazioni su pali",
                         LocalDate.of(2025, 4, 7), LocalDate.of(2025, 6, 30),
                         LocalDate.of(2025, 4, 7), LocalDate.of(2025, 6, 25),
                         StatoFase.TERMINATA, cTorre, sAlpha);

                    fase(faseLavorativaRepository, "Struttura in acciaio",
                         "Montaggio struttura reticolare in acciaio",
                         LocalDate.of(2025, 7, 1), LocalDate.of(2025, 10, 31),
                         LocalDate.of(2025, 7, 1), LocalDate.of(2025, 10, 28),
                         StatoFase.TERMINATA, cTorre, sDelta);

                    fase(faseLavorativaRepository, "Facciata e copertura",
                         "Installazione facciata continua e impermeabilizzazione copertura",
                         LocalDate.of(2025, 11, 1), LocalDate.of(2026, 1, 31),
                         LocalDate.of(2025, 11, 3), null,
                         StatoFase.IN_CORSO, cTorre, sGamma);

                    fase(faseLavorativaRepository, "Impianti e allestimento",
                         "Impianti tecnologici, allestimento uffici e collaudo",
                         LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31),
                         null, null, StatoFase.PIANIFICATA, cTorre, sBeta);
                }

                // ── Palazzo Medici (IN_RITARDO) — 3 fasi: T · IC · P ────────────────
                if (cPalazzo != null) {
                    fase(faseLavorativaRepository, "Fondamenta",
                         "Scavo e getto fondamenta",
                         LocalDate.of(2025, 3, 5), LocalDate.of(2025, 4, 30),
                         LocalDate.of(2025, 3, 5), LocalDate.of(2025, 5, 15), // ritardo in uscita
                         StatoFase.TERMINATA, cPalazzo, sBeta);

                    fase(faseLavorativaRepository, "Struttura portante",
                         "Costruzione muri portanti e solai — in ritardo sul programma",
                         LocalDate.of(2025, 5, 1), LocalDate.of(2025, 6, 30),
                         LocalDate.of(2025, 5, 20), null,  // inizio in ritardo, ancora aperta
                         StatoFase.IN_CORSO, cPalazzo, sAlpha);

                    fase(faseLavorativaRepository, "Impianti e finiture",
                         "Impianti, intonaci e pavimentazioni",
                         LocalDate.of(2025, 7, 1), LocalDate.of(2025, 9, 30),
                         null, null, StatoFase.PIANIFICATA, cPalazzo, sGamma);
                }

                // ── Centro Commerciale Nord (TERMINATO) — 4 fasi tutte terminate ─────
                if (cCentro != null) {
                    fase(faseLavorativaRepository, "Fondamenta",
                         "Scavo e getto delle fondamenta",
                         LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 31),
                         LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 28),
                         StatoFase.TERMINATA, cCentro, sAlpha);

                    fase(faseLavorativaRepository, "Struttura in acciaio",
                         "Montaggio struttura portante in acciaio",
                         LocalDate.of(2024, 4, 1), LocalDate.of(2024, 7, 31),
                         LocalDate.of(2024, 4, 2), LocalDate.of(2024, 7, 25),
                         StatoFase.TERMINATA, cCentro, sDelta);

                    fase(faseLavorativaRepository, "Impianti",
                         "Impianti elettrici, idraulici e HVAC",
                         LocalDate.of(2024, 8, 1), LocalDate.of(2024, 10, 31),
                         LocalDate.of(2024, 8, 1), LocalDate.of(2024, 10, 28),
                         StatoFase.TERMINATA, cCentro, sGamma);

                    fase(faseLavorativaRepository, "Finiture e collaudo",
                         "Pavimentazioni, finiture e collaudo finale",
                         LocalDate.of(2024, 11, 1), LocalDate.of(2024, 12, 20),
                         LocalDate.of(2024, 11, 1), LocalDate.of(2024, 12, 18),
                         StatoFase.TERMINATA, cCentro, sBeta);
                }

                System.out.println("Fasi create.");
            }

            // ── 7. DOCUMENTI ─────────────────────────────────────────────────────────
            if (documentoTecnicoRepository.findAll().isEmpty()) {
                List<Cantiere> cantieri = cantiereRepository.findAll();

                Cantiere cResidenza = find(cantieri, "Residenza La Pace");
                Cantiere cTorre     = find(cantieri, "Torre Uffici Milano");
                Cantiere cPalazzo   = find(cantieri, "Palazzo Medici");
                Cantiere cCentro    = find(cantieri, "Centro Commerciale Nord");

                // ── Residenza La Pace — tutte le 6 tipologie tecniche ────────────────
                if (cResidenza != null) {
                    docTecnico(documentoTecnicoRepository, "Pianta piano terra",           "PIANTA",     LocalDate.of(2025, 1, 20), cResidenza);
                    docTecnico(documentoTecnicoRepository, "Prospetto frontale",            "PROSPETTO",  LocalDate.of(2025, 1, 20), cResidenza);
                    docTecnico(documentoTecnicoRepository, "Foto stato avanzamento - Marzo","FOTO",       LocalDate.of(2025, 3, 15), cResidenza);
                    docTecnico(documentoTecnicoRepository, "Permesso di costruire",         "PERMESSO",   LocalDate.of(2025, 1, 10), cResidenza);
                    docTecnico(documentoTecnicoRepository, "Relazione geologica",           "RELAZIONE",  LocalDate.of(2025, 1, 8),  cResidenza);
                    docTecnico(documentoTecnicoRepository, "Capitolato d'appalto",          "ALTRO",      LocalDate.of(2025, 1, 5),  cResidenza);
                }

                // ── Torre Uffici Milano — mix tipologie ──────────────────────────────
                if (cTorre != null) {
                    docTecnico(documentoTecnicoRepository, "Pianta piano tipo",             "PIANTA",     LocalDate.of(2025, 4, 1),  cTorre);
                    docTecnico(documentoTecnicoRepository, "Progetto strutturale",          "RELAZIONE",  LocalDate.of(2025, 3, 28), cTorre);
                    docTecnico(documentoTecnicoRepository, "Autorizzazione sismica",        "PERMESSO",   LocalDate.of(2025, 3, 20), cTorre);
                    docTecnico(documentoTecnicoRepository, "Foto cantiere - Luglio",        "FOTO",       LocalDate.of(2025, 7, 31), cTorre);
                }

                // ── Palazzo Medici — documenti essenziali ────────────────────────────
                if (cPalazzo != null) {
                    docTecnico(documentoTecnicoRepository, "Rilievo planimetrico",          "PIANTA",     LocalDate.of(2025, 3, 1),  cPalazzo);
                    docTecnico(documentoTecnicoRepository, "Relazione tecnica variante",    "RELAZIONE",  LocalDate.of(2025, 6, 1),  cPalazzo);
                }

                // ── Centro Commerciale Nord — archivio completato ────────────────────
                if (cCentro != null) {
                    docTecnico(documentoTecnicoRepository, "Pianta piano terra",            "PIANTA",     LocalDate.of(2024, 1, 5),  cCentro);
                    docTecnico(documentoTecnicoRepository, "Prospetto principale",          "PROSPETTO",  LocalDate.of(2024, 1, 5),  cCentro);
                    docTecnico(documentoTecnicoRepository, "Verbale collaudo finale",       "RELAZIONE",  LocalDate.of(2024, 12, 18),cCentro);
                    docTecnico(documentoTecnicoRepository, "Foto inaugurazione",            "FOTO",       LocalDate.of(2024, 12, 20),cCentro);
                }

                System.out.println("Documenti tecnici creati.");
            }

            if (documentoContabileRepository.findAll().isEmpty()) {
                List<Cantiere> cantieri = cantiereRepository.findAll();

                Cantiere cResidenza = find(cantieri, "Residenza La Pace");
                Cantiere cTorre     = find(cantieri, "Torre Uffici Milano");
                Cantiere cPalazzo   = find(cantieri, "Palazzo Medici");
                Cantiere cCentro    = find(cantieri, "Centro Commerciale Nord");

                // ── Residenza La Pace — fatture miste + preventivo ───────────────────
                if (cResidenza != null) {
                    preventivo(documentoContabileRepository, "Preventivo impianto elettrico", 8_500.0,  LocalDate.of(2025, 1, 5),  cResidenza);
                    preventivo(documentoContabileRepository, "Preventivo impianto idraulico",  6_200.0,  LocalDate.of(2025, 1, 6),  cResidenza);
                    fattura(documentoContabileRepository,   "Fattura acconto lavori",         15_000.0, LocalDate.of(2025, 2, 1),  StatoFattura.SALDATO,     cResidenza);
                    fattura(documentoContabileRepository,   "Fattura SAL 1 — fondamenta",     28_000.0, LocalDate.of(2025, 4, 1),  StatoFattura.SALDATO,     cResidenza);
                    fattura(documentoContabileRepository,   "Fattura SAL 2 — struttura",      35_000.0, LocalDate.of(2025, 7, 15), StatoFattura.DA_SALDARE,  cResidenza);
                    fattura(documentoContabileRepository,   "Fattura finale stimata",         42_000.0, LocalDate.of(2025, 12, 20),StatoFattura.DA_SALDARE,  cResidenza);
                }

                // ── Torre Uffici Milano — grande commessa ────────────────────────────
                if (cTorre != null) {
                    preventivo(documentoContabileRepository, "Preventivo struttura acciaio",  120_000.0, LocalDate.of(2025, 3, 20), cTorre);
                    preventivo(documentoContabileRepository, "Preventivo facciata continua",   85_000.0, LocalDate.of(2025, 3, 22), cTorre);
                    fattura(documentoContabileRepository,   "Acconto contratto",               50_000.0, LocalDate.of(2025, 4, 15), StatoFattura.SALDATO,    cTorre);
                    fattura(documentoContabileRepository,   "SAL 1 — fondazioni",              75_000.0, LocalDate.of(2025, 7, 1),  StatoFattura.SALDATO,    cTorre);
                    fattura(documentoContabileRepository,   "SAL 2 — struttura acciaio",       80_000.0, LocalDate.of(2025, 11, 1), StatoFattura.DA_SALDARE, cTorre);
                    fattura(documentoContabileRepository,   "SAL 3 — facciata",                90_000.0, LocalDate.of(2026, 2, 1),  StatoFattura.DA_SALDARE, cTorre);
                }

                // ── Palazzo Medici — ritardi si riflettono sulle fatture ─────────────
                if (cPalazzo != null) {
                    preventivo(documentoContabileRepository, "Preventivo lavori completi",     65_000.0, LocalDate.of(2025, 2, 28), cPalazzo);
                    fattura(documentoContabileRepository,   "Acconto iniziale",                20_000.0, LocalDate.of(2025, 3, 10), StatoFattura.SALDATO,    cPalazzo);
                    fattura(documentoContabileRepository,   "SAL 1 — fondamenta",              22_000.0, LocalDate.of(2025, 6, 1),  StatoFattura.DA_SALDARE, cPalazzo);
                }

                // ── Centro Commerciale Nord — commessa chiusa, tutto saldato ─────────
                if (cCentro != null) {
                    preventivo(documentoContabileRepository, "Preventivo iniziale",           200_000.0, LocalDate.of(2023, 12, 10), cCentro);
                    fattura(documentoContabileRepository,   "Acconto contratto",               60_000.0, LocalDate.of(2024, 1, 15), StatoFattura.SALDATO,    cCentro);
                    fattura(documentoContabileRepository,   "SAL 1 — fondazioni",              55_000.0, LocalDate.of(2024, 4, 5),  StatoFattura.SALDATO,    cCentro);
                    fattura(documentoContabileRepository,   "SAL 2 — struttura",               58_000.0, LocalDate.of(2024, 8, 1),  StatoFattura.SALDATO,    cCentro);
                    fattura(documentoContabileRepository,   "Saldo finale",                    40_000.0, LocalDate.of(2024, 12, 22),StatoFattura.SALDATO,    cCentro);
                }

                System.out.println("Documenti contabili creati.");
            }

            System.out.println("=== DataLoader completato ===");
        };
    }

    // ── helper: Squadra ──────────────────────────────────────────────────────────

    private Squadra squadra(String nome, Specializzazione spec, int componenti, String referente) {
        Squadra s = new Squadra();
        s.setNome(nome); s.setSpecializzazione(spec);
        s.setNumeroComponenti(componenti); s.setNomeReferente(referente);
        return s;
    }

    // ── helper: FaseLavorativa ───────────────────────────────────────────────────

    private void fase(FaseLavorativaRepository repo,
                      String nome, String descrizione,
                      LocalDate inizioPrev, LocalDate finePrev,
                      LocalDate inizioEff, LocalDate fineEff,
                      StatoFase stato, Cantiere cantiere, Squadra squadra) {
        FaseLavorativa f = new FaseLavorativa();
        f.setNome(nome); f.setDescrizione(descrizione);
        f.setDataInizioPrevista(inizioPrev); f.setDataFinePrevista(finePrev);
        f.setDataInizioEffettiva(inizioEff); f.setDataFineEffettiva(fineEff);
        f.setStato(stato); f.setCantiere(cantiere);
        if (squadra != null) f.setSquadra(squadra);
        repo.save(f);
    }

    // ── helper: DocumentoTecnico ─────────────────────────────────────────────────

    private void docTecnico(DocumentoTecnicoRepository repo,
                             String nome, String tipologia,
                             LocalDate data, Cantiere cantiere) {
        DocumentoTecnico d = new DocumentoTecnico();
        d.setNome(nome); d.setTipologia(tipologia);
        d.setFileUrl(PDF_SAMPLE); d.setData(data); d.setCantiere(cantiere);
        repo.save(d);
    }

    // ── helper: Fattura ──────────────────────────────────────────────────────────

    private void fattura(DocumentoContabileRepository repo,
                         String nome, double importo, LocalDate data,
                         StatoFattura stato, Cantiere cantiere) {
        Fattura f = new Fattura();
        f.setNome(nome); f.setImporto(importo);
        f.setFileUrl(PDF_SAMPLE); f.setData(data);
        f.setStatoPagamento(stato); f.setCantiere(cantiere);
        repo.save(f);
    }

    // ── helper: Preventivo ───────────────────────────────────────────────────────

    private void preventivo(DocumentoContabileRepository repo,
                             String nome, double importo, LocalDate data,
                             Cantiere cantiere) {
        Preventivo p = new Preventivo();
        p.setNome(nome); p.setImporto(importo);
        p.setFileUrl(PDF_SAMPLE); p.setData(data); p.setCantiere(cantiere);
        repo.save(p);
    }

    // ── utility ──────────────────────────────────────────────────────────────────

    private static Cantiere find(List<Cantiere> list, String nome) {
        return list.stream().filter(c -> nome.equals(c.getNome())).findFirst().orElse(null);
    }

    private static Squadra findS(List<Squadra> list, String nome) {
        return list.stream().filter(s -> nome.equals(s.getNome())).findFirst().orElse(null);
    }
}
