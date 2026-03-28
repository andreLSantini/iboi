import { AlertCircle } from 'lucide-react';

export default function Alertas() {
  const alertas = [
    {
      id: 1,
      titulo: 'Vacina atrasada',
      descricao: '3 animais com vacinas pendentes ha mais de 7 dias.',
      prioridade: 'Critica',
      tipo: 'Sanitario',
      data: '2026-01-29',
    },
    {
      id: 2,
      titulo: 'Peso abaixo da media',
      descricao: 'Brinco 1240 com ganho inferior ao lote de referencia.',
      prioridade: 'Alta',
      tipo: 'Produtivo',
      data: '2026-01-28',
    },
    {
      id: 3,
      titulo: 'Pesagem pendente',
      descricao: '5 animais sem pesagem recente dentro da janela desejada.',
      prioridade: 'Media',
      tipo: 'Sanitario',
      data: '2026-01-27',
    },
  ];

  const getPrioridadeColor = (prioridade: string) => {
    switch (prioridade) {
      case 'Critica':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'Alta':
        return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'Media':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      default:
        return 'bg-blue-100 text-blue-800 border-blue-200';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <AlertCircle className="h-8 w-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Alertas estrategicos</h1>
          <p className="text-gray-600">Camada futura para recomendacoes e alertas automatizados orientados por dados.</p>
        </div>
      </div>

      <div className="rounded-2xl border border-primary-100 bg-primary-50 px-5 py-4 text-sm text-primary-700">
        Esta area mostra a direcao da camada estrategica do BovCore. O foco comercial atual segue em Trial, Basic e relatorios simples.
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        {[
          { label: 'Criticos', value: 1, color: 'text-red-600' },
          { label: 'Altos', value: 1, color: 'text-orange-600' },
          { label: 'Medios', value: 1, color: 'text-yellow-600' },
          { label: 'Total', value: 3, color: 'text-gray-600' },
        ].map((stat) => (
          <div key={stat.label} className="card">
            <p className="mb-1 text-sm text-gray-600">{stat.label}</p>
            <p className={`text-3xl font-bold ${stat.color}`}>{stat.value}</p>
          </div>
        ))}
      </div>

      <div className="space-y-4">
        {alertas.map((alerta) => (
          <div key={alerta.id} className="card">
            <div className="flex items-start justify-between gap-4">
              <div className="flex flex-1 items-start gap-4">
                <div
                  className={`rounded-lg p-3 ${
                    alerta.prioridade === 'Critica'
                      ? 'bg-red-100'
                      : alerta.prioridade === 'Alta'
                        ? 'bg-orange-100'
                        : 'bg-yellow-100'
                  }`}
                >
                  <AlertCircle
                    className={`h-6 w-6 ${
                      alerta.prioridade === 'Critica'
                        ? 'text-red-600'
                        : alerta.prioridade === 'Alta'
                          ? 'text-orange-600'
                          : 'text-yellow-600'
                    }`}
                  />
                </div>
                <div className="flex-1">
                  <div className="mb-2 flex items-start justify-between gap-3">
                    <h3 className="font-bold text-gray-900">{alerta.titulo}</h3>
                    <span className={`rounded-full border px-3 py-1 text-xs font-medium ${getPrioridadeColor(alerta.prioridade)}`}>
                      {alerta.prioridade}
                    </span>
                  </div>
                  <p className="mb-2 text-gray-600">{alerta.descricao}</p>
                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <span>Tipo: {alerta.tipo}</span>
                    <span>-</span>
                    <span>{new Date(alerta.data).toLocaleDateString('pt-BR')}</span>
                  </div>
                </div>
              </div>
              <button className="btn-primary" disabled>
                Em breve
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
