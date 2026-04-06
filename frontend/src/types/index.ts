export interface OnboardingRequest {
  nome: string;
  email: string;
  telefone?: string;
  senha: string;
  nomeEmpresa: string;
  tipoEmpresa: 'MATRIZ' | 'FILIAL';
  cnpj?: string;
  nomeFazenda: string;
  cidade: string;
  estado: string;
  tipoProducao: 'CORTE' | 'LEITE' | 'MISTO';
  tamanho?: number;
}

export interface UserSession {
  id: string;
  nome: string;
  email: string;
  role: string;
  farmRole: string;
}

export interface FarmSession {
  id: string;
  nome: string;
  cidade: string;
  estado: string;
}

export interface FarmSummary {
  id: string;
  name: string;
  city?: string;
  state?: string;
  productionType?: string;
  size?: number;
  active?: boolean;
  pastureCount?: number;
}

export interface CadastrarFazendaRequest {
  nome: string;
  cidade: string;
  estado: string;
  tipoProducao: 'CORTE' | 'LEITE' | 'MISTO';
  tamanho?: number;
}

export interface AtualizarFazendaRequest extends CadastrarFazendaRequest {
  ownerName?: string;
  ownerDocument?: string;
  phone?: string;
  email?: string;
  addressLine?: string;
  zipCode?: string;
  latitude?: number;
  longitude?: number;
  legalStatus?: string;
  documentProof?: string;
  ccir?: string;
  cib?: string;
  car?: string;
  mainExploration?: string;
  estimatedCapacity?: number;
  grazingArea?: number;
  legalReserveArea?: number;
  appArea?: number;
  productiveArea?: number;
}

export interface FarmDetail {
  id: string;
  name: string;
  city: string;
  state: string;
  productionType: 'CORTE' | 'LEITE' | 'MISTO';
  size?: number;
  ownerName?: string;
  ownerDocument?: string;
  phone?: string;
  email?: string;
  addressLine?: string;
  zipCode?: string;
  latitude?: number;
  longitude?: number;
  legalStatus?: string;
  documentProof?: string;
  ccir?: string;
  cib?: string;
  car?: string;
  mainExploration?: string;
  estimatedCapacity?: number;
  grazingArea?: number;
  legalReserveArea?: number;
  appArea?: number;
  productiveArea?: number;
  active: boolean;
}

export interface Pasture {
  id: string;
  name: string;
  areaHa?: number;
  latitude?: number;
  longitude?: number;
  geoJson?: string;
  notes?: string;
  active: boolean;
}

export interface CadastrarPastoRequest {
  nome: string;
  areaHa?: number;
  latitude: number;
  longitude: number;
  geoJson?: string;
  notes?: string;
}

export interface AuthResponse {
  accessToken: string;
  usuario: UserSession;
  fazenda: FarmSession;
  farms: FarmSummary[];
  defaultFarmId: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export type LoginResponse = AuthResponse;
export type OnboardingResponse = AuthResponse;

export type TipoAssinatura = 'TRIAL' | 'FREE' | 'BASIC' | 'PRO' | 'PREMIUM' | 'ENTERPRISE';
export type StatusAssinatura = 'TRIAL' | 'ATIVA' | 'VENCIDA' | 'CANCELADA' | 'SUSPENSA';
export type PeriodoPagamento = 'MENSAL' | 'SEMESTRAL' | 'ANUAL';
export type MetodoPagamento = 'CARTAO_CREDITO' | 'BOLETO' | 'PIX' | 'TRANSFERENCIA';
export type PlanoRecurso =
  | 'CADASTRO_BASICO'
  | 'CADASTRO_COMPLETO'
  | 'PESAGEM'
  | 'VACINACAO'
  | 'MOVIMENTACAO'
  | 'RELATORIOS'
  | 'FINANCEIRO_POR_ANIMAL'
  | 'CUSTO_POR_CABECA'
  | 'IA_DECISAO';

export interface AssinaturaDto {
  id: string;
  tipo: TipoAssinatura;
  status: StatusAssinatura;
  periodoPagamento?: PeriodoPagamento;
  dataInicio: string;
  dataVencimento: string;
  proximaCobranca?: string;
  valor?: number;
  diasRestantes: number;
  tituloPlano: string;
  descricaoPlano: string;
  limiteAnimais?: number;
  animaisCadastrados: number;
  recursos: PlanoRecurso[];
}

export interface EmpresaDto {
  id: string;
  nome: string;
  tipo: 'MATRIZ' | 'FILIAL';
  cnpj?: string;
  asaasCustomerId?: string;
  ativa: boolean;
}

export interface HistoricoPagamento {
  id: string;
  valor: number;
  dataVencimento: string;
  dataPagamento?: string;
  status: string;
  metodoPagamento?: MetodoPagamento;
  transacaoId?: string;
  gatewayProvider?: string;
  invoiceUrl?: string;
  bankSlipUrl?: string;
  pixPayload?: string;
  pixEncodedImage?: string;
}

export interface DashboardResponse {
  kpis: {
    totalAnimaisAtivos: number;
    nascimentosMes: number;
    mortesMes: number;
    despesasMes: number;
    animaisPorCategoria: Record<string, number>;
  };
  eventosRecentes: {
    data: string;
    tipo: string;
    animal: string;
    descricao: string;
  }[];
  agendamentosProximos: {
    dataPrevista: string;
    tipo: string;
    animal: string;
    produto: string;
  }[];
}

export interface RelatorioRebanhoResponse {
  totalAnimais: number;
  porCategoria: Record<string, number>;
  porSexo: Record<string, number>;
  porStatus: Record<string, number>;
  idadeMediaMeses: number;
  pesoMedio?: number | null;
}

export type PrioridadeAlerta = 'BAIXA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type StatusAlertaIA = 'ATIVO' | 'LIDO' | 'RESOLVIDO';

export interface AlertaInteligenteDto {
  id: string;
  tipo: string;
  prioridade: PrioridadeAlerta;
  titulo: string;
  mensagem: string;
  animal?: {
    id: string;
    brinco: string;
    nome?: string;
  };
  recomendacao?: string;
  status: StatusAlertaIA;
  criadoEm: string;
}

export interface DashboardInteligenteResponse {
  animaisRiscoAlto: {
    animalId: string;
    brinco: string;
    nome?: string;
    scoreRisco: number;
    fatoresRisco: string;
    nivel: string;
  }[];
  predicoesPeso: {
    animalId: string;
    brinco: string;
    pesoAtual?: number | null;
    pesoPrevistoEm90Dias: number;
  }[];
  alertasCriticos: number;
  recomendacoesIA: {
    contexto: string;
    recomendacao: string;
  }[];
  scoreRiscoMedio: number;
}

export type Sexo = 'MACHO' | 'FEMEA';
export type StatusAnimal = 'ATIVO' | 'VENDIDO' | 'MORTO' | 'DESCARTADO' | 'TRANSFERIDO';
export type CategoriaAnimal = 'BEZERRO' | 'NOVILHO' | 'NOVILHA' | 'BOI' | 'VACA' | 'TOURO' | 'MATRIZ';
export type OrigemAnimal = 'NASCIMENTO' | 'COMPRA';

export type Raca =
  | 'NELORE'
  | 'ANGUS'
  | 'BRAHMAN'
  | 'CARACU'
  | 'GUZERA'
  | 'TABAPUA'
  | 'SENEPOL'
  | 'CHAROL\u00CAS'
  | 'LIMOUSIN'
  | 'HEREFORD'
  | 'SIMENTAL'
  | 'CRUZAMENTO_INDUSTRIAL'
  | 'MESTICO'
  | 'OUTRAS';

export interface AnimalDto {
  id: string;
  brinco: string;
  rfid?: string;
  codigoSisbov?: string;
  sisbovAtivo: boolean;
  nome?: string;
  sexo: Sexo;
  raca: Raca;
  dataNascimento: string;
  dataEntrada?: string;
  idade: number;
  pesoAtual?: number;
  status: StatusAnimal;
  categoria: CategoriaAnimal;
  origem: OrigemAnimal;
  lote?: {
    id: string;
    nome: string;
  };
  pasture?: {
    id: string;
    nome: string;
  };
  pai?: {
    id: string;
    brinco: string;
    nome?: string;
  };
  mae?: {
    id: string;
    brinco: string;
    nome?: string;
  };
  observacoes?: string;
}

export interface CadastrarAnimalRequest {
  brinco: string;
  rfid?: string;
  codigoSisbov?: string;
  nome?: string;
  sexo: Sexo;
  raca: Raca;
  dataNascimento: string;
  pesoAtual?: number;
  categoria: CategoriaAnimal;
  origem: OrigemAnimal;
  loteId?: string;
  pastureId?: string;
  paiId?: string;
  maeId?: string;
  dataEntrada?: string;
  sisbovAtivo?: boolean;
  observacoes?: string;
}

export interface AtualizarAnimalRequest {
  rfid?: string;
  codigoSisbov?: string;
  nome?: string;
  raca?: Raca;
  pesoAtual?: number;
  categoria?: CategoriaAnimal;
  origem?: OrigemAnimal;
  loteId?: string;
  pastureId?: string;
  status?: StatusAnimal;
  dataEntrada?: string;
  sisbovAtivo?: boolean;
  observacoes?: string;
}

export interface ImportarAnimaisResponse {
  totalLinhas: number;
  importados: number;
  ignorados: number;
  erros: string[];
}

export type TipoMovimentacaoAnimal =
  | 'ENTRE_PASTOS'
  | 'ENTRE_FAZENDAS'
  | 'SAIDA_EXTERNA'
  | 'ENTRADA_EXTERNA';

export type TipoVacina =
  | 'AFTOSA'
  | 'BRUCELOSE'
  | 'CLOSTRIDIOSE'
  | 'RAIVA'
  | 'LEPTOSE'
  | 'IBR_BVD'
  | 'OUTRA';

export interface MovimentacaoAnimalDto {
  id: string;
  tipo: TipoMovimentacaoAnimal;
  movimentadaEm: string;
  farmOrigem?: { id: string; nome: string };
  farmDestino?: { id: string; nome: string };
  pastureOrigem?: { id: string; nome: string };
  pastureDestino?: { id: string; nome: string };
  numeroGta?: string;
  documentoExterno?: string;
  motivo?: string;
  observacoes?: string;
  responsavel?: string;
}

export interface RegistrarMovimentacaoAnimalRequest {
  tipo: TipoMovimentacaoAnimal;
  movimentadaEm: string;
  destinoFarmId?: string;
  destinoPastureId?: string;
  numeroGta?: string;
  documentoExterno?: string;
  motivo?: string;
  observacoes?: string;
}

export interface VacinacaoAnimalDto {
  id: string;
  tipo: TipoVacina;
  nomeVacina: string;
  dose?: number;
  unidadeMedida?: string;
  aplicadaEm: string;
  proximaDoseEm?: string;
  fabricante?: string;
  loteVacina?: string;
  observacoes?: string;
  responsavel?: string;
}

export interface RegistrarVacinacaoAnimalRequest {
  tipo: TipoVacina;
  nomeVacina: string;
  dose?: number;
  unidadeMedida?: string;
  aplicadaEm: string;
  proximaDoseEm?: string;
  fabricante?: string;
  loteVacina?: string;
  observacoes?: string;
}

export type TipoEvento =
  | 'VACINA'
  | 'VERMIFUGO'
  | 'PESAGEM'
  | 'MOVIMENTACAO'
  | 'NASCIMENTO'
  | 'DESMAME'
  | 'MORTE'
  | 'VENDA'
  | 'COMPRA'
  | 'TRATAMENTO'
  | 'INSEMINACAO'
  | 'COBERTURA'
  | 'PARTO'
  | 'DIAGNOSTICO_GESTACAO'
  | 'DESCARTE'
  | 'OBSERVACAO';

export interface EventoDto {
  id: string;
  animal: {
    id: string;
    brinco: string;
    nome?: string;
  };
  tipo: TipoEvento;
  data: string;
  descricao: string;
  peso?: number;
  produto?: string;
  dose?: number;
  unidadeMedida?: string;
  loteDestino?: {
    id: string;
    nome: string;
  };
  valor?: number;
  responsavel?: string;
}

export interface RegistrarEventoRequest {
  animalId: string;
  tipo: TipoEvento;
  data: string;
  descricao: string;
  peso?: number;
  produto?: string;
  dose?: number;
  unidadeMedida?: string;
  loteDestinoId?: string;
  valor?: number;
}

export interface LoteDto {
  id: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
  quantidadeAnimais: number;
  criadoEm: string;
}

export interface CadastrarLoteRequest {
  nome: string;
  descricao?: string;
}

export interface AtualizarLoteRequest {
  nome?: string;
  descricao?: string;
  ativo?: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
