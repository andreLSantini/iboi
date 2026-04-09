import { useEffect, useState } from 'react';
import {
  ArrowRightLeft,
  Beef,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Package,
  Plus,
  Search,
  Trash2,
  X,
  AlertCircle,
} from 'lucide-react';
import api from '../services/api';
import { getCurrentFarm, getFarms } from '../services/session';
import { loteService } from '../services/loteService';
import type {
  AnimalDto,
  CadastrarLoteRequest,
  AtualizarLoteRequest,
  FarmSummary,
  LoteDto,
  Pasture,
  RegistrarMovimentacaoLoteRequest,
  TipoMovimentacaoAnimal,
} from '../types/index';

function buildInitialMovementForm(loteId?: string): RegistrarMovimentacaoLoteRequest {
  return {
    loteOrigemId: loteId,
    animalIds: [],
    tipo: 'ENTRE_LOTES',
    movimentadaEm: new Date().toISOString().split('T')[0],
    destinoFarmId: '',
    destinoPastureId: '',
    destinoLoteId: '',
    numeroGta: '',
    documentoExterno: '',
    motivo: '',
    observacoes: '',
  };
}

export default function Lotes() {
  const currentFarm = getCurrentFarm();
  const farms = getFarms();

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

  const [showMovementModal, setShowMovementModal] = useState(false);
  const [movementError, setMovementError] = useState('');
  const [movingBatch, setMovingBatch] = useState(false);
  const [selectedLoteForMovement, setSelectedLoteForMovement] = useState<LoteDto | null>(null);
  const [loteAnimais, setLoteAnimais] = useState<AnimalDto[]>([]);
  const [selectedAnimalIds, setSelectedAnimalIds] = useState<string[]>([]);
  const [pastos, setPastos] = useState<Pasture[]>([]);
  const [movementForm, setMovementForm] = useState<RegistrarMovimentacaoLoteRequest>(buildInitialMovementForm());

  const [formData, setFormData] = useState<CadastrarLoteRequest>({
    nome: '',
    descricao: '',
  });

  useEffect(() => {
    void loadLotes();
  }, [currentPage, showAtivosOnly]);

  useEffect(() => {
    void loadPastos();
  }, [currentFarm?.id]);

  const loadLotes = async () => {
    try {
      setLoading(true);
      const response = await loteService.listar(showAtivosOnly, currentPage, 10);
      setLotes(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (loadError) {
      console.error('Erro ao carregar lotes:', loadError);
      setError('Erro ao carregar lotes');
    } finally {
      setLoading(false);
    }
  };

  const loadPastos = async () => {
    if (!currentFarm?.id) return;
    try {
      const response = await api.get<Pasture[]>(`/api/farms/${currentFarm.id}/pastures`);
      setPastos(Array.isArray(response.data) ? response.data : []);
    } catch (loadError) {
      console.error('Erro ao carregar pastos:', loadError);
    }
  };

  const loadAnimaisDoLote = async (lote: LoteDto) => {
    const response = await api.get('/api/animais', {
      params: {
        loteId: lote.id,
        status: 'ATIVO',
      },
    });
    const animais = Array.isArray(response.data) ? response.data : response.data.content || [];
    setLoteAnimais(animais);
    setSelectedAnimalIds(animais.map((animal: AnimalDto) => animal.id));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      if (editingLote) {
        const updateData: AtualizarLoteRequest = {
          nome: formData.nome,
          descricao: formData.descricao || undefined,
        };
        await loteService.atualizar(editingLote.id, updateData);
      } else {
        await loteService.cadastrar(formData);
      }
      closeModal();
      await loadLotes();
    } catch (submitError: any) {
      setError(submitError.response?.data?.message || 'Erro ao salvar lote');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Tem certeza que deseja excluir este lote?')) return;

    try {
      await loteService.deletar(id);
      await loadLotes();
    } catch (deleteError: any) {
      const message = deleteError.response?.data?.message || 'Erro ao excluir lote';
      alert(message);
    }
  };

  const openModal = (lote?: LoteDto) => {
    if (lote) {
      setEditingLote(lote);
      setFormData({
        nome: lote.nome,
        descricao: lote.descricao || '',
      });
    } else {
      setEditingLote(null);
      setFormData({
        nome: '',
        descricao: '',
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

  const openMovementModal = async (lote: LoteDto) => {
    try {
      setSelectedLoteForMovement(lote);
      setMovementForm(buildInitialMovementForm(lote.id));
      setMovementError('');
      setShowMovementModal(true);
      await loadAnimaisDoLote(lote);
    } catch (loadError: any) {
      console.error('Erro ao carregar animais do lote:', loadError);
      setMovementError(loadError.response?.data?.message || 'Erro ao carregar os animais do lote.');
    }
  };

  const closeMovementModal = () => {
    setShowMovementModal(false);
    setMovementError('');
    setMovingBatch(false);
    setSelectedLoteForMovement(null);
    setLoteAnimais([]);
    setSelectedAnimalIds([]);
    setMovementForm(buildInitialMovementForm());
  };

  const toggleAnimalSelection = (animalId: string) => {
    setSelectedAnimalIds((current) =>
      current.includes(animalId) ? current.filter((id) => id !== animalId) : [...current, animalId],
    );
  };

  const submitBatchMovement = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedLoteForMovement) return;

    if (selectedAnimalIds.length === 0) {
      setMovementError('Selecione ao menos um animal para movimentar.');
      return;
    }

    try {
      setMovingBatch(true);
      setMovementError('');
      const payload: RegistrarMovimentacaoLoteRequest = {
        ...movementForm,
        loteOrigemId: selectedLoteForMovement.id,
        animalIds: selectedAnimalIds,
        destinoFarmId: movementForm.destinoFarmId || undefined,
        destinoPastureId: movementForm.destinoPastureId || undefined,
        destinoLoteId: movementForm.destinoLoteId || undefined,
        numeroGta: movementForm.numeroGta || undefined,
        documentoExterno: movementForm.documentoExterno || undefined,
        motivo: movementForm.motivo || undefined,
        observacoes: movementForm.observacoes || undefined,
      };

      const response = await api.post('/api/animais/movimentacoes-em-lote', payload);
      const total = response.data?.totalAnimais ?? selectedAnimalIds.length;
      closeMovementModal();
      await loadLotes();
      alert(`${total} animal(is) movimentado(s) com sucesso.`);
    } catch (submitError: any) {
      console.error('Erro ao registrar movimentacao em lote:', submitError);
      setMovementError(submitError.response?.data?.message || 'Erro ao registrar movimentacao em lote.');
    } finally {
      setMovingBatch(false);
    }
  };

  const filteredLotes = lotes.filter((lote) => lote.nome.toLowerCase().includes(searchTerm.toLowerCase()));

  const movementOptions: TipoMovimentacaoAnimal[] = [
    'ENTRE_LOTES',
    'ENTRE_PASTOS',
    'ENTRE_FAZENDAS',
    'SAIDA_EXTERNA',
    'ENTRADA_EXTERNA',
  ];

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-12 w-12 animate-spin rounded-full border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Package className="h-8 w-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Lotes</h1>
            <p className="text-gray-600">
              {totalElements} {totalElements === 1 ? 'lote' : 'lotes'}
            </p>
          </div>
        </div>
        <button onClick={() => openModal()} className="btn-primary flex items-center gap-2">
          <Plus className="h-4 w-4" />
          Novo lote
        </button>
      </div>

      <div className="card space-y-4">
        <div className="flex gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 transform text-gray-400" />
            <input
              type="text"
              placeholder="Buscar por nome..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="input-field pl-10"
            />
          </div>
          <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-gray-300 px-4 py-2 hover:bg-gray-50">
            <input
              type="checkbox"
              checked={showAtivosOnly}
              onChange={(e) => {
                setShowAtivosOnly(e.target.checked);
                setCurrentPage(0);
              }}
              className="h-4 w-4 text-primary-600"
            />
            <span className="text-sm font-medium text-gray-700">Apenas ativos</span>
          </label>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredLotes.length === 0 ? (
          <div className="card col-span-full py-12 text-center">
            <Package className="mx-auto mb-4 h-16 w-16 text-gray-300" />
            <p className="text-gray-600">{searchTerm ? 'Nenhum lote encontrado' : 'Nenhum lote cadastrado'}</p>
          </div>
        ) : (
          filteredLotes.map((lote) => {
            const gmd30 = lote.gmdPorJanela.find((janela) => janela.janelaDias === 30);

            return (
              <div key={lote.id} className="card transition-shadow hover:shadow-lg">
                <div className="mb-3 flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`rounded-lg p-2 ${lote.ativo ? 'bg-green-100' : 'bg-gray-100'}`}>
                      <Package className={`h-5 w-5 ${lote.ativo ? 'text-green-600' : 'text-gray-600'}`} />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">{lote.nome}</h3>
                      <span
                        className={`rounded-full px-2 py-1 text-xs ${
                          lote.ativo ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {lote.ativo ? 'Ativo' : 'Inativo'}
                      </span>
                    </div>
                  </div>
                  <div className="flex gap-1">
                    <button
                      onClick={() => openModal(lote)}
                      className="rounded-lg p-2 text-gray-600 hover:bg-gray-100"
                      title="Editar"
                    >
                      <Edit2 className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(lote.id)}
                      className="rounded-lg p-2 text-red-600 hover:bg-red-100"
                      title="Excluir"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>

                {lote.descricao && <p className="mb-3 text-sm text-gray-600">{lote.descricao}</p>}

                <div className="border-t border-gray-200 pt-3 text-sm text-gray-600">
                  <div className="flex items-center gap-2">
                    <Beef className="h-4 w-4" />
                    <span>
                      {lote.quantidadeAnimais} {lote.quantidadeAnimais === 1 ? 'animal' : 'animais'}
                    </span>
                  </div>
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
                      {gmd30?.ganhoMedioDiario == null ? '-' : `${Number(gmd30.ganhoMedioDiario).toFixed(3)} kg/d`}
                    </p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => void openMovementModal(lote)}
                  className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-800 transition hover:bg-amber-100"
                >
                  <ArrowRightLeft className="h-4 w-4" />
                  Movimentacao em lote
                </button>

                <p className="mt-2 text-xs text-gray-500">Criado em {new Date(lote.criadoEm).toLocaleDateString('pt-BR')}</p>
              </div>
            );
          })
        )}
      </div>

      {totalPages > 1 && (
        <div className="card">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-600">
              Pagina {currentPage + 1} de {totalPages}
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={currentPage === 0}
                className="rounded-lg p-2 hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronLeft className="h-5 w-5" />
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
                      className={`rounded-lg px-3 py-1 text-sm font-medium ${
                        currentPage === page ? 'bg-primary-600 text-white' : 'text-gray-700 hover:bg-gray-100'
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
                className="rounded-lg p-2 hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronRight className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
          <div className="w-full max-w-md rounded-lg bg-white">
            <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
              <h2 className="text-xl font-bold text-gray-900">{editingLote ? 'Editar lote' : 'Novo lote'}</h2>
              <button onClick={closeModal} className="rounded-lg p-2 hover:bg-gray-100">
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4 p-6">
              {error && (
                <div className="flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4">
                  <AlertCircle className="mt-0.5 h-5 w-5 flex-shrink-0 text-red-600" />
                  <p className="text-sm text-red-800">{error}</p>
                </div>
              )}

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Nome *</label>
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
                <label className="mb-1 block text-sm font-medium text-gray-700">Descricao</label>
                <textarea
                  value={formData.descricao}
                  onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
                  className="input-field"
                  rows={3}
                  placeholder="Informacoes sobre o lote..."
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

      {showMovementModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="max-h-[92vh] w-full max-w-4xl overflow-y-auto rounded-3xl bg-white shadow-xl">
            <div className="sticky top-0 flex items-center justify-between border-b border-gray-100 bg-white px-6 py-4">
              <div>
                <h2 className="text-xl font-bold text-gray-900">Movimentacao em lote</h2>
                <p className="text-sm text-gray-600">
                  {selectedLoteForMovement ? `Origem: ${selectedLoteForMovement.nome}` : 'Selecione os animais e o destino.'}
                </p>
              </div>
              <button onClick={closeMovementModal} className="rounded-lg p-2 hover:bg-gray-100">
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={submitBatchMovement} className="space-y-6 p-6">
              {movementError && (
                <div className="flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 p-4">
                  <AlertCircle className="mt-0.5 h-5 w-5 flex-shrink-0 text-red-600" />
                  <p className="text-sm text-red-800">{movementError}</p>
                </div>
              )}

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
                <div className="rounded-2xl bg-amber-50 p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-amber-700">Animais selecionados</p>
                  <p className="mt-2 text-2xl font-bold text-gray-900">{selectedAnimalIds.length}</p>
                  <p className="mt-1 text-sm text-gray-600">de {loteAnimais.length} animais ativos no lote</p>
                </div>
                <div className="rounded-2xl bg-emerald-50 p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-emerald-700">Tipo</p>
                  <p className="mt-2 text-lg font-bold text-gray-900">{movementForm.tipo.replace(/_/g, ' ')}</p>
                  <p className="mt-1 text-sm text-gray-600">Fluxo otimizado para registrar varios animais de uma vez.</p>
                </div>
                <div className="rounded-2xl bg-blue-50 p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-blue-700">Data</p>
                  <p className="mt-2 text-lg font-bold text-gray-900">
                    {new Date(movementForm.movimentadaEm).toLocaleDateString('pt-BR')}
                  </p>
                  <p className="mt-1 text-sm text-gray-600">Use a data real do manejo de campo.</p>
                </div>
              </div>

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Tipo</label>
                  <select
                    className="input-field"
                    value={movementForm.tipo}
                    onChange={(e) =>
                      setMovementForm((current) => ({
                        ...current,
                        tipo: e.target.value as TipoMovimentacaoAnimal,
                      }))
                    }
                  >
                    {movementOptions.map((tipo) => (
                      <option key={tipo} value={tipo}>
                        {tipo.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Data da movimentacao</label>
                  <input
                    type="date"
                    className="input-field"
                    value={movementForm.movimentadaEm}
                    onChange={(e) => setMovementForm((current) => ({ ...current, movimentadaEm: e.target.value }))}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Lote destino</label>
                  <select
                    className="input-field"
                    value={movementForm.destinoLoteId || ''}
                    onChange={(e) => setMovementForm((current) => ({ ...current, destinoLoteId: e.target.value || undefined }))}
                  >
                    <option value="">Nao alterar</option>
                    {lotes
                      .filter((lote) => lote.id !== selectedLoteForMovement?.id)
                      .map((lote) => (
                        <option key={lote.id} value={lote.id}>
                          {lote.nome}
                        </option>
                      ))}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Pasto destino</label>
                  <select
                    className="input-field"
                    value={movementForm.destinoPastureId || ''}
                    onChange={(e) => setMovementForm((current) => ({ ...current, destinoPastureId: e.target.value || undefined }))}
                  >
                    <option value="">Nao alterar</option>
                    {pastos.map((pasto) => (
                      <option key={pasto.id} value={pasto.id}>
                        {pasto.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Fazenda destino</label>
                  <select
                    className="input-field"
                    value={movementForm.destinoFarmId || ''}
                    onChange={(e) => setMovementForm((current) => ({ ...current, destinoFarmId: e.target.value || undefined }))}
                  >
                    <option value="">Nao alterar</option>
                    {(farms as FarmSummary[]).map((farm) => (
                      <option key={farm.id} value={farm.id}>
                        {farm.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Numero GTA</label>
                  <input
                    className="input-field"
                    value={movementForm.numeroGta || ''}
                    onChange={(e) => setMovementForm((current) => ({ ...current, numeroGta: e.target.value }))}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">Documento externo</label>
                  <input
                    className="input-field"
                    value={movementForm.documentoExterno || ''}
                    onChange={(e) => setMovementForm((current) => ({ ...current, documentoExterno: e.target.value }))}
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="mb-1 block text-sm font-medium text-gray-700">Motivo</label>
                  <input
                    className="input-field"
                    value={movementForm.motivo || ''}
                    onChange={(e) => setMovementForm((current) => ({ ...current, motivo: e.target.value }))}
                    placeholder="Ex: remanejamento, apartacao, transferencia"
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="mb-1 block text-sm font-medium text-gray-700">Observacoes</label>
                  <textarea
                    rows={3}
                    className="input-field"
                    value={movementForm.observacoes || ''}
                    onChange={(e) => setMovementForm((current) => ({ ...current, observacoes: e.target.value }))}
                  />
                </div>
              </div>

              <div className="rounded-3xl border border-gray-200 bg-gray-50 p-4">
                <div className="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900">Selecao de animais</h3>
                    <p className="text-sm text-gray-600">Todos os animais ativos do lote ja entram marcados para agilizar o manejo.</p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => setSelectedAnimalIds(loteAnimais.map((animal) => animal.id))}
                      className="rounded-xl bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
                    >
                      Selecionar todos
                    </button>
                    <button
                      type="button"
                      onClick={() => setSelectedAnimalIds([])}
                      className="rounded-xl bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
                    >
                      Limpar
                    </button>
                  </div>
                </div>

                {loteAnimais.length === 0 ? (
                  <div className="rounded-2xl border border-dashed border-gray-300 p-6 text-center text-sm text-gray-500">
                    Nenhum animal ativo encontrado neste lote.
                  </div>
                ) : (
                  <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                    {loteAnimais.map((animal) => {
                      const checked = selectedAnimalIds.includes(animal.id);
                      return (
                        <label
                          key={animal.id}
                          className={`flex cursor-pointer items-start gap-3 rounded-2xl border px-4 py-3 ${
                            checked ? 'border-emerald-300 bg-emerald-50' : 'border-gray-200 bg-white'
                          }`}
                        >
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={() => toggleAnimalSelection(animal.id)}
                            className="mt-1 h-4 w-4 text-emerald-600"
                          />
                          <div>
                            <p className="font-semibold text-gray-900">{animal.brinco}</p>
                            <p className="text-sm text-gray-600">
                              {animal.nome || 'Sem nome'} • {animal.categoria.replace(/_/g, ' ')}
                            </p>
                            <p className="text-xs text-gray-500">
                              {animal.pesoAtual == null ? 'Peso nao informado' : `${Number(animal.pesoAtual).toFixed(1)} kg`}
                            </p>
                          </div>
                        </label>
                      );
                    })}
                  </div>
                )}
              </div>

              <div className="flex justify-end gap-3">
                <button type="button" onClick={closeMovementModal} className="btn-secondary">
                  Cancelar
                </button>
                <button type="submit" className="btn-primary" disabled={movingBatch}>
                  {movingBatch ? 'Registrando...' : 'Registrar movimentacao em lote'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
