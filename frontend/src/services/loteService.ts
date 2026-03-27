import api from './api';
import type { LoteDto, CadastrarLoteRequest, AtualizarLoteRequest, PageResponse } from '../types/index';

export const loteService = {
  listar: async (apenasAtivos?: boolean, page = 0, size = 20): Promise<PageResponse<LoteDto>> => {
    const params: any = { page, size };
    if (apenasAtivos !== undefined) {
      params.apenasAtivos = apenasAtivos;
    }
    const response = await api.get<PageResponse<LoteDto>>('/api/lotes', { params });
    return response.data;
  },

  buscarPorId: async (id: string): Promise<LoteDto> => {
    const response = await api.get<LoteDto>(`/api/lotes/${id}`);
    return response.data;
  },

  cadastrar: async (data: CadastrarLoteRequest): Promise<LoteDto> => {
    const response = await api.post<LoteDto>('/api/lotes', data);
    return response.data;
  },

  atualizar: async (id: string, data: AtualizarLoteRequest): Promise<LoteDto> => {
    const response = await api.put<LoteDto>(`/api/lotes/${id}`, data);
    return response.data;
  },

  deletar: async (id: string): Promise<void> => {
    await api.delete(`/api/lotes/${id}`);
  }
};
