import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight, CheckCircle, Loader2 } from 'lucide-react';
import api from '../services/api';
import { storeAuthSession } from '../services/session';
import type { OnboardingRequest, OnboardingResponse } from '../types';
import logo from '../assets/logo_transparente.png';

const ESTADOS_BRASIL = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
  'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
  'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
];

const TIPO_PRODUCAO_KEY = 'tipoProdu\u00E7\u00E3o' as const;

export default function Onboarding() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
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
    [TIPO_PRODUCAO_KEY]: 'CORTE',
    tamanho: undefined,
  });

  const updateField = (field: keyof OnboardingRequest, value: string | number | undefined) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await api.post<OnboardingResponse>('/onboarding', formData);
      storeAuthSession(response.data);
      navigate('/app/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao criar conta. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 p-4">
      <div className="w-full max-w-2xl">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center mb-6">
            <img src={logo} alt="Logo" className="w-48 h-48 object-contain" />
          </div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Comece seu trial</h1>
          <p className="text-lg text-gray-600">Empresa, fazenda e usuario admin criados em um fluxo so.</p>
        </div>

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
                {s < 3 && <div className={`flex-1 h-1 mx-2 ${s < step ? 'bg-primary-600' : 'bg-gray-200'}`} />}
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {error}
            </div>
          )}

          {step === 1 && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold text-gray-900">Seus dados</h2>
              <input className="input-field" placeholder="Nome completo" value={formData.nome} onChange={(e) => updateField('nome', e.target.value)} />
              <input className="input-field" placeholder="E-mail" value={formData.email} onChange={(e) => updateField('email', e.target.value)} />
              <input className="input-field" placeholder="Telefone" value={formData.telefone} onChange={(e) => updateField('telefone', e.target.value)} />
              <input className="input-field" type="password" placeholder="Senha" value={formData.senha} onChange={(e) => updateField('senha', e.target.value)} />
            </div>
          )}

          {step === 2 && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold text-gray-900">Sua empresa</h2>
              <input className="input-field" placeholder="Nome da empresa" value={formData.nomeEmpresa} onChange={(e) => updateField('nomeEmpresa', e.target.value)} />
              <input className="input-field" placeholder="CNPJ" value={formData.cnpj} onChange={(e) => updateField('cnpj', e.target.value)} />
              <div className="flex gap-4">
                {(['MATRIZ', 'FILIAL'] as const).map((tipo) => (
                  <button key={tipo} onClick={() => updateField('tipoEmpresa', tipo)} className={formData.tipoEmpresa === tipo ? 'btn-primary' : 'btn-secondary'}>
                    {tipo}
                  </button>
                ))}
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold text-gray-900">Sua fazenda</h2>
              <input className="input-field" placeholder="Nome da fazenda" value={formData.nomeFazenda} onChange={(e) => updateField('nomeFazenda', e.target.value)} />
              <div className="grid grid-cols-2 gap-4">
                <input className="input-field" placeholder="Cidade" value={formData.cidade} onChange={(e) => updateField('cidade', e.target.value)} />
                <select className="input-field" value={formData.estado} onChange={(e) => updateField('estado', e.target.value)}>
                  <option value="">Estado</option>
                  {ESTADOS_BRASIL.map((uf) => (
                    <option key={uf} value={uf}>
                      {uf}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex gap-4">
                {(['CORTE', 'LEITE', 'MISTO'] as const).map((tipo) => (
                  <button
                    key={tipo}
                    onClick={() => updateField(TIPO_PRODUCAO_KEY, tipo)}
                    className={formData[TIPO_PRODUCAO_KEY] === tipo ? 'btn-primary' : 'btn-secondary'}
                  >
                    {tipo}
                  </button>
                ))}
              </div>
              <input
                className="input-field"
                type="number"
                placeholder="Tamanho em hectares"
                value={formData.tamanho ?? ''}
                onChange={(e) => updateField('tamanho', e.target.value ? Number(e.target.value) : undefined)}
              />
            </div>
          )}

          <div className="mt-6 flex gap-3">
            {step > 1 && (
              <button onClick={() => setStep((prev) => prev - 1)} className="btn-secondary flex items-center gap-2">
                <ArrowLeft className="w-5 h-5" />
                Voltar
              </button>
            )}

            {step < 3 ? (
              <button onClick={() => setStep((prev) => prev + 1)} className="btn-primary flex-1 flex items-center justify-center gap-2">
                Proximo
                <ArrowRight className="w-5 h-5" />
              </button>
            ) : (
              <button onClick={handleSubmit} disabled={loading} className="btn-primary flex-1 flex items-center justify-center gap-2">
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Criando conta...
                  </>
                ) : (
                  <>
                    <CheckCircle className="w-5 h-5" />
                    Finalizar cadastro
                  </>
                )}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
