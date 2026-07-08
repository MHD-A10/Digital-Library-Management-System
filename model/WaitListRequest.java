package model;

import java.time.LocalDateTime;

public class WaitListRequest implements Comparable<WaitListRequest> {
    private String requestId;
    private Borrower borrower;
    private Book book;
    private LocalDateTime requestTime;
    private long sequenceNumber;

    public WaitListRequest(String requestId, Borrower borrower, Book book,
                           LocalDateTime requestTime, long sequenceNumber) {
        this.requestId = requestId;
        this.borrower = borrower;
        this.book = book;
        this.requestTime = requestTime;
        this.sequenceNumber = sequenceNumber;
    }

    public String getRequestId() {
        return requestId;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public Book getBook() {
        return book;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public int compareTo(WaitListRequest other) {
        if (this.borrower.isGraduatingStudent() && !other.borrower.isGraduatingStudent()) {
            return -1;
        }

        if (!this.borrower.isGraduatingStudent() && other.borrower.isGraduatingStudent()) {
            return 1;
        }

        return Long.compare(this.sequenceNumber, other.sequenceNumber);
    }
}