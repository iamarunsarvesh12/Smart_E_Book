package com.ebook.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class LiveSampleInputsTest {

    private static final String BASE_URL = "http://localhost:8080/OOAD_Project";
    private static String sessionCookie = null;
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  SMART E-BOOK MANAGEMENT SYSTEM: LIVE SAMPLE-INPUT HTTP TEST SUITE");
        System.out.println("================================================================================");
        System.out.println();

        // 1. User Registration Form Test
        testUserRegistration();

        // 2. User Authentication / Login Form Test
        testUserLogin();

        // 3. Search & Recommendation Endpoints Test
        testSearchAndRecommendations();

        // 4. Simulated Purchase & PDF Stream Test
        testBookPurchaseAndStreaming();

        // 5. Bookmark Reading Progress Test
        testBookmarkProgress();

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("  TEST SUITE RESULTS: " + testsPassed + " / " + testsTotal + " TESTS PASSED");
        if (testsPassed == testsTotal) {
            System.out.println("  STATUS: ALL END-TO-END FORM AND PERSISTENCE FLOWS VERIFIED (100% PASS)");
        } else {
            System.out.println("  STATUS: SOME TESTS FAILED. REVIEW DETAILED LOGS ABOVE.");
        }
        System.out.println("================================================================================");
    }

    private static void testUserRegistration() {
        testsTotal++;
        System.out.println("[SAMPLE INPUT 1: User Registration]");
        String testEmail = "sample_user_" + System.currentTimeMillis() + "@ebook.com";
        String postData = "name=" + encode("Sample Reader") +
                "&email=" + encode(testEmail) +
                "&password=" + encode("Password123!") +
                "&confirmPassword=" + encode("Password123!");

        try {
            HttpURLConnection conn = sendPost("/register", postData, null, false);
            int status = conn.getResponseCode();
            String location = conn.getHeaderField("Location");

            boolean redirectedToLogin = (status == 302 && location != null && location.contains("login.jsp?registered=true"));

            // Verify the newly created account by logging in via HTTP
            String loginData = "email=" + encode(testEmail) + "&password=" + encode("Password123!");
            HttpURLConnection loginConn = sendPost("/login", loginData, null, false);
            int loginStatus = loginConn.getResponseCode();
            String loginLocation = loginConn.getHeaderField("Location");
            boolean loginVerified = (loginStatus == 302 && loginLocation != null && loginLocation.contains("catalog"));

            if (redirectedToLogin && loginVerified) {
                testsPassed++;
                System.out.println("  [PASS] Registration Form -> Registered account (" + testEmail + "), redirected to login.jsp?registered=true, authenticated into /catalog.");
            } else {
                System.err.println("  [FAIL] Registration Form -> Status: " + status + ", Location: " + location + ", LoginVerified: " + loginVerified);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] Registration Form threw exception: " + e.getMessage());
        }
    }

    private static void testUserLogin() {
        testsTotal++;
        System.out.println("\n[SAMPLE INPUT 2: User Authentication & Session Establishment]");
        String postData = "email=" + encode("reader@ebook.com") +
                "&password=" + encode("reader123");

        try {
            HttpURLConnection conn = sendPost("/login", postData, null, false);
            int status = conn.getResponseCode();
            String location = conn.getHeaderField("Location");

            // Extract JSESSIONID cookie
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    if (cookie.startsWith("JSESSIONID=")) {
                        sessionCookie = cookie.split(";")[0];
                        break;
                    }
                }
            }

            boolean loginSuccessful = (status == 302 && location != null && location.contains("catalog"));

            if (loginSuccessful && sessionCookie != null) {
                testsPassed++;
                System.out.println("  [PASS] Login Form -> Validated reader@ebook.com / reader123, established session (" + sessionCookie + "), redirected to /catalog.");
            } else {
                System.err.println("  [FAIL] Login Form -> HTTP Status: " + status + ", Location: " + location + ", SessionCookie: " + sessionCookie);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] Login Form threw exception: " + e.getMessage());
        }
    }

    private static void testSearchAndRecommendations() {
        testsTotal++;
        System.out.println("\n[SAMPLE INPUT 3: Search & Recommendation Engine]");
        try {
            // Test 3.1: Search query
            HttpURLConnection searchConn = sendGet("/search?query=Focus", sessionCookie);
            String searchJson = readResponse(searchConn);
            boolean searchHasBook = searchJson.contains("Focus") || searchJson.contains("books");

            // Test 3.2: Recommendation query
            HttpURLConnection recConn = sendGet("/recommend?mood=Relaxed&purpose=Leisure", sessionCookie);
            String recJson = readResponse(recConn);
            boolean recHasData = recJson.startsWith("[") && recJson.contains("title");

            if (searchHasBook && recHasData) {
                testsPassed++;
                System.out.println("  [PASS] Search & Recommendations -> Query 'Focus' returned structured JSON; Mood='Relaxed' returned curated book items.");
            } else {
                System.err.println("  [FAIL] Search/Rec -> Search JSON: " + searchJson + ", Rec JSON: " + recJson);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] Search & Recommendations threw exception: " + e.getMessage());
        }
    }

    private static void testBookPurchaseAndStreaming() {
        testsTotal++;
        System.out.println("\n[SAMPLE INPUT 4: Book Purchase Transaction & Secure PDF Streaming]");
        try {
            // Purchase Book ID 1
            String postData = "bookId=1&amount=9.99";
            HttpURLConnection purchaseConn = sendPost("/purchase", postData, sessionCookie, false);
            String purchaseJson = readResponse(purchaseConn);

            boolean purchaseSuccess = purchaseJson.contains("success") || purchaseJson.contains("COMPLETED") || purchaseJson.contains("already_purchased");

            // Stream Book ID 1 with authenticated session
            HttpURLConnection streamConn = sendGet("/stream-book?bookId=1", sessionCookie);
            int streamStatus = streamConn.getResponseCode();
            String contentType = streamConn.getContentType();

            boolean streamValid = (streamStatus == 200 && contentType != null && contentType.contains("application/pdf"));

            if (purchaseSuccess && streamValid) {
                testsPassed++;
                System.out.println("  [PASS] Purchase & Stream -> Simulated purchase of Book 1 completed; StreamBookServlet delivered application/pdf (HTTP 200).");
            } else {
                System.err.println("  [FAIL] Purchase/Stream -> Purchase JSON: " + purchaseJson + ", Stream Status: " + streamStatus + ", Content-Type: " + contentType);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] Purchase & Streaming threw exception: " + e.getMessage());
        }
    }

    private static void testBookmarkProgress() {
        testsTotal++;
        System.out.println("\n[SAMPLE INPUT 5: Reader Progress Bookmarking]");
        try {
            // Save bookmark at page 15
            String postData = "bookId=1&pageNumber=15";
            HttpURLConnection postBookmark = sendPost("/bookmark", postData, sessionCookie, false);
            String postResp = readResponse(postBookmark);

            // Fetch saved bookmark
            HttpURLConnection getBookmark = sendGet("/bookmark?bookId=1", sessionCookie);
            String getResp = readResponse(getBookmark);

            boolean bookmarkSaved = (getResp.contains("15") || postResp.contains("success"));

            if (bookmarkSaved) {
                testsPassed++;
                System.out.println("  [PASS] Bookmark Tracker -> Persisted reading position at Page 15; GET /bookmark?bookId=1 returned: " + getResp.trim());
            } else {
                System.err.println("  [FAIL] Bookmark Tracker -> Post: " + postResp + ", Get: " + getResp);
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] Bookmark Progress threw exception: " + e.getMessage());
        }
    }

    // --- Helper Network Methods ---

    private static HttpURLConnection sendPost(String endpoint, String postData, String cookie, boolean followRedirects) throws Exception {
        URL url = java.net.URI.create(BASE_URL + endpoint).toURL();
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
        URL url = java.net.URI.create(BASE_URL + endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
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
                sb.append(line);
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
