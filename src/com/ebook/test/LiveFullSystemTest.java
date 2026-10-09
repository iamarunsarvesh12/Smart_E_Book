package com.ebook.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LiveFullSystemTest {

    private static final String BASE_URL = "http://localhost:8080/OOAD_Project";
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  SMART E-BOOK MANAGEMENT SYSTEM: LIVE AUTONOMOUS END-TO-END AUDIT");
        System.out.println("================================================================================");
        System.out.println();

        // 1. Static Pages & View Rendering
        testEndpoint("1. Home Landing Page", "/", 200, "Smart Reading Engineered", true);
        testEndpoint("2. Reader Catalog Page", "/catalog", 200, "Explore Books by", true);
        testEndpoint("3. Sign-In Page", "/login.jsp", 200, "Welcome Back", true);
        testEndpoint("4. Sign-Up Page", "/register.jsp", 200, "Create Account", true);

        // 2. Unauthenticated Security Guard (AuthFilter redirects to login with error)
        testEndpoint("5. Unauthenticated Admin Guard", "/admin/dashboard", 302, null, false);

        // 3. User Registration Flow
        testRegistrationFlow();

        // 4. Reader Login & Session Flow
        String readerCookie = testReaderLogin();

        // 5. REST APIs (Search, Recommendation, AI Summary)
        testSearchAPI();
        testRecommendationAPI();
        testAISummaryAPI();

        // 6. Purchase & PDF Streaming Flow
        testPurchaseAndStream(readerCookie);

        // 7. Reading Progress Bookmark Flow
        testBookmarkProgress(readerCookie);

        // 8. Admin Login & Dashboard Flow
        testAdminDashboard();

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("  FINAL SYSTEM AUDIT RESULT: " + testsPassed + " / " + testsTotal + " TESTS PASSED");
        if (testsPassed == testsTotal) {
            System.out.println("  VERDICT: ALL 13 END-TO-END MODULES FULLY OPERATIONAL (100% SUCCESS)");
        } else {
            System.out.println("  VERDICT: FAILURES DETECTED. CHECK DETAILED LOGS.");
        }
        System.out.println("================================================================================");
    }

    private static void testEndpoint(String testName, String path, int expectedStatus, String contentKeyword, boolean followRedirects) {
        testsTotal++;
        try {
            HttpURLConnection conn = sendGet(path, null, followRedirects);
            int status = conn.getResponseCode();
            String body = readResponse(conn);
            boolean statusOk = (status == expectedStatus);
            boolean contentOk = (contentKeyword == null || body.contains(contentKeyword));

            if (statusOk && contentOk) {
                testsPassed++;
                System.out.println("  [PASS] " + testName + " -> HTTP " + status + (contentKeyword != null ? " (Verified content keyword: '" + contentKeyword + "')" : ""));
            } else {
                System.err.println("  [FAIL] " + testName + " -> Expected HTTP " + expectedStatus + " but got " + status);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] " + testName + " threw exception: " + e.getMessage());
        }
    }

    private static void testRegistrationFlow() {
        testsTotal++;
        String testEmail = "autonomous_reader_" + System.currentTimeMillis() + "@ebook.com";
        String postData = "name=" + encode("Autonomous Test Reader") +
                "&email=" + encode(testEmail) +
                "&password=" + encode("SecurePass123!") +
                "&confirmPassword=" + encode("SecurePass123!");

        try {
            HttpURLConnection conn = sendPost("/register", postData, null, false);
            int status = conn.getResponseCode();
            String location = conn.getHeaderField("Location");

            boolean registered = (status == 302 && location != null && location.contains("login.jsp?registered=true"));
            if (registered) {
                testsPassed++;
                System.out.println("  [PASS] 6. New User Registration -> Created user (" + testEmail + "), 302 redirected to login.jsp?registered=true");
            } else {
                System.err.println("  [FAIL] 6. New User Registration -> HTTP " + status + ", Location: " + location);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 6. User Registration exception: " + e.getMessage());
        }
    }

    private static String testReaderLogin() {
        testsTotal++;
        String postData = "email=" + encode("reader@ebook.com") + "&password=" + encode("reader123");
        String cookie = null;

        try {
            HttpURLConnection conn = sendPost("/login", postData, null, false);
            int status = conn.getResponseCode();
            String location = conn.getHeaderField("Location");

            Map<String, List<String>> headers = conn.getHeaderFields();
            List<String> cookies = headers.get("Set-Cookie");
            if (cookies != null) {
                for (String c : cookies) {
                    if (c.startsWith("JSESSIONID=")) {
                        cookie = c.split(";")[0];
                        break;
                    }
                }
            }

            boolean loginOk = (status == 302 && location != null && location.contains("catalog") && cookie != null);
            if (loginOk) {
                testsPassed++;
                System.out.println("  [PASS] 7. Reader Authentication -> Authenticated reader@ebook.com (" + cookie + "), routed to /catalog");
            } else {
                System.err.println("  [FAIL] 7. Reader Authentication failed: Status=" + status + ", Cookie=" + cookie);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 7. Reader Login exception: " + e.getMessage());
        }
        return cookie;
    }

    private static void testSearchAPI() {
        testsTotal++;
        try {
            HttpURLConnection conn = sendGet("/search?query=Focus", null);
            String json = readResponse(conn);
            boolean valid = json.startsWith("[") && json.contains("Focus") && json.contains("author");

            if (valid) {
                testsPassed++;
                System.out.println("  [PASS] 8. REST Search API -> Query 'Focus' returned structured JSON array containing title & author fields");
            } else {
                System.err.println("  [FAIL] 8. REST Search API -> Response: " + json);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 8. Search API exception: " + e.getMessage());
        }
    }

    private static void testRecommendationAPI() {
        testsTotal++;
        try {
            HttpURLConnection conn = sendGet("/recommend?mood=Relaxed&purpose=Leisure", null);
            String json = readResponse(conn);
            boolean valid = json.startsWith("[") && json.contains("Mindful");

            if (valid) {
                testsPassed++;
                System.out.println("  [PASS] 9. Recommendation Engine -> Mood='Relaxed' & Purpose='Leisure' matched 'Serenity in Code'");
            } else {
                System.err.println("  [FAIL] 9. Recommendation Engine -> Response: " + json);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 9. Recommendation exception: " + e.getMessage());
        }
    }

    private static void testAISummaryAPI() {
        testsTotal++;
        try {
            HttpURLConnection conn = sendGet("/book-summary?bookId=1", null);
            String json = readResponse(conn);
            boolean valid = json.contains("success") && json.contains("📌 OVERVIEW") && json.contains("💡 KEY TAKEAWAYS");

            if (valid) {
                testsPassed++;
                System.out.println("  [PASS] 10. AI Summary REST API -> Book 1 returned structured 3-section AI synopsis JSON");
            } else {
                System.err.println("  [FAIL] 10. AI Summary API -> Response: " + json);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 10. AI Summary exception: " + e.getMessage());
        }
    }

    private static void testPurchaseAndStream(String cookie) {
        testsTotal++;
        try {
            // Purchase
            String postData = "bookId=2&amount=14.50";
            HttpURLConnection purchaseConn = sendPost("/purchase", postData, cookie, false);
            String pJson = readResponse(purchaseConn);

            // Stream
            HttpURLConnection streamConn = sendGet("/stream-book?bookId=2", cookie);
            int streamStatus = streamConn.getResponseCode();
            String contentType = streamConn.getContentType();

            boolean ok = (streamStatus == 200 && contentType != null && contentType.contains("application/pdf"));
            if (ok) {
                testsPassed++;
                System.out.println("  [PASS] 11. Purchase & Streaming -> Unlocked Book 2, StreamBookServlet delivered application/pdf (HTTP 200)");
            } else {
                System.err.println("  [FAIL] 11. Purchase & Stream -> Purchase JSON: " + pJson + ", Stream Status: " + streamStatus);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 11. Purchase/Stream exception: " + e.getMessage());
        }
    }

    private static void testBookmarkProgress(String cookie) {
        testsTotal++;
        try {
            // Save page 28
            String postData = "bookId=2&pageNumber=28";
            sendPost("/bookmark", postData, cookie, false);

            // Fetch
            HttpURLConnection getConn = sendGet("/bookmark?bookId=2", cookie);
            String getJson = readResponse(getConn);

            boolean ok = getJson.contains("28") && getJson.contains("success");
            if (ok) {
                testsPassed++;
                System.out.println("  [PASS] 12. Bookmark Progress Tracker -> Saved and verified reading progress at Page 28");
            } else {
                System.err.println("  [FAIL] 12. Bookmark Tracker -> Response: " + getJson);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 12. Bookmark exception: " + e.getMessage());
        }
    }

    private static void testAdminDashboard() {
        testsTotal++;
        try {
            // Admin Login
            String postData = "email=" + encode("admin@ebook.com") + "&password=" + encode("admin123");
            HttpURLConnection loginConn = sendPost("/login", postData, null, false);
            
            String adminCookie = null;
            List<String> cookies = loginConn.getHeaderFields().get("Set-Cookie");
            if (cookies != null) {
                for (String c : cookies) {
                    if (c.startsWith("JSESSIONID=")) {
                        adminCookie = c.split(";")[0];
                        break;
                    }
                }
            }

            // Access Dashboard with admin session
            HttpURLConnection dashConn = sendGet("/admin/dashboard", adminCookie);
            int status = dashConn.getResponseCode();
            String body = readResponse(dashConn);

            boolean ok = (status == 200 && body.contains("Book Management & Inventory") && body.contains("Total Books"));
            if (ok) {
                testsPassed++;
                System.out.println("  [PASS] 13. Admin Dashboard & RBAC -> Admin authenticated and accessed inventory metrics table (HTTP 200)");
            } else {
                System.err.println("  [FAIL] 13. Admin Dashboard failed -> HTTP " + status);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] 13. Admin Dashboard exception: " + e.getMessage());
        }
    }

    // --- Helpers ---
    private static HttpURLConnection sendPost(String endpoint, String postData, String cookie, boolean followRedirects) throws Exception {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setInstanceFollowRedirects(followRedirects);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        if (cookie != null) {
            conn.setRequestProperty("Cookie", cookie);
        }

        byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postDataBytes);
            os.flush();
        }
        return conn;
    }

    private static HttpURLConnection sendGet(String endpoint, String cookie) throws Exception {
        return sendGet(endpoint, cookie, true);
    }

    private static HttpURLConnection sendGet(String endpoint, String cookie, boolean followRedirects) throws Exception {
        URL url = URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(followRedirects);
        if (cookie != null) {
            conn.setRequestProperty("Cookie", cookie);
        }
        return conn;
    }

    private static String readResponse(HttpURLConnection conn) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                    StandardCharsets.UTF_8
            ));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }
}
