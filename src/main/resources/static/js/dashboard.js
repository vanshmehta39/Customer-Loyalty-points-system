/**
 * Customer Dashboard Controller Script
 */

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Authenticate user
    if (!Auth.requireAuth('CUSTOMER')) return;

    const user = Auth.getUser();
    await loadDashboardData(user.id);
});

async function loadDashboardData(userId) {
    try {
        const res = await apiRequest(`/customer/dashboard?userId=${userId}`);
        if (!res.success || !res.data) {
            showToast('Unable to load dashboard metrics.', 'error');
            return;
        }

        const data = res.data;

        // 1. Header & Welcome
        document.getElementById('dash-customer-name').textContent = data.fullName.split(' ')[0];

        // 2. Stat Cards
        document.getElementById('stat-points').textContent = data.currentPoints.toLocaleString();
        document.getElementById('stat-tier').textContent = data.currentTier;
        document.getElementById('stat-tier-icon').textContent = data.tierIcon || '🥉';
        document.getElementById('stat-tier-perk').textContent = data.discountPercentage > 0
            ? `${data.discountPercentage}% discount on checkout`
            : 'Basic loyalty benefits';
        document.getElementById('stat-spent').textContent = formatINR(data.totalSpent);
        document.getElementById('stat-lifetime').textContent = data.lifetimePoints.toLocaleString();
        document.getElementById('stat-redeemed').textContent = data.pointsRedeemed.toLocaleString();

        // 3. Visual Membership Progress Card
        document.getElementById('prog-tier-icon').textContent = data.tierIcon || '🥉';
        document.getElementById('prog-tier-name').textContent = `${data.currentTier} Member`;
        document.getElementById('prog-current-points').textContent = data.currentPoints.toLocaleString();

        const pointsNeededElem = document.getElementById('prog-points-needed');
        const nextTierElem = document.getElementById('prog-next-tier');
        const percentLabel = document.getElementById('prog-percent-label');
        const progressFill = document.getElementById('tier-progress-fill');

        if (data.nextTier && !data.nextTier.includes('Highest')) {
            pointsNeededElem.textContent = `${data.pointsToNextTier} points`;
            nextTierElem.textContent = data.nextTier;
            percentLabel.textContent = `${data.tierProgressPercent}%`;
            progressFill.style.width = `${data.tierProgressPercent}%`;
        } else {
            pointsNeededElem.textContent = 'Maximum Tier Achieved!';
            nextTierElem.textContent = 'Diamond VIP';
            percentLabel.textContent = '100%';
            progressFill.style.width = '100%';
        }

        // Show birthday bonus button if not claimed
        const bdayBtn = document.getElementById('birthday-btn');
        if (bdayBtn) {
            bdayBtn.style.display = 'inline-flex';
        }

        // 4. Render Recent Activities
        renderActivities(data.recentActivities);

        // 5. Render Recommended Rewards
        renderRecommendedRewards(data.recommendedRewards, data.currentPoints);

        // 6. Render Recent Notifications
        renderNotifications(data.notifications);

    } catch (err) {
        console.error('Error fetching dashboard:', err);
        showToast('Error loading dashboard: ' + err.message, 'error');
    }
}

function renderActivities(activities) {
    const list = document.getElementById('recent-activity-list');
    if (!activities || activities.length === 0) {
        list.innerHTML = `
            <div style="text-align: center; padding: 2rem; color: var(--text-muted);">
                <div style="font-size: 2rem; margin-bottom: 0.5rem;">🛍️</div>
                No points transactions yet. Start shopping to earn 10% points!
            </div>
        `;
        return;
    }

    list.innerHTML = activities.map(item => {
        const isPos = item.points > 0;
        let icon = '🛍️';
        let cls = 'purchase';
        if (item.transactionType === 'REWARD_REDEMPTION') {
            icon = '🎁';
            cls = 'redemption';
        } else if (item.transactionType.includes('BONUS') || item.transactionType === 'REFERRAL') {
            icon = '✨';
            cls = 'bonus';
        }

        return `
            <div class="activity-item">
                <div class="activity-info">
                    <div class="activity-icon-badge ${cls}">${icon}</div>
                    <div>
                        <div class="activity-title">${escapeHtml(item.description)}</div>
                        <div class="activity-date">${formatDate(item.createdAt)} &bull; Balance after: ${item.balanceAfter} pts</div>
                    </div>
                </div>
                <div class="points-pill ${isPos ? 'positive' : 'negative'}">
                    ${isPos ? '+' : ''}${item.points} pts
                </div>
            </div>
        `;
    }).join('');
}

function renderRecommendedRewards(rewards, currentPoints) {
    const grid = document.getElementById('recommended-rewards-grid');
    if (!rewards || rewards.length === 0) {
        grid.innerHTML = `<div style="grid-column: span 2; text-align: center; color: var(--text-muted);">No rewards available at this time.</div>`;
        return;
    }

    grid.innerHTML = rewards.map(r => {
        const canAfford = currentPoints >= r.pointsRequired;
        return `
            <div class="reward-mini-card">
                <div>
                    <div class="reward-mini-top">
                        <div class="reward-mini-icon">${r.icon || '🎁'}</div>
                        <div>
                            <div class="reward-mini-title">${escapeHtml(r.name)}</div>
                            <div class="reward-mini-cost">⭐ ${r.pointsRequired} Points</div>
                        </div>
                    </div>
                    <p style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 0.75rem; line-height: 1.4;">
                        ${escapeHtml(r.description)}
                    </p>
                </div>
                <button type="button" 
                        class="btn ${canAfford ? 'btn-accent' : 'btn-outline'} btn-sm btn-block"
                        ${!canAfford ? 'disabled' : ''}
                        onclick="quickRedeemReward(${r.id}, '${escapeHtml(r.name)}', ${r.pointsRequired})">
                    ${canAfford ? 'Redeem Voucher' : `Need ${r.pointsRequired - currentPoints} pts`}
                </button>
            </div>
        `;
    }).join('');
}

function renderNotifications(notifs) {
    const list = document.getElementById('recent-notifications-list');
    if (!notifs || notifs.length === 0) {
        list.innerHTML = `<div style="text-align: center; padding: 1.5rem; color: var(--text-muted);">No new notifications.</div>`;
        return;
    }

    list.innerHTML = notifs.map(n => `
        <div style="padding: 0.75rem 1rem; background: rgba(255,255,255,0.02); border-left: 3px solid ${n.isRead ? 'var(--border-color)' : 'var(--primary)'}; border-radius: var(--radius-sm);">
            <div style="font-weight: 600; font-size: 0.9rem; color: #fff;">${escapeHtml(n.title)}</div>
            <div style="font-size: 0.825rem; color: var(--text-secondary); margin-top: 0.25rem;">${escapeHtml(n.message)}</div>
            <div style="font-size: 0.725rem; color: var(--text-muted); margin-top: 0.35rem;">${formatDate(n.createdAt)}</div>
        </div>
    `).join('');
}

async function quickRedeemReward(rewardId, rewardName, pointsRequired) {
    if (!confirm(`Are you sure you want to redeem "${rewardName}" for ${pointsRequired} loyalty points?`)) {
        return;
    }

    const user = Auth.getUser();
    try {
        const res = await apiRequest(`/rewards/redeem?userId=${user.id}`, 'POST', { rewardId });
        if (res.success && res.data) {
            showToast(`🎉 Reward redeemed! Your coupon code is ${res.data.couponCode}`);
            // Refresh dashboard
            await loadDashboardData(user.id);
        }
    } catch (err) {
        showToast(err.message || 'Redemption failed.', 'error');
    }
}

async function claimBirthdayBonus() {
    const user = Auth.getUser();
    try {
        const res = await apiRequest(`/customer/claim-birthday-bonus?userId=${user.id}`, 'POST');
        if (res.success) {
            showToast(res.message || '🎉 Birthday bonus credited!');
            await loadDashboardData(user.id);
        }
    } catch (err) {
        showToast(err.message || 'Could not claim birthday bonus.', 'warning');
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
