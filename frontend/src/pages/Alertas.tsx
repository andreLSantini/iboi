import { AlertCircle } from 'lucide-react';

export default function Alertas() {
  const alertas = [
    {
      id: 1,
      titulo: 'Vacina Atrasada',
      descricao: '3 animais com vacinas pendentes há mais de 7 dias',
      prioridade: 'CRITICA',
      tipo: 'SANITARIO',
      data: '2026-01-29',
    },
    {
      id: 2,
      titulo: 'Peso Abaixo da Média',
      descricao: 'Brinco #1240 - Peso 30% abaixo da média do lote',
      prioridade: 'ALTA',
      tipo: 'PRODUTIVO',
      data: '2026-01-28',
    },
    {
      id: 3,
      titulo: 'Pesagem Pendente',
      descricao: '5 animais sem pesagem há mais de 90 dias',
      prioridade: 'MEDIA',
      tipo: 'SANITARIO',
      data: '2026-01-27',
    },
  ];

  const getPrioridadeColor = (prioridade: string) => {
    switch (prioridade) {
      case 'CRITICA':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'ALTA':
        return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'MEDIA':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      default:
        return 'bg-blue-100 text-blue-800 border-blue-200';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <AlertCircle className="w-8 h-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Alertas IA</h1>
          <p className="text-gray-600">Detecção inteligente de problemas</p>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {[
          { label: 'Críticos', value: 1, color: 'text-red-600' },
          { label: 'Altos', value: 1, color: 'text-orange-600' },
          { label: 'Médios', value: 1, color: 'text-yellow-600' },
          { label: 'Total', value: 3, color: 'text-gray-600' },
        ].map((stat, index) => (
          <div key={index} className="card">
            <p className="text-sm text-gray-600 mb-1">{stat.label}</p>
            <p className={`text-3xl font-bold ${stat.color}`}>{stat.value}</p>
          </div>
        ))}
      </div>

      {/* Alertas List */}
      <div className="space-y-4">
        {alertas.map((alerta) => (
          <div key={alerta.id} className="card hover:shadow-lg transition-shadow">
            <div className="flex items-start justify-between">
              <div className="flex items-start gap-4 flex-1">
                <div className={`p-3 rounded-lg ${
                  alerta.prioridade === 'CRITICA' ? 'bg-red-100' :
                  alerta.prioridade === 'ALTA' ? 'bg-orange-100' : 'bg-yellow-100'
                }`}>
                  <AlertCircle className={`w-6 h-6 ${
                    alerta.prioridade === 'CRITICA' ? 'text-red-600' :
                    alerta.prioridade === 'ALTA' ? 'text-orange-600' : 'text-yellow-600'
                  }`} />
                </div>
                <div className="flex-1">
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="font-bold text-gray-900">{alerta.titulo}</h3>
                    <span className={`px-3 py-1 rounded-full text-xs font-medium border ${
                      getPrioridadeColor(alerta.prioridade)
                    }`}>
                      {alerta.prioridade}
                    </span>
                  </div>
                  <p className="text-gray-600 mb-2">{alerta.descricao}</p>
                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <span>Tipo: {alerta.tipo}</span>
                    <span>•</span>
                    <span>{new Date(alerta.data).toLocaleDateString('pt-BR')}</span>
                  </div>
                </div>
              </div>
              <button className="btn-primary ml-4">
                Resolver
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
