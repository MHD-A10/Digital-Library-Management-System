package service;

import model.*;
import trees.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class BorrowingManager {
    private HashMap<String, BorrowRecord> recordsById;
    private WaitListManager waitListManager;
    private int recordCounter;
    private int defaultLoanDays;
    private int maxBorrowLimit;

    public BorrowingManager() {
        recordsById = new HashMap<>();
        waitListManager = new WaitListManager();
        recordCounter = 0;
        defaultLoanDays = 14;
        maxBorrowLimit = 5;
    }

    private String generateRecordId() {
        recordCounter++;
        return String.format("R%03d", recordCounter);
    }

    public BorrowResult borrow(Borrower borrower, Book book) {
        if (borrower == null || book == null) {
            return BorrowResult.INVALID_INPUT;
        }

        if (hasActiveBorrow(borrower, book)) {
            return BorrowResult.ALREADY_BORROWED;
        }

        if (borrower.getActiveBorrowCount() >= maxBorrowLimit) {
            return BorrowResult.BORROWER_LIMIT_REACHED;
        }

        if (book.getAvailableCopies() <= 0) {
            if (waitListManager.isAlreadyWaiting(borrower, book)) {
                return BorrowResult.ALREADY_WAITLISTED;
            }

            waitListManager.addRequest(borrower, book);
            return BorrowResult.WAITLISTED;
        }

        createBorrowRecord(borrower, book);

        return BorrowResult.SUCCESS;
    }

    private void createBorrowRecord(Borrower borrower, Book book) {
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.incrementBorrowCount();
        borrower.increaseActiveBorrowCount();

        LocalDate now = LocalDate.now();

        BorrowRecord record = new BorrowRecord(
                generateRecordId(),
                borrower,
                book,
                now,
                now.plusDays(defaultLoanDays)
        );

        recordsById.put(record.getRecordId(), record);
    }

    public boolean returnBook(String recordId) {
        if (recordId == null || recordId.trim().isEmpty()) {
            return false;
        }

        BorrowRecord record = recordsById.get(recordId);

        if (record == null || record.isReturned()) {
            return false;
        }

        record.markAsReturned();
        record.getBook().setAvailableCopies(record.getBook().getAvailableCopies() + 1);
        record.getBorrower().decreaseActiveBorrowCount();

        processWaitListForBook(record.getBook());

        return true;
    }

    public void processWaitListForBook(Book book) {
        if (book == null) {
            return;
        }

        ArrayList<WaitListRequest> skippedRequests = new ArrayList<>();

        while (book.getAvailableCopies() > 0 && waitListManager.hasWaitingRequests(book.getISBN())) {
            WaitListRequest request = waitListManager.pollNextRequest(book.getISBN());

            if (request == null) {
                break;
            }

            Borrower borrower = request.getBorrower();

            if (hasActiveBorrow(borrower, book)) {
                continue;
            }

            if (borrower.getActiveBorrowCount() >= maxBorrowLimit) {
                skippedRequests.add(request);
                continue;
            }

            createBorrowRecord(borrower, book);
        }

        for (WaitListRequest request : skippedRequests) {
            waitListManager.addExistingRequest(request);
        }
    }

    public boolean hasActiveBorrow(Borrower borrower, Book book) {
        if (borrower == null || book == null) {
            return false;
        }

        for (BorrowRecord record : recordsById.values()) {
            boolean sameBorrower = record.getBorrower().getId().equals(borrower.getId());
            boolean sameBook = record.getBook().getISBN().equals(book.getISBN());

            if (!record.isReturned() && sameBorrower && sameBook) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<BorrowRecord> getActiveRecordsForBorrower(Borrower borrower) {
        ArrayList<BorrowRecord> result = new ArrayList<>();

        if (borrower == null) {
            return result;
        }

        for (BorrowRecord record : recordsById.values()) {
            boolean sameBorrower = record.getBorrower().getId().equals(borrower.getId());

            if (!record.isReturned() && sameBorrower) {
                result.add(record);
            }
        }

        return result;
    }


    public boolean addExistingRecord(BorrowRecord record) {
        if (record == null || record.getRecordId() == null || record.getRecordId().trim().isEmpty()) {
            return false;
        }

        if (recordsById.containsKey(record.getRecordId())) {
            return false;
        }

        recordsById.put(record.getRecordId(), record);
        updateRecordCounterFromId(record.getRecordId());

        if (!record.isReturned()) {
            record.getBorrower().increaseActiveBorrowCount();
        }

        return true;
    }

    private void updateRecordCounterFromId(String recordId) {
        if (recordId == null || recordId.length() < 2 || Character.toUpperCase(recordId.charAt(0)) != 'R') {
            return;
        }

        try {
            int number = Integer.parseInt(recordId.substring(1));
            if (number > recordCounter) {
                recordCounter = number;
            }
        } catch (NumberFormatException ignored) {
        }
    }
    public BorrowRecord getRecord(String recordId) {
        if (recordId == null || recordId.trim().isEmpty()) {
            return null;
        }

        return recordsById.get(recordId);
    }

    public ArrayList<BorrowRecord> getAllRecords() {
        return new ArrayList<>(recordsById.values());
    }

    public ArrayList<WaitListRequest> getWaitListForBook(String ISBN) {
        return waitListManager.getRequestsForBook(ISBN);
    }

    public void clearWaitListForBook(String ISBN) {
        waitListManager.clearWaitListForBook(ISBN);
    }

    public WaitListManager getWaitListManager() {
        return waitListManager;
    }

}