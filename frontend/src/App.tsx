import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Login from './components/Login';
import Onboarding from './components/Onboarding';
import DashboardHome from './components/DashboardHome';
import Landing from './pages/Landing';
import Animais from './pages/Animais';
import AnimalDetalhes from './pages/AnimalDetalhes';
import Eventos from './pages/Eventos';
import Lotes from './pages/Lotes';
import Calendario from './pages/Calendario';
import Despesas from './pages/Despesas';
import Relatorios from './pages/Relatorios';
import Alertas from './pages/Alertas';
import Veterinarios from './pages/Veterinarios';
import Configuracoes from './pages/Configuracoes';
import Assinatura from './pages/Assinatura';
import { getToken } from './services/session';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  return getToken() ? <>{children}</> : <Navigate to="/login" replace />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/onboarding" element={<Onboarding />} />

        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/app/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardHome />} />
          <Route path="animais" element={<Animais />} />
          <Route path="animais/:id" element={<AnimalDetalhes />} />
          <Route path="eventos" element={<Eventos />} />
          <Route path="lotes" element={<Lotes />} />
          <Route path="calendario" element={<Calendario />} />
          <Route path="despesas" element={<Despesas />} />
          <Route path="relatorios" element={<Relatorios />} />
          <Route path="alertas" element={<Alertas />} />
          <Route path="veterinarios" element={<Veterinarios />} />
          <Route path="assinatura" element={<Assinatura />} />
          <Route path="configuracoes" element={<Configuracoes />} />
        </Route>

        <Route path="/dashboard" element={<Navigate to="/app/dashboard" replace />} />
        <Route path="/configuracoes" element={<Navigate to="/app/configuracoes" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
