import React from 'react';
import { Building2, Clock, ArrowRight, HardHat, CheckCircle, AlertTriangle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Card, CardBody } from '../components/shared/Card';
import { Button } from '../components/shared/Button';
import { useApp } from '../context/AppContext';

interface HomeClienteProps {
  onViewSites: () => void;
}

export function HomeCliente({ onViewSites }: HomeClienteProps) {
  const { currentUser, constructionSites } = useApp();

  // Filter sites for the current client
  const clientSites = constructionSites.filter(
    site => site.emailCliente?.toLowerCase() === currentUser?.email.toLowerCase()
  );

  const activeSites = clientSites.filter(s => s.stato === 'In Corso').length;
  const delayedSites = clientSites.filter(s => s.stato === 'In Ritardo').length;
  const completedSites = clientSites.filter(s => s.stato === 'Terminato').length;

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Banner */}
        <div className="bg-gradient-to-r from-red-700 to-red-800 rounded-2xl p-8 text-white shadow-lg mb-8">
          <div className="flex items-start gap-4">
            <div className="w-16 h-16 rounded-full bg-white/20 flex items-center justify-center flex-shrink-0">
              <HardHat className="w-8 h-8" />
            </div>
            <div>
              <h1 className="text-2xl font-bold mb-1">Benvenuto, {currentUser?.nome}!</h1>
              <p className="text-red-100">
                Monitora lo stato dei tuoi cantieri in tempo reale
              </p>
            </div>
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
                  <p className="text-sm text-gray-500">{clientSites.length} cantieri totali</p>
                </div>
              </div>

              <Button onClick={onViewSites} icon={<ArrowRight className="w-4 h-4" />}>
                Vedi Tutti
              </Button>
            </div>

            {/* Recent Sites Preview */}
            {clientSites.length === 0 ? (
              <div className="text-center py-8">
                <Building2 className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                <p className="text-gray-500">Non hai cantieri associati</p>
              </div>
            ) : (
              <div className="space-y-3">
                {clientSites.slice(0, 3).map(site => {
                  const statusColors = {
                    'Pianificato': 'bg-gray-100 text-gray-700',
                    'In Corso': 'bg-blue-100 text-blue-700',
                    'In Ritardo': 'bg-red-100 text-red-700',
                    'Terminato': 'bg-green-100 text-green-700',
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

        {/* Alert for delayed sites */}
        {delayedSites > 0 && (
          <Card className="border-red-200 bg-red-50">
            <CardBody className="p-5">
              <div className="flex items-start gap-4">
                <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center flex-shrink-0">
                  <AlertTriangle className="w-5 h-5 text-red-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-red-900 mb-1">Attenzione!</h3>
                  <p className="text-sm text-red-700">
                    Hai {delayedSites} cantiere{delayedSites > 1 ? 'i' : ''} in ritardo. Contatta l'azienda per maggiori informazioni.
                  </p>
                </div>
              </div>
            </CardBody>
          </Card>
        )}
      </main>
    </div>
  );
}
