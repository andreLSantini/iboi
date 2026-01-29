import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Beef,
  Calendar,
  Weight,
  Activity,
  Edit2,
  TrendingUp,
  AlertCircle
} from 'lucide-react';
import api from '../services/api';
import type { AnimalDto, EventoDto } from '../types/index';

export default function AnimalDetalhes() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [animal, setAnimal] = useState<AnimalDto | null>(null);
  const [eventos, setEventos] = useState<EventoDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      loadAnimalData(id);
    }
  }, [id]);

  const loadAnimalData = async (animalId: string) => {
    try {
      setLoading(true);
      const [animalRes, eventosRes] = await Promise.all([
        api.get<AnimalDto>(`/api/animais/${animalId}`),
        api.get<EventoDto[]>(`/api/eventos/animal/${animalId}`)
      ]);
      setAnimal(animalRes.data);
      setEventos(eventosRes.data);
    } catch (error) {
      console.error('Erro ao carregar dados:', error);
      alert('Erro ao carregar dados do animal');
      navigate('/animais');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      ATIVO: 'bg-green-100 text-green-800',
      VENDIDO: 'bg-blue-100 text-blue-800',
      MORTO: 'bg-red-100 text-red-800',
      DESCARTADO: 'bg-gray-100 text-gray-800',
      TRANSFERIDO: 'bg-yellow-100 text-yellow-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  const getTipoColor = (tipo: string) => {
    const colors: Record<string, string> = {
      VACINA: 'bg-blue-100 text-blue-800',
      VERMIFUGO: 'bg-purple-100 text-purple-800',
      PESAGEM: 'bg-green-100 text-green-800',
      TRATAMENTO: 'bg-red-100 text-red-800',
      VENDA: 'bg-yellow-100 text-yellow-800',
      COMPRA: 'bg-cyan-100 text-cyan-800'
    };
    return colors[tipo] || 'bg-gray-100 text-gray-800';
  };

  // Calcular estatísticas
  const pesagens = eventos.filter((e) => e.tipo === 'PESAGEM' && e.peso).sort((a, b) => new Date(a.data).getTime() - new Date(b.data).getTime());
  const ganhoPeso = pesagens.length >= 2
    ? ((pesagens[pesagens.length - 1].peso! - pesagens[0].peso!) / pesagens.length).toFixed(2)
    : 'N/A';

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!animal) {
    return (
      <div className="text-center py-12">
        <AlertCircle className="w-16 h-16 text-gray-300 mx-auto mb-4" />
        <p className="text-gray-600">Animal não encontrado</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/animais')}
            className="p-2 hover:bg-gray-100 rounded-lg"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <Beef className="w-8 h-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Brinco {animal.brinco} {animal.nome && `- ${animal.nome}`}
            </h1>
            <p className="text-gray-600">{animal.raca} • {animal.categoria}</p>
          </div>
        </div>
        <button
          onClick={() => navigate('/animais')}
          className="btn-secondary flex items-center gap-2"
        >
          <Edit2 className="w-4 h-4" />
          Editar
        </button>
      </div>

      {/* Informações Principais */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card">
          <div className="flex items-center gap-3 mb-2">
            <div className="bg-blue-100 p-2 rounded-lg">
              <Calendar className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Idade</p>
              <p className="text-xl font-bold text-gray-900">{animal.idade} meses</p>
            </div>
          </div>
          <p className="text-xs text-gray-500">
            Nascido em {new Date(animal.dataNascimento).toLocaleDateString('pt-BR')}
          </p>
        </div>

        <div className="card">
          <div className="flex items-center gap-3 mb-2">
            <div className="bg-green-100 p-2 rounded-lg">
              <Weight className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Peso Atual</p>
              <p className="text-xl font-bold text-gray-900">
                {animal.pesoAtual ? `${animal.pesoAtual} kg` : 'N/A'}
              </p>
            </div>
          </div>
          <p className="text-xs text-gray-500">Última atualização</p>
        </div>

        <div className="card">
          <div className="flex items-center gap-3 mb-2">
            <div className="bg-purple-100 p-2 rounded-lg">
              <TrendingUp className="w-5 h-5 text-purple-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Ganho Médio</p>
              <p className="text-xl font-bold text-gray-900">
                {typeof ganhoPeso === 'number' ? `${ganhoPeso} kg` : ganhoPeso}
              </p>
            </div>
          </div>
          <p className="text-xs text-gray-500">Por pesagem</p>
        </div>

        <div className="card">
          <div className="flex items-center gap-3 mb-2">
            <div className="bg-yellow-100 p-2 rounded-lg">
              <Activity className="w-5 h-5 text-yellow-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Status</p>
              <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(animal.status)}`}>
                {animal.status}
              </span>
            </div>
          </div>
          <p className="text-xs text-gray-500">Situação atual</p>
        </div>
      </div>

      {/* Detalhes */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Informações Gerais */}
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 mb-4">Informações Gerais</h2>
          <div className="space-y-3">
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Brinco:</span>
              <span className="font-medium text-gray-900">{animal.brinco}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Nome:</span>
              <span className="font-medium text-gray-900">{animal.nome || 'Não informado'}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Sexo:</span>
              <span className="font-medium text-gray-900">{animal.sexo}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Raça:</span>
              <span className="font-medium text-gray-900">{animal.raca}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Categoria:</span>
              <span className="font-medium text-gray-900">{animal.categoria}</span>
            </div>
            {animal.lote && (
              <div className="flex justify-between py-2 border-b border-gray-100">
                <span className="text-gray-600">Lote:</span>
                <span className="font-medium text-gray-900">{animal.lote.nome}</span>
              </div>
            )}
          </div>
        </div>

        {/* Genealogia */}
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 mb-4">Genealogia</h2>
          <div className="space-y-3">
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Pai:</span>
              <span className="font-medium text-gray-900">
                {animal.pai ? `${animal.pai.brinco} ${animal.pai.nome || ''}` : 'Não informado'}
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Mãe:</span>
              <span className="font-medium text-gray-900">
                {animal.mae ? `${animal.mae.brinco} ${animal.mae.nome || ''}` : 'Não informado'}
              </span>
            </div>
          </div>

          {animal.observacoes && (
            <div className="mt-4">
              <h3 className="text-sm font-medium text-gray-700 mb-2">Observações</h3>
              <p className="text-sm text-gray-600 bg-gray-50 p-3 rounded-lg">
                {animal.observacoes}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Gráfico de Pesagens */}
      {pesagens.length > 0 && (
        <div className="card">
          <h2 className="text-lg font-bold text-gray-900 mb-4">Evolução de Peso</h2>
          <div className="space-y-2">
            {pesagens.map((pesagem, index) => (
              <div key={index} className="flex items-center gap-4">
                <div className="text-sm text-gray-600 w-24">
                  {new Date(pesagem.data).toLocaleDateString('pt-BR')}
                </div>
                <div className="flex-1 bg-gray-200 rounded-full h-6 relative">
                  <div
                    className="bg-green-500 h-6 rounded-full flex items-center justify-end pr-3 text-white text-sm font-medium"
                    style={{
                      width: `${(pesagem.peso! / Math.max(...pesagens.map((p) => p.peso!))) * 100}%`
                    }}
                  >
                    {pesagem.peso} kg
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Histórico de Eventos */}
      <div className="card">
        <h2 className="text-lg font-bold text-gray-900 mb-4">
          Histórico de Eventos ({eventos.length})
        </h2>
        <div className="space-y-3">
          {eventos.length === 0 ? (
            <div className="text-center py-8">
              <Activity className="w-12 h-12 text-gray-300 mx-auto mb-2" />
              <p className="text-gray-600">Nenhum evento registrado</p>
            </div>
          ) : (
            eventos.map((evento) => (
              <div key={evento.id} className="flex items-start gap-4 p-4 bg-gray-50 rounded-lg">
                <div className={`p-2 rounded-lg ${getTipoColor(evento.tipo)}`}>
                  <Activity className="w-5 h-5" />
                </div>
                <div className="flex-1">
                  <div className="flex items-start justify-between mb-1">
                    <h3 className="font-medium text-gray-900">
                      {evento.tipo.replace(/_/g, ' ')}
                    </h3>
                    <span className="text-sm text-gray-500">
                      {new Date(evento.data).toLocaleDateString('pt-BR')}
                    </span>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">{evento.descricao}</p>
                  <div className="flex flex-wrap gap-3 text-xs text-gray-500">
                    {evento.peso && <span>Peso: {evento.peso} kg</span>}
                    {evento.produto && <span>Produto: {evento.produto}</span>}
                    {evento.dose && <span>Dose: {evento.dose} {evento.unidadeMedida}</span>}
                    {evento.valor && <span>Valor: R$ {evento.valor.toFixed(2)}</span>}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
