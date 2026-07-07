import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { useAuth } from './auth/AuthContext';
import Login from './pages/Login';
import Dashboard from './pages/admin/Dashboard';
import Students from './pages/admin/Students';
import Invoices from './pages/admin/Invoices';
import Reconciliation from './pages/admin/Reconciliation';
import ParentHome from './pages/parent/ParentHome';

export default function App() {
  const { user } = useAuth();
  const home = user?.roles.includes('PARENT') || user?.roles.includes('GUARDIAN') ? '/parent' : '/dashboard';

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route path="/dashboard" element={<ProtectedRoute roles={['SCHOOL_ADMIN', 'BURSAR']}><Dashboard /></ProtectedRoute>} />
        <Route path="/students" element={<ProtectedRoute roles={['SCHOOL_ADMIN', 'BURSAR']}><Students /></ProtectedRoute>} />
        <Route path="/invoices" element={<ProtectedRoute roles={['SCHOOL_ADMIN', 'BURSAR']}><Invoices /></ProtectedRoute>} />
        <Route path="/reconciliation" element={<ProtectedRoute roles={['SCHOOL_ADMIN', 'BURSAR']}><Reconciliation /></ProtectedRoute>} />
        <Route path="/parent" element={<ProtectedRoute roles={['PARENT', 'GUARDIAN']}><ParentHome /></ProtectedRoute>} />
      </Route>
      <Route path="*" element={<Navigate to={user ? home : '/login'} replace />} />
    </Routes>
  );
}
