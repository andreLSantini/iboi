import { Building, Edit3, Eye, MapPinned, PlusCircle, Sprout, Tractor, X } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import AnimalQuickViewModal from '../components/AnimalQuickViewModal';
import api from '../services/api';
import { getCurrentFarm, getFarms, storeAuthSession, storeCurrentFarm, storeFarms } from '../services/session';
import type {
  AreaOperacional,
  AnimalDto,
  AtualizarFazendaRequest,
  CadastrarFazendaRequest,
  CadastrarPastoRequest,
  FarmDetail,
  FarmOperacional,
  MultiFarmPortfolio,
  FarmSummary,
  LoginResponse,
  Pasture,
} from '../types';

const markerIcon = new L.Icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const emptyFarmForm: AtualizarFazendaRequest = {
  nome: '',
  cidade: '',
  estado: '',
  tipoProducao: 'CORTE',
  tamanho: undefined,
  ownerName: '',
  ownerDocument: '',
  phone: '',
  email: '',
  addressLine: '',
  zipCode: '',
  latitude: undefined,
  longitude: undefined,
  legalStatus: '',
  documentProof: '',
  ccir: '',
  cib: '',
  car: '',
  mainExploration: '',
  estimatedCapacity: undefined,
  grazingArea: undefined,
  legalReserveArea: undefined,
  appArea: undefined,
  productiveArea: undefined,
};

const emptyPastureForm: CadastrarPastoRequest = {
  nome: '',
  areaHa: undefined,
  latitude: 0,
  longitude: 0,
  geoJson: '',
  notes: '',
};

function Modal({
  open,
  title,
  subtitle,
  onClose,
  children,
}: {
  open: boolean;
  title: string;
  subtitle?: string;
  onClose: () => void;
  children: React.ReactNode;
}) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4">
      <div className="max-h-[90vh] w-full max-w-5xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <div className="sticky top-0 flex items-start justify-between gap-4 border-b border-slate-200 bg-white px-6 py-5">
          <div>
            <h2 className="text-xl font-bold text-slate-900">{title}</h2>
            {subtitle && <p className="mt-1 text-sm text-slate-600">{subtitle}</p>}
          </div>
          <button onClick={onClose} className="rounded-xl p-2 text-slate-500 hover:bg-slate-100">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="px-6 py-6">{children}</div>
      </div>
    </div>
  );
}

function MiniMap({
  latitude,
  longitude,
  label,
  className = 'h-48 w-full',
}: {
  latitude?: number;
  longitude?: number;
  label: string;
  className?: string;
}) {
  if (latitude == null || longitude == null) {
    return (
      <div className={`flex items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-slate-50 text-sm text-slate-500 ${className}`}>
        Coordenadas nao informadas
      </div>
    );
  }

  return (
    <div className={`overflow-hidden rounded-2xl border border-slate-200 ${className}`}>
      <MapContainer center={[latitude, longitude]} zoom={14} scrollWheelZoom={false} className="h-full w-full">
        <TileLayer attribution='&copy; OpenStreetMap contributors' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        <Marker position={[latitude, longitude]} icon={markerIcon}>
          <Popup>{label}</Popup>
        </Marker>
      </MapContainer>
    </div>
  );
}

export default function GestaoFazendas() {
  const [farms, setFarms] = useState<FarmSummary[]>(() => getFarms());
  const [portfolio, setPortfolio] = useState<MultiFarmPortfolio | null>(null);
  const [pastures, setPastures] = useState<Pasture[]>([]);
  const [farmAnimals, setFarmAnimals] = useState<AnimalDto[]>([]);
  const [saving, setSaving] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [message, setMessage] = useState('');
  const [selectedFarmId, setSelectedFarmId] = useState<string | null>(null);
  const [farmDetail, setFarmDetail] = useState<FarmDetail | null>(null);
  const [createFarmForm, setCreateFarmForm] = useState<CadastrarFazendaRequest>({
    nome: '',
    cidade: '',
    estado: '',
    tipoProducao: 'CORTE',
    tamanho: undefined,
  });
  const [editForm, setEditForm] = useState<AtualizarFazendaRequest>(emptyFarmForm);
  const [pastureForm, setPastureForm] = useState<CadastrarPastoRequest>(emptyPastureForm);
  const [createFarmOpen, setCreateFarmOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [pastureModalOpen, setPastureModalOpen] = useState(false);
  const [quickViewAnimalId, setQuickViewAnimalId] = useState<string | null>(null);
  const currentFarm = getCurrentFarm();

  useEffect(() => {
    void carregarFazendas();
  }, []);

  useEffect(() => {
    if (!selectedFarmId && currentFarm?.id) {
      setSelectedFarmId(currentFarm.id);
    }
  }, [currentFarm, selectedFarmId]);

  useEffect(() => {
    if (selectedFarmId) {
      void carregarDetalhes(selectedFarmId);
      void carregarPastos(selectedFarmId);
      void carregarAnimaisDaFazendaSelecionada(selectedFarmId);
    }
  }, [selectedFarmId]);

  const animaisPorPasto = useMemo(() => {
    return farmAnimals.reduce<Record<string, AnimalDto[]>>((acc, animal) => {
      const pastureId = animal.pasture?.id;
      if (!pastureId) {
        return acc;
      }
      if (!acc[pastureId]) {
        acc[pastureId] = [];
      }
      acc[pastureId].push(animal);
      return acc;
    }, {});
  }, [farmAnimals]);

  const fazendaOperacionalSelecionada = useMemo<FarmOperacional | null>(() => {
    if (!selectedFarmId || !portfolio) return null;
    return portfolio.fazendas.find((farm) => farm.id === selectedFarmId) ?? null;
  }, [portfolio, selectedFarmId]);

  const topAreasOperacionais = useMemo<AreaOperacional[]>(() => {
    return (fazendaOperacionalSelecionada?.areasOperacionais ?? [])
      .slice()
      .sort((a, b) => b.animaisAtivos - a.animaisAtivos)
      .slice(0, 4);
  }, [fazendaOperacionalSelecionada]);

  async function carregarFazendas() {
    try {
      const [farmsResponse, portfolioResponse] = await Promise.all([
        api.get<FarmSummary[]>('/api/farms'),
        api.get<MultiFarmPortfolio>('/api/farms/portfolio'),
      ]);
      setFarms(farmsResponse.data);
      setPortfolio(portfolioResponse.data);
      storeFarms(farmsResponse.data);

      if (!selectedFarmId && farmsResponse.data.length > 0) {
        setSelectedFarmId(currentFarm?.id ?? farmsResponse.data[0].id);
      }
    } catch (error) {
      console.error('Erro ao carregar fazendas', error);
    }
  }

  async function carregarDetalhes(farmId: string) {
    setLoadingDetail(true);
    try {
      const response = await api.get<FarmDetail>(`/api/farms/${farmId}`);
      setFarmDetail(response.data);
      setEditForm({
        nome: response.data.name,
        cidade: response.data.city,
        estado: response.data.state,
        tipoProducao: response.data.productionType,
        tamanho: response.data.size,
        ownerName: response.data.ownerName ?? '',
        ownerDocument: response.data.ownerDocument ?? '',
        phone: response.data.phone ?? '',
        email: response.data.email ?? '',
        addressLine: response.data.addressLine ?? '',
        zipCode: response.data.zipCode ?? '',
        latitude: response.data.latitude,
        longitude: response.data.longitude,
        legalStatus: response.data.legalStatus ?? '',
        documentProof: response.data.documentProof ?? '',
        ccir: response.data.ccir ?? '',
        cib: response.data.cib ?? '',
        car: response.data.car ?? '',
        mainExploration: response.data.mainExploration ?? '',
        estimatedCapacity: response.data.estimatedCapacity,
        grazingArea: response.data.grazingArea,
        legalReserveArea: response.data.legalReserveArea,
        appArea: response.data.appArea,
        productiveArea: response.data.productiveArea,
      });
    } catch (error) {
      console.error('Erro ao carregar fazenda', error);
    } finally {
      setLoadingDetail(false);
    }
  }

  async function carregarPastos(farmId: string) {
    try {
      const response = await api.get<Pasture[]>(`/api/farms/${farmId}/pastures`);
      setPastures(response.data);
    } catch (error) {
      console.error('Erro ao carregar pastos', error);
    }
  }

  async function carregarAnimaisDaFazendaSelecionada(farmId: string) {
    if (currentFarm?.id !== farmId) {
      setFarmAnimals([]);
      return;
    }

    try {
      const response = await api.get('/api/animais', {
        params: { size: 500 },
      });
      const data = Array.isArray(response.data) ? response.data : response.data.content || [];
      setFarmAnimals(data);
    } catch (error) {
      console.error('Erro ao carregar animais da fazenda selecionada', error);
      setFarmAnimals([]);
    }
  }

  async function cadastrarFazenda(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setMessage('');

    try {
      await api.post<FarmSummary>('/api/farms', createFarmForm);
      setCreateFarmForm({
        nome: '',
        cidade: '',
        estado: '',
        tipoProducao: 'CORTE',
        tamanho: undefined,
      });
      setCreateFarmOpen(false);
      setMessage('Nova fazenda cadastrada com sucesso.');
      await carregarFazendas();
    } catch (error: any) {
      setMessage(error.response?.data?.message || 'Nao foi possivel cadastrar a fazenda agora.');
    } finally {
      setSaving(false);
    }
  }

  async function salvarEdicao(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedFarmId) return;

    setSaving(true);
    setMessage('');

    try {
      const response = await api.put<FarmDetail>(`/api/farms/${selectedFarmId}`, editForm);
      setFarmDetail(response.data);

      if (currentFarm?.id === selectedFarmId) {
        storeCurrentFarm({
          id: response.data.id,
          nome: response.data.name,
          cidade: response.data.city,
          estado: response.data.state,
        });
      }

      setEditModalOpen(false);
      setMessage('Dados da fazenda atualizados com sucesso.');
      await carregarFazendas();
      await carregarDetalhes(selectedFarmId);
    } catch (error: any) {
      setMessage(error.response?.data?.message || 'Nao foi possivel salvar a fazenda agora.');
    } finally {
      setSaving(false);
    }
  }

  async function cadastrarPasto(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedFarmId) return;

    setSaving(true);
    setMessage('');

    try {
      await api.post<Pasture>(`/api/farms/${selectedFarmId}/pastures`, pastureForm);
      setPastureForm(emptyPastureForm);
      setPastureModalOpen(false);
      setMessage('Pasto cadastrado com geolocalizacao.');
      await carregarPastos(selectedFarmId);
      await carregarFazendas();
    } catch (error: any) {
      setMessage(error.response?.data?.message || 'Nao foi possivel cadastrar o pasto agora.');
    } finally {
      setSaving(false);
    }
  }

  async function trocarParaFazenda(farmId: string) {
    try {
      const response = await api.post<LoginResponse>('/api/farms/select', { farmId });
      storeAuthSession(response.data);
      setSelectedFarmId(farmId);
      window.location.href = '/app/dashboard';
    } catch (error) {
      console.error('Erro ao trocar fazenda', error);
    }
  }

  async function openEditModal(farmId: string) {
    const farmFromList = farms.find((farm) => farm.id === farmId);

    if (farmFromList) {
      setFarmDetail((prev) => ({
        id: farmId,
        name: farmFromList.name,
        city: farmFromList.city ?? prev?.city ?? '',
        state: farmFromList.state ?? prev?.state ?? '',
        productionType: (farmFromList.productionType as FarmDetail['productionType']) ?? prev?.productionType ?? 'CORTE',
        size: farmFromList.size ?? prev?.size,
        ownerName: prev?.ownerName,
        ownerDocument: prev?.ownerDocument,
        phone: prev?.phone,
        email: prev?.email,
        addressLine: prev?.addressLine,
        zipCode: prev?.zipCode,
        latitude: prev?.latitude,
        longitude: prev?.longitude,
        legalStatus: prev?.legalStatus,
        documentProof: prev?.documentProof,
        ccir: prev?.ccir,
        cib: prev?.cib,
        car: prev?.car,
        mainExploration: prev?.mainExploration,
        estimatedCapacity: prev?.estimatedCapacity,
        grazingArea: prev?.grazingArea,
        legalReserveArea: prev?.legalReserveArea,
        appArea: prev?.appArea,
        productiveArea: prev?.productiveArea,
        active: farmFromList.active ?? true,
      }));

      setEditForm((prev) => ({
        ...prev,
        nome: farmFromList.name,
        cidade: farmFromList.city ?? '',
        estado: farmFromList.state ?? '',
        tipoProducao: (farmFromList.productionType as AtualizarFazendaRequest['tipoProducao']) ?? 'CORTE',
        tamanho: farmFromList.size,
      }));
    }

    setSelectedFarmId(farmId);
    await carregarDetalhes(farmId);
    setEditModalOpen(true);
  }

  async function openPastureModal(farmId: string) {
    setSelectedFarmId(farmId);
    await carregarDetalhes(farmId);
    await carregarPastos(farmId);
    setPastureForm(emptyPastureForm);
    setPastureModalOpen(true);
  }

  return (
    <div className="space-y-6">
      <div className="card border-2 border-primary-200 bg-primary-50">
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-start gap-3">
            <Building className="mt-1 h-6 w-6 text-primary-600" />
            <div>
              <h1 className="text-2xl font-bold text-slate-900">Gestao de Fazendas</h1>
              <p className="text-sm text-slate-600">
                Visualize a carteira de fazendas da conta, abra edicao em modal e veja mini mapas da fazenda e dos pastos.
              </p>
            </div>
          </div>
          <button onClick={() => setCreateFarmOpen(true)} className="btn-primary">
            <PlusCircle className="h-4 w-4" />
            Nova fazenda
          </button>
        </div>
      </div>

      {message && (
        <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
          {message}
        </div>
      )}

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="card">
          {portfolio && (
            <div className="mb-5 grid gap-4 md:grid-cols-4">
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Fazendas ativas</p>
                <p className="mt-2 text-2xl font-bold text-slate-900">
                  {portfolio.resumo.fazendasAtivas}/{portfolio.resumo.totalFazendas}
                </p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Animais ativos</p>
                <p className="mt-2 text-2xl font-bold text-slate-900">{portfolio.resumo.totalAnimaisAtivos}</p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Lotes e pastos</p>
                <p className="mt-2 text-2xl font-bold text-slate-900">
                  {portfolio.resumo.totalLotesAtivos} / {portfolio.resumo.totalPastos}
                </p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Area produtiva</p>
                <p className="mt-2 text-2xl font-bold text-slate-900">{portfolio.resumo.areaProdutivaHa.toFixed(1)} ha</p>
              </div>
            </div>
          )}

          <div className="flex items-center gap-3">
            <Tractor className="h-6 w-6 text-primary-600" />
            <div>
              <h2 className="text-lg font-bold text-slate-900">Fazendas da conta</h2>
              <p className="text-sm text-slate-600">Listagem principal com acoes objetivas e leitura operacional por fazenda.</p>
            </div>
          </div>

          <div className="mt-5 overflow-hidden rounded-2xl border border-slate-200">
            <div className="hidden grid-cols-[1.2fr_0.9fr_0.75fr_0.75fr_0.95fr_1.2fr] gap-4 bg-slate-100 px-4 py-3 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500 md:grid">
              <span>Fazenda</span>
              <span>Local</span>
              <span>Producao</span>
              <span>Pastos</span>
              <span>Rebanho</span>
              <span>Acoes</span>
            </div>

            <div className="divide-y divide-slate-200 bg-white">
              {farms.length === 0 ? (
                <div className="px-4 py-8 text-sm text-slate-600">Nenhuma fazenda encontrada.</div>
              ) : (
                farms.map((farm) => (
                  <div key={farm.id} className="grid gap-4 px-4 py-4 md:grid-cols-[1.2fr_0.9fr_0.75fr_0.75fr_0.95fr_1.2fr] md:items-center">
                    <div>
                      <p className="font-semibold text-slate-900">{farm.name}</p>
                      <p className="mt-1 text-xs text-slate-500">
                        {farm.size ? `${farm.size} ha` : 'Area nao informada'}
                      </p>
                      {currentFarm?.id === farm.id && (
                        <span className="mt-2 inline-flex rounded-full bg-primary-100 px-2.5 py-1 text-[11px] font-semibold text-primary-700">
                          Fazenda ativa
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-slate-600">
                      {farm.city ?? 'Cidade'}
                      {farm.state ? `, ${farm.state}` : ''}
                    </p>
                    <p className="text-sm text-slate-600">{farm.productionType ?? 'N/A'}</p>
                    <p className="text-sm text-slate-600">{farm.pastureCount ?? 0}</p>
                    <div className="text-sm text-slate-600">
                      <p>{portfolio?.fazendas.find((item) => item.id === farm.id)?.animaisAtivos ?? 0} ativos</p>
                      <p className="text-xs text-slate-500">
                        {portfolio?.fazendas.find((item) => item.id === farm.id)?.lotesAtivos ?? 0} lotes
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <button onClick={() => void openEditModal(farm.id)} className="btn-secondary">
                        <Edit3 className="h-4 w-4" />
                        Editar
                      </button>
                      <button onClick={() => void openPastureModal(farm.id)} className="btn-secondary">
                        <Sprout className="h-4 w-4" />
                        Pastos
                      </button>
                      {currentFarm?.id !== farm.id && (
                        <button onClick={() => void trocarParaFazenda(farm.id)} className="btn-secondary">
                          Entrar
                        </button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="card">
            <div className="flex items-center gap-3">
              <MapPinned className="h-6 w-6 text-primary-600" />
              <div>
                <h2 className="text-lg font-bold text-slate-900">Contexto atual</h2>
                <p className="text-sm text-slate-600">Resumo da fazenda selecionada com base de areas operacionais.</p>
              </div>
            </div>

            <div className="mt-4 rounded-2xl bg-slate-50 p-4">
              <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Ativa</p>
              <p className="mt-2 text-xl font-bold text-slate-900">{currentFarm?.nome ?? 'Sem fazenda ativa'}</p>
              <p className="mt-1 text-sm text-slate-600">
                {currentFarm ? `${currentFarm.cidade}, ${currentFarm.estado}` : 'Selecione uma fazenda para operar.'}
              </p>
              {fazendaOperacionalSelecionada && (
                <div className="mt-4 grid grid-cols-2 gap-3">
                  <div className="rounded-2xl bg-white p-3">
                    <p className="text-xs uppercase tracking-[0.16em] text-slate-500">Rebanho ativo</p>
                    <p className="mt-2 text-lg font-bold text-slate-900">{fazendaOperacionalSelecionada.animaisAtivos}</p>
                  </div>
                  <div className="rounded-2xl bg-white p-3">
                    <p className="text-xs uppercase tracking-[0.16em] text-slate-500">Ocupacao estimada</p>
                    <p className="mt-2 text-lg font-bold text-slate-900">
                      {fazendaOperacionalSelecionada.taxaOcupacaoEstimada != null
                        ? `${fazendaOperacionalSelecionada.taxaOcupacaoEstimada.toFixed(1)}%`
                        : 'N/A'}
                    </p>
                  </div>
                </div>
              )}
              <div className="mt-4">
                <MiniMap
                  latitude={farmDetail?.latitude}
                  longitude={farmDetail?.longitude}
                  label={farmDetail?.name ?? 'Fazenda'}
                  className="h-52 w-full"
                />
              </div>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center gap-3">
              <Sprout className="h-6 w-6 text-primary-600" />
              <div>
                <h2 className="text-lg font-bold text-slate-900">Pastos da fazenda selecionada</h2>
                <p className="text-sm text-slate-600">Visualizacao rapida com mini mapa e leitura das areas operacionais.</p>
              </div>
            </div>

            {topAreasOperacionais.length > 0 && (
              <div className="mt-4 grid gap-3">
                {topAreasOperacionais.map((area) => (
                  <div key={area.id} className="rounded-2xl border border-slate-200 bg-white p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <p className="font-semibold text-slate-900">{area.nome}</p>
                        <p className="mt-1 text-sm text-slate-600">
                          {area.areaHa ? `${area.areaHa} ha` : 'Area nao informada'} • {area.animaisAtivos} animais ativos
                        </p>
                      </div>
                      <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${area.ativa ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                        {area.ativa ? 'Ativa' : 'Inativa'}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="mt-4 space-y-3">
              {selectedFarmId !== currentFarm?.id && pastures.length > 0 && (
                <div className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
                  Entre na fazenda para ver dinamicamente quantos bois existem em cada pasto e abrir a ficha rapida de cada um.
                </div>
              )}
              {pastures.length === 0 ? (
                <p className="text-sm text-slate-600">Nenhum pasto cadastrado ainda.</p>
              ) : (
                pastures.map((pasture) => (
                  <div key={pasture.id} className="rounded-2xl bg-slate-50 p-4">
                    <p className="font-semibold text-slate-900">{pasture.name}</p>
                    <p className="mt-1 text-sm text-slate-600">
                      {pasture.areaHa ? `${pasture.areaHa} ha` : 'Area nao informada'} | lat {pasture.latitude ?? '-'} | long {pasture.longitude ?? '-'}
                    </p>
                    <p className="mt-1 text-sm font-medium text-primary-700">
                      {selectedFarmId === currentFarm?.id
                        ? `${animaisPorPasto[pasture.id]?.length ?? 0} boi(s) vinculados a este pasto`
                        : 'Troque para esta fazenda para ver os bois deste pasto'}
                    </p>
                    {pasture.notes && <p className="mt-1 text-xs text-slate-500">{pasture.notes}</p>}
                    <div className="mt-3">
                      <MiniMap latitude={pasture.latitude} longitude={pasture.longitude} label={pasture.name} className="h-36 w-full" />
                    </div>
                    {selectedFarmId === currentFarm?.id && (animaisPorPasto[pasture.id]?.length ?? 0) > 0 && (
                      <div className="mt-3 flex flex-wrap gap-2">
                        {animaisPorPasto[pasture.id].slice(0, 6).map((animal) => (
                          <button
                            key={animal.id}
                            onClick={() => setQuickViewAnimalId(animal.id)}
                            className="inline-flex items-center gap-2 rounded-full border border-primary-200 bg-white px-3 py-1.5 text-xs font-medium text-primary-700 hover:bg-primary-50"
                          >
                            <Eye className="h-3.5 w-3.5" />
                            {animal.brinco}
                            {animal.nome ? ` - ${animal.nome}` : ''}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>

      <Modal
        open={createFarmOpen}
        title="Nova fazenda"
        subtitle="Cadastro rapido para adicionar outra operacao na mesma conta."
        onClose={() => setCreateFarmOpen(false)}
      >
        <form onSubmit={cadastrarFazenda} className="space-y-4">
          <input className="input-field" placeholder="Nome da fazenda" value={createFarmForm.nome} onChange={(e) => setCreateFarmForm((prev) => ({ ...prev, nome: e.target.value }))} required />
          <div className="grid grid-cols-2 gap-4">
            <input className="input-field" placeholder="Cidade" value={createFarmForm.cidade} onChange={(e) => setCreateFarmForm((prev) => ({ ...prev, cidade: e.target.value }))} required />
            <input className="input-field" placeholder="UF" maxLength={2} value={createFarmForm.estado} onChange={(e) => setCreateFarmForm((prev) => ({ ...prev, estado: e.target.value.toUpperCase() }))} required />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <select className="input-field" value={createFarmForm.tipoProducao} onChange={(e) => setCreateFarmForm((prev) => ({ ...prev, tipoProducao: e.target.value as CadastrarFazendaRequest['tipoProducao'] }))}>
              <option value="CORTE">Corte</option>
              <option value="LEITE">Leite</option>
              <option value="MISTO">Misto</option>
            </select>
            <input className="input-field" type="number" placeholder="Tamanho em hectares" value={createFarmForm.tamanho ?? ''} onChange={(e) => setCreateFarmForm((prev) => ({ ...prev, tamanho: e.target.value ? Number(e.target.value) : undefined }))} />
          </div>
          <div className="flex justify-end gap-3">
            <button type="button" onClick={() => setCreateFarmOpen(false)} className="btn-secondary">Cancelar</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Salvando...' : 'Cadastrar fazenda'}</button>
          </div>
        </form>
      </Modal>

      <Modal
        open={editModalOpen}
        title={`Editar fazenda${farmDetail ? `: ${farmDetail.name}` : ''}`}
        subtitle="Dados basicos, legais e operacionais em um unico modal de edicao."
        onClose={() => setEditModalOpen(false)}
      >
        <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
          {loadingDetail ? (
            <div className="xl:col-span-2 rounded-2xl bg-slate-50 p-8 text-center text-sm text-slate-600">
              Carregando dados da fazenda...
            </div>
          ) : (
            <>
          <form onSubmit={salvarEdicao} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="Nome" value={editForm.nome} onChange={(e) => setEditForm((prev) => ({ ...prev, nome: e.target.value }))} required />
              <input className="input-field" placeholder="Cidade" value={editForm.cidade} onChange={(e) => setEditForm((prev) => ({ ...prev, cidade: e.target.value }))} required />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="UF" maxLength={2} value={editForm.estado} onChange={(e) => setEditForm((prev) => ({ ...prev, estado: e.target.value.toUpperCase() }))} required />
              <select className="input-field" value={editForm.tipoProducao} onChange={(e) => setEditForm((prev) => ({ ...prev, tipoProducao: e.target.value as AtualizarFazendaRequest['tipoProducao'] }))}>
                <option value="CORTE">Corte</option>
                <option value="LEITE">Leite</option>
                <option value="MISTO">Misto</option>
              </select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="Responsavel" value={editForm.ownerName ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, ownerName: e.target.value }))} />
              <input className="input-field" placeholder="CPF/CNPJ" value={editForm.ownerDocument ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, ownerDocument: e.target.value }))} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="Telefone" value={editForm.phone ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, phone: e.target.value }))} />
              <input className="input-field" placeholder="E-mail" value={editForm.email ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, email: e.target.value }))} />
            </div>
            <input className="input-field" placeholder="Endereco / referencia" value={editForm.addressLine ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, addressLine: e.target.value }))} />
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="CEP" value={editForm.zipCode ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, zipCode: e.target.value }))} />
              <input className="input-field" type="number" placeholder="Area total (ha)" value={editForm.tamanho ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, tamanho: e.target.value ? Number(e.target.value) : undefined }))} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" type="number" step="any" placeholder="Latitude da sede" value={editForm.latitude ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, latitude: e.target.value ? Number(e.target.value) : undefined }))} />
              <input className="input-field" type="number" step="any" placeholder="Longitude da sede" value={editForm.longitude ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, longitude: e.target.value ? Number(e.target.value) : undefined }))} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="CCIR" value={editForm.ccir ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, ccir: e.target.value }))} />
              <input className="input-field" placeholder="CIB" value={editForm.cib ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, cib: e.target.value }))} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="CAR" value={editForm.car ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, car: e.target.value }))} />
              <input className="input-field" placeholder="Situacao juridica" value={editForm.legalStatus ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, legalStatus: e.target.value }))} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" placeholder="Exploracao principal" value={editForm.mainExploration ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, mainExploration: e.target.value }))} />
              <input className="input-field" type="number" placeholder="Capacidade estimada" value={editForm.estimatedCapacity ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, estimatedCapacity: e.target.value ? Number(e.target.value) : undefined }))} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" type="number" placeholder="Area de pasto (ha)" value={editForm.grazingArea ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, grazingArea: e.target.value ? Number(e.target.value) : undefined }))} />
              <input className="input-field" type="number" placeholder="Reserva legal (ha)" value={editForm.legalReserveArea ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, legalReserveArea: e.target.value ? Number(e.target.value) : undefined }))} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <input className="input-field" type="number" placeholder="APP (ha)" value={editForm.appArea ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, appArea: e.target.value ? Number(e.target.value) : undefined }))} />
              <input className="input-field" type="number" placeholder="Area produtiva (ha)" value={editForm.productiveArea ?? ''} onChange={(e) => setEditForm((prev) => ({ ...prev, productiveArea: e.target.value ? Number(e.target.value) : undefined }))} />
            </div>
            <div className="flex justify-end gap-3">
              <button type="button" onClick={() => setEditModalOpen(false)} className="btn-secondary">Cancelar</button>
              <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Salvando...' : 'Salvar fazenda'}</button>
            </div>
          </form>

          <div className="space-y-4">
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Mini mapa da fazenda</p>
              <p className="mt-2 text-sm text-slate-600">Visualizacao da sede com base na latitude e longitude cadastradas.</p>
            </div>
            <MiniMap latitude={editForm.latitude} longitude={editForm.longitude} label={farmDetail?.name ?? 'Fazenda'} className="h-80 w-full" />
          </div>
            </>
          )}
        </div>
      </Modal>

      <Modal
        open={pastureModalOpen}
        title={`Pastos${farmDetail ? ` de ${farmDetail.name}` : ''}`}
        subtitle="Cadastre o pasto com geolocalizacao e, se quiser, o contorno em GeoJSON."
        onClose={() => setPastureModalOpen(false)}
      >
        <div className="grid gap-6 xl:grid-cols-[1fr_1fr]">
          <div className="space-y-6">
            <div className="space-y-3">
              {pastures.length === 0 ? (
                <p className="text-sm text-slate-600">Nenhum pasto cadastrado ainda.</p>
              ) : (
                pastures.map((pasture) => (
                  <div key={pasture.id} className="rounded-2xl bg-slate-50 p-4">
                    <p className="font-semibold text-slate-900">{pasture.name}</p>
                    <p className="mt-1 text-sm text-slate-600">
                      {pasture.areaHa ? `${pasture.areaHa} ha` : 'Area nao informada'} | lat {pasture.latitude ?? '-'} | long {pasture.longitude ?? '-'}
                    </p>
                    <p className="mt-1 text-sm font-medium text-primary-700">
                      {selectedFarmId === currentFarm?.id
                        ? `${animaisPorPasto[pasture.id]?.length ?? 0} boi(s) neste pasto`
                        : 'Entre na fazenda para listar os bois deste pasto'}
                    </p>
                    {pasture.notes && <p className="mt-1 text-xs text-slate-500">{pasture.notes}</p>}
                    <div className="mt-3">
                      <MiniMap latitude={pasture.latitude} longitude={pasture.longitude} label={pasture.name} className="h-36 w-full" />
                    </div>
                    {selectedFarmId === currentFarm?.id && (animaisPorPasto[pasture.id]?.length ?? 0) > 0 && (
                      <div className="mt-3 flex flex-wrap gap-2">
                        {animaisPorPasto[pasture.id].map((animal) => (
                          <button
                            key={animal.id}
                            type="button"
                            onClick={() => setQuickViewAnimalId(animal.id)}
                            className="inline-flex items-center gap-2 rounded-full border border-primary-200 bg-white px-3 py-1.5 text-xs font-medium text-primary-700 hover:bg-primary-50"
                          >
                            <Eye className="h-3.5 w-3.5" />
                            {animal.brinco}
                            {animal.nome ? ` - ${animal.nome}` : ''}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="space-y-6">
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-slate-500">Novo pasto</p>
              <p className="mt-2 text-sm text-slate-600">Cadastre o ponto atual e depois podemos evoluir para contorno e lotacao.</p>
            </div>

            <MiniMap latitude={pastureForm.latitude || undefined} longitude={pastureForm.longitude || undefined} label={pastureForm.nome || 'Novo pasto'} className="h-72 w-full" />

            <form onSubmit={cadastrarPasto} className="space-y-4 border-t border-slate-200 pt-5">
              <input className="input-field" placeholder="Nome do pasto" value={pastureForm.nome} onChange={(e) => setPastureForm((prev) => ({ ...prev, nome: e.target.value }))} required />
              <div className="grid grid-cols-3 gap-4">
                <input className="input-field" type="number" placeholder="Area (ha)" value={pastureForm.areaHa ?? ''} onChange={(e) => setPastureForm((prev) => ({ ...prev, areaHa: e.target.value ? Number(e.target.value) : undefined }))} />
                <input className="input-field" type="number" step="any" placeholder="Latitude" value={pastureForm.latitude || ''} onChange={(e) => setPastureForm((prev) => ({ ...prev, latitude: Number(e.target.value) }))} required />
                <input className="input-field" type="number" step="any" placeholder="Longitude" value={pastureForm.longitude || ''} onChange={(e) => setPastureForm((prev) => ({ ...prev, longitude: Number(e.target.value) }))} required />
              </div>
              <textarea className="input-field min-h-24" placeholder="GeoJSON do contorno do pasto (opcional)" value={pastureForm.geoJson ?? ''} onChange={(e) => setPastureForm((prev) => ({ ...prev, geoJson: e.target.value }))} />
              <input className="input-field" placeholder="Observacoes" value={pastureForm.notes ?? ''} onChange={(e) => setPastureForm((prev) => ({ ...prev, notes: e.target.value }))} />
              <div className="flex justify-end gap-3">
                <button type="button" onClick={() => setPastureModalOpen(false)} className="btn-secondary">Fechar</button>
                <button type="submit" disabled={saving || !selectedFarmId} className="btn-primary">{saving ? 'Salvando...' : 'Cadastrar pasto'}</button>
              </div>
            </form>
          </div>
        </div>
      </Modal>

      <AnimalQuickViewModal
        animalId={quickViewAnimalId}
        open={Boolean(quickViewAnimalId)}
        onClose={() => setQuickViewAnimalId(null)}
      />
    </div>
  );
}
