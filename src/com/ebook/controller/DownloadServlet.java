package com.ebook.controller;

import com.ebook.dao.BookDAO;
import com.ebook.dao.DownloadDAO;
import com.ebook.dao.PurchaseDAO;
import com.ebook.model.Book;
import com.ebook.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookDAO bookDAO;
    private PurchaseDAO purchaseDAO;
    private DownloadDAO downloadDAO;

    @Override
    public void init() throws ServletException {
        this.bookDAO = new BookDAO();
        this.purchaseDAO = new PurchaseDAO();
        this.downloadDAO = new DownloadDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=unauthorized");
            return;
        }

        String bookIdStr = request.getParameter("bookId");
        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing book ID parameter.");
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(bookIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format.");
            return;
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found.");
            return;
        }

        // Authorization check: Free book, Admin user, or Purchased user
        boolean isFree = (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) == 0);
        boolean isAdmin = currentUser.isAdmin();
        boolean hasPurchased = purchaseDAO.hasUserPurchased(currentUser.getId(), bookId);

        if (!isFree && !isAdmin && !hasPurchased) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You must purchase this book before downloading.");
            return;
        }

        // Resolve Physical File on Server Disk
        String appPath = request.getServletContext().getRealPath("");
        String relativePdfPath = book.getPdfPath().replace("/", File.separator);
        File pdfFile = new File(appPath + File.separator + relativePdfPath);

        if (!pdfFile.exists() || !pdfFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Physical publication file is missing from server storage.");
            return;
        }

        // Audit Download Event
        downloadDAO.logDownload(currentUser.getId(), bookId);

        // Sanitize Filename for HTTP Headers
        String sanitizedFilename = book.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";

        // Set Secure Streaming Response Headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + sanitizedFilename + "\"");
        response.setContentLengthLong(pdfFile.length());
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        // Stream Bytes to Client
        try (FileInputStream in = new FileInputStream(pdfFile);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
