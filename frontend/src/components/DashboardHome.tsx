import { useEffect, useMemo, useRef, useState } from 'react';
import { Activity, AlertTriangle, Beef, CheckCircle2, CircleDashed, FileSpreadsheet, MapPinned, PackagePlus, TrendingUp, UploadCloud } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { getCurrentFarm, getUser } from '../services/session';
import type { AnimalDto, CategoriaAnimal, EventoDto, FarmDetail, ImportarAnimaisResponse, LoteDto, Pasture } from '../types';

interface DashboardStats {
  totalAnimais: number;
  animaisAtivos: number;
  animaisVendidos: number;
  eventosHoje: number;
  eventosUltimos7Dias: number;
  pesoMedio: number;
  categorias: Record<CategoriaAnimal, number>;
}

interface ChecklistItem {
  id: string;
  title: string;
  description: string;
  done: boolean;
  actionLabel?: string;
  action?: () => void;
}

const initialStats: DashboardStats = {
  totalAnimais: 0,
  animaisAtivos: 0,
  animaisVendidos: 0,
  eventosHoje: 0,
  eventosUltimos7Dias: 0,
  pesoMedio: 0,
  categorias: {
    BEZERRO: 0,
    NOVILHO: 0,
    NOVILHA: 0,
    BOI: 0,
    VACA: 0,
    TOURO: 0,
    MATRIZ: 0,
  },
};

export default function DashboardHome() {
  const navigate = useNavigate();
  const user = getUser();
  const currentFarm = getCurrentFarm();
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [stats, setStats] = useState<DashboardStats>(initialStats);
  const [animais, setAnimais] = useState<AnimalDto[]>([]);
  const [eventos, setEventos] = useState<EventoDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [farmDetail, setFarmDetail] = useState<FarmDetail | null>(null);
  const [pastures, setPastures] = useState<Pasture[]>([]);
  const [lotes, setLotes] = useState<LoteDto[]>([]);
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState<ImportarAnimaisResponse | null>(null);
  const [assistantError, setAssistantError] = useState('');

  useEffect(() => {
    void loadDashboardData();
  }, []);

  async function loadDashboardData() {
    try {
      setLoading(true);

      const requests: Promise<unknown>[] = [
        api.get('/api/animais'),
        api.get('/api/eventos'),
        api.get('/api/lotes', { params: { apenasAtivos: true } }),
      ];

      if (currentFarm?.id) {
        requests.push(api.get(`/api/farms/${currentFarm.id}`));
        requests.push(api.get(`/api/farms/${currentFarm.id}/pastures`));
      }

      const [animaisRes, eventosRes, lotesRes, farmRes, pasturesRes] = (await Promise.all(requests)) as any[];

      const animaisData = Array.isArray(animaisRes.data) ? animaisRes.data : animaisRes.data.content || [];
      const eventosData = Array.isArray(eventosRes.data) ? eventosRes.data : eventosRes.data.content || [];
      const lotesData = Array.isArray(lotesRes.data) ? lotesRes.data : lotesRes.data.content || [];

      const hoje = new Date();
      hoje.setHours(0, 0, 0, 0);
      const seteDiasAtras = new Date(hoje);
      seteDiasAtras.setDate(seteDiasAtras.getDate() - 7);

      const eventosHoje = eventosData.filter((evento: EventoDto) => {
        const eventDate = new Date(evento.data);
        eventDate.setHours(0, 0, 0, 0);
        return eventDate.getTime() === hoje.getTime();
      }).length;

      const eventosUltimos7Dias = eventosData.filter((evento: EventoDto) => new Date(evento.data) >= seteDiasAtras).length;
      const animaisAtivos = animaisData.filter((animal: AnimalDto) => animal.status === 'ATIVO');
      const pesoMedio = animaisAtivos.reduce((sum: number, animal: AnimalDto) => sum + (animal.pesoAtual || 0), 0) / animaisAtivos.length || 0;

      const categorias: Record<CategoriaAnimal, number> = {
        BEZERRO: 0,
        NOVILHO: 0,
        NOVILHA: 0,
        BOI: 0,
        VACA: 0,
        TOURO: 0,
        MATRIZ: 0,
      };

      animaisAtivos.forEach((animal: AnimalDto) => {
        categorias[animal.categoria]++;
      });

      setStats({
        totalAnimais: animaisData.length,
        animaisAtivos: animaisAtivos.length,
        animaisVendidos: animaisData.filter((animal: AnimalDto) => animal.status === 'VENDIDO').length,
        eventosHoje,
        eventosUltimos7Dias,
        pesoMedio: Math.round(pesoMedio),
        categorias,
      });

      setAnimais(animaisData);
      setEventos(eventosData.slice(0, 5));
      setLotes(lotesData);
      setFarmDetail(farmRes?.data ?? null);
      setPastures(pasturesRes?.data ?? []);
    } catch (error) {
      console.error('Erro ao carregar dashboard', error);
    } finally {
      setLoading(false);
    }
  }

  const farmProfileComplete = useMemo(() => {
    if (!farmDetail) {
      return false;
    }

    return Boolean(
      farmDetail.name &&
      farmDetail.city &&
      farmDetail.state &&
      farmDetail.size &&
      farmDetail.ownerName &&
      farmDetail.ownerDocument
    );
  }, [farmDetail]);

  const checklist: ChecklistItem[] = [
    {
      id: 'conta',
      title: 'Conta criada',
      description: 'Usuario administrador cadastrado e autenticado no sistema.',
      done: Boolean(user?.id),
    },
    {
      id: 'fazenda',
      title: 'Fazenda inicial criada',
      description: 'Primeira fazenda pronta para receber lotes, pastos e animais.',
      done: Boolean(currentFarm?.id),
      actionLabel: 'Gestao de Fazendas',
      action: () => navigate('/app/fazendas'),
    },
    {
      id: 'dados-fazenda',
      title: 'Dados da fazenda preenchidos',
      description: 'Complete responsavel, documento e area para ativar melhor os modulos.',
      done: farmProfileComplete,
      actionLabel: 'Completar dados',
      action: () => navigate('/app/fazendas'),
    },
    {
      id: 'lotes',
      title: 'Primeiro lote criado',
      description: 'Organize o rebanho por lote antes dos manejos e relatorios.',
      done: lotes.length > 0,
      actionLabel: 'Criar lote',
      action: () => navigate('/app/lotes'),
    },
    {
      id: 'pastos',
      title: 'Primeiro pasto criado',
      description: 'Cadastre ao menos um pasto para operar de forma visual no mapa.',
      done: pastures.length > 0,
      actionLabel: 'Cadastrar pasto',
      action: () => navigate('/app/fazendas'),
    },
    {
      id: 'animais',
      title: 'Animais importados ou cadastrados',
      description: 'Suba uma planilha CSV ou cadastre manualmente o primeiro rebanho.',
      done: animais.length > 0,
      actionLabel: 'Importar planilha',
      action: () => fileInputRef.current?.click(),
    },
  ];

  const completedCount = checklist.filter((item) => item.done).length;
  const progress = Math.round((completedCount / checklist.length) * 100);

  const cards = [
    { title: 'Total de animais', value: stats.totalAnimais, subtitle: `${stats.animaisAtivos} ativos`, icon: Beef, color: 'bg-blue-500' },
    { title: 'Eventos (7 dias)', value: stats.eventosUltimos7Dias, subtitle: `${stats.eventosHoje} hoje`, icon: Activity, color: 'bg-green-500' },
    { title: 'Peso medio', value: stats.pesoMedio > 0 ? `${stats.pesoMedio} kg` : 'N/A', subtitle: 'Rebanho ativo', icon: TrendingUp, color: 'bg-slate-900' },
    { title: 'Ativacao da fazenda', value: `${progress}%`, subtitle: `${completedCount}/${checklist.length} etapas`, icon: CheckCircle2, color: 'bg-amber-500' },
  ];

  const downloadTemplate = () => {
    const csv = [
      'brinco;nome;sexo;raca;dataNascimento;pesoAtual;categoria;lote;pasto;rfid;codigoSisbov;dataEntrada;sisbovAtivo;observacoes',
      '001;Lote inicial;MACHO;NELORE;2024-01-10;210;BEZERRO;Lote A;Pasto 1;;;2024-01-10;false;Primeiro cadastro',
    ].join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'modelo-importacao-animais.csv');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  };

  const handleImportFile = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }

    setImporting(true);
    setAssistantError('');
    setImportResult(null);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await api.post<ImportarAnimaisResponse>('/api/animais/importar-csv', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      setImportResult(response.data);
      await loadDashboardData();
    } catch (error: any) {
      setAssistantError(error.response?.data?.message || 'Nao foi possivel importar a planilha.');
    } finally {
      setImporting(false);
      event.target.value = '';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="h-12 w-12 animate-spin rounded-full border-b-2 border-primary-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <input ref={fileInputRef} type="file" accept=".csv,text/csv" className="hidden" onChange={(e) => void handleImportFile(e)} />

      <div className="card">
        <div className="flex items-center gap-3">
          <Activity className="h-8 w-8 text-primary-600" />
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Bem-vindo, {user?.nome ?? 'Admin'}.</h2>
            <p className="text-gray-600">
                Resumo operacional de {currentFarm?.nome ?? 'sua fazenda'} e ativacao do seu plano.
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <div key={card.title} className="card hover:shadow-lg transition-shadow">
              <div className={`mb-4 inline-flex rounded-lg p-3 ${card.color}`}>
                <Icon className="h-6 w-6 text-white" />
              </div>
              <p className="text-sm text-gray-600">{card.title}</p>
              <p className="mt-1 text-2xl font-bold text-gray-900">{card.value}</p>
              <p className="mt-1 text-xs text-gray-500">{card.subtitle}</p>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[1.6fr_1fr]">
        <div className="card">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary-500">Primeiros passos</p>
              <h3 className="mt-2 text-xl font-bold text-gray-900">Assistente de ativacao da fazenda</h3>
              <p className="mt-1 text-sm text-gray-600">
                Deixe a operacao pronta para usar no campo com importacao rapida, lotes, pastos e dados basicos completos.
              </p>
            </div>
            <div className="rounded-2xl bg-primary-50 px-4 py-3 text-right">
              <p className="text-xs uppercase tracking-[0.18em] text-primary-600">Ativacao</p>
              <p className="text-2xl font-bold text-primary-700">{progress}%</p>
              <p className="text-xs text-primary-600">{completedCount} de {checklist.length} etapas concluidas</p>
            </div>
          </div>

          <div className="mt-5 h-3 overflow-hidden rounded-full bg-gray-100">
            <div className="h-full rounded-full bg-primary-600 transition-all" style={{ width: `${progress}%` }} />
          </div>

          <div className="mt-6 space-y-3">
            {checklist.map((item) => (
              <div key={item.id} className="flex flex-col gap-3 rounded-2xl border border-gray-100 bg-gray-50 p-4 lg:flex-row lg:items-center lg:justify-between">
                <div className="flex items-start gap-3">
                  {item.done ? (
                    <CheckCircle2 className="mt-0.5 h-5 w-5 text-emerald-600" />
                  ) : (
                    <CircleDashed className="mt-0.5 h-5 w-5 text-amber-500" />
                  )}
                  <div>
                    <p className="font-medium text-gray-900">{item.title}</p>
                    <p className="text-sm text-gray-600">{item.description}</p>
                  </div>
                </div>
                {!item.done && item.action && item.actionLabel && (
                  <button onClick={item.action} className="btn-secondary whitespace-nowrap">
                    {item.actionLabel}
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-6">
          <div className="card">
            <div className="flex items-start gap-3">
              <FileSpreadsheet className="mt-1 h-6 w-6 text-primary-600" />
              <div className="flex-1">
                <h3 className="text-lg font-bold text-gray-900">Importacao simples por planilha</h3>
                <p className="mt-1 text-sm text-gray-600">
                  Suba um CSV com brinco, sexo, raca, data de nascimento e categoria para comecar rapido.
                </p>
              </div>
            </div>

            <div className="mt-4 grid gap-3">
              <button onClick={downloadTemplate} className="btn-secondary flex items-center justify-center gap-2">
                <FileSpreadsheet className="h-4 w-4" />
                Baixar modelo CSV
              </button>
              <button onClick={() => fileInputRef.current?.click()} disabled={importing} className="btn-primary flex items-center justify-center gap-2">
                <UploadCloud className="h-4 w-4" />
                {importing ? 'Importando...' : 'Importar animais'}
              </button>
            </div>

            {assistantError && (
              <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {assistantError}
              </div>
            )}

            {importResult && (
              <div className="mt-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                <p className="font-semibold">Importacao concluida</p>
                <p className="mt-1">
                  {importResult.importados} importados, {importResult.ignorados} ignorados, {importResult.totalLinhas} linhas processadas.
                </p>
                {importResult.erros.length > 0 && (
                  <div className="mt-2 space-y-1 text-xs text-amber-800">
                    {importResult.erros.slice(0, 5).map((erro) => (
                      <p key={erro}>{erro}</p>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="card">
            <h3 className="text-lg font-bold text-gray-900">Acoes rapidas de ativacao</h3>
            <div className="mt-4 space-y-3">
              <button onClick={() => navigate('/app/fazendas')} className="flex w-full items-center justify-between rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3 text-left hover:border-primary-200 hover:bg-primary-50">
                <div className="flex items-center gap-3">
                  <MapPinned className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="font-medium text-gray-900">Completar dados da fazenda</p>
                    <p className="text-sm text-gray-600">Documentos, area, responsavel e pastos.</p>
                  </div>
                </div>
              </button>

              <button onClick={() => navigate('/app/lotes')} className="flex w-full items-center justify-between rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3 text-left hover:border-primary-200 hover:bg-primary-50">
                <div className="flex items-center gap-3">
                  <PackagePlus className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="font-medium text-gray-900">Criar primeiro lote</p>
                    <p className="text-sm text-gray-600">Organize animais antes dos manejos.</p>
                  </div>
                </div>
              </button>

              <button onClick={() => navigate('/app/animais')} className="flex w-full items-center justify-between rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3 text-left hover:border-primary-200 hover:bg-primary-50">
                <div className="flex items-center gap-3">
                  <Beef className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="font-medium text-gray-900">Cadastrar manualmente</p>
                    <p className="text-sm text-gray-600">Se preferir, cadastre o primeiro animal agora.</p>
                  </div>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Free e billing</h3>
            <p className="text-sm text-gray-600">Acompanhe seu plano atual e destrave camadas pagas conforme a operacao cresce.</p>
          </div>
          <button onClick={() => navigate('/app/assinatura')} className="btn-primary">
            Fazer upgrade
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="card">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-gray-900">Ultimos eventos</h3>
            <button onClick={() => navigate('/app/eventos')} className="text-sm font-medium text-primary-600">
              Ver todos
            </button>
          </div>
          <div className="mt-4 space-y-3">
            {eventos.length === 0 ? (
              <p className="text-sm text-gray-600">Nenhum evento registrado.</p>
            ) : (
              eventos.map((evento) => (
                <div key={evento.id} className="rounded-lg bg-gray-50 p-3">
                  <p className="font-medium text-gray-900">{evento.tipo.replace(/_/g, ' ')}</p>
                  <p className="text-sm text-gray-600">{evento.animal.brinco}{evento.animal.nome ? ` - ${evento.animal.nome}` : ''}</p>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="card">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-gray-900">Animais recentes</h3>
            <button onClick={() => navigate('/app/animais')} className="text-sm font-medium text-primary-600">
              Ver todos
            </button>
          </div>
          <div className="mt-4 space-y-3">
            {animais.slice(0, 5).length === 0 ? (
              <p className="text-sm text-gray-600">Nenhum animal cadastrado.</p>
            ) : (
              animais.slice(0, 5).map((animal) => (
                <div key={animal.id} className="rounded-lg bg-gray-50 p-3">
                  <p className="font-medium text-gray-900">
                    Brinco {animal.brinco}
                    {animal.nome ? ` - ${animal.nome}` : ''}
                  </p>
                  <p className="text-sm text-gray-600">
                    {animal.raca} • {animal.categoria}
                    {animal.pasture ? ` • ${animal.pasture.nome}` : ''}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {progress < 100 && (
        <div className="rounded-2xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm text-amber-900">
          <div className="flex items-start gap-3">
            <AlertTriangle className="mt-0.5 h-5 w-5 text-amber-600" />
            <div>
              <p className="font-semibold">Ativacao ainda incompleta</p>
              <p className="mt-1">
                Quanto mais cedo voce concluir a configuracao da fazenda e subir o rebanho, mais rapido o sistema passa a gerar relatorios, alertas e recomendacoes uteis.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
