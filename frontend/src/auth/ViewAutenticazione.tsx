import React, { useState } from 'react';
import { HardHat, Mail, Lock, AlertCircle } from 'lucide-react';
import { Input } from '../components/shared/Input';
import { Button } from '../components/shared/Button';
import { useApp } from '../context/AppContext';

interface ViewAutenticazioneProps {
  onSuccess: (role: string) => void;
  onRegister: () => void;
}

export function ViewAutenticazione({ onSuccess, onRegister }: ViewAutenticazioneProps) {
  const { login } = useApp();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 500));

    const result = login(email, password);

    if (result.success && result.user) {
      onSuccess(result.user.role);
    } else {
      setError(result.error || 'Errore durante il login');
    }

    setIsLoading(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-rose-50 via-red-50 to-rose-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo and Title */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-4 mb-4">
            <img
              src="/icon-house.png"
              alt="Build-IT House Icon"
              className="h-24 w-auto"
            />
            <img
              src="/text-buildit.png"
              alt="Build-IT Text"
              className="h-12 w-auto"
            />
          </div>
          <p className="text-gray-600">Gestione Cantieri Edili</p>
        </div>

        {/* Login Form */}
        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
          <h2 className="text-2xl font-semibold text-gray-900 mb-6 text-center">Accedi</h2>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Email
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="w-5 h-5 text-gray-400" />
                </div>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-700 focus:border-red-700 transition-colors"
                  placeholder="inserisci la tua email"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="w-5 h-5 text-gray-400" />
                </div>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-700 focus:border-red-700 transition-colors"
                  placeholder="inserisci la password"
                  required
                />
              </div>
            </div>

            {error && (
              <div className="rounded-lg bg-red-50 border border-red-200 p-4">
                <div className="flex items-start gap-3">
                  <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                  <div className="flex-1">
                    <p className="text-sm text-red-800 font-medium">{error}</p>
                    {error === 'Utente non registrato' && (
                      <button
                        type="button"
                        onClick={onRegister}
                        className="mt-2 text-sm text-red-700 underline hover:text-red-800"
                      >
                        Registrati come nuovo cliente
                      </button>
                    )}
                  </div>
                </div>
              </div>
            )}

            <Button
              type="submit"
              size="lg"
              isLoading={isLoading}
              className="w-full bg-red-700 hover:bg-red-800 text-white"
            >
              Accedi
            </Button>
          </form>

          <div className="mt-6 pt-6 border-t border-gray-200">
            <p className="text-center text-sm text-gray-600">
              Nuovo cliente?{' '}
              <button
                onClick={onRegister}
                className="text-red-700 hover:text-red-800 font-medium"
              >
                Registrati qui
              </button>
            </p>
          </div>
        </div>

        {/* Demo credentials hint */}
        <div className="mt-6 bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-sm text-red-800 font-semibold mb-3">Credenziali Demo (clicca per compilare):</p>
          <div className="space-y-2">
            <button
              type="button"
              onClick={() => {
                setEmail('admin@buildit.it');
                setPassword('Admin123!');
              }}
              className="w-full text-left px-3 py-2 rounded-md bg-white hover:bg-red-100 border border-red-200 transition-colors text-sm"
            >
              <strong className="text-red-900">Admin:</strong>{' '}
              <span className="text-gray-600">admin@buildit.it / Admin123!</span>
            </button>
            <button
              type="button"
              onClick={() => {
                setEmail('luca.bianchi@buildit.it');
                setPassword('Dipendente1!');
              }}
              className="w-full text-left px-3 py-2 rounded-md bg-white hover:bg-red-100 border border-red-200 transition-colors text-sm"
            >
              <strong className="text-red-900">Dipendente:</strong>{' '}
              <span className="text-gray-600">luca.bianchi@buildit.it / Dipendente1!</span>
            </button>
            <button
              type="button"
              onClick={() => {
                setEmail('mario.conti@email.it');
                setPassword('Cliente1!');
              }}
              className="w-full text-left px-3 py-2 rounded-md bg-white hover:bg-red-100 border border-red-200 transition-colors text-sm"
            >
              <strong className="text-red-900">Cliente:</strong>{' '}
              <span className="text-gray-600">mario.conti@email.it / Cliente1!</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
