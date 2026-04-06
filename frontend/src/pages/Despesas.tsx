import { CalendarRange, DollarSign, Plus, TrendingDown, TrendingUp, Wallet } from 'lucide-react';
import { useEffect, useState } from 'react';
import api from '../services/api';
import type {
  CategoriaDespesa,
  DespesaDto,
  FluxoCaixaDto,
  FormaPagamentoFinanceira,
  ReceitaDto,
  RegistrarDespesaRequest,
  RegistrarReceitaRequest,
  StatusLancamentoFinanceiro,
  TipoReceita,
} from '../types';

type AbaFinanceira = 'DESPESAS' | 'RECEITAS' | 'FLUXO';

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
const statusDespesa: StatusLancamentoFinanceiro[] = ['PENDENTE', 'PAGO', 'VENCIDO'];
const statusReceita: StatusLancamentoFinanceiro[] = ['PENDENTE', 'RECEBIDO', 'VENCIDO'];

function hojeIso() {
  return new Date().toISOString().split('T')[0];
}

function inicioMesIso() {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
}

function fimMesIso() {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0];
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value || 0));
}

function formatDate(value?: string) {
  if (!value) return '-';
  return new Date(`${value}T00:00:00`).toLocaleDateString('pt-BR');
}

function statusClass(status: StatusLancamentoFinanceiro) {
  if (status === 'PAGO' || status === 'RECEBIDO') return 'bg-emerald-100 text-emerald-700';
  if (status === 'VENCIDO') return 'bg-red-100 text-red-700';
  return 'bg-amber-100 text-amber-700';
}

export default function Despesas() {
  const [abaAtiva, setAbaAtiva] = useState<AbaFinanceira>('FLUXO');
  const [despesas, setDespesas] = useState<DespesaDto[]>([]);
  const [receitas, setReceitas] = useState<ReceitaDto[]>([]);
  const [fluxo, setFluxo] = useState<FluxoCaixaDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [filtros, setFiltros] = useState({
    dataInicio: inicioMesIso(),
    dataFim: fimMesIso(),
  });

  const [despesaForm, setDespesaForm] = useState<RegistrarDespesaRequest>({
    categoria: 'ALIMENTACAO',
    descricao: '',
    valor: 0,
    data: hojeIso(),
    dataVencimento: hojeIso(),
    status: 'PENDENTE',
    formaPagamento: 'PIX',
    observacoes: '',
  });

  const [receitaForm, setReceitaForm] = useState<RegistrarReceitaRequest>({
    tipo: 'VENDA_ANIMAL',
    descricao: '',
    valor: 0,
    data: hojeIso(),
    dataVencimento: hojeIso(),
    status: 'PENDENTE',
    formaPagamento: 'PIX',
    comprador: '',
    quantidadeAnimais: undefined,
    observacoes: '',
  });

  async function carregarFinanceiro() {
    try {
      setLoading(true);
      setError('');
      const [despesasRes, receitasRes, fluxoRes] = await Promise.all([
        api.get<DespesaDto[]>('/api/despesas', { params: filtros }),
        api.get<ReceitaDto[]>('/api/receitas', { params: filtros }),
        api.get<FluxoCaixaDto>('/api/financeiro/fluxo-caixa', { params: filtros }),
      ]);
      setDespesas(Array.isArray(despesasRes.data) ? despesasRes.data : []);
      setReceitas(Array.isArray(receitasRes.data) ? receitasRes.data : []);
      setFluxo(fluxoRes.data ?? null);
    } catch (requestError: any) {
      setError(requestError.response?.data?.message || 'Nao foi possivel carregar o financeiro.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void carregarFinanceiro();
  }, [filtros.dataFim, filtros.dataInicio]);

  async function submitDespesa(e: React.FormEvent) {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      await api.post('/api/despesas', {
        ...despesaForm,
        dataVencimento: despesaForm.dataVencimento || despesaForm.data,
        dataLiquidacao: despesaForm.status === 'PAGO' ? (despesaForm.dataLiquidacao || despesaForm.data) : undefined,
        observacoes: despesaForm.observacoes || undefined,
      });
      await carregarFinanceiro();
      const hoje = hojeIso();
      setDespesaForm({
        categoria: 'ALIMENTACAO',
        descricao: '',
        valor: 0,
        data: hoje,
        dataVencimento: hoje,
        status: 'PENDENTE',
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
        dataVencimento: receitaForm.dataVencimento || receitaForm.data,
        dataLiquidacao: receitaForm.status === 'RECEBIDO' ? (receitaForm.dataLiquidacao || receitaForm.data) : undefined,
        comprador: receitaForm.comprador || undefined,
        quantidadeAnimais: receitaForm.quantidadeAnimais || undefined,
        observacoes: receitaForm.observacoes || undefined,
      });
      await carregarFinanceiro();
      const hoje = hojeIso();
      setReceitaForm({
        tipo: 'VENDA_ANIMAL',
        descricao: '',
        valor: 0,
        data: hoje,
        dataVencimento: hoje,
        status: 'PENDENTE',
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

  const resumo = fluxo?.resumo;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Wallet className="h-8 w-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Fluxo de caixa</h1>
          <p className="text-gray-600">Contas a pagar, contas a receber e visao consolidada do caixa da fazenda.</p>
        </div>
      </div>

      {error && <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div>}

      <div className="card">
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div>
            <label className="form-label">Data inicial</label>
            <input type="date" className="form-input" value={filtros.dataInicio} onChange={(e) => setFiltros((current) => ({ ...current, dataInicio: e.target.value }))} />
          </div>
          <div>
            <label className="form-label">Data final</label>
            <input type="date" className="form-input" value={filtros.dataFim} onChange={(e) => setFiltros((current) => ({ ...current, dataFim: e.target.value }))} />
          </div>
          <div className="flex items-end">
            <button type="button" className="btn-secondary w-full" onClick={() => void carregarFinanceiro()}>
              Atualizar periodo
            </button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-emerald-100 p-3 text-emerald-700">
              <TrendingUp className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Saldo realizado</p>
              <p className="text-2xl font-bold text-gray-900">{formatCurrency(Number(resumo?.saldoRealizado || 0))}</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-sky-100 p-3 text-sky-700">
              <CalendarRange className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Saldo projetado</p>
              <p className="text-2xl font-bold text-gray-900">{formatCurrency(Number(resumo?.saldoProjetado || 0))}</p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-amber-100 p-3 text-amber-700">
              <TrendingDown className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Pendente no periodo</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(Number((resumo?.totalPendenteReceber || 0) - (resumo?.totalPendentePagar || 0)))}
              </p>
            </div>
          </div>
        </div>
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-red-100 p-3 text-red-700">
              <DollarSign className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Titulos vencidos</p>
              <p className="text-2xl font-bold text-gray-900">{formatCurrency(Number(resumo?.totalVencido || 0))}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="flex flex-wrap gap-2">
          {(['FLUXO', 'DESPESAS', 'RECEITAS'] as const).map((aba) => (
            <button
              key={aba}
              onClick={() => setAbaAtiva(aba)}
              className={`rounded-full px-4 py-2 text-sm font-medium ${
                abaAtiva === aba ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {aba === 'FLUXO' ? 'Fluxo de caixa' : aba === 'DESPESAS' ? 'Contas a pagar' : 'Contas a receber'}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="card py-12 text-center text-gray-600">Carregando financeiro...</div>
      ) : abaAtiva === 'FLUXO' ? (
        <div className="card">
          <div className="mb-4 flex items-center justify-between gap-4">
            <div>
              <h2 className="text-xl font-bold text-gray-900">Movimentos do caixa</h2>
              <p className="text-sm text-gray-600">Lista consolidada por vencimento, com entradas e saidas do periodo.</p>
            </div>
            <div className="text-right text-sm text-gray-600">
              <p>Receber previsto: {formatCurrency(Number(resumo?.totalPrevistoReceber || 0))}</p>
              <p>Pagar previsto: {formatCurrency(Number(resumo?.totalPrevistoPagar || 0))}</p>
            </div>
          </div>

          <div className="space-y-3">
            {fluxo?.movimentos.length ? (
              fluxo.movimentos.map((movimento) => (
                <div key={`${movimento.tipo}-${movimento.id}`} className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                  <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <p className="font-semibold text-gray-900">{movimento.descricao}</p>
                        <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(movimento.status)}`}>
                          {movimento.status.replace(/_/g, ' ')}
                        </span>
                      </div>
                      <p className="text-sm text-gray-600">
                        {movimento.tipo === 'ENTRADA' ? 'Receita' : 'Despesa'} • {movimento.origem.replace(/_/g, ' ')}
                      </p>
                      <p className="text-xs text-gray-500">
                        Competencia {formatDate(movimento.dataCompetencia)} • Vencimento {formatDate(movimento.dataVencimento)} •{' '}
                        {movimento.dataLiquidacao ? `Liquidacao ${formatDate(movimento.dataLiquidacao)}` : 'Aguardando liquidacao'}
                      </p>
                      <p className="text-xs text-gray-500">
                        {movimento.formaPagamento.replace(/_/g, ' ')}
                        {movimento.contraparte ? ` • ${movimento.contraparte}` : ''}
                      </p>
                    </div>
                    <p className={`text-sm font-bold ${movimento.tipo === 'ENTRADA' ? 'text-emerald-600' : 'text-red-600'}`}>
                      {movimento.tipo === 'ENTRADA' ? '+ ' : '- '}
                      {formatCurrency(Number(movimento.valor))}
                    </p>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-sm text-gray-600">Nenhum movimento encontrado para o periodo selecionado.</p>
            )}
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <div className="card">
            <div className="mb-4 flex items-center gap-2">
              <Plus className="h-5 w-5 text-primary-600" />
              <h2 className="text-xl font-bold text-gray-900">
                {abaAtiva === 'DESPESAS' ? 'Registrar conta a pagar' : 'Registrar conta a receber'}
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
                    <label className="form-label">Data da competencia</label>
                    <input type="date" className="form-input" required value={despesaForm.data} onChange={(e) => setDespesaForm((current) => ({ ...current, data: e.target.value }))} />
                  </div>
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div>
                    <label className="form-label">Vencimento</label>
                    <input type="date" className="form-input" required value={despesaForm.dataVencimento || ''} onChange={(e) => setDespesaForm((current) => ({ ...current, dataVencimento: e.target.value }))} />
                  </div>
                  <div>
                    <label className="form-label">Status</label>
                    <select className="form-input" value={despesaForm.status || 'PENDENTE'} onChange={(e) => setDespesaForm((current) => ({ ...current, status: e.target.value as StatusLancamentoFinanceiro }))}>
                      {statusDespesa.map((status) => (
                        <option key={status} value={status}>
                          {status.replace(/_/g, ' ')}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                {despesaForm.status === 'PAGO' && (
                  <div>
                    <label className="form-label">Data do pagamento</label>
                    <input type="date" className="form-input" value={despesaForm.dataLiquidacao || despesaForm.data} onChange={(e) => setDespesaForm((current) => ({ ...current, dataLiquidacao: e.target.value }))} />
                  </div>
                )}
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
                  {saving ? 'Salvando...' : 'Salvar conta a pagar'}
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
                    <label className="form-label">Data da competencia</label>
                    <input type="date" className="form-input" required value={receitaForm.data} onChange={(e) => setReceitaForm((current) => ({ ...current, data: e.target.value }))} />
                  </div>
                </div>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <div>
                    <label className="form-label">Vencimento</label>
                    <input type="date" className="form-input" required value={receitaForm.dataVencimento || ''} onChange={(e) => setReceitaForm((current) => ({ ...current, dataVencimento: e.target.value }))} />
                  </div>
                  <div>
                    <label className="form-label">Status</label>
                    <select className="form-input" value={receitaForm.status || 'PENDENTE'} onChange={(e) => setReceitaForm((current) => ({ ...current, status: e.target.value as StatusLancamentoFinanceiro }))}>
                      {statusReceita.map((status) => (
                        <option key={status} value={status}>
                          {status.replace(/_/g, ' ')}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                {receitaForm.status === 'RECEBIDO' && (
                  <div>
                    <label className="form-label">Data do recebimento</label>
                    <input type="date" className="form-input" value={receitaForm.dataLiquidacao || receitaForm.data} onChange={(e) => setReceitaForm((current) => ({ ...current, dataLiquidacao: e.target.value }))} />
                  </div>
                )}
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
                  {saving ? 'Salvando...' : 'Salvar conta a receber'}
                </button>
              </form>
            )}
          </div>

          <div className="card">
            <h2 className="mb-4 text-xl font-bold text-gray-900">
              {abaAtiva === 'DESPESAS' ? 'Contas a pagar do periodo' : 'Contas a receber do periodo'}
            </h2>

            <div className="space-y-3">
              {abaAtiva === 'DESPESAS' ? (
                despesas.length === 0 ? (
                  <p className="text-sm text-gray-600">Nenhuma conta a pagar encontrada no periodo.</p>
                ) : (
                  despesas.map((despesa) => (
                    <div key={despesa.id} className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                      <div className="flex items-start justify-between gap-4">
                        <div>
                          <div className="flex flex-wrap items-center gap-2">
                            <p className="font-semibold text-gray-900">{despesa.descricao}</p>
                            <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(despesa.status)}`}>
                              {despesa.status.replace(/_/g, ' ')}
                            </span>
                          </div>
                          <p className="mt-1 text-sm text-gray-600">{despesa.categoria.replace(/_/g, ' ')}</p>
                          <p className="mt-1 text-xs text-gray-500">
                            Competencia {formatDate(despesa.data)} • Vencimento {formatDate(despesa.dataVencimento)} •{' '}
                            {despesa.dataLiquidacao ? `Pagamento ${formatDate(despesa.dataLiquidacao)}` : 'Nao liquidada'}
                          </p>
                        </div>
                        <p className="text-sm font-bold text-red-600">- {formatCurrency(Number(despesa.valor))}</p>
                      </div>
                    </div>
                  ))
                )
              ) : receitas.length === 0 ? (
                <p className="text-sm text-gray-600">Nenhuma conta a receber encontrada no periodo.</p>
              ) : (
                receitas.map((receita) => (
                  <div key={receita.id} className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="flex flex-wrap items-center gap-2">
                          <p className="font-semibold text-gray-900">{receita.descricao}</p>
                          <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusClass(receita.status)}`}>
                            {receita.status.replace(/_/g, ' ')}
                          </span>
                        </div>
                        <p className="mt-1 text-sm text-gray-600">{receita.tipo.replace(/_/g, ' ')}</p>
                        <p className="mt-1 text-xs text-gray-500">
                          Competencia {formatDate(receita.data)} • Vencimento {formatDate(receita.dataVencimento)} •{' '}
                          {receita.dataLiquidacao ? `Recebimento ${formatDate(receita.dataLiquidacao)}` : 'Nao recebida'}
                        </p>
                        <p className="mt-1 text-xs text-gray-500">
                          {receita.formaPagamento.replace(/_/g, ' ')}
                          {receita.comprador ? ` • ${receita.comprador}` : ''}
                        </p>
                      </div>
                      <p className="text-sm font-bold text-emerald-600">+ {formatCurrency(Number(receita.valor))}</p>
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
