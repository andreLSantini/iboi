import { Calendar } from 'lucide-react';

export default function Calendario() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Calendar className="w-8 h-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Calendário Sanitário</h1>
          <p className="text-gray-600">Agendamentos de vacinas e tratamentos</p>
        </div>
      </div>

      <div className="card">
        <div className="text-center py-12">
          <Calendar className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Funcionalidade em Construção
          </h3>
          <p className="text-gray-600">
            O calendário sanitário estará disponível em breve.
          </p>
        </div>
      </div>
    </div>
  );
}
