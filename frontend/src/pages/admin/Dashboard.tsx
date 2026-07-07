import { useEffect, useState } from 'react';
import {
  Bar, BarChart, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { api, naira } from '../../api/client';
import type { Dashboard } from '../../api/types';

const STATUS_COLORS: Record<string, string> = {
  PENDING: '#e6a417', PARTIAL: '#2b59ff', PAID: '#17a673', OVERPAID: '#7b3fe4',
};

export default function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    api.get<Dashboard>('/api/analytics/dashboard').then((r) => setData(r.data));
  }, []);

  if (!data) return <p className="muted">Loading analytics…</p>;

  const kpis = [
    { label: 'Expected (term)', value: naira(data.expectedNaira) },
    { label: 'Collected', value: naira(data.collectedNaira), sub: `${data.collectionRatePercent}% collection rate` },
    { label: 'Outstanding', value: naira(data.outstandingNaira) },
    { label: 'Scholarships', value: naira(data.scholarshipNaira) },
    { label: 'Credit held', value: naira(data.creditOutstandingNaira) },
    { label: 'Students', value: String(data.totalStudents), sub: `${data.totalClasses} classes` },
  ];

  return (
    <>
      <h1>Dashboard</h1>
      <p className="muted">Collections overview for the current term.</p>

      <div className="grid kpi-grid" style={{ marginTop: 16 }}>
        {kpis.map((k) => (
          <div key={k.label} className="card card-pad">
            <div className="kpi-label">{k.label}</div>
            <div className="kpi-value">{k.value}</div>
            {k.sub && <div className="kpi-sub">{k.sub}</div>}
          </div>
        ))}
      </div>

      <div className="grid cols-2" style={{ marginTop: 16 }}>
        <div className="card card-pad">
          <h3>Expected vs Collected by class</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={data.byClass}>
              <XAxis dataKey="className" fontSize={12} />
              <YAxis fontSize={12} tickFormatter={(v) => `${v / 1000}k`} />
              <Tooltip formatter={(v: number) => naira(v)} />
              <Legend />
              <Bar dataKey="expectedNaira" name="Expected" fill="#c3d0f7" radius={[4, 4, 0, 0]} />
              <Bar dataKey="collectedNaira" name="Collected" fill="#2b59ff" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card card-pad">
          <h3>Invoices by status</h3>
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie data={data.statusCounts} dataKey="count" nameKey="status" outerRadius={95} label>
                {data.statusCounts.map((s) => (
                  <Cell key={s.status} fill={STATUS_COLORS[s.status]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </>
  );
}
