import React, { useState } from 'react';
import { CreditCard, Wallet, Activity, ArrowUpRight, CheckCircle2, AlertTriangle, AlertCircle, Pencil, Check, X } from 'lucide-react';

export default function KpiCards({ summary, onUpdateBudget }) {
  const totalSpent = Number(summary?.totalSpentMonth || 0);
  const monthlyBudget = Number(summary?.monthlyBudget || 2000);
  const percentage = summary?.budgetPercentageUsed || 0;
  const transactionCount = summary?.transactionCount || 0;
  const topCategory = summary?.topCategory || 'FOOD';

  const [isEditingBudget, setIsEditingBudget] = useState(false);
  const [budgetInput, setBudgetInput] = useState('');
  const [savingBudget, setSavingBudget] = useState(false);

  const remaining = monthlyBudget - totalSpent;
  const isOverBudget = remaining < 0;

  const handleStartEdit = () => {
    setBudgetInput(monthlyBudget.toString());
    setIsEditingBudget(true);
  };

  const handleSaveBudget = async (e) => {
    e.preventDefault();
    const val = parseFloat(budgetInput);
    if (!val || val <= 0) return;
    setSavingBudget(true);
    try {
      if (onUpdateBudget) {
        await onUpdateBudget(val);
      }
      setIsEditingBudget(false);
    } catch (err) {
      alert('Failed to update monthly budget: ' + err.message);
    } finally {
      setSavingBudget(false);
    }
  };

  // Calculate day-of-month daily average pace
  const today = new Date().getDate();
  const dailyAvg = today > 0 ? totalSpent / today : 0;

  return (
    <div className="rounded-sm border border-stone-200 bg-white grid grid-cols-1 lg:grid-cols-12 divide-y lg:divide-y-0 lg:divide-x divide-stone-200">
      {/* Pane 1 (Anchor): Outflow, Budget Cap & Remaining Balance */}
      <div className="lg:col-span-6 p-5 sm:p-6 flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-sans font-semibold text-stone-400 uppercase tracking-[0.16em]">
              Monthly Outflow & Allocation
            </span>
            <span className="text-xs font-sans text-stone-600 bg-stone-100 border border-stone-200 px-2 py-0.5 rounded-xs">
              {transactionCount} transactions
            </span>
          </div>
          
          <div className="mt-3 flex items-baseline gap-1">
            <span className="text-sm font-sans font-medium text-stone-400">RM</span>
            <h3 className="text-3xl sm:text-4xl font-serif font-bold text-stone-900 tracking-tight">
              {totalSpent.toLocaleString('en-MY', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </h3>
            <span className="text-xs font-sans text-stone-400 ml-2 font-medium">
              ({percentage}% of cap)
            </span>
          </div>
        </div>

        <div className="mt-4 pt-3 border-t border-stone-100">
          <div className="w-full bg-stone-100 h-1.5 rounded-full overflow-hidden mb-2.5">
            <div 
              className={`h-full rounded-full transition-all duration-300 ${
                isOverBudget ? 'bg-red-600' : percentage > 80 ? 'bg-amber-600' : 'bg-[#153e31]'
              }`}
              style={{ width: `${Math.min(percentage, 100)}%` }}
            />
          </div>

          {isEditingBudget ? (
            <form onSubmit={handleSaveBudget} className="flex items-center gap-1.5">
              <span className="text-xs text-stone-500 font-medium">Cap: RM</span>
              <input
                type="number"
                step="1"
                min="1"
                required
                autoFocus
                value={budgetInput}
                onChange={(e) => setBudgetInput(e.target.value)}
                className="w-24 px-2 py-0.5 text-xs font-serif font-bold border border-stone-300 rounded-xs text-stone-900 focus:outline-none focus:border-[#153e31]"
              />
              <button
                type="submit"
                disabled={savingBudget}
                className="inline-flex items-center gap-0.5 px-2 py-0.5 text-xs font-medium bg-[#153e31] hover:bg-[#0f2e24] text-white rounded-xs transition"
              >
                <Check className="w-3 h-3" />
                {savingBudget ? '...' : 'Save'}
              </button>
              <button
                type="button"
                onClick={() => setIsEditingBudget(false)}
                className="p-1 text-stone-400 hover:text-stone-700 transition"
              >
                <X className="w-3 h-3" />
              </button>
            </form>
          ) : (
            <div className="flex items-center justify-between text-xs">
              <span className="text-stone-600">
                Remaining:{' '}
                <strong className={`font-serif text-sm font-bold ${isOverBudget ? 'text-red-700' : 'text-stone-900'}`}>
                  RM {Math.abs(remaining).toLocaleString('en-MY', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </strong>
                {isOverBudget && <span className="text-[10px] text-red-600 font-sans ml-1 uppercase font-semibold">(Over limit)</span>}
              </span>

              <button
                type="button"
                onClick={handleStartEdit}
                className="inline-flex items-center gap-1 text-stone-500 hover:text-stone-900 transition font-sans text-[11px]"
                title="Edit monthly cap"
              >
                <span>Cap: RM {monthlyBudget.toLocaleString('en-MY', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}</span>
                <Pencil className="w-3 h-3 text-stone-400" />
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Pane 2: Daily Velocity & Burn Rate */}
      <div className="lg:col-span-3 p-5 sm:p-6 flex flex-col justify-between bg-stone-50/40">
        <div>
          <span className="text-[10px] font-sans font-semibold text-stone-400 uppercase tracking-[0.16em]">
            Daily Burn Velocity
          </span>
          <div className="mt-3 flex items-baseline gap-1">
            <span className="text-sm font-sans font-medium text-stone-400">RM</span>
            <h3 className="text-2xl sm:text-3xl font-serif font-bold text-stone-900 tracking-tight">
              {dailyAvg.toFixed(2)}
            </h3>
            <span className="text-xs font-sans text-stone-400 ml-1">/ day</span>
          </div>
        </div>

        <div className="mt-4 pt-3 border-t border-stone-200/70 flex items-center justify-between text-xs text-stone-500">
          <span>Paced over {today} days</span>
          <span className="inline-flex items-center text-emerald-800 font-medium text-[11px] bg-emerald-50 px-1.5 py-0.5 rounded-xs border border-emerald-200/80">
            <ArrowUpRight className="w-3 h-3 mr-0.5" /> Steady
          </span>
        </div>
      </div>

      {/* Pane 3: Top Category & Health Status */}
      <div className="lg:col-span-3 p-5 sm:p-6 flex flex-col justify-between">
        <div>
          <span className="text-[10px] font-sans font-semibold text-stone-400 uppercase tracking-[0.16em]">
            Primary Expense Area
          </span>
          <div className="mt-3">
            <h3 className="text-xl sm:text-2xl font-serif font-bold text-stone-900 tracking-tight">
              {topCategory}
            </h3>
          </div>
        </div>

        <div className="mt-4 pt-3 border-t border-stone-200/70 flex items-center justify-between text-xs">
          <span className="text-stone-500 font-sans text-[11px]">Budget Status:</span>
          {isOverBudget ? (
            <span className="inline-flex items-center gap-1 font-medium text-red-800 bg-red-50 border border-red-200 px-2 py-0.5 rounded-xs text-[11px]">
              <AlertCircle className="w-3 h-3" /> Exceeded
            </span>
          ) : percentage > 80 ? (
            <span className="inline-flex items-center gap-1 font-medium text-amber-800 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-xs text-[11px]">
              <AlertTriangle className="w-3 h-3" /> High (80%+)
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 font-medium text-emerald-800 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-xs text-[11px]">
              <CheckCircle2 className="w-3 h-3 text-emerald-700" /> On Track
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
