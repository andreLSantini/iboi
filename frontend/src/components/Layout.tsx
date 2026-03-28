import { useState } from 'react';
import { useEffect } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  Activity,
  AlertCircle,
  BarChart3,
  Beef,
  Building,
  Calendar,
  CreditCard,
  DollarSign,
  Home,
  LogOut,
  Menu,
  Package,
  Settings,
  Users,
  X,
} from 'lucide-react';
import logo from '../assets/logo_transparente.png';
import api from '../services/api';
import { clearSession, getCurrentFarm, getFarms, getUser, storeAuthSession } from '../services/session';
import type { FarmSummary, LoginResponse } from '../types';

export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const user = getUser();
  const currentFarm = getCurrentFarm();
  const farms = getFarms();

  const menuItems = [
    { icon: Home, label: 'Dashboard', path: '/app/dashboard' },
    { icon: Building, label: 'Gestao de Fazendas', path: '/app/fazendas' },
    { icon: Beef, label: 'Animais', path: '/app/animais' },
    { icon: Package, label: 'Lotes', path: '/app/lotes' },
    { icon: Activity, label: 'Eventos', path: '/app/eventos' },
    { icon: Calendar, label: 'Calendario', path: '/app/calendario' },
    { icon: DollarSign, label: 'Despesas', path: '/app/despesas' },
    { icon: BarChart3, label: 'Relatorios', path: '/app/relatorios' },
    { icon: AlertCircle, label: 'Alertas estrategicos', path: '/app/alertas' },
    { icon: Users, label: 'Veterinarios', path: '/app/veterinarios' },
    { icon: CreditCard, label: 'Assinatura', path: '/app/assinatura' },
    { icon: Settings, label: 'Configuracoes', path: '/app/configuracoes' },
  ];

  const handleLogout = () => {
    clearSession();
    navigate('/login');
  };

  const handleFarmChange = async (farmId: string) => {
    try {
      const response = await api.post<LoginResponse>('/api/farms/select', { farmId });
      storeAuthSession(response.data);
      window.location.href = '/app/dashboard';
    } catch (error) {
      console.error('Erro ao trocar fazenda', error);
    }
  };

  useEffect(() => {
    if (!currentFarm && farms.length > 0) {
      void handleFarmChange(farms[0].id);
    }
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="fixed left-0 right-0 top-0 z-10 border-b border-gray-200 bg-white shadow-sm">
        <div className="flex items-center justify-between px-4 py-3">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen((prev) => !prev)}
              className="rounded-lg p-2 hover:bg-gray-100 lg:hidden"
            >
              {sidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
            <div className="flex items-center gap-3">
              <img src={logo} alt="Logo" className="h-14 w-auto object-contain" />
              <div className="hidden md:block">
                <p className="text-sm font-medium text-gray-600">Painel operacional do SaaS</p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-4">
            {farms.length > 0 && (
              <select
                value={currentFarm?.id ?? ''}
                onChange={(e) => void handleFarmChange(e.target.value)}
                className="hidden rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 shadow-sm md:block"
              >
                {farms.map((farm: FarmSummary) => (
                  <option key={farm.id} value={farm.id}>
                    {farm.name}
                  </option>
                ))}
              </select>
            )}

            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium text-gray-900">{user?.nome ?? 'Conta'}</p>
              <p className="text-xs text-gray-500">
                {currentFarm?.nome ?? 'Fazenda'} - {user?.farmRole ?? 'Admin'}
              </p>
            </div>
            <button onClick={handleLogout} className="rounded-lg p-2 text-gray-600 hover:bg-gray-100">
              <LogOut className="h-5 w-5" />
            </button>
          </div>
        </div>
      </header>

      <aside
        className={`fixed left-0 top-16 bottom-0 z-20 w-64 border-r border-gray-200 bg-white transition-transform duration-300 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0`}
      >
        <nav className="space-y-1 p-4">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const active = location.pathname === item.path || location.pathname.startsWith(`${item.path}/`);
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                className={`flex w-full items-center gap-3 rounded-lg px-4 py-3 transition-colors ${
                  active ? 'bg-primary-50 font-medium text-primary-700' : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <Icon className={`h-5 w-5 ${active ? 'text-primary-600' : 'text-gray-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </aside>

      <main className={`pt-16 transition-all duration-300 ${sidebarOpen ? 'lg:pl-64' : ''}`}>
        <div className="p-6">
          <Outlet />
        </div>
      </main>

      {sidebarOpen && (
        <div className="fixed inset-0 top-16 z-10 bg-black/50 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}
    </div>
  );
}
