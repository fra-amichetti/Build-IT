package com.buildit.backend.log;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Analizza i log alla ricerca di pattern di accesso sospetti:
 *  - 5+ tentativi di login falliti nella stessa ora dallo stesso utente
 *  - Login effettuato fuori dall'orario normale (07:00 – 22:00)
 */
@Component
public class AccessoSospettoAnalyzer {

    /** Soglia: oltre questa soglia di fallimenti nell'ultima ora → SOSPETTO */
    public static final int SOGLIA_SOSPETTO = 5;

    private static final int ORA_INIZIO_NORMALE = 7;
    private static final int ORA_FINE_NORMALE   = 22;

    /**
     * Verifica se l'email ha superato la soglia di fallimenti nell'ultima ora.
     * Se sì, persiste direttamente una voce ACCESSO_SOSPETTO nel repository.
     */
    public void rilevaSospettoDopoFallimento(String email, LogJdbcRepository repo) {
        int conteggio = repo.contaLoginFalliti(email, LocalDateTime.now().minusHours(1));
        if (conteggio >= SOGLIA_SOSPETTO) {
            LogEntry sospetto = LogEntry.of(
                email,
                TipoOperazione.ACCESSO_SOSPETTO,
                "Rilevati " + conteggio + " tentativi di login falliti nell'ultima ora",
                EsitoOperazione.SOSPETTO
            );
            repo.save(sospetto);
        }
    }

    /**
     * Restituisce le voci che indicano accesso anomalo per orario.
     * Da usare per arricchire la visualizzazione del log.
     */
    public List<LogEntry> filtraFuoriOrario(List<LogEntry> tutti) {
        return tutti.stream()
            .filter(e -> isFuoriOrario(e.getTimestamp()))
            .toList();
    }

    public boolean isFuoriOrario(LocalDateTime ts) {
        int ora = ts.getHour();
        return ora < ORA_INIZIO_NORMALE || ora > ORA_FINE_NORMALE;
    }
}
