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