
import React, { useState, useEffect } from 'react';
import {
  MapPin, Calendar, ArrowLeft, Edit, CheckCircle, FileText, Receipt,
  Plus, Clock, Mail, AlertCircle, ChevronRight, Building2, Users
} from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Card, CardBody, CardHeader } from '../components/shared/Card';
import { StatusBadge, getSiteStatusVariant, getPhaseStatusVariant } from '../components/shared/StatusBadge';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { useApp } from '../context/AppContext';
import { ConstructionSite, WorkPhase } from '../types';
import { iniziaLavoriCantiere, terminaCantiere } from '../services/api';
import { getFasi } from '../services/api';


type TabType = 'fasi' | 'tecnici' | 'contabili';

interface HomeCantiereProps {
  site: ConstructionSite;
  onBack: () => void;
  onEditSite?: () => void;
  onCloseSite?: () => void;
  onAddPhase?: () => void;
  onSelectPhase: (phase: WorkPhase) => void;
  onOpenTechnicalDocs: () => void;
  onOpenAccountingDocs: () => void;
  readOnly?: boolean;
}

export function HomeCantiere({
  site,
  onBack,
  onEditSite,
  onCloseSite,
  onAddPhase,
  onSelectPhase,
  onOpenTechnicalDocs,
  onOpenAccountingDocs,
  readOnly = false,
}: HomeCantiereProps) {
  const [activeTab, setActiveTab] = useState<TabType>('fasi');
const { getTeamById } = useApp();
const [phases, setPhases] = useState<any[]>([]);

useEffect(() => {
  getFasi(Number(site.id)).then(setPhases).catch(console.error);
}, [site.id]);
const [showTerminaDialog, setShowTerminaDialog] = useState(false);
const [terminaError, setTerminaError] = useState('');
  const formatDate = (dateStr?: string) => {
  if (!dateStr) return '—';
  const date = new Date(dateStr + 'T00:00:00');
  return date.toLocaleDateString('it-IT', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
};
function mapStatoFrontend(stato: string): string {
  switch (stato) {
    case 'PIANIFICATO': return 'Pianificato';
    case 'IN_CORSO': return 'In Corso';
    case 'IN_RITARDO': return 'In Ritardo';
    case 'TERMINATO': return 'Terminato';
    default: return stato;
  }
}

  const tabs: { id: TabType; label: string; icon: React.ReactNode }[] = [
    { id: 'fasi', label: 'Fasi', icon: <Clock className="w-4 h-4" /> },
    { id: 'tecnici', label: 'Documenti Tecnici', icon: <FileText className="w-4 h-4" /> },
    { id: 'contabili', label: 'Documenti Contabili', icon: <Receipt className="w-4 h-4" /> },
  ];

  // Check if site can be closed (not already terminated)
  const canCloseSite = site.stato !== 'TERMINATO';
function mapStatoFase(stato: string): string {
  switch (stato) {
    case 'PIANIFICATA': return 'Pianificata';
    case 'IN_CORSO': return 'In Corso';
    case 'IN_RITARDO': return 'In Ritardo';
    case 'TERMINATA': return 'Completata';
    default: return stato;
  }
}
  return (
    <div className="min-h-screen bg-gray-50">
      <Header showMenuButton onMenuClick={onBack} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Torna alla lista</span>
        </button>

        {terminaError && (
          <div className="mb-4 flex items-start gap-3 p-4 bg-red-50 border border-red-200 rounded-lg">
            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <p className="text-sm font-medium text-red-800">Impossibile terminare il cantiere</p>
              <p className="text-sm text-red-700 mt-0.5">{terminaError}</p>
            </div>
            <button onClick={() => setTerminaError('')} className="text-red-400 hover:text-red-600">
              <span className="sr-only">Chiudi</span>
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
        )}

        {/* Site Header Card */}
        <Card className="mb-6">
          <CardBody className="p-6">
            <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-6">
              <div className="flex-1">
                <div className="flex items-start gap-4">
                  <div className="w-14 h-14 rounded-xl bg-red-100 flex items-center justify-center flex-shrink-0">
                    <Building2 className="w-7 h-7 text-red-700" />
                  </div>
                  <div>
                    <h1 className="text-2xl font-bold text-gray-900">{site.nome}</h1>
                    <div className="flex items-center gap-2 text-gray-600 mt-1">
                      <MapPin className="w-4 h-4" />
                      <span>{site.indirizzo}</span>
                    </div>
                    {site.emailCliente && (
                      <div className="flex items-center gap-2 text-gray-600 mt-1">
                        <Mail className="w-4 h-4" />
                        <span>{site.emailCliente}</span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="mt-4 flex flex-wrap gap-4">
                  <div className="flex items-center gap-2 text-sm">
                    <Calendar className="w-4 h-4 text-gray-400" />
                    <span className="text-gray-600">Inizio Previsto:</span>
                    <span className="font-medium text-gray-900">{formatDate(site.dataInizioPrevista)}</span>
                  </div>
                  {site.dataInizioEffettiva && (
    <div className="flex items-center gap-2 text-sm">
      <Calendar className="w-4 h-4 text-gray-400" />
      <span className="text-gray-600">Inizio effettivo:</span>
      <span className="font-medium text-gray-900">{formatDate(site.dataInizioEffettiva)}</span>
    </div>
  )}
                  <div className="flex items-center gap-2 text-sm">
                    <Calendar className="w-4 h-4 text-gray-400" />
                    <span className="text-gray-600">Fine Prevista:</span>
                    <span className="font-medium text-gray-900">{formatDate(site.dataFinePrevista)}</span>
                  </div>
                  {site.dataFineEffettiva && (
                    <div className="flex items-center gap-2 text-sm">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span className="text-gray-600">Fine Effettiva:</span>
                      <span className="font-medium text-gray-900">{formatDate(site.dataFineEffettiva)}</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-3">
             <StatusBadge
    status={mapStatoFrontend(site.stato)}
    variant={getSiteStatusVariant(mapStatoFrontend(site.stato))}
    size="lg"
  />
 {!readOnly && onAddPhase && site.stato !== 'TERMINATO' && (
  <Button
    variant="secondary"
    size="sm"
    onClick={async () => {
      try {
        await iniziaLavoriCantiere(Number(site.id));
        onBack();
      } catch (err: any) {
        alert(err.message);
      }
    }}
    icon={<CheckCircle className="w-4 h-4" />}
  > 
    Avvia
  </Button>
)}
                {!readOnly && (
                  <>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={onEditSite}
                      disabled={site.stato === 'TERMINATO'}
                      icon={<Edit className="w-4 h-4" />}
                    >
                      Modifica
                    </Button>
                   {canCloseSite && onCloseSite && (
  <Button
    variant="danger"
    size="sm"
    onClick={() => setShowTerminaDialog(true)}
    icon={<CheckCircle className="w-4 h-4" />}
  >
    Termina
  </Button>
)} </>
                )}
            
              </div>
            </div>
          </CardBody>
        </Card>

        {/* Tabs */}
        <div className="mb-6">
          <div className="border-b border-gray-200">
            <nav className="flex gap-4 -mb-px">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-4 py-3 border-b-2 font-medium text-sm transition-colors ${
                    activeTab === tab.id
                      ? 'border-amber-500 text-red-700'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  {tab.icon}
                  {tab.label}
                </button>
              ))}
            </nav>
          </div>
        </div>

        {/* Tab Content */}
        {activeTab === 'fasi' && (
  <div>
    {!readOnly && onAddPhase && site.stato !== 'TERMINATO' && (
      <div className="mb-6">
        <Button onClick={onAddPhase} icon={<Plus className="w-4 h-4" />}>
          Aggiungi Fase
        </Button>
      </div>
    )}

    {phases.length === 0 ? (
      <div className="text-center py-8">
        <Clock className="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p className="text-gray-500">Nessuna fase presente</p>
      </div>
    ) : (
      <div className="relative">
        {/* Linea verticale della timeline */}
        <div className="absolute left-6 top-0 bottom-0 w-0.5 bg-gray-200" />

        <div className="space-y-4">
          {phases
            .sort((a, b) => new Date(a.dataInizioPrevista).getTime() - new Date(b.dataInizioPrevista).getTime())
            .map((phase, index) => {
              const statoMappato = mapStatoFase(phase.stato);
              const colori: Record<string, string> = {
                'Pianificata': 'bg-gray-400',
                'In Corso': 'bg-blue-500',
                'In Ritardo': 'bg-red-500',
                'Completata': 'bg-green-500',
              };
              const colore = colori[statoMappato] || 'bg-gray-400';

              return (
                <div key={phase.id} className="relative flex items-start gap-4 pl-14">
                  {/* Cerchio sulla timeline */}
                  <div className={`absolute left-4 w-5 h-5 rounded-full ${colore} border-2 border-white shadow-sm flex items-center justify-center z-10`}>
                    <span className="text-white text-xs font-bold">{index + 1}</span>
                  </div>

                  {/* Card della fase */}
                  <Card
                    className="flex-1 cursor-pointer hover:shadow-md transition-shadow"
                    hover
                    onClick={() => onSelectPhase(phase)}
                  >
                    <CardBody className="p-4">
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <h4 className="font-semibold text-gray-900">{phase.nome}</h4>
                            <StatusBadge
                              status={statoMappato}
                              variant={getPhaseStatusVariant(statoMappato)}
                            />
                          </div>
                          {phase.descrizione && (
                            <p className="text-sm text-gray-500 mb-2">{phase.descrizione}</p>
                          )}
                          <div className="flex flex-wrap gap-4 text-sm text-gray-500">
                            <div className="flex items-center gap-1">
                              <Calendar className="w-4 h-4" />
                              <span>Inizio: {formatDate(phase.dataInizioPrevista)}</span>
                            </div>
                            <div className="flex items-center gap-1">
                              <Calendar className="w-4 h-4" />
                              <span>Fine: {formatDate(phase.dataFineEffettiva || phase.dataFinePrevista)}</span>
                            </div>
                            {phase.squadra && (
                              <div className="flex items-center gap-1">
                                <Users className="w-4 h-4" />
                                <span>{phase.squadra.nome}</span>
                              </div>
                            )}
                          </div>
                        </div>
                        <ChevronRight className="w-5 h-5 text-gray-400 flex-shrink-0 mt-1" />
                      </div>
                    </CardBody>
                  </Card>
                </div>
              );
            })}
        </div>
      </div>
    )}
  </div>
)}

        {activeTab === 'tecnici' && (
          <Card hover onClick={onOpenTechnicalDocs}>
            <CardBody className="p-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-lg bg-blue-50 flex items-center justify-center">
                    <FileText className="w-6 h-6 text-blue-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">Documenti Tecnici</h3>
                    <p className="text-sm text-gray-500">Piante, prospetti, foto e permessi</p>
                  </div>
                </div>
                <ChevronRight className="w-5 h-5 text-gray-400" />
              </div>
            </CardBody>
          </Card>
        )}

        {activeTab === 'contabili' && (
          <Card hover onClick={onOpenAccountingDocs}>
            <CardBody className="p-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-lg bg-green-50 flex items-center justify-center">
                    <Receipt className="w-6 h-6 text-green-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">Documenti Contabili</h3>
                    <p className="text-sm text-gray-500">Fatture e preventivi</p>
                  </div>
                </div>
                <ChevronRight className="w-5 h-5 text-gray-400" />
              </div>
            </CardBody>
          </Card>
        )}
        <ConfirmDialog
  isOpen={showTerminaDialog}
  title="Termina Cantiere"
  message={`Sei sicuro di voler terminare "${site.nome}"? La data di fine effettiva sarà impostata ad oggi. Questa operazione è irreversibile.`}
  confirmLabel="Termina"
  onConfirm={async () => {
    setShowTerminaDialog(false);
    setTerminaError('');
    try {
      await terminaCantiere(Number(site.id));
      onBack();
    } catch (err: any) {
      setTerminaError(err.message || 'Errore durante la chiusura del cantiere');
    }
  }}
  onCancel={() => setShowTerminaDialog(false)}
  variant="danger"
/>
      </main>
    </div>
  )
  ;
}
