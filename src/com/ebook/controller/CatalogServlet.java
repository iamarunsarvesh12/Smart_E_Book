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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookDAO bookDAO;
    private PurchaseDAO purchaseDAO;

    @Override
    public void init() throws ServletException {
        this.bookDAO = new BookDAO();
        this.purchaseDAO = new PurchaseDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String searchQuery = request.getParameter("q");
        List<Book> booksList;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            booksList = bookDAO.searchBooks(searchQuery.trim());
        } else {
            booksList = bookDAO.getAllBooks();
        }

        List<String> categories = bookDAO.getAllCategories();
        List<String> allMoods = bookDAO.getAllMoods();
        List<String> allPurposes = bookDAO.getAllPurposes();

        // Check user session for unlocked purchased books
        Set<Integer> purchasedBookIds = new HashSet<>();
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser != null) {
            purchasedBookIds = purchaseDAO.getPurchasedBookIds(currentUser.getId());
        }

        request.setAttribute("booksList", booksList);
        request.setAttribute("categories", categories);
        request.setAttribute("allMoods", allMoods);
        request.setAttribute("allPurposes", allPurposes);
        request.setAttribute("purchasedBookIds", purchasedBookIds);
        request.setAttribute("searchQuery", searchQuery != null ? searchQuery.trim() : "");

        request.getRequestDispatcher("/catalog.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
