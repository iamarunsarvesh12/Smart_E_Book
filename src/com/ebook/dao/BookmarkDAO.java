package com.ebook.dao;

import com.ebook.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookmarkDAO {

    /**
     * Saves or updates reading progress bookmark using MySQL ON DUPLICATE KEY UPDATE.
     * 
     * @param userId User ID
     * @param bookId Book ID
     * @param lastPage Page number to save
     * @return true if persisted successfully
     */
    public boolean saveOrUpdateProgress(int userId, int bookId, int lastPage) {
        String sql = "INSERT INTO bookmarks (user_id, book_id, last_page_read) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE last_page_read = ?, updated_at = NOW()";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            pstmt.setInt(3, Math.max(1, lastPage));
            pstmt.setInt(4, Math.max(1, lastPage));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error saving reading bookmark: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Retrieves the last recorded page read for a user and book.
     * 
     * @param userId User ID
     * @param bookId Book ID
     * @return Page number (defaults to 1 if not previously bookmarked)
     */
    public int getLastReadPage(int userId, int bookId) {
        String sql = "SELECT last_page_read FROM bookmarks WHERE user_id = ? AND book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("last_page_read");
            }
            return 1;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error retrieving reading progress: " + e.getMessage());
            return 1;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }
}
