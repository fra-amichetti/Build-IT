import React, { useState } from 'react';
import { ArrowLeft, Plus, FileText, Download, Trash2, Calendar, Tag, FolderOpen } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { useApp } from '../context/AppContext';
import { ConstructionSite, TechnicalDocument } from '../types';

interface HomeDocumentiTecniciProps {
  site: ConstructionSite;
  onBack: () => void;
  onAddDocument?: () => void;
  readOnly?: boolean;
}

const documentTypeLabels: Record<string, string> = {
  prospetto: 'Prospetto',
  pianta: 'Pianta',
  foto: 'Foto',
  permesso: 'Permesso',
  relazione: 'Relazione',
  altro: 'Altro',
};

export function HomeDocumentiTecnici({
  site,
  onBack,
  onAddDocument,
  readOnly = false,
}: HomeDocumentiTecniciProps) {
  const { technicalDocuments, workPhases, deleteTechnicalDocument } = useApp();
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const siteDocs = technicalDocuments.filter(d => d.cantiereId === site.id);

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  const getPhaseName = (phaseId?: string) => {
    if (!phaseId) return 'Cantiere generale';
    const phase = workPhases.find(p => p.id === phaseId);
    return phase ? phase.nome : 'Fase sconosciuta';
  };

  const handleDelete = () => {
    if (deleteId) {
      deleteTechnicalDocument(deleteId);
      setDeleteId(null);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header showMenuButton onMenuClick={onBack} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Torna al cantiere</span>
        </button>

        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Documenti Tecnici</h1>
            <p className="text-gray-500 mt-1">{site.nome}</p>
          </div>

          {!readOnly && onAddDocument && (
            <Button onClick={onAddDocument} icon={<Plus className="w-4 h-4" />}>
              Aggiungi Documento
            </Button>
          )}
        </div>

        {siteDocs.length === 0 ? (
          <div className="text-center py-12">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
              <FolderOpen className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nessun documento</h3>
            <p className="text-gray-500">
              {readOnly ? 'Non ci sono documenti tecnici per questo cantiere' : 'Aggiungi il primo documento tecnico'}
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {siteDocs.map(doc => (
              <Card key={doc.id}>
                <CardBody className="p-5">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-4 flex-1">
                      <div className="w-12 h-12 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
                        <FileText className="w-6 h-6 text-blue-600" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-semibold text-gray-900 mb-1">{doc.nome}</h3>
                        <div className="flex flex-wrap items-center gap-3 text-sm text-gray-500">
                          <div className="flex items-center gap-1">
                            <Tag className="w-4 h-4" />
                            <span>{documentTypeLabels[doc.tipologia]}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Calendar className="w-4 h-4" />
                            <span>{formatDate(doc.data)}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <FolderOpen className="w-4 h-4" />
                            <span>{getPhaseName(doc.faseId)}</span>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => window.open(doc.fileUrl, '_blank')}
                        icon={<Download className="w-4 h-4" />}
                      >
                        <span className="hidden sm:inline">Scarica</span>
                      </Button>
                      {!readOnly && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setDeleteId(doc.id)}
                          className="text-red-600 hover:text-red-700 hover:bg-red-50"
                          icon={<Trash2 className="w-4 h-4" />}
                        >
                          <span className="hidden sm:inline">Elimina</span>
                        </Button>
                      )}
                    </div>
                  </div>
                </CardBody>
              </Card>
            ))}
          </div>
        )}

        <ConfirmDialog
          isOpen={!!deleteId}
          title="Elimina Documento"
          message="Sei sicuro di voler eliminare questo documento? Questa operazione non può essere annullata."
          confirmLabel="Elimina"
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          variant="danger"
        />
      </main>
    </div>
  );
}
