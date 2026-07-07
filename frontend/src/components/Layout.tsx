import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const adminNav = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/students', label: 'Students' },
  { to: '/invoices', label: 'Invoices & Payments' },
  { to: '/reconciliation', label: 'Reconciliation' },
];

const parentNav = [{ to: '/parent', label: 'My Children' }];

export function Layout() {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const nav = hasRole('PARENT', 'GUARDIAN') ? parentNav : adminNav;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">Fee<span>Bridge</span></div>
        {nav.map((item) => (
          <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            {item.label}
          </NavLink>
        ))}
        <div className="sidebar-footer">{user?.schoolName}</div>
      </aside>
      <div className="main">
        <div className="topbar">
          <strong>{titleFor(nav)}</strong>
          <div className="row" style={{ alignItems: 'center' }}>
            <span className="muted">{user?.firstName} {user?.lastName} · {user?.roles.join(', ')}</span>
            <button className="ghost" onClick={() => { logout(); navigate('/login'); }}>Sign out</button>
          </div>
        </div>
        <div className="content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}

function titleFor(_nav: unknown) {
  return 'FeeBridge';
}
