/**
 * Global Navbar Component
 */
document.addEventListener('DOMContentLoaded', () => {
    const navPlaceholder = document.getElementById('navbar-placeholder');
    if (!navPlaceholder) return;

    const user = Auth.getUser();
    const currentPath = window.location.pathname;

    let userNavContent = '';
    if (user && user.role === 'CUSTOMER') {
        userNavContent = `
            <div class="user-points-badge" title="Your current loyalty points balance">
                <span>⭐</span> <span id="nav-points-val">${user.pointsBalance || 0}</span> pts
            </div>
            <div class="badge badge-${(user.tierName || 'Bronze').toLowerCase()} user-tier-badge" id="nav-tier-badge">
                ${user.tierName || 'Bronze'}
            </div>
            <a href="/notifications.html" class="nav-icon-btn" title="Notifications" id="nav-notif-btn">
                <span>🔔</span>
                <span class="nav-badge-count" id="nav-notif-count" style="display: none;">0</span>
            </a>
            <div class="user-menu-btn" onclick="toggleUserDropdown(event)">
                <div class="avatar">${(user.fullName || 'U').charAt(0).toUpperCase()}</div>
                <span>${(user.fullName || 'Customer').split(' ')[0]}</span>
                <span style="font-size: 0.75rem; color: var(--text-muted);">▼</span>
            </div>
            <!-- Dropdown Menu -->
            <div id="user-dropdown-menu" style="display: none; position: absolute; top: 60px; right: 1.5rem; background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 0.75rem; min-width: 200px; box-shadow: var(--shadow-xl); z-index: 1000;">
                <div style="padding: 0.5rem 0.75rem; border-bottom: 1px solid var(--border-color); margin-bottom: 0.5rem;">
                    <div style="font-weight: 700; color: #fff;">${user.fullName}</div>
                    <div style="font-size: 0.8rem; color: var(--text-secondary);">${user.email}</div>
                </div>
                <a href="/profile.html" style="display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 0.75rem; color: var(--text-secondary); border-radius: var(--radius-sm); font-size: 0.9rem;">👤 My Profile</a>
                <a href="/my-rewards.html" style="display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 0.75rem; color: var(--text-secondary); border-radius: var(--radius-sm); font-size: 0.9rem;">🎟️ My Coupons</a>
                <a href="/points-history.html" style="display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 0.75rem; color: var(--text-secondary); border-radius: var(--radius-sm); font-size: 0.9rem;">📜 Points History</a>
                <hr style="border: none; border-top: 1px solid var(--border-color); margin: 0.5rem 0;">
                <a href="javascript:void(0)" onclick="Auth.logout()" style="display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 0.75rem; color: #f87171; border-radius: var(--radius-sm); font-size: 0.9rem;">🚪 Logout</a>
            </div>
        `;
    } else if (user && user.role === 'ADMIN') {
        userNavContent = `
            <a href="/admin-dashboard.html" class="btn btn-accent btn-sm">Admin Panel</a>
            <a href="javascript:void(0)" onclick="Auth.logout()" class="btn btn-outline btn-sm">Logout</a>
        `;
    } else {
        userNavContent = `
            <a href="/login.html" class="btn btn-outline btn-sm">Log In</a>
            <a href="/register.html" class="btn btn-primary btn-sm">Register</a>
        `;
    }

    navPlaceholder.innerHTML = `
        <nav class="navbar">
            <div class="container nav-container">
                <a href="/index.html" class="brand-logo">
                    <div class="logo-icon">⭐</div>
                    <div>Loyalty<span class="logo-highlight">Hub</span></div>
                </a>

                <ul class="nav-links">
                    <li><a href="/dashboard.html" class="nav-link ${currentPath.includes('dashboard') ? 'active' : ''}">Dashboard</a></li>
                    <li><a href="/products.html" class="nav-link ${currentPath.includes('products') || currentPath.includes('product-details') ? 'active' : ''}">Shop</a></li>
                    <li><a href="/rewards.html" class="nav-link ${currentPath.includes('rewards.html') ? 'active' : ''}">Rewards</a></li>
                    <li><a href="/my-rewards.html" class="nav-link ${currentPath.includes('my-rewards') ? 'active' : ''}">My Coupons</a></li>
                    <li><a href="/points-history.html" class="nav-link ${currentPath.includes('points-history') ? 'active' : ''}">Points History</a></li>
                </ul>

                <div class="nav-actions">
                    ${userNavContent}
                </div>
            </div>
        </nav>
    `;

    // Fetch live notifications and points count if customer is logged in
    if (user && user.role === 'CUSTOMER') {
        syncLiveNavState(user.id);
    }
});

function toggleUserDropdown(event) {
    event.stopPropagation();
    const menu = document.getElementById('user-dropdown-menu');
    if (menu) {
        menu.style.display = menu.style.display === 'none' ? 'block' : 'none';
    }
}

document.addEventListener('click', () => {
    const menu = document.getElementById('user-dropdown-menu');
    if (menu) menu.style.display = 'none';
});

async function syncLiveNavState(userId) {
    try {
        const notifRes = await apiRequest(`/notifications/unread-count?userId=${userId}`);
        const count = notifRes.data || 0;
        const countBadge = document.getElementById('nav-notif-count');
        if (countBadge) {
            if (count > 0) {
                countBadge.textContent = count > 99 ? '99+' : count;
                countBadge.style.display = 'flex';
            } else {
                countBadge.style.display = 'none';
            }
        }

        const profileRes = await apiRequest(`/customer/profile?userId=${userId}`);
        if (profileRes.success && profileRes.data) {
            const u = profileRes.data;
            const pointsElem = document.getElementById('nav-points-val');
            if (pointsElem) pointsElem.textContent = u.pointsBalance;

            // update cached user
            const cached = Auth.getUser();
            if (cached) {
                cached.pointsBalance = u.pointsBalance;
                Auth.setUser(cached);
            }
        }
    } catch (e) {
        console.warn("Could not sync live nav state:", e);
    }
}
