package com.ebook.util;

import com.ebook.model.Book;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AISummaryService {

    /**
     * Generates an executive structured AI summary for a given book.
     * Combines stored database summary with psychological mood framing and career/study goals.
     * 
     * @param book Book model entity
     * @return Formatted multi-section AI synopsis
     */
    public static String generateOrEnhanceSummary(Book book) {
        if (book == null) {
            return "No publication data available for AI summary generation.";
        }

        String rawSummary = (book.getSummary() != null && !book.getSummary().trim().isEmpty())
                ? book.getSummary().trim()
                : "A comprehensive publication covering " + book.getCategory() + " principles and methodologies.";

        String mood = (book.getMood() != null && !book.getMood().trim().isEmpty()) ? book.getMood().trim() : "Focus";
        String purpose = (book.getPurpose() != null && !book.getPurpose().trim().isEmpty()) ? book.getPurpose().trim() : "Career";

        StringBuilder sb = new StringBuilder();
        
        // 1. Overview Section
        sb.append("📌 OVERVIEW & CORE THEME\n");
        sb.append(rawSummary).append("\n\n");

        // 2. Key Takeaways Section
        sb.append("💡 KEY TAKEAWAYS & ACTIONABLE INSIGHTS\n");
        sb.append("• Strategic foundations: Master high-level concepts and modern industry best practices.\n");
        sb.append("• Mindset calibration: Aligned with your ").append(mood).append(" emotional state to maximize reading retention.\n");
        sb.append("• Practical application: Concrete frameworks tailored specifically for ").append(purpose).append(" success.\n\n");

        // 3. Target Profile Section
        sb.append("🎯 IDEAL READER PROFILE\n");
        sb.append("• Mindset: Readers seeking a ").append(mood.toLowerCase()).append(" reading experience without cognitive overload.\n");
        sb.append("• Goal: Tailored for professionals and students focusing on ").append(purpose.toLowerCase()).append(" objectives.");

        return sb.toString();
    }

    /**
     * Extensible HTTP Connection Hook for optional external LLM APIs (e.g. OpenAI, Gemini, HuggingFace).
     * Uses native Java SE java.net.HttpURLConnection without external libraries.
     * 
     * @param endpointUrl Target API endpoint
     * @param apiKey Authorization token
     * @param jsonPayload JSON formatted request body
     * @return Raw response string from API
     */
    public static String queryExternalLLM(String endpointUrl, String apiKey, String jsonPayload) {
        HttpURLConnection conn = null;
        try {
            java.net.URI uri = java.net.URI.create(endpointUrl);
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(12000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                System.err.println("LLM API returned HTTP code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.err.println("External LLM query fallback triggered: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
