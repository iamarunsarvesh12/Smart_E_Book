/**
 * In-Browser PDF Reader & Bookmark Tracking Script
 * Smart E-Book Management System
 */

document.addEventListener('DOMContentLoaded', () => {

    const contextPath = window.APP_CONTEXT_PATH || '';
    const bookId = window.BOOK_ID;
    const pageInput = document.getElementById('currentPage');
    const saveBtn = document.getElementById('saveBookmarkBtn');
    const toast = document.getElementById('bookmarkToast');
    const toastMessage = document.getElementById('toastMessage');

    if (!bookId) return;

    // 1. Fetch saved reading bookmark on load
    fetch(contextPath + '/bookmark?bookId=' + encodeURIComponent(bookId))
        .then(res => res.json())
        .then(data => {
            if (data.lastPage && data.lastPage > 1) {
                if (pageInput) pageInput.value = data.lastPage;
                showToast('📖 Resumed reading progress at Page ' + data.lastPage);
            }
        })
        .catch(err => {
            console.warn('Could not retrieve bookmark:', err);
        });

    // 2. Input Boundary Sanity Enforcement
    if (pageInput) {
        pageInput.addEventListener('change', () => {
            let val = parseInt(pageInput.value, 10);
            if (isNaN(val) || val < 1) {
                pageInput.value = 1;
            }
        });
    }

    // 3. Save Progress Button Click with Boundary Check
    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            let pageNum = pageInput ? parseInt(pageInput.value, 10) : 1;
            if (isNaN(pageNum) || pageNum < 1) {
                pageNum = 1;
                if (pageInput) pageInput.value = 1;
            }

            const formData = new URLSearchParams();
            formData.append('bookId', bookId);
            formData.append('pageNumber', pageNum);

            fetch(contextPath + '/bookmark', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (data.status === 'saved') {
                    showToast('✓ Reading bookmark saved at Page ' + pageNum + '!');
                } else {
                    showToast('⚠ Failed to save bookmark.');
                }
            })
            .catch(err => {
                console.error('Error saving bookmark:', err);
                showToast('⚠ Connection error while saving bookmark.');
            });
        });
    }

    function showToast(message) {
        if (!toast || !toastMessage) return;
        toastMessage.textContent = message;
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3500);
    }

});
