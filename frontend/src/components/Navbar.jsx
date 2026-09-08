import React from 'react';
import { Receipt, RefreshCw, ExternalLink, Download, LogOut, Plus } from 'lucide-react';
import { getExportCsvUrl } from '../services/api';

export default function Navbar({ 
  onRefresh, 
  loading, 
  onLogout, 
  onOpenAddExpense, 
  selectedYear, 
  selectedMonth,
  onGoHome
}) {
  const handleExportCsv = () => {
    window.open(getExportCsvUrl(selectedYear, selectedMonth), '_blank');
  };

  return (
    <header className="bg-white border-b border-stone-200 sticky top-0 z-30">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div 
          className="flex items-center space-x-3.5 cursor-pointer select-none"
          onClick={onGoHome}
          title="Back to Home"
        >
          <div className="w-8 h-8 bg-stone-900 text-stone-100 flex items-center justify-center rounded-sm font-serif font-bold text-sm tracking-tighter">
            TR
          </div>
          <div className="flex items-baseline">
            <span className="font-serif font-bold text-lg text-stone-900 tracking-tight">Telereceipt</span>
          </div>
        </div>

        <div className="flex items-center space-x-2 sm:space-x-2.5">
          {onGoHome && (
            <button
              onClick={onGoHome}
              className="text-xs text-stone-600 hover:text-stone-900 bg-white hover:bg-stone-50 border border-stone-200 px-2.5 py-1.5 rounded-sm font-medium transition"
            >
              About Bot
            </button>
          )}

          <button
            onClick={onRefresh}
            disabled={loading}
            className="p-1.5 text-stone-500 hover:text-stone-900 hover:bg-stone-100 rounded-sm border border-stone-200 transition"
            title="Refresh"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-stone-900' : ''}`} />
          </button>

          <a 
            href="https://t.me/DuitHilang_bot" 
            target="_blank" 
            rel="noreferrer"
            className="hidden lg:inline-flex items-center gap-1 text-xs text-stone-600 hover:text-stone-900 bg-white hover:bg-stone-50 border border-stone-200 px-2.5 py-1.5 rounded-sm font-medium transition"
          >
            <span>Bot</span>
            <ExternalLink className="w-3 h-3 text-stone-400" />
          </a>

          {onOpenAddExpense && (
            <button 
              onClick={onOpenAddExpense}
              className="inline-flex items-center gap-1.5 bg-[#153e31] hover:bg-[#0f2e24] text-white text-xs font-medium px-3.5 py-1.5 rounded-sm transition tracking-tight"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Record Outflow</span>
            </button>
          )}

          <button 
            onClick={handleExportCsv}
            className="inline-flex items-center gap-1.5 bg-white hover:bg-stone-50 text-stone-700 border border-stone-200 text-xs font-medium px-3 py-1.5 rounded-sm transition"
          >
            <Download className="w-3.5 h-3.5 text-stone-400" />
            <span>Export CSV</span>
          </button>

          {onLogout && (
            <button
              onClick={onLogout}
              className="p-1.5 text-stone-400 hover:text-red-700 hover:bg-red-50 rounded-sm border border-transparent hover:border-red-200 transition"
              title="Log Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
