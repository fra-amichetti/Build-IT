import React, { useEffect, useState } from 'react';
import {
  ArrowLeft, DollarSign, TrendingUp, Building2, Users, AlertTriangle,
  CheckCircle
} from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Card, CardBody, CardHeader } from '../components/shared/Card';
import { StatusBadge } from '../components/shared/StatusBadge';
import { getStatistiche } from '../services/api';

interface ViewMostraStatisticheProps {
  onBack: () => void;
  embedded?: boolean;
}

interface Statistiche {
  fatturatoTotale: number;
  fatturatoIncassato: number;
  saldoDaIncassare: number;
  numeroCantieriAttivi: number;
  numeroCantieriInRitardo: number;
  numeroCantieriTerminati: number;
  squadreImpiegate: { id: number; nome: string; specializzazione: string }[];
}
export function ViewMostraStatistiche({ onBack, embedded }: ViewMostraStatisticheProps) {
  const [stats, setStats] = useState<Statistiche | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getStatistiche()
      .then(setStats)
      .catch(() => setError('Errore nel caricamento delle statistiche'))
      .finally(() => setIsLoading(false));
  }, []);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('it-IT', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  };

  return (
    <div className="min-h-screen bg-gray-50">
     {!embedded && <Header showMenuButton onMenuClick={onBack} />}

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
       {!embedded && (
  <button
    onClick={onBack}
    className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
  >
    <ArrowLeft className="w-5 h-5" />
    <span>Torna alla home</span>
  </button>
)}

        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-red-100 flex items-center justify-center">
              <TrendingUp className="w-5 h-5 text-red-700" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Statistiche Aziendali</h1>
              <p className="text-gray-500 text-sm">Panoramica economica e operativa</p>
            </div>
          </div>
        </div>

        {isLoading && (
          <div className="text-center py-12">
            <div className="w-8 h-8 border-4 border-red-600 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
            <p className="text-gray-500">Caricamento...</p>
          </div>
        )}

        {error && (
          <div className="text-center py-12 text-red-600">{error}</div>
        )}

        {stats && (
          <>
            {/* Economic Indicators */}
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Indicatori Economici</h2>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <Card className="bg-gradient-to-br from-green-50 to-green-100 border-green-200">
                <CardBody className="p-5">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-sm text-green-700 font-medium">Fatturato Totale</p>
                    <DollarSign className="w-5 h-5 text-green-600" />
                  </div>
                  <p className="text-3xl font-bold text-green-900">
                    {formatCurrency(stats.fatturatoTotale)}
                  </p>
                </CardBody>
              </Card>

              <Card className="bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200">
                <CardBody className="p-5">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-sm text-blue-700 font-medium">Fatturato Incassato</p>
                    <CheckCircle className="w-5 h-5 text-blue-600" />
                  </div>
                  <p className="text-3xl font-bold text-blue-900">
                    {formatCurrency(stats.fatturatoIncassato)}
                  </p>
                </CardBody>
              </Card>

              <Card className="bg-gradient-to-br from-red-50 to-red-100 border-red-200">
                <CardBody className="p-5">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-sm text-red-800 font-medium">Saldo da Incassare</p>
                    <AlertTriangle className="w-5 h-5 text-red-700" />
                  </div>
                  <p className="text-3xl font-bold text-red-950">
                    {formatCurrency(stats.saldoDaIncassare)}
                  </p>
                </CardBody>
              </Card>
            </div>

            {/* Operational Indicators */}
            <h2 className="text-lg font-semibold text-gray-900 mb-4 mt-8">Indicatori Operativi</h2>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
              <Card>
                <CardBody className="p-5 text-center">
                  <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center mx-auto mb-3">
                    <Building2 className="w-6 h-6 text-blue-600" />
                  </div>
                  <p className="text-2xl font-bold text-gray-900">{stats.numeroCantieriAttivi}</p>
                  <p className="text-sm text-gray-500">Cantieri Attivi</p>
                </CardBody>
              </Card>

              <Card>
                <CardBody className="p-5 text-center">
                  <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-3">
                    <AlertTriangle className="w-6 h-6 text-red-600" />
                  </div>
                  <p className="text-2xl font-bold text-red-600">{stats.numeroCantieriInRitardo}</p>
                  <p className="text-sm text-gray-500">In Ritardo</p>
                </CardBody>
              </Card>

              <Card>
                <CardBody className="p-5 text-center">
                  <div className="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-3">
                    <CheckCircle className="w-6 h-6 text-green-600" />
                  </div>
                  <p className="text-2xl font-bold text-gray-900">{stats.numeroCantieriTerminati}</p>
                  <p className="text-sm text-gray-500">Terminati</p>
                </CardBody>
              </Card>

              <Card>
                <CardBody className="p-5 text-center">
                  <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-3">
                    <Users className="w-6 h-6 text-red-700" />
                  </div>
                  <p className="text-2xl font-bold text-gray-900">{stats.squadreImpiegate.length}</p>
                  <p className="text-sm text-gray-500">Squadre Impiegate</p>
                </CardBody>
              </Card>
            </div>

            {/* Active Teams List */}
            <Card>
              <CardHeader>
                <h3 className="font-medium text-gray-900">Squadre Attualmente Impiegate</h3>
              </CardHeader>
              <CardBody className="p-0">
                {stats.squadreImpiegate.length === 0 ? (
                  <div className="p-8 text-center text-gray-500">
                    Nessuna squadra attualmente impiegata
                  </div>
                ) : (
                  <div className="divide-y divide-gray-100">
                    {stats.squadreImpiegate.map(squadra => (
                      <div key={squadra.id} className="px-6 py-4">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="font-medium text-gray-900">{squadra.nome}</p>
                            <p className="text-sm text-gray-500">{squadra.specializzazione}</p>
                          </div>
                          <StatusBadge status="Occupata" variant="amber" size="sm" />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardBody>
            </Card>
          </>
        )}
      </main>
    </div>
  );
}