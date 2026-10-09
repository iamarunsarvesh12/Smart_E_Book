/**
 * Simulated Checkout and Dynamic Library Unlocking Module
 * Smart E-Book Management System
 */

document.addEventListener('DOMContentLoaded', () => {

    const contextPath = window.APP_CONTEXT_PATH || '';
    const currentUserId = window.CURRENT_USER_ID;

    const checkoutModal = document.getElementById('checkoutModal');
    const checkoutCloseBtn = document.getElementById('checkoutCloseBtn');
    const checkoutCancelBtn = document.getElementById('checkoutCancelBtn');
    const checkoutForm = document.getElementById('checkoutForm');
    
    const checkoutBookTitle = document.getElementById('checkoutBookTitle');
    const checkoutBookPrice = document.getElementById('checkoutBookPrice');
    const checkoutBookId = document.getElementById('checkoutBookId');
    const checkoutAmount = document.getElementById('checkoutAmount');
    const checkoutSubmitBtn = document.getElementById('checkoutSubmitBtn');

    // 1. Intercept Buy Clicks
    function bindCheckoutButtons() {
        const buyButtons = document.querySelectorAll('.btn-trigger-buy');
        buyButtons.forEach(btn => {
            btn.onclick = (e) => {
                e.preventDefault();

                if (!currentUserId || currentUserId <= 0) {
                    window.location.href = contextPath + '/login.jsp?error=unauthorized';
                    return;
                }

                const bookId = btn.getAttribute('data-id');
                const title = btn.getAttribute('data-title');
                const price = btn.getAttribute('data-price');

                openCheckoutModal(bookId, title, price);
            };
        });
    }

    function openCheckoutModal(bookId, title, price) {
        if (!checkoutModal) return;

        if (checkoutBookTitle) checkoutBookTitle.textContent = title;
        if (checkoutBookPrice) checkoutBookPrice.textContent = price > 0 ? '$' + Number(price).toFixed(2) : 'Free';
        if (checkoutBookId) checkoutBookId.value = bookId;
        if (checkoutAmount) checkoutAmount.value = price;

        checkoutModal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeCheckoutModal() {
        if (checkoutModal) {
            checkoutModal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    if (checkoutCloseBtn) checkoutCloseBtn.onclick = closeCheckoutModal;
    if (checkoutCancelBtn) checkoutCancelBtn.onclick = closeCheckoutModal;
    if (checkoutModal) {
        checkoutModal.onclick = (e) => {
            if (e.target === checkoutModal) closeCheckoutModal();
        };
    }

    // 2. Handle Simulated Payment Submission with Strict Validation
    if (checkoutForm) {
        checkoutForm.onsubmit = (e) => {
            e.preventDefault();

            const cardholderInput = checkoutForm.querySelector('input[type="text"][required]');
            if (cardholderInput && !cardholderInput.value.trim()) {
                alert('Please enter a valid cardholder name.');
                cardholderInput.focus();
                return;
            }

            const bookId = checkoutBookId.value;
            const amount = checkoutAmount.value;

            if (!bookId) {
                alert('Invalid publication selected for purchase.');
                return;
            }

            if (checkoutSubmitBtn) {
                checkoutSubmitBtn.disabled = true;
                checkoutSubmitBtn.innerHTML = '💳 Processing Payment...';
            }

            const formData = new URLSearchParams();
            formData.append('bookId', bookId);
            formData.append('amount', amount);

            fetch(contextPath + '/purchase', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (checkoutSubmitBtn) {
                    checkoutSubmitBtn.disabled = false;
                    checkoutSubmitBtn.innerHTML = 'Complete Purchase & Unlock &rarr;';
                }

                if (data.status === 'success' || data.status === 'already_owned') {
                    closeCheckoutModal();
                    showGlobalAlert('🎉 ' + data.message, 'success');
                    updateCardToPurchasedState(bookId);
                } else {
                    alert('Purchase failed: ' + (data.message || 'Payment simulation error.'));
                }
            })
            .catch(err => {
                if (checkoutSubmitBtn) {
                    checkoutSubmitBtn.disabled = false;
                    checkoutSubmitBtn.innerHTML = 'Complete Purchase & Unlock &rarr;';
                }
                console.error('Checkout error:', err);
                alert('Connection error during transaction processing.');
            });
        };
    }

    // 3. Dynamically update Card Buttons to Purchased State
    function updateCardToPurchasedState(bookId) {
        const cardButtons = document.querySelectorAll(`[data-id="${bookId}"]`);
        cardButtons.forEach(el => {
            const footer = el.closest('.book-card-footer');
            if (footer) {
                footer.innerHTML = `
                    <button type="button" 
                            class="btn-card-summary" 
                            data-id="${bookId}"
                            data-title="${el.getAttribute('data-title') || ''}">
                        🧠 AI Summary
                    </button>
                    <div class="purchased-btn-group">
                        <a href="${contextPath}/reader.jsp?bookId=${bookId}" class="btn-card-read">
                            📖 Read
                        </a>
                        <a href="${contextPath}/download?bookId=${bookId}" class="btn-card-download" title="Download PDF">
                            ⬇️
                        </a>
                    </div>
                `;
            }
        });
    }

    function showGlobalAlert(message, type) {
        let banner = document.getElementById('globalAlertBanner');
        if (!banner) {
            banner = document.createElement('div');
            banner.id = 'globalAlertBanner';
            banner.className = 'alert alert-' + type;
            banner.style.position = 'fixed';
            banner.style.top = '80px';
            banner.style.left = '50%';
            banner.style.transform = 'translateX(-50%)';
            banner.style.zIndex = '999';
            banner.style.boxShadow = '0 10px 30px rgba(0,0,0,0.5)';
            document.body.appendChild(banner);
        }
        banner.innerHTML = `<span>✓</span><span>${message}</span>`;
        banner.style.display = 'flex';
        setTimeout(() => {
            banner.style.display = 'none';
        }, 4000);
    }

    // Initialize initial buy triggers on page
    bindCheckoutButtons();

    // Export to window so dynamically created cards can bind too
    window.bindCheckoutButtons = bindCheckoutButtons;
    window.updateCardToPurchasedState = updateCardToPurchasedState;

});
