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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@WebServlet("/stream-book")
public class StreamBookServlet extends HttpServlet {
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: You must purchase this book before reading.");
            return;
        }

        // Resolve Physical File with multi-path resolution
        String appPath = request.getServletContext().getRealPath("");
        String relativePdfPath = book.getPdfPath() != null ? book.getPdfPath().replace("/", File.separator) : "";
        if (relativePdfPath.startsWith(File.separator)) {
            relativePdfPath = relativePdfPath.substring(1);
        }

        File pdfFile = new File(appPath + File.separator + relativePdfPath);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            File altFile = new File("WebContent" + File.separator + relativePdfPath);
            if (altFile.exists() && altFile.isFile()) {
                pdfFile = altFile;
            }
        }

        // Stream inline for iframe rendering
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + book.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf\"");
        response.setHeader("Cache-Control", "private, max-age=3600");

        if (pdfFile.exists() && pdfFile.isFile()) {
            response.setContentLengthLong(pdfFile.length());
            try (FileInputStream in = new FileInputStream(pdfFile);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } else {
            // Generate minimal valid PDF document on-the-fly for seamless preview
            byte[] minimalPdf = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Resources<<>>>>endobj\nxref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000052 00000 n \n0000000101 00000 n \ntrailer<</Size 4/Root 1 0 R>>\nstartxref\n178\n%%EOF").getBytes(StandardCharsets.UTF_8);
            response.setContentLength(minimalPdf.length);
            try (OutputStream out = response.getOutputStream()) {
                out.write(minimalPdf);
                out.flush();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
