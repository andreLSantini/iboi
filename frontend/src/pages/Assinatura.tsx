import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, CheckCircle2, Copy, CreditCard, ExternalLink, Loader2, QrCode, Sparkles, Building2 } from 'lucide-react';
import api from '../services/api';
import { clearSubscriptionReason, getSubscriptionReason } from '../services/session';
import type {
  AssinaturaDto,
  EmpresaDto,
  HistoricoPagamento,
  MetodoPagamento,
  PeriodoPagamento,
  PlanoRecurso,
  TipoAssinatura,
} from '../types';

const planos: Array<{
  tipo: TipoAssinatura;
  titulo: string;
  subtitulo: string;
  preco: Record<PeriodoPagamento, string>;
  recursos: PlanoRecurso[];
}> = [
  {
    tipo: 'TRIAL',
    titulo: 'Trial',
    subtitulo: 'Mesmo nucleo operacional do Basic por 30 dias, com limite inicial de ate 50 animais.',
    preco: { MENSAL: '30 dias', SEMESTRAL: '30 dias', ANUAL: '30 dias' },
    recursos: ['CADASTRO_COMPLETO', 'PESAGEM', 'VACINACAO', 'MOVIMENTACAO', 'RELATORIOS'],
  },
  {
    tipo: 'BASIC',
    titulo: 'Basic',
    subtitulo: 'Operacao completa com manejo, sanidade e relatorios simples.',
    preco: { MENSAL: 'R$ 79', SEMESTRAL: 'R$ 426,60', ANUAL: 'R$ 758,40' },
    recursos: ['CADASTRO_COMPLETO', 'PESAGEM', 'VACINACAO', 'MOVIMENTACAO', 'RELATORIOS'],
  },
];

const planosFuturos: Array<{
  tipo: TipoAssinatura;
  titulo: string;
  subtitulo: string;
  recursos: PlanoRecurso[];
}> = [
  {
    tipo: 'PRO',
    titulo: 'Pro',
    subtitulo: 'Em breve: leitura economica, financeiro por animal e custo por cabeca.',
    recursos: ['FINANCEIRO_POR_ANIMAL', 'CUSTO_POR_CABECA'],
  },
  {
    tipo: 'PREMIUM',
    titulo: 'Premium',
    subtitulo: 'Em breve: previsoes, score do animal e inteligencia de decisao.',
    recursos: ['IA_DECISAO'],
  },
];

const recursoLabel: Record<PlanoRecurso, string> = {
  CADASTRO_BASICO: 'Cadastro basico',
  CADASTRO_COMPLETO: 'Cadastro completo',
  PESAGEM: 'Pesagem',
  VACINACAO: 'Vacinacao',
  MOVIMENTACAO: 'Movimentacao',
  RELATORIOS: 'Relatorios',
  FINANCEIRO_POR_ANIMAL: 'Financeiro por animal',
  CUSTO_POR_CABECA: 'Custo por cabeca',
  IA_DECISAO: 'IA decisoria',
};

export default function Assinatura() {
  const [assinatura, setAssinatura] = useState<AssinaturaDto | null>(null);
  const [historico, setHistorico] = useState<HistoricoPagamento[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [periodo, setPeriodo] = useState<PeriodoPagamento>('MENSAL');
  const [metodo, setMetodo] = useState<MetodoPagamento>('PIX');
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [cobrancaAtual, setCobrancaAtual] = useState<HistoricoPagamento | null>(null);
  const [empresa, setEmpresa] = useState<EmpresaDto | null>(null);
  const [empresaForm, setEmpresaForm] = useState({ nome: '', cnpj: '' });

  const bloqueado = getSubscriptionReason() === 'SUBSCRIPTION_INACTIVE' || assinatura?.status === 'VENCIDA';
  const acessoPreservado = Boolean(assinatura && assinatura.diasRestantes > 0);

  useEffect(() => {
    void carregar();
  }, []);

  async function carregar() {
    try {
      const [assinaturaRes, historicoRes, empresaRes] = await Promise.all([
        api.get<AssinaturaDto>('/api/assinatura/minha'),
        api.get<HistoricoPagamento[]>('/api/pagamento/historico'),
        api.get<EmpresaDto>('/api/empresa/minha'),
      ]);

      setAssinatura(assinaturaRes.data);
      setHistorico(historicoRes.data);
      setCobrancaAtual(historicoRes.data.find((item) => item.status === 'PENDENTE') ?? null);
      setEmpresa(empresaRes.data);
      setEmpresaForm({
        nome: empresaRes.data.nome ?? '',
        cnpj: empresaRes.data.cnpj ?? '',
      });

      if (assinaturaRes.data.status === 'TRIAL' || assinaturaRes.data.status === 'ATIVA') {
        clearSubscriptionReason();
      }
    } catch (error) {
      console.error('Erro ao carregar assinatura', error);
    } finally {
      setLoading(false);
    }
  }

  async function upgrade(novoPlano: TipoAssinatura) {
    setSaving(true);
    setMensagem(null);

    try {
      await api.post('/api/assinatura/upgrade', { novoPlano, periodo });
      await carregar();
      setMensagem('Plano selecionado. Gere a cobranca no Asaas para ativar a nova camada.');
    } catch (error: any) {
      setMensagem(error.response?.data?.message || 'Nao foi possivel atualizar o plano agora.');
    } finally {
      setSaving(false);
    }
  }

  async function gerarCobranca() {
    setSaving(true);
    setMensagem(null);

    try {
      const response = await api.post('/api/pagamento/processar', { metodoPagamento: metodo });
      const pagamento = response.data.pagamento as HistoricoPagamento | undefined;
      await carregar();
      setCobrancaAtual(pagamento ?? null);
      setMensagem('Cobranca criada no Asaas. Use o link, boleto ou QR code abaixo para concluir.');
    } catch (error: any) {
      setMensagem(error.response?.data?.message || 'Nao foi possivel gerar a cobranca agora.');
    } finally {
      setSaving(false);
    }
  }

  async function copiarPix() {
    if (!cobrancaAtual?.pixPayload) return;
    await navigator.clipboard.writeText(cobrancaAtual.pixPayload);
    setMensagem('Codigo PIX copiado.');
  }

  async function salvarEmpresa() {
    setSaving(true);
    setMensagem(null);

    try {
      const response = await api.put<EmpresaDto>('/api/empresa/minha', {
        nome: empresaForm.nome,
        cnpj: empresaForm.cnpj || null,
      });
      setEmpresa(response.data);
      setEmpresaForm({
        nome: response.data.nome ?? '',
        cnpj: response.data.cnpj ?? '',
      });
      setMensagem('Dados da empresa atualizados. Tente gerar a cobranca novamente.');
    } catch (error: any) {
      setMensagem(error.response?.data?.message || 'Nao foi possivel atualizar os dados da empresa.');
    } finally {
      setSaving(false);
    }
  }

  const recursosAtivos = useMemo(() => assinatura?.recursos ?? [], [assinatura]);
  const usoAnimais = assinatura?.limiteAnimais ? `${assinatura.animaisCadastrados}/${assinatura.limiteAnimais}` : `${assinatura?.animaisCadastrados ?? 0}`;

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-10 w-10 animate-spin text-primary-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className={`card border-2 ${bloqueado ? 'border-amber-300 bg-amber-50' : 'border-primary-200 bg-primary-50'}`}>
        <div className="flex items-start gap-3">
          {bloqueado ? <AlertTriangle className="mt-1 h-6 w-6 text-amber-600" /> : <CheckCircle2 className="mt-1 h-6 w-6 text-primary-600" />}
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Assinatura e monetizacao</h1>
            <p className="text-sm text-slate-600">
              {bloqueado
                ? 'Seu acesso pago esta pendente. Regularize a cobranca para liberar o plano contratado.'
                : 'Hoje vendemos apenas o que ja esta redondo: Trial e Basic. Pro e Premium aparecem como proximas camadas.'}
            </p>
          </div>
        </div>
      </div>

      {assinatura && acessoPreservado && (
        <div className="rounded-2xl border border-sky-200 bg-sky-50 px-4 py-4 text-sm text-sky-900 shadow-sm">
          <p className="font-semibold">Seu periodo atual esta preservado.</p>
          <p className="mt-1">
            Se voce gerar a renovacao agora, o BovCore mantem os {assinatura.diasRestantes} dias restantes e soma o novo ciclo a partir do vencimento atual.
          </p>
        </div>
      )}

      {bloqueado && (
        <div className="rounded-2xl border border-amber-200 bg-white px-4 py-4 text-sm text-slate-700 shadow-sm">
          <p className="font-semibold text-slate-900">Como regularizar agora</p>
          <div className="mt-2 space-y-1">
            <p>1. Confira nome e CPF/CNPJ da empresa abaixo.</p>
            <p>2. Selecione o plano e o metodo de pagamento.</p>
            <p>3. Gere a cobranca e conclua o pagamento no Asaas.</p>
            <p>4. O webhook ativa a licenca automaticamente quando o pagamento for confirmado.</p>
          </div>
        </div>
      )}

      {mensagem && <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">{mensagem}</div>}

      <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <div className="space-y-6">
          <div className="card">
            <h2 className="text-lg font-bold text-slate-900">Plano atual</h2>
            <div className="mt-5 grid gap-4 md:grid-cols-4">
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Plano</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{assinatura?.tituloPlano ?? assinatura?.tipo ?? 'N/A'}</p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Status</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{assinatura?.status ?? 'N/A'}</p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Animais</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{usoAnimais}</p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Dias restantes</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{assinatura?.diasRestantes ?? 0}</p>
              </div>
            </div>
            <p className="mt-4 text-sm text-slate-600">{assinatura?.descricaoPlano}</p>

            <div className="mt-5 flex flex-wrap gap-2">
              {recursosAtivos.map((recurso) => (
                <span key={recurso} className="rounded-full bg-primary-100 px-3 py-1 text-xs font-medium text-primary-700">
                  {recursoLabel[recurso]}
                </span>
              ))}
            </div>
          </div>

          <div className="card">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-lg font-bold text-slate-900">Planos disponiveis agora</h2>
                <p className="text-sm text-slate-600">O foco comercial neste momento e o nucleo operacional que ja esta pronto para uso real.</p>
              </div>
              <Sparkles className="h-6 w-6 text-primary-600" />
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              {(['MENSAL', 'SEMESTRAL', 'ANUAL'] as PeriodoPagamento[]).map((item) => (
                <button key={item} onClick={() => setPeriodo(item)} className={periodo === item ? 'btn-primary' : 'btn-secondary'}>
                  {item}
                </button>
              ))}
            </div>

            <div className="mt-6 grid gap-4 lg:grid-cols-3">
              {planos.map((plano) => (
                <div key={plano.tipo} className={`rounded-3xl border p-5 ${assinatura?.tipo === plano.tipo ? 'border-primary-400 bg-primary-50' : 'border-slate-200 bg-slate-50'}`}>
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-500">{plano.tipo}</p>
                  <h3 className="mt-2 text-xl font-bold text-slate-900">{plano.titulo}</h3>
                  <p className="mt-1 text-2xl font-bold text-slate-900">{plano.preco[periodo]}</p>
                  <p className="mt-2 text-sm text-slate-600">{plano.subtitulo}</p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {plano.recursos.map((recurso) => (
                      <span key={recurso} className="rounded-full bg-white px-3 py-1 text-xs text-slate-700">
                        {recursoLabel[recurso]}
                      </span>
                    ))}
                  </div>
                  <button onClick={() => void upgrade(plano.tipo)} disabled={saving || assinatura?.tipo === plano.tipo || plano.tipo === 'TRIAL'} className="btn-primary mt-5 w-full">
                    {assinatura?.tipo === plano.tipo ? 'Plano atual' : 'Selecionar plano'}
                  </button>
                </div>
              ))}
            </div>

            <div className="mt-8">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <h3 className="text-base font-bold text-slate-900">Camadas futuras</h3>
                  <p className="text-sm text-slate-600">Mostramos o caminho do produto, mas sem vender o que ainda nao esta pronto.</p>
                </div>
                <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-amber-800">
                  em breve
                </span>
              </div>

              <div className="mt-4 grid gap-4 lg:grid-cols-2">
                {planosFuturos.map((plano) => (
                  <div key={plano.tipo} className="rounded-3xl border border-dashed border-slate-300 bg-white p-5">
                    <p className="text-xs uppercase tracking-[0.2em] text-slate-500">{plano.tipo}</p>
                    <h4 className="mt-2 text-xl font-bold text-slate-900">{plano.titulo}</h4>
                    <p className="mt-2 text-sm text-slate-600">{plano.subtitulo}</p>
                    <div className="mt-4 flex flex-wrap gap-2">
                      {plano.recursos.map((recurso) => (
                        <span key={recurso} className="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-700">
                          {recursoLabel[recurso]}
                        </span>
                      ))}
                    </div>
                    <button disabled className="btn-secondary mt-5 w-full cursor-not-allowed opacity-70">
                      Disponivel em breve
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="card">
            <div className="flex items-center gap-3">
              <Building2 className="h-6 w-6 text-primary-600" />
              <div>
                <h2 className="text-lg font-bold text-slate-900">Dados da empresa para cobranca</h2>
                <p className="text-sm text-slate-600">Se o Asaas recusar o CPF/CNPJ, corrija aqui e tente novamente.</p>
              </div>
            </div>

            <div className="mt-5 grid gap-4">
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Nome da empresa</label>
                <input
                  className="input-field"
                  value={empresaForm.nome}
                  onChange={(e) => setEmpresaForm((prev) => ({ ...prev, nome: e.target.value }))}
                  placeholder="Nome da empresa"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">CPF ou CNPJ</label>
                <input
                  className="input-field"
                  value={empresaForm.cnpj}
                  onChange={(e) => setEmpresaForm((prev) => ({ ...prev, cnpj: e.target.value }))}
                  placeholder="Somente numeros ou formatado"
                />
              </div>

              <div className="rounded-2xl bg-slate-50 p-4 text-sm text-slate-600">
                <p>Cliente Asaas atual: <strong>{empresa?.asaasCustomerId ?? 'ainda nao criado'}</strong></p>
                <p className="mt-1">Use um CPF ou CNPJ valido. Se o documento estiver incorreto, o Asaas recusa a cobranca.</p>
              </div>

              <button onClick={() => void salvarEmpresa()} disabled={saving} className="btn-secondary">
                {saving ? 'Salvando...' : 'Salvar dados da empresa'}
              </button>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center gap-3">
              <CreditCard className="h-6 w-6 text-primary-600" />
              <div>
                <h2 className="text-lg font-bold text-slate-900">Cobranca Asaas</h2>
                <p className="text-sm text-slate-600">PIX e boleto prontos para o Basic. Cartao e planos futuros ficam para a proxima camada.</p>
              </div>
            </div>

            <div className="mt-5 grid gap-2">
              {(['PIX', 'BOLETO'] as MetodoPagamento[]).map((item) => (
                <button key={item} onClick={() => setMetodo(item)} className={metodo === item ? 'btn-primary text-left' : 'btn-secondary text-left'}>
                  {item}
                </button>
              ))}
            </div>

            <button onClick={() => void gerarCobranca()} disabled={saving} className="btn-primary mt-5 w-full">
              {saving ? 'Gerando...' : 'Gerar cobranca'}
            </button>

            <p className="mt-3 text-xs leading-5 text-slate-500">
              A cobranca recorrente e criada no Asaas respeitando o seu periodo atual. Se ainda restam dias no plano ou trial, eles nao sao perdidos.
            </p>
          </div>

          {cobrancaAtual && (
            <div className="card">
              <div className="flex items-center gap-3">
                <QrCode className="h-6 w-6 text-primary-600" />
                <div>
                  <h2 className="text-lg font-bold text-slate-900">Cobranca atual</h2>
                  <p className="text-sm text-slate-600">Status {cobrancaAtual.status} via {cobrancaAtual.gatewayProvider ?? 'gateway'}.</p>
                </div>
              </div>

              <div className="mt-4 space-y-3">
                {cobrancaAtual.invoiceUrl && (
                  <a href={cobrancaAtual.invoiceUrl} target="_blank" rel="noreferrer" className="btn-secondary flex items-center justify-center gap-2">
                    Abrir cobranca
                    <ExternalLink className="h-4 w-4" />
                  </a>
                )}

                {cobrancaAtual.bankSlipUrl && (
                  <a href={cobrancaAtual.bankSlipUrl} target="_blank" rel="noreferrer" className="btn-secondary flex items-center justify-center gap-2">
                    Abrir boleto
                    <ExternalLink className="h-4 w-4" />
                  </a>
                )}

                {cobrancaAtual.pixEncodedImage && (
                  <img src={`data:image/png;base64,${cobrancaAtual.pixEncodedImage}`} alt="QR code PIX" className="mx-auto w-56 rounded-2xl border border-slate-200 bg-white p-3" />
                )}

                {cobrancaAtual.pixPayload && (
                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Codigo PIX</p>
                    <p className="mt-2 break-all text-sm text-slate-700">{cobrancaAtual.pixPayload}</p>
                    <button onClick={() => void copiarPix()} className="btn-primary mt-4 flex w-full items-center justify-center gap-2">
                      Copiar PIX
                      <Copy className="h-4 w-4" />
                    </button>
                  </div>
                )}
              </div>
            </div>
          )}

          <div className="card">
            <h2 className="text-lg font-bold text-slate-900">Historico</h2>
            <div className="mt-4 space-y-3">
              {historico.length === 0 ? (
                <p className="text-sm text-slate-600">Nenhum pagamento registrado.</p>
              ) : (
                historico.map((pagamento) => (
                  <div key={pagamento.id} className="rounded-2xl bg-slate-50 p-4">
                    <p className="font-semibold text-slate-900">{pagamento.status}</p>
                    <p className="text-sm text-slate-600">
                      R$ {Number(pagamento.valor).toFixed(2)} • {pagamento.metodoPagamento ?? 'N/A'}
                    </p>
                    <p className="text-xs text-slate-500">{pagamento.gatewayProvider ?? 'sem gateway'} • {pagamento.transacaoId ?? 'sem transacao'}</p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
