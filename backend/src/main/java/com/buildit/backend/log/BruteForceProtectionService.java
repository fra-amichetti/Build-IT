package com.buildit.backend.log;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Protezione contro attacchi brute-force sul login.
 * Blocca un'email per DURATA_BLOCCO_MS millisecondi dopo MAX_TENTATIVI
 * consecutivi falliti. Lo stato è in-memory e si azzera al riavvio
 * (sufficiente per la protezione a breve termine).
 */
@Service
public class BruteForceProtectionService {

    private static final int  MAX_TENTATIVI   = 3;
    private static final long DURATA_BLOCCO_MS = 10_000L; // 10 secondi

    private record InfoTentativi(int count, long ultimoTentativoMs) {}

    private final ConcurrentHashMap<String, InfoTentativi> tentativi = new ConcurrentHashMap<>();

    /**
     * Verifica se l'email è attualmente bloccata.
     * Se il blocco è scaduto, resetta automaticamente il contatore.
     */
    public boolean isBloccato(String email) {
        InfoTentativi info = tentativi.get(email);
        if (info == null || info.count() < MAX_TENTATIVI) return false;

        long trascorso = System.currentTimeMillis() - info.ultimoTentativoMs();
        if (trascorso >= DURATA_BLOCCO_MS) {
            tentativi.remove(email);
            return false;
        }
        return true;
    }

    /**
     * Restituisce i secondi di blocco rimananenti (arrotondati per eccesso).
     * Utile per il messaggio di errore mostrato all'utente.
     */
    public long secondiRimanenti(String email) {
        InfoTentativi info = tentativi.get(email);
        if (info == null || info.count() < MAX_TENTATIVI) return 0;
        long trascorso  = System.currentTimeMillis() - info.ultimoTentativoMs();
        long rimanentiMs = DURATA_BLOCCO_MS - trascorso;
        return rimanentiMs > 0 ? (rimanentiMs / 1000) + 1 : 0;
    }

    /** Incrementa il contatore di fallimenti per l'email specificata. */
    public void registraFallimento(String email) {
        tentativi.merge(email,
            new InfoTentativi(1, System.currentTimeMillis()),
            (old, n) -> new InfoTentativi(old.count() + 1, System.currentTimeMillis()));
    }

    /** Resetta il contatore (da chiamare dopo un login riuscito). */
    public void reset(String email) {
        tentativi.remove(email);
    }

    /** Restituisce il numero di tentativi falliti correnti per un'email. */
    public int getNumeroTentativi(String email) {
        InfoTentativi info = tentativi.get(email);
        return info != null ? info.count() : 0;
    }
}
