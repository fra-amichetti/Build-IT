import React, { useState } from 'react';
import { ArrowLeft, Plus, User, Mail, Trash2, Users, UserPlus } from 'lucide-react';
import { Header } from '../components/shared/Header';
import { Button } from '../components/shared/Button';
import { Input } from '../components/shared/Input';
import { Card, CardBody, CardHeader } from '../components/shared/Card';
import { ConfirmDialog } from '../components/shared/ConfirmDialog';
import { useApp } from '../context/AppContext';
import { User as UserType } from '../types';

interface HomeGestioneDipendentiProps {
  onBack: () => void;
}

export function HomeGestioneDipendenti({ onBack }: HomeGestioneDipendentiProps) {
  const { users, addEmployee, deleteEmployee } = useApp();
  const [showAddForm, setShowAddForm] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    nome: '',
    cognome: '',
    email: '',
    password: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  // Filter only employees
  const employees = users.filter(u => u.role === 'Dipendente');

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.nome.trim()) {
      newErrors.nome = 'Il nome è obbligatorio';
    }

    if (!formData.cognome.trim()) {
      newErrors.cognome = 'Il cognome è obbligatorio';
    }

    if (!formData.email.trim()) {
      newErrors.email = "L'email è obbligatoria";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Inserisci un indirizzo email valido';
    }

    if (!formData.password) {
      newErrors.password = 'La password è obbligatoria';
    } else if (formData.password.length < 8) {
      newErrors.password = 'La password deve contenere almeno 8 caratteri';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsLoading(true);
    await new Promise((resolve) => setTimeout(resolve, 500));

    const result = addEmployee(formData.nome, formData.cognome, formData.email, formData.password);

    if (result.success) {
      setFormData({ nome: '', cognome: '', email: '', password: '' });
      setShowAddForm(false);
      setErrors({});
    } else {
      setErrors({ email: result.error || 'Errore durante il salvataggio' });
    }

    setIsLoading(false);
  };

  const handleDelete = () => {
    if (deleteId) {
      deleteEmployee(deleteId);
      setDeleteId(null);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header showMenuButton onMenuClick={onBack} />

      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
          <span>Torna alla home</span>
        </button>

        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-emerald-100 flex items-center justify-center">
              <Users className="w-5 h-5 text-emerald-600" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Gestione Dipendenti</h1>
              <p className="text-gray-500 text-sm">{employees.length} dipendenti registrati</p>
            </div>
          </div>

          <Button onClick={() => setShowAddForm(true)} icon={<Plus className="w-4 h-4" />}>
            Aggiungi Dipendente
          </Button>
        </div>

        {/* Add Employee Form */}
        {showAddForm && (
          <Card className="mb-6">
            <CardHeader className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <UserPlus className="w-5 h-5 text-emerald-600" />
                <h3 className="font-medium text-gray-900">Nuovo Dipendente</h3>
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
                    label="Nome"
                    value={formData.nome}
                    onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                    error={errors.nome}
                    required
                  />
                  <Input
                    label="Cognome"
                    value={formData.cognome}
                    onChange={(e) => setFormData({ ...formData, cognome: e.target.value })}
                    error={errors.cognome}
                    required
                  />
                </div>

                <Input
                  label="Email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  error={errors.email}
                  required
                />

                <Input
                  label="Password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  error={errors.password}
                  helperText="Minimo 8 caratteri"
                  required
                />

                <div className="flex gap-3 pt-2">
                  <Button type="button" variant="secondary" onClick={() => setShowAddForm(false)}>
                    Annulla
                  </Button>
                  <Button type="submit" isLoading={isLoading}>
                    Salva Dipendente
                  </Button>
                </div>
              </form>
            </CardBody>
          </Card>
        )}

        {/* Employees List */}
        {employees.length === 0 ? (
          <div className="text-center py-12">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
              <Users className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Nessun dipendente</h3>
            <p className="text-gray-500">Aggiungi il primo dipendente</p>
          </div>
        ) : (
          <div className="space-y-3">
            {employees.map(emp => (
              <Card key={emp.id}>
                <CardBody className="p-5">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-full bg-emerald-100 flex items-center justify-center">
                        <User className="w-6 h-6 text-emerald-600" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-gray-900">
                          {emp.nome} {emp.cognome}
                        </h3>
                        <div className="flex items-center gap-2 text-sm text-gray-500 mt-1">
                          <Mail className="w-4 h-4" />
                          <span>{emp.email}</span>
                        </div>
                      </div>
                    </div>

                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setDeleteId(emp.id)}
                      className="text-red-600 hover:text-red-700 hover:bg-red-50"
                      icon={<Trash2 className="w-4 h-4" />}
                    >
                      Elimina
                    </Button>
                  </div>
                </CardBody>
              </Card>
            ))}
          </div>
        )}

        <ConfirmDialog
          isOpen={!!deleteId}
          title="Elimina Dipendente"
          message="Sei sicuro di voler eliminare questo dipendente? Questa operazione non può essere annullata."
          confirmLabel="Elimina"
          onConfirm={handleDelete}
          onCancel={() => setDeleteId(null)}
          variant="danger"
        />
      </main>
    </div>
  );
}
