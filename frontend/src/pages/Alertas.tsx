import { useEffect, useMemo, useState } from 'react';
import { AlertCircle, RefreshCcw, ShieldCheck, Sparkles } from 'lucide-react';
import api from '../services/api';
import type { AlertaInteligenteDto } from '../types';

const priorityStyles: Record<string, string> = {
  CRITICA: 'bg-red-100 text-red-800 border-red-200',
  ALTA: 'bg-orange-100 text-orange-800 border-orange-200',
  MEDIA: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  BAIXA: 'bg-blue-100 text-blue-800 border-blue-200',
};

export default function Alertas() {
  const [alertas, setAlertas] = useState<AlertaInteligenteDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);

  useEffect(() => {
    void loadAlertas();
  }, []);

  async function loadAlertas() {
    try {
      setLoading(true);
      const response = await api.get<AlertaInteligenteDto[]>('/api/alertas/ativos');
      setAlertas(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Erro ao carregar alertas', error);
      setAlertas([]);
    } finally {
      setLoading(false);
    }
  }

  async function gerarAlertas() {
    try {
      setRunning(true);
      await api.post('/api/alertas/gerar');
      await loadAlertas();
    } catch (error) {
      console.error('Erro ao gerar alertas', error);
      alert('Nao foi possivel atualizar os alertas agora.');
    } finally {
      setRunning(false);
    }
  }

  async function atualizarStatus(id: string, action: 'marcar-lido' | 'resolver') {
    try {
      await api.post(`/api/alertas/${id}/${action}`);
      await loadAlertas();
    } catch (error) {
      console.error('Erro ao atualizar alerta', error);
      alert('Nao foi possivel atualizar este alerta.');
    }
  }

  const stats = useMemo(() => {
    const criticos = alertas.filter((item) => item.prioridade === 'CRITICA').length;
    const altos = alertas.filter((item) => item.prioridade === 'ALTA').length;
    const medios = alertas.filter((item) => item.prioridade === 'MEDIA').length;
    return { criticos, altos, medios, total: alertas.length };
  }, [alertas]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div className="flex items-center gap-3">
          <AlertCircle className="h-8 w-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Alertas e IA assistida</h1>
            <p className="text-gray-600">Heuristicas praticas para priorizar pesagem, sanidade e risco operacional.</p>
          </div>
        </div>
        <button onClick={() => void gerarAlertas()} className="btn-primary flex items-center gap-2" disabled={running}>
          <RefreshCcw className={`h-4 w-4 ${running ? 'animate-spin' : ''}`} />
          {running ? 'Atualizando...' : 'Recalcular alertas'}
        </button>
      </div>

      <div className="rounded-2xl border border-primary-100 bg-primary-50 px-5 py-4 text-sm text-primary-700">
        Esta primeira camada de IA do BovCore usa regras operacionais e score heuristico para transformar dados da fazenda em prioridade de acao.
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        {[
          { label: 'Criticos', value: stats.criticos, color: 'text-red-600' },
          { label: 'Altos', value: stats.altos, color: 'text-orange-600' },
          { label: 'Medios', value: stats.medios, color: 'text-yellow-600' },
          { label: 'Total ativo', value: stats.total, color: 'text-gray-700' },
        ].map((stat) => (
          <div key={stat.label} className="card">
            <p className="mb-1 text-sm text-gray-600">{stat.label}</p>
            <p className={`text-3xl font-bold ${stat.color}`}>{stat.value}</p>
          </div>
        ))}
      </div>

      {loading ? (
        <div className="card text-sm text-gray-600">Carregando alertas...</div>
      ) : alertas.length === 0 ? (
        <div className="card">
          <div className="flex items-start gap-3">
            <ShieldCheck className="mt-0.5 h-5 w-5 text-emerald-600" />
            <div>
              <p className="font-semibold text-gray-900">Nenhum alerta ativo no momento</p>
              <p className="mt-1 text-sm text-gray-600">
                Rode o recalculo para analisar novamente pesagens, sanidade e historico recente do rebanho.
              </p>
            </div>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          {alertas.map((alerta) => (
            <div key={alerta.id} className="card">
              <div className="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                <div className="flex flex-1 items-start gap-4">
                  <div className="rounded-lg bg-slate-100 p-3">
                    <Sparkles className="h-6 w-6 text-primary-600" />
                  </div>
                  <div className="flex-1">
                    <div className="mb-2 flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
                      <h3 className="font-bold text-gray-900">{alerta.titulo}</h3>
                      <span className={`rounded-full border px-3 py-1 text-xs font-medium ${priorityStyles[alerta.prioridade] ?? priorityStyles.BAIXA}`}>
                        {alerta.prioridade}
                      </span>
                    </div>
                    <p className="text-gray-700">{alerta.mensagem}</p>
                    {alerta.recomendacao && (
                      <p className="mt-2 rounded-xl bg-primary-50 px-3 py-2 text-sm text-primary-700">
                        Recomendacao: {alerta.recomendacao}
                      </p>
                    )}
                    <div className="mt-3 flex flex-wrap items-center gap-3 text-sm text-gray-500">
                      <span>Tipo: {alerta.tipo.replace(/_/g, ' ')}</span>
                      {alerta.animal && <span>Animal: {alerta.animal.brinco}{alerta.animal.nome ? ` - ${alerta.animal.nome}` : ''}</span>}
                      <span>{new Date(alerta.criadoEm).toLocaleString('pt-BR')}</span>
                    </div>
                  </div>
                </div>
                <div className="flex gap-3 xl:flex-col">
                  <button onClick={() => void atualizarStatus(alerta.id, 'marcar-lido')} className="btn-secondary">
                    Marcar lido
                  </button>
                  <button onClick={() => void atualizarStatus(alerta.id, 'resolver')} className="btn-primary">
                    Resolver
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
