package model;

import java.util.ArrayList;
public class Author {
    private String name;
    private ArrayList<Book> books;
    private int readingTimes;

    public Author() {
        this.books = new ArrayList<>();
        this.readingTimes = 0;
    }
    public Author(String name) {
        this.name = name;
        this.books = new ArrayList<>();
        this.readingTimes = 0;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
    public int getBooksNumber() {
        return books.size();
    }

    public void setReadingTimes(int readingTimes) {
        this.readingTimes = readingTimes;
    }
    public int getReadingTimes() {
        return readingTimes;
    }

    public void addBook(Book book) {
        if (book != null && !books.contains(book)) {
            books.add(book);
        }
    }
    public void removeBook(Book book) {
        books.remove(book);
    }

    public void increaseReadingTimes() {
        readingTimes++;
    }
}