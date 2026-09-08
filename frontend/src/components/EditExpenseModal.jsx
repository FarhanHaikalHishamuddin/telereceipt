import React, { useState, useEffect } from 'react';
import { X, Edit3, Save } from 'lucide-react';

export default function EditExpenseModal({ expense, onClose, onSave }) {
  const [merchant, setMerchant] = useState('');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('OTHER');
  const [transactionDate, setTransactionDate] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (expense) {
      setMerchant(expense.merchant || '');
      setAmount(expense.amount || '');
      setCategory(expense.category || 'OTHER');
      setTransactionDate(expense.transactionDate || '');
      setNotes(expense.notes || '');
    }
  }, [expense]);

  if (!expense) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await onSave(expense.id, {
        merchant,
        amount: parseFloat(amount),
        category,
        transactionDate,
        notes
      });
      onClose();
    } catch (err) {
      alert('Failed to save expense changes: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-stone-900/40 backdrop-blur-[1px] z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-sm border border-stone-300 max-w-md w-full overflow-hidden shadow-xl flex flex-col max-h-[90vh]">
        <div className="p-4 sm:p-5 border-b border-stone-200 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-7 h-7 bg-stone-900 text-stone-100 rounded-xs flex items-center justify-center font-serif text-xs font-bold">
              ✎
            </div>
            <div>
              <h5 className="font-serif font-bold text-base text-stone-900">Edit Expense Entry</h5>
              <p className="text-xs text-stone-500 font-sans">Update transaction details</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-1.5 text-stone-400 hover:text-stone-700 rounded-xs transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 sm:p-5 space-y-4 overflow-y-auto">
          <div>
            <label className="block text-[10px] font-sans font-semibold text-stone-500 uppercase tracking-[0.14em] mb-1">
              Merchant / Counterparty
            </label>
            <input
              type="text"
              required
              value={merchant}
              onChange={(e) => setMerchant(e.target.value)}
              className="w-full text-xs bg-white border border-stone-300 rounded-sm px-3 py-2 text-stone-900 focus:outline-none focus:border-stone-900 transition font-sans"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[10px] font-sans font-semibold text-stone-500 uppercase tracking-[0.14em] mb-1">
                Amount (MYR)
              </label>
              <input
                type="number"
                step="0.01"
                required
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full text-xs bg-white border border-stone-300 rounded-sm px-3 py-2 text-stone-900 font-serif font-bold focus:outline-none focus:border-stone-900 transition"
              />
            </div>

            <div>
              <label className="block text-[10px] font-sans font-semibold text-stone-500 uppercase tracking-[0.14em] mb-1">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full text-xs bg-white border border-stone-300 rounded-sm px-3 py-2 text-stone-900 focus:outline-none focus:border-stone-900 transition font-sans"
              >
                <option value="FOOD">Food & Dining</option>
                <option value="TRANSPORT">Transport</option>
                <option value="GROCERIES">Groceries</option>
                <option value="UTILITIES">Utilities & Bills</option>
                <option value="SHOPPING">Shopping</option>
                <option value="ENTERTAINMENT">Entertainment</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-[10px] font-sans font-semibold text-stone-500 uppercase tracking-[0.14em] mb-1">
              Settlement Date
            </label>
            <input
              type="date"
              required
              value={transactionDate}
              onChange={(e) => setTransactionDate(e.target.value)}
              className="w-full text-xs bg-white border border-stone-300 rounded-sm px-3 py-2 text-stone-900 font-serif focus:outline-none focus:border-stone-900 transition"
            />
          </div>

          <div>
            <label className="block text-[10px] font-sans font-semibold text-stone-500 uppercase tracking-[0.14em] mb-1">
              Description / Notes (Optional)
            </label>
            <input
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Lunch with team"
              className="w-full text-xs bg-white border border-stone-300 rounded-sm px-3 py-2 text-stone-900 focus:outline-none focus:border-stone-900 transition font-sans"
            />
          </div>

          <div className="pt-3 border-t border-stone-200 flex justify-end gap-2.5">
            <button
              type="button"
              onClick={onClose}
              className="px-3.5 py-1.5 text-xs font-medium text-stone-600 bg-white hover:bg-stone-50 border border-stone-200 rounded-sm transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="inline-flex items-center gap-1.5 px-4 py-1.5 text-xs font-medium text-white bg-[#153e31] hover:bg-[#0f2e24] disabled:opacity-50 rounded-sm transition tracking-tight"
            >
              <Save className="w-3.5 h-3.5" /> {saving ? 'Saving...' : 'Update Record'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
