import React, { useState } from 'react';
import { ArrowLeft, Plus, Users, Trash2, UserCircle2, Wrench, AlertCircle } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input, Select } from '../components/shared/Input';
import { Card, CardBody, CardHeader } from '../components/shared/Card';
import { StatusBadge } from '../components/shared/StatusBadge';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { useApp } from '../context/AppContext';
import { Team, TeamSpecialization } from '../types';

interface HomeGestioneSquadreProps {
  onBack: () => void;
  embedded?: boolean;
}

const specializations: { value: TeamSpecialization; label: string }[] = [
  { value: 'Muratori', label: 'Muratori' },
  { value: 'Elettricisti', label: 'Elettricisti' },
  { value: 'Idraulici', label: 'Idraulici' },
  { value: 'Carpentieri', label: 'Carpentieri' },
];

export function HomeGestioneSquadre({ onBack, embedded }: HomeGestioneSquadreProps) {
  const { teams, addTeam, deleteTeam, workPhases } = useApp();
  const [showAddForm, setShowAddForm] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState('');
  const [formData, setFormData] = useState({
    nome: '',
    specializzazione: 'Muratori' as TeamSpecialization,
    numeroComponenti: '',
    nomeReferente: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const isTeamOccupied = (teamId: string) => {
    return workPhases.some(
      (phase) => phase.squadraId === teamId && (phase.stato === 'In Corso' || phase.stato === 'Pianificata')
    );
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.nome.trim()) {
      newErrors.nome = 'Il nome della squadra è obbligatorio';
    }

    const numComponents = parseInt(formData.numeroComponenti);
    if (!formData.numeroComponenti || isNaN(numComponents) || numComponents <= 0) {
      newErrors.numeroComponenti = 'Il numero di componenti deve essere maggiore di 0';
    }

    if (!formData.nomeReferente.trim()) {
      newErrors.nomeReferente = 'Il nome del referente è obbligatorio';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 500));

    const result = addTeam({
      nome: formData.nome,
      specializzazione: formData.specializzazione,
      numeroComponenti: parseInt(formData.numeroComponenti),
      nomeReferente: formData.nomeReferente,
    });

    if (result.success) {
      setFormData({ nome: '', specializzazione: 'Muratori', numeroComponenti: '', nomeReferente: '' });
      setShowAddForm(false);
      setErrors({});
    } else {
      setErrors({ nome: result.error || 'Errore durante il salvataggio' });
    }

    setIsLoading(false);
  };

  const handleDelete = () => {
    if (deleteId) {
      const result = deleteTeam(deleteId);
      if (result.success) {
        setDeleteId(null);
      } else {
        setDeleteError(result.error || 'Impossibile eliminare la squadra');
      }
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {!embedded && <Header showMenuButton onMenuClick={onBack} />}

      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
       {!embedded && ( <button
          onClick={onBack}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Torna alla home</span>
        </button>)}

        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center">
              <Users className="w-5 h-5 text-purple-600" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Gestione Squadre</h1>
              <p className="text-gray-500 text-sm">{teams.length} squadre attive</p>
            </div>
          </div>

          <Button onClick={() => setShowAddForm(true)} icon={<Plus className="w-4 h-4" />}>
            Aggiungi Squadra
          </Button>
        </div>

        {/* Add Team Form */}
        {showAddForm && (
          <Card className="mb-6">
            <CardHeader className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <UserCircle2 className="w-5 h-5 text-purple-600" />
                <h3 className="font-medium text-gray-900">Nuova Squadra</h3>
              </div>
              <button
                onClick={() => setShowAddForm(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                &times;
              </button>
            </CardHeader>
            <CardBody className="p-6">
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Input
                    label="Nome Squadra"
                    value={formData.nome}
                    onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                    error={errors.nome}
                    placeholder="es. Squadra Alpha"
                    required
                  />
                  <Select
                    label="Specializzazione"
                    value={formData.specializzazione}
                    onChange={(e) => setFormData({ ...formData, specializzazione: e.target.value as TeamSpecialization })}
                    options={specializations}
                    required
                  />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Input
                    label="Numero Componenti"
                    type="number"
                    min="1"
                    value={formData.numeroComponenti}
                    onChange={(e) => setFormData({ ...formData, numeroComponenti: e.target.value })}
                    error={errors.numeroComponenti}
                    required
                  />
                  <Input
                    label="Nome Referente"
                    value={formData.nomeReferente}
                    onChange={(e) => setFormData({ ...formData, nomeReferente: e.target.value })}
                    error={errors.nomeReferente}
                    required
                  />
                </div>

                <div className="flex gap-3 pt-2">
                  <Button type="button" variant="secondary" onClick={() => setShowAddForm(false)}>
                    Annulla
                  </Button>
                  <Button type="submit" isLoading={isLoading}>
                    Salva Squadra
                  </Button>
                </div>
              </form>
            </CardBody>
          </Card>
        )}

        {/* Delete Error Alert */}
        {deleteError && (
          <Card className="mb-6 border-red-200 bg-red-50">
            <CardBody className="p-4">
              <div className="flex items-center gap-3">
                <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                <p className="text-sm text-red-800">{deleteError}</p>
                <button
                  onClick={() => setDeleteError('')}
                  className="ml-auto text-red-600 hover:text-red-700"
                >
                  &times;
                </button>
              </div>
            </CardBody>
          </Card>
        )}

        {/* Teams List */}
        {teams.length === 0 ? (
          <div className="text-center py-12">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
              <Users className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nessuna squadra</h3>
            <p className="text-gray-500">Aggiungi la prima squadra</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {teams.map(team => {
              const isOccupied = isTeamOccupied(team.id);
              return (
                <Card key={team.id}>
                  <CardBody className="p-5">
                    <div className="flex items-start justify-between">
                      <div className="flex items-start gap-4">
                        <div className="w-12 h-12 rounded-lg bg-purple-100 flex items-center justify-center flex-shrink-0">
                          <Wrench className="w-6 h-6 text-purple-600" />
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-900 mb-1">{team.nome}</h3>
                          <div className="space-y-1 text-sm text-gray-500">
                            <p><strong>Specializzazione:</strong> {team.specializzazione}</p>
                            <p><strong>Componenti:</strong> {team.numeroComponenti}</p>
                            <p><strong>Referente:</strong> {team.nomeReferente}</p>
                          </div>
                          <div className="mt-2">
                            <StatusBadge
                              status={isOccupied ? 'Occupata' : 'Disponibile'}
                              variant={isOccupied ? 'amber' : 'green'}
                              size="sm"
                            />
                          </div>
                        </div>
                      </div>

                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setDeleteId(team.id)}
                        disabled={isOccupied}
                        className={`text-red-600 hover:text-red-700 hover:bg-red-50 ${isOccupied ? 'opacity-50 cursor-not-allowed' : ''}`}
                        icon={<Trash2 className="w-4 h-4" />}
                      />
                    </div>
                  </CardBody>
                </Card>
              );
            })}
          </div>
        )}

        <ConfirmDialog
          isOpen={!!deleteId}
          title="Elimina Squadra"
          message="Sei sicuro di voler eliminare questa squadra? Questa operazione non può essere annullata."
          confirmLabel="Elimina"
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          variant="danger"
        />
      </main>
    </div>
  );
}
