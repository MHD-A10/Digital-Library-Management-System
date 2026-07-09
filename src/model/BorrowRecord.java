package model;

import java.time.LocalDate;

public class BorrowRecord {
    private String recordId;
    private Borrower borrower;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate expectedReturnDate;
    private boolean returned;

    public BorrowRecord() {
    }

    public BorrowRecord(String recordId, Borrower borrower, Book book,
                        LocalDate borrowDate, LocalDate expectedReturnDate) {
        setRecordId(recordId);
        setBorrower(borrower);
        setBook(book);
        setBorrowDate(borrowDate);
        setExpectedReturnDate(expectedReturnDate);
        this.returned = false;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        if (recordId == null || recordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Record ID cannot be empty.");
        }

        this.recordId = recordId;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public void setBorrower(Borrower borrower) {
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower cannot be null.");
        }

        this.borrower = borrower;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null.");
        }

        this.book = book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        if (borrowDate == null) {
            throw new IllegalArgumentException("Borrow date cannot be null.");
        }

        this.borrowDate = borrowDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        if (expectedReturnDate == null) {
            throw new IllegalArgumentException("Expected return date cannot be null.");
        }

        this.expectedReturnDate = expectedReturnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markAsReturned() {
        this.returned = true;
    }
}