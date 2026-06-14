import React, { useState } from 'react';
import { ArrowLeft, Save, Clock, AlertCircle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select, Textarea } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { useApp } from '../context/AppContext';
import { ConstructionSite } from '../types';

interface ViewAggiungiFaseCantiereProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewAggiungiFaseCantiere({ site, onBack, onSuccess }: ViewAggiungiFaseCantiereProps) {
  const { teams, addWorkPhase } = useApp();
  const [formData, setFormData] = useState({
    nome: '',
    descrizione: '',
    dataInizio: '',
    dataFinePrevista: '',
    squadraId: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const availableTeams = teams.filter(team => {
    // In a real app, we'd check team availability here
    return true;
  });

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.nome.trim()) {
      newErrors.nome = 'Il nome della fase è obbligatorio';
    }

    if (!formData.dataInizio) {
      newErrors.dataInizio = 'La data di inizio è obbligatoria';
    }

    if (!formData.dataFinePrevista) {
      newErrors.dataFinePrevista = 'La data di fine prevista è obbligatoria';
    } else if (formData.dataInizio && new Date(formData.dataFinePrevista) <= new Date(formData.dataInizio)) {
      newErrors.dataFinePrevista = 'La data di fine deve essere successiva alla data di inizio';
    }

    if (formData.squadraId) {
      // Check team overlap
      const teamOverlap = teams.some(t => t.id === formData.squadraId);
      // This is simplified - actual overlap check happens in context
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 500));

    let stato: 'Pianificata' | 'In Corso' = 'Pianificata';
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const startDate = new Date(formData.dataInizio);
    startDate.setHours(0, 0, 0, 0);

    if (startDate <= today) {
      stato = 'In Corso';
    }

    const result = addWorkPhase({
      cantiereId: site.id,
      nome: formData.nome,
      descrizione: formData.descrizione,
      dataInizio: formData.dataInizio,
      dataFinePrevista: formData.dataFinePrevista,
      squadraId: formData.squadraId || 'team-1', // Default to first team if none selected
      stato,
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
          <span>Torna al cantiere</span>
        </button>

        <div className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center">
              <Clock className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Aggiungi Fase</h1>
              <p className="text-gray-500">{site.nome}</p>
            </div>
          </div>
        </div>

        <Card>
          <CardBody className="p-6">
            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Nome Fase"
                value={formData.nome}
                onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                error={errors.nome}
                placeholder="es. Demolizione, Impianto elettrico..."
                required
              />

              <Textarea
                label="Descrizione"
                value={formData.descrizione}
                onChange={(e) => setFormData({ ...formData, descrizione: e.target.value })}
                placeholder="Descrizione dettagliata della fase di lavoro..."
                rows={3}
              />

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <Input
                  label="Data Inizio"
                  type="date"
                  value={formData.dataInizio}
                  onChange={(e) => setFormData({ ...formData, dataInizio: e.target.value })}
                  error={errors.dataInizio}
                  required
                />

                <Input
                  label="Data Fine Prevista"
                  type="date"
                  value={formData.dataFinePrevista}
                  onChange={(e) => setFormData({ ...formData, dataFinePrevista: e.target.value })}
                  error={errors.dataFinePrevista}
                  required
                />
              </div>

              <Select
                label="Squadra Assegnata"
                value={formData.squadraId}
                onChange={(e) => setFormData({ ...formData, squadraId: e.target.value })}
                error={errors.squadraId}
                options={[
                  { value: '', label: 'Seleziona una squadra...' },
                  ...availableTeams.map(team => ({
                    value: team.id,
                    label: `${team.nome} (${team.specializzazione})`,
                  })),
                ]}
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
                  Salva Fase
                </Button>
              </div>
            </form>
          </CardBody>
        </Card>
      </main>
    </div>
  );
}
