import React from 'react';
import { ChevronLeft, ChevronRight, Calendar, RotateCcw } from 'lucide-react';

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

export default function MonthSelector({ selectedYear, selectedMonth, onChangeMonth }) {
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth() + 1;

  const isCurrentMonth = selectedYear === currentYear && selectedMonth === currentMonth;

  const handlePrev = () => {
    if (selectedMonth === 1) {
      onChangeMonth(selectedYear - 1, 12);
    } else {
      onChangeMonth(selectedYear, selectedMonth - 1);
    }
  };

  const handleNext = () => {
    if (selectedMonth === 12) {
      onChangeMonth(selectedYear + 1, 1);
    } else {
      onChangeMonth(selectedYear, selectedMonth + 1);
    }
  };

  const handleResetToCurrent = () => {
    onChangeMonth(currentYear, currentMonth);
  };

  return (
    <div className="bg-white p-3.5 sm:px-5 sm:py-3 rounded-sm border border-stone-200 flex flex-col sm:flex-row items-center justify-between gap-3">
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-sm bg-stone-100 border border-stone-200 text-stone-700 flex items-center justify-center font-serif text-sm">
          <Calendar className="w-4 h-4 text-stone-700" />
        </div>
        <div>
          <div className="text-[10px] font-sans font-semibold text-stone-400 uppercase tracking-[0.16em]">
            Accounting Period
          </div>
          <div className="flex items-baseline gap-2 mt-0.5">
            <span className="text-base font-serif font-bold text-stone-900 tracking-tight">
              {MONTH_NAMES[selectedMonth - 1]} {selectedYear}
            </span>
            {!isCurrentMonth ? (
              <span className="text-[10px] font-sans font-semibold uppercase tracking-wider bg-amber-50 text-amber-800 border border-amber-200/80 px-2 py-0.2 rounded-xs">
                Archival
              </span>
            ) : (
              <span className="text-[10px] font-sans font-medium text-emerald-800 bg-emerald-50 border border-emerald-200/80 px-2 py-0.2 rounded-xs">
                Active Cycle
              </span>
            )}
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2">
        {!isCurrentMonth && (
          <button
            onClick={handleResetToCurrent}
            className="inline-flex items-center gap-1 text-xs text-stone-600 hover:text-stone-900 font-medium px-2.5 py-1 rounded-sm bg-white hover:bg-stone-50 border border-stone-200 transition"
            title="Jump to Current Month"
          >
            <RotateCcw className="w-3 h-3 text-stone-400" /> Current Cycle
          </button>
        )}

        <div className="flex items-center border border-stone-200 bg-stone-50 rounded-sm p-0.5">
          <button
            onClick={handlePrev}
            className="p-1 text-stone-600 hover:text-stone-900 hover:bg-white rounded-xs transition"
            title="Previous Month"
          >
            <ChevronLeft className="w-3.5 h-3.5" />
          </button>

          <span className="px-3 text-xs font-serif font-bold text-stone-800 tracking-widest">
            {String(selectedMonth).padStart(2, '0')} / {selectedYear}
          </span>

          <button
            onClick={handleNext}
            className="p-1 text-stone-600 hover:text-stone-900 hover:bg-white rounded-xs transition"
            title="Next Month"
          >
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}
