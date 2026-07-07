import { FormEvent, useEffect, useState } from 'react';
import { api, apiError, naira } from '../../api/client';
import type { ClassDto, InvoiceDto, InvoiceStatus, Page } from '../../api/types';

const STATUSES: InvoiceStatus[] = ['PENDING', 'PARTIAL', 'PAID', 'OVERPAID'];

export default function Invoices() {
  const [classes, setClasses] = useState<ClassDto[]>([]);
  const [invoices, setInvoices] = useState<InvoiceDto[]>([]);
  const [status, setStatus] = useState('');
  const [classId, setClassId] = useState('');
  const [payFor, setPayFor] = useState<InvoiceDto | null>(null);

  useEffect(() => { api.get<ClassDto[]>('/api/classes').then((r) => setClasses(r.data)); }, []);

  function params() {
    const p: Record<string, string> = {};
    if (status) p.status = status;
    if (classId) p.classId = classId;
    return p;
  }

  function load() {
    api.get<Page<InvoiceDto>>('/api/invoices', { params: { ...params(), size: '100' } }).then((r) => setInvoices(r.data.content));
  }
  useEffect(load, [status, classId]);

  async function download(fmt: 'csv' | 'xlsx' | 'pdf') {
    const res = await api.get(`/api/reports/invoices.${fmt}`, { params: params(), responseType: 'blob' });
    const url = URL.createObjectURL(res.data as Blob);
    const a = document.createElement('a');
    a.href = url; a.download = `invoices.${fmt}`; a.click();
    URL.revokeObjectURL(url);
  }

  return (
    <>
      <div className="toolbar">
        <h1 style={{ margin: 0 }}>Invoices &amp; Payments</h1>
        <div className="spacer" />
        <button className="ghost" onClick={() => download('csv')}>⬇ CSV</button>
        <button className="ghost" onClick={() => download('xlsx')}>⬇ Excel</button>
        <button className="ghost" onClick={() => download('pdf')}>⬇ PDF</button>
      </div>

      <div className="toolbar">
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s}>{s}</option>)}
        </select>
        <select value={classId} onChange={(e) => setClassId(e.target.value)}>
          <option value="">All classes</option>
          {classes.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>

      <div className="card table-wrap">
        <table>
          <thead>
            <tr><th>Student</th><th>Class</th><th>Net</th><th>Paid</th><th>Balance</th><th>Status</th><th></th></tr>
          </thead>
          <tbody>
            {invoices.map((i) => (
              <tr key={i.id}>
                <td>{i.studentName}<div className="muted" style={{ fontSize: 12 }}>{i.admissionNo}</div></td>
                <td>{i.className}</td>
                <td>{naira(i.netNaira)}</td>
                <td>{naira(i.amountPaidNaira)}</td>
                <td>{naira(i.balanceNaira)}</td>
                <td><span className={`badge ${i.status}`}>{i.status}</span></td>
                <td>{i.balanceNaira > 0 && <button className="ghost" onClick={() => setPayFor(i)}>Record cash</button>}</td>
              </tr>
            ))}
            {invoices.length === 0 && <tr><td colSpan={7} className="muted">No invoices match this filter.</td></tr>}
          </tbody>
        </table>
      </div>

      {payFor && <OfflineModal invoice={payFor} onClose={() => setPayFor(null)} onDone={() => { setPayFor(null); load(); }} />}
    </>
  );
}

function OfflineModal({ invoice, onClose, onDone }: { invoice: InvoiceDto; onClose: () => void; onDone: () => void }) {
  const [amount, setAmount] = useState(String(invoice.balanceNaira));
  const [method, setMethod] = useState('CASH');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      await api.post('/api/payments/offline', {
        studentId: invoice.studentId, invoiceId: invoice.id, method, amountNaira: Number(amount),
        reference: 'CASH-' + Date.now(),
      });
      onDone();
    } catch (err) {
      setError(apiError(err));
    } finally { setBusy(false); }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="card card-pad modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h2>Record cash / offline payment</h2>
        <p className="muted">{invoice.studentName} · balance {naira(invoice.balanceNaira)}. The parent is notified by SMS &amp; email.</p>
        <div className="row">
          <div className="field" style={{ flex: 1 }}><label>Amount (₦)</label><input style={{ width: '100%' }} type="number" value={amount} onChange={(e) => setAmount(e.target.value)} /></div>
          <div className="field" style={{ flex: 1 }}>
            <label>Method</label>
            <select style={{ width: '100%' }} value={method} onChange={(e) => setMethod(e.target.value)}>
              <option>CASH</option><option>BANK_TRANSFER</option><option>POS</option>
            </select>
          </div>
        </div>
        {error && <div className="error">{error}</div>}
        <div className="row" style={{ marginTop: 8 }}>
          <button type="submit" disabled={busy}>{busy ? 'Recording…' : 'Record payment'}</button>
          <button type="button" className="ghost" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
