import React, { useEffect, useState } from 'react';
import { Building2, Shield } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Card, CardBody } from '../components/shared/Card';
import { Button } from '../components/shared/Button';
import { HomeGestioneDipendenti } from './HomeGestioneDipendenti';
import { HomeGestioneSquadre } from './HomeGestioneSquadre';
import { ViewMostraStatistiche } from './ViewMostraStatistiche';
import { getStatistiche, getSquadre } from '../services/api';

interface HomeAmministratoreProps {
  onNavigate: (screen: 'cantieri') => void;
  nomeUtente?: string;
}

export function HomeAmministratore({ onNavigate, nomeUtente }: HomeAmministratoreProps) {
  const [activeTab, setActiveTab] = useState<'overview' | 'dipendenti' | 'squadre' | 'statistiche'>('overview');
  const [activeSites, setActiveSites] = useState(0);
  const [delayedSites, setDelayedSites] = useState(0);
  const [totaleSquadre, setTotaleSquadre] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    caricaPanoramica();
  }, []);

  const caricaPanoramica = async () => {
    setIsLoading(true);
    try {
      const [stats, squadre] = await Promise.all([
        getStatistiche(),
        getSquadre(),
      ]);
      setActiveSites(stats.numeroCantieriAttivi);
      setDelayedSites(stats.numeroCantieriInRitardo);
      setTotaleSquadre(squadre.length);
    } catch (err) {
      console.error('Errore nel caricamento della panoramica', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Section */}
        <div className="mb-8">
          <div className="bg-gradient-to-r from-red-700 to-red-800 rounded-2xl p-8 text-white shadow-lg">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-14 h-14 rounded-full bg-white/20 flex items-center justify-center">
                <Shield className="w-7 h-7" />
              </div>
              <div>
                <h1 className="text-2xl font-bold">Benvenuto, {nomeUtente}!</h1>
                <p className="text-red-100">Pannello Amministratore</p>
              </div>
            </div>
            <p className="text-red-100 mb-6">
              Gestisci tutti gli aspetti della tua azienda edile da un unico pannello.
            </p>

            <div className="grid grid-cols-3 gap-4">
              <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4">
                <p className="text-3xl font-bold">{isLoading ? '—' : activeSites}</p>
                <p className="text-red-100 text-sm">Cantieri Attivi</p>
              </div>
              <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4">
                <p className="text-3xl font-bold">{isLoading ? '—' : delayedSites}</p>
                <p className="text-red-100 text-sm">In Ritardo</p>
              </div>
              <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4">
                <p className="text-3xl font-bold">{isLoading ? '—' : totaleSquadre}</p>
                <p className="text-red-100 text-sm">Squadre</p>
              </div>
            </div>
          </div>
        </div>

        {/* Navigation Tabs & Action Button */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setActiveTab('dipendenti')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                activeTab === 'dipendenti'
                  ? 'bg-red-600 text-white'
                  : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'
              }`}
            >
              Dipendenti
            </button>
            <button
              onClick={() => setActiveTab('squadre')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                activeTab === 'squadre'
                  ? 'bg-red-600 text-white'
                  : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'
              }`}
            >
              Squadre
            </button>
            <button
              onClick={() => setActiveTab('statistiche')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                activeTab === 'statistiche'
                  ? 'bg-red-600 text-white'
                  : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'
              }`}
            >
              Statistiche
            </button>
          </div>

          <Button
            onClick={() => onNavigate('cantieri')}
            icon={<Building2 className="w-4 h-4" />}
            className="whitespace-nowrap"
          >
            Vai alla Lista Cantieri
          </Button>
        </div>

        {/* Tab Content */}
        

        {activeTab === 'dipendenti' && (
          <HomeGestioneDipendenti onBack={() => setActiveTab('overview')} embedded />
        )}

        {activeTab === 'squadre' && (
          <HomeGestioneSquadre onBack={() => setActiveTab('overview')} embedded />
        )}

        {activeTab === 'statistiche' && (
          <ViewMostraStatistiche onBack={() => setActiveTab('overview')} embedded />
        )}
      </main>
    </div>
  );
}