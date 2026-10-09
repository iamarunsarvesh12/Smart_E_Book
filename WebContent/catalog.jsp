<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.ebook.model.User" %>
<%@ page import="com.ebook.model.Book" %>
<%@ page import="com.ebook.dao.BookDAO" %>
<%@ page import="com.ebook.dao.PurchaseDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.math.BigDecimal" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");

    // Fallback if accessed directly without passing through CatalogServlet
    List<Book> booksList = (List<Book>) request.getAttribute("booksList");
    List<String> categories = (List<String>) request.getAttribute("categories");
    List<String> allMoods = (List<String>) request.getAttribute("allMoods");
    List<String> allPurposes = (List<String>) request.getAttribute("allPurposes");
    Set<Integer> purchasedBookIds = (Set<Integer>) request.getAttribute("purchasedBookIds");
    String searchQuery = (String) request.getAttribute("searchQuery");

    if (booksList == null) {
        BookDAO bookDAO = new BookDAO();
        PurchaseDAO purchaseDAO = new PurchaseDAO();

        searchQuery = request.getParameter("q");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            booksList = bookDAO.searchBooks(searchQuery.trim());
        } else {
            booksList = bookDAO.getAllBooks();
        }
        categories = bookDAO.getAllCategories();
        allMoods = bookDAO.getAllMoods();
        allPurposes = bookDAO.getAllPurposes();

        if (currentUser != null) {
            purchasedBookIds = purchaseDAO.getPurchasedBookIds(currentUser.getId());
        } else {
            purchasedBookIds = new HashSet<Integer>();
        }
    }

    if (purchasedBookIds == null) {
        purchasedBookIds = new HashSet<Integer>();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Curated Library & AI Storefront - Smart E-Book</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css">
</head>
<body>

    <!-- Header Navbar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/index.jsp" class="brand">
            <div class="brand-icon">📚</div>
            <span>Smart E-Book Library</span>
        </a>
        <nav class="nav-links">
            <a href="${pageContext.request.contextPath}/index.jsp" class="nav-link">Home</a>
            <a href="${pageContext.request.contextPath}/catalog" class="nav-link" style="color: #ffffff; font-weight: 700;">Catalog</a>
            <% if (currentUser != null) { %>
                <span class="nav-link" style="color: #a5b4fc;">
                    Hello, <strong><%= currentUser.getName() %></strong>
                </span>
                <% if (currentUser.isAdmin()) { %>
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary">
                        Admin Portal
                    </a>
                <% } %>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary">
                    Sign Out
                </a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-secondary">Sign In</a>
                <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-primary">Sign Up</a>
            <% } %>
        </nav>
    </header>

    <main class="container">

        <% if ("unpurchased".equals(request.getParameter("error"))) { %>
            <div class="alert alert-error" style="margin-bottom: 2rem;">
                <span>⛔</span>
                <span>Access Denied: Please unlock or purchase this publication before accessing the online reader.</span>
            </div>
        <% } %>
        
        <!-- Search Hero Section -->
        <section class="catalog-hero">
            <h1 class="hero-title" style="font-size: 2.6rem; margin-bottom: 0.75rem;">
                Explore Books by <span class="gradient-text">Mood, Purpose & AI</span>
            </h1>
            <p class="hero-subtitle" style="font-size: 1.05rem; margin-bottom: 1.5rem;">
                Intelligent recommendations, real-time voice & text search, secure in-browser streaming, and executive AI synopses.
            </p>

            <!-- Search Input Container -->
            <div class="search-container">
                <div class="search-input-wrapper">
                    <span class="search-icon-left">🔍</span>
                    <input type="text" 
                           id="search-input" 
                           class="search-input" 
                           placeholder="Search by title, author, or category (e.g. Focus, Jenkins, Technology)..." 
                           value="<%= searchQuery != null ? searchQuery : "" %>"
                           autocomplete="off">
                    
                    <div class="search-actions">
                        <button type="button" 
                                id="clear-btn" 
                                class="clear-btn <%= (searchQuery != null && !searchQuery.isEmpty()) ? "visible" : "" %>" 
                                title="Clear search">
                            ✖
                        </button>
                        <button type="button" 
                                id="mic-btn" 
                                class="mic-btn" 
                                title="Click to search with your voice">
                            🎙️
                        </button>
                    </div>
                </div>
            </div>

            <!-- Voice Status Indicator -->
            <div id="voice-status" class="voice-status-bar hidden"></div>
        </section>

        <!-- Recommendation Engine Dual-Filter Control Center -->
        <section class="recommendation-panel">
            
            <!-- Row 1: Mood Filter Bar -->
            <div class="filter-row">
                <div class="filter-label">
                    <span>🧠 Reading Mood:</span>
                </div>
                <div class="filter-pills">
                    <button type="button" class="mood-pill active" data-mood="all">
                        ✨ All Moods
                    </button>
                    <% if (allMoods != null) { %>
                        <% for (String mood : allMoods) { %>
                            <button type="button" class="mood-pill" data-mood="<%= mood %>">
                                <%= mood %>
                            </button>
                        <% } %>
                    <% } %>
                </div>
            </div>

            <!-- Row 2: Purpose Filter Bar -->
            <div class="filter-row">
                <div class="filter-label">
                    <span>🎯 Goal Purpose:</span>
                </div>
                <div class="filter-pills">
                    <button type="button" class="purpose-pill active" data-purpose="all">
                        🚀 All Purposes
                    </button>
                    <% if (allPurposes != null) { %>
                        <% for (String purpose : allPurposes) { %>
                            <button type="button" class="purpose-pill" data-purpose="<%= purpose %>">
                                <%= purpose %>
                            </button>
                        <% } %>
                    <% } %>
                </div>
            </div>

            <!-- Row 3: Category Filter Bar -->
            <div class="filter-row" style="margin-top: 0.5rem; padding-top: 0.75rem; border-top: 1px dashed rgba(255,255,255,0.08);">
                <div class="filter-label">
                    <span>🏷️ Categories:</span>
                </div>
                <div class="filter-pills">
                    <button type="button" class="category-pill active" data-category="ALL">
                        All Categories
                    </button>
                    <% if (categories != null) { %>
                        <% for (String cat : categories) { %>
                            <button type="button" class="category-pill" data-category="<%= cat %>">
                                <%= cat %>
                            </button>
                        <% } %>
                    <% } %>
                </div>
            </div>

        </section>

        <!-- Books Grid Area -->
        <div id="books-grid" class="books-grid">
            <% if (booksList != null && !booksList.isEmpty()) { %>
                <% for (Book book : booksList) { 
                    String moodClass = "badge-focus";
                    if ("Relaxed".equalsIgnoreCase(book.getMood())) moodClass = "badge-relaxed";
                    else if ("Inspired".equalsIgnoreCase(book.getMood())) moodClass = "badge-inspired";
                    else if ("Uplifting".equalsIgnoreCase(book.getMood())) moodClass = "badge-uplifting";
                    else if ("Calm".equalsIgnoreCase(book.getMood())) moodClass = "badge-calm";

                    boolean isFree = (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) == 0);
                    boolean isAdmin = (currentUser != null && currentUser.isAdmin());
                    boolean isPurchased = (purchasedBookIds != null && purchasedBookIds.contains(book.getId()));
                    boolean canRead = (isFree || isAdmin || isPurchased);

                    String priceDisplay = (!isFree) ? "$" + book.getPrice() : "Free";
                    double priceVal = (!isFree) ? book.getPrice().doubleValue() : 0.00;
                %>
                    <div class="book-card">
                        <div class="book-cover-wrapper">
                            <img src="${pageContext.request.contextPath}/<%= book.getCoverPath() %>" 
                                 alt="<%= book.getTitle() %>" 
                                 class="book-cover-img"
                                 onerror="this.src='https://via.placeholder.com/260x340/1e293b/94a3b8?text=<%= java.net.URLEncoder.encode(book.getTitle(), "UTF-8") %>';">
                            <div class="book-price-tag"><%= priceDisplay %></div>
                        </div>
                        <div class="book-card-body">
                            <div class="book-category"><%= book.getCategory() != null ? book.getCategory() : "General" %></div>
                            <h3 class="book-title" title="<%= book.getTitle() %>"><%= book.getTitle() %></h3>
                            <div class="book-author">by <%= book.getAuthor() %></div>
                            
                            <div class="book-tags">
                                <span class="badge-mood <%= moodClass %>"><%= book.getMood() %></span>
                                <span class="badge-purpose"><%= book.getPurpose() %></span>
                            </div>

                            <div class="book-card-footer">
                                <button type="button" 
                                        class="btn-card-summary" 
                                        data-id="<%= book.getId() %>"
                                        data-title="<%= book.getTitle().replace("\"", "&quot;") %>">
                                    🧠 AI Summary
                                </button>
                                
                                <% if (canRead) { %>
                                    <div class="purchased-btn-group">
                                        <a href="${pageContext.request.contextPath}/reader.jsp?bookId=<%= book.getId() %>" class="btn-card-read">
                                            📖 Read
                                        </a>
                                        <a href="${pageContext.request.contextPath}/download?bookId=<%= book.getId() %>" class="btn-card-download" title="Download PDF">
                                            ⬇️
                                        </a>
                                    </div>
                                <% } else { %>
                                    <button type="button" 
                                            class="btn-card-buy btn-trigger-buy" 
                                            data-id="<%= book.getId() %>"
                                            data-title="<%= book.getTitle().replace("\"", "&quot;") %>"
                                            data-price="<%= priceVal %>">
                                        💳 Buy <%= priceDisplay %>
                                    </button>
                                <% } %>

                            </div>
                        </div>
                    </div>
                <% } %>
            <% } else { %>
                <div class="empty-state">
                    <div class="empty-state-icon">📚</div>
                    <h3>No books available in the catalog</h3>
                    <p>There are currently no publications matching your filter criteria.</p>
                </div>
            <% } %>
        </div>

    </main>

    <!-- AI Summary Enhanced Modal Dialog -->
    <div id="summaryModal" class="modal-backdrop">
        <div class="modal-dialog">
            <div class="modal-header">
                <div>
                    <h3 id="modalBookTitle">Executive AI Synopsis</h3>
                    <div id="modalBookMeta"></div>
                </div>
                <button type="button" id="modalCloseBtn" class="modal-close-btn">&times;</button>
            </div>
            <div id="modalBookSummary" class="modal-body">
                Summary loading...
            </div>
            <div class="modal-footer">
                <button type="button" id="modalOkBtn" class="btn btn-primary" style="padding: 0.55rem 1.4rem;">
                    Close Synopsis
                </button>
            </div>
        </div>
    </div>

    <!-- Simulated Checkout Modal Dialog -->
    <div id="checkoutModal" class="modal-backdrop">
        <div class="modal-dialog" style="max-width: 520px;">
            <div class="modal-header">
                <h3>Unlock Publication</h3>
                <button type="button" id="checkoutCloseBtn" class="modal-close-btn">&times;</button>
            </div>
            <div class="modal-body">
                <div class="checkout-order-summary">
                    <div>
                        <h4 id="checkoutBookTitle">Book Title</h4>
                        <span>Instant In-Browser Streaming & PDF Download</span>
                    </div>
                    <div id="checkoutBookPrice" class="checkout-price-badge">$0.00</div>
                </div>

                <form id="checkoutForm">
                    <input type="hidden" id="checkoutBookId" name="bookId">
                    <input type="hidden" id="checkoutAmount" name="amount">

                    <div class="form-group">
                        <label>Simulated Card Number</label>
                        <input type="text" class="form-control" value="4532 •••• •••• 8821" readonly>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="form-group">
                            <label>Cardholder Name</label>
                            <input type="text" class="form-control" value="<%= (currentUser != null) ? currentUser.getName() : "Reader" %>" required>
                        </div>
                        <div class="form-group">
                            <label>Expiry / CVV</label>
                            <input type="text" class="form-control" value="12/28 • 842" readonly>
                        </div>
                    </div>

                    <div style="margin-top: 1rem;">
                        <button type="submit" id="checkoutSubmitBtn" class="auth-submit-btn" style="margin-top: 0;">
                            Complete Purchase & Unlock &rarr;
                        </button>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" id="checkoutCancelBtn" class="btn btn-secondary">
                    Cancel
                </button>
            </div>
        </div>
    </div>

    <!-- Footer -->
    <footer class="footer">
        <p>&copy; <%= java.time.Year.now().getValue() %> Smart E-Book Management System • E-Commerce & In-Browser Reader</p>
    </footer>

    <!-- Global App Context & Client Scripts -->
    <script>
        window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
        window.CURRENT_USER_ID = <%= (currentUser != null) ? currentUser.getId() : -1 %>;
        window.IS_ADMIN = <%= (currentUser != null && currentUser.isAdmin()) ? "true" : "false" %>;
        window.PURCHASED_BOOK_IDS = [
            <% 
                if (purchasedBookIds != null && !purchasedBookIds.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Integer id : purchasedBookIds) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(id);
                    }
                    out.print(sb.toString());
                }
            %>
        ];
    </script>
    <script src="${pageContext.request.contextPath}/js/voice-search.js"></script>
    <script src="${pageContext.request.contextPath}/js/recommendations.js"></script>
    <script src="${pageContext.request.contextPath}/js/checkout.js"></script>

</body>
</html>
