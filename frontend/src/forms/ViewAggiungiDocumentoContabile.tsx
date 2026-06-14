import React, { useState } from 'react';
import { ArrowLeft, Save, Receipt, Upload } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select } from '../components/shared/Input';
import { Card, CardBody } from '../components/shared/Card';
import { useApp } from '../context/AppContext';
import { ConstructionSite, AccountingDocumentType, PaymentStatus } from '../types';

interface ViewAggiungiDocumentoContabileProps {
  site: ConstructionSite;
  onBack: () => void;
  onSuccess: () => void;
}

export function ViewAggiungiDocumentoContabile({ site, onBack, onSuccess }: ViewAggiungiDocumentoContabileProps) {
  const { addAccountingDocument, workPhases } = useApp();
  const [formData, setFormData] = useState({
    nome: '',
    tipo: 'Fattura' as AccountingDocumentType,
    importo: '',
    data: new Date().toISOString().split('T')[0],
    statoPagamento: 'Da Saldare' as PaymentStatus,
    faseId: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [fileName, setFileName] = useState('');

  const sitePhases = workPhases.filter(p => p.cantiereId === site.id);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.nome.trim()) {
      newErrors.nome = 'Il nome è obbligatorio';
    }

    const importoNum = parseFloat(formData.importo);
    if (!formData.importo || isNaN(importoNum) || importoNum <= 0) {
      newErrors.importo = "L'importo deve essere maggiore di 0";
    }

    if (!formData.data) {
      newErrors.data = 'La data è obbligatoria';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 500));

    addAccountingDocument({
      cantiereId: site.id,
      faseId: formData.faseId || undefined,
      nome: formData.nome,
      tipo: formData.tipo,
      importo: parseFloat(formData.importo),
      data: formData.data,
      fileUrl: fileName ? `/documents/${fileName}` : `/documents/doc-${Date.now()}.pdf`,
      statoPagamento: formData.tipo === 'Fattura' ? formData.statoPagamento : undefined,
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
                onChange={(e) => setFormData({ ...formData, tipo: e.target.value as AccountingDocumentType })}
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
                label="Data"
                type="date"
                value={formData.data}
                onChange={(e) => setFormData({ ...formData, data: e.target.value })}
                error={errors.data}
                required
              />

              {formData.tipo === 'Fattura' && (
                <Select
                  label="Stato Pagamento"
                  value={formData.statoPagamento}
                  onChange={(e) => setFormData({ ...formData, statoPagamento: e.target.value as PaymentStatus })}
                  options={[
                    { value: 'Da Saldare', label: 'Da Saldare' },
                    { value: 'Saldato', label: 'Saldato' },
                  ]}
                  required
                />
              )}

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
                  File (.pdf)
                </label>
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-amber-500 transition-colors cursor-pointer">
                  <input
                    type="file"
                    accept=".pdf"
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
                        <p className="text-xs text-gray-400 mt-1">Solo PDF</p>
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
