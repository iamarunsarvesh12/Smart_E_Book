package com.ebook.controller;

import com.ebook.dao.BookDAO;
import com.ebook.model.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;

@WebServlet("/admin/add-book")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB memory buffer threshold
    maxFileSize = 1024 * 1024 * 50,       // 50MB max file size
    maxRequestSize = 1024 * 1024 * 60     // 60MB max request size
)
public class AddBookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookDAO bookDAO;

    @Override
    public void init() throws ServletException {
        this.bookDAO = new BookDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String category = request.getParameter("category");
        String mood = request.getParameter("mood");
        String purpose = request.getParameter("purpose");
        String summary = request.getParameter("summary");
        String priceStr = request.getParameter("price");

        // Basic Text Validation
        if (title == null || title.trim().isEmpty() ||
            author == null || author.trim().isEmpty() ||
            category == null || category.trim().isEmpty() ||
            mood == null || mood.trim().isEmpty() ||
            purpose == null || purpose.trim().isEmpty() ||
            summary == null || summary.trim().isEmpty()) {
            
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=missing_fields");
            return;
        }

        BigDecimal price = BigDecimal.ZERO;
        if (priceStr != null && !priceStr.trim().isEmpty()) {
            try {
                price = new BigDecimal(priceStr.trim());
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    price = BigDecimal.ZERO;
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=invalid_price");
                return;
            }
        }

        // File Parts Extraction
        Part coverPart = request.getPart("coverImage");
        Part pdfPart = request.getPart("pdfFile");

        if (coverPart == null || coverPart.getSize() == 0 || pdfPart == null || pdfPart.getSize() == 0) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=missing_files");
            return;
        }

        String coverOriginalName = Paths.get(coverPart.getSubmittedFileName()).getFileName().toString();
        String pdfOriginalName = Paths.get(pdfPart.getSubmittedFileName()).getFileName().toString();

        // Sanitize and Validate File Extensions
        String coverExt = getFileExtension(coverOriginalName).toLowerCase();
        String pdfExt = getFileExtension(pdfOriginalName).toLowerCase();

        if (!coverExt.matches("\\.(jpg|jpeg|png|webp)")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=invalid_cover_format");
            return;
        }

        if (!pdfExt.matches("\\.(pdf|epub)")) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=invalid_book_format");
            return;
        }

        // Generate Unique Disk Filenames
        String coverFileName = System.currentTimeMillis() + "_cover" + coverExt;
        String pdfFileName = System.currentTimeMillis() + "_book" + pdfExt;

        // Resolve Server Paths & Guarantee Directories Exist
        String appPath = request.getServletContext().getRealPath("");
        String coverUploadDir = appPath + File.separator + "uploads" + File.separator + "covers";
        String pdfUploadDir = appPath + File.separator + "uploads" + File.separator + "books";

        File coverDir = new File(coverUploadDir);
        if (!coverDir.exists()) {
            coverDir.mkdirs();
        }

        File pdfDir = new File(pdfUploadDir);
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        // Save Files to Server Storage
        String coverPhysicalPath = coverUploadDir + File.separator + coverFileName;
        String pdfPhysicalPath = pdfUploadDir + File.separator + pdfFileName;

        coverPart.write(coverPhysicalPath);
        pdfPart.write(pdfPhysicalPath);

        // Store relative Web paths in Database
        String relativeCoverPath = "uploads/covers/" + coverFileName;
        String relativePdfPath = "uploads/books/" + pdfFileName;

        Book book = new Book(
            title.trim(),
            author.trim(),
            category.trim(),
            mood.trim(),
            purpose.trim(),
            summary.trim(),
            price,
            relativePdfPath,
            relativeCoverPath
        );

        boolean isAdded = bookDAO.addBook(book);

        if (isAdded) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?success=added");
        } else {
            // Clean up uploaded files if DB insertion failed
            new File(coverPhysicalPath).delete();
            new File(pdfPhysicalPath).delete();
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=db_error");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastIndex = filename.lastIndexOf('.');
        return (lastIndex != -1) ? filename.substring(lastIndex) : "";
    }
}
