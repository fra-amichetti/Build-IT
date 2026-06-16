import React, { useState, useEffect } from 'react';
import { ArrowLeft, Save, FileText, Link } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { ConstructionSite } from '../types';
import { aggiungiDocumentoTecnico, getFasi } from '../services/api';

interface ViewAggiungiDocumentoTecnicoProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}
 
export function ViewAggiungiDocumentoTecnico({ site, onBack, onSuccess }: ViewAggiungiDocumentoTecnicoProps) {
  const [fasi, setFasi] = useState<any[]>([]);
  const [formData, setFormData] = useState({
  nome: '',
  tipologia: '',
  fileUrl: '',
  faseId: '',
  data: new Date().toISOString().split('T')[0],
});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    getFasi(Number(site.id)).then(setFasi).catch(console.error);
  }, [site.id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (!formData.nome.trim()) newErrors.nome = 'Il nome è obbligatorio';
    if (!formData.fileUrl.trim()) newErrors.fileUrl = 'Il link al file è obbligatorio';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setIsLoading(true);
    try {
     await aggiungiDocumentoTecnico(
  Number(site.id),
  formData.nome,
  formData.tipologia,
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
            <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center">
              <FileText className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Aggiungi Documento Tecnico</h1>
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
                placeholder="es. Pianta piano primo"
                required
              />
<Input
  label="Tipologia"
  value={formData.tipologia}
  onChange={(e) => setFormData({ ...formData, tipologia: e.target.value })}
  error={errors.tipologia}
  placeholder="es. Pianta, Foto, Permesso..."
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