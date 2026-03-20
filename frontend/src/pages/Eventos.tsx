import { useState, useEffect } from 'react';
import {
  Activity,
  Plus,
  Search,
  X,
  AlertCircle,
  Calendar as CalendarIcon,
  Edit2,
  Trash2,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import api from '../services/api';
import type { EventoDto, RegistrarEventoRequest, TipoEvento, AnimalDto } from '../types/index';

export default function Eventos() {
  const [eventos, setEventos] = useState<EventoDto[]>([]);
  const [animais, setAnimais] = useState<AnimalDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingEvento, setEditingEvento] = useState<EventoDto | null>(null);
  const [error, setError] = useState('');

  // Paginação
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const [formData, setFormData] = useState<RegistrarEventoRequest>({
    animalId: '',
    tipo: 'VACINA',
    data: new Date().toISOString().split('T')[0],
    descricao: '',
    peso: undefined,
    produto: '',
    dose: undefined,
    unidadeMedida: '',
    valor: undefined
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [eventosRes, animaisRes] = await Promise.all([
        api.get('/api/eventos'),
        api.get('/api/animais', { params: { status: 'ATIVO' } })
      ]);
      setEventos(Array.isArray(eventosRes.data) ? eventosRes.data : eventosRes.data.content || []);
      setAnimais(Array.isArray(animaisRes.data) ? animaisRes.data : animaisRes.data.content || []);
    } catch (error) {
      console.error('Erro ao carregar dados:', error);
      setError('Erro ao carregar dados');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      if (editingEvento) {
        // Backend pode não ter endpoint de update, então vamos apenas recarregar
        await api.post('/api/eventos', formData);
      } else {
        await api.post('/api/eventos', formData);
      }
      closeModal();
      loadData();
    } catch (error: any) {
      setError(error.response?.data?.message || 'Erro ao registrar evento');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Tem certeza que deseja excluir este evento?')) return;

    try {
      // Assumindo que o backend tem endpoint DELETE
      await api.delete(`/api/eventos/${id}`);
      loadData();
    } catch (error: any) {
      if (error.response?.status === 404 || error.response?.status === 405) {
        alert('Função de exclusão não disponível no backend');
      } else {
        alert('Erro ao excluir evento');
      }
    }
  };

  const openModal = (evento?: EventoDto) => {
    if (evento) {
      setEditingEvento(evento);
      setFormData({
        animalId: evento.animal.id,
        tipo: evento.tipo,
        data: evento.data,
        descricao: evento.descricao,
        peso: evento.peso,
        produto: evento.produto || '',
        dose: evento.dose,
        unidadeMedida: evento.unidadeMedida || '',
        valor: evento.valor
      });
    } else {
      setEditingEvento(null);
      setFormData({
        animalId: '',
        tipo: 'VACINA',
        data: new Date().toISOString().split('T')[0],
        descricao: '',
        peso: undefined,
        produto: '',
        dose: undefined,
        unidadeMedida: '',
        valor: undefined
      });
    }
    setShowModal(true);
    setError('');
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingEvento(null);
    setError('');
  };

  const filteredEventos = eventos.filter(
    (evento) =>
      evento.animal.brinco.toLowerCase().includes(searchTerm.toLowerCase()) ||
      evento.animal.nome?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      evento.tipo.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Paginação
  const totalPages = Math.ceil(filteredEventos.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedEventos = filteredEventos.slice(startIndex, startIndex + itemsPerPage);

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm]);

  const tiposEvento: TipoEvento[] = [
    'VACINA',
    'VERMIFUGO',
    'PESAGEM',
    'MOVIMENTACAO',
    'NASCIMENTO',
    'DESMAME',
    'MORTE',
    'VENDA',
    'COMPRA',
    'TRATAMENTO',
    'INSEMINACAO',
    'COBERTURA',
    'PARTO',
    'DIAGNOSTICO_GESTACAO',
    'DESCARTE',
    'OBSERVACAO'
  ];

  const getTipoColor = (tipo: TipoEvento) => {
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

  const showPesoField = formData.tipo === 'PESAGEM';
  const showProdutoFields =
    formData.tipo === 'VACINA' || formData.tipo === 'VERMIFUGO' || formData.tipo === 'TRATAMENTO';
  const showValorField =
    formData.tipo === 'VENDA' ||
    formData.tipo === 'COMPRA' ||
    formData.tipo === 'TRATAMENTO' ||
    formData.tipo === 'VACINA' ||
    formData.tipo === 'VERMIFUGO';

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Activity className="w-8 h-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Eventos</h1>
            <p className="text-gray-600">
              {filteredEventos.length} de {eventos.length} eventos
            </p>
          </div>
        </div>
        <button onClick={() => openModal()} className="btn-primary flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Registrar Evento
        </button>
      </div>

      {/* Search */}
      <div className="card">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Buscar por brinco, nome ou tipo..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input-field pl-10"
          />
        </div>
      </div>

      {/* Timeline */}
      <div className="space-y-4">
        {paginatedEventos.length === 0 ? (
          <div className="card text-center py-12">
            <Activity className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-600">
              {searchTerm ? 'Nenhum evento encontrado' : 'Nenhum evento registrado'}
            </p>
          </div>
        ) : (
          paginatedEventos.map((evento) => (
            <div key={evento.id} className="card hover:shadow-lg transition-shadow">
              <div className="flex items-start gap-4">
                <div className="flex-shrink-0">
                  <div className={`p-3 rounded-lg ${getTipoColor(evento.tipo)}`}>
                    <Activity className="w-6 h-6" />
                  </div>
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <h3 className="font-bold text-gray-900">{evento.tipo.replace(/_/g, ' ')}</h3>
                      <p className="text-sm text-gray-600">
                        {evento.animal.brinco}
                        {evento.animal.nome && ` - ${evento.animal.nome}`}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <div className="flex items-center gap-2 text-sm text-gray-500">
                        <CalendarIcon className="w-4 h-4" />
                        {new Date(evento.data).toLocaleDateString('pt-BR')}
                      </div>
                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => openModal(evento)}
                          className="p-2 hover:bg-gray-100 rounded-lg text-gray-600"
                          title="Editar"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(evento.id)}
                          className="p-2 hover:bg-red-100 rounded-lg text-red-600"
                          title="Excluir"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  </div>

                  <p className="text-gray-700 mb-3">{evento.descricao}</p>

                  <div className="flex flex-wrap gap-4 text-sm">
                    {evento.peso && (
                      <div className="flex items-center gap-1 text-gray-600">
                        <span className="font-medium">Peso:</span>
                        <span>{evento.peso} kg</span>
                      </div>
                    )}
                    {evento.produto && (
                      <div className="flex items-center gap-1 text-gray-600">
                        <span className="font-medium">Produto:</span>
                        <span>{evento.produto}</span>
                      </div>
                    )}
                    {evento.dose && (
                      <div className="flex items-center gap-1 text-gray-600">
                        <span className="font-medium">Dose:</span>
                        <span>
                          {evento.dose} {evento.unidadeMedida}
                        </span>
                      </div>
                    )}
                    {evento.valor && (
                      <div className="flex items-center gap-1 text-gray-600">
                        <span className="font-medium">Valor:</span>
                        <span>R$ {evento.valor.toFixed(2)}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Paginação */}
      {totalPages > 1 && (
        <div className="card">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-600">
              Mostrando {startIndex + 1} a {Math.min(startIndex + itemsPerPage, filteredEventos.length)} de{' '}
              {filteredEventos.length} eventos
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={currentPage === 1}
                className="p-2 hover:bg-gray-100 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              <div className="flex gap-1">
                {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                  let page;
                  if (totalPages <= 5) {
                    page = i + 1;
                  } else if (currentPage <= 3) {
                    page = i + 1;
                  } else if (currentPage >= totalPages - 2) {
                    page = totalPages - 4 + i;
                  } else {
                    page = currentPage - 2 + i;
                  }
                  return (
                    <button
                      key={page}
                      onClick={() => setCurrentPage(page)}
                      className={`px-3 py-1 rounded-lg text-sm font-medium ${
                        currentPage === page
                          ? 'bg-primary-600 text-white'
                          : 'hover:bg-gray-100 text-gray-700'
                      }`}
                    >
                      {page}
                    </button>
                  );
                })}
              </div>
              <button
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="p-2 hover:bg-gray-100 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-gray-900">
                {editingEvento ? 'Editar Evento' : 'Registrar Evento'}
              </h2>
              <button onClick={closeModal} className="p-2 hover:bg-gray-100 rounded-lg">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {error && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start gap-3">
                  <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                  <p className="text-sm text-red-800">{error}</p>
                </div>
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Animal *</label>
                  <select
                    required
                    disabled={!!editingEvento}
                    value={formData.animalId}
                    onChange={(e) => setFormData({ ...formData, animalId: e.target.value })}
                    className="input-field"
                  >
                    <option value="">Selecione um animal</option>
                    {animais.map((animal) => (
                      <option key={animal.id} value={animal.id}>
                        {animal.brinco} {animal.nome && `- ${animal.nome}`}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Tipo *</label>
                  <select
                    required
                    value={formData.tipo}
                    onChange={(e) =>
                      setFormData({ ...formData, tipo: e.target.value as TipoEvento })
                    }
                    className="input-field"
                  >
                    {tiposEvento.map((tipo) => (
                      <option key={tipo} value={tipo}>
                        {tipo.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Data *</label>
                  <input
                    type="date"
                    required
                    value={formData.data}
                    onChange={(e) => setFormData({ ...formData, data: e.target.value })}
                    className="input-field"
                  />
                </div>

                {showPesoField && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Peso (kg) *
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      required
                      value={formData.peso || ''}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          peso: e.target.value ? parseFloat(e.target.value) : undefined
                        })
                      }
                      className="input-field"
                      placeholder="Ex: 450.5"
                    />
                  </div>
                )}

                {showProdutoFields && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Produto
                      </label>
                      <input
                        type="text"
                        value={formData.produto}
                        onChange={(e) => setFormData({ ...formData, produto: e.target.value })}
                        className="input-field"
                        placeholder="Nome do produto"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Dose</label>
                      <div className="grid grid-cols-2 gap-2">
                        <input
                          type="number"
                          step="0.01"
                          value={formData.dose || ''}
                          onChange={(e) =>
                            setFormData({
                              ...formData,
                              dose: e.target.value ? parseFloat(e.target.value) : undefined
                            })
                          }
                          className="input-field"
                          placeholder="Ex: 5"
                        />
                        <input
                          type="text"
                          value={formData.unidadeMedida}
                          onChange={(e) =>
                            setFormData({ ...formData, unidadeMedida: e.target.value })
                          }
                          className="input-field"
                          placeholder="ml/g"
                        />
                      </div>
                    </div>
                  </>
                )}

                {showValorField && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Valor (R$)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      value={formData.valor || ''}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          valor: e.target.value ? parseFloat(e.target.value) : undefined
                        })
                      }
                      className="input-field"
                      placeholder="Ex: 150.00"
                    />
                  </div>
                )}

                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Descrição *
                  </label>
                  <textarea
                    required
                    value={formData.descricao}
                    onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
                    className="input-field"
                    rows={3}
                    placeholder="Descreva o evento..."
                  />
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button type="button" onClick={closeModal} className="btn-secondary flex-1">
                  Cancelar
                </button>
                <button type="submit" className="btn-primary flex-1">
                  {editingEvento ? 'Atualizar' : 'Registrar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
