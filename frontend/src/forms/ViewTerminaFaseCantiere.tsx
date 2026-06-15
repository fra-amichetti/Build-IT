import React from 'react';
import { CheckCircle, AlertCircle, Clock } from 'lucide-react';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { WorkPhase, ConstructionSite } from '../types';
import { terminaFase } from '../services/api';

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

  const handleConfirm = async () => {
    try {
      await terminaFase(Number(phase.id));
      onSuccess();
    } catch (err: any) {
      alert(err.message);
    }
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
        Sei sicuro di voler terminare questa fase? La data di fine effettiva sarà impostata ad oggi. Questa operazione è irreversibile.
      </p>
    </div>
  </div>

  <div className="flex gap-3 justify-end">
    <Button variant="secondary" onClick={onCancel}>
      Annulla
    </Button>
    <Button variant="danger" onClick={handleConfirm} icon={<CheckCircle className="w-4 h-4" />}>
      Termina Fase
    </Button>
  </div>
</div>
      </div>
    </div>
  );
}