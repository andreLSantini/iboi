import { Calendar } from 'lucide-react';

export default function Calendario() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Calendar className="h-8 w-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Calendario sanitario</h1>
          <p className="text-gray-600">Agenda sanitaria dedicada entra na proxima etapa do produto.</p>
        </div>
      </div>

      <div className="card">
        <div className="py-12 text-center">
          <Calendar className="mx-auto mb-4 h-16 w-16 text-gray-300" />
          <h3 className="mb-2 text-lg font-medium text-gray-900">Calendario dedicado em evolucao</h3>
          <p className="text-gray-600">
            Enquanto isso, os proximos manejos ja aparecem nos relatorios simples e no dashboard operacional.
          </p>
        </div>
      </div>
    </div>
  );
}
