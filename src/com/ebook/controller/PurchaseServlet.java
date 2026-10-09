package com.ebook.controller;

import com.ebook.dao.BookDAO;
import com.ebook.dao.PurchaseDAO;
import com.ebook.model.Book;
import com.ebook.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet("/purchase")
public class PurchaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private PurchaseDAO purchaseDAO;
    private BookDAO bookDAO;

    @Override
    public void init() throws ServletException {
        this.purchaseDAO = new PurchaseDAO();
        this.bookDAO = new BookDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"status\":\"unauthorized\",\"message\":\"Please log in to purchase books.\"}");
            out.flush();
            return;
        }

        String bookIdStr = request.getParameter("bookId");
        String amountStr = request.getParameter("amount");

        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            out.print("{\"status\":\"error\",\"message\":\"Missing book ID parameter.\"}");
            out.flush();
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(bookIdStr.trim());
        } catch (NumberFormatException e) {
            out.print("{\"status\":\"error\",\"message\":\"Invalid book ID format.\"}");
            out.flush();
            return;
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            out.print("{\"status\":\"error\",\"message\":\"Book not found in library catalog.\"}");
            out.flush();
            return;
        }

        // Check if user already owns this publication
        if (purchaseDAO.hasUserPurchased(currentUser.getId(), bookId)) {
            out.print("{\"status\":\"already_owned\",\"bookId\":" + bookId + ",\"message\":\"You already own this book.\"}");
            out.flush();
            return;
        }

        double amount = 0.00;
        if (amountStr != null && !amountStr.trim().isEmpty()) {
            try {
                amount = Double.parseDouble(amountStr.trim());
            } catch (NumberFormatException e) {
                amount = (book.getPrice() != null) ? book.getPrice().doubleValue() : 0.00;
            }
        } else if (book.getPrice() != null) {
            amount = book.getPrice().doubleValue();
        }

        boolean isPurchased = purchaseDAO.recordPurchase(currentUser.getId(), bookId, amount);

        if (isPurchased) {
            out.print("{\"status\":\"success\",\"bookId\":" + bookId + ",\"message\":\"Payment completed and book unlocked successfully!\"}");
        } else {
            out.print("{\"status\":\"error\",\"message\":\"Transaction processing failed. Please try again.\"}");
        }
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/catalog");
    }
}
