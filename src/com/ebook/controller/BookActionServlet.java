package com.ebook.controller;

import com.ebook.dao.BookDAO;
import com.ebook.model.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@WebServlet("/admin/book-action")
public class BookActionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookDAO bookDAO;

    @Override
    public void init() throws ServletException {
        this.bookDAO = new BookDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processAction(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processAction(request, response);
    }

    private void processAction(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String action = request.getParameter("action");
        String bookIdStr = request.getParameter("bookId");

        if (action == null || bookIdStr == null || bookIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=invalid_action");
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(bookIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=invalid_book_id");
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            // Find existing book to clean up physical storage files
            Book existingBook = bookDAO.getBookById(bookId);
            if (existingBook != null) {
                String appPath = request.getServletContext().getRealPath("");

                if (existingBook.getCoverPath() != null && !existingBook.getCoverPath().isEmpty()) {
                    File coverFile = new File(appPath + File.separator + existingBook.getCoverPath().replace("/", File.separator));
                    if (coverFile.exists()) {
                        coverFile.delete();
                    }
                }

                if (existingBook.getPdfPath() != null && !existingBook.getPdfPath().isEmpty()) {
                    File pdfFile = new File(appPath + File.separator + existingBook.getPdfPath().replace("/", File.separator));
                    if (pdfFile.exists()) {
                        pdfFile.delete();
                    }
                }

                boolean isDeleted = bookDAO.deleteBook(bookId);
                if (isDeleted) {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard?success=deleted");
                    return;
                }
            }
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=delete_failed");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        }
    }
}
