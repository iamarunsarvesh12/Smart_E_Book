package com.ebook.controller;

import com.ebook.dao.BookmarkDAO;
import com.ebook.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/bookmark")
public class BookmarkServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookmarkDAO bookmarkDAO;

    @Override
    public void init() throws ServletException {
        this.bookmarkDAO = new BookmarkDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            out.print("{\"status\":\"unauthorized\",\"lastPage\":1}");
            out.flush();
            return;
        }

        String bookIdStr = request.getParameter("bookId");
        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            out.print("{\"status\":\"error\",\"lastPage\":1}");
            out.flush();
            return;
        }

        try {
            int bookId = Integer.parseInt(bookIdStr.trim());
            int lastPage = bookmarkDAO.getLastReadPage(currentUser.getId(), bookId);
            out.print("{\"status\":\"success\",\"bookId\":" + bookId + ",\"lastPage\":" + lastPage + "}");
        } catch (NumberFormatException e) {
            out.print("{\"status\":\"error\",\"lastPage\":1}");
        }
        out.flush();
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
            out.print("{\"status\":\"unauthorized\",\"message\":\"User not logged in.\"}");
            out.flush();
            return;
        }

        String bookIdStr = request.getParameter("bookId");
        String pageStr = request.getParameter("pageNumber");

        if (bookIdStr == null || pageStr == null) {
            out.print("{\"status\":\"error\",\"message\":\"Missing bookId or pageNumber.\"}");
            out.flush();
            return;
        }

        try {
            int bookId = Integer.parseInt(bookIdStr.trim());
            int pageNumber = Integer.parseInt(pageStr.trim());

            boolean isSaved = bookmarkDAO.saveOrUpdateProgress(currentUser.getId(), bookId, pageNumber);
            if (isSaved) {
                out.print("{\"status\":\"saved\",\"page\":" + pageNumber + ",\"message\":\"Bookmark saved!\"}");
            } else {
                out.print("{\"status\":\"error\",\"message\":\"Failed to save bookmark.\"}");
            }
        } catch (NumberFormatException e) {
            out.print("{\"status\":\"error\",\"message\":\"Invalid numerical inputs.\"}");
        }
        out.flush();
    }
}
