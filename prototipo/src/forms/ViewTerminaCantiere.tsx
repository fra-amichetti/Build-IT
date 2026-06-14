import React, { useState } from 'react';
import { ArrowLeft, CheckCircle, AlertCircle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { useApp } from '../context/AppContext';
import { ConstructionSite } from '../types';

interface ViewTerminaCantiereProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewTerminaCantiere({ site, onBack, onSuccess }: ViewTerminaCantiereProps) {
  const { closeConstructionSite } = useApp();
  const [dataConsegna, setDataConsegna] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!dataConsegna) {
      setError('La data di consegna è obbligatoria');
      return;
    }

    const startDate = new Date(site.dataInizio);
    const deliveryDate = new Date(dataConsegna);

    if (deliveryDate <= startDate) {
      setError('La data di consegna deve essere successiva alla data di inizio');
      return;
    }

    setIsLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 500));

    const success = closeConstructionSite(site.id, dataConsegna);

    if (success) {
      onSuccess();
    } else {
      setError('Errore durante la chiusura del cantiere');
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

        <div className="mb-6 flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center">
            <CheckCircle className="w-5 h-5 text-green-600" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Termina Cantiere</h1>
            <p className="text-gray-500">Conferma la chiusura del cantiere</p>
          </div>
        </div>

        <Card className="mb-6">
          <CardBody className="p-4 bg-red-50 border-red-200">
            <div className="flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-red-700 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-medium text-red-950">Attenzione!</p>
                <p className="text-sm text-red-800 mt-1">
                  Questa operazione è irreversibile. Una volta terminato, il cantiere non potrà più essere modificato.
                </p>
              </div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="mb-6 p-4 bg-gray-50 rounded-lg">
              <h3 className="font-medium text-gray-900 mb-3">{site.nome}</h3>
              <div className="space-y-2 text-sm">
                <p className="text-gray-600">
                  <span className="font-medium">Indirizzo:</span> {site.indirizzo}
                </p>
                <p className="text-gray-600">
                  <span className="font-medium">Data inizio:</span>{' '}
                  {new Date(site.dataInizio).toLocaleDateString('it-IT')}
                </p>
                <p className="text-gray-600">
                  <span className="font-medium">Fine stimata:</span>{' '}
                  {new Date(site.dataFineStimata).toLocaleDateString('it-IT')}
                </p>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Data Consegna Effettiva"
                type="date"
                value={dataConsegna}
                onChange={(e) => setDataConsegna(e.target.value)}
                error={error}
                required
              />

              <div className="flex gap-3 pt-4">
                <Button type="button" variant="secondary" onClick={onBack}>
                  Annulla
                </Button>
                <Button
                  type="submit"
                  variant="danger"
                  isLoading={isLoading}
                  icon={<CheckCircle className="w-4 h-4" />}
                >
                  Termina Cantiere
                </Button>
              </div>
            </form>
          </CardBody>
        </Card>
      </main>
    </div>
  );
}
