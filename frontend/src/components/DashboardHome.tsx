import { useEffect, useState } from 'react';
import {
  TrendingUp,
  Beef,
  AlertTriangle,
  Calendar,
  DollarSign,
  Activity,
  ArrowUp,
  ArrowDown,
  TrendingDown
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import type { AnimalDto, EventoDto, StatusAnimal, CategoriaAnimal } from '../types/index';

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
  const user = JSON.parse(localStorage.getItem('user') || '{}');
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
      MATRIZ: 0
    }
  });
  const [animais, setAnimais] = useState<AnimalDto[]>([]);
  const [eventos, setEventos] = useState<EventoDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const [animaisRes, eventosRes] = await Promise.all([
        api.get<AnimalDto[]>('/api/animais'),
        api.get<EventoDto[]>('/api/eventos')
      ]);

      const animaisData = animaisRes.data;
      const eventosData = eventosRes.data;

      // Calcular estatísticas
      const hoje = new Date();
      hoje.setHours(0, 0, 0, 0);
      const seteDiasAtras = new Date(hoje);
      seteDiasAtras.setDate(seteDiasAtras.getDate() - 7);

      const eventosHoje = eventosData.filter((e) => {
        const eventDate = new Date(e.data);
        eventDate.setHours(0, 0, 0, 0);
        return eventDate.getTime() === hoje.getTime();
      }).length;

      const eventosUltimos7Dias = eventosData.filter((e) => {
        const eventDate = new Date(e.data);
        return eventDate >= seteDiasAtras;
      }).length;

      const animaisAtivos = animaisData.filter((a) => a.status === 'ATIVO');
      const pesoMedio =
        animaisAtivos.reduce((sum, a) => sum + (a.pesoAtual || 0), 0) / animaisAtivos.length || 0;

      const categorias: Record<CategoriaAnimal, number> = {
        BEZERRO: 0,
        NOVILHO: 0,
        NOVILHA: 0,
        BOI: 0,
        VACA: 0,
        TOURO: 0,
        MATRIZ: 0
      };

      animaisAtivos.forEach((animal) => {
        categorias[animal.categoria]++;
      });

      setStats({
        totalAnimais: animaisData.length,
        animaisAtivos: animaisAtivos.length,
        animaisVendidos: animaisData.filter((a) => a.status === 'VENDIDO').length,
        eventosHoje,
        eventosUltimos7Dias,
        pesoMedio: Math.round(pesoMedio),
        categorias
      });

      setAnimais(animaisData);
      setEventos(eventosData.slice(0, 5));
    } catch (error) {
      console.error('Erro ao carregar dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const cards = [
    {
      title: 'Total de Animais',
      value: stats.totalAnimais,
      subtitle: `${stats.animaisAtivos} ativos`,
      icon: Beef,
      color: 'bg-blue-500',
      trend: `${stats.animaisVendidos} vendidos`,
      trendUp: true
    },
    {
      title: 'Eventos (7 dias)',
      value: stats.eventosUltimos7Dias,
      subtitle: `${stats.eventosHoje} hoje`,
      icon: Activity,
      color: 'bg-green-500',
      trend: 'Última semana',
      trendUp: true
    },
    {
      title: 'Peso Médio',
      value: stats.pesoMedio > 0 ? `${stats.pesoMedio} kg` : 'N/A',
      subtitle: 'Rebanho ativo',
      icon: TrendingUp,
      color: 'bg-purple-500',
      trend: 'Média geral',
      trendUp: true
    },
    {
      title: 'Alertas Críticos',
      value: 0,
      subtitle: 'Atenção necessária',
      icon: AlertTriangle,
      color: 'bg-red-500',
      trend: 'Nenhum alerta',
      trendUp: false
    }
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="card">
        <div className="flex items-center gap-3 mb-2">
          <Activity className="w-8 h-8 text-primary-600" />
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Bem-vindo, {user.nome}! 👋</h2>
            <p className="text-gray-600">Aqui está um resumo da sua fazenda hoje</p>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {cards.map((card, index) => {
          const Icon = card.icon;
          return (
            <div key={index} className="card hover:shadow-lg transition-shadow">
              <div className="flex items-start justify-between mb-4">
                <div className={`${card.color} p-3 rounded-lg`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
              </div>
              <div>
                <p className="text-sm text-gray-600 mb-1">{card.title}</p>
                <p className="text-2xl font-bold text-gray-900 mb-1">{card.value}</p>
                <p className="text-xs text-gray-500">{card.subtitle}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Distribuição por Categoria */}
      <div className="card">
        <h3 className="text-lg font-bold text-gray-900 mb-4">Distribuição por Categoria</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-4">
          {Object.entries(stats.categorias).map(([categoria, count]) => (
            <div key={categoria} className="text-center p-4 bg-gray-50 rounded-lg">
              <p className="text-2xl font-bold text-primary-600">{count}</p>
              <p className="text-xs text-gray-600 mt-1">{categoria}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Info Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Últimos Eventos */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-gray-900">Últimos Eventos</h3>
            <button
              onClick={() => navigate('/eventos')}
              className="text-sm text-primary-600 hover:text-primary-700 font-medium"
            >
              Ver todos
            </button>
          </div>
          <div className="space-y-3">
            {eventos.length === 0 ? (
              <div className="text-center py-8">
                <Activity className="w-12 h-12 text-gray-300 mx-auto mb-2" />
                <p className="text-sm text-gray-600">Nenhum evento registrado</p>
              </div>
            ) : (
              eventos.map((evento) => (
                <div
                  key={evento.id}
                  className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                  onClick={() => navigate('/eventos')}
                >
                  <div>
                    <p className="font-medium text-gray-900">{evento.tipo.replace(/_/g, ' ')}</p>
                    <p className="text-sm text-gray-600">
                      {evento.animal.brinco}
                      {evento.animal.nome && ` - ${evento.animal.nome}`}
                    </p>
                  </div>
                  <p className="text-xs text-gray-500">
                    {new Date(evento.data).toLocaleDateString('pt-BR')}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Animais Recentes */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-gray-900">Animais Recentes</h3>
            <button
              onClick={() => navigate('/animais')}
              className="text-sm text-primary-600 hover:text-primary-700 font-medium"
            >
              Ver todos
            </button>
          </div>
          <div className="space-y-3">
            {animais.slice(0, 5).length === 0 ? (
              <div className="text-center py-8">
                <Beef className="w-12 h-12 text-gray-300 mx-auto mb-2" />
                <p className="text-sm text-gray-600">Nenhum animal cadastrado</p>
              </div>
            ) : (
              animais.slice(0, 5).map((animal) => (
                <div
                  key={animal.id}
                  className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
                  onClick={() => navigate(`/animais/${animal.id}`)}
                >
                  <div>
                    <p className="font-medium text-gray-900">
                      Brinco {animal.brinco}
                      {animal.nome && ` - ${animal.nome}`}
                    </p>
                    <p className="text-sm text-gray-600">
                      {animal.raca} • {animal.categoria}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900">
                      {animal.pesoAtual ? `${animal.pesoAtual} kg` : '-'}
                    </p>
                    <p className="text-xs text-gray-500">{animal.idade} meses</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Trial Info */}
      <div className="card bg-gradient-to-r from-primary-50 to-primary-100 border-primary-200">
        <div className="flex items-center justify-between flex-col sm:flex-row gap-4">
          <div className="flex items-center gap-3">
            <div className="bg-primary-600 p-3 rounded-lg">
              <TrendingUp className="w-6 h-6 text-white" />
            </div>
            <div>
              <p className="font-bold text-gray-900">Período Trial - 30 dias restantes</p>
              <p className="text-sm text-gray-600">Aproveite todos os recursos gratuitamente</p>
            </div>
          </div>
          <button className="btn-primary">Fazer Upgrade</button>
        </div>
      </div>
    </div>
  );
}
