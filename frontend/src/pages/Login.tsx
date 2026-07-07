import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { apiError } from '../api/client';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@greenfield.edu.ng');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const user = await login(email, password);
      const parent = user.roles.includes('PARENT') || user.roles.includes('GUARDIAN');
      navigate(parent ? '/parent' : '/dashboard');
    } catch (err) {
      setError(apiError(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-wrap">
      <form className="card card-pad auth-card" onSubmit={submit}>
        <div className="brand" style={{ padding: '0 0 6px', color: 'var(--text)' }}>Fee<span>Bridge</span></div>
        <p className="muted" style={{ marginTop: 0 }}>School fee payment &amp; reconciliation</p>
        <div className="field">
          <label>Email</label>
          <input style={{ width: '100%' }} type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div className="field">
          <label>Password</label>
          <input style={{ width: '100%' }} type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        {error && <div className="error">{error}</div>}
        <button type="submit" disabled={busy} style={{ width: '100%', marginTop: 6 }}>
          {busy ? 'Signing in…' : 'Sign in'}
        </button>
        <div className="hint">
          <strong>Demo logins</strong><br />
          Admin — admin@greenfield.edu.ng<br />
          Parent — obi@example.com<br />
          Password for both — <code>password123</code>
        </div>
      </form>
    </div>
  );
}
