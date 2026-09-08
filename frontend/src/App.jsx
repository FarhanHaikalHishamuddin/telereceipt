import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import KpiCards from './components/KpiCards';
import CategoryDonutChart from './components/CategoryDonutChart';
import TransactionTable from './components/TransactionTable';
import ReceiptModal from './components/ReceiptModal';
import EditExpenseModal from './components/EditExpenseModal';
import AddExpenseModal from './components/AddExpenseModal';
import LoginPrompt from './components/LoginPrompt';
import DailySpendingChart from './components/DailySpendingChart';
import MonthSelector from './components/MonthSelector';
import HomePage from './components/HomePage';
import { 
  fetchExpenses, 
  fetchAnalyticsSummary, 
  fetchCategorySummary, 
  fetchDailySpending,
  fetchReceiptUrl,
  updateExpense,
  deleteExpense,
  updateBudget,
  getAuthToken,
  clearAuthToken
} from './services/api';

export default function App() {
  const [token, setToken] = useState(() => getAuthToken());
  const [expenses, setExpenses] = useState([]);
  const [summary, setSummary] = useState({
    totalSpentMonth: 0,
    monthlyBudget: 2000,
    budgetPercentageUsed: 0,
    transactionCount: 0,
    topCategory: 'FOOD'
  });
  const now = new Date();
  const [selectedYear, setSelectedYear] = useState(now.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(now.getMonth() + 1);
  const [categoryData, setCategoryData] = useState([]);
  const [dailyData, setDailyData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeReceiptUrl, setActiveReceiptUrl] = useState(null);
  const [editingExpense, setEditingExpense] = useState(null);
  const [isAddExpenseOpen, setIsAddExpenseOpen] = useState(false);
  const [bypassedLogin, setBypassedLogin] = useState(false);
  const [view, setView] = useState(() => {
    const urlParams = new URLSearchParams(window.location.search);
    return (urlParams.get('token') || getAuthToken()) ? 'dashboard' : 'home';
  });
  useEffect(() => {
    document.documentElement.classList.remove('dark');
    localStorage.removeItem('theme');
  }, []);

  const loadData = useCallback(async (year = selectedYear, month = selectedMonth) => {
    setLoading(true);
    try {
      const [expensesData, summaryData, catData, dailySpending] = await Promise.all([
        fetchExpenses(year, month).catch(() => []),
        fetchAnalyticsSummary(year, month).catch(() => null),
        fetchCategorySummary(year, month).catch(() => []),
        fetchDailySpending(year, month).catch(() => [])
      ]);

      if (expensesData) setExpenses(expensesData);
      if (summaryData) setSummary(summaryData);
      if (catData) {
        setCategoryData(catData.map(item => ({
          name: item.category,
          value: Number(item.totalAmount)
        })));
      }
      if (dailySpending) setDailyData(dailySpending);
    } catch (err) {
      console.warn('Backend API connection note:', err.message);
    } finally {
      setLoading(false);
    }
  }, [selectedYear, selectedMonth]);

  const handleMonthChange = (year, month) => {
    setSelectedYear(year);
    setSelectedMonth(month);
    loadData(year, month);
  };

  useEffect(() => {
    if (token || bypassedLogin) {
      loadData();
    }
  }, [token, bypassedLogin, loadData]);

  const handleViewReceipt = async (expenseId) => {
    try {
      const data = await fetchReceiptUrl(expenseId);
      if (data && data.url) {
        setActiveReceiptUrl(data.url);
      } else {
        alert('No receipt image found for this transaction.');
      }
    } catch (err) {
      alert('Failed to load receipt image.');
    }
  };

  const handleSaveExpense = async (id, updatedFields) => {
    await updateExpense(id, updatedFields);
    await loadData(selectedYear, selectedMonth);
  };

  const handleDeleteExpense = async (id) => {
    if (window.confirm('Are you sure you want to delete this expense?')) {
      try {
        await deleteExpense(id);
        await loadData(selectedYear, selectedMonth);
      } catch (err) {
        alert('Failed to delete expense: ' + err.message);
      }
    }
  };

  const handleLogout = () => {
    clearAuthToken();
    setToken(null);
    setBypassedLogin(false);
  };

  const handleUpdateBudget = async (newBudget) => {
    await updateBudget(newBudget, selectedYear, selectedMonth);
    await loadData(selectedYear, selectedMonth);
  };

  // 1. If on home view, render HomePage
  if (view === 'home') {
    return (
      <HomePage 
        onGoToDashboard={() => setView('dashboard')}
        hasSession={!!(token || bypassedLogin)}
      />
    );
  }

  // 2. If no token present and not in local dev bypass, show login prompt
  if (!token && !bypassedLogin) {
    return (
      <LoginPrompt 
        onLoginSuccess={(newToken) => {
          setToken(newToken);
          setView('dashboard');
        }} 
        onContinueLocal={() => {
          setBypassedLogin(true);
          setView('dashboard');
        }} 
        onBackToHome={() => setView('home')}
      />
    );
  }

  return (
    <div className="min-h-screen bg-[#fafaf8] text-stone-900 font-sans antialiased selection:bg-stone-200">
      <Navbar 
        onRefresh={() => loadData(selectedYear, selectedMonth)} 
        loading={loading} 
        onLogout={() => {
          handleLogout();
          setView('home');
        }} 
        onOpenAddExpense={() => setIsAddExpenseOpen(true)}
        selectedYear={selectedYear}
        selectedMonth={selectedMonth}
        onGoHome={() => setView('home')}
      />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        <MonthSelector 
          selectedYear={selectedYear} 
          selectedMonth={selectedMonth} 
          onChangeMonth={handleMonthChange} 
        />

        <KpiCards summary={summary} onUpdateBudget={handleUpdateBudget} />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <DailySpendingChart data={dailyData} />
          <CategoryDonutChart data={categoryData} />
        </div>

        <TransactionTable 
          expenses={expenses} 
          loading={loading} 
          onViewReceipt={handleViewReceipt}
          onEditExpense={(expense) => setEditingExpense(expense)}
          onDeleteExpense={handleDeleteExpense}
        />
      </main>

      <ReceiptModal 
        imageUrl={activeReceiptUrl} 
        onClose={() => setActiveReceiptUrl(null)} 
      />

      <EditExpenseModal
        expense={editingExpense}
        onClose={() => setEditingExpense(null)}
        onSave={handleSaveExpense}
      />

      {isAddExpenseOpen && (
        <AddExpenseModal
          onClose={() => setIsAddExpenseOpen(false)}
          onSaveSuccess={() => loadData(selectedYear, selectedMonth)}
        />
      )}
    </div>
  );
}
