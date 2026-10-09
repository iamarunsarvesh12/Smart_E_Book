package com.ebook.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Download implements Serializable {
    private static final long serialVersionUID = 1L;

    private int downloadId;
    private int userId;
    private int bookId;
    private Timestamp downloadTime;

    private String bookTitle;
    private String userName;

    public Download() {
    }

    public Download(int userId, int bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    public Download(int downloadId, int userId, int bookId, Timestamp downloadTime) {
        this.downloadId = downloadId;
        this.userId = userId;
        this.bookId = bookId;
        this.downloadTime = downloadTime;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
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

    public Timestamp getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(Timestamp downloadTime) {
        this.downloadTime = downloadTime;
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
        return "Download{" +
                "downloadId=" + downloadId +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", downloadTime=" + downloadTime +
                '}';
    }
}
