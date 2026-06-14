import React from 'react';
import { CheckCircle, AlertCircle, Clock } from 'lucide-react';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { useApp } from '../context/AppContext';
import { WorkPhase, ConstructionSite } from '../types';

interface ViewTerminaFaseCantiereProps {
  phase: WorkPhase;
  site: ConstructionSite;
  isOpen: boolean;
  onCancel: () => void;
  onSuccess: () => void;
}

export function ViewTerminaFaseCantiere({
  phase,
  site,
  isOpen,
  onCancel,
  onSuccess,
}: ViewTerminaFaseCantiereProps) {
  const { completeWorkPhase } = useApp();

  const handleConfirm = () => {
    completeWorkPhase(phase.id);
    onSuccess();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:p-0">
        <div className="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75" onClick={onCancel} />

        <div className="relative inline-block w-full max-w-md p-6 my-8 overflow-hidden text-left align-middle transition-all transform bg-white shadow-xl rounded-2xl">
          <div className="flex items-start gap-4 mb-6">
            <div className="flex-shrink-0 w-12 h-12 rounded-full bg-green-100 flex items-center justify-center">
              <CheckCircle className="w-6 h-6 text-green-600" />
            </div>

            <div className="flex-1">
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Termina Fase</h3>
              <p className="text-sm text-gray-500">
                Confermi di voler marcare questa fase come completata?
              </p>
            </div>
          </div>

          {/* Phase Info */}
          <Card className="mb-6 bg-gray-50">
            <CardBody className="p-4">
              <div className="flex items-center gap-3 mb-3">
                <Clock className="w-5 h-5 text-gray-400" />
                <div>
                  <p className="font-medium text-gray-900">{phase.nome}</p>
                  <p className="text-sm text-gray-500">{site.nome}</p>
                </div>
              </div>
              <div className="text-sm text-gray-600">
                <p><strong>Inizio:</strong> {new Date(phase.dataInizio).toLocaleDateString('it-IT')}</p>
                <p><strong>Fine prevista:</strong> {new Date(phase.dataFinePrevista).toLocaleDateString('it-IT')}</p>
              </div>
            </CardBody>
          </Card>

          <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-6">
            <div className="flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-red-700 flex-shrink-0 mt-0.5" />
              <p className="text-sm text-red-900">
                Questa operazione è irreversibile. La fase diventerà in sola lettura.
              </p>
            </div>
          </div>

          <div className="flex gap-3 justify-end">
            <Button variant="secondary" onClick={onCancel}>
              Annulla
            </Button>
            <Button onClick={handleConfirm} icon={<CheckCircle className="w-4 h-4" />}>
              Termina Fase
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
