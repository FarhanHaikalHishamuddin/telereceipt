import React from 'react';
import { BarChart3 } from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Cell
} from 'recharts';

export default function DailySpendingChart({ data }) {
  const hasSpending = data && data.some(item => Number(item.amount) > 0);
  const maxDayAmount = hasSpending ? Math.max(...data.map(d => Number(d.amount) || 0)) : 0;

  // Swiss Financial Editorial Tooltip
  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const item = payload[0].payload;
      return (
        <div className="bg-white p-2.5 rounded-sm border border-stone-300 shadow-sm text-xs space-y-1">
          <div className="text-stone-400 font-sans text-[11px] flex items-center justify-between gap-4">
            <span>Day {item.day}</span>
            <span>{item.date}</span>
          </div>
          <div className="font-serif font-bold text-base text-stone-900">
            RM {Number(item.amount).toLocaleString('en-MY', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="lg:col-span-2 rounded-sm border border-stone-200 bg-white p-5 sm:p-6 flex flex-col justify-between">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h4 className="text-lg font-serif font-bold text-stone-900 tracking-tight">
            Daily Cash Outflow
          </h4>
          <p className="text-xs text-stone-500 font-sans mt-0.5">
            Daily distribution of settled expenses for this cycle
          </p>
        </div>

        {hasSpending && maxDayAmount > 0 && (
          <div className="hidden sm:inline-flex items-center gap-1.5 text-xs font-sans text-stone-600 bg-stone-50 border border-stone-200 px-2.5 py-1 rounded-xs">
            <span className="text-stone-400">Peak:</span>
            <span className="font-serif font-bold text-stone-900">RM {maxDayAmount.toFixed(2)}</span>
          </div>
        )}
      </div>

      <div className="h-64 w-full flex items-center justify-center">
        {hasSpending ? (
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <CartesianGrid strokeDasharray="2 2" vertical={false} stroke="#f0efeb" />
              <XAxis 
                dataKey="day" 
                tickLine={false} 
                axisLine={false} 
                tick={{ fontSize: 11, fill: '#78716c' }}
              />
              <YAxis 
                tickLine={false} 
                axisLine={false} 
                tick={{ fontSize: 11, fill: '#78716c' }}
                tickFormatter={(val) => `RM ${val}`}
              />
              <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(21, 62, 49, 0.04)' }} />
              <Bar 
                dataKey="amount" 
                radius={[1, 1, 0, 0]}
              >
                {data.map((entry, index) => (
                  <Cell 
                    key={`bar-${index}`} 
                    fill={entry.amount > 0 ? '#153e31' : '#f5f5f4'} 
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-center py-10">
            <p className="text-xs font-serif text-stone-600 font-bold">No daily outflow recorded for this cycle.</p>
            <p className="text-xs text-stone-400 font-sans mt-1">
              Add an expense from Telegram or web to populate the timeline.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
