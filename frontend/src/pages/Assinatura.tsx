import { useEffect, useState } from 'react';
import { AlertTriangle, CheckCircle2, Copy, CreditCard, ExternalLink, Loader2, QrCode } from 'lucide-react';
import api from '../services/api';
import { getSubscriptionReason } from '../services/session';
import type { AssinaturaDto, HistoricoPagamento, MetodoPagamento, PeriodoPagamento, TipoAssinatura } from '../types';

const planos: Array<{ tipo: TipoAssinatura; titulo: string; descricao: string }> = [
  { tipo: 'BASIC', titulo: 'Basic', descricao: 'Plano enxuto para operacoes menores.' },
  { tipo: 'PRO', titulo: 'Pro', descricao: 'Mais capacidade para escalar o uso.' },
  { tipo: 'ENTERPRISE', titulo: 'Enterprise', descricao: 'Camada premium para contas maiores.' },
];

export default function Assinatura() {
  const [assinatura, setAssinatura] = useState<AssinaturaDto | null>(null);
  const [historico, setHistorico] = useState<HistoricoPagamento[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [periodo, setPeriodo] = useState<PeriodoPagamento>('MENSAL');
  const [metodo, setMetodo] = useState<MetodoPagamento>('PIX');
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [cobrancaAtual, setCobrancaAtual] = useState<HistoricoPagamento | null>(null);

  const bloqueado = getSubscriptionReason() === 'SUBSCRIPTION_INACTIVE' || assinatura?.status === 'VENCIDA';

  useEffect(() => {
    void carregar();
  }, []);

  async function carregar() {
    try {
      const [assinaturaRes, historicoRes] = await Promise.all([
        api.get<AssinaturaDto>('/api/assinatura/minha'),
        api.get<HistoricoPagamento[]>('/api/pagamento/historico'),
      ]);

      const historicoOrdenado = historicoRes.data;
      setAssinatura(assinaturaRes.data);
      setHistorico(historicoOrdenado);
      setCobrancaAtual(historicoOrdenado.find((item) => item.status === 'PENDENTE') ?? null);
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
      setMensagem('Plano atualizado. Agora gere a cobranca no Asaas para concluir o upgrade.');
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
      setMensagem('Cobranca criada no Asaas. Use o link ou QR code abaixo para pagar.');
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
          {bloqueado ? (
            <AlertTriangle className="mt-1 h-6 w-6 text-amber-600" />
          ) : (
            <CheckCircle2 className="mt-1 h-6 w-6 text-primary-600" />
          )}
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Assinatura</h1>
            <p className="text-sm text-slate-600">
              {bloqueado
                ? 'Seu acesso operacional depende da regularizacao do plano.'
                : 'Billing conectado ao Asaas para emissao de cobrancas e reconciliacao por webhook.'}
            </p>
          </div>
        </div>
      </div>

      {mensagem && <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">{mensagem}</div>}

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="space-y-6">
          <div className="card">
            <h2 className="text-lg font-bold text-slate-900">Resumo atual</h2>
            <div className="mt-5 grid gap-4 md:grid-cols-3">
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Plano</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{assinatura?.tipo ?? 'N/A'}</p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Status</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{assinatura?.status ?? 'N/A'}</p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Dias restantes</p>
                <p className="mt-2 text-xl font-bold text-slate-900">{assinatura?.diasRestantes ?? 0}</p>
              </div>
            </div>
          </div>

          <div className="card">
            <h2 className="text-lg font-bold text-slate-900">Escolha um plano</h2>
            <div className="mt-4 flex flex-wrap gap-2">
              {(['MENSAL', 'SEMESTRAL', 'ANUAL'] as PeriodoPagamento[]).map((item) => (
                <button key={item} onClick={() => setPeriodo(item)} className={periodo === item ? 'btn-primary' : 'btn-secondary'}>
                  {item}
                </button>
              ))}
            </div>

            <div className="mt-6 grid gap-4 lg:grid-cols-3">
              {planos.map((plano) => (
                <div key={plano.tipo} className="rounded-3xl border border-slate-200 bg-slate-50 p-5">
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-500">{plano.tipo}</p>
                  <h3 className="mt-2 text-xl font-bold text-slate-900">{plano.titulo}</h3>
                  <p className="mt-2 text-sm text-slate-600">{plano.descricao}</p>
                  <button onClick={() => void upgrade(plano.tipo)} disabled={saving} className="btn-primary mt-5 w-full">
                    Selecionar plano
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="card">
            <div className="flex items-center gap-3">
              <CreditCard className="h-6 w-6 text-primary-600" />
              <div>
                <h2 className="text-lg font-bold text-slate-900">Gerar cobranca no Asaas</h2>
                <p className="text-sm text-slate-600">PIX e boleto estao prontos. Cartao fica para a proxima camada de checkout.</p>
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
                  <img
                    src={`data:image/png;base64,${cobrancaAtual.pixEncodedImage}`}
                    alt="QR code PIX"
                    className="mx-auto w-56 rounded-2xl border border-slate-200 bg-white p-3"
                  />
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
