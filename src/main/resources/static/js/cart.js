/**
 * Shopping Cart Logic
 */

let activeCouponCode = sessionStorage.getItem('appliedCoupon') || '';

document.addEventListener('DOMContentLoaded', () => {
    if (!Auth.requireAuth('CUSTOMER')) return;

    const couponInput = document.getElementById('coupon-input');
    if (couponInput && activeCouponCode) {
        couponInput.value = activeCouponCode;
    }

    loadCart();
});

async function loadCart() {
    const user = Auth.getUser();
    const container = document.getElementById('cart-items-container');

    try {
        let url = `/cart?userId=${user.id}`;
        if (activeCouponCode) {
            url += `&couponCode=${encodeURIComponent(activeCouponCode)}`;
        }

        const res = await apiRequest(url);
        if (!res.success || !res.data) {
            container.innerHTML = `<div style="text-align: center; color: var(--danger);">Failed to load cart.</div>`;
            return;
        }

        const data = res.data;
        document.getElementById('cart-count').textContent = data.itemCount || 0;

        // Render Cart Items
        if (!data.items || data.items.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 4rem 1rem; color: var(--text-muted);">
                    <div style="font-size: 3.5rem; margin-bottom: 1rem;">🛒</div>
                    <h3>Your cart is currently empty</h3>
                    <p style="margin: 0.5rem 0 1.5rem;">Explore our store and earn 10% loyalty points on every purchase!</p>
                    <a href="/products.html" class="btn btn-primary">Start Shopping Now</a>
                </div>
            `;
            document.getElementById('checkout-btn').classList.add('disabled');
            document.getElementById('checkout-btn').style.pointerEvents = 'none';
            updateSummaryUI(data);
            return;
        }

        document.getElementById('checkout-btn').classList.remove('disabled');
        document.getElementById('checkout-btn').style.pointerEvents = 'auto';

        container.innerHTML = data.items.map(item => {
            const p = item.product;
            const subtotal = p.price * item.quantity;
            return `
                <div class="cart-item-row">
                    <img src="${p.imageUrl}" alt="${escapeHtml(p.name)}" class="cart-img">
                    <div>
                        <a href="/product-details.html?id=${p.id}" style="font-weight: 700; color: #fff; font-size: 1rem;">
                            ${escapeHtml(p.name)}
                        </a>
                        <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">
                            Category: ${escapeHtml(p.category)}
                        </div>
                    </div>
                    <div style="font-weight: 600; color: var(--text-secondary);">
                        ${formatINR(p.price)}
                    </div>
                    <div>
                        <div class="qty-stepper" style="transform: scale(0.9); transform-origin: left;">
                            <button type="button" onclick="updateItemQuantity(${item.id}, ${item.quantity - 1})">-</button>
                            <input type="text" value="${item.quantity}" readonly>
                            <button type="button" onclick="updateItemQuantity(${item.id}, ${item.quantity + 1})">+</button>
                        </div>
                    </div>
                    <div style="font-weight: 800; color: #fff; font-size: 1.05rem;">
                        ${formatINR(subtotal)}
                    </div>
                    <div>
                        <button type="button" 
                                onclick="removeCartItem(${item.id})" 
                                style="background: transparent; border: none; color: #f87171; cursor: pointer; font-size: 1.25rem;"
                                title="Remove item">
                            &times;
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        // Update Summary UI
        updateSummaryUI(data);

    } catch (err) {
        container.innerHTML = `<div style="text-align: center; color: var(--danger); padding: 2rem;">Error: ${err.message}</div>`;
    }
}

function updateSummaryUI(data) {
    document.getElementById('sum-subtotal').textContent = formatINR(data.subtotal || 0);

    // Tier discount
    const tierRow = document.getElementById('tier-discount-row');
    if (data.tierDiscountAmount && data.tierDiscountAmount > 0) {
        tierRow.style.display = 'flex';
        document.getElementById('sum-tier-name').textContent = data.tierName;
        document.getElementById('sum-tier-discount').textContent = `-${formatINR(data.tierDiscountAmount)}`;
    } else {
        tierRow.style.display = 'none';
    }

    // Coupon discount
    const couponRow = document.getElementById('coupon-discount-row');
    const couponMsg = document.getElementById('coupon-msg');
    if (data.couponValid && data.couponDiscount > 0) {
        couponRow.style.display = 'flex';
        document.getElementById('sum-coupon-discount').textContent = `-${formatINR(data.couponDiscount)}`;
        couponMsg.style.display = 'block';
        couponMsg.style.color = '#34d399';
        couponMsg.textContent = data.couponMessage || 'Coupon applied successfully!';
    } else if (activeCouponCode && !data.couponValid) {
        couponRow.style.display = 'none';
        couponMsg.style.display = 'block';
        couponMsg.style.color = '#f87171';
        couponMsg.textContent = data.couponMessage || 'Invalid coupon code.';
    } else {
        couponRow.style.display = 'none';
        couponMsg.style.display = 'none';
    }

    // Delivery
    const deliveryElem = document.getElementById('sum-delivery');
    if (data.deliveryCharge === 0) {
        deliveryElem.innerHTML = `<span style="color: #34d399; font-weight: 700;">FREE</span>`;
    } else {
        deliveryElem.textContent = formatINR(data.deliveryCharge);
    }

    // Final total & estimated points
    document.getElementById('sum-total').textContent = formatINR(data.finalTotal || 0);
    document.getElementById('sum-estimated-points').textContent = `${data.estimatedPoints || 0} Points`;
}

async function updateItemQuantity(itemId, quantity) {
    const user = Auth.getUser();
    try {
        await apiRequest(`/cart/items/${itemId}?userId=${user.id}&quantity=${quantity}`, 'PUT');
        await loadCart();
    } catch (err) {
        showToast(err.message || 'Failed to update quantity.', 'error');
    }
}

async function removeCartItem(itemId) {
    const user = Auth.getUser();
    try {
        await apiRequest(`/cart/items/${itemId}?userId=${user.id}`, 'DELETE');
        showToast('Item removed from cart.');
        await loadCart();
    } catch (err) {
        showToast(err.message || 'Failed to remove item.', 'error');
    }
}

async function clearFullCart() {
    if (!confirm('Are you sure you want to clear your cart?')) return;
    const user = Auth.getUser();
    try {
        await apiRequest(`/cart/clear?userId=${user.id}`, 'DELETE');
        showToast('Cart cleared.');
        await loadCart();
    } catch (err) {
        showToast(err.message || 'Could not clear cart.', 'error');
    }
}

function applyCoupon() {
    const input = document.getElementById('coupon-input');
    const code = input ? input.value.trim().toUpperCase() : '';
    activeCouponCode = code;
    sessionStorage.setItem('appliedCoupon', code);
    loadCart();
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
