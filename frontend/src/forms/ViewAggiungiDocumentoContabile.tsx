import React, { useState, useEffect } from 'react';
import { ArrowLeft, Save, Receipt } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { ConstructionSite } from '../types';
import { aggiungiDocumentoContabile, getFasi } from '../services/api';

interface ViewAggiungiDocumentoContabileProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewAggiungiDocumentoContabile({ site, onBack, onSuccess }: ViewAggiungiDocumentoContabileProps) {
  const [fasi, setFasi] = useState<any[]>([]);
 const [formData, setFormData] = useState({
  nome: '',
  tipo: 'Fattura',
  importo: '',
  fileUrl: '',
  faseId: '',
  data: new Date().toISOString().split('T')[0],
});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    getFasi(Number(site.id)).then(setFasi).catch(console.error);
  }, [site.id]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.nome.trim()) newErrors.nome = 'Il nome è obbligatorio';
    const importoNum = parseFloat(formData.importo);
    if (!formData.importo || isNaN(importoNum) || importoNum <= 0) {
      newErrors.importo = "L'importo deve essere maggiore di 0";
    }
    if (!formData.fileUrl.trim()) newErrors.fileUrl = 'Il link al file è obbligatorio';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    try {
      await aggiungiDocumentoContabile(
  Number(site.id),
  formData.nome,
  formData.tipo,
  parseFloat(formData.importo),
  formData.fileUrl,
  formData.data,
  formData.faseId ? Number(formData.faseId) : undefined
 );
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
          <span>Torna ai documenti</span>
        </button>

        <div className="mb-6">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center">
              <Receipt className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Aggiungi Documento Contabile</h1>
              <p className="text-gray-500">{site.nome}</p>
            </div>
          </div>
        </div>

        <Card>
          <CardBody className="p-6">
            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Nome Documento"
                value={formData.nome}
                onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                error={errors.nome}
                placeholder="es. Fattura lavori elettrici"
                required
              />

              <Select
                label="Tipo Documento"
                value={formData.tipo}
                onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
                options={[
                  { value: 'Fattura', label: 'Fattura' },
                  { value: 'Preventivo', label: 'Preventivo' },
                ]}
                required
              />

              <Input
                label="Importo (Euro)"
                type="number"
                step="0.01"
                min="0.01"
                value={formData.importo}
                onChange={(e) => setFormData({ ...formData, importo: e.target.value })}
                error={errors.importo}
                placeholder="0.00"
                required
              />

              <Input
                label="Link al file"
                value={formData.fileUrl}
                onChange={(e) => setFormData({ ...formData, fileUrl: e.target.value })}
                error={errors.fileUrl}
                placeholder="es. https://drive.google.com/..."
                required
              />

<Input
  label="Data caricamento"
  type="date"
  value={formData.data}
  onChange={(e) => setFormData({ ...formData, data: e.target.value })}
  required
/>
              <Select
                label="Fase Associata (opzionale)"
                value={formData.faseId}
                onChange={(e) => setFormData({ ...formData, faseId: e.target.value })}
                options={[
                  { value: '', label: 'Cantiere generale' },
                  ...fasi.map(f => ({ value: f.id.toString(), label: f.nome })),
                ]}
              />

              <div className="flex gap-3 pt-4">
                <Button type="button" variant="secondary" onClick={onBack}>
                  Annulla
                </Button>
                <Button type="submit" isLoading={isLoading} icon={<Save className="w-4 h-4" />}>
                  Salva Documento
                </Button>
              </div>
            </form>
          </CardBody>
        </Card>
      </main>
    </div>
  );
}