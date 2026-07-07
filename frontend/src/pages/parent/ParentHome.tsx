import { useEffect, useState } from 'react';
import { api, apiError, naira } from '../../api/client';
import type { ChildDto, OrderDto } from '../../api/types';

export default function ParentHome() {
  const [children, setChildren] = useState<ChildDto[]>([]);
  const [amounts, setAmounts] = useState<Record<number, string>>({});
  const [order, setOrder] = useState<OrderDto | null>(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function load() {
    api.get<ChildDto[]>('/api/parent/children').then((r) => {
      setChildren(r.data);
      const init: Record<number, string> = {};
      r.data.forEach((c) => { init[c.studentId] = c.outstandingNaira > 0 ? String(c.outstandingNaira) : ''; });
      setAmounts(init);
    });
  }
  useEffect(load, []);

  async function pay() {
    setBusy(true); setError('');
    try {
      const allocations = children
        .filter((c) => Number(amounts[c.studentId]) > 0)
        .map((c) => ({ studentId: c.studentId, amountNaira: Number(amounts[c.studentId]) }));
      if (allocations.length === 0) { setError('Enter an amount for at least one child.'); setBusy(false); return; }
      const { data } = await api.post<OrderDto>('/api/parent/orders', { allocations });
      setOrder(data);
    } catch (err) {
      setError(apiError(err));
    } finally { setBusy(false); }
  }

  const total = children.reduce((sum, c) => sum + (Number(amounts[c.studentId]) || 0), 0);

  return (
    <>
      <h1>My Children</h1>
      <p className="muted">Pay for one or several children in a single transfer. Overpayment is carried to next term.</p>

      <div className="card table-wrap" style={{ marginTop: 12 }}>
        <table>
          <thead>
            <tr><th>Child</th><th>Class</th><th>Outstanding</th><th>Credit</th><th>Amount to pay (₦)</th></tr>
          </thead>
          <tbody>
            {children.map((c) => (
              <tr key={c.studentId}>
                <td>{c.fullName}<div className="muted" style={{ fontSize: 12 }}>{c.admissionNo}</div></td>
                <td>{c.className} · {c.residencyType}</td>
                <td>{naira(c.outstandingNaira)}</td>
                <td>{naira(c.creditNaira)}</td>
                <td>
                  <input type="number" style={{ width: 130 }} value={amounts[c.studentId] ?? ''}
                    onChange={(e) => setAmounts({ ...amounts, [c.studentId]: e.target.value })} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="toolbar" style={{ marginTop: 16 }}>
        <strong>Total: {naira(total)}</strong>
        <div className="spacer" />
        <button onClick={pay} disabled={busy || total <= 0}>{busy ? 'Generating account…' : 'Pay with bank transfer'}</button>
      </div>
      {error && <div className="error">{error}</div>}

      {order && <AccountModal order={order} onClose={() => { setOrder(null); load(); }} />}
    </>
  );
}

function AccountModal({ order, onClose }: { order: OrderDto; onClose: () => void }) {
  const va = order.virtualAccount;
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="card card-pad modal" onClick={(e) => e.stopPropagation()}>
        <h2>Transfer {naira(order.totalNaira)} to this account</h2>
        <p className="muted">A dedicated Nomba account was created for this payment. Once your transfer lands, the fees are reconciled automatically.</p>
        {va && (
          <div className="card card-pad" style={{ background: '#f8faff', marginBottom: 12 }}>
            <div className="kpi-label">{va.bankName}</div>
            <div className="acct">{va.accountNumber}</div>
            <div className="muted">{va.accountName}</div>
            <div className="kpi-sub">Expires {new Date(va.expiryAt).toLocaleString()}</div>
          </div>
        )}
        <div className="muted" style={{ fontSize: 13 }}>Reference: {order.reference}</div>
        <div className="row" style={{ marginTop: 12 }}>
          <button onClick={onClose}>Done</button>
        </div>
      </div>
    </div>
  );
}
