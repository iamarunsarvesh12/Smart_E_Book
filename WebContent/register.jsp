<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account - Smart E-Book Management System</title>
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
            <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-secondary">Sign In</a>
        </nav>
    </header>

    <!-- Main Auth Container -->
    <div class="auth-wrapper">
        <div class="auth-card">
            
            <div class="auth-header">
                <div class="auth-icon">🚀</div>
                <h2>Create Account</h2>
                <p>Join our personalized, mood-driven reading community</p>
            </div>

            <!-- Server Error Alert -->
            <% 
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null && !errorMessage.isEmpty()) { 
            %>
                <div class="alert alert-error">
                    <span>⚠</span>
                    <span><%= errorMessage %></span>
                </div>
            <% } %>

            <!-- Registration Form -->
            <form id="registerForm" action="${pageContext.request.contextPath}/register" method="POST" novalidate>
                
                <div class="form-group">
                    <label for="name">Full Name</label>
                    <input type="text" 
                           id="name" 
                           name="name" 
                           class="form-control" 
                           placeholder="e.g. Jane Doe" 
                           value="<%= request.getAttribute("name") != null ? request.getAttribute("name") : "" %>" 
                           required 
                           autocomplete="name">
                    <div class="error-hint"></div>
                </div>

                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" 
                           id="email" 
                           name="email" 
                           class="form-control" 
                           placeholder="e.g. jane.doe@example.com" 
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>" 
                           required 
                           autocomplete="email">
                    <div class="error-hint"></div>
                </div>

                <div class="form-group">
                    <label for="password">Password (min 6 characters)</label>
                    <input type="password" 
                           id="password" 
                           name="password" 
                           class="form-control" 
                           placeholder="Create a strong password" 
                           required 
                           autocomplete="new-password">
                    <div class="error-hint"></div>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input type="password" 
                           id="confirmPassword" 
                           name="confirmPassword" 
                           class="form-control" 
                           placeholder="Re-type your password" 
                           required 
                           autocomplete="new-password">
                    <div class="error-hint"></div>
                </div>

                <button type="submit" class="auth-submit-btn">
                    Create Account &rarr;
                </button>
            </form>

            <div class="auth-footer">
                Already registered? 
                <a href="${pageContext.request.contextPath}/login.jsp">Sign in to your account</a>
            </div>

        </div>
    </div>

    <!-- Client-side Validation Script -->
    <script src="${pageContext.request.contextPath}/js/auth-validation.js"></script>

</body>
</html>
