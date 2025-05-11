package model;

public class Book {
    private int id;
    private String name;
    private String author;

    // Constructor
    public Book(int id, String name, String author) {
        this.id = id;
        this.name = name;
        this.author = author;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    // Override toString() method to display Book details
    @Override
    public String toString() {
        return "Book [ID=" + id + ", Name=" + name + ", Author=" + author + "]";
    }
}

