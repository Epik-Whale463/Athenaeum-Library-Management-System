package dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DBConnection;
import model.Book;

public class BookDAO {

    // Add a new book
    public void addBook(String name, String author) {
        String query = "INSERT INTO books (name, author) VALUES (?, ?)";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, author);
            stmt.executeUpdate();
            System.out.println("Book added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View all books
    public List<Book> viewAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books";

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String author = rs.getString("author");
                books.add(new Book(id, name, author));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    // Update book
    public void updateBook(int id, String newName, String newAuthor) {
        String query = "UPDATE books SET name = ?, author = ? WHERE id = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setString(2, newAuthor);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            System.out.println("Book updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete book
    public void deleteBook(int id) {
        String query = "DELETE FROM books WHERE id = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Book deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Search book by name or author
    public List<Book> searchBook(String searchQuery) {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE name LIKE ? OR author LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, "%" + searchQuery + "%");
            stmt.setString(2, "%" + searchQuery + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String author = rs.getString("author");
                books.add(new Book(id, name, author));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }
    
    // Get book by ID
    public Book getBookById(int id) {
        String query = "SELECT * FROM books WHERE id = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("name");
                String author = rs.getString("author");
                return new Book(id, name, author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
