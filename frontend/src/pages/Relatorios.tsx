import { useEffect, useMemo, useState } from 'react';
import { Activity, BarChart3, CalendarClock, Download, FileText, Scale, ShieldAlert, Wallet } from 'lucide-react';
import api from '../services/api';
import type { DashboardResponse, RelatorioRebanhoResponse } from '../types';

const categoryLabel = (value: string) =>
  value
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase());

export default function Relatorios() {
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [rebanho, setRebanho] = useState<RelatorioRebanhoResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    async function loadReports() {
      try {
        setLoading(true);
        setError('');

        const [dashboardRes, rebanhoRes] = await Promise.all([
          api.get<DashboardResponse>('/api/relatorios/dashboard'),
          api.get<RelatorioRebanhoResponse>('/api/relatorios/rebanho'),
        ]);

        setDashboard(dashboardRes.data);
        setRebanho(rebanhoRes.data);
      } catch (requestError: any) {
        setError(requestError.response?.data?.message || 'Nao foi possivel carregar os relatorios agora.');
      } finally {
        setLoading(false);
      }
    }

    void loadReports();
  }, []);

  const topCategories = useMemo(() => {
    const source = dashboard?.kpis.animaisPorCategoria || rebanho?.porCategoria || {};
    return Object.entries(source)
      .sort(([, first], [, second]) => Number(second) - Number(first))
      .slice(0, 5);
  }, [dashboard, rebanho]);

  const statusList = useMemo(() => {
    return Object.entries(rebanho?.porStatus || {}).sort(([, first], [, second]) => Number(second) - Number(first));
  }, [rebanho]);

  const sexoList = useMemo(() => {
    return Object.entries(rebanho?.porSexo || {}).sort(([, first], [, second]) => Number(second) - Number(first));
  }, [rebanho]);

  const summaryCards = [
    {
      label: 'Animais ativos',
      value: dashboard?.kpis.totalAnimaisAtivos ?? 0,
      helper: 'Base operacional atual',
      icon: Activity,
      tone: 'bg-emerald-500',
    },
    {
      label: 'Nascimentos no mes',
      value: dashboard?.kpis.nascimentosMes ?? 0,
      helper: 'Acompanhamento recente',
      icon: CalendarClock,
      tone: 'bg-sky-500',
    },
    {
      label: 'Mortes no mes',
      value: dashboard?.kpis.mortesMes ?? 0,
      helper: 'Sinal de atencao sanitaria',
      icon: ShieldAlert,
      tone: 'bg-rose-500',
    },
    {
      label: 'Peso medio',
      value: rebanho?.pesoMedio ? `${Number(rebanho.pesoMedio).toFixed(0)} kg` : 'N/A',
      helper: `Idade media: ${rebanho?.idadeMediaMeses ?? 0} meses`,
      icon: Scale,
      tone: 'bg-slate-900',
    },
  ];

  const gmdCards = rebanho?.gmdPorJanela ?? [];

  async function exportarPdfFazenda() {
    try {
      setExporting(true);
      const response = await api.get('/api/relatorios/exportar/fazenda.pdf', {
        responseType: 'blob',
      });

      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'relatorio-fazenda-bovcore.pdf');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (requestError: any) {
      setError(requestError.response?.data?.message || 'Nao foi possivel exportar o PDF da fazenda agora.');
    } finally {
      setExporting(false);
    }
  }

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-12 w-12 animate-spin rounded-full border-b-2 border-primary-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div className="flex items-center gap-3">
          <BarChart3 className="h-8 w-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Relatorios simples</h1>
            <p className="text-gray-600">
              Panorama operacional do rebanho, eventos recentes e indicadores que ja fazem parte do plano atual.
            </p>
          </div>
        </div>
        <div className="flex flex-col items-stretch gap-3 sm:items-end">
          <button onClick={() => void exportarPdfFazenda()} disabled={exporting} className="btn-primary inline-flex items-center justify-center gap-2">
            <Download className="h-4 w-4" />
            {exporting ? 'Exportando PDF...' : 'Exportar PDF da fazenda'}
          </button>
          <div className="rounded-2xl border border-primary-100 bg-primary-50 px-4 py-3 text-sm text-primary-700">
            Financeiro detalhado, custos por cabeca e camada estrategica seguem como proximas evolucoes do BovCore.
          </div>
        </div>
      </div>

      {error && (
        <div className="rounded-2xl border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-700">{error}</div>
      )}

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-4">
        {summaryCards.map((card) => {
          const Icon = card.icon;
          return (
            <div key={card.label} className="card">
              <div className={`mb-4 inline-flex rounded-xl p-3 ${card.tone}`}>
                <Icon className="h-5 w-5 text-white" />
              </div>
              <p className="text-sm text-gray-600">{card.label}</p>
              <p className="mt-1 text-2xl font-bold text-gray-900">{card.value}</p>
              <p className="mt-1 text-xs text-gray-500">{card.helper}</p>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        {gmdCards.map((janela) => (
          <div key={janela.janelaDias} className="card">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-emerald-700">GMD {janela.janelaDias} dias</p>
            <p className="mt-2 text-2xl font-bold text-gray-900">
              {janela.ganhoMedioDiario == null ? '-' : `${Number(janela.ganhoMedioDiario).toFixed(3)} kg/dia`}
            </p>
            <p className="mt-1 text-xs text-gray-500">Media consolidada dos animais com base suficiente na janela.</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-bold text-gray-900">Composicao do rebanho</h2>
              <p className="text-sm text-gray-600">Leitura rapida por categoria para apoiar manejo, lote e planejamento.</p>
            </div>
            <div className="rounded-xl bg-gray-50 px-3 py-2 text-right">
              <p className="text-xs uppercase tracking-[0.18em] text-gray-500">Total</p>
              <p className="text-xl font-bold text-gray-900">{rebanho?.totalAnimais ?? 0}</p>
            </div>
          </div>

          <div className="mt-5 space-y-4">
            {topCategories.length === 0 ? (
              <p className="text-sm text-gray-600">Ainda nao ha dados suficientes para montar a composicao do rebanho.</p>
            ) : (
              topCategories.map(([category, count]) => {
                const total = rebanho?.totalAnimais || 1;
                const width = Math.max(6, Math.round((Number(count) / total) * 100));
                return (
                  <div key={category} className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="font-medium text-gray-900">{categoryLabel(category)}</span>
                      <span className="text-gray-600">{count} animais</span>
                    </div>
                    <div className="h-3 overflow-hidden rounded-full bg-gray-100">
                      <div className="h-full rounded-full bg-primary-600" style={{ width: `${width}%` }} />
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        <div className="card space-y-5">
          <div>
            <h2 className="text-lg font-bold text-gray-900">Distribuicao por status</h2>
            <p className="text-sm text-gray-600">Leitura simples para ver o estado atual da base.</p>
          </div>

          <div className="grid gap-3">
            {statusList.length === 0 ? (
              <p className="text-sm text-gray-600">Nenhum status disponivel ainda.</p>
            ) : (
              statusList.map(([status, count]) => (
                <div key={status} className="rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <div className="flex items-center justify-between">
                    <p className="font-medium text-gray-900">{categoryLabel(status)}</p>
                    <p className="text-sm text-gray-600">{count}</p>
                  </div>
                </div>
              ))
            )}
          </div>

          <div>
            <h3 className="text-sm font-semibold uppercase tracking-[0.18em] text-gray-500">Sexo</h3>
            <div className="mt-3 grid grid-cols-2 gap-3">
              {sexoList.map(([sexo, count]) => (
                <div key={sexo} className="rounded-2xl border border-gray-100 bg-white px-4 py-3">
                  <p className="text-sm text-gray-600">{categoryLabel(sexo)}</p>
                  <p className="text-xl font-bold text-gray-900">{count}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <div className="card">
          <div className="flex items-center gap-3">
            <FileText className="h-5 w-5 text-primary-600" />
            <div>
              <h2 className="text-lg font-bold text-gray-900">Eventos recentes</h2>
              <p className="text-sm text-gray-600">Ultimos registros operacionais da fazenda.</p>
            </div>
          </div>

          <div className="mt-4 space-y-3">
            {dashboard?.eventosRecentes.length ? (
              dashboard.eventosRecentes.map((evento) => (
                <div key={`${evento.data}-${evento.tipo}-${evento.animal}`} className="rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <div className="flex items-center justify-between gap-3">
                    <p className="font-medium text-gray-900">{categoryLabel(evento.tipo)}</p>
                    <p className="text-xs uppercase tracking-[0.12em] text-gray-500">{new Date(evento.data).toLocaleDateString('pt-BR')}</p>
                  </div>
                  <p className="mt-1 text-sm text-gray-700">{evento.animal}</p>
                  <p className="mt-1 text-sm text-gray-600">{evento.descricao}</p>
                </div>
              ))
            ) : (
              <p className="text-sm text-gray-600">Nenhum evento recente encontrado.</p>
            )}
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-3">
            <Wallet className="h-5 w-5 text-primary-600" />
            <div>
              <h2 className="text-lg font-bold text-gray-900">Agenda e sinais operacionais</h2>
              <p className="text-sm text-gray-600">Proximos manejos e uma leitura simples do mes.</p>
            </div>
          </div>

          <div className="mt-4 grid gap-4">
            <div className="rounded-2xl border border-gray-100 bg-primary-50 px-4 py-4">
              <p className="text-sm text-primary-700">Despesas do mes</p>
              <p className="mt-1 text-2xl font-bold text-primary-900">
                {Number(dashboard?.kpis.despesasMes || 0).toLocaleString('pt-BR', {
                  style: 'currency',
                  currency: 'BRL',
                })}
              </p>
              <p className="mt-1 text-xs text-primary-700">Visao resumida. Financeiro detalhado segue como proxima camada.</p>
            </div>

            <div className="space-y-3">
              {dashboard?.agendamentosProximos.length ? (
                dashboard.agendamentosProximos.map((agendamento) => (
                  <div
                    key={`${agendamento.dataPrevista}-${agendamento.tipo}-${agendamento.animal}`}
                    className="rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3"
                  >
                    <div className="flex items-center justify-between gap-3">
                      <p className="font-medium text-gray-900">{categoryLabel(agendamento.tipo)}</p>
                      <p className="text-xs uppercase tracking-[0.12em] text-gray-500">
                        {new Date(agendamento.dataPrevista).toLocaleDateString('pt-BR')}
                      </p>
                    </div>
                    <p className="mt-1 text-sm text-gray-700">{agendamento.animal}</p>
                    <p className="mt-1 text-sm text-gray-600">{agendamento.produto}</p>
                  </div>
                ))
              ) : (
                <div className="rounded-2xl border border-dashed border-gray-200 px-4 py-6 text-sm text-gray-600">
                  Nenhum agendamento proximo encontrado neste momento.
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
