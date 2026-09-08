import React, { useState } from 'react';
import { Search, Image as ImageIcon, Edit2, Trash2 } from 'lucide-react';

const CATEGORY_COLORS = {
  FOOD: '#153e31',
  TRANSPORT: '#854d0e',
  GROCERIES: '#155e75',
  UTILITIES: '#44403c',
  SHOPPING: '#9a3412',
  ENTERTAINMENT: '#581c87',
  OTHER: '#78716c',
};

export default function TransactionTable({ expenses, loading, onViewReceipt, onEditExpense, onDeleteExpense }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  const filteredExpenses = expenses.filter(expense => {
    const merchant = expense.merchant || '';
    const notes = expense.notes || '';
    const matchesSearch = merchant.toLowerCase().includes(searchTerm.toLowerCase()) ||
      notes.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'ALL' || expense.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const getMerchantInitials = (name) => {
    if (!name) return 'EX';
    const words = name.trim().split(/\s+/);
    if (words.length > 1) {
      return (words[0][0] + words[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  };

  return (
    <div className="w-full rounded-sm border border-stone-200 bg-white flex flex-col overflow-hidden">
      {/* Header Toolbar */}
      <div className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-stone-200">
        <div>
          <div className="flex items-center gap-2.5">
            <h4 className="text-lg font-serif font-bold text-stone-900 tracking-tight">
              Settled Transactions
            </h4>
            <span className="text-xs font-sans text-stone-600 bg-stone-100 border border-stone-200 px-2 py-0.5 rounded-xs">
              {filteredExpenses.length} entries
            </span>
          </div>
        </div>
        
        <div className="flex items-center gap-2.5">
          <div className="relative">
            <Search className="w-3.5 h-3.5 absolute left-2.5 top-2.5 text-stone-400" />
            <input
              type="text"
              placeholder="Search merchant or notes..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-8 pr-3 py-1.5 text-xs bg-stone-50 border border-stone-200 text-stone-900 rounded-sm focus:outline-none focus:border-stone-800 w-48 sm:w-56 transition font-sans"
            />
          </div>

          <select 
            value={selectedCategory} 
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="py-1.5 px-3 text-xs bg-stone-50 border border-stone-200 rounded-sm text-stone-700 focus:outline-none focus:border-stone-800 transition font-sans"
          >
            <option value="ALL">All Categories</option>
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

      {/* Ledger Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs text-stone-700">
          <thead className="bg-stone-50 text-stone-500 uppercase font-sans font-semibold text-[10px] tracking-[0.14em] border-b border-stone-200">
            <tr>
              <th className="py-3 px-5">Settlement Date</th>
              <th className="py-3 px-4">Merchant & Description</th>
              <th className="py-3 px-4">Category</th>
              <th className="py-3 px-4">Documentation</th>
              <th className="py-3 px-5 text-right">Outflow (MYR)</th>
              <th className="py-3 px-4 text-center">Manage</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-stone-100">
            {filteredExpenses.map((expense) => (
              <tr key={expense.id} className="hover:bg-stone-50/70 transition-colors group">
                <td className="py-3.5 px-5 whitespace-nowrap text-stone-600 font-serif text-xs">
                  {expense.transactionDate}
                </td>
                <td className="py-3.5 px-4">
                  <div className="flex items-center gap-3">
                    <div className="w-7 h-7 rounded-xs bg-stone-100 text-stone-700 font-serif font-bold text-xs flex items-center justify-center shrink-0 border border-stone-200">
                      {getMerchantInitials(expense.merchant)}
                    </div>
                    <div>
                      <div className="font-semibold text-stone-900 text-xs font-sans">{expense.merchant}</div>
                      {expense.notes && <div className="text-[11px] text-stone-400 font-sans mt-0.5">{expense.notes}</div>}
                    </div>
                  </div>
                </td>
                <td className="py-3.5 px-4 whitespace-nowrap">
                  <span 
                    className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-xs text-[11px] font-sans font-medium bg-stone-50 text-stone-700 border border-stone-200"
                  >
                    <span 
                      className="w-1.5 h-1.5 rounded-xs"
                      style={{ backgroundColor: CATEGORY_COLORS[expense.category] || '#78716c' }}
                    />
                    {expense.category}
                  </span>
                </td>
                <td className="py-3.5 px-4 whitespace-nowrap">
                  {expense.receiptImagePath ? (
                    <button
                      onClick={() => onViewReceipt(expense.id)}
                      className="inline-flex items-center gap-1 text-xs text-stone-700 hover:text-stone-950 bg-white hover:bg-stone-50 border border-stone-200 px-2 py-0.5 rounded-xs font-sans transition"
                    >
                      <ImageIcon className="w-3 h-3 text-stone-400" />
                      <span>Verified Slip</span>
                    </button>
                  ) : (
                    <span className="text-stone-300 text-xs ml-2">—</span>
                  )}
                </td>
                <td className="py-3.5 px-5 whitespace-nowrap text-right font-serif font-bold text-stone-900 text-sm">
                  RM {Number(expense.amount).toFixed(2)}
                </td>
                <td className="py-3.5 px-4 whitespace-nowrap text-center">
                  <div className="inline-flex items-center gap-1">
                    <button
                      onClick={() => onEditExpense(expense)}
                      className="p-1 text-stone-400 hover:text-stone-900 hover:bg-stone-100 rounded-xs transition"
                      title="Edit Entry"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => onDeleteExpense(expense.id)}
                      className="p-1 text-stone-400 hover:text-red-700 hover:bg-red-50 rounded-xs transition"
                      title="Delete Entry"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {filteredExpenses.length === 0 && !loading && (
              <tr>
                <td colSpan="6" className="py-12 text-center">
                  <div className="text-xs font-serif text-stone-600 font-bold">No transactions match your filter.</div>
                  <div className="text-xs text-stone-400 font-sans mt-1">
                    Try clearing the search query or adjust the accounting period above.
                  </div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
