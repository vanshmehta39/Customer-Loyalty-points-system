/**
 * Checkout Controller
 */

let activeCouponCode = sessionStorage.getItem('appliedCoupon') || '';

document.addEventListener('DOMContentLoaded', async () => {
    if (!Auth.requireAuth('CUSTOMER')) return;

    const user = Auth.getUser();

    // Pre-fill user information if available
    try {
        const profRes = await apiRequest(`/customer/profile?userId=${user.id}`);
        if (profRes.success && profRes.data) {
            const p = profRes.data;
            if (p.fullName) document.getElementById('ship-name').value = p.fullName;
            if (p.city) document.getElementById('ship-city').value = p.city;
            if (p.phone) document.getElementById('ship-phone').value = p.phone;
        }
    } catch (ignored) {}

    // Radio style toggle
    document.querySelectorAll('.payment-option-card').forEach(card => {
        card.addEventListener('click', () => {
            document.querySelectorAll('.payment-option-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');
        });
    });

    await loadCheckoutSummary();

    // Form submit
    document.getElementById('checkout-form').addEventListener('submit', handlePlaceOrder);
});

async function loadCheckoutSummary() {
    const user = Auth.getUser();
    try {
        let url = `/cart?userId=${user.id}`;
        if (activeCouponCode) url += `&couponCode=${encodeURIComponent(activeCouponCode)}`;

        const res = await apiRequest(url);
        if (!res.success || !res.data || !res.data.items || res.data.items.length === 0) {
            showToast('Your cart is empty. Please add products first.', 'warning');
            setTimeout(() => window.location.href = '/products.html', 1200);
            return;
        }

        const data = res.data;

        // Render mini items list
        const preview = document.getElementById('checkout-items-preview');
        preview.innerHTML = data.items.map(item => `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; font-size: 0.85rem;">
                <span style="color: var(--text-primary);">${item.quantity}x ${escapeHtml(item.product.name)}</span>
                <strong style="color: #fff;">${formatINR(item.product.price * item.quantity)}</strong>
            </div>
        `).join('');

        document.getElementById('chk-subtotal').textContent = formatINR(data.subtotal || 0);

        // Tier discount
        const tierRow = document.getElementById('chk-tier-discount-row');
        if (data.tierDiscountAmount && data.tierDiscountAmount > 0) {
            tierRow.style.display = 'flex';
            document.getElementById('chk-tier-name').textContent = data.tierName;
            document.getElementById('chk-tier-discount').textContent = `-${formatINR(data.tierDiscountAmount)}`;
        }

        // Coupon discount
        const couponRow = document.getElementById('chk-coupon-discount-row');
        if (data.couponValid && data.couponDiscount > 0) {
            couponRow.style.display = 'flex';
            document.getElementById('chk-coupon-code').textContent = activeCouponCode;
            document.getElementById('chk-coupon-discount').textContent = `-${formatINR(data.couponDiscount)}`;
        }

        // Delivery
        const delElem = document.getElementById('chk-delivery');
        delElem.innerHTML = data.deliveryCharge === 0 ? '<span style="color: #34d399; font-weight: 700;">FREE</span>' : formatINR(data.deliveryCharge);

        document.getElementById('chk-total').textContent = formatINR(data.finalTotal || 0);
        document.getElementById('chk-points-earned').textContent = `${data.estimatedPoints || 0} Points`;

    } catch (err) {
        showToast('Error loading checkout summary: ' + err.message, 'error');
    }
}

async function handlePlaceOrder(e) {
    e.preventDefault();
    const btn = document.getElementById('place-order-btn');
    btn.disabled = true;
    btn.textContent = 'Processing & Calculating Points...';

    const user = Auth.getUser();
    const shipName = document.getElementById('ship-name').value.trim();
    const shipAddress = document.getElementById('ship-address').value.trim();
    const shipCity = document.getElementById('ship-city').value.trim();
    const shipPhone = document.getElementById('ship-phone').value.trim();
    const fullAddress = `${shipName}, ${shipAddress}, ${shipCity} - Phone: ${shipPhone}`;

    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;

    try {
        const res = await apiRequest(`/orders/checkout?userId=${user.id}`, 'POST', {
            shippingAddress: fullAddress,
            paymentMethod,
            couponCode: activeCouponCode || null
        });

        if (res.success && res.data) {
            // Clear used coupon from session
            sessionStorage.removeItem('appliedCoupon');

            // Store order response for confirmation page
            sessionStorage.setItem('lastCompletedOrder', JSON.stringify(res.data));

            showToast('Order placed successfully! Redirecting...');
            setTimeout(() => {
                window.location.href = '/order-confirmation.html';
            }, 600);
        }
    } catch (err) {
        showToast(err.message || 'Failed to place order.', 'error');
        btn.disabled = false;
        btn.textContent = '🔒 Place Order & Earn Points';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
