import {
  ArrowLeft, DollarSign, TrendingUp, Building2, Users, AlertTriangle,
  CheckCircle, Receipt
} from 'lucide-react';
import {
  BarChart, PieChart, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Bar, Pie, Cell
} from 'recharts';
import { Header } from '../components/shared/Header';
import { Card, CardBody, CardHeader } from '../components/shared/Card';
import { StatusBadge } from '../components/shared/StatusBadge';
import { useApp } from '../context/AppContext';

interface ViewMostraStatisticheProps {
  onBack: () => void;
}

export function ViewMostraStatistiche({ onBack }: ViewMostraStatisticheProps) {
  const { constructionSites, accountingDocuments, teams, workPhases } = useApp();

  // Economic calculations
  const fatture = accountingDocuments.filter(d => d.tipo === 'Fattura');
  const totaleFatturato = fatture.reduce((sum, f) => sum + f.importo, 0);
  const fattureSaldate = fatture.filter(f => f.statoPagamento === 'Saldato');
  const incassato = fattureSaldate.reduce((sum, f) => sum + f.importo, 0);
  const daIncassare = totaleFatturato - incassato;

  // Operational calculations
  const cantieriAttivi = constructionSites.filter(s => s.stato === 'In Corso').length;
  const cantieriRitardo = constructionSites.filter(s => s.stato === 'In Ritardo').length;
  const cantieriTerminati = constructionSites.filter(s => s.stato === 'Terminato').length;
  const cantieriPianificati = constructionSites.filter(s => s.stato === 'Pianificato').length;

  // Revenue per site
  const fatturatoPerCantiere = constructionSites.map(site => {
    const siteFatture = fatture.filter(f => f.cantiereId === site.id);
    const totale = siteFatture.reduce((sum, f) => sum + f.importo, 0);
    return {
      nome: site.nome.length > 20 ? site.nome.substring(0, 20) + '...' : site.nome,
      totale,
    };
  }).sort((a, b) => b.totale - a.totale).slice(0, 5);

  // Unpaid invoices
  const fattureDaIncassare = fatture.filter(f => f.statoPagamento === 'Da Saldare');

  // Active teams
  const squadreAttive = teams.filter(team => {
    return workPhases.some(
      phase => phase.squadraId === team.id && (phase.stato === 'In Corso' || phase.stato === 'Pianificata')
    );
  });

  // Site status distribution for pie chart
  const statusData = [
    { name: 'Pianificato', value: cantieriPianificati, color: '#9ca3af' },
    { name: 'In Corso', value: cantieriAttivi, color: '#3b82f6' },
    { name: 'In Ritardo', value: cantieriRitardo, color: '#ef4444' },
    { name: 'Terminato', value: cantieriTerminati, color: '#10b981' },
  ].filter(d => d.value > 0);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('it-IT', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
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
          <span>Torna alla home</span>
        </button>

        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-red-100 flex items-center justify-center">
              <TrendingUp className="w-5 h-5 text-red-700" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Statistiche Aziendali</h1>
              <p className="text-gray-500 text-sm">Panoramica economica e operativa</p>
            </div>
          </div>
        </div>

        {/* Economic Indicators */}
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Indicatori Economici</h2>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <Card className="bg-gradient-to-br from-green-50 to-green-100 border-green-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-green-700 font-medium">Fatturato Totale</p>
                <DollarSign className="w-5 h-5 text-green-600" />
              </div>
              <p className="text-3xl font-bold text-green-900">{formatCurrency(totaleFatturato)}</p>
            </CardBody>
          </Card>

          <Card className="bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-blue-700 font-medium">Fatturato Incassato</p>
                <CheckCircle className="w-5 h-5 text-blue-600" />
              </div>
              <p className="text-3xl font-bold text-blue-900">{formatCurrency(incassato)}</p>
            </CardBody>
          </Card>

          <Card className="bg-gradient-to-br from-red-50 to-red-100 border-red-200">
            <CardBody className="p-5">
              <div className="flex items-center justify-between mb-2">
                <p className="text-sm text-red-800 font-medium">Saldo da Incassare</p>
                <AlertTriangle className="w-5 h-5 text-red-700" />
              </div>
              <p className="text-3xl font-bold text-red-950">{formatCurrency(daIncassare)}</p>
            </CardBody>
          </Card>
        </div>

        {/* Revenue per Site Chart */}
        <Card className="mb-8">
          <CardHeader>
            <h3 className="font-medium text-gray-900">Fatturato per Cantiere (Top 5)</h3>
          </CardHeader>
          <CardBody className="p-6">
            {fatturatoPerCantiere.length > 0 ? (
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={fatturatoPerCantiere} layout="vertical">
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" tickFormatter={(v) => `${(v/1000).toFixed(0)}k`} />
                    <YAxis type="category" dataKey="nome" width={120} tick={{ fontSize: 11 }} />
                    <Tooltip
                      formatter={(value) => formatCurrency(Number(value))}
                      labelFormatter={(label) => `Cantiere: ${label}`}
                    />
                    <Bar dataKey="totale" fill="#b91c1c" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <div className="h-64 flex items-center justify-center text-gray-500">
                Nessun dato disponibile
              </div>
            )}
          </CardBody>
        </Card>

        {/* Unpaid Invoices List */}
        {fattureDaIncassare.length > 0 && (
          <Card className="mb-8 border-red-200">
            <CardHeader className="bg-red-50">
              <div className="flex items-center gap-2">
                <Receipt className="w-5 h-5 text-red-700" />
                <h3 className="font-medium text-gray-900">Fatture da Incassare</h3>
              </div>
            </CardHeader>
            <CardBody className="p-0">
              <div className="divide-y divide-gray-100">
                {fattureDaIncassare.map(fattura => {
                  const site = constructionSites.find(s => s.id === fattura.cantiereId);
                  return (
                    <div key={fattura.id} className="px-6 py-4 hover:bg-gray-50">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-medium text-gray-900">{fattura.nome}</p>
                          <p className="text-sm text-gray-500">{site?.nome || 'Cantiere non trovato'}</p>
                        </div>
                        <p className="font-semibold text-red-700">{formatCurrency(fattura.importo)}</p>
                      </div>
                    </div>
                  );
                })}
              </div>
              <div className="px-6 py-4 bg-red-50 border-t border-red-200">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-red-900">Totale da incassare:</span>
                  <span className="text-lg font-bold text-red-950">{formatCurrency(daIncassare)}</span>
                </div>
              </div>
            </CardBody>
          </Card>
        )}

        {/* Operational Indicators */}
        <h2 className="text-lg font-semibold text-gray-900 mb-4 mt-8">Indicatori Operativi</h2>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <Card>
            <CardBody className="p-5 text-center">
              <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center mx-auto mb-3">
                <Building2 className="w-6 h-6 text-blue-600" />
              </div>
              <p className="text-2xl font-bold text-gray-900">{cantieriAttivi}</p>
              <p className="text-sm text-gray-500">Cantieri Attivi</p>
            </CardBody>
          </Card>

          <Card>
            <CardBody className="p-5 text-center">
              <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-3">
                <AlertTriangle className="w-6 h-6 text-red-600" />
              </div>
              <p className="text-2xl font-bold text-red-600">{cantieriRitardo}</p>
              <p className="text-sm text-gray-500">In Ritardo</p>
            </CardBody>
          </Card>

          <Card>
            <CardBody className="p-5 text-center">
              <div className="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-3">
                <CheckCircle className="w-6 h-6 text-green-600" />
              </div>
              <p className="text-2xl font-bold text-gray-900">{cantieriTerminati}</p>
              <p className="text-sm text-gray-500">Terminati</p>
            </CardBody>
          </Card>

          <Card>
            <CardBody className="p-5 text-center">
              <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-3">
                <Users className="w-6 h-6 text-red-700" />
              </div>
              <p className="text-2xl font-bold text-gray-900">{squadreAttive.length}</p>
              <p className="text-sm text-gray-500">Squadre Impiegate</p>
            </CardBody>
          </Card>
        </div>

        {/* Site Status Distribution */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <Card>
            <CardHeader>
              <h3 className="font-medium text-gray-900">Distribuzione Stato Cantieri</h3>
            </CardHeader>
            <CardBody className="p-6">
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={statusData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={100}
                      paddingAngle={5}
                      dataKey="value"
                      label={({ name, value }) => `${name}: ${value}`}
                    >
                      {statusData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </CardBody>
          </Card>

          {/* Active Teams */}
          <Card>
            <CardHeader>
              <h3 className="font-medium text-gray-900">Squadre Attualmente Impiegate</h3>
            </CardHeader>
            <CardBody className="p-0">
              {squadreAttive.length === 0 ? (
                <div className="p-8 text-center text-gray-500">
                  Nessuna squadra attualmente impiegata
                </div>
              ) : (
                <div className="divide-y divide-gray-100">
                  {squadreAttive.map(team => {
                    const activePhase = workPhases.find(
                      p => p.squadraId === team.id && p.stato === 'In Corso'
                    );
                    return (
                      <div key={team.id} className="px-6 py-4">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="font-medium text-gray-900">{team.nome}</p>
                            <p className="text-sm text-gray-500">{team.specializzazione}</p>
                          </div>
                          <div className="text-right">
                            <StatusBadge status="Occupata" variant="amber" size="sm" />
                            {activePhase && (
                              <p className="text-xs text-gray-400 mt-1">{activePhase.nome}</p>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </CardBody>
          </Card>
        </div>
      </main>
    </div>
  );
}
