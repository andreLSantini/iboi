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
  tipoProdução: 'CORTE' | 'LEITE' | 'MISTO';
  tamanho?: number;
}

export interface OnboardingResponse {
  accessToken: string;
  usuario: {
    id: string;
    nome: string;
    email: string;
    role: string;
    farmRole: string;
  };
  fazenda: {
    id: string;
    nome: string;
    cidade: string;
    estado: string;
  };
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken: string;
  usuario: {
    id: string;
    nome: string;
    email: string;
  };
}

// ============= ANIMAIS =============
export type Sexo = 'MACHO' | 'FEMEA';

export type StatusAnimal = 'ATIVO' | 'VENDIDO' | 'MORTO' | 'DESCARTADO' | 'TRANSFERIDO';

export type CategoriaAnimal =
  | 'BEZERRO'
  | 'NOVILHO'
  | 'NOVILHA'
  | 'BOI'
  | 'VACA'
  | 'TOURO'
  | 'MATRIZ';

export type Raca =
  | 'NELORE'
  | 'ANGUS'
  | 'BRAHMAN'
  | 'CARACU'
  | 'GUZERA'
  | 'TABAPUA'
  | 'SENEPOL'
  | 'CHAROLÊS'
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

// ============= EVENTOS =============
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
