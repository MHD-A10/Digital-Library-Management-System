package service;

import model.*;
import trees.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class WaitListManager {
    private HashMap<String, PriorityQueue<WaitListRequest>> waitListsByISBN;
    private int requestCounter;
    private long sequenceCounter;

    public WaitListManager() {
        waitListsByISBN = new HashMap<>();
        requestCounter = 0;
        sequenceCounter = 0;
    }

    private String generateRequestId() {
        requestCounter++;
        return String.format("W%03d", requestCounter);
    }

    public boolean addRequest(Borrower borrower, Book book) {
        if (borrower == null || book == null) {
            return false;
        }

        if (isAlreadyWaiting(borrower, book)) {
            return false;
        }

        sequenceCounter++;

        WaitListRequest request = new WaitListRequest(
                generateRequestId(),
                borrower,
                book,
                LocalDateTime.now(),
                sequenceCounter
        );

        String ISBN = book.getISBN();

        waitListsByISBN.putIfAbsent(ISBN, new PriorityQueue<>());
        waitListsByISBN.get(ISBN).add(request);

        return true;
    }

    public void addExistingRequest(WaitListRequest request) {
        if (request == null || request.getBook() == null) {
            return;
        }

        String ISBN = request.getBook().getISBN();

        waitListsByISBN.putIfAbsent(ISBN, new PriorityQueue<>());
        waitListsByISBN.get(ISBN).add(request);
    }

    public WaitListRequest pollNextRequest(String ISBN) {
        PriorityQueue<WaitListRequest> queue = getNormalizedQueue(ISBN);

        if (queue == null || queue.isEmpty()) {
            return null;
        }

        return queue.poll();
    }

    public boolean hasWaitingRequests(String ISBN) {
        PriorityQueue<WaitListRequest> queue = waitListsByISBN.get(ISBN);

        return queue != null && !queue.isEmpty();
    }

    public boolean isAlreadyWaiting(Borrower borrower, Book book) {
        if (borrower == null || book == null) {
            return false;
        }

        PriorityQueue<WaitListRequest> queue = waitListsByISBN.get(book.getISBN());

        if (queue == null) {
            return false;
        }

        for (WaitListRequest request : queue) {
            boolean sameBorrower = request.getBorrower().getId().equals(borrower.getId());
            boolean sameBook = request.getBook().getISBN().equals(book.getISBN());

            if (sameBorrower && sameBook) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<WaitListRequest> getRequestsForBook(String ISBN) {
        ArrayList<WaitListRequest> result = new ArrayList<>();

        PriorityQueue<WaitListRequest> queue = getNormalizedQueue(ISBN);

        if (queue == null) {
            return result;
        }

        result.addAll(queue);
        result.sort(null);

        return result;
    }


    private PriorityQueue<WaitListRequest> getNormalizedQueue(String ISBN) {
        PriorityQueue<WaitListRequest> queue = waitListsByISBN.get(ISBN);

        if (queue == null) {
            return null;
        }

        ArrayList<WaitListRequest> ordered = new ArrayList<>(queue);
        ordered.sort(null);

        PriorityQueue<WaitListRequest> normalized = new PriorityQueue<>();
        normalized.addAll(ordered);
        waitListsByISBN.put(ISBN, normalized);

        return normalized;
    }
    public void clearWaitListForBook(String ISBN) {
        waitListsByISBN.remove(ISBN);
    }
}