import React from 'react';
import { PieChart as PieChartIcon } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

const CATEGORY_COLORS = {
  FOOD: '#153e31',
  TRANSPORT: '#854d0e',
  GROCERIES: '#155e75',
  UTILITIES: '#44403c',
  SHOPPING: '#9a3412',
  ENTERTAINMENT: '#581c87',
  OTHER: '#78716c',
};

const CATEGORY_LABELS = {
  FOOD: 'Food & Dining',
  TRANSPORT: 'Transport',
  GROCERIES: 'Groceries',
  UTILITIES: 'Utilities & Bills',
  SHOPPING: 'Shopping',
  ENTERTAINMENT: 'Entertainment',
  OTHER: 'Other',
};

export default function CategoryDonutChart({ data }) {
  const total = data ? data.reduce((acc, curr) => acc + Number(curr.value || 0), 0) : 0;
  const hasData = data && data.length > 0 && total > 0;

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const item = payload[0];
      const percent = total > 0 ? ((item.value / total) * 100).toFixed(1) : 0;
      const displayName = CATEGORY_LABELS[item.name] || item.name;
      return (
        <div className="bg-white p-2.5 rounded-sm border border-stone-300 shadow-sm text-xs space-y-0.5">
          <div className="text-stone-500 font-sans font-medium text-[11px] uppercase tracking-wider">{displayName}</div>
          <div className="font-serif font-bold text-base text-stone-900">
            RM {Number(item.value).toFixed(2)}{' '}
            <span className="text-xs font-sans text-stone-400 font-normal">({percent}%)</span>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="rounded-sm border border-stone-200 bg-white p-5 sm:p-6 flex flex-col justify-between">
      <div className="mb-2">
        <h4 className="text-lg font-serif font-bold text-stone-900 tracking-tight">
          Category Distribution
        </h4>
      </div>

      <div className="h-52 w-full flex items-center justify-center relative">
        {hasData ? (
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={2}
                dataKey="value"
                stroke="transparent"
                strokeWidth={1}
              >
                {data.map((entry) => (
                  <Cell 
                    key={`cell-${entry.name}`} 
                    fill={CATEGORY_COLORS[entry.name] || '#78716c'} 
                  />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-center py-8">
            <p className="text-xs font-serif text-stone-600 font-bold">No categorical outflow for this cycle.</p>
            <p className="text-xs text-stone-400 font-sans mt-1">
              Recorded expenses appear categorized here.
            </p>
          </div>
        )}
      </div>

      {/* Editorial Itemized Legend */}
      {hasData && (
        <div className="pt-3 border-t border-stone-200/70 grid grid-cols-2 gap-x-3 gap-y-1.5 text-xs">
          {data.map((item) => {
            const pct = total > 0 ? ((item.value / total) * 100).toFixed(0) : 0;
            const displayName = CATEGORY_LABELS[item.name] || item.name;
            return (
              <div key={item.name} className="flex items-center justify-between text-stone-600 font-sans">
                <span className="flex items-center gap-1.5 truncate">
                  <span 
                    className="w-2 h-2 rounded-xs shrink-0" 
                    style={{ backgroundColor: CATEGORY_COLORS[item.name] || '#78716c' }} 
                  />
                  <span className="truncate text-xs font-medium">{displayName}</span>
                </span>
                <span className="font-serif font-bold text-stone-900 text-xs">{pct}%</span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
