package com.buildit.backend.log;

public enum TipoOperazione {
    // Auth
    LOGIN_OK,
    LOGIN_FALLITO,
    LOGIN_BLOCCATO,
    LOGOUT,
    REGISTRAZIONE,
    // Cantieri
    CREA_CANTIERE,
    MODIFICA_CANTIERE,
    AVVIA_CANTIERE,
    TERMINA_CANTIERE,
    // Fasi
    CREA_FASE,
    MODIFICA_FASE,
    AVVIA_FASE,
    TERMINA_FASE,
    // Squadre
    CREA_SQUADRA,
    ELIMINA_SQUADRA,
    ASSEGNA_SQUADRA,
    // Dipendenti
    CREA_DIPENDENTE,
    // Documenti
    CARICA_DOCUMENTO_TECNICO,
    CARICA_DOCUMENTO_CONTABILE,
    // Sicurezza
    ACCESSO_SOSPETTO
}
