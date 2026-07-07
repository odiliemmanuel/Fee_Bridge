import { Navigate } from 'react-router-dom';
import { ReactNode } from 'react';
import { useAuth } from '../auth/AuthContext';

export function ProtectedRoute({ roles, children }: { roles?: string[]; children: ReactNode }) {
  const { user, hasRole } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !hasRole(...roles)) {
    return <Navigate to={user.roles.includes('PARENT') || user.roles.includes('GUARDIAN') ? '/parent' : '/dashboard'} replace />;
  }
  return <>{children}</>;
}
