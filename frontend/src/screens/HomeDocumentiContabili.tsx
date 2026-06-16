import React, { useState, useEffect } from 'react';
import { ArrowLeft, Plus, Receipt, Download, Trash2, Calendar, DollarSign, FolderOpen, CheckCircle, AlertCircle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { StatusBadge, getPaymentStatusVariant } from '../components/shared/StatusBadge';
import { ConstructionSite } from '../types';
import { getDocumentiContabili, eliminaDocumentoContabile, saldaFattura } from '../services/api';
import { ExternalLink } from 'lucide-react';

interface HomeDocumentiContabiliProps {
  site: ConstructionSite;
  onBack: () => void;
  onAddDocument?: () => void;
  readOnly?: boolean;
}

export function HomeDocumentiContabili({
  site,
  onBack,
  onAddDocument,
  readOnly = false,
}: HomeDocumentiContabiliProps) {
  const [documenti, setDocumenti] = useState<any[]>([]);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  useEffect(() => {
    caricaDocumenti();
  }, [site.id]);

  const caricaDocumenti = async () => {
    try {
      const data = await getDocumentiContabili(Number(site.id));
      if (Array.isArray(data)) setDocumenti(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fatture = documenti.filter(d => d instanceof Object && 'statoPagamento' in d);
  const totalFatturato = fatture.reduce((sum: number, f: any) => sum + f.importo, 0);
  const incassato = fatture.filter((f: any) => f.statoPagamento === 'SALDATO').reduce((sum: number, f: any) => sum + f.importo, 0);
  const daIncassare = totalFatturato - incassato;

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return '—';
    return new Date(dateStr + 'T00:00:00').toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('it-IT', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  };

  const getTipo = (doc: any) => {
    return doc.statoPagamento !== undefined ? 'Fattura' : 'Preventivo';
  };

  const handleDelete = async () => {
    if (deleteId) {
      try {
        await eliminaDocumentoContabile(Number(site.id), deleteId);
        await caricaDocumenti();
        setDeleteId(null);
      } catch (err: any) {
        alert(err.message);
      }
    }
  };

  const handleSalda = async (docId: number) => {
    try {
      await saldaFattura(Number(site.id), docId);
      await caricaDocumenti();
    } catch (err: any) {
      alert(err.message);
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
            <h1 className="text-2xl font-bold text-gray-900">Documenti Contabili</h1>
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
              <Receipt className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nessun documento</h3>
            <p className="text-gray-500">
              {readOnly ? 'Non ci sono documenti contabili' : 'Aggiungi il primo documento contabile'}
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {documenti.map((doc: any) => {
              const tipo = getTipo(doc);
              return (
                <Card key={doc.id}>
                  <CardBody className="p-5">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-4 flex-1">
                        <div className={`w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0 ${tipo === 'Fattura' ? 'bg-green-50' : 'bg-blue-50'}`}>
                          <Receipt className={`w-6 h-6 ${tipo === 'Fattura' ? 'text-green-600' : 'text-blue-600'}`} />
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <h3 className="font-semibold text-gray-900">{doc.nome}</h3>
                            <span className={`px-2 py-0.5 text-xs font-medium rounded ${tipo === 'Fattura' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'}`}>
                              {tipo}
                            </span>
                          </div>
                          <div className="flex flex-wrap items-center gap-3 text-sm text-gray-500 mb-2">
                            <div className="flex items-center gap-1">
                              <DollarSign className="w-4 h-4" />
                              <span className="font-medium text-gray-900">{formatCurrency(doc.importo)}</span>
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
                          {tipo === 'Fattura' && doc.statoPagamento && (
                            <StatusBadge
                              status={doc.statoPagamento === 'SALDATO' ? 'Saldato' : 'Da Saldare'}
                              variant={getPaymentStatusVariant(doc.statoPagamento === 'SALDATO' ? 'Saldato' : 'Da Saldare')}
                              size="sm"
                            />
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        {tipo === 'Fattura' && doc.statoPagamento === 'DA_SALDARE' && !readOnly && (
                          <Button variant="primary" size="sm" onClick={() => handleSalda(doc.id)}>
                            Salda
                          </Button>
                        )}
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
              );
            })}
          </div>
        )}

        <ConfirmDialog
          isOpen={deleteId !== null}
          title="Elimina Documento"
          message="Sei sicuro di voler eliminare questo documento contabile?"
          confirmLabel="Elimina"
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          variant="danger"
        />
      </main>
    </div>
  );
}