import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Onboarding from './components/Onboarding';
import Layout from './components/Layout';
import DashboardHome from './components/DashboardHome';
import Animais from './pages/Animais';
import AnimalDetalhes from './pages/AnimalDetalhes';
import Eventos from './pages/Eventos';
import Calendario from './pages/Calendario';
import Despesas from './pages/Despesas';
import Relatorios from './pages/Relatorios';
import Alertas from './pages/Alertas';
import Veterinarios from './pages/Veterinarios';
import Configuracoes from './pages/Configuracoes';

// Protected Route Component
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('token');
  return token ? <>{children}</> : <Navigate to="/login" replace />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/onboarding" element={<Onboarding />} />

        {/* Protected Routes with Layout */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardHome />} />
          <Route path="animais" element={<Animais />} />
          <Route path="animais/:id" element={<AnimalDetalhes />} />
          <Route path="eventos" element={<Eventos />} />
          <Route path="calendario" element={<Calendario />} />
          <Route path="despesas" element={<Despesas />} />
          <Route path="relatorios" element={<Relatorios />} />
          <Route path="alertas" element={<Alertas />} />
          <Route path="veterinarios" element={<Veterinarios />} />
          <Route path="configuracoes" element={<Configuracoes />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
