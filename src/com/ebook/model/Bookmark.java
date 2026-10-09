package com.ebook.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Bookmark implements Serializable {
    private static final long serialVersionUID = 1L;

    private int bookmarkId;
    private int userId;
    private int bookId;
    private int lastPageRead;
    private Timestamp updatedAt;

    private String bookTitle;
    private String coverPath;

    public Bookmark() {
    }

    public Bookmark(int userId, int bookId, int lastPageRead) {
        this.userId = userId;
        this.bookId = bookId;
        this.lastPageRead = lastPageRead;
    }

    public Bookmark(int bookmarkId, int userId, int bookId, int lastPageRead, Timestamp updatedAt) {
        this.bookmarkId = bookmarkId;
        this.userId = userId;
        this.bookId = bookId;
        this.lastPageRead = lastPageRead;
        this.updatedAt = updatedAt;
    }

    public int getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(int bookmarkId) {
        this.bookmarkId = bookmarkId;
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

    public int getLastPageRead() {
        return lastPageRead;
    }

    public void setLastPageRead(int lastPageRead) {
        this.lastPageRead = lastPageRead;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "bookmarkId=" + bookmarkId +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", lastPageRead=" + lastPageRead +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
