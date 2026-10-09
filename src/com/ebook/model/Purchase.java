package com.ebook.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Purchase implements Serializable {
    private static final long serialVersionUID = 1L;

    private int orderId;
    private int userId;
    private int bookId;
    private BigDecimal amount;
    private String paymentStatus; // e.g., 'COMPLETED', 'PENDING', 'FAILED'
    private Timestamp orderDate;

    // Optional book & user title/name references for joined views
    private String bookTitle;
    private String userName;

    public Purchase() {
    }

    public Purchase(int userId, int bookId, BigDecimal amount, String paymentStatus) {
        this.userId = userId;
        this.bookId = bookId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    public Purchase(int orderId, int userId, int bookId, BigDecimal amount, String paymentStatus, Timestamp orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.bookId = bookId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.orderDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", amount=" + amount +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
}
