/**
 * Centralized API & State Management
 */

const API_BASE = '/api';

// Current session user helper
const Auth = {
    getUser() {
        const u = localStorage.getItem('loyalty_user');
        return u ? JSON.parse(u) : null;
    },
    setUser(user) {
        localStorage.setItem('loyalty_user', JSON.stringify(user));
    },
    clearUser() {
        localStorage.removeItem('loyalty_user');
    },
    isLoggedIn() {
        return !!this.getUser();
    },
    requireAuth(allowedRole = null) {
        const u = this.getUser();
        if (!u) {
            window.location.href = '/login.html';
            return false;
        }
        if (allowedRole && u.role !== allowedRole) {
            if (allowedRole === 'ADMIN') {
                window.location.href = '/admin-login.html';
            } else {
                window.location.href = '/dashboard.html';
            }
            return false;
        }
        return true;
    },
    logout() {
        this.clearUser();
        window.location.href = '/login.html';
    }
};

// Generic Fetch Wrapper
async function apiRequest(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    if (body) {
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, options);
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Request failed');
        }
        return data;
    } catch (error) {
        console.error(`API Error on ${endpoint}:`, error);
        throw error;
    }
}

// Toast Alert System
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = '✨';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';
    if (type === 'warning') icon = '🔔';

    toast.innerHTML = `<span>${icon}</span> <div>${message}</div>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Format Currency
function formatINR(val) {
    if (val === undefined || val === null) return '₹0';
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 0
    }).format(val);
}

// Format Date
function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
    });
}
