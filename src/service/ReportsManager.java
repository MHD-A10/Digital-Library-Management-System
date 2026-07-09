package service;

import model.*;
import trees.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

public class ReportsManager {
    private static final int REPORT_WIDTH = 92;


    public ArrayList<Book> getMostBorrowedBooks(ArrayList<Book> books) {
        ArrayList<Book> result = new ArrayList<>();

        for (Book book : books) {
            if (book.getBorrowCount() > 0) {
                result.add(book);
            }
        }

        result.sort((book1, book2) -> {
            int borrowCompare = Integer.compare(book2.getBorrowCount(), book1.getBorrowCount());

            if (borrowCompare != 0) {
                return borrowCompare;
            }

            return book1.getISBN().compareToIgnoreCase(book2.getISBN());
        });

        return result;
    }

    public ArrayList<Author> getMostReadAuthors(ArrayList<Author> authors) {
        ArrayList<Author> result = new ArrayList<>();

        for (Author author : authors) {
            if (author.getReadingTimes() > 0) {
                result.add(author);
            }
        }

        result.sort((author1, author2) -> {
            int readingCompare = Integer.compare(author2.getReadingTimes(), author1.getReadingTimes());

            if (readingCompare != 0) {
                return readingCompare;
            }

            return author1.getName().compareToIgnoreCase(author2.getName());
        });

        return result;
    }

    public ArrayList<BorrowRecord> getOverdueRecords(ArrayList<BorrowRecord> records) {
        ArrayList<BorrowRecord> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (BorrowRecord record : records) {
            if (!record.isReturned() && today.isAfter(record.getExpectedReturnDate())) {
                result.add(record);
            }
        }

        result.sort((record1, record2) ->
                record1.getExpectedReturnDate().compareTo(record2.getExpectedReturnDate())
        );

        return result;
    }

    public int getTotalAvailableCopies(ArrayList<Book> books) {
        int total = 0;

        for (Book book : books) {
            total += book.getAvailableCopies();
        }

        return total;
    }

    public int getAvailableTitlesCount(ArrayList<Book> books) {
        int count = 0;

        for (Book book : books) {
            if (book.getAvailableCopies() > 0) {
                count++;
            }
        }

        return count;
    }

    public int getTotalCopies(ArrayList<Book> books) {
        int total = 0;

        for (Book book : books) {
            total += book.getTotalCopies();
        }

        return total;
    }

    public int getBorrowedCopiesCount(ArrayList<Book> books) {
        int total = 0;

        for (Book book : books) {
            total += book.getTotalCopies() - book.getAvailableCopies();
        }

        return total;
    }

    public int getActiveBorrowRecordsCount(ArrayList<BorrowRecord> records) {
        int count = 0;

        for (BorrowRecord record : records) {
            if (!record.isReturned()) {
                count++;
            }
        }

        return count;
    }

    public int getReturnedBorrowRecordsCount(ArrayList<BorrowRecord> records) {
        int count = 0;

        for (BorrowRecord record : records) {
            if (record.isReturned()) {
                count++;
            }
        }

        return count;
    }

    public String generateFullReport(ArrayList<Book> books,
                                     ArrayList<Author> authors,
                                     ArrayList<BorrowRecord> records) {
        StringBuilder report = new StringBuilder();

        appendMainHeader(report, "DIGITAL LIBRARY ANALYTICS REPORT");

        appendSummarySection(report, books, authors, records);
        appendMostBorrowedBooksSection(report, books);
        appendMostReadAuthorsSection(report, authors);
        appendAvailabilitySection(report, books);
        appendOverdueRecordsSection(report, records);
        appendBorrowRecordsSection(report, records);

        appendFooter(report);

        return report.toString();
    }

    public void printFullReport(ArrayList<Book> books,
                                ArrayList<Author> authors,
                                ArrayList<BorrowRecord> records) {
        String report = generateFullReport(books, authors, records);
        System.out.println(report);
    }

    public boolean exportFullReportToFile(ArrayList<Book> books,
                                          ArrayList<Author> authors,
                                          ArrayList<BorrowRecord> records,
                                          String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        String report = generateFullReport(books, authors, records);

        try (FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
            writer.write(report);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to export report: " + e.getMessage());
            return false;
        }
    }

    private void appendSummarySection(StringBuilder report,
                                      ArrayList<Book> books,
                                      ArrayList<Author> authors,
                                      ArrayList<BorrowRecord> records) {
        appendHeader(report, "SUMMARY");

        appendMetric(report, "Book titles", books.size());
        appendMetric(report, "Authors", authors.size());
        appendMetric(report, "Total copies", getTotalCopies(books));
        appendMetric(report, "Available copies", getTotalAvailableCopies(books));
        appendMetric(report, "Available titles", getAvailableTitlesCount(books));
        appendMetric(report, "Borrowed copies", getBorrowedCopiesCount(books));
        appendMetric(report, "All borrow records", records.size());
        appendMetric(report, "Active borrow records", getActiveBorrowRecordsCount(records));
        appendMetric(report, "Returned borrow records", getReturnedBorrowRecordsCount(records));
        appendMetric(report, "Overdue borrow records", getOverdueRecords(records).size());
        report.append("\n");
    }
    private void appendMostBorrowedBooksSection(StringBuilder report, ArrayList<Book> books) {
        appendHeader(report, "MOST BORROWED BOOKS");

        ArrayList<Book> mostBorrowedBooks = getMostBorrowedBooks(books);

        if (mostBorrowedBooks.isEmpty()) {
            report.append("  No borrowed books yet.\n\n");
            return;
        }

        appendTableLine(report);
        report.append(String.format("  %-4s %-11s %-24s %-22s %8s%n", "#", "ISBN", "Title", "Author", "Borrows"));
        appendTableLine(report);

        int rank = 1;
        for (Book book : mostBorrowedBooks) {
            report.append(String.format("  %-4d %-11s %-24s %-22s %8d%n",
                    rank,
                    safeText(book.getISBN(), 11),
                    safeText(book.getTitle(), 24),
                    safeText(book.getAuthorName(), 22),
                    book.getBorrowCount()));
            rank++;
        }

        appendTableLine(report);
        report.append("\n");
    }
    private void appendMostReadAuthorsSection(StringBuilder report, ArrayList<Author> authors) {
        appendHeader(report, "MOST READ AUTHORS");

        ArrayList<Author> mostReadAuthors = getMostReadAuthors(authors);

        if (mostReadAuthors.isEmpty()) {
            report.append("  No author reading data yet.\n\n");
            return;
        }

        appendTableLine(report);
        report.append(String.format("  %-4s %-30s %14s %14s%n", "#", "Author", "Read Times", "Books Count"));
        appendTableLine(report);

        int rank = 1;
        for (Author author : mostReadAuthors) {
            report.append(String.format("  %-4d %-30s %14d %14d%n",
                    rank,
                    safeText(author.getName(), 30),
                    author.getReadingTimes(),
                    author.getBooksNumber()));
            rank++;
        }

        appendTableLine(report);
        report.append("\n");
    }
    private void appendAvailabilitySection(StringBuilder report, ArrayList<Book> books) {
        appendHeader(report, "BOOK AVAILABILITY");

        if (books.isEmpty()) {
            report.append("  No books in the system.\n\n");
            return;
        }

        appendTableLine(report);
        report.append(String.format("  %-11s %-25s %8s %10s %9s%n", "ISBN", "Title", "Total", "Available", "Borrowed"));
        appendTableLine(report);

        for (Book book : books) {
            int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
            report.append(String.format("  %-11s %-25s %8d %10d %9d%n",
                    safeText(book.getISBN(), 11),
                    safeText(book.getTitle(), 25),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    borrowedCopies));
        }

        appendTableLine(report);
        report.append("\n");
    }
    private void appendOverdueRecordsSection(StringBuilder report, ArrayList<BorrowRecord> records) {
        appendHeader(report, "OVERDUE BORROW RECORDS");

        ArrayList<BorrowRecord> overdueRecords = getOverdueRecords(records);

        if (overdueRecords.isEmpty()) {
            report.append("  No overdue borrow records.\n\n");
            return;
        }

        appendTableLine(report);
        report.append(String.format("  %-8s %-24s %-24s %-12s %9s%n", "Record", "Borrower", "Book", "Due Date", "Late"));
        appendTableLine(report);

        LocalDate today = LocalDate.now();
        for (BorrowRecord record : overdueRecords) {
            long lateDays = Math.max(0, today.toEpochDay() - record.getExpectedReturnDate().toEpochDay());
            report.append(String.format("  %-8s %-24s %-24s %-12s %8sd%n",
                    safeText(record.getRecordId(), 8),
                    safeText(record.getBorrower().getName(), 24),
                    safeText(record.getBook().getTitle(), 24),
                    record.getExpectedReturnDate(),
                    lateDays));
        }

        appendTableLine(report);
        report.append("\n");
    }
    private void appendBorrowRecordsSection(StringBuilder report, ArrayList<BorrowRecord> records) {
        appendHeader(report, "BORROW RECORDS");

        if (records.isEmpty()) {
            report.append("  No borrow records yet.\n\n");
            return;
        }

        ArrayList<BorrowRecord> sortedRecords = new ArrayList<>(records);
        sortedRecords.sort((record1, record2) -> record1.getRecordId().compareToIgnoreCase(record2.getRecordId()));

        appendTableLine(report);
        report.append(String.format("  %-8s %-22s %-23s %-12s %-12s %-8s%n",
                "Record", "Borrower", "Book", "Borrowed", "Due", "Status"));
        appendTableLine(report);

        for (BorrowRecord record : sortedRecords) {
            String status = record.isReturned() ? "Returned" : "Active";
            report.append(String.format("  %-8s %-22s %-23s %-12s %-12s %-8s%n",
                    safeText(record.getRecordId(), 8),
                    safeText(record.getBorrower().getName(), 22),
                    safeText(record.getBook().getTitle(), 23),
                    record.getBorrowDate(),
                    record.getExpectedReturnDate(),
                    status));
        }

        appendTableLine(report);
        report.append("\n");
    }
    private void appendMetric(StringBuilder report, String label, int value) {
        report.append(String.format("  %-30s : %s%n", label, value));
    }

    private void appendTableLine(StringBuilder report) {
        report.append("  ").append("-".repeat(REPORT_WIDTH - 4)).append("\n");
    }

    private String safeText(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        String clean = text.replace('\n', ' ').replace('\r', ' ').trim();
        if (clean.length() <= maxLength) {
            return clean;
        }

        if (maxLength <= 3) {
            return clean.substring(0, maxLength);
        }

        return clean.substring(0, maxLength - 3) + "...";
    }

    private void appendMainHeader(StringBuilder report, String title) {
        String line = "=".repeat(REPORT_WIDTH);
        report.append(line).append("\n");
        report.append(center(title, REPORT_WIDTH)).append("\n");
        report.append(line).append("\n\n");
    }

    private String center(String text, int width) {
        if (text == null) {
            text = "";
        }

        if (text.length() >= width) {
            return text;
        }

        int left = (width - text.length()) / 2;
        int right = width - text.length() - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private void appendHeader(StringBuilder report, String title) {
        report.append("-- ").append(title).append(" ");
        int rest = Math.max(0, REPORT_WIDTH - title.length() - 4);
        report.append("-".repeat(rest)).append("\n");
    }
    private void appendFooter(StringBuilder report) {
        String line = "=".repeat(REPORT_WIDTH);
        report.append(line).append("\n");
        report.append(center("END OF REPORT", REPORT_WIDTH)).append("\n");
        report.append(line).append("\n");
    }
}