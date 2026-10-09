package com.ebook.controller;

import com.ebook.dao.BookDAO;
import com.ebook.model.Book;
import com.ebook.util.AISummaryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/book-summary")
public class AISummaryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookDAO bookDAO;

    @Override
    public void init() throws ServletException {
        this.bookDAO = new BookDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String bookIdStr = request.getParameter("bookId");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            out.print("{\"status\":\"error\",\"message\":\"Missing bookId parameter\"}");
            out.flush();
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(bookIdStr.trim());
        } catch (NumberFormatException e) {
            out.print("{\"status\":\"error\",\"message\":\"Invalid bookId format\"}");
            out.flush();
            return;
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"status\":\"error\",\"message\":\"Book not found\"}");
            out.flush();
            return;
        }

        String structuredSummary = AISummaryService.generateOrEnhanceSummary(book);

        StringBuilder json = new StringBuilder("{");
        json.append("\"status\":\"success\",")
            .append("\"bookId\":").append(book.getId()).append(",")
            .append("\"title\":\"").append(escapeJson(book.getTitle())).append("\",")
            .append("\"author\":\"").append(escapeJson(book.getAuthor())).append("\",")
            .append("\"category\":\"").append(escapeJson(book.getCategory())).append("\",")
            .append("\"mood\":\"").append(escapeJson(book.getMood())).append("\",")
            .append("\"purpose\":\"").append(escapeJson(book.getPurpose())).append("\",")
            .append("\"summary\":\"").append(escapeJson(structuredSummary)).append("\"")
            .append("}");

        out.print(json.toString());
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
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
