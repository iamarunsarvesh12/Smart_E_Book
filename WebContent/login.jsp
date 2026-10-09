<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In - Smart E-Book Management System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body>

    <!-- Header Navbar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/index.jsp" class="brand">
            <div class="brand-icon">📚</div>
            <span>Smart E-Book</span>
        </a>
        <nav class="nav-links">
            <a href="${pageContext.request.contextPath}/index.jsp" class="nav-link">Home</a>
            <a href="${pageContext.request.contextPath}/catalog" class="nav-link">Catalog</a>
            <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-secondary">Create Account</a>
        </nav>
    </header>

    <!-- Main Auth Container -->
    <div class="auth-wrapper">
        <div class="auth-card">
            
            <div class="auth-header">
                <div class="auth-icon">🔐</div>
                <h2>Welcome Back</h2>
                <p>Sign in to access your curated library & bookmarks</p>
            </div>

            <!-- Context Alerts -->
            <% if ("true".equals(request.getParameter("registered"))) { %>
                <div class="alert alert-success">
                    <span>✓</span>
                    <span>Account registered successfully! Please sign in below.</span>
                </div>
            <% } %>

            <% if ("true".equals(request.getParameter("logout"))) { %>
                <div class="alert alert-info">
                    <span>ℹ</span>
                    <span>You have been safely signed out.</span>
                </div>
            <% } %>

            <% if ("unauthorized".equals(request.getParameter("error"))) { %>
                <div class="alert alert-error">
                    <span>⚠</span>
                    <span>Authentication required. Please sign in as an admin.</span>
                </div>
            <% } %>

            <% if ("forbidden".equals(request.getParameter("error"))) { %>
                <div class="alert alert-error">
                    <span>⛔</span>
                    <span>Access Denied: Administrator privileges required.</span>
                </div>
            <% } %>

            <% 
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null && !errorMessage.isEmpty()) { 
            %>
                <div class="alert alert-error">
                    <span>⚠</span>
                    <span><%= errorMessage %></span>
                </div>
            <% } %>

            <!-- Login Form -->
            <form id="loginForm" action="${pageContext.request.contextPath}/login" method="POST" novalidate>
                
                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" 
                           id="email" 
                           name="email" 
                           class="form-control" 
                           placeholder="e.g. reader@ebook.com"
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>" 
                           required 
                           autocomplete="email">
                    <div class="error-hint"></div>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" 
                           id="password" 
                           name="password" 
                           class="form-control" 
                           placeholder="Enter your password" 
                           required 
                           autocomplete="current-password">
                    <div class="error-hint"></div>
                </div>

                <button type="submit" class="auth-submit-btn">
                    Sign In &rarr;
                </button>
            </form>

            <div class="auth-footer">
                Don't have an account yet? 
                <a href="${pageContext.request.contextPath}/register.jsp">Create one now</a>
            </div>

            <!-- Demo Credentials Helper -->
            <div class="demo-accounts">
                <strong>Seed Test Credentials:</strong><br>
                Admin: <code>admin@ebook.com</code> / <code>admin123</code><br>
                Reader: <code>reader@ebook.com</code> / <code>reader123</code>
            </div>

        </div>
    </div>

    <!-- Client-side Validation Script -->
    <script src="${pageContext.request.contextPath}/js/auth-validation.js"></script>

</body>
</html>
