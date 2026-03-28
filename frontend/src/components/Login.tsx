import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Loader2, LogIn } from 'lucide-react';
import api from '../services/api';
import { storeAuthSession } from '../services/session';
import type { FarmSummary, LoginRequest, LoginResponse } from '../types';
import logo from '../assets/logo_transparente.png';

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [pendingResponse, setPendingResponse] = useState<LoginResponse | null>(null);
  const [selectedFarmId, setSelectedFarmId] = useState('');

  async function finalizarLoginComFazenda(baseResponse: LoginResponse, farmId: string) {
    if (farmId === baseResponse.defaultFarmId) {
      storeAuthSession(baseResponse);
      navigate('/app/dashboard');
      return;
    }

    const switched = await api.post<LoginResponse>(
      '/api/farms/select',
      { farmId },
      {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${baseResponse.accessToken}`,
        },
      }
    );

    storeAuthSession(switched.data);
    navigate('/app/dashboard');
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const request: LoginRequest = { email, senha };
      const response = await api.post<LoginResponse>('/auth/login', request);

      if (response.data.farms.length > 1) {
        setPendingResponse(response.data);
        setSelectedFarmId(response.data.defaultFarmId);
      } else {
        await finalizarLoginComFazenda(response.data, response.data.defaultFarmId);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao fazer login. Verifique suas credenciais.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center mb-4">
            <img src={logo} alt="Logo" className="w-48 h-48 object-contain" />
          </div>
          <p className="text-lg text-gray-600 font-medium">Gestao inteligente de gado</p>
        </div>

        <div className="card">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Entrar</h2>

          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {error}
            </div>
          )}

          {pendingResponse ? (
            <div className="space-y-4">
              <div>
                <p className="text-sm font-medium text-gray-700">Escolha a fazenda para entrar</p>
                <p className="mt-1 text-sm text-gray-500">Cada fazenda mantém os dados operacionais separados.</p>
              </div>

              <div className="space-y-2">
                {pendingResponse.farms.map((farm: FarmSummary) => (
                  <button
                    key={farm.id}
                    type="button"
                    onClick={() => setSelectedFarmId(farm.id)}
                    className={`w-full rounded-xl border px-4 py-3 text-left transition ${
                      selectedFarmId === farm.id
                        ? 'border-primary-500 bg-primary-50 text-primary-800'
                        : 'border-gray-200 bg-white text-gray-700 hover:border-primary-300'
                    }`}
                  >
                    <p className="font-semibold">{farm.name}</p>
                  </button>
                ))}
              </div>

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={() => {
                    setPendingResponse(null);
                    setSelectedFarmId('');
                  }}
                  className="btn-secondary flex-1"
                >
                  Voltar
                </button>
                <button
                  type="button"
                  onClick={() => void finalizarLoginComFazenda(pendingResponse, selectedFarmId)}
                  disabled={!selectedFarmId || loading}
                  className="btn-primary flex flex-1 items-center justify-center gap-2"
                >
                  {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <LogIn className="w-5 h-5" />}
                  Entrar na fazenda
                </button>
              </div>
            </div>
          ) : (
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">E-mail</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="input-field"
                  placeholder="seu@email.com"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Senha</label>
                <input
                  type="password"
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  className="input-field"
                  placeholder="********"
                  required
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full btn-primary flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Entrando...
                  </>
                ) : (
                  <>
                    <LogIn className="w-5 h-5" />
                    Entrar
                  </>
                )}
              </button>
            </form>
          )}

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Nao tem uma conta?{' '}
              <button
                onClick={() => navigate('/onboarding')}
                className="text-primary-600 hover:text-primary-700 font-medium"
              >
                Cadastre-se gratis
              </button>
            </p>
            <p className="text-xs text-gray-500 mt-2">30 dias de trial gratuito.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
