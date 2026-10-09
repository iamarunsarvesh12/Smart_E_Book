package com.ebook.dao;

import com.ebook.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DownloadDAO {

    /**
     * Logs a book download activity into the downloads audit table.
     * 
     * @param userId ID of downloading user
     * @param bookId ID of book downloaded
     * @return true if logged successfully
     */
    public boolean logDownload(int userId, int bookId) {
        String sql = "INSERT INTO downloads (user_id, book_id, download_time) VALUES (?, ?, NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error logging book download: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Retrieves total download count for a specific publication.
     * 
     * @param bookId Book ID
     * @return Total count of downloads
     */
    public int getDownloadCount(int bookId) {
        String sql = "SELECT COUNT(*) FROM downloads WHERE book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error counting downloads: " + e.getMessage());
            return 0;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }
}
