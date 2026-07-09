package data;

import model.*;
import service.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DataPersistence {

    private enum Section {
        NONE,
        BOOKS,
        BORROWERS,
        BORROW_RECORDS,
        WAIT_LIST
    }

    public boolean saveToFile(LibraryManager library, String fileName) {
        if (library == null || fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName), StandardCharsets.UTF_8)) {
            writer.write("DIGITAL_LIBRARY_DATA_V1");
            writer.newLine();
            writer.newLine();

            writeBooks(writer, library);
            writeBorrowers(writer, library);
            writeBorrowRecords(writer, library);
            writeWaitList(writer, library);

            return true;
        } catch (IOException e) {
            System.err.println("Failed to save library data: " + e.getMessage());
            return false;
        }
    }

    public boolean loadFromFile(LibraryManager library, String fileName) {
        if (library == null || fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        LibraryManager loadedLibrary = new LibraryManager();
        ArrayList<String[]> bookRows = new ArrayList<>();
        ArrayList<String[]> borrowerRows = new ArrayList<>();
        ArrayList<String[]> recordRows = new ArrayList<>();
        ArrayList<String[]> waitRows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName), StandardCharsets.UTF_8)) {
            Section section = Section.NONE;
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#") || line.equals("DIGITAL_LIBRARY_DATA_V1")) {
                    continue;
                }

                if (line.equals("[BOOKS]")) {
                    section = Section.BOOKS;
                    continue;
                }
                if (line.equals("[BORROWERS]")) {
                    section = Section.BORROWERS;
                    continue;
                }
                if (line.equals("[BORROW_RECORDS]")) {
                    section = Section.BORROW_RECORDS;
                    continue;
                }
                if (line.equals("[WAIT_LIST]")) {
                    section = Section.WAIT_LIST;
                    continue;
                }

                String[] parts = splitEscaped(line);

                switch (section) {
                    case BOOKS:
                        bookRows.add(parts);
                        break;
                    case BORROWERS:
                        borrowerRows.add(parts);
                        break;
                    case BORROW_RECORDS:
                        recordRows.add(parts);
                        break;
                    case WAIT_LIST:
                        waitRows.add(parts);
                        break;
                    default:
                        throw new IllegalArgumentException("Data line found before any section: " + line);
                }
            }

            importBooks(loadedLibrary, bookRows);
            importBorrowers(loadedLibrary, borrowerRows);
            importBorrowRecords(loadedLibrary, recordRows);
            importWaitList(loadedLibrary, waitRows);
            rebuildAuthorReadingTimes(loadedLibrary);

            library.replaceDataFrom(loadedLibrary);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to load library data: " + e.getMessage());
            return false;
        }
    }

    private void writeBooks(BufferedWriter writer, LibraryManager library) throws IOException {
        writer.write("[BOOKS]");
        writer.newLine();

        for (Book book : library.getBooksInInsertionOrder()) {
            writer.write(join(
                    book.getISBN(),
                    book.getTitle(),
                    book.getAuthorName(),
                    String.valueOf(book.getTotalCopies()),
                    String.valueOf(book.getAvailableCopies()),
                    String.valueOf(book.getBorrowCount())
            ));
            writer.newLine();
        }

        writer.newLine();
    }

    private void writeBorrowers(BufferedWriter writer, LibraryManager library) throws IOException {
        writer.write("[BORROWERS]");
        writer.newLine();

        for (Borrower borrower : library.getBorrowers()) {
            writer.write(join(
                    borrower.getId(),
                    borrower.getName(),
                    String.valueOf(borrower.isGraduatingStudent())
            ));
            writer.newLine();
        }

        writer.newLine();
    }

    private void writeBorrowRecords(BufferedWriter writer, LibraryManager library) throws IOException {
        writer.write("[BORROW_RECORDS]");
        writer.newLine();

        ArrayList<BorrowRecord> records = library.getAllBorrowRecords();
        records.sort((r1, r2) -> r1.getRecordId().compareToIgnoreCase(r2.getRecordId()));

        for (BorrowRecord record : records) {
            writer.write(join(
                    record.getRecordId(),
                    record.getBorrower().getId(),
                    record.getBook().getISBN(),
                    record.getBorrowDate().toString(),
                    record.getExpectedReturnDate().toString(),
                    String.valueOf(record.isReturned())
            ));
            writer.newLine();
        }

        writer.newLine();
    }

    private void writeWaitList(BufferedWriter writer, LibraryManager library) throws IOException {
        writer.write("[WAIT_LIST]");
        writer.newLine();

        ArrayList<WaitListRequest> requests = new ArrayList<>();
        for (Book book : library.getBooks()) {
            requests.addAll(library.getWaitListForBook(book.getISBN()));
        }
        requests.sort(null);

        for (WaitListRequest request : requests) {
            writer.write(join(
                    request.getRequestId(),
                    request.getBorrower().getId(),
                    request.getBook().getISBN(),
                    request.getRequestTime().toString(),
                    String.valueOf(request.getSequenceNumber())
            ));
            writer.newLine();
        }

        writer.newLine();
    }

    private void importBooks(LibraryManager library, ArrayList<String[]> rows) {
        for (String[] row : rows) {
            requireColumns(row, 6, "BOOKS");

            String isbn = row[0];
            String title = row[1];
            String authorName = row[2];
            int totalCopies = Integer.parseInt(row[3]);
            int availableCopies = Integer.parseInt(row[4]);
            int borrowCount = Integer.parseInt(row[5]);

            if (availableCopies < 0 || availableCopies > totalCopies || borrowCount < 0) {
                throw new IllegalArgumentException("Invalid book counters for ISBN: " + isbn);
            }

            if (!library.addBook(isbn, title, authorName, totalCopies)) {
                throw new IllegalArgumentException("Cannot import book: " + isbn);
            }

            Book book = library.searchBookByISBN(isbn);
            book.setAvailableCopies(availableCopies);
            book.setBorrowCount(borrowCount);
        }
    }

    private void importBorrowers(LibraryManager library, ArrayList<String[]> rows) {
        for (String[] row : rows) {
            requireColumns(row, 3, "BORROWERS");

            String id = row[0];
            String name = row[1];
            boolean graduating = Boolean.parseBoolean(row[2]);

            if (!library.addBorrower(id, name, graduating)) {
                throw new IllegalArgumentException("Cannot import borrower: " + id);
            }
        }
    }

    private void importBorrowRecords(LibraryManager library, ArrayList<String[]> rows) {
        for (String[] row : rows) {
            requireColumns(row, 6, "BORROW_RECORDS");

            String recordId = row[0];
            Borrower borrower = library.searchBorrowerById(row[1]);
            Book book = library.searchBookByISBN(row[2]);
            LocalDate borrowDate = LocalDate.parse(row[3]);
            LocalDate expectedReturnDate = LocalDate.parse(row[4]);
            boolean returned = Boolean.parseBoolean(row[5]);

            if (borrower == null || book == null) {
                throw new IllegalArgumentException("Record references missing borrower or book: " + recordId);
            }

            BorrowRecord record = new BorrowRecord(recordId, borrower, book, borrowDate, expectedReturnDate);
            if (returned) {
                record.markAsReturned();
            }

            library.getBorrowingManager().addExistingRecord(record);
        }
    }

    private void importWaitList(LibraryManager library, ArrayList<String[]> rows) {
        for (String[] row : rows) {
            requireColumns(row, 5, "WAIT_LIST");

            String requestId = row[0];
            Borrower borrower = library.searchBorrowerById(row[1]);
            Book book = library.searchBookByISBN(row[2]);
            LocalDateTime requestTime = LocalDateTime.parse(row[3]);
            long sequenceNumber = Long.parseLong(row[4]);

            if (borrower == null || book == null) {
                throw new IllegalArgumentException("Wait request references missing borrower or book: " + requestId);
            }

            WaitListRequest request = new WaitListRequest(requestId, borrower, book, requestTime, sequenceNumber);
            library.getBorrowingManager().getWaitListManager().addExistingRequest(request);
        }
    }

    private void rebuildAuthorReadingTimes(LibraryManager library) {
        for (Author author : library.getAuthors()) {
            author.setReadingTimes(0);
        }

        for (Book book : library.getBooks()) {
            Author author = book.getAuthor();
            if (author != null) {
                author.setReadingTimes(author.getReadingTimes() + book.getBorrowCount());
            }
        }
    }

    private void requireColumns(String[] row, int count, String section) {
        if (row.length != count) {
            throw new IllegalArgumentException(section + " row must contain " + count + " values.");
        }
    }

    private String join(String... values) {
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append('|');
            }
            line.append(escape(values[i]));
        }

        return line.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("|", "\\p")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    private String[] splitEscaped(String line) {
        ArrayList<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaping = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (escaping) {
                if (ch == 'p') {
                    current.append('|');
                } else if (ch == 'n') {
                    current.append('\n');
                } else {
                    current.append(ch);
                }
                escaping = false;
                continue;
            }

            if (ch == '\\') {
                escaping = true;
                continue;
            }

            if (ch == '|') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (escaping) {
            current.append('\\');
        }

        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }
}