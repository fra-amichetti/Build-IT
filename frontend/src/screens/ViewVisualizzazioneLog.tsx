import React, { useEffect, useState } from 'react';
import { AlertTriangle, ChevronLeft, Filter, RefreshCw, Shield } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { getLogs, getAccessiSospetti, LogFiltri } from '../services/api';

// ── Tipi ─────────────────────────────────────────────────────────────────────

interface LogEntry {
  id: number;
  timestamp: string;
  emailUtente: string;
  operazione: string;
  messaggio: string;
  esito: 'SUCCESSO' | 'FALLITO' | 'SOSPETTO';
}


// ── Helpers ───────────────────────────────────────────────────────────────────

function esitoClass(esito: string): string {
  switch (esito) {
    case 'SUCCESSO': return 'bg-green-100 text-green-800';
    case 'FALLITO':  return 'bg-red-100 text-red-800';
    case 'SOSPETTO': return 'bg-orange-100 text-orange-800';
    default:         return 'bg-gray-100 text-gray-800';
  }
}

function formatTimestamp(ts: string): string {
  try {
    return new Date(ts).toLocaleString('it-IT', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    });
  } catch {
    return ts;
  }
}

// ── Componente principale ─────────────────────────────────────────────────────

interface Props {
  onBack: () => void;
  embedded?: boolean;
}

export function ViewVisualizzazioneLog({ onBack, embedded }: Props) {
  const [entries, setEntries] = useState<LogEntry[]>([]);
  const [sospetti, setSospetti] = useState<LogEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errore, setErrore] = useState('');
  const [showSospetti, setShowSospetti] = useState(false);


  const carica = async (filtri?: LogFiltri) => {
    setIsLoading(true);
    setErrore('');
    try {
      const [logs, acc] = await Promise.all([getLogs(filtri), getAccessiSospetti()]);
      setEntries(logs);
      setSospetti(acc);
    } catch (e: any) {
      setErrore(e.message || 'Errore nel caricamento del log');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { carica(); }, []);

 
  const displayList = showSospetti ? sospetti : entries;

  const content = (
    <div className="space-y-6">
     

      {/* Accessi Sospetti banner */}
      {sospetti.length > 0 && (
        <div
          className="bg-orange-50 border border-orange-300 rounded-xl p-4 flex items-center justify-between cursor-pointer hover:bg-orange-100 transition-colors"
          onClick={() => setShowSospetti(v => !v)}
        >
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-orange-600" />
            <div>
              <p className="font-semibold text-orange-800">
                {sospetti.length} accesso/i sospetto/i rilevato/i
              </p>
              <p className="text-sm text-orange-600">Clicca per {showSospetti ? 'nascondere' : 'visualizzare'}</p>
            </div>
          </div>
          {showSospetti && (
            <button
              onClick={e => { e.stopPropagation(); setShowSospetti(false); }}
              className="text-orange-700 text-sm underline"
            >
              Mostra tutti i log
            </button>
          )}
        </div>
      )}

      {/* Tabella */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
          <h2 className="font-semibold text-gray-800">
            {showSospetti ? 'Accessi Sospetti' : 'Log di Sistema'}
            <span className="ml-2 text-sm text-gray-500">({displayList.length} voci)</span>
          </h2>
        </div>

        {isLoading ? (
          <div className="p-8 text-center text-gray-500">Caricamento...</div>
        ) : errore ? (
          <div className="p-8 text-center text-red-600">{errore}</div>
        ) : displayList.length === 0 ? (
          <div className="p-8 text-center text-gray-400">Nessuna voce trovata</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
                <tr>
                  <th className="px-4 py-3 text-left">Data/Ora</th>
                  <th className="px-4 py-3 text-left">Utente</th>
                  <th className="px-4 py-3 text-left">Operazione</th>
                  <th className="px-4 py-3 text-left">Messaggio</th>
                  <th className="px-4 py-3 text-left">Esito</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {displayList.map(entry => (
                  <tr key={entry.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-500 whitespace-nowrap font-mono text-xs">
                      {formatTimestamp(entry.timestamp)}
                    </td>
                    <td className="px-4 py-3 text-gray-700 whitespace-nowrap max-w-[180px] truncate">
                      {entry.emailUtente}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <span className="font-mono text-xs bg-gray-100 px-2 py-0.5 rounded">
                        {entry.operazione}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-600 max-w-xs">
                      {entry.messaggio}
                    </td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${esitoClass(entry.esito)}`}>
                        {entry.esito}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );

  if (embedded) return content;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center gap-3 mb-6">
          <button
            onClick={onBack}
            className="flex items-center gap-1 text-gray-600 hover:text-gray-900 transition-colors"
          >
            <ChevronLeft className="w-5 h-5" />
            Indietro
          </button>
          <div className="flex items-center gap-2">
            <Shield className="w-5 h-5 text-red-600" />
            <h1 className="text-xl font-bold text-gray-900">Log di Sistema</h1>
          </div>
        </div>
        {content}
      </main>
    </div>
  );
}
