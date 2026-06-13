
# BuildIT

**BuildIT** è una piattaforma web centralizzata per la gestione di cantieri edili, progettata per sostituire strumenti frammentati come fogli Excel, email e cartelle condivise. Il sistema offre un ambiente integrato sia per le imprese di costruzione che per i loro committenti.

---

## 📌 Panoramica

L'obiettivo è semplificare il ciclo di vita completo di un cantiere: dalla pianificazione delle fasi lavorative, all'assegnazione delle squadre operative, fino al monitoraggio della documentazione tecnica e contabile. I clienti accedono a un portale dedicato in sola lettura per seguire l'avanzamento dei lavori e la situazione dei pagamenti.

---

## 👥 Ruoli e Permessi

| Ruolo | Permessi |
|-------|----------|
| **Amministratore** | Accesso completo: gestione cantieri, fasi, squadre, dipendenti, documenti, statistiche aziendali, visualizzazione log |
| **Dipendente** | Gestione cantieri, fasi e documenti (tecnici e contabili) |
| **Cliente** | Registrazione autonoma, consultazione in sola lettura dei propri cantieri (timeline, documenti, pagamenti) |

---

## ✅ Requisiti Funzionali Principali

- **Autenticazione e registrazione** sicura (blocco dopo 3 tentativi falliti)
- **Gestione cantieri** con stati: Pianificato, In Corso, In Ritardo, Terminato
- **Gestione fasi lavorative** con controllo automatico di sovrapposizione assegnazione squadre
- **Associazione automatica** cantiere-cliente tramite email
- **Verifica automatica giornaliera** delle scadenze (stato "In Ritardo")
- **Gestione documenti tecnici** (PDF, JPG, PNG) e **contabili** (fatture con stato pagamento, preventivi)
- **Statistiche economiche**: fatturato totale, incassi, saldo da incassare
- **Statistiche operative**: cantieri attivi/in ritardo/terminati, squadre impiegate
- **Logging completo** di tutte le operazioni con analisi per accessi sospetti

---

## 🛡️ Sicurezza

- Password con hashing crittografico (min 8 caratteri, maiuscola, minuscola, numero, carattere speciale)
- Blocco temporaneo dopo 3 tentativi di autenticazione falliti
- Log delle operazioni accessibile solo all'Amministratore
- Backup automatico e periodico del database
- Conformità GDPR

---

## 🏗️ Architettura

- **Pattern**: Client-Server a 3 livelli (Entity-Control-Boundary) con pattern Broker
- **Protocollo**: TLS/HTTPS per tutte le comunicazioni
- **Persistenza**: DBMS relazionale con CRUD manuali
- **Separazione**: Database principale e database Log su server distinti

### Componenti server
- Server Gestione Operativa (Dipendente/Amministratore/Cliente)
- Server Amministratore (funzionalità esclusive)
- Server Autenticazione
- Server Log
- DBMS relazionale
- Server Log dedicato

---

## 📊 Modellazione del Dominio

Le entità principali includono:
- `Utente` (abstract) → `Amministratore`, `Dipendente`, `Cliente`
- `Cantiere` con composizione di `FaseLavorativa` (0..*)
- `Squadra` con vincolo di disponibilità (`isDisponibile()`)
- Gerarchia documentale: `Documento` (abstract) → `DocumentoTecnico`, `DocumentoContabile` → `Fattura`, `Preventivo`
- `Log` con voci di log tracciate da `Logger`

**Principi OOP applicati**: polimorfismo per validazione estensioni file e calcolo statistiche (Fattura contribuisce, Preventivo no), Dependency Inversion tramite interfacce.

---

## 🧪 Testing

Suite JUnit copre:
- `TestCantiere` – inizio lavori, terminazione, verifica ritardo
- `TestFaseLavorativa` – avvio, terminazione, date illegiche, sola lettura dopo terminazione
- `TestSquadra` – verifica disponibilità con e senza sovrapposizioni
- `TestFattura` – getter/setter, saldo importo
- `TestAutenticazioneController` – login corretto/errato, logout
- `TestCantiereController` – aggiunta fase con squadra disponibile/occupata, avvio/terminazione cantiere, controllo scadenza
- `TestDipendentiController` – aggiunta/eliminazione, email duplicata
- `TestStatisticheController` – coerenza fatturato, valori non negativi
- `TestSquadreController` – eliminazione squadra disponibile/assegnata, nome duplicato

---

## 📅 Piano di Rilascio

| Periodo | Fase |
|---------|------|
| Giugno 2026 | Primo prototipo |
| Novembre 2026 | Prima versione con tutte le funzionalità + test usabilità |
| Febbraio 2027 | Versione beta con accesso limitato |
| Giugno 2027 | Rilascio al pubblico |

---

## 📁 Struttura Repository

```
BuildIT/
├── docs/                       # Documentazione completa
├── src/
│   ├── client/                 # View (interfacce utente)
│   │   ├── RegistrazioneView
│   │   ├── AutenticazioneView
│   │   ├── HomeAmministratore
│   │   ├── HomeDipendente
│   │   ├── HomeCliente
│   │   └── ...
│   ├── server/                 # Server e Broker
│   │   ├── FiltroRichieste
│   │   └── ...
│   ├── domain/                 # Modello del dominio
│   │   ├── Utente.java
│   │   ├── Cantiere.java
│   │   ├── FaseLavorativa.java
│   │   ├── Squadra.java
│   │   ├── Documento.java
│   │   ├── Fattura.java
│   │   ├── Preventivo.java
│   │   └── Log.java
│   ├── controllers/            # Logica di business
│   │   ├── AutenticazioneController
│   │   ├── CantiereController
│   │   ├── DipendentiController
│   │   ├── SquadreController
│   │   ├── StatisticheController
│   │   └── LoggerController
│   └── persistence/            # Accesso al DB
├── tests/                      # Suite JUnit
└── README.md
```

---

## 👨‍💻 Team di Sviluppo

| Nome | Matricola |
|------|-----------|
| Francesco Amichetti | 0001117098 |
| Martina Testi | 0001114098 |
| Sofia Torzolini | 0001127529 |

---

## 📄 Licenza

Progetto sviluppato a scopi didattici. Tutti i diritti riservati.
