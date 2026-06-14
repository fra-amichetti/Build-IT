import React from 'react';
import { Plus, MapPin, Calendar, Mail, ArrowLeft, HardHat } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { StatusBadge, getSiteStatusVariant } from '../components/shared/StatusBadge';
import { useApp } from '../context/AppContext';
import { ConstructionSite, SiteStatus } from '../types';

interface HomeListaCantieriProps {
  onBack?: () => void;
  onSelectSite: (site: ConstructionSite) => void;
  onAddSite?: () => void;
  readOnly?: boolean;
  clientEmail?: string;
}

export function HomeListaCantieri({
  onBack,
  onSelectSite,
  onAddSite,
  readOnly = false,
  clientEmail,
}: HomeListaCantieriProps) {
  const { constructionSites } = useApp();

  // Filter sites by client email if provided
  let filteredSites = constructionSites;
  if (clientEmail) {
    filteredSites = constructionSites.filter(
      (site) => site.emailCliente?.toLowerCase() === clientEmail.toLowerCase()
    );
  }

  // Sort by status priority (In Ritardo > In Corso > Pianificato > Terminato)
  const statusPriority: Record<SiteStatus, number> = {
    'In Ritardo': 0,
    'In Corso': 1,
    'Pianificato': 2,
    'Terminato': 3,
  };

  filteredSites = [...filteredSites].sort(
    (a, b) => statusPriority[a.stato] - statusPriority[b.stato]
  );

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header showMenuButton={!!onBack} onMenuClick={onBack} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          {onBack && (
            <button
              onClick={onBack}
              className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4 transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              <span>Torna indietro</span>
            </button>
          )}

          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Cantieri</h1>
              <p className="text-gray-500 mt-1">
                {clientEmail
                  ? `I tuoi cantieri (${filteredSites.length})`
                  : `Tutti i cantieri (${filteredSites.length})`}
              </p>
            </div>

            {!readOnly && onAddSite && (
              <Button onClick={onAddSite} icon={<Plus className="w-4 h-4" />}>
                Aggiungi Cantiere
              </Button>
            )}
          </div>
        </div>

        {/* Sites List */}
        {filteredSites.length === 0 ? (
          <div className="text-center py-12">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
              <HardHat className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nessun cantiere presente</h3>
            <p className="text-gray-500">Aggiungi un nuovo cantiere per iniziare</p>
          </div>
        ) : (
          <div className="grid gap-4">
            {filteredSites.map((site) => (
              <Card
                key={site.id}
                hover
                onClick={() => onSelectSite(site)}
              >
                <CardBody className="p-6">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-start gap-3">
                        <div className="w-10 h-10 rounded-lg bg-red-50 flex items-center justify-center flex-shrink-0 mt-0.5">
                          <HardHat className="w-5 h-5 text-red-700" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <h3 className="font-semibold text-gray-900 text-lg">{site.nome}</h3>
                          <div className="flex items-center gap-2 text-gray-500 text-sm mt-1">
                            <MapPin className="w-4 h-4 flex-shrink-0" />
                            <span>{site.indirizzo}</span>
                          </div>
                          {site.emailCliente && (
                            <div className="flex items-center gap-2 text-gray-500 text-sm mt-1">
                              <Mail className="w-4 h-4 flex-shrink-0" />
                              <span>{site.emailCliente}</span>
                            </div>
                          )}
                          <div className="flex items-center gap-2 text-gray-500 text-sm mt-1">
                            <Calendar className="w-4 h-4 flex-shrink-0" />
                            <span>
              Fine stimata: {formatDate(site.dataFineStimata)}
            </span>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <StatusBadge
                        status={site.stato}
                        variant={getSiteStatusVariant(site.stato)}
                        size="lg"
                      />
                    </div>
                  </div>
                </CardBody>
              </Card>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
