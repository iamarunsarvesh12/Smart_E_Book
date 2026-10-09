/**
 * Mood & Purpose Recommendation Engine and AI Summary Module
 * Smart E-Book Management System
 */

document.addEventListener('DOMContentLoaded', () => {

    const contextPath = window.APP_CONTEXT_PATH || '';
    const booksGrid = document.getElementById('books-grid');
    const moodPills = document.querySelectorAll('.mood-pill');
    const purposePills = document.querySelectorAll('.purpose-pill');

    let activeMood = 'all';
    let activePurpose = 'all';

    // -------------------------------------------------------------
    // 1. Recommendation Filtering
    // -------------------------------------------------------------
    moodPills.forEach(pill => {
        pill.addEventListener('click', (e) => {
            e.preventDefault();
            moodPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            activeMood = pill.getAttribute('data-mood') || 'all';
            fetchRecommendations();
        });
    });

    purposePills.forEach(pill => {
        pill.addEventListener('click', (e) => {
            e.preventDefault();
            purposePills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            activePurpose = pill.getAttribute('data-purpose') || 'all';
            fetchRecommendations();
        });
    });

    function fetchRecommendations() {
        const url = contextPath + '/recommend?mood=' + encodeURIComponent(activeMood) + '&purpose=' + encodeURIComponent(activePurpose);

        fetch(url)
            .then(res => {
                if (!res.ok) throw new Error('HTTP error ' + res.status);
                return res.json();
            })
            .then(books => {
                renderRecommendationGrid(books);
            })
            .catch(err => {
                console.error('Recommendation fetch error:', err);
            });
    }

    function renderRecommendationGrid(books) {
        if (!booksGrid) return;
        booksGrid.innerHTML = '';

        if (!books || books.length === 0) {
            booksGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🎯</div>
                    <h3>No matches for this Mindset & Purpose combination</h3>
                    <p>Try selecting <strong>"All Moods"</strong> or <strong>"All Purposes"</strong> to broaden your discovery criteria.</p>
                </div>
            `;
            return;
        }

        const purchasedIds = window.PURCHASED_BOOK_IDS || [];
        const isAdmin = window.IS_ADMIN === true;

        books.forEach(book => {
            let moodClass = 'badge-focus';
            const m = (book.mood || '').toLowerCase();
            if (m === 'relaxed') moodClass = 'badge-relaxed';
            else if (m === 'inspired') moodClass = 'badge-inspired';
            else if (m === 'uplifting') moodClass = 'badge-uplifting';
            else if (m === 'calm') moodClass = 'badge-calm';

            const priceNum = (book.price) ? Number(book.price) : 0;
            const priceDisplay = priceNum > 0 ? `$${priceNum.toFixed(2)}` : 'Free';
            const isOwned = (priceNum === 0) || isAdmin || purchasedIds.includes(book.id);

            const coverUrl = contextPath + '/' + book.coverPath;

            let actionHtml = '';
            if (isOwned) {
                actionHtml = `
                    <div class="purchased-btn-group">
                        <a href="${contextPath}/reader.jsp?bookId=${book.id}" class="btn-card-read">
                            📖 Read
                        </a>
                        <a href="${contextPath}/download?bookId=${book.id}" class="btn-card-download" title="Download PDF">
                            ⬇️
                        </a>
                    </div>
                `;
            } else {
                actionHtml = `
                    <button type="button" 
                            class="btn-card-buy btn-trigger-buy" 
                            data-id="${book.id}"
                            data-title="${escapeHtml(book.title)}"
                            data-price="${priceNum}">
                        💳 Buy ${priceDisplay}
                    </button>
                `;
            }

            const card = document.createElement('div');
            card.className = 'book-card';
            card.innerHTML = `
                <div class="book-cover-wrapper">
                    <img src="${coverUrl}" 
                         alt="${escapeHtml(book.title)}" 
                         class="book-cover-img"
                         onerror="this.src='https://via.placeholder.com/260x340/1e293b/94a3b8?text=${encodeURIComponent(book.title)}';">
                    <div class="book-price-tag">${priceDisplay}</div>
                </div>
                <div class="book-card-body">
                    <div class="book-category">${escapeHtml(book.category || 'General')}</div>
                    <h3 class="book-title" title="${escapeHtml(book.title)}">${escapeHtml(book.title)}</h3>
                    <div class="book-author">by ${escapeHtml(book.author || 'Unknown')}</div>
                    <div class="book-tags">
                        <span class="badge-mood ${moodClass}">${escapeHtml(book.mood || '')}</span>
                        <span class="badge-purpose">${escapeHtml(book.purpose || '')}</span>
                    </div>
                    <div class="book-card-footer">
                        <button type="button" 
                                class="btn-card-summary" 
                                data-id="${book.id}"
                                data-title="${escapeHtml(book.title)}">
                            🧠 AI Summary
                        </button>
                        ${actionHtml}
                    </div>
                </div>
            `;
            booksGrid.appendChild(card);
        });

        // Re-bind modal buttons and checkout buttons
        bindAISummaryButtons();
        if (window.bindCheckoutButtons) {
            window.bindCheckoutButtons();
        }
    }

    // -------------------------------------------------------------
    // 2. AI Summary Modal Handler
    // -------------------------------------------------------------
    const modalBackdrop = document.getElementById('summaryModal');
    const modalTitle = document.getElementById('modalBookTitle');
    const modalMeta = document.getElementById('modalBookMeta');
    const modalBody = document.getElementById('modalBookSummary');
    const modalCloseBtn = document.getElementById('modalCloseBtn');
    const modalOkBtn = document.getElementById('modalOkBtn');

    function bindAISummaryButtons() {
        const summaryBtns = document.querySelectorAll('.btn-card-summary');
        summaryBtns.forEach(btn => {
            btn.onclick = (e) => {
                e.preventDefault();
                const bookId = btn.getAttribute('data-id');
                const fallbackTitle = btn.getAttribute('data-title') || 'Book Synopsis';
                openAISummary(bookId, fallbackTitle);
            };
        });
    }

    function openAISummary(bookId, title) {
        if (!modalBackdrop || !modalBody) return;

        if (modalTitle) modalTitle.textContent = title;
        if (modalMeta) modalMeta.innerHTML = '';
        
        modalBody.innerHTML = `
            <div style="text-align: center; padding: 2rem 1rem;">
                <div class="pulse-dot" style="margin: 0 auto 1rem; width: 14px; height: 14px;"></div>
                <p style="color: #9ca3af; font-size: 0.95rem;">Analyzing book content and structuring executive AI summary...</p>
            </div>
        `;
        modalBackdrop.classList.add('active');
        document.body.style.overflow = 'hidden';

        if (!bookId) {
            modalBody.innerHTML = `<p>${escapeHtml(btn.getAttribute('data-summary') || 'No summary available.')}</p>`;
            return;
        }

        fetch(contextPath + '/book-summary?bookId=' + encodeURIComponent(bookId))
            .then(res => res.json())
            .then(data => {
                if (data.status === 'success') {
                    if (modalTitle) modalTitle.textContent = data.title;
                    if (modalMeta) {
                        modalMeta.innerHTML = `
                            <div style="display: flex; gap: 0.5rem; align-items: center; margin-top: 0.35rem; flex-wrap: wrap;">
                                <span style="font-size: 0.85rem; color: #9ca3af;">by ${escapeHtml(data.author)}</span>
                                <span class="badge-mood badge-focus">${escapeHtml(data.mood)}</span>
                                <span class="badge-purpose">${escapeHtml(data.purpose)}</span>
                            </div>
                        `;
                    }
                    modalBody.innerHTML = formatAISummaryHtml(data.summary);
                } else {
                    modalBody.innerHTML = `<p style="color: #f87171;">Unable to load AI summary: ${escapeHtml(data.message || 'Unknown error')}</p>`;
                }
            })
            .catch(err => {
                console.error('Error fetching AI summary:', err);
                modalBody.innerHTML = `<p style="color: #f87171;">Failed to connect to AI Summary Engine.</p>`;
            });
    }

    function formatAISummaryHtml(rawText) {
        if (!rawText) return '<p>No summary available.</p>';
        const lines = rawText.split('\n');
        let html = '';

        lines.forEach(line => {
            const trimmed = line.trim();
            if (!trimmed) {
                html += '<br>';
            } else if (trimmed.startsWith('📌') || trimmed.startsWith('💡') || trimmed.startsWith('🎯')) {
                html += `<h4 style="color: #818cf8; font-size: 1.05rem; font-weight: 700; margin: 1.25rem 0 0.5rem;">${escapeHtml(trimmed)}</h4>`;
            } else if (trimmed.startsWith('•')) {
                html += `<div style="padding-left: 1rem; margin-bottom: 0.35rem; color: #e2e8f0;">${escapeHtml(trimmed)}</div>`;
            } else {
                html += `<p style="margin-bottom: 0.65rem; color: #cbd5e1; line-height: 1.6;">${escapeHtml(trimmed)}</p>`;
            }
        });

        return html;
    }

    function closeSummaryModal() {
        if (modalBackdrop) {
            modalBackdrop.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    if (modalCloseBtn) modalCloseBtn.onclick = closeSummaryModal;
    if (modalOkBtn) modalOkBtn.onclick = closeSummaryModal;
    if (modalBackdrop) {
        modalBackdrop.onclick = (e) => {
            if (e.target === modalBackdrop) closeSummaryModal();
        };
    }

    // Initialize buttons on initial server load
    bindAISummaryButtons();

    function escapeHtml(text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

});
