package api;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import dao.BookDAO;
import model.Book;

public class LibraryApiServer {
    
    private static final int PORT = 8080;
    private static final String API_CONTEXT = "/api";
    private static final Gson gson = new Gson();
    
    public static void main(String[] args) {
        // Initialize BookDAO
        BookDAO bookDAO = new BookDAO();
        
        // Configure Spark
        port(PORT);
        
        // Enable CORS for frontend development
        enableCORS();
        
        // Define API endpoints
        path(API_CONTEXT, () -> {
            // Get all books
            get("/books", (req, res) -> {
                try {
                    res.type("application/json");
                    List<Book> books = bookDAO.viewAllBooks();
                    return gson.toJson(books);
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return gson.toJson(createErrorResponse("Failed to retrieve books: " + e.getMessage()));
                }
            });
            
            // Get a specific book by ID
            get("/books/:id", (req, res) -> {
                try {
                    res.type("application/json");
                    int id = Integer.parseInt(req.params(":id"));
                    Book book = bookDAO.getBookById(id);
                    
                    if (book != null) {
                        return gson.toJson(book);
                    } else {
                        res.status(404);
                        return gson.toJson(createErrorResponse("Book not found with ID: " + id));
                    }
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson(createErrorResponse("Invalid book ID format"));
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return gson.toJson(createErrorResponse("Failed to retrieve book: " + e.getMessage()));
                }
            });
            
            // Search for books
            get("/books/search", (req, res) -> {
                try {
                    res.type("application/json");
                    String query = req.queryParams("query");
                    
                    if (query == null || query.trim().isEmpty()) {
                        res.status(400);
                        return gson.toJson(createErrorResponse("Search query is required"));
                    }
                    
                    List<Book> books = bookDAO.searchBook(query);
                    return gson.toJson(books);
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return gson.toJson(createErrorResponse("Failed to search books: " + e.getMessage()));
                }
            });
            
            // Add a new book
            post("/books", (req, res) -> {
                try {
                    res.type("application/json");
                    Book book = gson.fromJson(req.body(), Book.class);
                    
                    if (book.getName() == null || book.getAuthor() == null ||
                        book.getName().trim().isEmpty() || book.getAuthor().trim().isEmpty()) {
                        res.status(400);
                        return gson.toJson(createErrorResponse("Book name and author are required"));
                    }
                    
                    bookDAO.addBook(book.getName(), book.getAuthor());
                    
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Book added successfully");
                    
                    res.status(201); // Created
                    return gson.toJson(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return gson.toJson(createErrorResponse("Failed to add book: " + e.getMessage()));
                }
            });
            
            // Update a book
            put("/books/:id", (req, res) -> {
                try {
                    res.type("application/json");
                    int id = Integer.parseInt(req.params(":id"));
                    Book book = gson.fromJson(req.body(), Book.class);
                    
                    if (book.getName() == null || book.getAuthor() == null ||
                        book.getName().trim().isEmpty() || book.getAuthor().trim().isEmpty()) {
                        res.status(400);
                        return gson.toJson(createErrorResponse("Book name and author are required"));
                    }
                    
                    // Check if book exists
                    Book existingBook = bookDAO.getBookById(id);
                    if (existingBook == null) {
                        res.status(404);
                        return gson.toJson(createErrorResponse("Book not found with ID: " + id));
                    }
                    
                    bookDAO.updateBook(id, book.getName(), book.getAuthor());
                    
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Book updated successfully");
                    
                    return gson.toJson(response);
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson(createErrorResponse("Invalid book ID format"));
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return gson.toJson(createErrorResponse("Failed to update book: " + e.getMessage()));
                }
            });
            
            // Delete a book
            delete("/books/:id", (req, res) -> {
                try {
                    res.type("application/json");
                    int id = Integer.parseInt(req.params(":id"));
                    
                    // Check if book exists
                    Book existingBook = bookDAO.getBookById(id);
                    if (existingBook == null) {
                        res.status(404);
                        return gson.toJson(createErrorResponse("Book not found with ID: " + id));
                    }
                    
                    bookDAO.deleteBook(id);
                    
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Book deleted successfully");
                    
                    return gson.toJson(response);
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson(createErrorResponse("Invalid book ID format"));
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return gson.toJson(createErrorResponse("Failed to delete book: " + e.getMessage()));
                }
            });
        });
        
        System.out.println("Server started on port " + PORT);
        System.out.println("API endpoints available at http://localhost:" + PORT + API_CONTEXT);
    }
    
    // Enable CORS for development
    private static void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            // Note: this may need to be extended based on your specific requirements
        });
    }
    
    // Helper method to create error response
    private static Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }
}