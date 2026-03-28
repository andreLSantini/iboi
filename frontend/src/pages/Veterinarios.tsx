import { Plus, Users } from 'lucide-react';

export default function Veterinarios() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Users className="h-8 w-8 text-primary-600" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Veterinarios</h1>
            <p className="text-gray-600">Camada futura para compartilhamento com veterinarios e parceiros tecnicos.</p>
          </div>
        </div>
        <button className="btn-primary flex items-center gap-2" disabled>
          <Plus className="h-4 w-4" />
          Em breve
        </button>
      </div>

      <div className="card">
        <div className="py-12 text-center">
          <Users className="mx-auto mb-4 h-16 w-16 text-gray-300" />
          <h3 className="mb-2 text-lg font-medium text-gray-900">Compartilhamento tecnico ainda nao liberado</h3>
          <p className="text-gray-600">
            Hoje o foco comercial do BovCore esta no nucleo operacional. O espaco para veterinarios entra na proxima fase.
          </p>
        </div>
      </div>
    </div>
  );
}
