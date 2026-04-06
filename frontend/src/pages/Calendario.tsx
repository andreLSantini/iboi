import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, Calendar, ClipboardList, Scale, ShieldPlus, Syringe } from 'lucide-react';
import api from '../services/api';
import type { AgendaOperacionalResponse, CategoriaAgenda, SituacaoAgenda } from '../types';

function categoriaIcon(categoria: CategoriaAgenda) {
  switch (categoria) {
    case 'SANITARIO':
      return Syringe;
    case 'PESAGEM':
      return Scale;
    case 'REPRODUCAO':
      return ShieldPlus;
    default:
      return ClipboardList;
  }
}

function categoriaLabel(categoria: CategoriaAgenda) {
  switch (categoria) {
    case 'SANITARIO':
      return 'Sanitario';
    case 'PESAGEM':
      return 'Pesagem';
    case 'REPRODUCAO':
      return 'Reproducao';
    default:
      return categoria;
  }
}

function situacaoClasses(situacao: SituacaoAgenda) {
  switch (situacao) {
    case 'ATRASADO':
      return 'bg-red-100 text-red-800';
    case 'HOJE':
      return 'bg-amber-100 text-amber-800';
    case 'PROXIMO':
      return 'bg-emerald-100 text-emerald-800';
    default:
      return 'bg-slate-100 text-slate-800';
  }
}

export default function Calendario() {
  const [agenda, setAgenda] = useState<AgendaOperacionalResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [categoriaAtiva, setCategoriaAtiva] = useState<'TODAS' | CategoriaAgenda>('TODAS');

  useEffect(() => {
    async function loadAgenda() {
      try {
        setLoading(true);
        setError('');
        const response = await api.get<AgendaOperacionalResponse>('/api/agenda/operacional');
        setAgenda(response.data);
      } catch (requestError: any) {
        setError(requestError.response?.data?.message || 'Nao foi possivel carregar a agenda operacional.');
      } finally {
        setLoading(false);
      }
    }

    void loadAgenda();
  }, []);

  const itens = useMemo(() => {
    const lista = agenda?.itens ?? [];
    if (categoriaAtiva === 'TODAS') {
      return lista;
    }
    return lista.filter((item) => item.categoria === categoriaAtiva);
  }, [agenda, categoriaAtiva]);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Calendar className="h-8 w-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Agenda operacional</h1>
          <p className="text-gray-600">Manejo sanitario, pesagem e reproducao em uma fila unica de execucao.</p>
        </div>
      </div>

      {loading ? (
        <div className="card py-12 text-center text-gray-600">Carregando agenda...</div>
      ) : error ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
            <div className="card">
              <p className="text-sm text-gray-600">Total de itens</p>
              <p className="mt-2 text-2xl font-bold text-gray-900">{agenda?.resumo.total ?? 0}</p>
            </div>
            <div className="card">
              <p className="text-sm text-gray-600">Atrasados</p>
              <p className="mt-2 text-2xl font-bold text-red-600">{agenda?.resumo.atrasados ?? 0}</p>
            </div>
            <div className="card">
              <p className="text-sm text-gray-600">Para hoje</p>
              <p className="mt-2 text-2xl font-bold text-amber-600">{agenda?.resumo.hoje ?? 0}</p>
            </div>
            <div className="card">
              <p className="text-sm text-gray-600">Proximos 7 dias</p>
              <p className="mt-2 text-2xl font-bold text-emerald-600">{agenda?.resumo.proximos7Dias ?? 0}</p>
            </div>
          </div>

          <div className="card">
            <div className="flex flex-wrap gap-2">
              {(['TODAS', 'SANITARIO', 'PESAGEM', 'REPRODUCAO'] as const).map((categoria) => (
                <button
                  key={categoria}
                  onClick={() => setCategoriaAtiva(categoria)}
                  className={`rounded-full px-4 py-2 text-sm font-medium ${
                    categoriaAtiva === categoria ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  {categoria === 'TODAS' ? 'Todas' : categoriaLabel(categoria)}
                </button>
              ))}
            </div>
          </div>

          <div className="space-y-4">
            {itens.length === 0 ? (
              <div className="card py-12 text-center">
                <ClipboardList className="mx-auto mb-4 h-12 w-12 text-gray-300" />
                <p className="text-gray-600">Nenhum item encontrado para este filtro.</p>
              </div>
            ) : (
              itens.map((item) => {
                const Icon = categoriaIcon(item.categoria);
                return (
                  <div key={item.id} className="card">
                    <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                      <div className="flex gap-4">
                        <div className="rounded-2xl bg-primary-50 p-3 text-primary-700">
                          <Icon className="h-5 w-5" />
                        </div>
                        <div>
                          <div className="flex flex-wrap items-center gap-2">
                            <h2 className="text-lg font-bold text-gray-900">{item.titulo}</h2>
                            <span className={`rounded-full px-3 py-1 text-xs font-semibold ${situacaoClasses(item.situacao)}`}>
                              {item.situacao}
                            </span>
                            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
                              {categoriaLabel(item.categoria)}
                            </span>
                          </div>
                          <p className="mt-2 text-sm text-gray-600">{item.descricao}</p>
                          <div className="mt-3 flex flex-wrap gap-4 text-sm text-gray-600">
                            <span>Data prevista: {new Date(item.dataPrevista).toLocaleDateString('pt-BR')}</span>
                            <span>
                              {item.diasParaVencimento < 0
                                ? `${Math.abs(item.diasParaVencimento)} dia(s) atrasado(s)`
                                : item.diasParaVencimento === 0
                                  ? 'Vence hoje'
                                  : `Vence em ${item.diasParaVencimento} dia(s)`}
                            </span>
                            {item.animal && (
                              <span>
                                Animal: {item.animal.brinco}
                                {item.animal.nome ? ` - ${item.animal.nome}` : ''}
                              </span>
                            )}
                            {item.lote && <span>Lote: {item.lote.nome}</span>}
                          </div>
                        </div>
                      </div>
                      {item.situacao === 'ATRASADO' && <AlertTriangle className="h-5 w-5 text-red-500" />}
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </>
      )}
    </div>
  );
}
