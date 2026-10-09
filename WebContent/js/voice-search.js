/**
 * Voice & Real-Time Text Search Module
 * Web Speech API + Vanilla JavaScript AJAX Grid Renderer
 * Smart E-Book Management System
 */

document.addEventListener('DOMContentLoaded', () => {
    
    // Core Elements
    const searchInput = document.getElementById('search-input');
    const micBtn = document.getElementById('mic-btn');
    const clearBtn = document.getElementById('clear-btn');
    const voiceStatus = document.getElementById('voice-status');
    const booksGrid = document.getElementById('books-grid');
    const contextPath = window.APP_CONTEXT_PATH || '';

    let debounceTimer = null;
    let recognition = null;
    let isListening = false;

    // -------------------------------------------------------------
    // 1. Web Speech API Initialization
    // -------------------------------------------------------------
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

    if (SpeechRecognition) {
        recognition = new SpeechRecognition();
        recognition.continuous = false;
        recognition.interimResults = false;
        recognition.lang = 'en-US';

        recognition.onstart = () => {
            isListening = true;
            micBtn.classList.add('listening');
            micBtn.setAttribute('title', 'Listening... Click to cancel');
            if (voiceStatus) {
                voiceStatus.textContent = '🎙️ Listening... Speak book title, author, or category';
                voiceStatus.classList.remove('hidden');
            }
        };

        recognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            if (searchInput) {
                searchInput.value = transcript;
                toggleClearButton(true);
            }
            stopListening();
            if (voiceStatus) {
                voiceStatus.textContent = '✓ Heard: "' + transcript + '"';
                setTimeout(() => {
                    voiceStatus.classList.add('hidden');
                }, 3000);
            }
            fetchSearch(transcript);
        };

        recognition.onerror = (event) => {
            console.warn('Speech recognition error:', event.error);
            stopListening();
            if (voiceStatus) {
                if (event.error === 'not-allowed') {
                    voiceStatus.textContent = '⚠ Microphone permission denied.';
                } else if (event.error === 'no-speech') {
                    voiceStatus.textContent = 'ℹ No speech detected. Please try again.';
                } else {
                    voiceStatus.textContent = '⚠ Speech recognition issue: ' + event.error;
                }
                setTimeout(() => {
                    voiceStatus.classList.add('hidden');
                }, 3500);
            }
        };

        recognition.onend = () => {
            stopListening();
        };

    } else {
        if (micBtn) {
            micBtn.setAttribute('title', 'Voice search not supported in this browser');
            micBtn.style.opacity = '0.5';
            micBtn.style.cursor = 'not-allowed';
        }
    }

    function toggleListening() {
        if (!recognition) {
            alert('Voice search is not supported in your current browser. Please use Chrome, Edge, or Safari.');
            return;
        }

        if (isListening) {
            recognition.stop();
            stopListening();
        } else {
            try {
                recognition.start();
            } catch (err) {
                console.error('Error starting speech recognition:', err);
                stopListening();
            }
        }
    }

    function stopListening() {
        isListening = false;
        if (micBtn) {
            micBtn.classList.remove('listening');
            micBtn.setAttribute('title', 'Click to search with your voice');
        }
    }

    if (micBtn) {
        micBtn.addEventListener('click', (e) => {
            e.preventDefault();
            toggleListening();
        });
    }

    // -------------------------------------------------------------
    // 2. Real-Time Debounced Text Search
    // -------------------------------------------------------------
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            const query = searchInput.value;
            toggleClearButton(query.trim().length > 0);

            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                fetchSearch(query.trim());
            }, 300);
        });

        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                clearTimeout(debounceTimer);
                fetchSearch(searchInput.value.trim());
            }
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (searchInput) {
                searchInput.value = '';
                searchInput.focus();
            }
            toggleClearButton(false);
            fetchSearch('');
            resetActiveCategoryPill();
        });
    }

    function toggleClearButton(visible) {
        if (clearBtn) {
            if (visible) {
                clearBtn.classList.add('visible');
            } else {
                clearBtn.classList.remove('visible');
            }
        }
    }

    // -------------------------------------------------------------
    // 3. Category Filter Pills
    // -------------------------------------------------------------
    const categoryPills = document.querySelectorAll('.category-pill');
    categoryPills.forEach(pill => {
        pill.addEventListener('click', (e) => {
            e.preventDefault();
            categoryPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');

            const selectedCategory = pill.getAttribute('data-category');
            if (selectedCategory === 'ALL') {
                if (searchInput) searchInput.value = '';
                toggleClearButton(false);
                fetchSearch('');
            } else {
                if (searchInput) searchInput.value = selectedCategory;
                toggleClearButton(true);
                fetchSearch(selectedCategory);
            }
        });
    });

    function resetActiveCategoryPill() {
        categoryPills.forEach(p => {
            if (p.getAttribute('data-category') === 'ALL') {
                p.classList.add('active');
            } else {
                p.classList.remove('active');
            }
        });
    }

    // -------------------------------------------------------------
    // 4. AJAX Fetch & Dynamic DOM Grid Renderer
    // -------------------------------------------------------------
    function fetchSearch(query) {
        const url = contextPath + '/search?query=' + encodeURIComponent(query || '');

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('HTTP error ' + response.status);
                }
                return response.json();
            })
            .then(books => {
                renderBooksGrid(books, query);
            })
            .catch(error => {
                console.error('Search request failed:', error);
            });
    }

    function renderBooksGrid(books, query) {
        if (!booksGrid) return;
        booksGrid.innerHTML = '';

        if (!books || books.length === 0) {
            booksGrid.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🔍</div>
                    <h3>No matching books found</h3>
                    <p>We couldn't find any books matching <strong>"${escapeHtml(query || '')}"</strong>. Try speaking or typing another author, title, or category.</p>
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

        // Re-bind actions
        if (window.bindCheckoutButtons) {
            window.bindCheckoutButtons();
        }
    }

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
