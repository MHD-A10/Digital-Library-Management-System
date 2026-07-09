package model;

public class Book {
    private String ISBN;
    private String title;
    private Author author;
    private int totalCopies;
    private int availableCopies;
    private int borrowCount;

    public Book() {

    }
    public Book(String ISBN, String title, Author author, int totalCopies) {
        setISBN(ISBN);
        setTitle(title);
        setTotalCopies(totalCopies);
        this.availableCopies = totalCopies;
        this.borrowCount = 0;

        setAuthor(author);
    }

    public static boolean isValidISBN(String ISBN) {
        if (ISBN == null || ISBN.trim().isEmpty()) {
            return false;
        }

        return ISBN.matches("\\d{3}-\\d{3}");
    }

    public void setISBN(String ISBN) {
        if (!isValidISBN(ISBN)) {
            throw new IllegalArgumentException("ISBN must be in the format XXX-XXX, digits only.");
        }

        this.ISBN = ISBN;
    }
    public String getISBN() {
        return ISBN;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty.");
        }

        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setAuthor(Author newAuthor) {
        if (this.author != null) {
            this.author.removeBook(this);
        }

        this.author = newAuthor;

        if (this.author != null) {
            this.author.addBook(this);
        }
    }
    public Author getAuthor() {
        return author;
    }
    public String getAuthorName() {
        if (author == null) {
            return "";
        }

        return author.getName();
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative.");
        }

        this.totalCopies = totalCopies;
    }
    public int getTotalCopies() {
        return totalCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative.");
        }

        this.availableCopies = availableCopies;
    }
    public int getAvailableCopies() {
        return availableCopies;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        if (borrowCount < 0) {
            throw new IllegalArgumentException("Borrow count cannot be negative.");
        }

        this.borrowCount = borrowCount;
    }

    public void incrementBorrowCount() {
        borrowCount++;

        if (author != null) {
            author.increaseReadingTimes();
        }
    }
}