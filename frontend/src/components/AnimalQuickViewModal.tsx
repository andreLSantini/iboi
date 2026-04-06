import { Activity, Calendar, GitBranch, Loader2, MapPinned, ShieldPlus, Tag, TrendingUp, X } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import type { AnimalDto, AnimalFichaCompletaDto, EventoDto, MovimentacaoAnimalDto, PesagemAnimalDto, VacinacaoAnimalDto } from '../types';

type Props = {
  animalId: string | null;
  open: boolean;
  onClose: () => void;
};

function InfoBlock({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3">
      <p className="text-xs uppercase tracking-[0.16em] text-gray-500">{label}</p>
      <p className="mt-2 text-sm font-semibold text-gray-900">{value}</p>
    </div>
  );
}

export default function AnimalQuickViewModal({ animalId, open, onClose }: Props) {
  const navigate = useNavigate();
  const [animal, setAnimal] = useState<AnimalDto | null>(null);
  const [pesagens, setPesagens] = useState<PesagemAnimalDto[]>([]);
  const [eventos, setEventos] = useState<EventoDto[]>([]);
  const [vacinacoes, setVacinacoes] = useState<VacinacaoAnimalDto[]>([]);
  const [movimentacoes, setMovimentacoes] = useState<MovimentacaoAnimalDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!open || !animalId) {
      return;
    }

    async function loadAnimalPreview() {
      try {
        setLoading(true);
        setError('');

        const fichaRes = await api.get<AnimalFichaCompletaDto>(`/api/animais/${animalId}/ficha-completa`);

        setAnimal(fichaRes.data.animal);
        setPesagens(Array.isArray(fichaRes.data.pesagens) ? fichaRes.data.pesagens : []);
        setEventos(Array.isArray(fichaRes.data.eventos) ? fichaRes.data.eventos : []);
        setVacinacoes(Array.isArray(fichaRes.data.vacinacoes) ? fichaRes.data.vacinacoes : []);
        setMovimentacoes(Array.isArray(fichaRes.data.movimentacoes) ? fichaRes.data.movimentacoes : []);
      } catch (requestError: any) {
        setError(requestError.response?.data?.message || 'Nao foi possivel carregar a ficha rapida do animal.');
      } finally {
        setLoading(false);
      }
    }

    void loadAnimalPreview();
  }, [animalId, open]);

  const latestVacinacao = vacinacoes[0];
  const latestMovimentacao = movimentacoes[0];

  const gainInsight = useMemo(() => {
    const pesagensOrdenadas = [...pesagens].sort((a, b) => new Date(a.data).getTime() - new Date(b.data).getTime());

    if (pesagensOrdenadas.length < 2) {
      return 'Aguardando base de pesagem';
    }

    const first = pesagensOrdenadas[0];
    const last = pesagensOrdenadas[pesagensOrdenadas.length - 1];
    const diffDays = Math.max(1, Math.round((new Date(last.data).getTime() - new Date(first.data).getTime()) / (1000 * 60 * 60 * 24)));
    const diffWeight = Number(last.peso || 0) - Number(first.peso || 0);
    return `${(diffWeight / diffDays).toFixed(2)} kg/dia`;
  }, [pesagens]);

  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 p-4">
      <div className="max-h-[92vh] w-full max-w-5xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <div className="sticky top-0 z-10 flex items-start justify-between gap-4 border-b border-gray-100 bg-white px-6 py-5">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-primary-600">Ficha rapida do boi</p>
            <h2 className="mt-2 text-2xl font-bold text-gray-900">
              {animal ? `Brinco ${animal.brinco}${animal.nome ? ` - ${animal.nome}` : ''}` : 'Carregando animal'}
            </h2>
            <p className="mt-1 text-sm text-gray-600">Visualizacao dinamica antes de abrir a ficha completa ou exportar relatorio.</p>
          </div>
          <button onClick={onClose} className="rounded-xl p-2 text-gray-500 hover:bg-gray-100">
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="p-6">
          {loading ? (
            <div className="flex h-64 items-center justify-center">
              <Loader2 className="h-10 w-10 animate-spin text-primary-600" />
            </div>
          ) : error ? (
            <div className="rounded-2xl border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-700">{error}</div>
          ) : animal ? (
            <div className="space-y-6">
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
                <InfoBlock label="Categoria" value={animal.categoria} />
                <InfoBlock label="Peso atual" value={animal.pesoAtual ? `${animal.pesoAtual} kg` : 'Nao informado'} />
                <InfoBlock label="Pasto atual" value={animal.pasture?.nome || 'Sem vinculo'} />
                <InfoBlock label="GMD estimado" value={gainInsight} />
              </div>

              <div className="grid grid-cols-1 gap-6 xl:grid-cols-[1.2fr_0.8fr]">
                <div className="space-y-6">
                  <section className="rounded-3xl border border-gray-200 bg-white p-5">
                    <div className="mb-4 flex items-center gap-2">
                      <Tag className="h-5 w-5 text-primary-600" />
                      <h3 className="text-lg font-bold text-gray-900">Dados principais</h3>
                    </div>
                    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                      <InfoBlock label="Nome" value={animal.nome || '-'} />
                      <InfoBlock label="Raca" value={animal.raca} />
                      <InfoBlock label="Sexo" value={animal.sexo} />
                      <InfoBlock label="Status" value={animal.status} />
                      <InfoBlock label="Origem" value={animal.origem === 'NASCIMENTO' ? 'Nascimento' : 'Compra'} />
                      <InfoBlock label="Lote" value={animal.lote?.nome || '-'} />
                      <InfoBlock label="SISBOV" value={animal.codigoSisbov || (animal.sisbovAtivo ? 'Ativo sem codigo' : 'Nao ativo')} />
                      <InfoBlock label="RFID" value={animal.rfid || '-'} />
                      <InfoBlock
                        label="Nascimento"
                        value={new Date(animal.dataNascimento).toLocaleDateString('pt-BR')}
                      />
                    </div>
                    {animal.observacoes && (
                      <div className="mt-4 rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.16em] text-gray-500">Observacoes</p>
                        <p className="mt-2 text-sm text-gray-700">{animal.observacoes}</p>
                      </div>
                    )}
                  </section>

                  <section className="rounded-3xl border border-gray-200 bg-white p-5">
                    <div className="mb-4 flex items-center gap-2">
                      <Activity className="h-5 w-5 text-primary-600" />
                      <h3 className="text-lg font-bold text-gray-900">Timeline recente</h3>
                    </div>
                    <div className="space-y-3">
                      {eventos.slice(0, 5).length === 0 ? (
                        <p className="text-sm text-gray-600">Nenhum evento recente registrado.</p>
                      ) : (
                        eventos.slice(0, 5).map((evento) => (
                          <div key={evento.id} className="rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3">
                            <div className="flex items-center justify-between gap-3">
                              <p className="font-semibold text-gray-900">{evento.tipo.replace(/_/g, ' ')}</p>
                              <span className="text-xs uppercase tracking-[0.12em] text-gray-500">
                                {new Date(evento.data).toLocaleDateString('pt-BR')}
                              </span>
                            </div>
                            <p className="mt-1 text-sm text-gray-700">{evento.descricao}</p>
                            {evento.peso && <p className="mt-1 text-xs text-gray-500">Peso registrado: {evento.peso} kg</p>}
                          </div>
                        ))
                      )}
                    </div>
                  </section>
                </div>

                <div className="space-y-6">
                  <section className="rounded-3xl border border-gray-200 bg-white p-5">
                    <div className="mb-4 flex items-center gap-2">
                      <ShieldPlus className="h-5 w-5 text-primary-600" />
                      <h3 className="text-lg font-bold text-gray-900">Sanidade</h3>
                    </div>
                    <div className="space-y-3">
                      <InfoBlock label="Vacinas registradas" value={String(vacinacoes.length)} />
                      <InfoBlock
                        label="Ultima vacinacao"
                        value={latestVacinacao ? `${latestVacinacao.nomeVacina} em ${new Date(latestVacinacao.aplicadaEm).toLocaleDateString('pt-BR')}` : 'Sem registro'}
                      />
                    </div>
                  </section>

                  <section className="rounded-3xl border border-gray-200 bg-white p-5">
                    <div className="mb-4 flex items-center gap-2">
                      <GitBranch className="h-5 w-5 text-primary-600" />
                      <h3 className="text-lg font-bold text-gray-900">Rastreabilidade</h3>
                    </div>
                    <div className="space-y-3">
                      <InfoBlock label="Movimentacoes" value={String(movimentacoes.length)} />
                      <InfoBlock
                        label="Ultima movimentacao"
                        value={
                          latestMovimentacao
                            ? `${latestMovimentacao.tipo.replace(/_/g, ' ')} em ${new Date(latestMovimentacao.movimentadaEm).toLocaleDateString('pt-BR')}`
                            : 'Sem registro'
                        }
                      />
                    </div>
                  </section>

                  <section className="rounded-3xl border border-gray-200 bg-white p-5">
                    <div className="mb-4 flex items-center gap-2">
                      <MapPinned className="h-5 w-5 text-primary-600" />
                      <h3 className="text-lg font-bold text-gray-900">Localizacao operacional</h3>
                    </div>
                    <div className="space-y-3">
                      <InfoBlock label="Pasto" value={animal.pasture?.nome || 'Sem pasto'} />
                      <InfoBlock label="Lote" value={animal.lote?.nome || 'Sem lote'} />
                      <InfoBlock label="Idade" value={`${animal.idade} meses`} />
                    </div>
                  </section>
                </div>
              </div>

              <div className="flex flex-col gap-3 border-t border-gray-100 pt-5 sm:flex-row sm:justify-end">
                <button onClick={() => navigate(`/app/animais/${animal.id}`)} className="btn-secondary inline-flex items-center justify-center gap-2">
                  <Calendar className="h-4 w-4" />
                  Abrir ficha completa
                </button>
                <button onClick={onClose} className="btn-primary inline-flex items-center justify-center gap-2">
                  <TrendingUp className="h-4 w-4" />
                  Continuar no mapa/lista
                </button>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
