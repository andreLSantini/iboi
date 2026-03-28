import { Home, LogOut } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo_transparente.png';

export default function Dashboard() {
  const navigate = useNavigate();

  const getUserFromStorage = () => {
    try {
      const userStr = localStorage.getItem('user');
      if (!userStr || userStr === 'undefined' || userStr === 'null') {
        return {};
      }
      return JSON.parse(userStr);
    } catch (error) {
      console.error('Error parsing user from localStorage:', error);
      return {};
    }
  };

  const user = getUserFromStorage();

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white shadow-sm">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <img src={logo} alt="BovCore Logo" className="h-16 w-auto object-contain" />
            <div>
              <p className="text-sm font-medium text-gray-600">Gestao operacional para pecuaria</p>
            </div>
          </div>

          <button onClick={handleLogout} className="btn-secondary flex items-center gap-2">
            <LogOut className="h-4 w-4" />
            Sair
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="card">
          <div className="mb-6 flex items-center gap-3">
            <Home className="h-8 w-8 text-primary-600" />
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Bem-vindo, {user.nome}!</h2>
              <p className="text-gray-600">Seu ambiente BovCore esta pronto para uso.</p>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <div className="rounded-lg border border-primary-200 bg-primary-50 p-4">
              <p className="mb-1 text-sm font-medium text-primary-700">Plano de entrada</p>
              <p className="text-2xl font-bold text-primary-900">TRIAL</p>
            </div>

            <div className="rounded-lg border border-green-200 bg-green-50 p-4">
              <p className="mb-1 text-sm font-medium text-green-700">Plano atual</p>
              <p className="text-2xl font-bold text-green-900">TRIAL</p>
            </div>

            <div className="rounded-lg border border-blue-200 bg-blue-50 p-4">
              <p className="mb-1 text-sm font-medium text-blue-700">Role</p>
              <p className="text-2xl font-bold text-blue-900">{user.farmRole}</p>
            </div>
          </div>

          <div className="mt-6 rounded-lg border border-yellow-200 bg-yellow-50 p-4">
            <p className="text-sm text-yellow-800">
              <strong>Painel legado.</strong> A experiencia principal do BovCore fica no dashboard operacional novo.
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
