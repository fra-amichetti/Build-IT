package com.buildit.backend.log;

import java.time.LocalDateTime;

/**
 * Rappresenta una singola voce del log.
 * POJO semplice — la persistenza è gestita da LogJdbcRepository via JDBC
 * sul database ServerLog separato.
 */
public class LogEntry {

    private Long id;
    private LocalDateTime timestamp;
    private String emailUtente;
    private TipoOperazione operazione;
    private String messaggio;
    private EsitoOperazione esito;

    public static LogEntry of(String email, TipoOperazione op, String msg, EsitoOperazione esito) {
        LogEntry e = new LogEntry();
        e.timestamp   = LocalDateTime.now();
        e.emailUtente = (email != null && !email.isBlank()) ? email : "SISTEMA";
        e.operazione  = op;
        e.messaggio   = msg != null ? msg : "";
        e.esito       = esito;
        return e;
    }

    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }

    public LocalDateTime getTimestamp()             { return timestamp; }
    public void setTimestamp(LocalDateTime ts)       { this.timestamp = ts; }

    public String getEmailUtente()                  { return emailUtente; }
    public void setEmailUtente(String email)        { this.emailUtente = email; }

    public TipoOperazione getOperazione()           { return operazione; }
    public void setOperazione(TipoOperazione op)    { this.operazione = op; }

    public String getMessaggio()                    { return messaggio; }
    public void setMessaggio(String msg)            { this.messaggio = msg; }

    public EsitoOperazione getEsito()               { return esito; }
    public void setEsito(EsitoOperazione esito)     { this.esito = esito; }
}
