/**
 * Centralized API client for Telereceipt dashboard endpoints with token authentication.
 */

export function getAuthToken() {
  const urlParams = new URLSearchParams(window.location.search);
  const urlToken = urlParams.get('token');
  if (urlToken) {
    localStorage.setItem('telereceipt_token', urlToken);
    // Clean URL
    window.history.replaceState({}, document.title, window.location.pathname);
    return urlToken;
  }
  return localStorage.getItem('telereceipt_token');
}

export function setAuthToken(token) {
  if (token) {
    localStorage.setItem('telereceipt_token', token);
  }
}

export function clearAuthToken() {
  localStorage.removeItem('telereceipt_token');
}

function getHeaders() {
  const token = getAuthToken();
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export async function fetchExpenses(year, month) {
  const token = getAuthToken();
  const params = new URLSearchParams();
  if (token) params.append('token', token);
  if (year && month) {
    params.append('year', year);
    params.append('month', month);
  }
  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`/api/expenses${query}`, { headers: getHeaders() });
  if (!res.ok) throw new Error('Failed to fetch expenses');
  return res.json();
}

export async function fetchAnalyticsSummary(year, month) {
  const token = getAuthToken();
  const params = new URLSearchParams();
  if (token) params.append('token', token);
  if (year && month) {
    params.append('year', year);
    params.append('month', month);
  }
  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`/api/analytics/summary${query}`, { headers: getHeaders() });
  if (!res.ok) throw new Error('Failed to fetch analytics summary');
  return res.json();
}

export async function fetchCategorySummary(year, month) {
  const token = getAuthToken();
  const params = new URLSearchParams();
  if (token) params.append('token', token);
  if (year && month) {
    params.append('year', year);
    params.append('month', month);
  }
  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`/api/analytics/categories${query}`, { headers: getHeaders() });
  if (!res.ok) throw new Error('Failed to fetch category summary');
  return res.json();
}

export async function fetchDailySpending(year, month) {
  const token = getAuthToken();
  const params = new URLSearchParams();
  if (token) params.append('token', token);
  if (year && month) {
    params.append('year', year);
    params.append('month', month);
  }
  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`/api/analytics/daily${query}`, { headers: getHeaders() });
  if (!res.ok) throw new Error('Failed to fetch daily spending');
  return res.json();
}

export async function fetchReceiptUrl(expenseId) {
  const res = await fetch(`/api/expenses/${expenseId}/receipt-url`, { headers: getHeaders() });
  if (!res.ok) throw new Error('Failed to fetch receipt URL');
  return res.json();
}

export function getExportCsvUrl(year, month) {
  const token = getAuthToken();
  const params = new URLSearchParams();
  if (token) params.append('token', token);
  if (year && month) {
    params.append('year', year);
    params.append('month', month);
  }
  const query = params.toString() ? `?${params.toString()}` : '';
  return `/api/expenses/export${query}`;
}

export async function updateExpense(id, payload) {
  const token = getAuthToken();
  const url = token ? `/api/expenses/${id}?token=${token}` : `/api/expenses/${id}`;
  const res = await fetch(url, {
    method: 'PUT',
    headers: getHeaders(),
    body: JSON.stringify(payload)
  });
  if (!res.ok) throw new Error('Failed to update expense');
  return res.json();
}

export async function deleteExpense(id) {
  const token = getAuthToken();
  const url = token ? `/api/expenses/${id}?token=${token}` : `/api/expenses/${id}`;
  const res = await fetch(url, {
    method: 'DELETE',
    headers: getHeaders()
  });
  if (!res.ok) throw new Error('Failed to delete expense');
  return true;
}

export async function fetchUserProfile() {
  const token = getAuthToken();
  const url = token ? `/api/user/profile?token=${token}` : '/api/user/profile';
  const res = await fetch(url, { headers: getHeaders() });
  if (!res.ok) throw new Error('Failed to fetch user profile');
  return res.json();
}

export async function updateBudget(budgetAmount, year = null, month = null) {
  const token = getAuthToken();
  const url = token ? `/api/user/budget?token=${token}` : '/api/user/budget';
  const res = await fetch(url, {
    method: 'PUT',
    headers: getHeaders(),
    body: JSON.stringify({ 
      budget: budgetAmount,
      year: year || null,
      month: month || null
    })
  });
  if (!res.ok) throw new Error('Failed to update budget');
  return res.json();
}

export async function requestLoginOtp(username) {
  const res = await fetch('/api/auth/request-otp', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username })
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || 'Failed to request login code');
  }
  return data;
}

export async function verifyLoginOtp(username, code) {
  const res = await fetch('/api/auth/verify-otp', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, code })
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || 'Invalid or expired code');
  }
  if (data.token) {
    setAuthToken(data.token);
  }
  return data;
}

export async function createExpense(payload) {
  const token = getAuthToken();
  const url = token ? `/api/expenses?token=${token}` : '/api/expenses';
  const res = await fetch(url, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || 'Failed to create expense');
  }
  return data;
}

export async function scanReceiptImage(file) {
  const token = getAuthToken();
  const url = token ? `/api/expenses/scan?token=${token}` : '/api/expenses/scan';
  const formData = new FormData();
  formData.append('file', file);

  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method: 'POST',
    headers,
    body: formData
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || 'Failed to scan receipt image');
  }
  return data;
}
