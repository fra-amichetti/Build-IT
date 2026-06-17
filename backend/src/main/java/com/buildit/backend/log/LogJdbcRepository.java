package com.buildit.backend.log;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository JDBC per la persistenza dei log sul database ServerLog separato.
 * Usa il logJdbcTemplate che punta alla sorgente dati secondaria.
 */
@Repository
public class LogJdbcRepository {

    private static final String INSERT =
        "INSERT INTO log_entries (timestamp, email_utente, operazione, messaggio, esito) " +
        "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_ALL =
        "SELECT * FROM log_entries ORDER BY timestamp DESC";

    private static final String COUNT_FALLITI =
        "SELECT COUNT(*) FROM log_entries " +
        "WHERE email_utente = ? AND operazione = 'LOGIN_FALLITO' AND timestamp >= ?";

    private final JdbcTemplate jdbc;

    public LogJdbcRepository(@Qualifier("logJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(LogEntry entry) {
        jdbc.update(INSERT,
            Timestamp.valueOf(entry.getTimestamp()),
            entry.getEmailUtente(),
            entry.getOperazione().name(),
            entry.getMessaggio(),
            entry.getEsito().name());
    }

    public List<LogEntry> findAll() {
        return jdbc.query(SELECT_ALL, rowMapper());
    }

    /**
     * Filtra con tutti i parametri opzionali.
     * Costruisce la query dinamicamente per evitare SQL injection.
     */
    public List<LogEntry> filtra(String email, TipoOperazione operazione,
                                  LocalDateTime da, LocalDateTime a) {
        StringBuilder sql = new StringBuilder("SELECT * FROM log_entries WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (email != null && !email.isBlank()) {
            sql.append(" AND LOWER(email_utente) LIKE LOWER(?)");
            params.add("%" + email + "%");
        }
        if (operazione != null) {
            sql.append(" AND operazione = ?");
            params.add(operazione.name());
        }
        if (da != null) {
            sql.append(" AND timestamp >= ?");
            params.add(Timestamp.valueOf(da));
        }
        if (a != null) {
            sql.append(" AND timestamp <= ?");
            params.add(Timestamp.valueOf(a));
        }
        sql.append(" ORDER BY timestamp DESC");

        return jdbc.query(sql.toString(), rowMapper(), params.toArray());
    }

    public List<LogEntry> findBySospetto() {
        return jdbc.query(
            "SELECT * FROM log_entries WHERE esito = 'SOSPETTO' ORDER BY timestamp DESC",
            rowMapper());
    }

    /** Conta i tentativi di login falliti per un'email nelle ultime N ore. */
    public int contaLoginFalliti(String email, LocalDateTime da) {
        Integer count = jdbc.queryForObject(COUNT_FALLITI, Integer.class,
            email, Timestamp.valueOf(da));
        return count != null ? count : 0;
    }

    private RowMapper<LogEntry> rowMapper() {
        return (rs, rowNum) -> {
            LogEntry e = new LogEntry();
            e.setId(rs.getLong("id"));
            e.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            e.setEmailUtente(rs.getString("email_utente"));
            e.setOperazione(TipoOperazione.valueOf(rs.getString("operazione")));
            e.setMessaggio(rs.getString("messaggio"));
            e.setEsito(EsitoOperazione.valueOf(rs.getString("esito")));
            return e;
        };
    }
}
