<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Smart E-Book Management System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Top Navigation Bar -->
    <header class="navbar">
        <div class="brand">
            <div class="brand-icon">📚</div>
            <span>Smart E-Book</span>
        </div>
        <nav class="nav-links">
            <a href="${pageContext.request.contextPath}/index.jsp" class="nav-link">Home</a>
            <a href="${pageContext.request.contextPath}/catalog" class="nav-link">Explore Catalog</a>
            <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-secondary">Sign In</a>
            <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-primary">Get Started</a>
        </nav>
    </header>

    <!-- Main Container -->
    <main class="container">
        
        <!-- Hero Section -->
        <section class="hero-section">
            <div class="badge-tag">
                <span>✨ Phase 1 Online • Core MVC Architecture</span>
            </div>
            <h1 class="hero-title">
                Smart Reading Engineered for <span class="gradient-text">Mood & Purpose</span>
            </h1>
            <p class="hero-subtitle">
                Discover, stream, and bookmark specialized books matched to your emotional mindset and professional goals. Built with lightweight native Java Servlets, plain JDBC, and MySQL.
            </p>
            <div class="hero-cta">
                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">
                    Browse E-Book Catalog &rarr;
                </a>
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-secondary">
                    Member Login
                </a>
            </div>
        </section>

        <!-- System Architecture Highlights -->
        <section class="features-grid">
            <div class="card">
                <div class="card-icon">🎯</div>
                <h3 class="card-title">Mood & Purpose Engine</h3>
                <p class="card-text">
                    Filter curated technical, philosophical, and leadership literature based on mindset (Focus, Inspired, Relaxed) and goal (Career, Exam Prep).
                </p>
            </div>

            <div class="card">
                <div class="card-icon">⚡</div>
                <h3 class="card-title">Zero-Overhead MVC</h3>
                <p class="card-text">
                    Pure Java EE Servlets, JSTL/JSP views, and direct JDBC prepared statements delivering maximum performance and thread-safe execution.
                </p>
            </div>

            <div class="card">
                <div class="card-icon">📖</div>
                <h3 class="card-title">Interactive Reader</h3>
                <p class="card-text">
                    Read directly in your browser with automatic page-level bookmark synchronization and instant secure download capabilities.
                </p>
            </div>
        </section>

        <!-- System Status Banner -->
        <div class="system-status">
            <div class="status-indicator">
                <div class="pulse-dot"></div>
                <span>Database & JDBC Connection Engine Ready (`smart_ebook_db`)</span>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/catalog" class="nav-link" style="color: #818cf8; text-decoration: underline;">
                    View Sample Library (4 Books Seeded)
                </a>
            </div>
        </div>

    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>&copy; <%= java.time.Year.now().getValue() %> Smart E-Book Management System. Built with Core Java & MySQL.</p>
    </footer>

</body>
</html>
