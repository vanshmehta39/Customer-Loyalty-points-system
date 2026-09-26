/**
 * Products Catalog Logic
 */

let currentCategory = 'All';
let currentSearch = '';
let searchTimeout = null;

document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    updateCartCount();

    // Search input listener
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                currentSearch = e.target.value.trim();
                loadProducts();
            }, 300);
        });
    }
});

async function loadProducts() {
    const grid = document.getElementById('products-grid');
    grid.innerHTML = `<div style="grid-column: span 4; text-align: center; padding: 3rem; color: var(--text-muted);">Fetching products...</div>`;

    let url = `/products?`;
    if (currentCategory && currentCategory !== 'All') {
        url += `category=${encodeURIComponent(currentCategory)}&`;
    }
    if (currentSearch) {
        url += `search=${encodeURIComponent(currentSearch)}&`;
    }

    try {
        const res = await apiRequest(url);
        if (!res.success || !res.data || res.data.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: span 4; text-align: center; padding: 4rem; color: var(--text-muted);">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">🔍</div>
                    <h3>No products found</h3>
                    <p style="margin-top: 0.5rem;">Try modifying your search or choosing another category.</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = res.data.map(p => {
            const approxPoints = Math.round(p.price * 0.10);
            return `
                <div class="product-card">
                    <div class="product-image-wrap">
                        <img src="${p.imageUrl}" alt="${escapeHtml(p.name)}" loading="lazy">
                        <span class="product-category-tag">${escapeHtml(p.category)}</span>
                    </div>

                    <div class="product-card-body">
                        <h3 class="product-card-title">${escapeHtml(p.name)}</h3>
                        
                        <div class="product-rating">
                            <span>★</span>
                            <strong style="color: #fff;">${p.rating || 4.5}</strong>
                            <span class="product-rating-count">(${p.reviewsCount || 0} reviews)</span>
                        </div>

                        <div class="product-points-hint" style="margin-bottom: 0.875rem;">
                            🪙 Earn ~${approxPoints} loyalty points
                        </div>

                        <div class="product-card-footer">
                            <div class="product-price">${formatINR(p.price)}</div>
                            <div style="display: flex; gap: 0.5rem;">
                                <a href="/product-details.html?id=${p.id}" class="btn btn-secondary btn-sm" title="View Details">
                                    Details
                                </a>
                                <button type="button" class="btn btn-primary btn-sm" onclick="addProductToCart(${p.id}, '${escapeHtml(p.name)}')">
                                    Add
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

    } catch (err) {
        grid.innerHTML = `<div style="grid-column: span 4; text-align: center; color: var(--danger); padding: 3rem;">Error loading products: ${err.message}</div>`;
    }
}

function setCategory(cat) {
    currentCategory = cat;
    document.querySelectorAll('#category-pills .filter-pill').forEach(btn => {
        if (btn.textContent.trim().toLowerCase() === cat.toLowerCase() || (cat === 'All' && btn.textContent.includes('All'))) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
    loadProducts();
}

function resetFilters() {
    currentCategory = 'All';
    currentSearch = '';
    const searchInput = document.getElementById('search-input');
    if (searchInput) searchInput.value = '';
    setCategory('All');
}

async function addProductToCart(productId, productName) {
    const user = Auth.getUser();
    if (!user) {
        showToast('Please sign in to add products to your cart.', 'warning');
        setTimeout(() => window.location.href = '/login.html', 1000);
        return;
    }

    try {
        const res = await apiRequest(`/cart/items?userId=${user.id}`, 'POST', {
            productId,
            quantity: 1
        });
        if (res.success) {
            showToast(`Added "${productName}" to your cart! 🛍️`);
            updateCartCount();
        }
    } catch (err) {
        showToast(err.message || 'Could not add to cart.', 'error');
    }
}

async function updateCartCount() {
    const user = Auth.getUser();
    if (!user) return;

    try {
        const res = await apiRequest(`/cart?userId=${user.id}`);
        if (res.success && res.data) {
            const countElem = document.getElementById('cart-item-count');
            if (countElem) {
                countElem.textContent = res.data.itemCount || 0;
            }
        }
    } catch (ignored) {}
}

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
