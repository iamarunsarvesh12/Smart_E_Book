package com.ebook.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String author;
    private String category;
    private String mood;
    private String purpose;
    private String summary;
    private BigDecimal price;
    private String pdfPath;
    private String coverPath;
    private Timestamp createdAt;

    // Default No-Argument Constructor
    public Book() {
    }

    // Constructor without ID and CreatedAt (Useful for book creation / uploading)
    public Book(String title, String author, String category, String mood, String purpose, 
                String summary, BigDecimal price, String pdfPath, String coverPath) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.mood = mood;
        this.purpose = purpose;
        this.summary = summary;
        this.price = price;
        this.pdfPath = pdfPath;
        this.coverPath = coverPath;
    }

    // Full Parameterized Constructor
    public Book(int id, String title, String author, String category, String mood, String purpose, 
                String summary, BigDecimal price, String pdfPath, String coverPath, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.mood = mood;
        this.purpose = purpose;
        this.summary = summary;
        this.price = price;
        this.pdfPath = pdfPath;
        this.coverPath = coverPath;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", mood='" + mood + '\'' +
                ", purpose='" + purpose + '\'' +
                ", price=" + price +
                ", pdfPath='" + pdfPath + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
