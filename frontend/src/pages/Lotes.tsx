import { useState, useEffect } from 'react';
import {
  Package,
  Plus,
  Search,
  Edit2,
  Trash2,
  X,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  Beef
} from 'lucide-react';
import { loteService } from '../services/loteService';
import type {
  LoteDto,
  CadastrarLoteRequest,
  AtualizarLoteRequest
} from '../types/index';

export default function Lotes() {
  const [lotes, setLotes] = useState<LoteDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingLote, setEditingLote] = useState<LoteDto | null>(null);
  const [error, setError] = useState('');
  const [showAtivosOnly, setShowAtivosOnly] = useState(false);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [formData, setFormData] = useState<CadastrarLoteRequest>({
    nome: '',
    descricao: ''
  });

  useEffect(() => {
    loadLotes();
  }, [currentPage, showAtivosOnly]);

  const loadLotes = async () => {
    try {
      setLoading(true);
      const response = await loteService.listar(showAtivosOnly, currentPage, 10);
      setLotes(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Erro ao carregar lotes:', error);
      setError('Erro ao carregar lotes');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      if (editingLote) {
        const updateData: AtualizarLoteRequest = {
          nome: formData.nome,
          descricao: formData.descricao || undefined
        };
        await loteService.atualizar(editingLote.id, updateData);
      } else {
        await loteService.cadastrar(formData);
      }
      closeModal();
      loadLotes();
    } catch (error: any) {
      setError(error.response?.data?.message || 'Erro ao salvar lote');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Tem certeza que deseja excluir este lote?')) return;

    try {
      await loteService.deletar(id);
      loadLotes();
    } catch (error: any) {
      const message = error.response?.data?.message || 'Erro ao excluir lote';
      alert(message);
    }
  };

  const openModal = (lote?: LoteDto) => {
    if (lote) {
      setEditingLote(lote);
      setFormData({
        nome: lote.nome,
        descricao: lote.descricao || ''
      });
    } else {
      setEditingLote(null);
      setFormData({
        nome: '',
        descricao: ''
      });
    }
    setShowModal(true);
    setError('');
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingLote(null);
    setError('');
  };

  const filteredLotes = lotes.filter((lote) =>
    lote.nome.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Package className="w-8 h-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Lotes</h1>
            <p className="text-gray-600">
              {totalElements} {totalElements === 1 ? 'lote' : 'lotes'}
            </p>
          </div>
        </div>
        <button onClick={() => openModal()} className="btn-primary flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Novo Lote
        </button>
      </div>

      <div className="card space-y-4">
        <div className="flex gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por nome..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="input-field pl-10"
            />
          </div>
          <label className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50">
            <input
              type="checkbox"
              checked={showAtivosOnly}
              onChange={(e) => {
                setShowAtivosOnly(e.target.checked);
                setCurrentPage(0);
              }}
              className="w-4 h-4 text-primary-600"
            />
            <span className="text-sm font-medium text-gray-700">Apenas ativos</span>
          </label>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredLotes.length === 0 ? (
          <div className="col-span-full card text-center py-12">
            <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-600">
              {searchTerm ? 'Nenhum lote encontrado' : 'Nenhum lote cadastrado'}
            </p>
          </div>
        ) : (
          filteredLotes.map((lote) => (
            <div key={lote.id} className="card hover:shadow-lg transition-shadow">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg ${lote.ativo ? 'bg-green-100' : 'bg-gray-100'}`}>
                    <Package className={`w-5 h-5 ${lote.ativo ? 'text-green-600' : 'text-gray-600'}`} />
                  </div>
                  <div>
                    <h3 className="font-bold text-gray-900">{lote.nome}</h3>
                    <span
                      className={`text-xs px-2 py-1 rounded-full ${
                        lote.ativo
                          ? 'bg-green-100 text-green-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}
                    >
                      {lote.ativo ? 'Ativo' : 'Inativo'}
                    </span>
                  </div>
                </div>
                <div className="flex gap-1">
                  <button
                    onClick={() => openModal(lote)}
                    className="p-2 hover:bg-gray-100 rounded-lg text-gray-600"
                    title="Editar"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => handleDelete(lote.id)}
                    className="p-2 hover:bg-red-100 rounded-lg text-red-600"
                    title="Excluir"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {lote.descricao && (
                <p className="text-sm text-gray-600 mb-3">{lote.descricao}</p>
              )}

              <div className="flex items-center gap-2 text-sm text-gray-600 pt-3 border-t border-gray-200">
                <Beef className="w-4 h-4" />
                <span>
                  {lote.quantidadeAnimais} {lote.quantidadeAnimais === 1 ? 'animal' : 'animais'}
                </span>
              </div>

              <div className="mt-3 grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-emerald-50 px-3 py-2">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-emerald-700">Peso medio</p>
                  <p className="mt-1 font-semibold text-gray-900">
                    {lote.pesoMedioAtual == null ? '-' : `${Number(lote.pesoMedioAtual).toFixed(1)} kg`}
                  </p>
                </div>
                <div className="rounded-xl bg-blue-50 px-3 py-2">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-blue-700">GMD 30d</p>
                  <p className="mt-1 font-semibold text-gray-900">
                    {lote.gmdMedio30Dias == null ? '-' : `${Number(lote.gmdMedio30Dias).toFixed(3)} kg/d`}
                  </p>
                </div>
              </div>

              <p className="text-xs text-gray-500 mt-2">
                Criado em {new Date(lote.criadoEm).toLocaleDateString('pt-BR')}
              </p>
            </div>
          ))
        )}
      </div>

      {totalPages > 1 && (
        <div className="card">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-600">
              Página {currentPage + 1} de {totalPages}
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={currentPage === 0}
                className="p-2 hover:bg-gray-100 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              <div className="flex gap-1">
                {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                  let page;
                  if (totalPages <= 5) {
                    page = i;
                  } else if (currentPage <= 2) {
                    page = i;
                  } else if (currentPage >= totalPages - 3) {
                    page = totalPages - 5 + i;
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
                      {page + 1}
                    </button>
                  );
                })}
              </div>
              <button
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className="p-2 hover:bg-gray-100 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-gray-900">
                {editingLote ? 'Editar Lote' : 'Novo Lote'}
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

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nome *</label>
                <input
                  type="text"
                  required
                  value={formData.nome}
                  onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                  className="input-field"
                  placeholder="Ex: Lote 1 - Engorda"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descrição</label>
                <textarea
                  value={formData.descricao}
                  onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
                  className="input-field"
                  rows={3}
                  placeholder="Informações sobre o lote..."
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button type="button" onClick={closeModal} className="btn-secondary flex-1">
                  Cancelar
                </button>
                <button type="submit" className="btn-primary flex-1">
                  {editingLote ? 'Atualizar' : 'Cadastrar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
