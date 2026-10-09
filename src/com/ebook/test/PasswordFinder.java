package com.ebook.test;

import java.sql.Connection;
import java.sql.DriverManager;

public class PasswordFinder {
    public static void main(String[] args) {
        String[] candidates = {"root", "", "password", "admin", "123456", "1234", "root123", "mysql", "12345678", "password123"};
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("Driver error: " + e.getMessage());
            return;
        }

        for (String pwd : candidates) {
            try {
                Connection conn = DriverManager.getConnection(url, "root", pwd);
                System.out.println("FOUND_MYSQL_PASSWORD: [" + pwd + "]");
                conn.close();
                return;
            } catch (Exception e) {
                System.out.println("Failed with [" + pwd + "]: " + e.getMessage());
            }
        }
        System.out.println("None of the standard passwords matched.");
    }
}
