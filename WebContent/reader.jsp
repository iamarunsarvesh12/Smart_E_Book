<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.ebook.model.User" %>
<%@ page import="com.ebook.model.Book" %>
<%@ page import="com.ebook.dao.BookDAO" %>
<%@ page import="com.ebook.dao.PurchaseDAO" %>
<%@ page import="java.math.BigDecimal" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=unauthorized");
        return;
    }

    String bookIdStr = request.getParameter("bookId");
    if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/catalog");
        return;
    }

    int bookId = 0;
    try {
        bookId = Integer.parseInt(bookIdStr.trim());
    } catch (NumberFormatException e) {
        response.sendRedirect(request.getContextPath() + "/catalog");
        return;
    }

    BookDAO bookDAO = new BookDAO();
    Book book = bookDAO.getBookById(bookId);

    if (book == null) {
        response.sendRedirect(request.getContextPath() + "/catalog");
        return;
    }

    PurchaseDAO purchaseDAO = new PurchaseDAO();
    boolean isFree = (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) == 0);
    boolean isAdmin = currentUser.isAdmin();
    boolean hasPurchased = purchaseDAO.hasUserPurchased(currentUser.getId(), bookId);

    if (!isFree && !isAdmin && !hasPurchased) {
        response.sendRedirect(request.getContextPath() + "/catalog?error=unpurchased");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reading: <%= book.getTitle() %> - Smart E-Book</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/reader.css">
</head>
<body class="reader-body">

    <!-- Top Reader Navigation & Action Bar -->
    <header class="reader-header">
        <div class="reader-nav-left">
            <a href="${pageContext.request.contextPath}/catalog" class="reader-back-btn">
                &larr; Exit Reader
            </a>
            <div class="reader-book-info">
                <h2 title="<%= book.getTitle() %>"><%= book.getTitle() %></h2>
                <span>by <%= book.getAuthor() %></span>
            </div>
        </div>

        <div class="reader-controls-right">
            
            <!-- Bookmark Tracker -->
            <div class="page-tracker">
                <span>Page</span>
                <input type="number" id="currentPage" class="page-input" value="1" min="1">
            </div>

            <button type="button" id="saveBookmarkBtn" class="btn-bookmark">
                🔖 Save Progress
            </button>

            <!-- Download Attachment Shortcut -->
            <a href="${pageContext.request.contextPath}/download?bookId=<%= book.getId() %>" class="btn-download-shortcut">
                ⬇️ Download PDF
            </a>

        </div>
    </header>

    <!-- Embedded Secure Streaming Reader Viewer -->
    <main class="reader-frame-container">
        <iframe id="pdf-viewer" 
                class="reader-frame" 
                src="${pageContext.request.contextPath}/stream-book?bookId=<%= book.getId() %>"
                title="<%= book.getTitle() %> Viewer">
        </iframe>
    </main>

    <!-- Bookmark Toast Notification -->
    <div id="bookmarkToast" class="bookmark-toast">
        <span id="toastMessage">✓ Bookmark saved</span>
    </div>

    <!-- Script Configuration -->
    <script>
        window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
        window.BOOK_ID = <%= book.getId() %>;
    </script>
    <script src="${pageContext.request.contextPath}/js/reader.js"></script>

</body>
</html>
