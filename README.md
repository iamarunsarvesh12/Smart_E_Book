# 🚀 Smart E-Book Management System — Setup & Execution Guide

Welcome to the **Smart E-Book Management System**, an enterprise-grade MVC web application engineered with pure **Java Servlets, JSP, JDBC, MySQL 8.0 / Embedded MySQL Dialect, HTML5/CSS3, and Vanilla JavaScript** running on **Apache Tomcat**.

The project is designed for **100% autonomous, zero-setup execution with a single command**.

---

## ⚡ 1. Single-Command Quick Start (Run in 1 Second)

Execute **one single command** in your terminal inside the project root folder. Everything is automated:

### 🪟 Windows (Command Prompt / Terminal / Double-Click)
```cmd
run.bat
```

### 🪟 Windows (PowerShell)
```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

### 🍎 macOS / 🐧 Linux (Terminal)
```bash
chmod +x run.sh && ./run.sh
```

---

## 🔄 2. Complete Execution Process & Pipeline Lifecycle

When you run the single command above, the automated runner executes the entire end-to-end lifecycle automatically:

```
+-------------------------------------------------------------------------------+
|                      SINGLE-COMMAND EXECUTION PIPELINE                        |
+-------------------------------------------------------------------------------+
  [Step 1] JDK & Environment Auto-Detection
           * Detects JDK 11 / 17 / 21 / 25 in system PATH or Program Files.
           * Automatically configures JAVA_HOME and JRE_HOME in memory.

  [Step 2] Directory Structuring & Clean Compilation
           * Generates target build directory: WebContent/WEB-INF/classes
           * Provisions upload storage: WebContent/uploads/books & covers
           * Compiles all Java Servlets, DAOs, Models, Filters, and Tests.
           * Synchronizes database/schema.sql to classes classpath.

  [Step 3] Pre-Flight Database & System Diagnostics
           * Verifies MySQL 8.0 connectivity on port 3306.
           * Auto-initializes zero-config embedded persistent database engine
             if local MySQL is not running.
           * Executes 30/30 unit & integration tests across 7 test suites.
           * Validates SQL injection resilience, RBAC filters, and endpoints.

  [Step 4] Apache Tomcat Auto-Provisioning & Deployment
           * Detects system Tomcat or auto-provisions portable Tomcat 9 into .tomcat/
           * Injects setenv.bat with accurate JDK paths to prevent environment errors.
           * Deploys complete WebContent bundle into Tomcat webapps/OOAD_Project.

  [Step 5] Safe Port & Server Initialization
           * Detects and frees conflicting processes on port 8080.
           * Launches Apache Tomcat server in a dedicated background console.

  [Step 6] Live Health Check Polling
           * Continuously polls http://localhost:8080/OOAD_Project/
           * Confirms HTTP 200/302 response when the server is fully ready.

  [Step 7] Automated Browser Launch
           * Automatically launches Google Chrome (or default browser) directly
             to the application home page!
+-------------------------------------------------------------------------------+
```

---

## 📋 3. System Prerequisites

| Component | Minimum Version | Recommended | Notes |
| :--- | :--- | :--- | :--- |
| **Java JDK** | JDK 11 | JDK 17, 21, or 25 | Ensure `javac` and `java` are installed |
| **Database** | *Auto-Handled* | MySQL 8.0 or Embedded | Zero configuration required (works out of the box) |
| **Apache Tomcat** | *Auto-Provisioned* | Tomcat 9.0 (Included) | Automatically detected or downloaded into `.tomcat/` |
| **Web Browser** | Any Modern Browser | Google Chrome (Auto-Launched) | Native Web Speech API voice search support |

---

## 🗄️ 4. Optional: Dedicated MySQL Database Setup

The application works out-of-the-box with zero database configuration. If you prefer using a dedicated local MySQL 8.0 server:

### Step 4.1 — Import Schema & Seed Data
```bash
mysql -u root -p < database/schema.sql
```

### Step 4.2 — Configure Credentials (If Needed)
Default credentials configured in [`src/com/ebook/util/DBConnection.java`](file:///c:/Users/Admin/Documents/OOAD%20Project/src/com/ebook/util/DBConnection.java):
- **Database URL**: `jdbc:mysql://localhost:3306/smart_ebook_db`
- **Username**: `root`
- **Password**: `password` (or set the `EBOOK_DB_PASSWORD` environment variable)

---

## 🔑 5. Default Login Credentials

The system comes pre-seeded with two active accounts (passwords are SHA-256 hashed):

| Role | Email Address | Password | Privileges & Access |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@ebook.com` | `admin123` | Full inventory CRUD, upload PDF/covers, delete books, sales metrics |
| **Reader / User** | `reader@ebook.com` | `reader123` | Catalog browsing, voice search, mood filter, simulated checkout, PDF reader & bookmarks |

*(You can also click **Get Started / Sign Up** on the website to register brand-new accounts instantly).*

---

## 🌐 6. Key Application Endpoints

Once launched, access the system features directly:

| Page / Feature | URL | Description |
| :--- | :--- | :--- |
| **Home Landing Page** | `http://localhost:8080/OOAD_Project/` | Hero section, feature overview, quick launch point |
| **E-Book Catalog** | `http://localhost:8080/OOAD_Project/catalog` | Catalog with voice search, mood/purpose selector, category pills |
| **Admin Dashboard** | `http://localhost:8080/OOAD_Project/admin/dashboard` | Book inventory table, PDF & cover upload forms, delete actions |
| **User Login** | `http://localhost:8080/OOAD_Project/login.jsp` | SHA-256 authenticated session sign-in |
| **User Registration** | `http://localhost:8080/OOAD_Project/register.jsp` | Instant reader registration with live client-side validation |
| **In-Browser Reader** | `http://localhost:8080/OOAD_Project/reader.jsp?bookId=1` | PDF viewer with live bookmark auto-saving & progress tracking |

---

## 🛠️ 7. Diagnostic Testing & Verification Commands

To run the verification test suites independently at any time:

### Full-Spectrum Diagnostics (16 Checks):
```bash
java -cp "WebContent/WEB-INF/lib/*;WebContent/WEB-INF/classes" com.ebook.test.FullSpectrumDiagnosticTest
```

### End-to-End System Verification (30 Tests):
```bash
java -cp "WebContent/WEB-INF/lib/*;WebContent/WEB-INF/classes" com.ebook.test.SystemVerificationTest
```

---

## 🖥️ 8. Alternative IDE Setup (Eclipse / IntelliJ IDEA)

If you prefer launching and debugging through an IDE:

### 🔷 Eclipse IDE:
1. **Import Project**: `File` ➔ `Open Projects from File System...` ➔ Select project root folder.
2. **Configure Facets**: Right-click project ➔ `Properties` ➔ `Project Facets` ➔ Check `Dynamic Web Module (4.0)` and `Java`. Set Web Content directory to `WebContent`.
3. **Run on Server**: Right-click project ➔ `Run As` ➔ `Run on Server` ➔ Select Apache Tomcat.

### 🟨 IntelliJ IDEA:
1. **Open Project**: `File` ➔ `Open` ➔ Select project root folder.
2. **Configure Artifacts**: `File` ➔ `Project Structure` ➔ `Artifacts` ➔ `+` ➔ `Web Application: Exploded` ➔ From Modules (`WebContent`).
3. **Run Configuration**: `Run` ➔ `Edit Configurations` ➔ `+` ➔ `Tomcat Server (Local)` ➔ In Deployment tab, select exploded artifact with context `/OOAD_Project` ➔ Click `Run`.

---

**Happy Reading & Exploring the Smart E-Book Management System!** 📚✨
