package com.buildit.backend.log;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaccia per il sistema di logging dell'applicazione.
 * Astrae la scrittura e la lettura dei log, indipendentemente
 * dal meccanismo di persistenza sottostante.
 */
public interface Logger {

    /**
     * Registra un'operazione nel log.
     *
     * @param emailUtente email dell'utente che ha eseguito l'operazione
     * @param operazione  tipo di operazione eseguita
     * @param messaggio   descrizione dell'operazione
     * @param esito       esito dell'operazione
     */
    void log(String emailUtente, TipoOperazione operazione, String messaggio, EsitoOperazione esito);

    /** Restituisce tutte le voci del log ordinate per timestamp decrescente. */
    List<LogEntry> getAll();

    /**
     * Filtra il log in base ai criteri forniti (tutti opzionali).
     *
     * @param emailUtente filtro parziale sull'email (case-insensitive)
     * @param operazione  filtro esatto sul tipo di operazione
     * @param da          data di inizio del periodo (inclusa)
     * @param a           data di fine del periodo (inclusa)
     */
    List<LogEntry> filtra(String emailUtente, TipoOperazione operazione, LocalDate da, LocalDate a);

    /** Restituisce solo le voci con esito SOSPETTO. */
    List<LogEntry> getAccessiSospetti();
}
