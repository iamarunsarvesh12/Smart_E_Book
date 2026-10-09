package com.ebook.dao;

import com.ebook.model.Purchase;
import com.ebook.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PurchaseDAO {

    /**
     * Records a completed purchase in the database.
     * 
     * @param userId ID of purchasing user
     * @param bookId ID of book being purchased
     * @param amount Transaction amount
     * @return true if successfully recorded, false otherwise
     */
    public boolean recordPurchase(int userId, int bookId, double amount) {
        String sql = "INSERT INTO purchases (user_id, book_id, amount, payment_status, order_date) "
                   + "VALUES (?, ?, ?, 'COMPLETED', NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            pstmt.setBigDecimal(3, BigDecimal.valueOf(amount));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error recording purchase (user=" + userId + ", book=" + bookId + "): " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Checks if a user has completed a purchase for a specific book.
     * 
     * @param userId User ID
     * @param bookId Book ID
     * @return true if purchase record exists with COMPLETED status
     */
    public boolean hasUserPurchased(int userId, int bookId) {
        String sql = "SELECT order_id FROM purchases WHERE user_id = ? AND book_id = ? AND payment_status = 'COMPLETED'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error checking purchase status: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }

    /**
     * Retrieves a set of book IDs purchased by a user for fast O(1) lookup.
     * 
     * @param userId User ID
     * @return Set of purchased book IDs
     */
    public Set<Integer> getPurchasedBookIds(int userId) {
        Set<Integer> bookIds = new HashSet<>();
        String sql = "SELECT book_id FROM purchases WHERE user_id = ? AND payment_status = 'COMPLETED'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                bookIds.add(rs.getInt("book_id"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching purchased book IDs: " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return bookIds;
    }

    /**
     * Retrieves all purchase history records for a user with joined book titles.
     * 
     * @param userId User ID
     * @return List of Purchase entities
     */
    public List<Purchase> getUserPurchases(int userId) {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT p.order_id, p.user_id, p.book_id, p.amount, p.payment_status, p.order_date, b.title AS book_title "
                   + "FROM purchases p "
                   + "JOIN books b ON p.book_id = b.id "
                   + "WHERE p.user_id = ? ORDER BY p.order_date DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Purchase p = new Purchase();
                p.setOrderId(rs.getInt("order_id"));
                p.setUserId(rs.getInt("user_id"));
                p.setBookId(rs.getInt("book_id"));
                p.setAmount(rs.getBigDecimal("amount"));
                p.setPaymentStatus(rs.getString("payment_status"));
                p.setOrderDate(rs.getTimestamp("order_date"));
                p.setBookTitle(rs.getString("book_title"));
                purchases.add(p);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching user purchases: " + e.getMessage());
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
        return purchases;
    }
}
