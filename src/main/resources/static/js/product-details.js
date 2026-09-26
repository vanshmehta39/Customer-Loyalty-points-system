/**
 * Product Details & Review Controller
 */

let currentProduct = null;
let selectedRating = 5;

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const productId = params.get('id');

    if (!productId) {
        window.location.href = '/products.html';
        return;
    }

    loadProductDetails(productId);
    loadReviews(productId);
    initStarRating();

    // Review form submission
    const reviewForm = document.getElementById('review-form');
    if (reviewForm) {
        reviewForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const user = Auth.getUser();
            if (!user) {
                showToast('Please sign in to leave a review and claim your +25 points.', 'warning');
                setTimeout(() => window.location.href = '/login.html', 1000);
                return;
            }

            const btn = document.getElementById('submit-review-btn');
            btn.disabled = true;
            btn.textContent = 'Posting Review & Crediting Points...';

            const reviewText = document.getElementById('review-text').value.trim();

            try {
                const res = await apiRequest(`/reviews?userId=${user.id}`, 'POST', {
                    productId: currentProduct.id,
                    rating: selectedRating,
                    reviewText
                });

                if (res.success) {
                    showToast('🎉 Review posted! +25 Loyalty Points credited to your account!');
                    document.getElementById('review-text').value = '';
                    loadReviews(currentProduct.id);
                    loadProductDetails(currentProduct.id); // update rating
                    // Sync points in nav
                    syncLiveNavState(user.id);
                }
            } catch (err) {
                showToast(err.message || 'Failed to submit review.', 'error');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Post Review & Earn +25 Points';
            }
        });
    }
});

async function loadProductDetails(id) {
    const container = document.getElementById('product-container');
    try {
        const res = await apiRequest(`/products/${id}`);
        if (!res.success || !res.data) {
            container.innerHTML = `<div style="grid-column: span 2; text-align: center; color: var(--danger);">Product not found.</div>`;
            return;
        }

        currentProduct = res.data;
        const estPoints = Math.round(currentProduct.price * 0.10);

        container.innerHTML = `
            <div>
                <img src="${currentProduct.imageUrl}" alt="${escapeHtml(currentProduct.name)}" class="product-hero-image">
            </div>

            <div style="display: flex; flex-direction: column; justify-content: center;">
                <div class="badge badge-primary" style="align-self: flex-start; margin-bottom: 0.75rem;">
                    ${escapeHtml(currentProduct.category)}
                </div>
                
                <h1 style="font-size: 2.2rem; margin-bottom: 0.75rem; line-height: 1.2;">
                    ${escapeHtml(currentProduct.name)}
                </h1>

                <div class="product-rating" style="margin-bottom: 1.25rem;">
                    <span style="font-size: 1.2rem;">★</span>
                    <strong style="color: #fff; font-size: 1.1rem;">${currentProduct.rating || 4.5}</strong>
                    <span class="product-rating-count">(${currentProduct.reviewsCount || 0} customer reviews)</span>
                </div>

                <div style="font-size: 2.2rem; font-weight: 800; color: #fff; font-family: var(--font-heading); margin-bottom: 0.75rem;">
                    ${formatINR(currentProduct.price)}
                </div>

                <div class="estimated-points-box" style="margin-bottom: 1.5rem;">
                    <div class="points-icon">🪙</div>
                    <div>
                        <h4 id="calc-points-label">Earn ~${estPoints} Loyalty Points</h4>
                        <p>Calculated at approximately 10% of checkout price upon completed purchase.</p>
                    </div>
                </div>

                <p style="color: var(--text-secondary); font-size: 1rem; line-height: 1.7; margin-bottom: 2rem;">
                    ${escapeHtml(currentProduct.description)}
                </p>

                <!-- Quantity & Add to Cart -->
                <div style="display: flex; gap: 1rem; align-items: center; margin-bottom: 1.5rem;">
                    <div class="qty-stepper">
                        <button type="button" onclick="adjustQty(-1)">-</button>
                        <input type="text" id="qty-input" value="1" readonly>
                        <button type="button" onclick="adjustQty(1)">+</button>
                    </div>

                    <button type="button" class="btn btn-primary btn-lg" style="flex: 1;" onclick="handleAddToCart()">
                        🛍️ Add to Shopping Cart
                    </button>
                </div>

                <div style="font-size: 0.85rem; color: var(--text-muted); display: flex; gap: 1.5rem;">
                    <span>✓ In Stock (${currentProduct.stockQuantity} units)</span>
                    <span>✓ Secure Checkout</span>
                    <span>✓ Eligible for Loyalty Rewards</span>
                </div>
            </div>
        `;

    } catch (err) {
        container.innerHTML = `<div style="grid-column: span 2; text-align: center; color: var(--danger);">Failed to load product details: ${err.message}</div>`;
    }
}

function adjustQty(delta) {
    const input = document.getElementById('qty-input');
    if (!input || !currentProduct) return;
    let val = parseInt(input.value) || 1;
    val = Math.max(1, Math.min(val + delta, currentProduct.stockQuantity || 10));
    input.value = val;

    // Update dynamic points preview
    const estPoints = Math.round(currentProduct.price * val * 0.10);
    const label = document.getElementById('calc-points-label');
    if (label) {
        label.textContent = `Earn ~${estPoints} Loyalty Points`;
    }
}

async function handleAddToCart() {
    const user = Auth.getUser();
    if (!user) {
        showToast('Please sign in to add products to your cart.', 'warning');
        setTimeout(() => window.location.href = '/login.html', 1000);
        return;
    }

    const qtyInput = document.getElementById('qty-input');
    const quantity = qtyInput ? parseInt(qtyInput.value) || 1 : 1;

    try {
        const res = await apiRequest(`/cart/items?userId=${user.id}`, 'POST', {
            productId: currentProduct.id,
            quantity
        });
        if (res.success) {
            showToast(`Added ${quantity}x "${currentProduct.name}" to cart! 🛍️`);
        }
    } catch (err) {
        showToast(err.message || 'Could not add to cart.', 'error');
    }
}

async function loadReviews(productId) {
    const list = document.getElementById('reviews-list');
    const badge = document.getElementById('reviews-count-badge');
    try {
        const res = await apiRequest(`/reviews/product/${productId}`);
        if (!res.success || !res.data || res.data.length === 0) {
            if (badge) badge.textContent = '0';
            list.innerHTML = `<div style="color: var(--text-muted); text-align: center; padding: 2rem;">No customer reviews yet. Be the first to review and earn +25 points!</div>`;
            return;
        }

        if (badge) badge.textContent = res.data.length;

        list.innerHTML = res.data.map(rev => {
            const stars = '★'.repeat(rev.rating) + '☆'.repeat(5 - rev.rating);
            const reviewerName = rev.user ? rev.user.fullName : 'Verified Customer';
            return `
                <div style="padding: 1rem; background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-md);">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                        <div style="font-weight: 700; color: #fff;">${escapeHtml(reviewerName)}</div>
                        <div style="color: #fbbf24; font-size: 1rem;">${stars}</div>
                    </div>
                    <p style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.5;">${escapeHtml(rev.reviewText)}</p>
                    <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.5rem;">${formatDate(rev.createdAt)}</div>
                </div>
            `;
        }).join('');

    } catch (err) {
        console.error('Error fetching reviews:', err);
    }
}

function initStarRating() {
    const starBtns = document.querySelectorAll('.star-btn');
    starBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            selectedRating = parseInt(btn.getAttribute('data-value'));
            document.getElementById('selected-rating').value = selectedRating;
            updateStarVisuals();
        });
        btn.addEventListener('mouseenter', () => {
            const hoverVal = parseInt(btn.getAttribute('data-value'));
            highlightStars(hoverVal);
        });
    });

    const box = document.getElementById('star-rating-box');
    if (box) {
        box.addEventListener('mouseleave', () => {
            updateStarVisuals();
        });
    }

    updateStarVisuals();
}

function highlightStars(count) {
    document.querySelectorAll('.star-btn').forEach(btn => {
        const val = parseInt(btn.getAttribute('data-value'));
        btn.style.color = val <= count ? '#fbbf24' : '#64748b';
    });
}

function updateStarVisuals() {
    highlightStars(selectedRating);
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
