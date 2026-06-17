import React, { useState, useEffect } from 'react';
import { Building2, Clock, ArrowRight, CheckCircle, AlertTriangle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Card, CardBody } from '../components/shared/Card';
import { Button } from '../components/shared/Button';
import { getElencoCantieriCliente } from '../services/api';

interface HomeClienteProps {
  onViewSites: () => void;
  utente: any;
}

export function HomeCliente({ onViewSites, utente }: HomeClienteProps) {
  const [cantieri, setCantieri] = useState<any[]>([]);

  useEffect(() => {
    if (utente?.email) {
      getElencoCantieriCliente(utente.email)
        .then(setCantieri)
        .catch(console.error);
    }
  }, [utente]);

  const activeSites = cantieri.filter(s => s.stato === 'IN_CORSO').length;
  const delayedSites = cantieri.filter(s => s.stato === 'IN_RITARDO').length;
  const completedSites = cantieri.filter(s => s.stato === 'TERMINATO').length;
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Banner */}
        <div className="bg-gradient-to-r from-red-700 to-red-800 rounded-2xl p-8 text-white shadow-lg mb-8">
          <div>
            <h1 className="text-2xl font-bold mb-1">Benvenuto, {utente?.nome}!</h1>
            <p className="text-red-100">Monitora lo stato dei tuoi cantieri in tempo reale</p>
          </div>
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          <Card className="bg-blue-50 border-blue-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-blue-700 mb-1">Cantieri Attivi</p>
                  <p className="text-3xl font-bold text-blue-900">{activeSites}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-blue-100 flex items-center justify-center">
                  <Clock className="w-6 h-6 text-blue-600" />
                </div>
              </div>
            </CardBody>
          </Card>

          <Card className="bg-red-50 border-red-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-red-700 mb-1">In Ritardo</p>
                  <p className="text-3xl font-bold text-red-900">{delayedSites}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-red-100 flex items-center justify-center">
                  <AlertTriangle className="w-6 h-6 text-red-600" />
                </div>
              </div>
            </CardBody>
          </Card>

          <Card className="bg-green-50 border-green-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-green-700 mb-1">Completati</p>
                  <p className="text-3xl font-bold text-green-900">{completedSites}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-green-100 flex items-center justify-center">
                  <CheckCircle className="w-6 h-6 text-green-600" />
                </div>
              </div>
            </CardBody>
          </Card>
        </div>

        {/* Sites Overview */}
        <Card className="mb-6">
          <CardBody className="p-6">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-red-100 flex items-center justify-center">
                  <Building2 className="w-5 h-5 text-red-700" />
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-gray-900">I Tuoi Cantieri</h2>
                  <p className="text-sm text-gray-500">{cantieri.length} cantieri totali</p>
                </div>
              </div>

              <Button onClick={onViewSites} icon={<ArrowRight className="w-4 h-4" />}>
                Vedi Tutti
              </Button>
            </div>

            {/* Recent Sites Preview */}
            {cantieri.length === 0 ? (
              <div className="text-center py-8">
                <Building2 className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                <p className="text-gray-500">Non hai cantieri associati</p>
              </div>
            ) : (
              <div className="space-y-3">
                {cantieri.slice(0, 3).map(site => {
                 const statusColors: Record<string, string> = {
  'PIANIFICATO': 'bg-gray-100 text-gray-700',
  'IN_CORSO': 'bg-blue-100 text-blue-700',
  'IN_RITARDO': 'bg-red-100 text-red-700',
  'TERMINATO': 'bg-green-100 text-green-700',
};

                  return (
                    <div
                      key={site.id}
                      className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                      onClick={onViewSites}
                    >
                      <div>
                        <h3 className="font-medium text-gray-900">{site.nome}</h3>
                        <p className="text-sm text-gray-500">{site.indirizzo}</p>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusColors[site.stato]}`}>
                        {site.stato}
                      </span>
                    </div>
                  );
                })}
              </div>
            )}
          </CardBody>
        </Card>

    
      </main>
    </div>
  );
}
