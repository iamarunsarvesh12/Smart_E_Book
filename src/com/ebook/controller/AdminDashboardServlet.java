package com.ebook.controller;

import com.ebook.dao.BookDAO;
import com.ebook.model.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookDAO bookDAO;

    @Override
    public void init() throws ServletException {
        this.bookDAO = new BookDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Book> booksList = bookDAO.getAllBooks();
        request.setAttribute("booksList", booksList);

        // Compute Quick Summary Metrics
        int totalBooks = booksList.size();
        Set<String> categories = new HashSet<>();
        for (Book b : booksList) {
            if (b.getCategory() != null && !b.getCategory().trim().isEmpty()) {
                categories.add(b.getCategory().trim());
            }
        }
        
        request.setAttribute("totalBooks", totalBooks);
        request.setAttribute("totalCategories", categories.size());

        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
