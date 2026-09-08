import React, { useState, useRef } from 'react';
import { X, Sparkles, PlusCircle, UploadCloud, Loader2, CheckCircle2 } from 'lucide-react';
import { scanReceiptImage, createExpense } from '../services/api';

export default function AddExpenseModal({ onClose, onSaveSuccess }) {
  const [activeTab, setActiveTab] = useState('SCAN'); // 'SCAN' | 'MANUAL'
  const [merchant, setMerchant] = useState('');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('FOOD');
  const [transactionDate, setTransactionDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [notes, setNotes] = useState('');
  const [receiptImagePath, setReceiptImagePath] = useState(null);

  const [scanning, setScanning] = useState(false);
  const [scanSuccess, setScanSuccess] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const fileInputRef = useRef(null);

  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setScanning(true);
    setError('');
    setScanSuccess(false);

    try {
      const result = await scanReceiptImage(file);
      if (result.merchant) setMerchant(result.merchant);
      if (result.totalAmount) setAmount(result.totalAmount.toString());
      if (result.category) setCategory(result.category);
      if (result.transactionDate) setTransactionDate(result.transactionDate);
      if (result.receiptImagePath) setReceiptImagePath(result.receiptImagePath);
      setScanSuccess(true);
    } catch (err) {
      setError('AI Scan failed: ' + err.message);
    } finally {
      setScanning(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    try {
      await createExpense({
        merchant: merchant.trim(),
        amount: parseFloat(amount),
        category,
        transactionDate,
        notes: notes.trim(),
        receiptImagePath
      });
      onSaveSuccess();
      onClose();
    } catch (err) {
      setError('Failed to record expense: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-stone-900/40 backdrop-blur-[1px] z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-sm border border-stone-300 max-w-md w-full overflow-hidden shadow-xl flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-4 sm:p-5 border-b border-stone-200 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-7 h-7 bg-stone-900 text-stone-100 rounded-xs flex items-center justify-center font-serif text-xs font-bold">
              +
            </div>
            <div>
              <h5 className="font-serif font-bold text-base text-stone-900">Record Outflow Entry</h5>
              <p className="text-xs text-stone-500 font-sans">Post verified transaction record</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-1.5 text-stone-400 hover:text-stone-700 rounded-xs transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Segmented Tab Switcher */}
        <div className="px-4 sm:px-5 pt-3 pb-2">
          <div className="bg-stone-100 p-0.5 rounded-sm border border-stone-200 flex gap-0.5">
            <button
              type="button"
              onClick={() => setActiveTab('SCAN')}
              className={`flex-1 py-1.5 text-xs font-medium rounded-xs flex items-center justify-center gap-1.5 transition ${
                activeTab === 'SCAN'
                  ? 'bg-white text-stone-900 shadow-2xs font-semibold'
                  : 'text-stone-500 hover:text-stone-900'
              }`}
            >
              <div className="w-3.5 h-3.5 text-[#153e31]" /> AI Slip Scan
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('MANUAL')}
              className={`flex-1 py-1.5 text-xs font-medium rounded-xs flex items-center justify-center gap-1.5 transition ${
                activeTab === 'MANUAL'
                  ? 'bg-white text-stone-900 shadow-2xs font-semibold'
                  : 'text-stone-500 hover:text-stone-900'
              }`}
            >
              Manual Entry
            </button>
          </div>
        </div>

        {/* Content & Form */}
        <form onSubmit={handleSubmit} className="p-4 sm:p-5 pt-2 space-y-4 overflow-y-auto">
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-800 text-xs rounded-xs font-sans">
              {error}
            </div>
          )}

          {activeTab === 'SCAN' && (
            <div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFileUpload}
                className="hidden"
              />

              <div
                onClick={() => fileInputRef.current?.click()}
                className="border border-dashed border-stone-300 hover:border-stone-500 bg-stone-50/60 rounded-sm p-5 text-center cursor-pointer transition flex flex-col items-center justify-center gap-2 group"
              >
                {scanning ? (
                  <>
                    <Loader2 className="w-6 h-6 text-[#153e31] animate-spin" />
                    <div className="text-xs font-serif font-bold text-stone-900">Extracting receipt details...</div>
                    <div className="text-xs text-stone-500 font-sans">Parsing merchant, total, category and timestamp</div>
                  </>
                ) : scanSuccess ? (
                  <>
                    <CheckCircle2 className="w-6 h-6 text-emerald-800" />
                    <div className="text-xs font-serif font-bold text-emerald-900">Receipt Extracted Successfully</div>
                    <div className="text-xs text-stone-500 font-sans">Details populated below. Click to replace slip.</div>
                  </>
                ) : (
                  <>
                    <div className="p-2 bg-white rounded-xs border border-stone-200 shadow-2xs">
                      <UploadCloud className="w-4 h-4 text-stone-700" />
                    </div>
                    <div className="text-xs font-serif font-bold text-stone-900">Upload Receipt Slip</div>
                    <div className="text-xs text-stone-400 font-sans">Accepts PNG, JPG, or payment screenshots</div>
                  </>
                )}
              </div>
            </div>
          )}

          <div>
            <label className="block text-[10px] font-sans font-semibold text-stone-500 uppercase tracking-[0.14em] mb-1">
              Merchant / Counterparty
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Shell, McDonald's, Jaya Grocer"
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
                placeholder="0.00"
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
              placeholder="e.g. Lunch with engineering team"
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
              disabled={saving || scanning}
              className="inline-flex items-center gap-1.5 px-4 py-1.5 text-xs font-medium text-white bg-[#153e31] hover:bg-[#0f2e24] disabled:opacity-50 rounded-sm transition tracking-tight"
            >
              {saving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <PlusCircle className="w-3.5 h-3.5" />}
              Save Entry
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
