package com.ebook.dao;

import com.ebook.model.User;
import com.ebook.util.DBConnection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    /**
     * Converts a raw plaintext password into a SHA-256 hexadecimal string.
     * 
     * @param base Raw password string
     * @return 64-character SHA-256 hex string
     */
    public static String hashPassword(String base) {
        if (base == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found in JVM runtime.", e);
        }
    }

    /**
     * Checks if a given email is already registered in the users table.
     * 
     * @param email Email address to verify
     * @return true if email exists, false otherwise
     */
    public boolean isEmailTaken(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email.trim().toLowerCase());
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error checking email availability: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }

    /**
     * Registers a new user into the database.
     * Checks for duplicate email, hashes the raw password with SHA-256, and inserts the user.
     * 
     * @param user User object containing registration details
     * @return true if user registration was successful, false otherwise
     */
    public boolean registerUser(User user) {
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            return false;
        }

        String sanitizedEmail = user.getEmail().trim().toLowerCase();
        if (isEmailTaken(sanitizedEmail)) {
            return false;
        }

        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Hash password before persisting
            String hashedPassword = hashPassword(user.getPassword());
            String role = (user.getRole() != null && !user.getRole().trim().isEmpty()) ? user.getRole() : "user";

            pstmt.setString(1, user.getName().trim());
            pstmt.setString(2, sanitizedEmail);
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, role);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                DBConnection.closeResultSet(generatedKeys);
                return true;
            }
            return false;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error during user registration: " + e.getMessage());
            return false;
        } finally {
            DBConnection.closeResources(null, pstmt, conn);
        }
    }

    /**
     * Authenticates a user with email and raw password.
     * Hashes the raw password with SHA-256 and queries the database.
     * 
     * @param email User email
     * @param rawPassword User plaintext password
     * @return Authenticated User object if valid, null otherwise
     */
    public User authenticateUser(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return null;
        }

        String sql = "SELECT id, name, email, password, role, created_at FROM users WHERE email = ? AND password = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            String hashedPassword = hashPassword(rawPassword);
            pstmt.setString(1, email.trim().toLowerCase());
            pstmt.setString(2, hashedPassword);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error during user authentication: " + e.getMessage());
            return null;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }

    /**
     * Retrieves a user by their unique primary key ID.
     * 
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(int userId) {
        String sql = "SELECT id, name, email, password, role, created_at FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            return null;
        } finally {
            DBConnection.closeResources(rs, pstmt, conn);
        }
    }
}
