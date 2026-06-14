import React, { useState } from 'react';
import { ArrowLeft, Save, HardHat } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { useApp } from '../context/AppContext';
import { ConstructionSite } from '../types';

interface ViewModificaCantiereProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewModificaCantiere({ site, onBack, onSuccess }: ViewModificaCantiereProps) {
  const { updateConstructionSite } = useApp();
  const [formData, setFormData] = useState({
    nome: site.nome,
    indirizzo: site.indirizzo,
    emailCliente: site.emailCliente || '',
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
    await new Promise((resolve) => setTimeout(resolve, 500));

    updateConstructionSite(site.id, {
      nome: formData.nome,
      indirizzo: formData.indirizzo,
      emailCliente: formData.emailCliente || undefined,
    });

    setIsLoading(false);
    onSuccess();
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
            <div className="w-10 h-10 rounded-lg bg-red-100 flex items-center justify-center">
              <HardHat className="w-5 h-5 text-red-700" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900">Modifica Cantiere</h1>
          </div>
          <p className="text-gray-500">Modifica i dati del cantiere</p>
        </div>

        <Card>
          <CardBody className="p-6">
            <div className="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
              <p className="text-sm text-gray-600">
                <strong>Data Fine Stimata:</strong>{' '}
                {new Date(site.dataFineStimata).toLocaleDateString('it-IT', {
                  day: '2-digit',
                  month: 'long',
                  year: 'numeric',
                })}
              </p>
              <p className="text-xs text-gray-500 mt-1">
                La data di fine stimata non può essere modificata
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Nome Cantiere"
                value={formData.nome}
                onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                error={errors.nome}
                required
              />

              <Input
                label="Indirizzo"
                value={formData.indirizzo}
                onChange={(e) => setFormData({ ...formData, indirizzo: e.target.value })}
                error={errors.indirizzo}
                required
              />

              <Input
                label="Email Cliente Associato"
                type="email"
                value={formData.emailCliente}
                onChange={(e) => setFormData({ ...formData, emailCliente: e.target.value })}
                error={errors.emailCliente}
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
