import React from 'react';
import { Receipt, X } from 'lucide-react';

export default function ReceiptModal({ imageUrl, onClose }) {
  if (!imageUrl) return null;

  return (
    <div className="fixed inset-0 bg-stone-900/40 backdrop-blur-[1px] z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-none border border-stone-300 max-w-lg w-full overflow-hidden shadow-sm flex flex-col max-h-[90vh]">
        <div className="p-4 sm:p-5 border-b border-stone-200 flex items-center justify-between bg-stone-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-7 h-7 rounded-none bg-stone-900 text-stone-50 flex items-center justify-center text-xs font-serif">
              <Receipt className="w-3.5 h-3.5" />
            </div>
            <div>
              <h5 className="font-serif font-medium text-base text-stone-900">Receipt Document</h5>
              <p className="text-[11px] text-stone-500 font-mono tracking-tight">Captured voucher & audit record</p>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-1.5 text-stone-400 hover:text-stone-900 hover:bg-stone-100 transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
        <div className="p-4 bg-[#fafaf8] flex items-center justify-center max-h-[70vh] overflow-auto">
          <img 
            src={imageUrl} 
            alt="Receipt" 
            className="border border-stone-300 max-w-full max-h-[65vh] object-contain shadow-xs"
          />
        </div>
        <div className="p-4 bg-stone-50/50 border-t border-stone-200 flex justify-end">
          <button 
            onClick={onClose}
            className="px-4 py-1.5 text-xs font-medium text-stone-700 bg-white hover:bg-stone-100 border border-stone-300 transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
