import { Bell, CreditCard, Settings, User } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function Configuracoes() {
  const navigate = useNavigate();

  const sections = [
    {
      icon: User,
      titulo: 'Perfil',
      descricao: 'Edite dados da empresa e informacoes usadas na cobranca.',
      action: () => navigate('/app/assinatura'),
    },
    {
      icon: CreditCard,
      titulo: 'Assinatura',
      descricao: 'Acompanhe trial, upgrade e pagamentos.',
      action: () => navigate('/app/assinatura'),
    },
    { icon: Bell, titulo: 'Notificacoes', descricao: 'Preferencias de alertas e comunicacoes.' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Settings className="h-8 w-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Configuracoes</h1>
          <p className="text-gray-600">Ajustes pessoais, assinatura e preferencias gerais da conta.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
        {sections.map((section) => {
          const Icon = section.icon;
          return (
            <button
              key={section.titulo}
              onClick={section.action}
              className="card text-left transition-shadow hover:shadow-lg"
            >
              <div className="flex items-start gap-4">
                <div className="rounded-lg bg-primary-100 p-3">
                  <Icon className="h-6 w-6 text-primary-600" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900">{section.titulo}</h3>
                  <p className="mt-1 text-sm text-gray-600">{section.descricao}</p>
                </div>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
