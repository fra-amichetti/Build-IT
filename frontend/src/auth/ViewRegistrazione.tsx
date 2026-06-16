import React, { useState } from 'react';
import { HardHat, ArrowLeft, CheckCircle } from 'lucide-react';
import { Input } from '../components/shared/Input';
import { Button } from '../components/shared/Button';
import { register } from '../services/api.ts';
interface ViewRegistrazioneProps {
  onBack: () => void;
}

export function ViewRegistrazione({ onBack }: ViewRegistrazioneProps) {

  const [nome, setNome] = useState('');
  const [cognome, setCognome] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!nome.trim()) {
      newErrors.nome = 'Il nome è obbligatorio';
    }

    if (!cognome.trim()) {
      newErrors.cognome = 'Il cognome è obbligatorio';
    }

    if (!email.trim()) {
      newErrors.email = "L'email è obbligatoria";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = 'Inserisci un indirizzo email valido';
    }

    if (!password) {
      newErrors.password = 'La password è obbligatoria';
    } else {
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!?.@/]).{8,}$/;
      if (!passwordRegex.test(password)) {
        newErrors.password = 'La password deve contenere almeno 8 caratteri, una maiuscola, una minuscola, un numero e un carattere speciale tra !?.@/';
      }
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = 'Conferma la password';
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Le password non coincidono';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  if (!validateForm()) return;
  setIsLoading(true);

  try {
    await register(nome, cognome, email, password);
    setSuccess(true);
    setTimeout(() => onBack(), 2000);
  } catch (err: any) {
    setErrors({ email: err.message || 'Errore durante la registrazione' });
  }

  setIsLoading(false);
};

  if (success) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-red-50 via-rose-50 to-red-100 flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8 text-center max-w-md w-full">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 mb-4">
            <CheckCircle className="w-8 h-8 text-green-600" />
          </div>
          <h2 className="text-2xl font-semibold text-gray-900 mb-2">Registrazione completata!</h2>
          <p className="text-gray-600">Verrai reindirizzato alla pagina di login...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-red-50 via-rose-50 to-red-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-6">
          <div className="flex items-center justify-center gap-3">
            <img
              src="/icon-house.png"
              alt="Build-IT House Icon"
              className="h-16 w-auto"
            />
            <img
              src="/text-buildit.png"
              alt="Build-IT Text"
              className="h-8 w-auto"
            />
          </div>
        </div>

        {/* Registration Form */}
        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
          <div className="flex items-center gap-3 mb-6">
            <button
              onClick={onBack}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-5 h-5 text-gray-600" />
            </button>
            <h2 className="text-xl font-semibold text-gray-900">Registrazione Cliente</h2>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Nome"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              error={errors.nome}
              required
            />

            <Input
              label="Cognome"
              value={cognome}
              onChange={(e) => setCognome(e.target.value)}
              error={errors.cognome}
              required
            />

            <Input
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={errors.email}
              required
            />

<Input
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              helperText="Min. 8 car., 1 Maiusc., 1 Minusc., 1 Num., 1 Spec. tra !?.@/"
              required
            />

            <Input
              label="Conferma Password"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              error={errors.confirmPassword}
              required
            />

            <Button
              type="submit"
              size="lg"
              isLoading={isLoading}
              className="w-full"
            >
              Registrati
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
}
