package service;

import model.*;
import trees.*;

import java.util.ArrayList;
import java.util.HashMap;

public class LibraryManager {
    private BookAVLTree booksTree;
    private ArrayList<Book> booksInsertionOrder;
    private ArrayList<Author> authors;
    private HashMap<String, Borrower> borrowersById;
    private BorrowingManager borrowingManager;

    public LibraryManager() {
        booksTree = new BookAVLTree();
        booksInsertionOrder = new ArrayList<>();
        authors = new ArrayList<>();
        borrowersById = new HashMap<>();
        borrowingManager = new BorrowingManager();
    }

    public boolean addBook(String ISBN, String title, String authorName, int totalCopies) {
        if (!Book.isValidISBN(ISBN)) {
            return false;
        }

        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        if (authorName == null || authorName.trim().isEmpty()) {
            return false;
        }

        if (totalCopies < 0) {
            return false;
        }

        if (searchBookByISBN(ISBN) != null) {
            return false;
        }

        Author author = getOrCreateAuthor(authorName);

        try {
            Book book = new Book(ISBN, title, author, totalCopies);

            boolean inserted = booksTree.insert(book);

            if (!inserted) {
                book.setAuthor(null);
                return false;
            }

            booksInsertionOrder.add(book);
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to add book: " + e.getMessage());
            return false;
        }
    }

    public Book searchBookByISBN(String ISBN) {
        if (!Book.isValidISBN(ISBN)) {
            return null;
        }

        return booksTree.search(ISBN);
    }

    public boolean updateBookCopies(String ISBN, int newTotalCopies) {
        Book book = searchBookByISBN(ISBN);

        if (book == null || newTotalCopies < 0) {
            return false;
        }

        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();

        if (newTotalCopies < borrowedCopies) {
            return false;
        }

        book.setTotalCopies(newTotalCopies);
        book.setAvailableCopies(newTotalCopies - borrowedCopies);

        borrowingManager.processWaitListForBook(book);

        return true;
    }

    public boolean deleteBook(String ISBN) {
        Book book = searchBookByISBN(ISBN);

        if (book == null) {
            return false;
        }

        if (book.getAvailableCopies() < book.getTotalCopies()) {
            return false;
        }

        boolean deleted = booksTree.delete(ISBN);

        if (deleted) {
            book.setAuthor(null);
            booksInsertionOrder.remove(book);
            borrowingManager.clearWaitListForBook(ISBN);
        }

        return deleted;
    }

    private Author getOrCreateAuthor(String authorName) {
        Author author = searchAuthorByName(authorName);

        if (author != null) {
            return author;
        }

        Author newAuthor = new Author(authorName);
        authors.add(newAuthor);

        return newAuthor;
    }

    public Author searchAuthorByName(String authorName) {
        if (authorName == null || authorName.trim().isEmpty()) {
            return null;
        }

        for (Author author : authors) {
            if (author.getName().equalsIgnoreCase(authorName)) {
                return author;
            }
        }

        return null;
    }

    public boolean addBorrower(String id, String name, boolean graduatingStudent) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (borrowersById.containsKey(id)) {
            return false;
        }

        try {
            Borrower borrower = new Borrower(id, name, graduatingStudent);
            borrowersById.put(id, borrower);

            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to add borrower: " + e.getMessage());
            return false;
        }
    }

    public Borrower searchBorrowerById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        return borrowersById.get(id);
    }

    public BorrowResult borrowBook(String borrowerId, String ISBN) {
        Borrower borrower = searchBorrowerById(borrowerId);
        Book book = searchBookByISBN(ISBN);

        return borrowingManager.borrow(borrower, book);
    }

    public boolean returnBook(String recordId) {
        return borrowingManager.returnBook(recordId);
    }

    public BorrowRecord getBorrowRecord(String recordId) {
        return borrowingManager.getRecord(recordId);
    }

    public ArrayList<BorrowRecord> getAllBorrowRecords() {
        return borrowingManager.getAllRecords();
    }

    public ArrayList<BorrowRecord> getActiveRecordsForBorrower(String borrowerId) {
        Borrower borrower = searchBorrowerById(borrowerId);
        return borrowingManager.getActiveRecordsForBorrower(borrower);
    }

    public ArrayList<WaitListRequest> getWaitListForBook(String ISBN) {
        if (!Book.isValidISBN(ISBN)) {
            return new ArrayList<>();
        }

        return borrowingManager.getWaitListForBook(ISBN);
    }

    public ArrayList<Book> getBooks() {
        return booksTree.getAllBooksInOrder();
    }

    public ArrayList<Book> getBooksInInsertionOrder() {
        return new ArrayList<>(booksInsertionOrder);
    }

    public ArrayList<Author> getAuthors() {
        return authors;
    }

    public ArrayList<Borrower> getBorrowers() {
        return new ArrayList<>(borrowersById.values());
    }


    public void replaceDataFrom(LibraryManager other) {
        if (other == null) {
            return;
        }

        this.booksTree = other.booksTree;
        this.booksInsertionOrder = other.booksInsertionOrder;
        this.authors = other.authors;
        this.borrowersById = other.borrowersById;
        this.borrowingManager = other.borrowingManager;
    }
    public void printBooksInOrder() {
        booksTree.printInOrder();
    }

    public BorrowingManager getBorrowingManager() {
        return borrowingManager;
    }

    public BookAVLNode getAVLRoot() {
        return booksTree.getRoot();
    }
}
