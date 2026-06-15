// User and Role Types
export type UserRole = 'Amministratore' | 'Dipendente' | 'Cliente';

export interface User {
  id: string;
  email: string;
  password: string;
  nome: string;
  cognome: string;
  role: UserRole;
}

// Construction Site Types
export type SiteStatus = 'Pianificato' | 'In Corso' | 'In Ritardo' | 'Terminato';

export interface ConstructionSite {
  id: number;                        // Long Java → number TypeScript
  nome: string;
  indirizzo: string;
  dataInizioPrevista: string;
  dataInizioEffettiva?: string;
  dataFinePrevista: string;
  dataFineEffettiva?: string;
  emailCliente?: string;
  stato: string;                     // stringa libera: "PIANIFICATO", "IN_CORSO", ecc.
}

// Work Phase Types
export type PhaseStatus = 'Pianificata' | 'In Corso' | 'Completata' | 'In Ritardo';

export interface WorkPhase {
  id: string;
  cantiereId: string;
  nome: string;
  descrizione: string;
  dataInizio: string;
  dataInizioEffettiva?: string;
  dataFinePrevista: string;
  dataFineEffettiva?: string;
  squadraId: string;
  stato: PhaseStatus;
}

// Team Types
export type TeamSpecialization = 'Muratori' | 'Elettricisti' | 'Idraulici' | 'Carpentieri';

export interface Team {
  id: string;
  nome: string;
  specializzazione: TeamSpecialization;
  numeroComponenti: number;
  nomeReferente: string;
}

// Technical Document Types
export type TechnicalDocumentType = 'prospetto' | 'pianta' | 'foto' | 'permesso' | 'relazione' | 'altro';

export interface TechnicalDocument {
  id: string;
  cantiereId: string;
  faseId?: string;
  nome: string;
  tipologia: TechnicalDocumentType;
  fileUrl: string;
  data: string;
}

// Accounting Document Types
export type AccountingDocumentType = 'Fattura' | 'Preventivo';
export type PaymentStatus = 'Da Saldare' | 'Saldato';

export interface AccountingDocument {
  id: string;
  cantiereId: string;
  faseId?: string;
  nome: string;
  tipo: AccountingDocumentType;
  importo: number;
  data: string;
  fileUrl: string;
  statoPagamento?: PaymentStatus;
}
