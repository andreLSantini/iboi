import { useNavigate } from 'react-router-dom';
import { LogOut, Home } from 'lucide-react';

export default function Dashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-2xl">🐂</span>
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900">iBoi</h1>
                <p className="text-sm text-gray-500">Gestão Inteligente de Gado</p>
              </div>
            </div>

            <button
              onClick={handleLogout}
              className="btn-secondary flex items-center gap-2"
            >
              <LogOut className="w-4 h-4" />
              Sair
            </button>
          </div>
        </div>
      </header>

      {/* Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="card">
          <div className="flex items-center gap-3 mb-6">
            <Home className="w-8 h-8 text-primary-600" />
            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                Bem-vindo, {user.nome}! 🎉
              </h2>
              <p className="text-gray-600">Seu sistema está pronto para uso!</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-4 bg-primary-50 border border-primary-200 rounded-lg">
              <p className="text-sm text-primary-700 font-medium mb-1">Trial Ativo</p>
              <p className="text-2xl font-bold text-primary-900">30 dias</p>
            </div>

            <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
              <p className="text-sm text-green-700 font-medium mb-1">Plano</p>
              <p className="text-2xl font-bold text-green-900">TRIAL</p>
            </div>

            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-sm text-blue-700 font-medium mb-1">Role</p>
              <p className="text-2xl font-bold text-blue-900">{user.farmRole}</p>
            </div>
          </div>

          <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
            <p className="text-sm text-yellow-800">
              ℹ️ <strong>Dashboard em construção!</strong> Em breve você terá acesso a todas as funcionalidades do iBoi.
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
