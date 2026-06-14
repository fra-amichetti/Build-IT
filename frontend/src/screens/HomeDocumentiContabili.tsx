import React, { useState } from 'react';
import { ArrowLeft, Plus, Receipt, Download, Trash2, Calendar, DollarSign, FolderOpen, CheckCircle, AlertCircle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Card, CardBody } from '../components/shared/Card';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { StatusBadge, getPaymentStatusVariant } from '../components/shared/StatusBadge';
import { useApp } from '../context/AppContext';
import { ConstructionSite, AccountingDocument } from '../types';

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
  const { accountingDocuments, workPhases, deleteAccountingDocument, markInvoicePaid } = useApp();
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const siteDocs = accountingDocuments.filter(d => d.cantiereId === site.id);

  // Calculate totals
  const fatture = siteDocs.filter(d => d.tipo === 'Fattura');
  const totalFatturato = fatture.reduce((sum, f) => sum + f.importo, 0);
  const incassato = fatture.filter(f => f.statoPagamento === 'Saldato').reduce((sum, f) => sum + f.importo, 0);
  const daIncassare = totalFatturato - incassato;

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('it-IT', {
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

  const getPhaseName = (phaseId?: string) => {
    if (!phaseId) return 'Cantiere generale';
    const phase = workPhases.find(p => p.id === phaseId);
    return phase ? phase.nome : 'Fase sconosciuta';
  };

  const handleDelete = () => {
    if (deleteId) {
      deleteAccountingDocument(deleteId);
      setDeleteId(null);
    }
  };

  const handleMarkPaid = (docId: string) => {
    markInvoicePaid(docId);
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

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <Card className="bg-gradient-to-br from-green-50 to-green-100 border-green-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-green-700 mb-1">Totale Fatturato</p>
                  <p className="text-2xl font-bold text-green-900">{formatCurrency(totalFatturato)}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-green-200 flex items-center justify-center">
                  <DollarSign className="w-6 h-6 text-green-700" />
                </div>
              </div>
            </CardBody>
          </Card>

          <Card className="bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-blue-700 mb-1">Incassato</p>
                  <p className="text-2xl font-bold text-blue-900">{formatCurrency(incassato)}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-blue-200 flex items-center justify-center">
                  <CheckCircle className="w-6 h-6 text-blue-700" />
                </div>
              </div>
            </CardBody>
          </Card>

          <Card className="bg-gradient-to-br from-red-50 to-red-100 border-red-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-red-800 mb-1">Da Incassare</p>
                  <p className="text-2xl font-bold text-red-950">{formatCurrency(daIncassare)}</p>
                </div>
                <div className="w-12 h-12 rounded-lg bg-red-200 flex items-center justify-center">
                  <AlertCircle className="w-6 h-6 text-red-800" />
                </div>
              </div>
            </CardBody>
          </Card>
        </div>

        {/* Unpaid Invoices Alert */}
        {daIncassare > 0 && (
          <Card className="mb-6 border-red-200 bg-red-50">
            <CardBody className="p-4">
              <div className="flex items-center gap-3">
                <AlertCircle className="w-5 h-5 text-red-700 flex-shrink-0" />
                <p className="text-sm text-red-900">
                  <strong>Attenzione:</strong> Ci sono {fatture.filter(f => f.statoPagamento === 'Da Saldare').length} fatture da saldare per un totale di {formatCurrency(daIncassare)}
                </p>
              </div>
            </CardBody>
          </Card>
        )}

        {siteDocs.length === 0 ? (
          <div className="text-center py-12">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
              <Receipt className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nessun documento</h3>
            <p className="text-gray-500">
              {readOnly ? 'Non ci sono documenti contabili per questo cantiere' : 'Aggiungi il primo documento contabile'}
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {siteDocs.map(doc => (
              <Card key={doc.id}>
                <CardBody className="p-5">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-4 flex-1">
                      <div className={`w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0 ${
                        doc.tipo === 'Fattura' ? 'bg-green-50' : 'bg-blue-50'
                      }`}>
                        <Receipt className={`w-6 h-6 ${
                          doc.tipo === 'Fattura' ? 'text-green-600' : 'text-blue-600'
                        }`} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="font-semibold text-gray-900">{doc.nome}</h3>
                          <span className={`px-2 py-0.5 text-xs font-medium rounded ${
                            doc.tipo === 'Fattura'
                              ? 'bg-green-100 text-green-700'
                              : 'bg-blue-100 text-blue-700'
                          }`}>
                            {doc.tipo}
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
                          <div className="flex items-center gap-1">
                            <FolderOpen className="w-4 h-4" />
                            <span>{getPhaseName(doc.faseId)}</span>
                          </div>
                        </div>
                        {doc.tipo === 'Fattura' && doc.statoPagamento && (
                          <StatusBadge
                            status={doc.statoPagamento}
                            variant={getPaymentStatusVariant(doc.statoPagamento)}
                            size="sm"
                          />
                        )}
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      {doc.tipo === 'Fattura' && doc.statoPagamento === 'Da Saldare' && !readOnly && (
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => handleMarkPaid(doc.id)}
                        >
                          Salda
                        </Button>
                      )}
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
          message="Sei sicuro di voler eliminare questo documento contabile? Questa operazione non può essere annullata."
          confirmLabel="Elimina"
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          variant="danger"
        />
      </main>
    </div>
  );
}
