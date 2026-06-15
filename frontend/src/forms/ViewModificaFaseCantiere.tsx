import React, { useState } from 'react';
import { ArrowLeft, Save, Clock } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Textarea } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { WorkPhase, ConstructionSite } from '../types';
import { modificaFase } from '../services/api';

interface ViewModificaFaseCantiereProps {
  phase: WorkPhase;
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewModificaFaseCantiere({ phase, site, onBack, onSuccess }: ViewModificaFaseCantiereProps) {
  const [formData, setFormData] = useState({
    nome: phase.nome,
    descrizione: phase.descrizione || '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    if (!formData.nome.trim()) {
      setErrors({ nome: 'Il nome è obbligatorio' });
      return;
    }

    setIsLoading(true);
    try {
      await modificaFase(Number(phase.id), {
        nome: formData.nome,
        descrizione: formData.descrizione,
      });
      onSuccess();
    } catch (err: any) {
      setErrors({ nome: err.message || 'Errore durante il salvataggio' });
    }
    setIsLoading(false);
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
                <p className="text-gray-500">Data Inizio Prevista</p>
                <p className="font-medium text-gray-900">
                  {new Date(phase.dataInizioPrevista + 'T00:00:00').toLocaleDateString('it-IT')}
                </p>
              </div>
              <div>
                <p className="text-gray-500">Data Fine Prevista</p>
                <p className="font-medium text-gray-900">
                  {new Date(phase.dataFinePrevista + 'T00:00:00').toLocaleDateString('it-IT')}
                </p>
              </div>
            </div>
            <p className="text-xs text-gray-400 mt-2">
              Le date non sono modificabili
            </p>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Nome Fase"
                value={formData.nome}
                onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                error={errors.nome}
                required
              />
              <Textarea
                label="Descrizione"
                value={formData.descrizione}
                onChange={(e) => setFormData({ ...formData, descrizione: e.target.value })}
                rows={3}
              />
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