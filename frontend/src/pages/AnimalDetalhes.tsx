import { type ComponentType, type ReactNode, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  CheckCircle2,
  Download,
  Edit3,
  Filter,
  GitBranch,
  MapPinned,
  Plus,
  ShieldPlus,
  Tag,
  TrendingUp,
  Weight,
  X,
} from 'lucide-react';
import api from '../services/api';
import { getCurrentFarm, getFarms } from '../services/session';
import type {
  AnimalDto,
  AnimalFichaCompletaDto,
  AtualizarAnimalRequest,
  CategoriaAnimal,
  EventoDto,
  FarmSummary,
  LoteDto,
  MovimentacaoAnimalDto,
  OrigemAnimal,
  Pasture,
  PesagemAnimalDto,
  Raca,
  RegistrarMovimentacaoAnimalRequest,
  RegistrarVacinacaoAnimalRequest,
  StatusAnimal,
  TipoMovimentacaoAnimal,
  TipoVacina,
  VacinacaoAnimalDto,
} from '../types';

type TimelineCategory = 'all' | 'evento' | 'vacinacao' | 'movimentacao';

type TimelineItem = {
  id: string;
  date: string;
  category: 'evento' | 'vacinacao' | 'movimentacao';
  title: string;
  subtitle?: string;
  description?: string;
  meta: string[];
};

function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-3xl bg-white shadow-xl">
        <div className="sticky top-0 flex items-center justify-between border-b border-gray-100 bg-white px-6 py-4">
          <h2 className="text-xl font-bold text-gray-900">{title}</h2>
          <button onClick={onClose} className="rounded-lg p-2 hover:bg-gray-100">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

function StatCard({
  icon: Icon,
  title,
  value,
  subtitle,
  color,
}: {
  icon: ComponentType<{ className?: string }>;
  title: string;
  value: string;
  subtitle: string;
  color: string;
}) {
  return (
    <div className="card">
      <div className={`mb-3 inline-flex rounded-lg p-3 ${color}`}>
        <Icon className="h-5 w-5 text-white" />
      </div>
      <p className="text-sm text-gray-600">{title}</p>
      <p className="mt-1 text-xl font-bold text-gray-900">{value}</p>
      <p className="mt-1 text-xs text-gray-500">{subtitle}</p>
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-start justify-between gap-4 border-b border-gray-100 py-2">
      <span className="text-sm text-gray-600">{label}</span>
      <span className="text-right text-sm font-medium text-gray-900">{value}</span>
    </div>
  );
}

function QuickInsight({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-2xl border border-gray-100 bg-white/70 p-4">
      <p className="text-sm font-semibold text-gray-900">{title}</p>
      <p className="mt-1 text-sm text-gray-600">{description}</p>
    </div>
  );
}

function WeightCurve({ pesagens }: { pesagens: PesagemAnimalDto[] }) {
  if (pesagens.length === 0) {
    return <p className="text-sm text-gray-500">Nenhuma pesagem registrada.</p>;
  }

  if (pesagens.length === 1) {
    return (
      <div className="rounded-2xl bg-gray-50 p-4 text-sm text-gray-600">
        Existe apenas uma pesagem registrada: <span className="font-medium text-gray-900">{pesagens[0].peso} kg</span> em{' '}
        {new Date(pesagens[0].data).toLocaleDateString('pt-BR')}.
      </div>
    );
  }

  const width = 640;
  const height = 220;
  const padding = 28;
  const values = pesagens.map((item) => Number(item.peso || 0));
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = Math.max(1, max - min);
  const points = pesagens.map((item, index) => {
    const x = padding + (index * (width - padding * 2)) / Math.max(1, pesagens.length - 1);
    const y = height - padding - ((Number(item.peso || 0) - min) / range) * (height - padding * 2);
    return { x, y };
  });
  const path = points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ');

  return (
    <div className="space-y-4">
      <div className="overflow-hidden rounded-3xl border border-emerald-100 bg-gradient-to-br from-emerald-50 via-white to-blue-50 p-4">
        <svg viewBox={`0 0 ${width} ${height}`} className="h-56 w-full">
          <defs>
            <linearGradient id="weightLine" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#10b981" />
              <stop offset="100%" stopColor="#2563eb" />
            </linearGradient>
          </defs>
          {[0, 0.5, 1].map((step) => {
            const y = padding + step * (height - padding * 2);
            const label = Math.round(max - step * range);
            return (
              <g key={step}>
                <line x1={padding} y1={y} x2={width - padding} y2={y} stroke="#d1d5db" strokeDasharray="4 6" />
                <text x={8} y={y + 4} fontSize="11" fill="#6b7280">
                  {label} kg
                </text>
              </g>
            );
          })}
          <path d={path} fill="none" stroke="url(#weightLine)" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
          {points.map((point, index) => (
            <circle key={index} cx={point.x} cy={point.y} r="5" fill="#ffffff" stroke="#10b981" strokeWidth="3" />
          ))}
        </svg>
      </div>
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
        {pesagens.map((pesagem) => (
          <div key={pesagem.id} className="rounded-2xl bg-gray-50 p-3">
            <p className="text-sm font-medium text-gray-900">{pesagem.peso} kg</p>
            <p className="text-xs text-gray-500">{new Date(pesagem.data).toLocaleDateString('pt-BR')}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function AnimalDetalhes() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const currentFarm = getCurrentFarm();
  const farms = getFarms();

  const [animal, setAnimal] = useState<AnimalDto | null>(null);
  const [pesagens, setPesagens] = useState<PesagemAnimalDto[]>([]);
  const [eventos, setEventos] = useState<EventoDto[]>([]);
  const [eventosReprodutivos, setEventosReprodutivos] = useState<EventoDto[]>([]);
  const [vacinacoes, setVacinacoes] = useState<VacinacaoAnimalDto[]>([]);
  const [movimentacoes, setMovimentacoes] = useState<MovimentacaoAnimalDto[]>([]);
  const [pastures, setPastures] = useState<Pasture[]>([]);
  const [lotes, setLotes] = useState<LoteDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingInline, setEditingInline] = useState(false);
  const [savingInline, setSavingInline] = useState(false);
  const [timelineFilter, setTimelineFilter] = useState<TimelineCategory>('all');
  const [showVaccinationModal, setShowVaccinationModal] = useState(false);
  const [showMovementModal, setShowMovementModal] = useState(false);
  const [savingVaccination, setSavingVaccination] = useState(false);
  const [savingMovement, setSavingMovement] = useState(false);
  const [exportingPdf, setExportingPdf] = useState(false);

  const [vaccinationForm, setVaccinationForm] = useState<RegistrarVacinacaoAnimalRequest>({
    tipo: 'AFTOSA',
    nomeVacina: '',
    aplicadaEm: new Date().toISOString().split('T')[0],
    dose: undefined,
    unidadeMedida: 'ml',
    proximaDoseEm: '',
    fabricante: '',
    loteVacina: '',
    observacoes: '',
  });

  const [movementForm, setMovementForm] = useState<RegistrarMovimentacaoAnimalRequest>({
    tipo: 'ENTRE_PASTOS',
    movimentadaEm: new Date().toISOString().split('T')[0],
    destinoFarmId: '',
    destinoPastureId: '',
    numeroGta: '',
    documentoExterno: '',
    motivo: '',
    observacoes: '',
  });

  const [inlineForm, setInlineForm] = useState<AtualizarAnimalRequest>({
    rfid: '',
    codigoSisbov: '',
    nome: '',
    raca: undefined,
    pesoAtual: undefined,
    categoria: undefined,
    origem: undefined,
    loteId: undefined,
    pastureId: undefined,
    status: undefined,
    dataEntrada: '',
    sisbovAtivo: false,
    observacoes: '',
  });

  useEffect(() => {
    if (id) {
      void loadAnimalData(id);
    }
  }, [id]);

  async function loadAnimalData(animalId: string) {
    try {
      setLoading(true);
      setError('');

      const requests: Promise<any>[] = [
        api.get<AnimalFichaCompletaDto>(`/api/animais/${animalId}/ficha-completa`),
        api.get('/api/lotes', { params: { apenasAtivos: true } }),
      ];

      if (currentFarm?.id) {
        requests.push(api.get(`/api/farms/${currentFarm.id}/pastures`));
      }

      const [animalRes, lotesRes, pasturesRes] = await Promise.all(requests);
      const fichaCompleta = animalRes.data as AnimalFichaCompletaDto;
      const animalData = fichaCompleta.animal;

      setAnimal(animalData);
      setPesagens(Array.isArray(fichaCompleta.pesagens) ? fichaCompleta.pesagens : []);
      setEventos(Array.isArray(fichaCompleta.eventos) ? fichaCompleta.eventos : []);
      setEventosReprodutivos(Array.isArray(fichaCompleta.eventosReprodutivos) ? fichaCompleta.eventosReprodutivos : []);
      setVacinacoes(Array.isArray(fichaCompleta.vacinacoes) ? fichaCompleta.vacinacoes : []);
      setMovimentacoes(Array.isArray(fichaCompleta.movimentacoes) ? fichaCompleta.movimentacoes : []);
      setLotes(Array.isArray(lotesRes.data) ? lotesRes.data : lotesRes.data.content || []);
      setPastures(pasturesRes?.data ?? []);
      setInlineForm({
        rfid: animalData.rfid || '',
        codigoSisbov: animalData.codigoSisbov || '',
        nome: animalData.nome || '',
        raca: animalData.raca,
        pesoAtual: animalData.pesoAtual,
        categoria: animalData.categoria,
        origem: animalData.origem,
        loteId: animalData.lote?.id,
        pastureId: animalData.pasture?.id,
        status: animalData.status,
        dataEntrada: animalData.dataEntrada || '',
        sisbovAtivo: animalData.sisbovAtivo,
        observacoes: animalData.observacoes || '',
      });
    } catch (loadError) {
      console.error('Erro ao carregar dados do animal:', loadError);
      setError('Nao foi possivel carregar a ficha do animal.');
    } finally {
      setLoading(false);
    }
  }

  const timeline = useMemo<TimelineItem[]>(() => {
    const items: TimelineItem[] = [];

    eventos.forEach((evento) => {
      items.push({
        id: `evento-${evento.id}`,
        date: evento.data,
        category: 'evento',
        title: evento.tipo.replace(/_/g, ' '),
        subtitle: evento.descricao,
        meta: [
          evento.peso ? `Peso ${evento.peso} kg` : '',
          evento.produto ? `Produto ${evento.produto}` : '',
          evento.valor ? `Valor R$ ${evento.valor.toFixed(2)}` : '',
        ].filter(Boolean),
      });
    });

    vacinacoes.forEach((vacinacao) => {
      items.push({
        id: `vacinacao-${vacinacao.id}`,
        date: vacinacao.aplicadaEm,
        category: 'vacinacao',
        title: `Vacina ${vacinacao.nomeVacina}`,
        subtitle: vacinacao.tipo.replace(/_/g, ' '),
        meta: [
          vacinacao.dose ? `Dose ${vacinacao.dose} ${vacinacao.unidadeMedida || ''}`.trim() : '',
          vacinacao.loteVacina ? `Lote ${vacinacao.loteVacina}` : '',
          vacinacao.proximaDoseEm ? `Proxima dose ${new Date(vacinacao.proximaDoseEm).toLocaleDateString('pt-BR')}` : '',
        ].filter(Boolean),
      });
    });

    movimentacoes.forEach((movimentacao) => {
      items.push({
        id: `movimentacao-${movimentacao.id}`,
        date: movimentacao.movimentadaEm,
        category: 'movimentacao',
        title: movimentacao.tipo.replace(/_/g, ' '),
        subtitle: movimentacao.motivo || 'Registro de rastreabilidade',
        description: movimentacao.pastureDestino?.nome
          ? `Destino: ${movimentacao.pastureDestino.nome}`
          : movimentacao.farmDestino?.nome
            ? `Destino: ${movimentacao.farmDestino.nome}`
            : undefined,
        meta: [
          movimentacao.numeroGta ? `GTA ${movimentacao.numeroGta}` : '',
          movimentacao.documentoExterno ? `Doc ${movimentacao.documentoExterno}` : '',
          movimentacao.responsavel ? `Resp. ${movimentacao.responsavel}` : '',
        ].filter(Boolean),
      });
    });

    return items.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }, [eventos, vacinacoes, movimentacoes]);

  const filteredTimeline = useMemo(() => {
    if (timelineFilter === 'all') return timeline;
    return timeline.filter((item) => item.category === timelineFilter);
  }, [timeline, timelineFilter]);

  const pesagensOrdenadas = useMemo(
    () => [...pesagens].sort((a, b) => new Date(a.data).getTime() - new Date(b.data).getTime()),
    [pesagens],
  );

  const ganhoPesoMedio = useMemo(() => {
    if (pesagensOrdenadas.length < 2) return null;
    const first = pesagensOrdenadas[0];
    const last = pesagensOrdenadas[pesagensOrdenadas.length - 1];
    const diffDays = Math.max(1, Math.round((new Date(last.data).getTime() - new Date(first.data).getTime()) / (1000 * 60 * 60 * 24)));
    return ((Number(last.peso) - Number(first.peso)) / diffDays).toFixed(2);
  }, [pesagensOrdenadas]);

  const quickInsights = useMemo(() => {
    if (!animal) return [];
    return [
      {
        title: animal.pasture ? 'Animal localizado' : 'Sem localizacao operacional',
        description: animal.pasture
          ? `Atualmente no pasto ${animal.pasture.nome}${animal.lote ? ` e lote ${animal.lote.nome}` : ''}.`
          : 'Vincule o animal a um pasto para melhorar o manejo visual e a rastreabilidade.',
      },
      {
        title: animal.sisbovAtivo ? 'Compliance ativo' : 'Compliance incompleto',
        description: animal.sisbovAtivo
          ? `SISBOV ativo${animal.codigoSisbov ? ` com codigo ${animal.codigoSisbov}` : ''}.`
          : 'Ative o SISBOV e preencha RFID/codigo para fortalecer a rastreabilidade.',
      },
      {
        title: vacinacoes.length > 0 ? 'Sanidade acompanhada' : 'Sem vacinacao registrada',
        description: vacinacoes.length > 0
          ? `${vacinacoes.length} registro(s) de vacinacao disponiveis na ficha.`
          : 'Registre a primeira vacinacao para dar contexto sanitario ao animal.',
      },
      {
        title: eventosReprodutivos.length > 0 ? 'Historico reprodutivo ativo' : 'Sem eventos reprodutivos',
        description: eventosReprodutivos.length > 0
          ? `${eventosReprodutivos.length} registro(s) reprodutivos consolidados na ficha.`
          : 'Use eventos de cobertura, inseminacao, diagnostico e parto para montar a linha do tempo da matriz.',
      },
      {
        title: pesagensOrdenadas.length > 1 ? 'Base produtiva disponivel' : 'Pouca base produtiva',
        description: pesagensOrdenadas.length > 1
          ? `Curva de peso com ${pesagensOrdenadas.length} pontos e GMD estimado em ${ganhoPesoMedio} kg/dia.`
          : 'Registre mais pesagens para liberar analises de ganho e desempenho.',
      },
    ];
  }, [animal, vacinacoes.length, eventosReprodutivos.length, pesagensOrdenadas.length, ganhoPesoMedio]);

  const resumoReprodutivo = useMemo(() => {
    const ultimaCobertura = eventosReprodutivos.find((evento) => evento.tipo === 'COBERTURA' || evento.tipo === 'INSEMINACAO');
    const ultimoDiagnostico = eventosReprodutivos.find((evento) => evento.tipo === 'DIAGNOSTICO_GESTACAO');
    const ultimoParto = eventosReprodutivos.find((evento) => evento.tipo === 'PARTO');
    return {
      ultimaCobertura,
      ultimoDiagnostico,
      ultimoParto,
    };
  }, [eventosReprodutivos]);

  async function submitVaccination(e: React.FormEvent) {
    e.preventDefault();
    if (!animal) return;

    try {
      setSavingVaccination(true);
      await api.post(`/api/animais/${animal.id}/vacinacoes`, {
        ...vaccinationForm,
        dose: vaccinationForm.dose || undefined,
        proximaDoseEm: vaccinationForm.proximaDoseEm || undefined,
        fabricante: vaccinationForm.fabricante || undefined,
        loteVacina: vaccinationForm.loteVacina || undefined,
        observacoes: vaccinationForm.observacoes || undefined,
      });
      setVaccinationForm({
        tipo: 'AFTOSA',
        nomeVacina: '',
        aplicadaEm: new Date().toISOString().split('T')[0],
        dose: undefined,
        unidadeMedida: 'ml',
        proximaDoseEm: '',
        fabricante: '',
        loteVacina: '',
        observacoes: '',
      });
      setShowVaccinationModal(false);
      await loadAnimalData(animal.id);
    } catch (submitError: any) {
      alert(submitError.response?.data?.message || 'Erro ao registrar vacinacao.');
    } finally {
      setSavingVaccination(false);
    }
  }

  async function submitMovement(e: React.FormEvent) {
    e.preventDefault();
    if (!animal) return;

    try {
      setSavingMovement(true);
      await api.post(`/api/animais/${animal.id}/movimentacoes`, {
        ...movementForm,
        destinoFarmId: movementForm.destinoFarmId || undefined,
        destinoPastureId: movementForm.destinoPastureId || undefined,
        numeroGta: movementForm.numeroGta || undefined,
        documentoExterno: movementForm.documentoExterno || undefined,
        motivo: movementForm.motivo || undefined,
        observacoes: movementForm.observacoes || undefined,
      });
      setMovementForm({
        tipo: 'ENTRE_PASTOS',
        movimentadaEm: new Date().toISOString().split('T')[0],
        destinoFarmId: '',
        destinoPastureId: '',
        numeroGta: '',
        documentoExterno: '',
        motivo: '',
        observacoes: '',
      });
      setShowMovementModal(false);
      await loadAnimalData(animal.id);
    } catch (submitError: any) {
      alert(submitError.response?.data?.message || 'Erro ao registrar movimentacao.');
    } finally {
      setSavingMovement(false);
    }
  }

  async function submitInlineEdit(e: React.FormEvent) {
    e.preventDefault();
    if (!animal) return;

    try {
      setSavingInline(true);
      const payload: AtualizarAnimalRequest = {
        rfid: inlineForm.rfid || undefined,
        codigoSisbov: inlineForm.codigoSisbov || undefined,
        nome: inlineForm.nome || undefined,
        raca: inlineForm.raca,
        pesoAtual: inlineForm.pesoAtual,
        categoria: inlineForm.categoria,
        origem: inlineForm.origem,
        loteId: inlineForm.loteId || undefined,
        pastureId: inlineForm.pastureId || undefined,
        status: inlineForm.status,
        dataEntrada: inlineForm.dataEntrada || undefined,
        sisbovAtivo: inlineForm.sisbovAtivo,
        observacoes: inlineForm.observacoes || undefined,
      };

      await api.put(`/api/animais/${animal.id}`, payload);
      setEditingInline(false);
      await loadAnimalData(animal.id);
    } catch (submitError: any) {
      alert(submitError.response?.data?.message || 'Erro ao atualizar animal.');
    } finally {
      setSavingInline(false);
    }
  }

  async function exportarPdfAnimal() {
    if (!animal) return;

    try {
      setExportingPdf(true);
      const response = await api.get(`/api/relatorios/exportar/animal/${animal.id}.pdf`, {
        responseType: 'blob',
      });

      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `relatorio-animal-${animal.brinco}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (requestError: any) {
      alert(requestError.response?.data?.message || 'Nao foi possivel exportar o PDF do animal.');
    } finally {
      setExportingPdf(false);
    }
  }

  function getStatusColor(status: StatusAnimal) {
    const colors: Record<StatusAnimal, string> = {
      ATIVO: 'bg-emerald-100 text-emerald-800',
      VENDIDO: 'bg-blue-100 text-blue-800',
      MORTO: 'bg-red-100 text-red-800',
      DESCARTADO: 'bg-gray-200 text-gray-800',
      TRANSFERIDO: 'bg-amber-100 text-amber-800',
    };
    return colors[status];
  }

  function timelineAccent(category: TimelineItem['category']) {
    const colors = {
      evento: 'border-l-emerald-400 bg-emerald-50/60',
      vacinacao: 'border-l-blue-400 bg-blue-50/60',
      movimentacao: 'border-l-amber-400 bg-amber-50/60',
    };
    return colors[category];
  }

  const racasOptions: Raca[] = [
    'NELORE',
    'ANGUS',
    'BRAHMAN',
    'CARACU',
    'GUZERA',
    'TABAPUA',
    'SENEPOL',
    'CHAROLÊS',
    'LIMOUSIN',
    'HEREFORD',
    'SIMENTAL',
    'CRUZAMENTO_INDUSTRIAL',
    'MESTICO',
    'OUTRAS',
  ];
  const categoriaOptions: CategoriaAnimal[] = ['BEZERRO', 'NOVILHO', 'NOVILHA', 'BOI', 'VACA', 'TOURO', 'MATRIZ'];
  const origemOptions: OrigemAnimal[] = ['NASCIMENTO', 'COMPRA'];
  const statusOptions: StatusAnimal[] = ['ATIVO', 'VENDIDO', 'MORTO', 'DESCARTADO', 'TRANSFERIDO'];
  const vaccineOptions: TipoVacina[] = ['AFTOSA', 'BRUCELOSE', 'CLOSTRIDIOSE', 'RAIVA', 'LEPTOSE', 'IBR_BVD', 'OUTRA'];
  const movementOptions: TipoMovimentacaoAnimal[] = ['ENTRE_PASTOS', 'ENTRE_FAZENDAS', 'SAIDA_EXTERNA', 'ENTRADA_EXTERNA'];
  const nextDose = vacinacoes.find((item) => item.proximaDoseEm)?.proximaDoseEm;

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-emerald-600 border-t-transparent" />
          <p className="mt-4 text-gray-600">Carregando ficha do animal...</p>
        </div>
      </div>
    );
  }

  if (error || !animal) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">
        <p className="font-semibold">Nao foi possivel abrir esta ficha.</p>
        <p className="mt-2 text-sm">{error || 'Animal nao encontrado.'}</p>
        <button onClick={() => navigate('/app/animais')} className="btn-secondary mt-4">
          Voltar para animais
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-start gap-3">
          <button onClick={() => navigate('/app/animais')} className="btn-secondary !px-3 !py-2">
            <ArrowLeft className="h-4 w-4" />
          </button>
          <div>
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-3xl font-bold text-gray-900">{animal.nome || `Animal ${animal.brinco}`}</h1>
              <span className={`rounded-full px-3 py-1 text-xs font-semibold ${getStatusColor(animal.status)}`}>{animal.status}</span>
              {animal.sisbovAtivo && (
                <span className="rounded-full bg-blue-100 px-3 py-1 text-xs font-semibold text-blue-800">SISBOV ativo</span>
              )}
            </div>
            <p className="mt-2 text-sm text-gray-600">
              Brinco {animal.brinco} • {animal.raca} • {animal.sexo} • {animal.idade} meses
            </p>
          </div>
        </div>

        <div className="flex flex-wrap gap-3">
          <button onClick={() => setEditingInline((current) => !current)} className="btn-secondary">
            <Edit3 className="h-4 w-4" />
            {editingInline ? 'Fechar edicao' : 'Editar inline'}
          </button>
          <button onClick={() => setShowVaccinationModal(true)} className="btn-secondary">
            <ShieldPlus className="h-4 w-4" />
            Registrar vacinacao
          </button>
          <button onClick={() => setShowMovementModal(true)} className="btn-primary">
            <Plus className="h-4 w-4" />
            Registrar movimentacao
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-5">
        <StatCard
          icon={Calendar}
          title="Nascimento"
          value={new Date(animal.dataNascimento).toLocaleDateString('pt-BR')}
          subtitle={animal.dataEntrada ? `Entrada em ${new Date(animal.dataEntrada).toLocaleDateString('pt-BR')}` : 'Sem data de entrada informada'}
          color="bg-emerald-500"
        />
        <StatCard
          icon={Weight}
          title="Peso atual"
          value={animal.pesoAtual ? `${animal.pesoAtual} kg` : 'Nao informado'}
          subtitle={pesagensOrdenadas.length > 0 ? `${pesagensOrdenadas.length} pesagens registradas` : 'Sem historico ainda'}
          color="bg-blue-500"
        />
        <StatCard
          icon={TrendingUp}
          title="GMD estimado"
          value={ganhoPesoMedio ? `${ganhoPesoMedio} kg/dia` : 'Aguardando base'}
          subtitle="Calculado a partir da curva de peso"
          color="bg-amber-500"
        />
        <StatCard
          icon={MapPinned}
          title="Pasto atual"
          value={animal.pasture?.nome || 'Sem vinculo'}
          subtitle={animal.lote ? `Lote ${animal.lote.nome}` : 'Sem lote associado'}
          color="bg-teal-500"
        />
        <StatCard
          icon={Tag}
          title="Compliance"
          value={animal.rfid || animal.codigoSisbov || 'Pendente'}
          subtitle={animal.codigoSisbov ? `SISBOV ${animal.codigoSisbov}` : 'RFID/SISBOV nao preenchidos'}
          color="bg-slate-600"
        />
      </div>

      <section className="rounded-3xl border border-emerald-100 bg-gradient-to-r from-emerald-50 via-white to-blue-50 p-6">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="h-5 w-5 text-emerald-600" />
            <h2 className="text-lg font-bold text-gray-900">Mini relatorio rapido</h2>
          </div>
          <button onClick={() => void exportarPdfAnimal()} disabled={exportingPdf} className="btn-primary inline-flex items-center gap-2">
            <Download className="h-4 w-4" />
            {exportingPdf ? 'Exportando PDF...' : 'Exportar PDF do animal'}
          </button>
        </div>
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2 xl:grid-cols-4">
          {quickInsights.map((insight) => (
            <QuickInsight key={insight.title} title={insight.title} description={insight.description} />
          ))}
        </div>
      </section>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="space-y-6">
          <section className="card">
            <div className="mb-5 flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-gray-900">Ficha do animal</h2>
                <p className="text-sm text-gray-600">Visualizacao operacional e edicao inline dos dados principais.</p>
              </div>
              {editingInline && <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-800">Modo edicao</span>}
            </div>

            {!editingInline ? (
              <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                <InfoRow label="Brinco" value={animal.brinco} />
                <InfoRow label="Nome" value={animal.nome || '-'} />
                <InfoRow label="RFID" value={animal.rfid || '-'} />
                <InfoRow label="Codigo SISBOV" value={animal.codigoSisbov || '-'} />
                <InfoRow label="Raca" value={animal.raca} />
                <InfoRow label="Categoria" value={animal.categoria} />
                <InfoRow label="Origem" value={animal.origem === 'NASCIMENTO' ? 'Nascimento' : 'Compra'} />
                <InfoRow label="Status" value={animal.status} />
                <InfoRow label="Lote atual" value={animal.lote?.nome || '-'} />
                <InfoRow label="Pasto atual" value={animal.pasture?.nome || '-'} />
                <InfoRow label="Data de entrada" value={animal.dataEntrada ? new Date(animal.dataEntrada).toLocaleDateString('pt-BR') : '-'} />
                <InfoRow label="Observacoes" value={animal.observacoes || '-'} />
              </div>
            ) : (
              <form onSubmit={submitInlineEdit} className="space-y-4">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div>
                    <label className="form-label">Nome</label>
                    <input className="form-input" value={inlineForm.nome || ''} onChange={(e) => setInlineForm((current) => ({ ...current, nome: e.target.value }))} />
                  </div>
                  <div>
                    <label className="form-label">RFID</label>
                    <input className="form-input" value={inlineForm.rfid || ''} onChange={(e) => setInlineForm((current) => ({ ...current, rfid: e.target.value }))} />
                  </div>
                  <div>
                    <label className="form-label">Codigo SISBOV</label>
                    <input className="form-input" value={inlineForm.codigoSisbov || ''} onChange={(e) => setInlineForm((current) => ({ ...current, codigoSisbov: e.target.value }))} />
                  </div>
                  <div>
                    <label className="form-label">Peso atual (kg)</label>
                    <input type="number" step="0.1" className="form-input" value={inlineForm.pesoAtual ?? ''} onChange={(e) => setInlineForm((current) => ({ ...current, pesoAtual: e.target.value ? Number(e.target.value) : undefined }))} />
                  </div>
                  <div>
                    <label className="form-label">Raca</label>
                    <select className="form-input" value={inlineForm.raca || animal.raca} onChange={(e) => setInlineForm((current) => ({ ...current, raca: e.target.value as Raca }))}>
                      {racasOptions.map((raca) => (
                        <option key={raca} value={raca}>
                          {raca}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Categoria</label>
                    <select className="form-input" value={inlineForm.categoria || animal.categoria} onChange={(e) => setInlineForm((current) => ({ ...current, categoria: e.target.value as CategoriaAnimal }))}>
                      {categoriaOptions.map((categoria) => (
                        <option key={categoria} value={categoria}>
                          {categoria}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Origem</label>
                    <select className="form-input" value={inlineForm.origem || animal.origem} onChange={(e) => setInlineForm((current) => ({ ...current, origem: e.target.value as OrigemAnimal }))}>
                      {origemOptions.map((origem) => (
                        <option key={origem} value={origem}>
                          {origem === 'NASCIMENTO' ? 'Nascimento' : 'Compra'}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Status</label>
                    <select className="form-input" value={inlineForm.status || animal.status} onChange={(e) => setInlineForm((current) => ({ ...current, status: e.target.value as StatusAnimal }))}>
                      {statusOptions.map((status) => (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Data de entrada</label>
                    <input type="date" className="form-input" value={inlineForm.dataEntrada || ''} onChange={(e) => setInlineForm((current) => ({ ...current, dataEntrada: e.target.value }))} />
                  </div>
                  <div>
                    <label className="form-label">Lote atual</label>
                    <select className="form-input" value={inlineForm.loteId || ''} onChange={(e) => setInlineForm((current) => ({ ...current, loteId: e.target.value || undefined }))}>
                      <option value="">Sem lote</option>
                      {lotes.map((lote) => (
                        <option key={lote.id} value={lote.id}>
                          {lote.nome}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Pasto atual</label>
                    <select className="form-input" value={inlineForm.pastureId || ''} onChange={(e) => setInlineForm((current) => ({ ...current, pastureId: e.target.value || undefined }))}>
                      <option value="">Sem pasto</option>
                      {pastures.map((pasto) => (
                        <option key={pasto.id} value={pasto.id}>
                          {pasto.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                <label className="flex items-center gap-2 text-sm text-gray-700">
                  <input type="checkbox" checked={Boolean(inlineForm.sisbovAtivo)} onChange={(e) => setInlineForm((current) => ({ ...current, sisbovAtivo: e.target.checked }))} />
                  SISBOV ativo
                </label>
                <div>
                  <label className="form-label">Observacoes</label>
                  <textarea rows={4} className="form-input" value={inlineForm.observacoes || ''} onChange={(e) => setInlineForm((current) => ({ ...current, observacoes: e.target.value }))} />
                </div>
                <div className="flex justify-end gap-3">
                  <button type="button" onClick={() => setEditingInline(false)} className="btn-secondary">
                    Cancelar
                  </button>
                  <button type="submit" className="btn-primary" disabled={savingInline}>
                    {savingInline ? 'Salvando...' : 'Salvar alteracoes'}
                  </button>
                </div>
              </form>
            )}
          </section>

          <section className="card">
            <div className="mb-5 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
              <div>
                <h2 className="text-xl font-bold text-gray-900">Timeline unica do animal</h2>
                <p className="text-sm text-gray-600">Eventos, vacinacoes e movimentacoes no mesmo contexto.</p>
              </div>
              <div className="flex flex-wrap gap-2">
                {[
                  { key: 'all', label: 'Tudo' },
                  { key: 'evento', label: 'Eventos' },
                  { key: 'vacinacao', label: 'Vacinacao' },
                  { key: 'movimentacao', label: 'Movimentacao' },
                ].map((item) => (
                  <button
                    key={item.key}
                    onClick={() => setTimelineFilter(item.key as TimelineCategory)}
                    className={`rounded-full px-4 py-2 text-sm font-medium ${timelineFilter === item.key ? 'bg-emerald-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
                  >
                    <Filter className="mr-2 inline h-4 w-4" />
                    {item.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-4">
              {filteredTimeline.length === 0 ? (
                <div className="rounded-2xl border border-dashed border-gray-300 p-6 text-center text-sm text-gray-500">
                  Nenhum registro encontrado para este filtro.
                </div>
              ) : (
                filteredTimeline.map((item) => (
                  <div key={item.id} className={`rounded-2xl border-l-4 p-4 ${timelineAccent(item.category)}`}>
                    <div className="flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
                      <div>
                        <p className="text-sm font-semibold uppercase tracking-wide text-gray-500">{item.category}</p>
                        <h3 className="text-lg font-bold text-gray-900">{item.title}</h3>
                        {item.subtitle && <p className="text-sm text-gray-600">{item.subtitle}</p>}
                        {item.description && <p className="mt-1 text-sm text-gray-600">{item.description}</p>}
                      </div>
                      <span className="rounded-full bg-white/80 px-3 py-1 text-xs font-medium text-gray-700">
                        {new Date(item.date).toLocaleDateString('pt-BR')}
                      </span>
                    </div>
                    {item.meta.length > 0 && (
                      <div className="mt-3 flex flex-wrap gap-2">
                        {item.meta.map((meta) => (
                          <span key={meta} className="rounded-full bg-white/90 px-3 py-1 text-xs text-gray-700">
                            {meta}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </section>
        </div>

        <div className="space-y-6">
          <section className="card">
            <div className="mb-4 flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-emerald-600" />
              <h2 className="text-xl font-bold text-gray-900">Curva de peso</h2>
            </div>
            <WeightCurve pesagens={pesagensOrdenadas} />
            {pesagens.length > 0 && (
              <div className="mt-6 overflow-hidden rounded-2xl border border-gray-100">
                <table className="min-w-full divide-y divide-gray-100 text-sm">
                  <thead className="bg-gray-50 text-left text-gray-600">
                    <tr>
                      <th className="px-4 py-3 font-medium">Data</th>
                      <th className="px-4 py-3 font-medium">Peso</th>
                      <th className="px-4 py-3 font-medium">Variacao</th>
                      <th className="px-4 py-3 font-medium">Dias</th>
                      <th className="px-4 py-3 font-medium">GMD</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 bg-white">
                    {pesagens.map((pesagem) => (
                      <tr key={pesagem.id}>
                        <td className="px-4 py-3 text-gray-700">{new Date(pesagem.data).toLocaleDateString('pt-BR')}</td>
                        <td className="px-4 py-3 font-medium text-gray-900">{pesagem.peso} kg</td>
                        <td className="px-4 py-3 text-gray-700">
                          {pesagem.variacaoPeso == null ? '-' : `${Number(pesagem.variacaoPeso).toFixed(2)} kg`}
                        </td>
                        <td className="px-4 py-3 text-gray-700">{pesagem.diasDesdeAnterior ?? '-'}</td>
                        <td className="px-4 py-3 text-gray-700">
                          {pesagem.ganhoMedioDiario == null ? '-' : `${Number(pesagem.ganhoMedioDiario).toFixed(3)} kg/dia`}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="card">
            <div className="mb-4 flex items-center gap-2">
              <ShieldPlus className="h-5 w-5 text-blue-600" />
              <h2 className="text-xl font-bold text-gray-900">Resumo sanitario</h2>
            </div>
            <div className="space-y-3">
              <InfoRow label="Vacinas registradas" value={String(vacinacoes.length)} />
              <InfoRow label="Ultima aplicacao" value={vacinacoes[0] ? new Date(vacinacoes[0].aplicadaEm).toLocaleDateString('pt-BR') : '-'} />
              <InfoRow label="Proxima dose" value={nextDose ? new Date(nextDose).toLocaleDateString('pt-BR') : '-'} />
            </div>
          </section>

          <section className="card">
            <div className="mb-4 flex items-center gap-2">
              <Calendar className="h-5 w-5 text-fuchsia-600" />
              <h2 className="text-xl font-bold text-gray-900">Resumo reprodutivo</h2>
            </div>
            <div className="space-y-3">
              <InfoRow
                label="Ultima cobertura / IATF"
                value={
                  resumoReprodutivo.ultimaCobertura
                    ? `${resumoReprodutivo.ultimaCobertura.tipo.replace(/_/g, ' ')} em ${new Date(resumoReprodutivo.ultimaCobertura.data).toLocaleDateString('pt-BR')}`
                    : '-'
                }
              />
              <InfoRow
                label="Ultimo diagnostico"
                value={
                  resumoReprodutivo.ultimoDiagnostico
                    ? `${resumoReprodutivo.ultimoDiagnostico.diagnosticoPositivo === true ? 'Positivo' : resumoReprodutivo.ultimoDiagnostico.diagnosticoPositivo === false ? 'Negativo' : 'Registrado'} em ${new Date(resumoReprodutivo.ultimoDiagnostico.data).toLocaleDateString('pt-BR')}`
                    : '-'
                }
              />
              <InfoRow
                label="Data prevista de parto"
                value={
                  resumoReprodutivo.ultimoDiagnostico?.dataPrevistaParto
                    ? new Date(resumoReprodutivo.ultimoDiagnostico.dataPrevistaParto).toLocaleDateString('pt-BR')
                    : resumoReprodutivo.ultimaCobertura?.dataPrevistaParto
                      ? new Date(resumoReprodutivo.ultimaCobertura.dataPrevistaParto).toLocaleDateString('pt-BR')
                      : '-'
                }
              />
              <InfoRow
                label="Ultimo parto"
                value={
                  resumoReprodutivo.ultimoParto
                    ? new Date(resumoReprodutivo.ultimoParto.data).toLocaleDateString('pt-BR')
                    : '-'
                }
              />
            </div>
          </section>

          <section className="card">
            <div className="mb-4 flex items-center gap-2">
              <GitBranch className="h-5 w-5 text-amber-600" />
              <h2 className="text-xl font-bold text-gray-900">Resumo de movimentacoes</h2>
            </div>
            <div className="space-y-3">
              <InfoRow label="Movimentacoes" value={String(movimentacoes.length)} />
              <InfoRow label="Ultima movimentacao" value={movimentacoes[0] ? new Date(movimentacoes[0].movimentadaEm).toLocaleDateString('pt-BR') : '-'} />
              <InfoRow label="Fazenda ativa" value={currentFarm?.nome || 'Nao definida'} />
            </div>
          </section>
        </div>
      </div>

      {showVaccinationModal && (
        <Modal title="Registrar vacinacao" onClose={() => setShowVaccinationModal(false)}>
          <form onSubmit={submitVaccination} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <label className="form-label">Tipo</label>
                <select className="form-input" value={vaccinationForm.tipo} onChange={(e) => setVaccinationForm((current) => ({ ...current, tipo: e.target.value as TipoVacina }))}>
                  {vaccineOptions.map((tipo) => (
                    <option key={tipo} value={tipo}>
                      {tipo}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="form-label">Data da aplicacao</label>
                <input type="date" className="form-input" value={vaccinationForm.aplicadaEm} onChange={(e) => setVaccinationForm((current) => ({ ...current, aplicadaEm: e.target.value }))} />
              </div>
              <div className="md:col-span-2">
                <label className="form-label">Nome da vacina</label>
                <input className="form-input" required value={vaccinationForm.nomeVacina} onChange={(e) => setVaccinationForm((current) => ({ ...current, nomeVacina: e.target.value }))} />
              </div>
              <div>
                <label className="form-label">Dose</label>
                <input type="number" step="0.1" className="form-input" value={vaccinationForm.dose ?? ''} onChange={(e) => setVaccinationForm((current) => ({ ...current, dose: e.target.value ? Number(e.target.value) : undefined }))} />
              </div>
              <div>
                <label className="form-label">Unidade</label>
                <input className="form-input" value={vaccinationForm.unidadeMedida || ''} onChange={(e) => setVaccinationForm((current) => ({ ...current, unidadeMedida: e.target.value }))} />
              </div>
              <div>
                <label className="form-label">Proxima dose</label>
                <input type="date" className="form-input" value={vaccinationForm.proximaDoseEm || ''} onChange={(e) => setVaccinationForm((current) => ({ ...current, proximaDoseEm: e.target.value }))} />
              </div>
              <div>
                <label className="form-label">Lote da vacina</label>
                <input className="form-input" value={vaccinationForm.loteVacina || ''} onChange={(e) => setVaccinationForm((current) => ({ ...current, loteVacina: e.target.value }))} />
              </div>
              <div className="md:col-span-2">
                <label className="form-label">Observacoes</label>
                <textarea rows={3} className="form-input" value={vaccinationForm.observacoes || ''} onChange={(e) => setVaccinationForm((current) => ({ ...current, observacoes: e.target.value }))} />
              </div>
            </div>
            <div className="flex justify-end gap-3">
              <button type="button" className="btn-secondary" onClick={() => setShowVaccinationModal(false)}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary" disabled={savingVaccination}>
                {savingVaccination ? 'Salvando...' : 'Registrar vacinacao'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {showMovementModal && (
        <Modal title="Registrar movimentacao" onClose={() => setShowMovementModal(false)}>
          <form onSubmit={submitMovement} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <label className="form-label">Tipo</label>
                <select className="form-input" value={movementForm.tipo} onChange={(e) => setMovementForm((current) => ({ ...current, tipo: e.target.value as TipoMovimentacaoAnimal }))}>
                  {movementOptions.map((tipo) => (
                    <option key={tipo} value={tipo}>
                      {tipo}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="form-label">Data</label>
                <input type="date" className="form-input" value={movementForm.movimentadaEm} onChange={(e) => setMovementForm((current) => ({ ...current, movimentadaEm: e.target.value }))} />
              </div>
              <div>
                <label className="form-label">Fazenda destino</label>
                <select className="form-input" value={movementForm.destinoFarmId || ''} onChange={(e) => setMovementForm((current) => ({ ...current, destinoFarmId: e.target.value || undefined }))}>
                  <option value="">Nao alterar</option>
                  {(farms as FarmSummary[]).map((farm) => (
                    <option key={farm.id} value={farm.id}>
                      {farm.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="form-label">Pasto destino</label>
                <select className="form-input" value={movementForm.destinoPastureId || ''} onChange={(e) => setMovementForm((current) => ({ ...current, destinoPastureId: e.target.value || undefined }))}>
                  <option value="">Nao alterar</option>
                  {pastures.map((pasto) => (
                    <option key={pasto.id} value={pasto.id}>
                      {pasto.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="form-label">Numero GTA</label>
                <input className="form-input" value={movementForm.numeroGta || ''} onChange={(e) => setMovementForm((current) => ({ ...current, numeroGta: e.target.value }))} />
              </div>
              <div>
                <label className="form-label">Documento externo</label>
                <input className="form-input" value={movementForm.documentoExterno || ''} onChange={(e) => setMovementForm((current) => ({ ...current, documentoExterno: e.target.value }))} />
              </div>
              <div className="md:col-span-2">
                <label className="form-label">Motivo</label>
                <input className="form-input" value={movementForm.motivo || ''} onChange={(e) => setMovementForm((current) => ({ ...current, motivo: e.target.value }))} />
              </div>
              <div className="md:col-span-2">
                <label className="form-label">Observacoes</label>
                <textarea rows={3} className="form-input" value={movementForm.observacoes || ''} onChange={(e) => setMovementForm((current) => ({ ...current, observacoes: e.target.value }))} />
              </div>
            </div>
            <div className="flex justify-end gap-3">
              <button type="button" className="btn-secondary" onClick={() => setShowMovementModal(false)}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary" disabled={savingMovement}>
                {savingMovement ? 'Salvando...' : 'Registrar movimentacao'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
