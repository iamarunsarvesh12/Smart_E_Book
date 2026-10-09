<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.ebook.model.User" %>
<%@ page import="com.ebook.model.Book" %>
<%@ page import="com.ebook.dao.BookDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.math.BigDecimal" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null || !currentUser.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=unauthorized");
        return;
    }

    // Ensure booksList is available even if directly accessed
    List<Book> booksList = (List<Book>) request.getAttribute("booksList");
    if (booksList == null) {
        BookDAO bookDAO = new BookDAO();
        booksList = bookDAO.getAllBooks();
    }

    int totalBooks = booksList != null ? booksList.size() : 0;
    Set<String> categories = new HashSet<>();
    if (booksList != null) {
        for (Book b : booksList) {
            if (b.getCategory() != null && !b.getCategory().trim().isEmpty()) {
                categories.add(b.getCategory().trim());
            }
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Smart E-Book Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>

    <!-- Admin Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="brand">
            <div class="brand-icon">🛡️</div>
            <span>Smart E-Book Admin</span>
        </a>
        <nav class="nav-links">
            <span class="nav-link" style="color: #cbd5e1;">
                Admin: <strong style="color: #38bdf8;"><%= currentUser.getEmail() %></strong>
            </span>
            <a href="${pageContext.request.contextPath}/catalog" class="nav-link" target="_blank">
                Reader Catalog View ↗
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary">
                Sign Out
            </a>
        </nav>
    </header>

    <!-- Main Admin Container -->
    <main class="container">
        
        <!-- Header Title Area -->
        <div class="admin-header">
            <div class="admin-title-area">
                <h1>Book Management & Inventory</h1>
                <p>Upload new e-books, manage mood/purpose taxonomy, and control library catalog</p>
            </div>
            <button type="button" class="collapse-toggle" onclick="toggleAddBookForm()">
                <span id="toggleIcon">➕</span> <span id="toggleText">Add New E-Book</span>
            </button>
        </div>

        <!-- System Alerts / Flash Messages -->
        <% String successParam = request.getParameter("success"); %>
        <% if ("added".equals(successParam)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <span>E-Book and digital assets successfully uploaded and added to the library catalog!</span>
            </div>
        <% } else if ("deleted".equals(successParam)) { %>
            <div class="alert alert-success">
                <span>✓</span>
                <span>E-Book record and associated storage files deleted successfully.</span>
            </div>
        <% } %>

        <% String errorParam = request.getParameter("error"); %>
        <% if (errorParam != null) { %>
            <div class="alert alert-error">
                <span>⚠</span>
                <span>
                    <% if ("missing_fields".equals(errorParam)) { %>
                        Please fill in all required book metadata fields.
                    <% } else if ("invalid_price".equals(errorParam)) { %>
                        Invalid price format entered. Please enter a valid decimal number.
                    <% } else if ("missing_files".equals(errorParam)) { %>
                        Both Cover Image and E-Book PDF/EPUB files are required for upload.
                    <% } else if ("invalid_cover_format".equals(errorParam)) { %>
                        Invalid cover format. Allowed: .jpg, .jpeg, .png, .webp
                    <% } else if ("invalid_book_format".equals(errorParam)) { %>
                        Invalid e-book format. Allowed: .pdf, .epub
                    <% } else if ("delete_failed".equals(errorParam)) { %>
                        Failed to delete the selected book record.
                    <% } else { %>
                        An error occurred while processing your request. Error code: <%= errorParam %>
                    <% } %>
                </span>
            </div>
        <% } %>

        <!-- Statistics Metrics Row -->
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">📚</div>
                <div class="stat-info">
                    <h4>Total Books</h4>
                    <div class="stat-number"><%= totalBooks %></div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🏷️</div>
                <div class="stat-info">
                    <h4>Active Categories</h4>
                    <div class="stat-number"><%= categories.size() %></div>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">💾</div>
                <div class="stat-info">
                    <h4>Storage Engine</h4>
                    <div class="stat-number" style="font-size: 1.35rem; color: #34d399;">Local Disk / Active</div>
                </div>
            </div>
        </section>

        <!-- Add Book Collapsible Form Card -->
        <section id="addBookSection" class="dashboard-section" style="display: none;">
            <div class="section-header">
                <h2>Add New E-Book Publication</h2>
                <span style="font-size: 0.85rem; color: #9ca3af;">All fields marked * are required</span>
            </div>

            <form action="${pageContext.request.contextPath}/admin/add-book" 
                  method="POST" 
                  enctype="multipart/form-data" 
                  onsubmit="return validateBookForm(this);">
                
                <div class="form-row-2">
                    <div class="form-group-admin">
                        <label for="title">Book Title *</label>
                        <input type="text" id="title" name="title" class="form-input-admin" placeholder="e.g. Clean Architecture & Patterns" required>
                    </div>
                    <div class="form-group-admin">
                        <label for="author">Author *</label>
                        <input type="text" id="author" name="author" class="form-input-admin" placeholder="e.g. Robert C. Martin" required>
                    </div>
                </div>

                <div class="form-row-3">
                    <div class="form-group-admin">
                        <label for="category">Category *</label>
                        <input type="text" id="category" name="category" class="form-input-admin" placeholder="e.g. Technology, Business, Science" required>
                    </div>
                    <div class="form-group-admin">
                        <label for="mood">Mood *</label>
                        <select id="mood" name="mood" class="form-select-admin" required>
                            <option value="Focus">Focus (Productivity & Mastery)</option>
                            <option value="Relaxed">Relaxed (Mindfulness & Leisure)</option>
                            <option value="Inspired">Inspired (Creativity & Breakthrough)</option>
                            <option value="Uplifting">Uplifting (Optimism & Energy)</option>
                            <option value="Calm">Calm (Deep Reading & Reflection)</option>
                        </select>
                    </div>
                    <div class="form-group-admin">
                        <label for="purpose">Purpose *</label>
                        <select id="purpose" name="purpose" class="form-select-admin" required>
                            <option value="Career">Career Growth</option>
                            <option value="Study">In-Depth Study</option>
                            <option value="Exam Prep">Exam Preparation</option>
                            <option value="Leisure">Leisure Reading</option>
                            <option value="Skill Building">Skill Building</option>
                        </select>
                    </div>
                </div>

                <div class="form-row-2">
                    <div class="form-group-admin">
                        <label for="price">Price ($ USD) *</label>
                        <input type="number" id="price" name="price" step="0.01" min="0" class="form-input-admin" placeholder="0.00 for free" required>
                    </div>
                    <div class="form-group-admin">
                        <label>Cover Image (.jpg, .png, .webp) *</label>
                        <div class="file-upload-box">
                            <input type="file" id="coverImage" name="coverImage" accept="image/jpeg,image/png,image/webp" required>
                        </div>
                    </div>
                </div>

                <div class="form-group-admin">
                    <label>E-Book PDF/EPUB File (.pdf, .epub) *</label>
                    <div class="file-upload-box">
                        <input type="file" id="pdfFile" name="pdfFile" accept=".pdf,.epub,application/pdf" required>
                    </div>
                </div>

                <div class="form-group-admin">
                    <label for="summary">AI Summary / Book Description *</label>
                    <textarea id="summary" name="summary" class="form-textarea-admin" placeholder="Provide a rich summary highlighting key concepts and takeaways..." required></textarea>
                </div>

                <div style="display: flex; gap: 1rem; margin-top: 1.5rem;">
                    <button type="submit" class="btn btn-primary" style="padding: 0.8rem 1.8rem;">
                        Publish E-Book to Catalog &rarr;
                    </button>
                    <button type="button" class="btn btn-secondary" onclick="toggleAddBookForm()">
                        Cancel
                    </button>
                </div>

            </form>
        </section>

        <!-- Current Books Inventory Table -->
        <section class="dashboard-section">
            <div class="section-header">
                <h2>Published E-Book Inventory (<%= totalBooks %>)</h2>
                <span style="font-size: 0.85rem; color: #9ca3af;">Live catalog database</span>
            </div>

            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Cover</th>
                            <th>Title & Author</th>
                            <th>Category</th>
                            <th>Mood</th>
                            <th>Purpose</th>
                            <th>Price</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (booksList != null && !booksList.isEmpty()) { %>
                            <% for (Book book : booksList) { 
                                String moodClass = "badge-focus";
                                if ("Relaxed".equalsIgnoreCase(book.getMood())) moodClass = "badge-relaxed";
                                else if ("Inspired".equalsIgnoreCase(book.getMood())) moodClass = "badge-inspired";
                                else if ("Uplifting".equalsIgnoreCase(book.getMood())) moodClass = "badge-uplifting";
                                else if ("Calm".equalsIgnoreCase(book.getMood())) moodClass = "badge-calm";
                            %>
                                <tr>
                                    <td>
                                        <img src="${pageContext.request.contextPath}/<%= book.getCoverPath() %>" 
                                             alt="<%= book.getTitle() %>" 
                                             class="book-thumbnail"
                                             onerror="this.src='https://via.placeholder.com/48x68/1e293b/94a3b8?text=Cover';">
                                    </td>
                                    <td>
                                        <div class="book-title-cell"><%= book.getTitle() %></div>
                                        <div class="book-author-cell">by <%= book.getAuthor() %></div>
                                    </td>
                                    <td><%= book.getCategory() %></td>
                                    <td>
                                        <span class="badge-mood <%= moodClass %>"><%= book.getMood() %></span>
                                    </td>
                                    <td>
                                        <span class="badge-purpose"><%= book.getPurpose() %></span>
                                    </td>
                                    <td style="font-weight: 600; color: #34d399;">
                                        <%= (book.getPrice() != null && book.getPrice().compareTo(BigDecimal.ZERO) > 0) ? "$" + book.getPrice() : "Free" %>
                                    </td>
                                    <td>
                                        <div style="display: flex; gap: 0.5rem;">
                                            <a href="${pageContext.request.contextPath}/<%= book.getPdfPath() %>" target="_blank" class="btn-preview" title="Preview PDF">
                                                📄 View
                                            </a>
                                            <a href="${pageContext.request.contextPath}/admin/book-action?action=delete&bookId=<%= book.getId() %>" 
                                               class="btn-delete"
                                               onclick="return confirm('Are you sure you want to permanently delete \'<%= book.getTitle().replace("'", "\\'") %>\' and its associated files?');">
                                                🗑️ Delete
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        <% } else { %>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 2.5rem; color: #9ca3af;">
                                    No e-books published yet. Click <strong>"Add New E-Book"</strong> above to publish the first book!
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>&copy; <%= java.time.Year.now().getValue() %> Smart E-Book Management System • Admin Panel</p>
    </footer>

    <!-- Interactive Scripts -->
    <script>
        function toggleAddBookForm() {
            const section = document.getElementById('addBookSection');
            const icon = document.getElementById('toggleIcon');
            const text = document.getElementById('toggleText');
            if (section.style.display === 'none' || section.style.display === '') {
                section.style.display = 'block';
                icon.textContent = '✖';
                text.textContent = 'Close Form';
                section.scrollIntoView({ behavior: 'smooth' });
            } else {
                section.style.display = 'none';
                icon.textContent = '➕';
                text.textContent = 'Add New E-Book';
            }
        }

        function validateBookForm(form) {
            const coverInput = form.querySelector('#coverImage');
            const pdfInput = form.querySelector('#pdfFile');

            if (coverInput.files.length > 0) {
                const coverName = coverInput.files[0].name.toLowerCase();
                if (!coverName.match(/\.(jpg|jpeg|png|webp)$/)) {
                    alert('Invalid cover image format. Please select a .jpg, .jpeg, .png, or .webp file.');
                    return false;
                }
            }

            if (pdfInput.files.length > 0) {
                const pdfName = pdfInput.files[0].name.toLowerCase();
                if (!pdfName.match(/\.(pdf|epub)$/)) {
                    alert('Invalid e-book file format. Please select a .pdf or .epub file.');
                    return false;
                }
            }

            return true;
        }
    </script>

</body>
</html>
