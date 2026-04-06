import { DollarSign, Plus, TrendingDown, TrendingUp } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import api from '../services/api';
import type {
  CategoriaDespesa,
  DespesaDto,
  FormaPagamentoFinanceira,
  ReceitaDto,
  RegistrarDespesaRequest,
  RegistrarReceitaRequest,
  TipoReceita,
} from '../types';

type AbaFinanceira = 'DESPESAS' | 'RECEITAS';

const formasPagamento: FormaPagamentoFinanceira[] = [
  'DINHEIRO',
  'PIX',
  'CARTAO_CREDITO',
  'CARTAO_DEBITO',
  'BOLETO',
  'TRANSFERENCIA',
  'CHEQUE',
];

const categoriasDespesa: CategoriaDespesa[] = [
  'ALIMENTACAO',
  'VETERINARIO',
  'MEDICAMENTOS',
  'MAO_DE_OBRA',
  'MANUTENCAO',
  'REPRODUCAO',
  'ENERGIA',
  'IMPOSTOS',
  'TRANSPORTE',
  'OUTRAS',
];

const tiposReceita: TipoReceita[] = ['VENDA_ANIMAL', 'VENDA_LOTE', 'PRESTACAO_SERVICO', 'BONIFICACAO', 'OUTRAS'];

export default function Despesas() {
  const [abaAtiva, setAbaAtiva] = useState<AbaFinanceira>('DESPESAS');
  const [despesas, setDespesas] = useState<DespesaDto[]>([]);
  const [receitas, setReceitas] = useState<ReceitaDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [despesaForm, setDespesaForm] = useState<RegistrarDespesaRequest>({
    categoria: 'ALIMENTACAO',
    descricao: '',
    valor: 0,
    data: new Date().toISOString().split('T')[0],
    formaPagamento: 'PIX',
    observacoes: '',
  });

  const [receitaForm, setReceitaForm] = useState<RegistrarReceitaRequest>({
    tipo: 'VENDA_ANIMAL',
    descricao: '',
    valor: 0,
    data: new Date().toISOString().split('T')[0],
    formaPagamento: 'PIX',
    comprador: '',
    quantidadeAnimais: undefined,
    observacoes: '',
  });

  useEffect(() => {
    async function loadFinanceiro() {
      try {
        setLoading(true);
        setError('');
        const [despesasRes, receitasRes] = await Promise.all([
          api.get<DespesaDto[]>('/api/despesas'),
          api.get<ReceitaDto[]>('/api/receitas'),
        ]);
        setDespesas(Array.isArray(despesasRes.data) ? despesasRes.data : []);
        setReceitas(Array.isArray(receitasRes.data) ? receitasRes.data : []);
      } catch (requestError: any) {
        setError(requestError.response?.data?.message || 'Nao foi possivel carregar o financeiro.');
      } finally {
        setLoading(false);
      }
    }

    void loadFinanceiro();
  }, []);

  const totalDespesas = useMemo(
    () => despesas.reduce((sum, item) => sum + Number(item.valor || 0), 0),
    [despesas],
  );
  const totalReceitas = useMemo(
    () => receitas.reduce((sum, item) => sum + Number(item.valor || 0), 0),
    [receitas],
  );

  async function submitDespesa(e: React.FormEvent) {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      await api.post('/api/despesas', {
        ...despesaForm,
        observacoes: despesaForm.observacoes || undefined,
      });
      const response = await api.get<DespesaDto[]>('/api/despesas');
      setDespesas(Array.isArray(response.data) ? response.data : []);
      setDespesaForm({
        categoria: 'ALIMENTACAO',
        descricao: '',
        valor: 0,
        data: new Date().toISOString().split('T')[0],
        formaPagamento: 'PIX',
        observacoes: '',
      });
    } catch (requestError: any) {
      setError(requestError.response?.data?.message || 'Nao foi possivel registrar a despesa.');
    } finally {
      setSaving(false);
    }
  }

  async function submitReceita(e: React.FormEvent) {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      await api.post('/api/receitas', {
        ...receitaForm,
        comprador: receitaForm.comprador || undefined,
        quantidadeAnimais: receitaForm.quantidadeAnimais || undefined,
        observacoes: receitaForm.observacoes || undefined,
      });
      const response = await api.get<ReceitaDto[]>('/api/receitas');
      setReceitas(Array.isArray(response.data) ? response.data : []);
      setReceitaForm({
        tipo: 'VENDA_ANIMAL',
        descricao: '',
        valor: 0,
        data: new Date().toISOString().split('T')[0],
        formaPagamento: 'PIX',
        comprador: '',
        quantidadeAnimais: undefined,
        observacoes: '',
      });
    } catch (requestError: any) {
      setError(requestError.response?.data?.message || 'Nao foi possivel registrar a receita.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <DollarSign className="h-8 w-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Financeiro inicial</h1>
          <p className="text-gray-600">Registro de despesas, receitas e vendas para a primeira camada gerencial da fazenda.</p>
        </div>
      </div>

      {error && <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div>}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-red-100 p-3 text-red-700">
              <TrendingDown className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Total de despesas</p>
              <p className="text-2xl font-bold text-gray-900">R$ {totalDespesas.toFixed(2)}</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-emerald-100 p-3 text-emerald-700">
              <TrendingUp className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Total de receitas</p>
              <p className="text-2xl font-bold text-gray-900">R$ {totalReceitas.toFixed(2)}</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-slate-100 p-3 text-slate-700">
              <DollarSign className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Saldo simples</p>
              <p className="text-2xl font-bold text-gray-900">R$ {(totalReceitas - totalDespesas).toFixed(2)}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="flex flex-wrap gap-2">
          {(['DESPESAS', 'RECEITAS'] as const).map((aba) => (
            <button
              key={aba}
              onClick={() => setAbaAtiva(aba)}
              className={`rounded-full px-4 py-2 text-sm font-medium ${
                abaAtiva === aba ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {aba === 'DESPESAS' ? 'Despesas' : 'Receitas e vendas'}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="card py-12 text-center text-gray-600">Carregando financeiro...</div>
      ) : (
        <div className="grid grid-cols-1 gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <div className="card">
            <div className="mb-4 flex items-center gap-2">
              <Plus className="h-5 w-5 text-primary-600" />
              <h2 className="text-xl font-bold text-gray-900">
                {abaAtiva === 'DESPESAS' ? 'Registrar despesa' : 'Registrar receita'}
              </h2>
            </div>

            {abaAtiva === 'DESPESAS' ? (
              <form onSubmit={submitDespesa} className="space-y-4">
                <div>
                  <label className="form-label">Categoria</label>
                  <select className="form-input" value={despesaForm.categoria} onChange={(e) => setDespesaForm((current) => ({ ...current, categoria: e.target.value as CategoriaDespesa }))}>
                    {categoriasDespesa.map((categoria) => (
                      <option key={categoria} value={categoria}>
                        {categoria.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="form-label">Descricao</label>
                  <input className="form-input" required value={despesaForm.descricao} onChange={(e) => setDespesaForm((current) => ({ ...current, descricao: e.target.value }))} />
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div>
                    <label className="form-label">Valor</label>
                    <input type="number" step="0.01" min="0" className="form-input" required value={despesaForm.valor || ''} onChange={(e) => setDespesaForm((current) => ({ ...current, valor: e.target.value ? Number(e.target.value) : 0 }))} />
                  </div>
                  <div>
                    <label className="form-label">Data</label>
                    <input type="date" className="form-input" required value={despesaForm.data} onChange={(e) => setDespesaForm((current) => ({ ...current, data: e.target.value }))} />
                  </div>
                </div>
                <div>
                  <label className="form-label">Forma de pagamento</label>
                  <select className="form-input" value={despesaForm.formaPagamento} onChange={(e) => setDespesaForm((current) => ({ ...current, formaPagamento: e.target.value as FormaPagamentoFinanceira }))}>
                    {formasPagamento.map((forma) => (
                      <option key={forma} value={forma}>
                        {forma.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="form-label">Observacoes</label>
                  <textarea rows={3} className="form-input" value={despesaForm.observacoes || ''} onChange={(e) => setDespesaForm((current) => ({ ...current, observacoes: e.target.value }))} />
                </div>
                <button type="submit" className="btn-primary w-full" disabled={saving}>
                  {saving ? 'Salvando...' : 'Salvar despesa'}
                </button>
              </form>
            ) : (
              <form onSubmit={submitReceita} className="space-y-4">
                <div>
                  <label className="form-label">Tipo</label>
                  <select className="form-input" value={receitaForm.tipo} onChange={(e) => setReceitaForm((current) => ({ ...current, tipo: e.target.value as TipoReceita }))}>
                    {tiposReceita.map((tipo) => (
                      <option key={tipo} value={tipo}>
                        {tipo.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="form-label">Descricao</label>
                  <input className="form-input" required value={receitaForm.descricao} onChange={(e) => setReceitaForm((current) => ({ ...current, descricao: e.target.value }))} />
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div>
                    <label className="form-label">Valor</label>
                    <input type="number" step="0.01" min="0" className="form-input" required value={receitaForm.valor || ''} onChange={(e) => setReceitaForm((current) => ({ ...current, valor: e.target.value ? Number(e.target.value) : 0 }))} />
                  </div>
                  <div>
                    <label className="form-label">Data</label>
                    <input type="date" className="form-input" required value={receitaForm.data} onChange={(e) => setReceitaForm((current) => ({ ...current, data: e.target.value }))} />
                  </div>
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div>
                    <label className="form-label">Forma de pagamento</label>
                    <select className="form-input" value={receitaForm.formaPagamento} onChange={(e) => setReceitaForm((current) => ({ ...current, formaPagamento: e.target.value as FormaPagamentoFinanceira }))}>
                      {formasPagamento.map((forma) => (
                        <option key={forma} value={forma}>
                          {forma.replace(/_/g, ' ')}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="form-label">Comprador</label>
                    <input className="form-input" value={receitaForm.comprador || ''} onChange={(e) => setReceitaForm((current) => ({ ...current, comprador: e.target.value }))} />
                  </div>
                </div>
                <div>
                  <label className="form-label">Quantidade de animais</label>
                  <input type="number" min="0" className="form-input" value={receitaForm.quantidadeAnimais || ''} onChange={(e) => setReceitaForm((current) => ({ ...current, quantidadeAnimais: e.target.value ? Number(e.target.value) : undefined }))} />
                </div>
                <div>
                  <label className="form-label">Observacoes</label>
                  <textarea rows={3} className="form-input" value={receitaForm.observacoes || ''} onChange={(e) => setReceitaForm((current) => ({ ...current, observacoes: e.target.value }))} />
                </div>
                <button type="submit" className="btn-primary w-full" disabled={saving}>
                  {saving ? 'Salvando...' : 'Salvar receita'}
                </button>
              </form>
            )}
          </div>

          <div className="card">
            <h2 className="mb-4 text-xl font-bold text-gray-900">
              {abaAtiva === 'DESPESAS' ? 'Despesas registradas' : 'Receitas registradas'}
            </h2>

            <div className="space-y-3">
              {abaAtiva === 'DESPESAS' ? (
                despesas.length === 0 ? (
                  <p className="text-sm text-gray-600">Nenhuma despesa registrada ainda.</p>
                ) : (
                  despesas.map((despesa) => (
                    <div key={despesa.id} className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                      <div className="flex items-start justify-between gap-4">
                        <div>
                          <p className="font-semibold text-gray-900">{despesa.descricao}</p>
                          <p className="mt-1 text-sm text-gray-600">{despesa.categoria.replace(/_/g, ' ')}</p>
                          <p className="mt-1 text-xs text-gray-500">{new Date(despesa.data).toLocaleDateString('pt-BR')} • {despesa.formaPagamento.replace(/_/g, ' ')}</p>
                        </div>
                        <p className="text-sm font-bold text-red-600">- R$ {Number(despesa.valor).toFixed(2)}</p>
                      </div>
                    </div>
                  ))
                )
              ) : receitas.length === 0 ? (
                <p className="text-sm text-gray-600">Nenhuma receita registrada ainda.</p>
              ) : (
                receitas.map((receita) => (
                  <div key={receita.id} className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <p className="font-semibold text-gray-900">{receita.descricao}</p>
                        <p className="mt-1 text-sm text-gray-600">{receita.tipo.replace(/_/g, ' ')}</p>
                        <p className="mt-1 text-xs text-gray-500">
                          {new Date(receita.data).toLocaleDateString('pt-BR')} • {receita.formaPagamento.replace(/_/g, ' ')}
                          {receita.comprador ? ` • ${receita.comprador}` : ''}
                        </p>
                      </div>
                      <p className="text-sm font-bold text-emerald-600">+ R$ {Number(receita.valor).toFixed(2)}</p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
