package com.buildit.backend.config;

import com.buildit.backend.log.LogJdbcRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configurazione del database separato "ServerLog" per la persistenza dei log.
 *
 * Architettura:
 *   - Il database principale (Supabase) gestisce le entità applicative (JPA).
 *   - Il database ServerLog gestisce solo i log di sistema (JDBC puro).
 *
 * Per usare un vero DB separato:
 *   1. Crea un secondo progetto Supabase (o un server PostgreSQL locale).
 *   2. Aggiorna app.log-datasource.url/username/password in application.properties.
 *   3. La tabella log_entries verrà creata automaticamente al primo avvio.
 *
 * NOTA IMPORTANTE: il logJdbcTemplate è definito come bean di tipo JdbcTemplate
 * (NON di tipo DataSource) per evitare conflitti con l'auto-configurazione Spring Boot
 * del DataSource principale. La connessione HikariCP è creata internamente.
 */
@Configuration
public class LogDataSourceConfig {

    @Value("${app.log-datasource.url}")
    private String url;

    @Value("${app.log-datasource.username}")
    private String username;

    @Value("${app.log-datasource.password}")
    private String password;

    /**
     * Pool di connessioni dedicato al database ServerLog.
     * Restituisce JdbcTemplate (non DataSource) per non interferire con
     * Spring Boot DataSourceAutoConfiguration.
     */
    @Bean("logJdbcTemplate")
    public JdbcTemplate logJdbcTemplate() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(username);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(1);
        cfg.setPoolName("ServerLog-Pool");
        cfg.setConnectionTimeout(10_000);
        return new JdbcTemplate(new HikariDataSource(cfg));
    }

    /**
     * Crea automaticamente la tabella log_entries sul database ServerLog
     * al primo avvio dell'applicazione (idempotente grazie a IF NOT EXISTS).
     *
     * Schema SQL equivalente per creazione manuale:
     *
     *   CREATE TABLE IF NOT EXISTS log_entries (
     *       id           BIGSERIAL    PRIMARY KEY,
     *       timestamp    TIMESTAMP    NOT NULL,
     *       email_utente VARCHAR(255) NOT NULL,
     *       operazione   VARCHAR(50)  NOT NULL,
     *       messaggio    VARCHAR(500) NOT NULL,
     *       esito        VARCHAR(20)  NOT NULL
     *   );
     *   CREATE INDEX IF NOT EXISTS idx_log_email     ON log_entries(email_utente);
     *   CREATE INDEX IF NOT EXISTS idx_log_operazione ON log_entries(operazione);
     *   CREATE INDEX IF NOT EXISTS idx_log_timestamp  ON log_entries(timestamp DESC);
     *   CREATE INDEX IF NOT EXISTS idx_log_esito      ON log_entries(esito);
     */
    @Bean
    ApplicationRunner initLogSchema(@Qualifier("logJdbcTemplate") JdbcTemplate logJdbc) {
        return args -> {
            logJdbc.execute("""
                CREATE TABLE IF NOT EXISTS log_entries (
                    id           BIGSERIAL    PRIMARY KEY,
                    timestamp    TIMESTAMP    NOT NULL,
                    email_utente VARCHAR(255) NOT NULL,
                    operazione   VARCHAR(50)  NOT NULL,
                    messaggio    VARCHAR(500) NOT NULL,
                    esito        VARCHAR(20)  NOT NULL
                )
                """);
            logJdbc.execute("CREATE INDEX IF NOT EXISTS idx_log_email      ON log_entries(email_utente)");
            logJdbc.execute("CREATE INDEX IF NOT EXISTS idx_log_operazione  ON log_entries(operazione)");
            logJdbc.execute("CREATE INDEX IF NOT EXISTS idx_log_timestamp   ON log_entries(timestamp DESC)");
            logJdbc.execute("CREATE INDEX IF NOT EXISTS idx_log_esito       ON log_entries(esito)");
            System.out.println("[ServerLog] Schema log_entries pronto.");
        };
    }
}
