import React, { useState } from 'react';
import { ArrowLeft, Save, FileText, Upload } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { useApp } from '../context/AppContext';
import { ConstructionSite, TechnicalDocumentType } from '../types';

interface ViewAggiungiDocumentoTecnicoProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

const documentTypes: { value: TechnicalDocumentType; label: string }[] = [
  { value: 'prospetto', label: 'Prospetto' },
  { value: 'pianta', label: 'Pianta' },
  { value: 'foto', label: 'Foto' },
  { value: 'permesso', label: 'Permesso' },
  { value: 'relazione', label: 'Relazione' },
  { value: 'altro', label: 'Altro' },
];

export function ViewAggiungiDocumentoTecnico({ site, onBack, onSuccess }: ViewAggiungiDocumentoTecnicoProps) {
  const { addTechnicalDocument, workPhases } = useApp();
  const [formData, setFormData] = useState({
    nome: '',
    tipologia: 'altro' as TechnicalDocumentType,
    data: new Date().toISOString().split('T')[0],
    faseId: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [fileName, setFileName] = useState('');

  const sitePhases = workPhases.filter(p => p.cantiereId === site.id);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const newErrors: Record<string, string> = {};

    if (!formData.nome.trim()) {
      newErrors.nome = 'Il nome è obbligatorio';
    }

    if (!formData.data) {
      newErrors.data = 'La data è obbligatoria';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setIsLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 500));

    addTechnicalDocument({
      cantiereId: site.id,
      faseId: formData.faseId || undefined,
      nome: formData.nome,
      tipologia: formData.tipologia,
      data: formData.data,
      fileUrl: fileName ? `/documents/${fileName}` : `/documents/doc-${Date.now()}.pdf`,
    });

    setIsLoading(false);
    onSuccess();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setFileName(file.name);
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

              <Select
                label="Tipologia"
                value={formData.tipologia}
                onChange={(e) => setFormData({ ...formData, tipologia: e.target.value as TechnicalDocumentType })}
                options={documentTypes}
                required
              />

              <Input
                label="Data"
                type="date"
                value={formData.data}
                onChange={(e) => setFormData({ ...formData, data: e.target.value })}
                error={errors.data}
                required
              />

              <Select
                label="Fase Associata (opzionale)"
                value={formData.faseId}
                onChange={(e) => setFormData({ ...formData, faseId: e.target.value })}
                options={[
                  { value: '', label: 'Cantiere generale' },
                  ...sitePhases.map(p => ({ value: p.id, label: p.nome })),
                ]}
              />

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  File (.pdf, .jpg, .png)
                </label>
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-amber-500 transition-colors cursor-pointer">
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    onChange={handleFileChange}
                    className="hidden"
                    id="file-upload"
                  />
                  <label htmlFor="file-upload" className="cursor-pointer">
                    <Upload className="w-8 h-8 text-gray-400 mx-auto mb-2" />
                    {fileName ? (
                      <p className="text-sm text-gray-900">{fileName}</p>
                    ) : (
                      <>
                        <p className="text-sm text-gray-600">Clicca per caricare un file</p>
                        <p className="text-xs text-gray-400 mt-1">PDF, JPG, PNG fino a 10MB</p>
                      </>
                    )}
                  </label>
                </div>
              </div>

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
