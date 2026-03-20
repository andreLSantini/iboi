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
  'tipoProdu\u00E7\u00E3o': 'CORTE' | 'LEITE' | 'MISTO';
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

export type TipoAssinatura = 'TRIAL' | 'BASIC' | 'PRO' | 'ENTERPRISE';
export type StatusAssinatura = 'TRIAL' | 'ATIVA' | 'VENCIDA' | 'CANCELADA' | 'SUSPENSA';
export type PeriodoPagamento = 'MENSAL' | 'SEMESTRAL' | 'ANUAL';
export type MetodoPagamento = 'CARTAO_CREDITO' | 'BOLETO' | 'PIX' | 'TRANSFERENCIA';

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

export type Sexo = 'MACHO' | 'FEMEA';
export type StatusAnimal = 'ATIVO' | 'VENDIDO' | 'MORTO' | 'DESCARTADO' | 'TRANSFERIDO';
export type CategoriaAnimal = 'BEZERRO' | 'NOVILHO' | 'NOVILHA' | 'BOI' | 'VACA' | 'TOURO' | 'MATRIZ';

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
  nome?: string;
  sexo: Sexo;
  raca: Raca;
  dataNascimento: string;
  idade: number;
  pesoAtual?: number;
  status: StatusAnimal;
  categoria: CategoriaAnimal;
  lote?: {
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
  nome?: string;
  sexo: Sexo;
  raca: Raca;
  dataNascimento: string;
  pesoAtual?: number;
  categoria: CategoriaAnimal;
  loteId?: string;
  paiId?: string;
  maeId?: string;
  observacoes?: string;
}

export interface AtualizarAnimalRequest {
  nome?: string;
  raca?: Raca;
  pesoAtual?: number;
  categoria?: CategoriaAnimal;
  loteId?: string;
  status?: StatusAnimal;
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
