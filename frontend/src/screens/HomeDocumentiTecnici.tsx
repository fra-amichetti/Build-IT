import React, { useState, useEffect } from 'react';
import { ArrowLeft, Plus, FileText, Download, Trash2, Calendar, Tag, FolderOpen } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { ConstructionSite } from '../types';
import { getDocumentiTecnici, eliminaDocumentoTecnico } from '../services/api';
import { ExternalLink } from 'lucide-react';

interface HomeDocumentiTecniciProps {
  site: ConstructionSite;
  onBack: () => void;
  onAddDocument?: () => void;
  readOnly?: boolean;
}

const documentTypeLabels: Record<string, string> = {
  PROSPETTO: 'Prospetto',
  PIANTA: 'Pianta',
  FOTO: 'Foto',
  PERMESSO: 'Permesso',
  RELAZIONE: 'Relazione',
  ALTRO: 'Altro',
};

export function HomeDocumentiTecnici({
  site,
  onBack,
  onAddDocument,
  readOnly = false,
}: HomeDocumentiTecniciProps) {
  const [documenti, setDocumenti] = useState<any[]>([]);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  useEffect(() => {
    caricaDocumenti();
  }, [site.id]);

  const caricaDocumenti = async () => {
    try {
      const data = await getDocumentiTecnici(Number(site.id));
      if (Array.isArray(data)) setDocumenti(data);
    } catch (err) {
      console.error(err);
    }
  };

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '—';
    return new Date(dateStr + 'T00:00:00').toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  const handleDelete = async () => {
    if (deleteId) {
      try {
        await eliminaDocumentoTecnico(Number(site.id), deleteId);
        await caricaDocumenti();
        setDeleteId(null);
      } catch (err: any) {
        alert(err.message);
      }
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

        {documenti.length === 0 ? (
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
            {documenti.map(doc => (
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
                            <span>{documentTypeLabels[doc.tipologia] || doc.tipologia}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Calendar className="w-4 h-4" />
                            <span>{formatDate(doc.data)}</span>
                          </div>
                          {doc.fase && (
                            <div className="flex items-center gap-1">
                              <FolderOpen className="w-4 h-4" />
                              <span>{doc.fase.nome}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => window.open(doc.fileUrl, '_blank')}
                       icon={<ExternalLink className="w-4 h-4" />}
                      >
                        <span className="hidden sm:inline">Apri</span>
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
          isOpen={deleteId !== null}
          title="Elimina Documento"
          message="Sei sicuro di voler eliminare questo documento?"
          confirmLabel="Elimina"
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          variant="danger"
        />
      </main>
    </div>
  );
}