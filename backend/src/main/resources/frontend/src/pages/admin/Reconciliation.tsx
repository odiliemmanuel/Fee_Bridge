import { useEffect, useState } from 'react';
import { api, naira } from '../../api/client';
import type { ReconExceptionDto } from '../../api/types';

export default function Reconciliation() {
  const [exceptions, setExceptions] = useState<ReconExceptionDto[]>([]);
  const [ledgerMsg, setLedgerMsg] = useState('');

  function load() {
    api.get<ReconExceptionDto[]>('/api/reconciliation/exceptions').then((r) => setExceptions(r.data));
  }
  useEffect(load, []);

  async function runScan() {
    const { data } = await api.post('/api/reconciliation/run');
    setLedgerMsg(`Scan complete — checked ${data.checked}, ${data.exceptionsCreated} new exception(s).`);
    load();
  }

  async function verifyLedger() {
    const { data } = await api.get<unknown[]>('/api/reconciliation/verify-ledger');
    setLedgerMsg(data.length === 0
      ? '✓ All student ledgers reconcile with their invoice balances.'
      : `⚠ ${data.length} student(s) have ledger imbalances.`);
    load();
  }

  return (
    <>
      <div className="toolbar">
        <h1 style={{ margin: 0 }}>Reconciliation</h1>
        <div className="spacer" />
        <button className="ghost" onClick={verifyLedger}>Verify ledgers</button>
        <button onClick={runScan}>Scan unmatched credits</button>
      </div>

      {ledgerMsg && <div className="card card-pad" style={{ marginBottom: 16 }}>{ledgerMsg}</div>}

      <div className="card table-wrap">
        <table>
          <thead>
            <tr><th>Type</th><th>Reference</th><th>Amount</th><th>Detail</th><th>Resolved</th></tr>
          </thead>
          <tbody>
            {exceptions.map((e) => (
              <tr key={e.id}>
                <td>{e.type}</td>
                <td>{e.reference}</td>
                <td>{e.actualNaira != null ? naira(e.actualNaira) : '—'}</td>
                <td className="muted">{e.detail}</td>
                <td>{e.resolved ? 'Yes' : 'No'}</td>
              </tr>
            ))}
            {exceptions.length === 0 && <tr><td colSpan={5} className="muted">No exceptions — everything reconciles. 🎉</td></tr>}
          </tbody>
        </table>
      </div>
    </>
  );
}
