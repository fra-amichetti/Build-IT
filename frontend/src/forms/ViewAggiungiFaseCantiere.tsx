import React, { useState, useEffect } from 'react';
import { ArrowLeft, Save, Clock, AlertCircle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select, Textarea } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { ConstructionSite } from '../types';
import { aggiungiFase, getSquadre } from '../services/api';

interface ViewAggiungiFaseCantiereProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewAggiungiFaseCantiere({ site, onBack, onSuccess }: ViewAggiungiFaseCantiereProps) {
  const [squadre, setSquadre] = useState<any[]>([]);
  const [formData, setFormData] = useState({
    nome: '',
    descrizione: '',
    dataInizioPrevista: '',
    dataFinePrevista: '',
    squadraId: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    getSquadre()
      .then(data => { if (Array.isArray(data)) setSquadre(data); })
      .catch(console.error);
  }, []);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.nome.trim()) newErrors.nome = 'Il nome della fase è obbligatorio';
    if (!formData.dataInizioPrevista) newErrors.dataInizioPrevista = 'La data di inizio è obbligatoria';
    if (!formData.dataFinePrevista) {
      newErrors.dataFinePrevista = 'La data di fine prevista è obbligatoria';
    } else if (formData.dataInizioPrevista && new Date(formData.dataFinePrevista) <= new Date(formData.dataInizioPrevista)) {
      newErrors.dataFinePrevista = 'La data di fine deve essere successiva alla data di inizio';
    }
    if (!formData.squadraId) newErrors.squadraId = 'La squadra è obbligatoria';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitError('');
    if (!validateForm()) return;

    setIsLoading(true);
    try {
      await aggiungiFase(
        Number(site.id),
        formData.nome,
        formData.descrizione,
        formData.dataInizioPrevista,
        formData.dataFinePrevista,
        formData.squadraId || undefined
      );
      onSuccess();
    } catch (err: any) {
      setSubmitError(err.message || 'Errore durante il salvataggio');
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

        {submitError && (
          <div className="mb-4 flex items-start gap-3 p-4 bg-red-50 border border-red-200 rounded-lg">
            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
            <p className="text-sm text-red-800">{submitError}</p>
          </div>
        )}

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
                  label="Data Inizio Prevista"
                  type="date"
                  value={formData.dataInizioPrevista}
                  onChange={(e) => setFormData({ ...formData, dataInizioPrevista: e.target.value })}
                  error={errors.dataInizioPrevista}
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
                required
                options={[
                  { value: '', label: squadre.length === 0 ? 'Caricamento squadre...' : 'Seleziona una squadra...' },
                  ...squadre.map(s => ({
                    value: s.id.toString(),
                    label: `${s.nome} (${s.specializzazione})`,
                  })),
                ]}
              />

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