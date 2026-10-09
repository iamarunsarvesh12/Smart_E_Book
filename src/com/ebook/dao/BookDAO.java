package com.ebook.dao;

import com.ebook.model.Book;
import com.ebook.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    /**
     * Inserts a new book record into the books table.
     * 
     * @param book Book entity to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean addBook(Book book) {
        if (book == null) {
            return false;
        }

        String sql = "INSERT INTO books (title, author, category, mood, purpose, summary, price, pdf_path, cover_path) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, book.getTitle().trim());
            pstmt.setString(2, book.getAuthor().trim());
            pstmt.setString(3, book.getCategory().trim());
            pstmt.setString(4, book.getMood().trim());
            pstmt.setString(5, book.getPurpose().trim());
            pstmt.setString(6, book.getSummary().trim());
            pstmt.setBigDecimal(7, book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO);
            pstmt.setString(8, book.getPdfPath());
            pstmt.setString(9, book.getCoverPath());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getInt(1));
                }
                DBConnection.closeResultSet(generatedKeys);
                return true;
            }
            return false;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error adding book to database: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Retrieves all books from the database ordered by ID descending.
     * 
     * @return List of Book objects
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, category, mood, purpose, summary, price, pdf_path, cover_path, created_at "
                   + "FROM books ORDER BY id DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = mapResultSetToBook(rs);
                books.add(book);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching all books: " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return books;
    }

    /**
     * Searches for books matching keyword in title, author, or category.
     * If keyword is null or empty, returns all books.
     * 
     * @param keyword Search query term
     * @return List of matched Book objects
     */
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }

        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, category, mood, purpose, summary, price, pdf_path, cover_path, created_at "
                   + "FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(category) LIKE ? "
                   + "ORDER BY id DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            String sqlKeyword = "%" + keyword.trim().toLowerCase() + "%";
            pstmt.setString(1, sqlKeyword);
            pstmt.setString(2, sqlKeyword);
            pstmt.setString(3, sqlKeyword);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = mapResultSetToBook(rs);
                books.add(book);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error searching books with keyword '" + keyword + "': " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return books;
    }

    /**
     * Retrieves book recommendations filtered by mood and/or purpose.
     * 
     * @param mood Filter mood (e.g. Focus, Relaxed, Inspired) or 'All'
     * @param purpose Filter purpose (e.g. Career, Study, Exam Prep) or 'All'
     * @return Filtered list of Book objects
     */
    public List<Book> getRecommendations(String mood, String purpose) {
        boolean hasMood = (mood != null && !mood.trim().isEmpty() && !"all".equalsIgnoreCase(mood.trim()));
        boolean hasPurpose = (purpose != null && !purpose.trim().isEmpty() && !"all".equalsIgnoreCase(purpose.trim()));

        if (!hasMood && !hasPurpose) {
            return getAllBooks();
        }

        List<Book> books = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, title, author, category, mood, purpose, summary, price, pdf_path, cover_path, created_at FROM books WHERE ");
        
        if (hasMood && hasPurpose) {
            sql.append("LOWER(mood) = LOWER(?) AND LOWER(purpose) = LOWER(?) ");
        } else if (hasMood) {
            sql.append("LOWER(mood) = LOWER(?) ");
        } else {
            sql.append("LOWER(purpose) = LOWER(?) ");
        }
        sql.append("ORDER BY id DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            if (hasMood && hasPurpose) {
                pstmt.setString(1, mood.trim());
                pstmt.setString(2, purpose.trim());
            } else if (hasMood) {
                pstmt.setString(1, mood.trim());
            } else {
                pstmt.setString(1, purpose.trim());
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error getting recommendations: " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return books;
    }

    /**
     * Retrieves distinct categories from the database.
     * 
     * @return List of category names
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM books WHERE category IS NOT NULL AND category != '' ORDER BY category ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return categories;
    }

    /**
     * Retrieves distinct moods from the database.
     * 
     * @return List of mood strings
     */
    public List<String> getAllMoods() {
        List<String> moods = new ArrayList<>();
        String sql = "SELECT DISTINCT mood FROM books WHERE mood IS NOT NULL AND mood != '' ORDER BY mood ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                moods.add(rs.getString("mood"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching moods: " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return moods;
    }

    /**
     * Retrieves distinct purposes from the database.
     * 
     * @return List of purpose strings
     */
    public List<String> getAllPurposes() {
        List<String> purposes = new ArrayList<>();
        String sql = "SELECT DISTINCT purpose FROM books WHERE purpose IS NOT NULL AND purpose != '' ORDER BY purpose ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                purposes.add(rs.getString("purpose"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching purposes: " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return purposes;
    }

    /**
     * Updates the summary column for a specific book.
     * 
     * @param bookId ID of book
     * @param newSummary New summary text
     * @return true if updated successfully
     */
    public boolean updateSummary(int bookId, String newSummary) {
        String sql = "UPDATE books SET summary = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newSummary);
            pstmt.setInt(2, bookId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error updating book summary: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Retrieves a single book by its primary key ID.
     * 
     * @param bookId Unique book ID
     * @return Populated Book object or null if not found
     */
    public Book getBookById(int bookId) {
        String sql = "SELECT id, title, author, category, mood, purpose, summary, price, pdf_path, cover_path, created_at "
                   + "FROM books WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching book by ID (" + bookId + "): " + e.getMessage());
            return null;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }

    /**
     * Updates an existing book record. If new PDF or Cover paths are not provided, retains the existing ones.
     * 
     * @param book Book object containing updated details
     * @return true if updated successfully, false otherwise
     */
    public boolean updateBook(Book book) {
        if (book == null || book.getId() <= 0) {
            return false;
        }

        Book existing = getBookById(book.getId());
        if (existing == null) {
            return false;
        }

        String pdfPath = (book.getPdfPath() != null && !book.getPdfPath().trim().isEmpty()) 
                         ? book.getPdfPath() : existing.getPdfPath();
        String coverPath = (book.getCoverPath() != null && !book.getCoverPath().trim().isEmpty()) 
                           ? book.getCoverPath() : existing.getCoverPath();

        String sql = "UPDATE books SET title = ?, author = ?, category = ?, mood = ?, purpose = ?, "
                   + "summary = ?, price = ?, pdf_path = ?, cover_path = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, book.getTitle().trim());
            pstmt.setString(2, book.getAuthor().trim());
            pstmt.setString(3, book.getCategory().trim());
            pstmt.setString(4, book.getMood().trim());
            pstmt.setString(5, book.getPurpose().trim());
            pstmt.setString(6, book.getSummary().trim());
            pstmt.setBigDecimal(7, book.getPrice() != null ? book.getPrice() : BigDecimal.ZERO);
            pstmt.setString(8, pdfPath);
            pstmt.setString(9, coverPath);
            pstmt.setInt(10, book.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Deletes a book by its ID. Cascading foreign keys will automatically clean up dependent records.
     * 
     * @param bookId Unique book ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error deleting book (" + bookId + "): " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Retrieves total book count from the database.
     * 
     * @return Total book count
     */
    public int getTotalBooksCount() {
        String sql = "SELECT COUNT(*) FROM books";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error counting books: " + e.getMessage());
            return 0;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }

    /**
     * Helper mapper converting a SQL ResultSet row to a Book model entity.
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setCategory(rs.getString("category"));
        book.setMood(rs.getString("mood"));
        book.setPurpose(rs.getString("purpose"));
        book.setSummary(rs.getString("summary"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setPdfPath(rs.getString("pdf_path"));
        book.setCoverPath(rs.getString("cover_path"));
        book.setCreatedAt(rs.getTimestamp("created_at"));
        return book;
    }
}
