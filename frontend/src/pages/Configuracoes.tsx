import { Settings, User, Building, CreditCard, Bell } from 'lucide-react';

export default function Configuracoes() {
  const sections = [
    {
      icon: User,
      titulo: 'Perfil',
      descricao: 'Edite suas informações pessoais',
    },
    {
      icon: Building,
      titulo: 'Empresa',
      descricao: 'Gerencie dados da empresa e fazenda',
    },
    {
      icon: CreditCard,
      titulo: 'Assinatura',
      descricao: 'Planos e pagamentos',
    },
    {
      icon: Bell,
      titulo: 'Notificações',
      descricao: 'Preferências de alertas',
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Settings className="w-8 h-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Configurações</h1>
          <p className="text-gray-600">Gerencie suas preferências</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {sections.map((section, index) => {
          const Icon = section.icon;
          return (
            <div key={index} className="card hover:shadow-lg transition-shadow cursor-pointer">
              <div className="flex items-start gap-4">
                <div className="bg-primary-100 p-3 rounded-lg">
                  <Icon className="w-6 h-6 text-primary-600" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900 mb-1">{section.titulo}</h3>
                  <p className="text-sm text-gray-600">{section.descricao}</p>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
