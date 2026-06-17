package com.buildit.backend.log;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementazione concreta di Logger.
 * Persiste ogni voce sul database ServerLog via JDBC e
 * scatena automaticamente la rilevazione di accessi sospetti
 * dopo ogni tentativo di login fallito.
 */
@Service
public class LoggerImpl implements Logger {

    private final LogJdbcRepository     logRepo;
    private final AccessoSospettoAnalyzer analyzer;

    public LoggerImpl(LogJdbcRepository logRepo, AccessoSospettoAnalyzer analyzer) {
        this.logRepo   = logRepo;
        this.analyzer  = analyzer;
    }

    @Override
    public void log(String emailUtente, TipoOperazione operazione,
                    String messaggio, EsitoOperazione esito) {
        LogEntry entry = LogEntry.of(emailUtente, operazione, messaggio, esito);
        logRepo.save(entry);

        // Analisi sospetti: attivata solo dopo un login fallito
        if (operazione == TipoOperazione.LOGIN_FALLITO) {
            analyzer.rilevaSospettoDopoFallimento(emailUtente, logRepo);
        }
    }

    @Override
    public List<LogEntry> getAll() {
        return logRepo.findAll();
    }

    @Override
    public List<LogEntry> filtra(String emailUtente, TipoOperazione operazione,
                                  LocalDate da, LocalDate a) {
        LocalDateTime from = da != null ? da.atStartOfDay()         : null;
        LocalDateTime to   = a  != null ? a.atTime(23, 59, 59)      : null;
        return logRepo.filtra(emailUtente, operazione, from, to);
    }

    @Override
    public List<LogEntry> getAccessiSospetti() {
        return logRepo.findBySospetto();
    }
}
