import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, ArrowLeft, CheckCircle, Loader2 } from 'lucide-react';
import api from '../services/api';
import type { OnboardingRequest, OnboardingResponse } from '../types/index';

const ESTADOS_BRASIL = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
  'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
  'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
];

export default function Onboarding() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Form data
  const [formData, setFormData] = useState<OnboardingRequest>({
    nome: '',
    email: '',
    telefone: '',
    senha: '',
    nomeEmpresa: '',
    tipoEmpresa: 'MATRIZ',
    cnpj: '',
    nomeFazenda: '',
    cidade: '',
    estado: '',
    tipoProdução: 'CORTE',
    tamanho: undefined,
  });

  const updateField = (field: keyof OnboardingRequest, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const nextStep = () => {
    if (step < 3) setStep(step + 1);
  };

  const prevStep = () => {
    if (step > 1) setStep(step - 1);
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await api.post<OnboardingResponse>('/onboarding', formData);

      localStorage.setItem('token', response.data.accessToken);
      localStorage.setItem('user', JSON.stringify(response.data.usuario));

      // Sucesso! Redirecionar para dashboard
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao criar conta. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 p-4">
      <div className="w-full max-w-2xl">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-600 rounded-2xl mb-4">
            <span className="text-3xl">🐂</span>
          </div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Bem-vindo ao iBoi!</h1>
          <p className="text-gray-600">Comece seu trial gratuito de 30 dias</p>
        </div>

        {/* Progress */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-2">
            {[1, 2, 3].map((s) => (
              <div key={s} className="flex items-center flex-1">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center font-bold ${
                    s < step
                      ? 'bg-primary-600 text-white'
                      : s === step
                      ? 'bg-primary-600 text-white ring-4 ring-primary-200'
                      : 'bg-gray-200 text-gray-500'
                  }`}
                >
                  {s < step ? <CheckCircle className="w-6 h-6" /> : s}
                </div>
                {s < 3 && (
                  <div className={`flex-1 h-1 mx-2 ${s < step ? 'bg-primary-600' : 'bg-gray-200'}`} />
                )}
              </div>
            ))}
          </div>
          <div className="flex justify-between text-sm">
            <span className={step === 1 ? 'text-primary-600 font-medium' : 'text-gray-500'}>
              Dados Pessoais
            </span>
            <span className={step === 2 ? 'text-primary-600 font-medium' : 'text-gray-500'}>
              Empresa
            </span>
            <span className={step === 3 ? 'text-primary-600 font-medium' : 'text-gray-500'}>
              Fazenda
            </span>
          </div>
        </div>

        {/* Card */}
        <div className="card">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* Step 1: Dados Pessoais */}
          {step === 1 && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Seus Dados</h2>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nome Completo *
                </label>
                <input
                  type="text"
                  value={formData.nome}
                  onChange={(e) => updateField('nome', e.target.value)}
                  className="input-field"
                  placeholder="João Silva"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  E-mail *
                </label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => updateField('email', e.target.value)}
                  className="input-field"
                  placeholder="joao@exemplo.com"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Telefone
                </label>
                <input
                  type="tel"
                  value={formData.telefone}
                  onChange={(e) => updateField('telefone', e.target.value)}
                  className="input-field"
                  placeholder="(44) 99999-8888"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Senha *
                </label>
                <input
                  type="password"
                  value={formData.senha}
                  onChange={(e) => updateField('senha', e.target.value)}
                  className="input-field"
                  placeholder="Mínimo 6 caracteres"
                  required
                  minLength={6}
                />
              </div>
            </div>
          )}

          {/* Step 2: Empresa */}
          {step === 2 && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Sua Empresa</h2>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nome da Empresa *
                </label>
                <input
                  type="text"
                  value={formData.nomeEmpresa}
                  onChange={(e) => updateField('nomeEmpresa', e.target.value)}
                  className="input-field"
                  placeholder="Agropecuária Silva Ltda"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  CNPJ
                </label>
                <input
                  type="text"
                  value={formData.cnpj}
                  onChange={(e) => updateField('cnpj', e.target.value)}
                  className="input-field"
                  placeholder="12.345.678/0001-90"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tipo de Empresa *
                </label>
                <div className="flex gap-4">
                  <label className="flex items-center">
                    <input
                      type="radio"
                      value="MATRIZ"
                      checked={formData.tipoEmpresa === 'MATRIZ'}
                      onChange={(e) => updateField('tipoEmpresa', e.target.value)}
                      className="mr-2"
                    />
                    Matriz
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      value="FILIAL"
                      checked={formData.tipoEmpresa === 'FILIAL'}
                      onChange={(e) => updateField('tipoEmpresa', e.target.value)}
                      className="mr-2"
                    />
                    Filial
                  </label>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Fazenda */}
          {step === 3 && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Sua Fazenda</h2>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nome da Fazenda *
                </label>
                <input
                  type="text"
                  value={formData.nomeFazenda}
                  onChange={(e) => updateField('nomeFazenda', e.target.value)}
                  className="input-field"
                  placeholder="Fazenda Boa Vista"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Cidade *
                  </label>
                  <input
                    type="text"
                    value={formData.cidade}
                    onChange={(e) => updateField('cidade', e.target.value)}
                    className="input-field"
                    placeholder="Maringá"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Estado *
                  </label>
                  <select
                    value={formData.estado}
                    onChange={(e) => updateField('estado', e.target.value)}
                    className="input-field"
                    required
                  >
                    <option value="">Selecione...</option>
                    {ESTADOS_BRASIL.map((uf) => (
                      <option key={uf} value={uf}>
                        {uf}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tipo de Produção *
                </label>
                <div className="flex gap-4">
                  <label className="flex items-center">
                    <input
                      type="radio"
                      value="CORTE"
                      checked={formData.tipoProdução === 'CORTE'}
                      onChange={(e) => updateField('tipoProdução', e.target.value)}
                      className="mr-2"
                    />
                    Corte
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      value="LEITE"
                      checked={formData.tipoProdução === 'LEITE'}
                      onChange={(e) => updateField('tipoProdução', e.target.value)}
                      className="mr-2"
                    />
                    Leite
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      value="MISTO"
                      checked={formData.tipoProdução === 'MISTO'}
                      onChange={(e) => updateField('tipoProdução', e.target.value)}
                      className="mr-2"
                    />
                    Misto
                  </label>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tamanho (hectares)
                </label>
                <input
                  type="number"
                  value={formData.tamanho || ''}
                  onChange={(e) => updateField('tamanho', e.target.value ? Number(e.target.value) : undefined)}
                  className="input-field"
                  placeholder="500"
                  min="0"
                  step="0.1"
                />
              </div>
            </div>
          )}

          {/* Navigation Buttons */}
          <div className="mt-6 flex gap-3">
            {step > 1 && (
              <button onClick={prevStep} className="btn-secondary flex items-center gap-2">
                <ArrowLeft className="w-5 h-5" />
                Voltar
              </button>
            )}

            {step < 3 ? (
              <button onClick={nextStep} className="btn-primary flex-1 flex items-center justify-center gap-2">
                Próximo
                <ArrowRight className="w-5 h-5" />
              </button>
            ) : (
              <button
                onClick={handleSubmit}
                disabled={loading}
                className="btn-primary flex-1 flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Criando conta...
                  </>
                ) : (
                  <>
                    <CheckCircle className="w-5 h-5" />
                    Finalizar Cadastro
                  </>
                )}
              </button>
            )}
          </div>

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Já tem uma conta?{' '}
              <button
                onClick={() => navigate('/login')}
                className="text-primary-600 hover:text-primary-700 font-medium"
              >
                Fazer login
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
