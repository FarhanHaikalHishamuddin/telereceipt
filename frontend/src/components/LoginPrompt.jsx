import React, { useState } from 'react';
import { Lock, ExternalLink, Send, KeyRound, ArrowLeft, Loader2, AlertCircle } from 'lucide-react';
import { requestLoginOtp, verifyLoginOtp } from '../services/api';

export default function LoginPrompt({ onLoginSuccess, onContinueLocal, onBackToHome }) {
  const [step, setStep] = useState('REQUEST'); // 'REQUEST' | 'VERIFY'
  const [username, setUsername] = useState('');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleRequestOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await requestLoginOtp(username);
      setStep('VERIFY');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await verifyLoginOtp(username, code);
      if (onLoginSuccess) {
        onLoginSuccess(res.token);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#fafaf8] flex items-center justify-center p-4 text-stone-900">
      <div className="bg-white border border-stone-300 p-8 sm:p-10 max-w-md w-full shadow-sm text-center space-y-7 relative">
        {onBackToHome && (
          <div className="text-left -mt-2 -mb-2">
            <button
              onClick={onBackToHome}
              className="inline-flex items-center gap-1.5 text-xs text-stone-500 hover:text-stone-900 transition"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Back to Home</span>
            </button>
          </div>
        )}

        <div className="w-12 h-12 bg-stone-900 text-stone-50 flex items-center justify-center mx-auto border border-stone-800">
          {step === 'REQUEST' ? <Lock className="w-5 h-5" /> : <KeyRound className="w-5 h-5" />}
        </div>

        <div>
          <div className="text-[10px] font-mono uppercase tracking-widest text-stone-400 mb-1">
            Archival Expense Audit
          </div>
          <h2 className="text-2xl font-serif font-normal text-stone-900 tracking-tight">
            {step === 'REQUEST' ? 'TeleReceipt Portal' : 'Enter Verification Passcode'}
          </h2>
          <p className="text-xs text-stone-500 mt-2 leading-relaxed max-w-xs mx-auto">
            {step === 'REQUEST'
              ? 'Enter your Telegram handle to receive a one-time verification passcode directly via @DuitHilang_bot.'
              : 'A 6-digit passcode has been transmitted to your Telegram conversation with @DuitHilang_bot.'}
          </p>
        </div>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 text-xs text-red-800 flex items-center gap-2 text-left font-medium">
            <AlertCircle className="w-4 h-4 shrink-0 text-red-600" />
            <span>{error}</span>
          </div>
        )}

        {step === 'REQUEST' ? (
          <form onSubmit={handleRequestOtp} className="space-y-5 text-left">
            <div>
              <label className="block text-[11px] font-mono uppercase tracking-wider text-stone-500 mb-1.5">
                Telegram Handle
              </label>
              <div className="relative">
                <span className="absolute left-3.5 top-2.5 text-stone-400 font-mono text-sm">@</span>
                <input
                  type="text"
                  required
                  placeholder="username"
                  value={username.replace('@', '')}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full pl-8 pr-3.5 py-2.5 text-xs bg-stone-50/50 border border-stone-300 focus:bg-white text-stone-900 focus:outline-none focus:border-stone-900 transition font-mono"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading || !username.trim()}
              className="w-full inline-flex items-center justify-center gap-2 bg-[#153e31] hover:bg-[#1b4d3e] disabled:opacity-50 text-white text-xs font-medium py-2.5 px-4 transition tracking-wide shadow-xs"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" /> Transmitting Passcode...
                </>
              ) : (
                <>
                  <Send className="w-4 h-4" /> Request One-Time Passcode
                </>
              )}
            </button>
          </form>
        ) : (
          <form onSubmit={handleVerifyOtp} className="space-y-5 text-left">
            <div>
              <label className="block text-[11px] font-mono uppercase tracking-wider text-stone-500 mb-1.5 text-center">
                6-Digit Security Token
              </label>
              <input
                type="text"
                required
                maxLength={6}
                autoFocus
                placeholder="000000"
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, ''))}
                className="w-full py-2.5 text-center text-2xl font-mono tracking-[0.4em] bg-stone-50/50 border border-stone-300 focus:bg-white focus:outline-none focus:border-stone-900 font-bold text-stone-900 transition"
              />
            </div>

            <button
              type="submit"
              disabled={loading || code.length !== 6}
              className="w-full inline-flex items-center justify-center gap-2 bg-[#153e31] hover:bg-[#1b4d3e] disabled:opacity-50 text-white text-xs font-medium py-2.5 px-4 transition tracking-wide shadow-xs"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" /> Verifying Passcode...
                </>
              ) : (
                <>
                  <KeyRound className="w-4 h-4" /> Verify & Authorize Access
                </>
              )}
            </button>

            <button
              type="button"
              onClick={() => {
                setStep('REQUEST');
                setCode('');
                setError('');
              }}
              className="w-full inline-flex items-center justify-center gap-1.5 text-xs text-stone-500 hover:text-stone-900 font-medium py-1.5 transition"
            >
              <ArrowLeft className="w-3.5 h-3.5" /> Back / Change Handle
            </button>
          </form>
        )}

        <div className="pt-4 border-t border-stone-200 text-center space-y-3">
          <p className="text-[11px] text-stone-500">
            Haven't registered your receipts bot yet?
          </p>
          <div>
            <a
              href="https://t.me/DuitHilang_bot"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-stone-50 hover:bg-stone-100 text-xs font-mono text-stone-700 border border-stone-300 transition"
            >
              <span>Open @DuitHilang_bot</span>
              <ExternalLink className="w-3 h-3 text-stone-400" />
            </a>
          </div>

          {onContinueLocal && (
            <div>
              <button
                onClick={onContinueLocal}
                className="text-[11px] text-stone-400 hover:text-stone-600 transition font-mono underline decoration-stone-300"
              >
                (Developer: Continue with local session)
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
