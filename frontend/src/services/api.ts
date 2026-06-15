const BASE_URL = 'http://localhost:8080/api';

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

// ── CANTIERI ──────────────────────────────────────────────

// Tutti i cantieri (admin/dipendente)
export async function getElencoCantieri() {
  const response = await fetch(`${BASE_URL}/cantieri`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento dei cantieri');
  return data;
}

// Cantieri filtrati per email cliente
export async function getElencoCantieriCliente(email: string) {
  const response = await fetch(`${BASE_URL}/cantieri?email=${email}`);
  const data = await response.json();
  if (!response.ok) throw new Error('Errore nel caricamento dei cantieri');
  return data;
}

// Dettaglio singolo cantiere
export async function getDettagliCantiere(id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${id}`);
  const data = await response.json();
  if (!response.ok) throw new Error('Cantiere non trovato');
  return data;
}

// Crea nuovo cantiere
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

// Modifica cantiere
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

// Avvia cantiere
export async function iniziaLavoriCantiere(id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${id}/avvia`, {
    method: 'PUT',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nell\'avvio del cantiere');
  return data;
}

// Termina cantiere
export async function terminaCantiere(id: number) {
  const response = await fetch(`${BASE_URL}/cantieri/${id}/termina`, {
    method: 'PUT',
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.errore || 'Errore nella chiusura del cantiere');
  return data;
}