import React from 'react';
import { 
  Send, 
  Camera, 
  PieChart, 
  ArrowRight, 
  ExternalLink, 
  Receipt
} from 'lucide-react';

export default function HomePage({ onGoToDashboard, hasSession }) {
  return (
    <div className="min-h-screen bg-[#fafaf8] text-stone-900 font-sans selection:bg-stone-200">
      {/* Top Navigation */}
      <header className="bg-white border-b border-stone-200 sticky top-0 z-30">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-stone-900 text-stone-100 flex items-center justify-center rounded-sm font-bold text-sm tracking-tighter">
              TR
            </div>
            <span className="font-bold text-lg text-stone-900 tracking-tight">TeleReceipt</span>
          </div>

          <div className="flex items-center gap-2.5">
            <a
              href="https://t.me/DuitHilang_bot"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1.5 text-xs text-stone-600 hover:text-stone-900 bg-stone-50 hover:bg-stone-100 border border-stone-200 px-3 py-1.5 rounded-sm font-medium transition"
            >
              <span>@DuitHilang_bot</span>
              <ExternalLink className="w-3 h-3 text-stone-400" />
            </a>

            <button
              onClick={onGoToDashboard}
              className="inline-flex items-center gap-1.5 bg-[#153e31] hover:bg-[#0f2e24] text-white text-xs font-medium px-4 py-1.5 rounded-sm transition tracking-tight"
            >
              <span>{hasSession ? 'Open Dashboard' : 'Sign In'}</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="max-w-4xl mx-auto px-4 sm:px-6 pt-16 pb-12 text-center">
        <h1 className="text-3xl sm:text-5xl font-bold text-stone-900 tracking-tight leading-tight sm:leading-tight">
          Track your everyday spending right inside Telegram.
        </h1>

        <p className="text-base sm:text-lg text-stone-600 max-w-2xl mx-auto mt-4 leading-relaxed font-normal">
          No complicated apps. No spreadsheets to fill in. Just tell the bot what you bought, or snap a picture of your receipt — everything is saved automatically.
        </p>

        <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3">
          <a
            href="https://t.me/DuitHilang_bot"
            target="_blank"
            rel="noreferrer"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 bg-[#153e31] hover:bg-[#0f2e24] text-white text-sm font-medium px-6 py-2.5 rounded-sm transition shadow-xs"
          >
            <Send className="w-4 h-4" />
            <span>Open @DuitHilang_bot on Telegram</span>
          </a>

          <button
            onClick={onGoToDashboard}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 bg-white hover:bg-stone-50 text-stone-800 border border-stone-300 text-sm font-medium px-6 py-2.5 rounded-sm transition"
          >
            <span>View Web Dashboard</span>
          </button>
        </div>
      </section>

      {/* 3 Simple Ways It Works */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 py-12">
        <div className="text-center mb-10">
          <h2 className="text-2xl font-bold text-stone-900 tracking-tight">How it works</h2>
          <p className="text-xs text-stone-500 mt-1">Three easy ways to keep your finances under control.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Card 1: Text message */}
          <div className="bg-white border border-stone-200 rounded-sm p-6 flex flex-col justify-between">
            <div>
              <div className="w-9 h-9 bg-stone-100 border border-stone-200 text-stone-800 rounded-sm flex items-center justify-center mb-4">
                <Send className="w-4 h-4" />
              </div>
              <h3 className="text-base font-bold text-stone-900 mb-1.5">1. Text what you spent</h3>
              <p className="text-xs text-stone-600 leading-relaxed">
                Just bought food or paid for petrol? Send a quick message like <span className="font-mono bg-stone-100 px-1 py-0.5 rounded text-stone-800">10 Mee Goreng</span> or <span className="font-mono bg-stone-100 px-1 py-0.5 rounded text-stone-800">15 grab</span>.
              </p>
            </div>

            {/* Chat preview */}
            <div className="mt-5 bg-stone-50 border border-stone-200 rounded-sm p-3 text-xs space-y-2">
              <div className="bg-white border border-stone-200 p-2 rounded-sm text-stone-800 text-right ml-auto max-w-[85%]">
                10.00 Mee Goreng
              </div>
              <div className="bg-[#153e31]/10 border border-[#153e31]/20 p-2 rounded-sm text-stone-900 max-w-[90%]">
                <div className="font-semibold text-emerald-900">✅ Saved!</div>
                <div className="text-[11px] text-stone-600 mt-0.5">RM 10.00 · Food & Dining</div>
              </div>
            </div>
          </div>

          {/* Card 2: Snap a receipt */}
          <div className="bg-white border border-stone-200 rounded-sm p-6 flex flex-col justify-between">
            <div>
              <div className="w-9 h-9 bg-stone-100 border border-stone-200 text-stone-800 rounded-sm flex items-center justify-center mb-4">
                <Camera className="w-4 h-4" />
              </div>
              <h3 className="text-base font-bold text-stone-900 mb-1.5">2. Snap a receipt photo</h3>
              <p className="text-xs text-stone-600 leading-relaxed">
                Send a photo of any receipt, paper bill, or payment screenshot. The bot automatically reads the store name, date, and price.
              </p>
            </div>

            {/* Receipt preview */}
            <div className="mt-5 bg-stone-50 border border-stone-200 rounded-sm p-3 text-xs space-y-2">
              <div className="bg-white border border-stone-200 p-2 rounded-sm text-stone-800 text-right ml-auto max-w-[85%] flex items-center justify-end gap-1">
                <Receipt className="w-3.5 h-3.5 text-stone-400" />
                <span>[Receipt Photo]</span>
              </div>
              <div className="bg-[#153e31]/10 border border-[#153e31]/20 p-2 rounded-sm text-stone-900 max-w-[90%]">
                <div className="font-semibold text-emerald-900">✅ Receipt Read!</div>
                <div className="text-[11px] text-stone-600 mt-0.5">RM 34.50 at Zus Coffee</div>
              </div>
            </div>
          </div>

          {/* Card 3: Monthly budget */}
          <div className="bg-white border border-stone-200 rounded-sm p-6 flex flex-col justify-between">
            <div>
              <div className="w-9 h-9 bg-stone-100 border border-stone-200 text-stone-800 rounded-sm flex items-center justify-center mb-4">
                <PieChart className="w-4 h-4" />
              </div>
              <h3 className="text-base font-bold text-stone-900 mb-1.5">3. Set a monthly budget</h3>
              <p className="text-xs text-stone-600 leading-relaxed">
                Type <span className="font-mono bg-stone-100 px-1 py-0.5 rounded text-stone-800">/budget 2000</span> to set your monthly spending limit. The bot alerts you before you overspend.
              </p>
            </div>

            {/* Budget preview */}
            <div className="mt-5 bg-stone-50 border border-stone-200 rounded-sm p-3 text-xs space-y-2">
              <div className="flex items-center justify-between text-[11px] text-stone-600">
                <span>Monthly Budget</span>
                <span className="font-bold text-stone-900">RM 2,000.00</span>
              </div>
              <div className="w-full bg-stone-200 h-2 rounded-xs overflow-hidden">
                <div className="bg-[#153e31] h-full w-[45%]"></div>
              </div>
              <div className="text-[11px] text-stone-500 text-right">
                RM 1,100.00 remaining
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Web Dashboard Features */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 py-12">
        <div className="bg-white border border-stone-200 rounded-sm p-8 sm:p-10">
          <div className="max-w-2xl">
            <span className="text-[11px] font-semibold text-stone-400 uppercase tracking-wider">Web Dashboard</span>
            <h2 className="text-2xl sm:text-3xl font-bold text-stone-900 tracking-tight mt-1">
              See the big picture of your spending.
            </h2>
            <p className="text-xs sm:text-sm text-stone-600 mt-2 leading-relaxed">
              Whenever you want to look at your full history, open this dashboard from your phone or computer.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 mt-8 pt-6 border-t border-stone-200">
            <div className="space-y-1">
              <div className="text-xs font-bold text-stone-900">Daily Spending</div>
              <p className="text-[11px] text-stone-500 leading-relaxed">See which days of the week you spend the most money.</p>
            </div>

            <div className="space-y-1">
              <div className="text-xs font-bold text-stone-900">Category Breakdown</div>
              <p className="text-[11px] text-stone-500 leading-relaxed">Know how much went to food, bills, transport, and shopping.</p>
            </div>

            <div className="space-y-1">
              <div className="text-xs font-bold text-stone-900">Saved Receipts</div>
              <p className="text-[11px] text-stone-500 leading-relaxed">Tap on any expense to view the original photo of your receipt.</p>
            </div>

            <div className="space-y-1">
              <div className="text-xs font-bold text-stone-900">Search & Export</div>
              <p className="text-[11px] text-stone-500 leading-relaxed">Quickly find past purchases or download your records as a CSV.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Bottom CTA Banner */}
      <section className="max-w-4xl mx-auto px-4 sm:px-6 py-16 text-center">
        <h2 className="text-2xl sm:text-3xl font-bold text-stone-900 tracking-tight">
          Ready to start tracking?
        </h2>
        <p className="text-xs sm:text-sm text-stone-600 mt-2 max-w-md mx-auto">
          Start a conversation with @DuitHilang_bot on Telegram. It takes less than 30 seconds to set up.
        </p>

        <div className="mt-6 flex items-center justify-center gap-3">
          <a
            href="https://t.me/DuitHilang_bot"
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 bg-[#153e31] hover:bg-[#0f2e24] text-white text-xs font-medium px-5 py-2.5 rounded-sm transition shadow-xs"
          >
            <Send className="w-3.5 h-3.5" />
            <span>Open @DuitHilang_bot on Telegram</span>
          </a>

          <button
            onClick={onGoToDashboard}
            className="inline-flex items-center gap-1.5 bg-white hover:bg-stone-50 text-stone-800 border border-stone-300 text-xs font-medium px-4 py-2.5 rounded-sm transition"
          >
            <span>Open Dashboard</span>
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-stone-200 py-6 text-center text-xs text-stone-400">
        <p>TeleReceipt · Powered by @DuitHilang_bot</p>
      </footer>
    </div>
  );
}
