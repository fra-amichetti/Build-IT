import React, { useState } from 'react';
import { ArrowLeft, Save, Clock, AlertCircle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select, Textarea } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { useApp } from '../context/AppContext';
import { WorkPhase, ConstructionSite } from '../types';

interface ViewModificaFaseCantiereProps {
  phase: WorkPhase;
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewModificaFaseCantiere({ phase, site, onBack, onSuccess }: ViewModificaFaseCantiereProps) {
  const { teams, updateWorkPhase } = useApp();
  const [formData, setFormData] = useState({
    descrizione: phase.descrizione,
    dataFinePrevista: phase.dataFinePrevista,
    squadraId: phase.squadraId,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    if (!formData.dataFinePrevista) {
      setErrors({ dataFinePrevista: 'La data di fine prevista è obbligatoria' });
      return;
    }

    if (new Date(formData.dataFinePrevista) <= new Date(phase.dataInizio)) {
      setErrors({ dataFinePrevista: 'La data di fine deve essere successiva alla data di inizio' });
      return;
    }

    setIsLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 500));

    const result = updateWorkPhase(phase.id, {
      descrizione: formData.descrizione,
      dataFinePrevista: formData.dataFinePrevista,
      squadraId: formData.squadraId,
    });

    setIsLoading(false);

    if (result.success) {
      onSuccess();
    } else {
      setErrors({ squadraId: result.error || 'Errore durante il salvataggio' });
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header showMenuButton onMenuClick={onBack} />

      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Torna alla fase</span>
        </button>

        <div className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center">
              <Clock className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Modifica Fase</h1>
              <p className="text-gray-500">{phase.nome}</p>
            </div>
          </div>
        </div>

        <Card className="mb-6">
          <CardBody className="p-4 bg-gray-50">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-gray-500">Nome Fase</p>
                <p className="font-medium text-gray-900">{phase.nome}</p>
              </div>
              <div>
                <p className="text-gray-500">Data Inizio</p>
                <p className="font-medium text-gray-900">
                  {new Date(phase.dataInizio).toLocaleDateString('it-IT')}
                </p>
              </div>
            </div>
            <p className="text-xs text-gray-400 mt-2">
              Nome e data inizio non sono modificabili
            </p>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <form onSubmit={handleSubmit} className="space-y-5">
              <Textarea
                label="Descrizione"
                value={formData.descrizione}
                onChange={(e) => setFormData({ ...formData, descrizione: e.target.value })}
                rows={3}
              />

              <Input
                label="Nuova Data Fine Prevista"
                type="date"
                value={formData.dataFinePrevista}
                onChange={(e) => setFormData({ ...formData, dataFinePrevista: e.target.value })}
                error={errors.dataFinePrevista}
                required
              />

              <Select
                label="Squadra Assegnata"
                value={formData.squadraId}
                onChange={(e) => setFormData({ ...formData, squadraId: e.target.value })}
                error={errors.squadraId}
                options={teams.map(team => ({
                  value: team.id,
                  label: `${team.nome} (${team.specializzazione})`,
                }))}
              />

              {errors.squadraId && (
                <div className="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-lg">
                  <AlertCircle className="w-4 h-4 text-red-600" />
                  <p className="text-sm text-red-700">{errors.squadraId}</p>
                </div>
              )}

              <div className="flex gap-3 pt-4">
                <Button type="button" variant="secondary" onClick={onBack}>
                  Annulla
                </Button>
                <Button type="submit" isLoading={isLoading} icon={<Save className="w-4 h-4" />}>
                  Salva Modifiche
                </Button>
              </div>
            </form>
          </CardBody>
        </Card>
      </main>
    </div>
  );
}
