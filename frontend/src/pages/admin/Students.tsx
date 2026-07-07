import { FormEvent, useEffect, useState } from 'react';
import { api, apiError } from '../../api/client';
import type { ClassDto, Page, StudentDto } from '../../api/types';

export default function Students() {
  const [classes, setClasses] = useState<ClassDto[]>([]);
  const [students, setStudents] = useState<StudentDto[]>([]);
  const [q, setQ] = useState('');
  const [classId, setClassId] = useState('');
  const [status, setStatus] = useState('');
  const [showCreate, setShowCreate] = useState(false);

  useEffect(() => {
    api.get<ClassDto[]>('/api/classes').then((r) => setClasses(r.data));
  }, []);

  function load() {
    const params: Record<string, string> = { size: '50' };
    if (q) params.q = q;
    if (classId) params.classId = classId;
    if (status) params.status = status;
    api.get<Page<StudentDto>>('/api/students', { params }).then((r) => setStudents(r.data.content));
  }

  useEffect(load, [classId, status]);

  return (
    <>
      <div className="toolbar">
        <h1 style={{ margin: 0 }}>Students</h1>
        <div className="spacer" />
        <button onClick={() => setShowCreate(true)}>+ New student</button>
      </div>

      <div className="toolbar">
        <input placeholder="Search name or admission no…" value={q}
          onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && load()} />
        <button className="ghost" onClick={load}>Search</button>
        <select value={classId} onChange={(e) => setClassId(e.target.value)}>
          <option value="">All classes</option>
          {classes.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          <option>ACTIVE</option><option>GRADUATED</option><option>WITHDRAWN</option>
        </select>
      </div>

      <div className="card table-wrap">
        <table>
          <thead>
            <tr><th>Admission</th><th>Name</th><th>Class</th><th>Residency</th><th>Status</th></tr>
          </thead>
          <tbody>
            {students.map((s) => (
              <tr key={s.id}>
                <td>{s.admissionNo}</td>
                <td>{s.fullName}</td>
                <td>{s.className ?? '—'}</td>
                <td>{s.residencyType}</td>
                <td>{s.status}</td>
              </tr>
            ))}
            {students.length === 0 && <tr><td colSpan={5} className="muted">No students found.</td></tr>}
          </tbody>
        </table>
      </div>

      {showCreate && <CreateStudent classes={classes} onClose={() => setShowCreate(false)} onCreated={() => { setShowCreate(false); load(); }} />}
    </>
  );
}

function CreateStudent({ classes, onClose, onCreated }: { classes: ClassDto[]; onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState({ firstName: '', lastName: '', classId: '', residencyType: 'DAY' });
  const [guardian, setGuardian] = useState({ firstName: '', lastName: '', phone: '', email: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      const body: any = {
        firstName: form.firstName, lastName: form.lastName,
        classId: form.classId ? Number(form.classId) : null,
        residencyType: form.residencyType, autoEnroll: true,
      };
      if (guardian.firstName && guardian.phone) {
        body.guardian = {
          guardian: { firstName: guardian.firstName, lastName: guardian.lastName, phone: guardian.phone, email: guardian.email },
          relationship: 'GUARDIAN', isPrimary: true, isPayer: true, isDelegatedPayer: false,
        };
      }
      await api.post('/api/students', body);
      onCreated();
    } catch (err) {
      setError(apiError(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="card card-pad modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h2>New student</h2>
        <div className="row">
          <div className="field" style={{ flex: 1 }}><label>First name</label><input style={{ width: '100%' }} value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required /></div>
          <div className="field" style={{ flex: 1 }}><label>Last name</label><input style={{ width: '100%' }} value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required /></div>
        </div>
        <div className="row">
          <div className="field" style={{ flex: 1 }}>
            <label>Class</label>
            <select style={{ width: '100%' }} value={form.classId} onChange={(e) => setForm({ ...form, classId: e.target.value })} required>
              <option value="">Select…</option>
              {classes.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="field" style={{ flex: 1 }}>
            <label>Residency</label>
            <select style={{ width: '100%' }} value={form.residencyType} onChange={(e) => setForm({ ...form, residencyType: e.target.value })}>
              <option>DAY</option><option>BOARDING</option>
            </select>
          </div>
        </div>
        <p className="muted" style={{ margin: '6px 0' }}>Optional guardian (matched to an existing parent by phone)</p>
        <div className="row">
          <div className="field" style={{ flex: 1 }}><label>Guardian name</label><input style={{ width: '100%' }} placeholder="First" value={guardian.firstName} onChange={(e) => setGuardian({ ...guardian, firstName: e.target.value })} /></div>
          <div className="field" style={{ flex: 1 }}><label>&nbsp;</label><input style={{ width: '100%' }} placeholder="Last" value={guardian.lastName} onChange={(e) => setGuardian({ ...guardian, lastName: e.target.value })} /></div>
        </div>
        <div className="row">
          <div className="field" style={{ flex: 1 }}><label>Phone</label><input style={{ width: '100%' }} value={guardian.phone} onChange={(e) => setGuardian({ ...guardian, phone: e.target.value })} /></div>
          <div className="field" style={{ flex: 1 }}><label>Email</label><input style={{ width: '100%' }} value={guardian.email} onChange={(e) => setGuardian({ ...guardian, email: e.target.value })} /></div>
        </div>
        {error && <div className="error">{error}</div>}
        <div className="row" style={{ marginTop: 8 }}>
          <button type="submit" disabled={busy}>{busy ? 'Saving…' : 'Create student'}</button>
          <button type="button" className="ghost" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
