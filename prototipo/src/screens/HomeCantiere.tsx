import React, { useState } from 'react';
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
  const { getPhasesBySite, getTeamById } = useApp();
  const [activeTab, setActiveTab] = useState<TabType>('fasi');

  const phases = getPhasesBySite(site.id);

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  const tabs: { id: TabType; label: string; icon: React.ReactNode }[] = [
    { id: 'fasi', label: 'Fasi', icon: <Clock className="w-4 h-4" /> },
    { id: 'tecnici', label: 'Documenti Tecnici', icon: <FileText className="w-4 h-4" /> },
    { id: 'contabili', label: 'Documenti Contabili', icon: <Receipt className="w-4 h-4" /> },
  ];

  // Check if site can be closed (not already terminated)
  const canCloseSite = site.stato !== 'Terminato';

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
                    <span className="text-gray-600">Inizio:</span>
                    <span className="font-medium text-gray-900">{formatDate(site.dataInizio)}</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm">
                    <Calendar className="w-4 h-4 text-gray-400" />
                    <span className="text-gray-600">Fine stimata:</span>
                    <span className="font-medium text-gray-900">{formatDate(site.dataFineStimata)}</span>
                  </div>
                  {site.dataConsegnaEffettiva && (
                    <div className="flex items-center gap-2 text-sm">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span className="text-gray-600">Consegna:</span>
                      <span className="font-medium text-gray-900">{formatDate(site.dataConsegnaEffettiva)}</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="flex flex-col sm:flex-row gap-3">
                <StatusBadge
                  status={site.stato}
                  variant={getSiteStatusVariant(site.stato)}
                  size="lg"
                />

                {!readOnly && (
                  <>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={onEditSite}
                      disabled={site.stato === 'Terminato'}
                      icon={<Edit className="w-4 h-4" />}
                    >
                      Modifica
                    </Button>
                    {canCloseSite && onCloseSite && (
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={onCloseSite}
                        icon={<CheckCircle className="w-4 h-4" />}
                      >
                        Termina
                      </Button>
                    )}
                  </>
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
            {!readOnly && onAddPhase && site.stato !== 'Terminato' && (
              <div className="mb-4">
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
              <div className="space-y-3">
                {phases
                  .sort((a, b) => new Date(a.dataInizio).getTime() - new Date(b.dataInizio).getTime())
                  .map((phase, index) => {
                  const team = getTeamById(phase.squadraId);
                  return (
                    <Card
                      key={phase.id}
                      hover
                      onClick={() => onSelectPhase(phase)}
                    >
                      <CardBody className="p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-4">
                            <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-sm font-medium text-gray-600">
                              {index + 1}
                            </div>
                            <div>
                              <h4 className="font-medium text-gray-900">{phase.nome}</h4>
                              <div className="flex items-center gap-4 mt-1 text-sm text-gray-500">
                                <span>
                                  {formatDate(phase.dataInizio)} - {formatDate(phase.dataFineEffettiva || phase.dataFinePrevista)}
                                </span>
                                {team && (
                                  <div className="flex items-center gap-1">
                                    <Users className="w-4 h-4" />
                                    <span>{team.nome}</span>
                                  </div>
                                )}
                              </div>
                            </div>
                          </div>

                          <div className="flex items-center gap-3">
                            <StatusBadge
                              status={phase.stato}
                              variant={getPhaseStatusVariant(phase.stato)}
                            />
                            <ChevronRight className="w-5 h-5 text-gray-400" />
                          </div>
                        </div>
                      </CardBody>
                    </Card>
                  );
                })}
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
      </main>
    </div>
  );
}
