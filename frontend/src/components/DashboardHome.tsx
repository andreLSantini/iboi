import { useEffect, useState } from 'react';
import { Activity, AlertTriangle, Beef, TrendingUp } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { getUser } from '../services/session';
import type { AnimalDto, CategoriaAnimal, EventoDto } from '../types';

interface DashboardStats {
  totalAnimais: number;
  animaisAtivos: number;
  animaisVendidos: number;
  eventosHoje: number;
  eventosUltimos7Dias: number;
  pesoMedio: number;
  categorias: Record<CategoriaAnimal, number>;
}

export default function DashboardHome() {
  const navigate = useNavigate();
  const user = getUser();
  const [stats, setStats] = useState<DashboardStats>({
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
  });
  const [animais, setAnimais] = useState<AnimalDto[]>([]);
  const [eventos, setEventos] = useState<EventoDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void loadDashboardData();
  }, []);

  async function loadDashboardData() {
    try {
      const [animaisRes, eventosRes] = await Promise.all([api.get('/api/animais'), api.get('/api/eventos')]);
      const animaisData = Array.isArray(animaisRes.data) ? animaisRes.data : animaisRes.data.content || [];
      const eventosData = Array.isArray(eventosRes.data) ? eventosRes.data : eventosRes.data.content || [];

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
    } catch (error) {
      console.error('Erro ao carregar dashboard', error);
    } finally {
      setLoading(false);
    }
  }

  const cards = [
    { title: 'Total de animais', value: stats.totalAnimais, subtitle: `${stats.animaisAtivos} ativos`, icon: Beef, color: 'bg-blue-500' },
    { title: 'Eventos (7 dias)', value: stats.eventosUltimos7Dias, subtitle: `${stats.eventosHoje} hoje`, icon: Activity, color: 'bg-green-500' },
    { title: 'Peso medio', value: stats.pesoMedio > 0 ? `${stats.pesoMedio} kg` : 'N/A', subtitle: 'Rebanho ativo', icon: TrendingUp, color: 'bg-slate-900' },
    { title: 'Alertas criticos', value: 0, subtitle: 'Nenhum alerta critico', icon: AlertTriangle, color: 'bg-red-500' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="h-12 w-12 animate-spin rounded-full border-b-2 border-primary-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="card">
        <div className="flex items-center gap-3">
          <Activity className="h-8 w-8 text-primary-600" />
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Bem-vindo, {user?.nome ?? 'Admin'}.</h2>
            <p className="text-gray-600">Resumo operacional da fazenda e do trial.</p>
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

      <div className="card">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Trial e billing</h3>
            <p className="text-sm text-gray-600">Acompanhe status da assinatura e avance para o plano pago.</p>
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
                  <p className="text-sm text-gray-600">{animal.raca} • {animal.categoria}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
