import { DollarSign, Plus } from 'lucide-react';

export default function Despesas() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <DollarSign className="h-8 w-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Despesas</h1>
            <p className="text-gray-600">Camada futura para financeiro detalhado, custos por categoria e leitura por animal.</p>
          </div>
        </div>
        <button className="btn-primary flex items-center gap-2" disabled>
          <Plus className="h-4 w-4" />
          Em breve
        </button>
      </div>

      <div className="card">
        <div className="py-12 text-center">
          <DollarSign className="mx-auto mb-4 h-16 w-16 text-gray-300" />
          <h3 className="mb-2 text-lg font-medium text-gray-900">Financeiro detalhado vem depois</h3>
          <p className="text-gray-600">
            O nucleo operacional e os relatorios simples ja estao ativos. O financeiro detalhado sera liberado nas proximas camadas pagas.
          </p>
        </div>
      </div>
    </div>
  );
}
