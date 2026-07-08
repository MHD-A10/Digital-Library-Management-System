package trees;

import model.*;

public class BookAVLNode {
    private Book book;
    private BookAVLNode left;
    private BookAVLNode right;
    private int height;

    public BookAVLNode(Book book) {
        this.book = book;
        this.left = null;
        this.right = null;
        this.height = 1;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookAVLNode getLeft() {
        return left;
    }

    public void setLeft(BookAVLNode left) {
        this.left = left;
    }

    public BookAVLNode getRight() {
        return right;
    }

    public void setRight(BookAVLNode right) {
        this.right = right;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}