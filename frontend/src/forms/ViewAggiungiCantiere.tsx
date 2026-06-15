import React, { useState } from 'react';
import { ArrowLeft, Save, HardHat } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
// import { useApp } from '../context/AppContext';
import { aggiungiCantiere } from '../services/api';

interface ViewAggiungiCantiereProps {
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewAggiungiCantiere({ onBack, onSuccess }: ViewAggiungiCantiereProps) {
  // const { addConstructionSite } = useApp();
  const [formData, setFormData] = useState({
    nome: '',
    indirizzo: '',
    dataInizio: '',
    dataFineStimata: '',
    emailCliente: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.nome.trim()) {
      newErrors.nome = 'Il nome del cantiere è obbligatorio';
    }

    if (!formData.indirizzo.trim()) {
      newErrors.indirizzo = "L'indirizzo è obbligatorio";
    }

    if (!formData.dataInizio) {
      newErrors.dataInizio = 'La data di inizio è obbligatoria';
    }

    if (!formData.dataFineStimata) {
      newErrors.dataFineStimata = 'La data di fine stimata è obbligatoria';
    } else if (formData.dataInizio && new Date(formData.dataFineStimata) <= new Date(formData.dataInizio)) {
      newErrors.dataFineStimata = 'La data di fine deve essere successiva alla data di inizio';
    }

    if (formData.emailCliente && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.emailCliente)) {
      newErrors.emailCliente = 'Inserisci un indirizzo email valido';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;
  
    setIsLoading(true);
    try {
      await aggiungiCantiere(
        formData.nome,
        formData.indirizzo,
        formData.dataInizio,
        formData.dataFineStimata,
        formData.emailCliente || undefined
      );
      onSuccess();
    } catch (err) { // Rimosso ': any'
      // Verifichiamo in modo sicuro il tipo di errore
      if (err instanceof Error) {
        setErrors({ nome: err.message });
      } else {
        setErrors({ nome: 'Errore nella creazione del cantiere' });
      }
    } finally {
      setIsLoading(false);
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
          <span>Torna alla lista</span>
        </button>

        <div className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-red-100 flex items-center justify-center">
              <HardHat className="w-5 h-5 text-red-700" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900">Aggiungi Cantiere</h1>
          </div>
          <p className="text-gray-500">Inserisci i dati del nuovo cantiere</p>
        </div>

        <Card>
          <CardBody className="p-6">
            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Nome Cantiere"
                value={formData.nome}
                onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                error={errors.nome}
                placeholder="es. Ristrutturazione Villa Roma"
                required
              />

              <Input
                label="Indirizzo"
                value={formData.indirizzo}
                onChange={(e) => setFormData({ ...formData, indirizzo: e.target.value })}
                error={errors.indirizzo}
                placeholder="es. Via delle Rose 45, Roma"
                required
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
                  label="Data Fine Stimata"
                  type="date"
                  value={formData.dataFineStimata}
                  onChange={(e) => setFormData({ ...formData, dataFineStimata: e.target.value })}
                  error={errors.dataFineStimata}
                  helperText="Non modificabile dopo la creazione"
                  required
                />
              </div>

              <Input
                label="Email Cliente Associato"
                type="email"
                value={formData.emailCliente}
                onChange={(e) => setFormData({ ...formData, emailCliente: e.target.value })}
                error={errors.emailCliente}
                placeholder="es. cliente@email.it"
              />

              <div className="flex gap-3 pt-4">
                <Button type="button" variant="secondary" onClick={onBack}>
                  Annulla
                </Button>
                <Button type="submit" isLoading={isLoading} icon={<Save className="w-4 h-4" />}>
                  Salva Cantiere
                </Button>
              </div>
            </form>
          </CardBody>
        </Card>
      </main>
    </div>
  );
}
