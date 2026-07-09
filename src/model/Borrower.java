package model;

public class Borrower {
    private String id;
    private String name;
    private boolean graduatingStudent;
    private int activeBorrowCount;

    public Borrower() {
        this.activeBorrowCount = 0;
    }

    public Borrower(String id, String name, boolean graduatingStudent) {
        setId(id);
        setName(name);
        this.graduatingStudent = graduatingStudent;
        this.activeBorrowCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Borrower ID cannot be empty.");
        }

        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Borrower name cannot be empty.");
        }

        this.name = name;
    }

    public boolean isGraduatingStudent() {
        return graduatingStudent;
    }

    public void setGraduatingStudent(boolean graduatingStudent) {
        this.graduatingStudent = graduatingStudent;
    }

    public int getActiveBorrowCount() {
        return activeBorrowCount;
    }

    public void increaseActiveBorrowCount() {
        activeBorrowCount++;
    }

    public void decreaseActiveBorrowCount() {
        if (activeBorrowCount > 0) {
            activeBorrowCount--;
        }
    }
}