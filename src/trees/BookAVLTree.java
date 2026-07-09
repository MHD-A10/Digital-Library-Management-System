package trees;

import model.*;

import java.util.ArrayList;

public class BookAVLTree {
    private BookAVLNode root;
    private boolean inserted;

    public BookAVLTree() {
        this.root = null;
        this.inserted = false;
    }

    public BookAVLNode getRoot() {
        return root;
    }

    public boolean insert(Book book) {
        if (book == null || !Book.isValidISBN(book.getISBN())) {
            return false;
        }

        inserted = false;
        root = insertRecursive(root, book);

        return inserted;
    }

    private BookAVLNode insertRecursive(BookAVLNode current, Book book) {
        if (current == null) {
            inserted = true;
            return new BookAVLNode(book);
        }

        String newISBN = book.getISBN();
        String currentISBN = current.getBook().getISBN();

        int comparison = newISBN.compareToIgnoreCase(currentISBN);

        if (comparison == 0) {
            return current;
        }

        if (comparison < 0) {
            BookAVLNode newLeft = insertRecursive(current.getLeft(), book);
            current.setLeft(newLeft);
        } else {
            BookAVLNode newRight = insertRecursive(current.getRight(), book);
            current.setRight(newRight);
        }

        updateHeight(current);

        return rebalance(current);
    }

    public Book search(String ISBN) {
        if (!Book.isValidISBN(ISBN)) {
            return null;
        }

        return searchRecursive(root, ISBN);
    }

    private Book searchRecursive(BookAVLNode current, String ISBN) {
        if (current == null) {
            return null;
        }

        String wantedISBN = ISBN;
        String currentISBN = current.getBook().getISBN();

        int comparison = wantedISBN.compareToIgnoreCase(currentISBN);

        if (comparison == 0) {
            return current.getBook();
        }

        if (comparison < 0) {
            return searchRecursive(current.getLeft(), ISBN);
        } else {
            return searchRecursive(current.getRight(), ISBN);
        }
    }

    private int height(BookAVLNode node) {
        if (node == null) {
            return 0;
        }

        return node.getHeight();
    }

    private void updateHeight(BookAVLNode node) {
        int leftHeight = height(node.getLeft());
        int rightHeight = height(node.getRight());

        int newHeight = Math.max(leftHeight, rightHeight) + 1;

        node.setHeight(newHeight);
    }

    private int getBalance(BookAVLNode node) {
        if (node == null) {
            return 0;
        }

        return height(node.getLeft()) - height(node.getRight());
    }

    private BookAVLNode rebalance(BookAVLNode node) {
        int balance = getBalance(node);

        if (balance > 1) {
            if (getBalance(node.getLeft()) < 0) {
                BookAVLNode newLeft = rotateLeft(node.getLeft());
                node.setLeft(newLeft);
            }

            return rotateRight(node);
        }

        if (balance < -1) {
            if (getBalance(node.getRight()) > 0) {
                BookAVLNode newRight = rotateRight(node.getRight());
                node.setRight(newRight);
            }

            return rotateLeft(node);
        }

        return node;
    }

    private BookAVLNode rotateRight(BookAVLNode oldRoot) {
        BookAVLNode newRoot = oldRoot.getLeft();
        BookAVLNode movedSubtree = newRoot.getRight();

        newRoot.setRight(oldRoot);
        oldRoot.setLeft(movedSubtree);

        updateHeight(oldRoot);
        updateHeight(newRoot);

        return newRoot;
    }

    private BookAVLNode rotateLeft(BookAVLNode oldRoot) {
        BookAVLNode newRoot = oldRoot.getRight();
        BookAVLNode movedSubtree = newRoot.getLeft();

        newRoot.setLeft(oldRoot);
        oldRoot.setRight(movedSubtree);

        updateHeight(oldRoot);
        updateHeight(newRoot);

        return newRoot;
    }

    public void printInOrder() {
        printInOrderRecursive(root);
    }

    private void printInOrderRecursive(BookAVLNode current) {
        if (current == null) {
            return;
        }

        printInOrderRecursive(current.getLeft());

        Book book = current.getBook();

        System.out.println(
                "ISBN: " + book.getISBN()
                        + ", Title: " + book.getTitle()
                        + ", Author: " + book.getAuthorName()
                        + ", Height: " + current.getHeight()
        );

        printInOrderRecursive(current.getRight());
    }

    public ArrayList<Book> getAllBooksInOrder() {
        ArrayList<Book> books = new ArrayList<>();
        fillBooksInOrder(root, books);
        return books;
    }

    private void fillBooksInOrder(BookAVLNode current, ArrayList<Book> books) {
        if (current == null) {
            return;
        }

        fillBooksInOrder(current.getLeft(), books);
        books.add(current.getBook());
        fillBooksInOrder(current.getRight(), books);
    }

    public boolean delete(String ISBN) {
        if (!Book.isValidISBN(ISBN)) {
            return false;
        }

        if (search(ISBN) == null) {
            return false;
        }

        root = deleteRecursive(root, ISBN);
        return true;
    }

    private BookAVLNode deleteRecursive(BookAVLNode current, String ISBN) {
        if (current == null) {
            return null;
        }

        String currentISBN = current.getBook().getISBN();

        if (ISBN.compareToIgnoreCase(currentISBN) < 0) {
            BookAVLNode newLeft = deleteRecursive(current.getLeft(), ISBN);
            current.setLeft(newLeft);
        } else if (ISBN.compareToIgnoreCase(currentISBN) > 0) {
            BookAVLNode newRight = deleteRecursive(current.getRight(), ISBN);
            current.setRight(newRight);
        } else {
            if (current.getLeft() == null && current.getRight() == null) {
                return null;
            }

            if (current.getLeft() == null) {
                return current.getRight();
            }

            if (current.getRight() == null) {
                return current.getLeft();
            }

            BookAVLNode smallestNode = findSmallestNode(current.getRight());
            current.setBook(smallestNode.getBook());

            BookAVLNode newRight = deleteRecursive(current.getRight(), smallestNode.getBook().getISBN());
            current.setRight(newRight);
        }

        updateHeight(current);

        return rebalance(current);
    }

    private BookAVLNode findSmallestNode(BookAVLNode current) {
        while (current.getLeft() != null) {
            current = current.getLeft();
        }

        return current;
    }
}