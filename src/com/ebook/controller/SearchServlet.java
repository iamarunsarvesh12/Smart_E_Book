package com.ebook.controller;

import com.ebook.dao.BookDAO;
import com.ebook.model.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookDAO bookDAO;

    @Override
    public void init() throws ServletException {
        this.bookDAO = new BookDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String query = request.getParameter("query");
        if (query == null) {
            query = request.getParameter("q");
        }

        List<Book> results = bookDAO.searchBooks(query);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print(buildJsonArray(results));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Serializes a List of Book entities into a valid JSON array string.
     */
    private String buildJsonArray(List<Book> books) {
        StringBuilder json = new StringBuilder("[");
        if (books != null && !books.isEmpty()) {
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                json.append("{")
                    .append("\"id\":").append(book.getId()).append(",")
                    .append("\"title\":\"").append(escapeJson(book.getTitle())).append("\",")
                    .append("\"author\":\"").append(escapeJson(book.getAuthor())).append("\",")
                    .append("\"category\":\"").append(escapeJson(book.getCategory())).append("\",")
                    .append("\"mood\":\"").append(escapeJson(book.getMood())).append("\",")
                    .append("\"purpose\":\"").append(escapeJson(book.getPurpose())).append("\",")
                    .append("\"summary\":\"").append(escapeJson(book.getSummary())).append("\",")
                    .append("\"price\":").append(book.getPrice() != null ? book.getPrice() : 0.00).append(",")
                    .append("\"pdfPath\":\"").append(escapeJson(book.getPdfPath())).append("\",")
                    .append("\"coverPath\":\"").append(escapeJson(book.getCoverPath())).append("\"")
                    .append("}");

                if (i < books.size() - 1) {
                    json.append(",");
                }
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Safely escapes string characters for JSON serialization.
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                    break;
            }
        }
        return escaped.toString();
    }
}
