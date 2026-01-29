import { DollarSign, Plus } from 'lucide-react';

export default function Despesas() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <DollarSign className="w-8 h-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Despesas</h1>
            <p className="text-gray-600">Controle financeiro da fazenda</p>
          </div>
        </div>
        <button className="btn-primary flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Nova Despesa
        </button>
      </div>

      <div className="card">
        <div className="text-center py-12">
          <DollarSign className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Funcionalidade em Construção
          </h3>
          <p className="text-gray-600">
            O controle de despesas estará disponível em breve.
          </p>
        </div>
      </div>
    </div>
  );
}
