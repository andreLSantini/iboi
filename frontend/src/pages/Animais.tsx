import { useState, useEffect } from 'react';
import {
  Beef,
  Plus,
  Search,
  Filter,
  Edit2,
  Trash2,
  X,
  AlertCircle,
  Eye,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import type {
  AnimalDto,
  CadastrarAnimalRequest,
  AtualizarAnimalRequest,
  Sexo,
  CategoriaAnimal,
  Raca,
  StatusAnimal,
  LoteDto,
  Pasture
} from '../types/index';

export default function Animais() {
  const navigate = useNavigate();
  const [animais, setAnimais] = useState<AnimalDto[]>([]);
  const [lotes, setLotes] = useState<LoteDto[]>([]);
  const [pastos, setPastos] = useState<Pasture[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [editingAnimal, setEditingAnimal] = useState<AnimalDto | null>(null);
  const [error, setError] = useState('');

  // Filtros
  const [filters, setFilters] = useState({
    status: '' as StatusAnimal | '',
    categoria: '' as CategoriaAnimal | '',
    sexo: '' as Sexo | '',
    raca: '' as Raca | ''
  });

  // Paginação
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const [formData, setFormData] = useState<CadastrarAnimalRequest>({
    brinco: '',
    rfid: '',
    codigoSisbov: '',
    nome: '',
    sexo: 'MACHO',
    raca: 'NELORE',
    dataNascimento: '',
    dataEntrada: '',
    pesoAtual: undefined,
    categoria: 'BEZERRO',
    loteId: undefined,
    pastureId: undefined,
    sisbovAtivo: false,
    observacoes: ''
  });

  useEffect(() => {
    loadAnimais();
    loadLotes();
    loadPastos();
  }, []);

  const loadAnimais = async () => {
    try {
      setLoading(true);
      const response = await api.get('/api/animais');
      const data = Array.isArray(response.data) ? response.data : response.data.content || [];
      setAnimais(data);
    } catch (error) {
      console.error('Erro ao carregar animais:', error);
      setError('Erro ao carregar animais');
    } finally {
      setLoading(false);
    }
  };

  const loadLotes = async () => {
    try {
      const response = await api.get('/api/lotes', {
        params: { apenasAtivos: true }
      });
      const data = Array.isArray(response.data) ? response.data : response.data.content || [];
      setLotes(data);
    } catch (error) {
      console.error('Erro ao carregar lotes:', error);
    }
  };

  const loadPastos = async () => {
    try {
      const farmsRaw = localStorage.getItem('current_farm');
      const farm = farmsRaw ? JSON.parse(farmsRaw) : null;
      if (!farm?.id) {
        return;
      }
      const response = await api.get(`/api/farms/${farm.id}/pastures`);
      setPastos(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Erro ao carregar pastos:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      if (editingAnimal) {
        const updateData: AtualizarAnimalRequest = {
          rfid: formData.rfid || undefined,
          codigoSisbov: formData.codigoSisbov || undefined,
          nome: formData.nome || undefined,
          raca: formData.raca,
          pesoAtual: formData.pesoAtual,
          categoria: formData.categoria,
          loteId: formData.loteId,
          pastureId: formData.pastureId,
          dataEntrada: formData.dataEntrada || undefined,
          sisbovAtivo: formData.sisbovAtivo,
          observacoes: formData.observacoes || undefined
        };
        await api.put(`/api/animais/${editingAnimal.id}`, updateData);
      } else {
        await api.post('/api/animais', formData);
      }
      closeModal();
      loadAnimais();
    } catch (error: any) {
      setError(error.response?.data?.message || 'Erro ao salvar animal');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Tem certeza que deseja excluir este animal?')) return;

    try {
      await api.delete(`/api/animais/${id}`);
      loadAnimais();
    } catch (error) {
      alert('Erro ao excluir animal');
    }
  };

  const openModal = (animal?: AnimalDto) => {
    if (animal) {
      setEditingAnimal(animal);
      setFormData({
        brinco: animal.brinco,
        rfid: animal.rfid || '',
        codigoSisbov: animal.codigoSisbov || '',
        nome: animal.nome || '',
        sexo: animal.sexo,
        raca: animal.raca,
        dataNascimento: animal.dataNascimento,
        dataEntrada: animal.dataEntrada || '',
        pesoAtual: animal.pesoAtual,
        categoria: animal.categoria,
        loteId: animal.lote?.id,
        pastureId: animal.pasture?.id,
        sisbovAtivo: animal.sisbovAtivo,
        observacoes: animal.observacoes || ''
      });
    } else {
      setEditingAnimal(null);
      setFormData({
        brinco: '',
        rfid: '',
        codigoSisbov: '',
        nome: '',
        sexo: 'MACHO',
        raca: 'NELORE',
        dataNascimento: '',
        dataEntrada: '',
        pesoAtual: undefined,
        categoria: 'BEZERRO',
        loteId: undefined,
        pastureId: undefined,
        sisbovAtivo: false,
        observacoes: ''
      });
    }
    setShowModal(true);
    setError('');
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingAnimal(null);
    setError('');
  };

  const clearFilters = () => {
    setFilters({
      status: '',
      categoria: '',
      sexo: '',
      raca: ''
    });
  };

  // Aplicar filtros e busca
  const filteredAnimais = animais.filter((animal) => {
    const matchSearch =
      animal.brinco.toLowerCase().includes(searchTerm.toLowerCase()) ||
      animal.nome?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchStatus = !filters.status || animal.status === filters.status;
    const matchCategoria = !filters.categoria || animal.categoria === filters.categoria;
    const matchSexo = !filters.sexo || animal.sexo === filters.sexo;
    const matchRaca = !filters.raca || animal.raca === filters.raca;

    return matchSearch && matchStatus && matchCategoria && matchSexo && matchRaca;
  });

  // Paginação
  const totalPages = Math.ceil(filteredAnimais.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedAnimais = filteredAnimais.slice(startIndex, startIndex + itemsPerPage);

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, filters]);

  const getStatusColor = (status: StatusAnimal) => {
    const colors = {
      ATIVO: 'bg-green-100 text-green-800',
      VENDIDO: 'bg-blue-100 text-blue-800',
      MORTO: 'bg-red-100 text-red-800',
      DESCARTADO: 'bg-gray-100 text-gray-800',
      TRANSFERIDO: 'bg-yellow-100 text-yellow-800'
    };
    return colors[status];
  };

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
    'OUTRAS'
  ];

  const categoriasOptions: CategoriaAnimal[] = [
    'BEZERRO',
    'NOVILHO',
    'NOVILHA',
    'BOI',
    'VACA',
    'TOURO',
    'MATRIZ'
  ];

  const statusOptions: StatusAnimal[] = [
    'ATIVO',
    'VENDIDO',
    'MORTO',
    'DESCARTADO',
    'TRANSFERIDO'
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
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Beef className="w-8 h-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Animais</h1>
            <p className="text-gray-600">
              {filteredAnimais.length} de {animais.length} animais
            </p>
          </div>
        </div>
        <button onClick={() => openModal()} className="btn-primary flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Adicionar Animal
        </button>
      </div>

      {/* Search and Filter */}
      <div className="card space-y-4">
        <div className="flex gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por brinco ou nome..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="input-field pl-10"
            />
          </div>
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`btn-secondary flex items-center gap-2 ${
              Object.values(filters).some((f) => f) ? 'bg-primary-50 border-primary-300' : ''
            }`}
          >
            <Filter className="w-4 h-4" />
            Filtros
            {Object.values(filters).filter((f) => f).length > 0 && (
              <span className="bg-primary-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                {Object.values(filters).filter((f) => f).length}
              </span>
            )}
          </button>
        </div>

        {showFilters && (
          <div className="pt-4 border-t border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                <select
                  value={filters.status}
                  onChange={(e) =>
                    setFilters({ ...filters, status: e.target.value as StatusAnimal | '' })
                  }
                  className="input-field"
                >
                  <option value="">Todos</option>
                  {statusOptions.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Categoria</label>
                <select
                  value={filters.categoria}
                  onChange={(e) =>
                    setFilters({ ...filters, categoria: e.target.value as CategoriaAnimal | '' })
                  }
                  className="input-field"
                >
                  <option value="">Todas</option>
                  {categoriasOptions.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Sexo</label>
                <select
                  value={filters.sexo}
                  onChange={(e) => setFilters({ ...filters, sexo: e.target.value as Sexo | '' })}
                  className="input-field"
                >
                  <option value="">Todos</option>
                  <option value="MACHO">Macho</option>
                  <option value="FEMEA">Fêmea</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Raça</label>
                <select
                  value={filters.raca}
                  onChange={(e) => setFilters({ ...filters, raca: e.target.value as Raca | '' })}
                  className="input-field"
                >
                  <option value="">Todas</option>
                  {racasOptions.map((raca) => (
                    <option key={raca} value={raca}>
                      {raca.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {Object.values(filters).some((f) => f) && (
              <div className="mt-4">
                <button onClick={clearFilters} className="text-sm text-primary-600 hover:text-primary-700 font-medium">
                  Limpar filtros
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Table */}
      <div className="card overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-gray-200">
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Brinco</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Nome</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Sexo</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Raça</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Categoria</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Idade</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Peso</th>
              <th className="text-left py-3 px-4 font-semibold text-gray-700">Status</th>
              <th className="text-right py-3 px-4 font-semibold text-gray-700">Ações</th>
            </tr>
          </thead>
          <tbody>
            {paginatedAnimais.length === 0 ? (
              <tr>
                <td colSpan={9} className="text-center py-12">
                  <Beef className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                  <p className="text-gray-600">
                    {searchTerm || Object.values(filters).some((f) => f)
                      ? 'Nenhum animal encontrado'
                      : 'Nenhum animal cadastrado'}
                  </p>
                </td>
              </tr>
            ) : (
              paginatedAnimais.map((animal) => (
                <tr key={animal.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium text-gray-900">{animal.brinco}</td>
                  <td className="py-3 px-4 text-gray-700">{animal.nome || '-'}</td>
                  <td className="py-3 px-4 text-gray-700">{animal.sexo}</td>
                  <td className="py-3 px-4 text-gray-700">{animal.raca}</td>
                  <td className="py-3 px-4 text-gray-700">{animal.categoria}</td>
                  <td className="py-3 px-4 text-gray-700">{animal.idade} meses</td>
                  <td className="py-3 px-4 text-gray-700">
                    {animal.pesoAtual ? `${animal.pesoAtual} kg` : '-'}
                  </td>
                  <td className="py-3 px-4">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(
                        animal.status
                      )}`}
                    >
                      {animal.status}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => navigate(`/app/animais/${animal.id}`)}
                        className="p-2 hover:bg-blue-100 rounded-lg text-blue-600"
                        title="Ver detalhes"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => openModal(animal)}
                        className="p-2 hover:bg-gray-100 rounded-lg text-gray-600"
                        title="Editar"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(animal.id)}
                        className="p-2 hover:bg-red-100 rounded-lg text-red-600"
                        title="Excluir"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Paginação */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200">
            <div className="text-sm text-gray-600">
              Mostrando {startIndex + 1} a {Math.min(startIndex + itemsPerPage, filteredAnimais.length)} de{' '}
              {filteredAnimais.length} animais
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
                {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
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
                ))}
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
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-gray-900">
                {editingAnimal ? 'Editar Animal' : 'Adicionar Animal'}
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
                  <label className="block text-sm font-medium text-gray-700 mb-1">Brinco *</label>
                  <input
                    type="text"
                    required
                    disabled={!!editingAnimal}
                    value={formData.brinco}
                    onChange={(e) => setFormData({ ...formData, brinco: e.target.value })}
                    className="input-field"
                    placeholder="Ex: 1234"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">RFID</label>
                  <input
                    type="text"
                    value={formData.rfid || ''}
                    onChange={(e) => setFormData({ ...formData, rfid: e.target.value })}
                    className="input-field"
                    placeholder="Ex: 789123456"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Codigo SISBOV</label>
                  <input
                    type="text"
                    value={formData.codigoSisbov || ''}
                    onChange={(e) => setFormData({ ...formData, codigoSisbov: e.target.value })}
                    className="input-field"
                    placeholder="Ex: BR123456789"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Nome</label>
                  <input
                    type="text"
                    value={formData.nome}
                    onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                    className="input-field"
                    placeholder="Ex: Estrela"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Data de Entrada
                  </label>
                  <input
                    type="date"
                    value={formData.dataEntrada || ''}
                    onChange={(e) => setFormData({ ...formData, dataEntrada: e.target.value })}
                    className="input-field"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Sexo *</label>
                  <select
                    required
                    disabled={!!editingAnimal}
                    value={formData.sexo}
                    onChange={(e) => setFormData({ ...formData, sexo: e.target.value as Sexo })}
                    className="input-field"
                  >
                    <option value="MACHO">Macho</option>
                    <option value="FEMEA">Fêmea</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Raça *</label>
                  <select
                    required
                    value={formData.raca}
                    onChange={(e) => setFormData({ ...formData, raca: e.target.value as Raca })}
                    className="input-field"
                  >
                    {racasOptions.map((raca) => (
                      <option key={raca} value={raca}>
                        {raca.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Data de Nascimento *
                  </label>
                  <input
                    type="date"
                    required
                    disabled={!!editingAnimal}
                    value={formData.dataNascimento}
                    onChange={(e) => setFormData({ ...formData, dataNascimento: e.target.value })}
                    className="input-field"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Categoria *
                  </label>
                  <select
                    required
                    value={formData.categoria}
                    onChange={(e) =>
                      setFormData({ ...formData, categoria: e.target.value as CategoriaAnimal })
                    }
                    className="input-field"
                  >
                    {categoriasOptions.map((cat) => (
                      <option key={cat} value={cat}>
                        {cat}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Lote
                  </label>
                  <select
                    value={formData.loteId || ''}
                    onChange={(e) =>
                      setFormData({ ...formData, loteId: e.target.value || undefined })
                    }
                    className="input-field"
                  >
                    <option value="">Sem lote</option>
                    {lotes.map((lote) => (
                      <option key={lote.id} value={lote.id}>
                        {lote.nome} ({lote.quantidadeAnimais} animais)
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Pasto Atual
                  </label>
                  <select
                    value={formData.pastureId || ''}
                    onChange={(e) =>
                      setFormData({ ...formData, pastureId: e.target.value || undefined })
                    }
                    className="input-field"
                  >
                    <option value="">Sem pasto</option>
                    {pastos.map((pasto) => (
                      <option key={pasto.id} value={pasto.id}>
                        {pasto.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Peso Atual (kg)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.pesoAtual || ''}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        pesoAtual: e.target.value ? parseFloat(e.target.value) : undefined
                      })
                    }
                    className="input-field"
                    placeholder="Ex: 450.5"
                  />
                </div>

                <div className="md:col-span-2">
                  <label className="flex items-center gap-3 rounded-lg border border-gray-200 px-4 py-3">
                    <input
                      type="checkbox"
                      checked={Boolean(formData.sisbovAtivo)}
                      onChange={(e) => setFormData({ ...formData, sisbovAtivo: e.target.checked })}
                    />
                    <span className="text-sm font-medium text-gray-700">Animal com SISBOV ativo</span>
                  </label>
                </div>

                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Observações
                  </label>
                  <textarea
                    value={formData.observacoes}
                    onChange={(e) => setFormData({ ...formData, observacoes: e.target.value })}
                    className="input-field"
                    rows={3}
                    placeholder="Informações adicionais sobre o animal..."
                  />
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button type="button" onClick={closeModal} className="btn-secondary flex-1">
                  Cancelar
                </button>
                <button type="submit" className="btn-primary flex-1">
                  {editingAnimal ? 'Atualizar' : 'Cadastrar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
