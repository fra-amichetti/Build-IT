const BASE_URL = 'https://build-it-production.up.railway.app/api';
export const FILE_BASE_URL = 'https://build-it-production.up.railway.app';

export async function login(email: string, password: string) {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.errore || 'Errore di login');
  }

  return data;
}

export async function register(nome: string, cognome: string, email: string, password: string) {
  const response = await fetch(`${BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nome, cognome, email, password }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.errore || 'Errore durante la registrazione');
  }

  return data;
}

export async function aggiungiDipendente(nome: string, cognome: string, email: string, password: string, incarico: string) {
  const response = await fetch(`${BASE_URL}/dipendenti`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nome, cognome, email, password, incarico }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.errore || 'Errore durante l\'aggiunta del dipendente');
  }

  return data;
}

export async function getDipendenti() {
  const response = await fetch(`${BASE_URL}/dipendenti`);
  const data = await response.json();

  if (!response.ok) {
    throw new Error('Errore nel caricamento dei dipendenti');
  }

  return data;
}

export async function eliminaDipendente(id: number) {
  const response = await fetch(`${BASE_URL}/dipendenti/${id}`, {
    method: 'DELETE',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore durante l\'eliminazione');
  return data;
}

// ── CANTIERI ──────────────────────────────────────────────

export async function getElencoCantieri() {
  const response = await fetch(`${BASE_URL}/cantieri`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento dei cantieri');
  return data;
}

export async function getElencoCantieriCliente(email: string) {
  const response = await fetch(`${BASE_URL}/cantieri?email=${email}`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento dei cantieri');
  return data;
}

export async function getDettagliCantiere(id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${id}`);
  const data = await response.json();
  if (!response.ok) throw new Error('Cantiere non trovato');
  return data;
}

export async function aggiungiCantiere(
  nome: string,
  indirizzo: string,
  dataInizioPrevista: string,
  dataFinePrevista: string,
  emailCliente?: string
) {
  const response = await fetch(`${BASE_URL}/cantieri`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      nome,
      indirizzo,
      dataInizioPrevista,
      dataFinePrevista,
      emailCliente,
    }),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nella creazione del cantiere');
  return data;
}

export async function modificaCantiere(
  id: number,
  updates: { nome?: string; indirizzo?: string; emailCliente?: string }
) {
  const response = await fetch(`${BASE_URL}/cantieri/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updates),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nella modifica del cantiere');
  return data;
}

export async function iniziaLavoriCantiere(id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${id}/avvia`, {
    method: 'PUT',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nell\'avvio del cantiere');
  return data;
}

export async function terminaCantiere(id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${id}/termina`, {
    method: 'PUT',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nella chiusura del cantiere');
  return data;
}

// ── FASI ──────────────────────────────────────────────

export async function getFasi(cantiereId: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/fasi`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento delle fasi');
  return data;
}

export async function aggiungiFase(
  cantiereId: number,
  nome: string,
  descrizione: string,
  dataInizioPrevista: string,
  dataFinePrevista: string,
  squadraId?: string
) {
  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/fasi`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nome, descrizione, dataInizioPrevista, dataFinePrevista, squadraId }),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nella creazione della fase');
  return data;
}

export async function modificaFase(
  id: number,
  updates: { nome?: string; descrizione?: string; dataFinePrevista?: string; squadraId?: string }
)  {
  const response = await fetch(`${BASE_URL}/fasi/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updates),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nella modifica della fase');
  return data;
}

export async function avviaFase(id: number) {
  const response = await fetch(`${BASE_URL}/fasi/${id}/avvia`, { method: 'PUT' });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nell\'avvio della fase');
  return data;
}

export async function terminaFase(id: number) {
  const response = await fetch(`${BASE_URL}/fasi/${id}/termina`, { method: 'PUT' });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nella chiusura della fase');
  return data;
}

export async function getDettagliFase(id: number) {
  const response = await fetch(`${BASE_URL}/fasi/${id}`);
  const data = await response.json();
  if (!response.ok) throw new Error('Fase non trovata');
  return data;
}

// ── SQUADRE ──────────────────────────────────────────────

export async function getSquadre() {
  const response = await fetch(`${BASE_URL}/squadre`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento delle squadre');
  return data;
}

export async function aggiungiSquadra(
  nome: string,
  specializzazione: string,
  numeroComponenti: number,
  nomeReferente: string
) {
  const response = await fetch(`${BASE_URL}/squadre`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nome, specializzazione, numeroComponenti: numeroComponenti.toString(), nomeReferente }),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore durante l\'aggiunta della squadra');
  return data;
}

export async function eliminaSquadra(id: number) {
  const response = await fetch(`${BASE_URL}/squadre/${id}`, { method: 'DELETE' });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore durante l\'eliminazione della squadra');
  return data;
}

// ── STATISTICHE ──────────────────────────────────────────

export async function getStatistiche() {
  const response = await fetch(`${BASE_URL}/statistiche`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento delle statistiche');
  return data;
}

export async function getStatisticheCantiere(idCantiere: number) {
  const response = await fetch(`${BASE_URL}/statistiche/cantiere/${idCantiere}`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento delle statistiche del cantiere');
  return data;
}

// ── DOCUMENTI TECNICI ──────────────────────────────────────────────

export async function getDocumentiTecnici(cantiereId: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/documenti-tecnici`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento dei documenti tecnici');
  return data;
}

export async function aggiungiDocumentoTecnico(
  cantiereId: number,
  nome: string,
  tipologia: string,
  file: File,
  data: string,
  faseId?: number
) {
  const formData = new FormData();
  formData.append('nome', nome);
  formData.append('tipologia', tipologia);
  formData.append('file', file);
  formData.append('data', data);
  if (faseId !== undefined) formData.append('faseId', faseId.toString());

  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/documenti-tecnici`, {
    method: 'POST',
    body: formData,
  });
  const data2 = await response.json();
  if (!response.ok) throw new Error(data2.errore || 'Errore durante l\'aggiunta del documento');
  return data2;
}

export async function eliminaDocumentoTecnico(cantiereId: number, id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/documenti-tecnici/${id}`, {
    method: 'DELETE',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore durante l\'eliminazione');
  return data;
}

// ── DOCUMENTI CONTABILI ──────────────────────────────────────────────

export async function getDocumentiContabili(cantiereId: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/documenti-contabili`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento dei documenti contabili');
  return data;
}

export async function aggiungiDocumentoContabile(
  cantiereId: number,
  nome: string,
  tipo: string,
  importo: number,
  file: File,
  data: string,
  faseId?: number
) {
  const formData = new FormData();
  formData.append('nome', nome);
  formData.append('tipo', tipo);
  formData.append('importo', importo.toString());
  formData.append('file', file);
  formData.append('data', data);
  if (faseId !== undefined) formData.append('faseId', faseId.toString());

  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/documenti-contabili`, {
    method: 'POST',
    body: formData,
  });
  const data2 = await response.json();
  if (!response.ok) throw new Error(data2.errore || 'Errore durante l\'aggiunta del documento');
  return data2;
}

export async function eliminaDocumentoContabile(cantiereId: number, id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/documenti-contabili/${id}`, {
    method: 'DELETE',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore durante l\'eliminazione');
  return data;
}

export async function saldaFattura(cantiereId: number, id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${cantiereId}/documenti-contabili/${id}/salda`, {
    method: 'PUT',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore durante il saldo della fattura');
  return data;
}
