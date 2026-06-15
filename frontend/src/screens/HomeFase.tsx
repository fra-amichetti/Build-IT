import React from 'react';
import {
  ArrowLeft, Calendar, Users, Clock, Edit, CheckCircle, FileText,
  AlertCircle, MapPin
} from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { StatusBadge, getPhaseStatusVariant } from '../components/shared/StatusBadge';
import { useApp } from '../context/AppContext';
import { WorkPhase, ConstructionSite, TechnicalDocument, AccountingDocument } from '../types';
import { avviaFase } from '../services/api';

interface HomeFaseProps {
  phase: WorkPhase;
  site: ConstructionSite;
  onBack: () => void;
  onEditPhase?: () => void;
  onCompletePhase?: () => void;
  readOnly?: boolean;
}

export function HomeFase({
  phase,
  site,
  onBack,
  onEditPhase,
  onCompletePhase,
  readOnly = false,
}: HomeFaseProps) {
  const { getTeamById, technicalDocuments, accountingDocuments } = useApp();

  const team = phase.squadra || (phase.squadraId ? getTeamById(phase.squadraId) : undefined);
  const phaseTechDocs = technicalDocuments.filter(d => d.faseId === phase.id);
  const phaseAccDocs = accountingDocuments.filter(d => d.faseId === phase.id);

  const formatDate = (dateStr?: string) => {
  if (!dateStr) return '—';
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('it-IT', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
};

 const canModify = phase.stato !== 'TERMINATA' && site.stato !== 'TERMINATO';function mapStatoFase(stato: string): string {
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

      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Torna al cantiere</span>
        </button>

        {/* Phase Header */}
       <Card className="mb-6">
  <CardBody className="p-6">
    <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-6">
      <div className="flex-1">
        <div className="flex items-start gap-4">
          <div className="w-14 h-14 rounded-xl bg-blue-100 flex items-center justify-center flex-shrink-0">
            <Clock className="w-7 h-7 text-blue-600" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{phase.nome}</h1>
            <div className="flex items-center gap-2 text-gray-600 mt-1">
              <MapPin className="w-4 h-4" />
              <span>{site.nome}</span>
            </div>
            {phase.descrizione && (
              <p className="text-gray-500 mt-3 max-w-2xl">{phase.descrizione}</p>
            )}
          </div>
        </div>

        <div className="mt-6 grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-3 bg-gray-50 rounded-lg">
            <p className="text-xs text-gray-500 mb-1">Inizio Previsto</p>
            <p className="font-medium text-gray-900">{formatDate(phase.dataInizioPrevista)}</p>
          </div>
          {phase.dataInizioEffettiva && (
            <div className="p-3 bg-blue-50 rounded-lg">
              <p className="text-xs text-blue-600 mb-1">Inizio Effettivo</p>
              <p className="font-medium text-blue-900">{formatDate(phase.dataInizioEffettiva)}</p>
            </div>
          )}
          <div className="p-3 bg-gray-50 rounded-lg">
            <p className="text-xs text-gray-500 mb-1">Fine Prevista</p>
            <p className="font-medium text-gray-900">{formatDate(phase.dataFinePrevista)}</p>
          </div>
          {phase.dataFineEffettiva && (
            <div className="p-3 bg-green-50 rounded-lg">
              <p className="text-xs text-green-600 mb-1">Fine Effettiva</p>
              <p className="font-medium text-green-900">{formatDate(phase.dataFineEffettiva)}</p>
            </div>
          )}
          {phase.squadra && (
            <div className="p-3 bg-red-50 rounded-lg">
              <p className="text-xs text-red-700 mb-1">Squadra</p>
              <p className="font-medium text-red-950">{phase.squadra.nome}</p>
            </div>
          )}
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <StatusBadge
          status={mapStatoFase(phase.stato)}
          variant={getPhaseStatusVariant(mapStatoFase(phase.stato))}
          size="lg"
        />
        {!readOnly && mapStatoFase(phase.stato) === 'Pianificata' && (
          <Button
            variant="secondary"
            size="sm"
            onClick={async () => {
              try {
                await avviaFase(Number(phase.id));
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
        {!readOnly && canModify && (
          <>
            <Button
              variant="secondary"
              size="sm"
              onClick={onEditPhase}
              icon={<Edit className="w-4 h-4" />}
            >
              Modifica
            </Button>
            {phase.stato !== 'TERMINATA' && onCompletePhase && (
              <Button
                variant="primary"
                size="sm"
                onClick={onCompletePhase}
                icon={<CheckCircle className="w-4 h-4" />}
              >
                Termina Fase
              </Button>
            )}
          </>
        )}
      </div>
    </div>
  </CardBody>
</Card>
        {/* Team Info */}
        {team && (
          <Card className="mb-6">
            <CardBody className="p-6">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-lg bg-purple-50 flex items-center justify-center">
                  <Users className="w-6 h-6 text-purple-600" />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold text-gray-900">{team.nome}</h3>
                  <div className="flex items-center gap-4 mt-1 text-sm text-gray-600">
                    <span>Specializzazione: {team.specializzazione}</span>
                    <span>Componenti: {team.numeroComponenti}</span>
                    <span>Referente: {team.nomeReferente}</span>
                  </div>
                </div>
              </div>
            </CardBody>
          </Card>
        )}

        {/* Documents Summary */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Card>
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
                    <FileText className="w-5 h-5 text-blue-600" />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Documenti Tecnici</p>
                    <p className="text-sm text-gray-500">{phaseTechDocs.length} documenti</p>
                  </div>
                </div>
              </div>
            </CardBody>
          </Card>

          <Card>
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center">
                    <FileText className="w-5 h-5 text-green-600" />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Documenti Contabili</p>
                    <p className="text-sm text-gray-500">{phaseAccDocs.length} documenti</p>
                  </div>
                </div>
              </div>
            </CardBody>
          </Card>
        </div>
      </main>
    </div>
  );
}
