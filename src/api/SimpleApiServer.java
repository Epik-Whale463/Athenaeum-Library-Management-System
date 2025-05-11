package api;

import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.net.httpserver.*;
import dao.BookDAO;
import model.Book;

/**
 * A simple HTTP server for handling REST API requests
 * using Java's built-in HttpServer instead of Spark
 */
public class SimpleApiServer {
    
    private static final int PORT = 8080;
    
    public static void main(String[] args) throws IOException {
        // Create an HttpServer instance
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Initialize BookDAO
        BookDAO bookDAO = new BookDAO();
        
        // Create context for book endpoints
        server.createContext("/api/books", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String query = exchange.getRequestURI().getQuery();
                String response = "";
                int statusCode = 200;
                
                // Set CORS headers for all responses
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                // Handle OPTIONS request for CORS preflight
                if (method.equals("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1); // No content status code
                    exchange.close();
                    return;
                }
                
                try {
                    // GET requests for books
                    if (method.equals("GET")) {
                        // Handle search query - fixed to match frontend
                        if (query != null && query.startsWith("query=")) {
                            String searchTerm = query.substring(6); // Remove "query=" prefix
                            searchTerm = URLDecoder.decode(searchTerm, "UTF-8");
                            List<Book> books = bookDAO.searchBook(searchTerm);
                            response = convertBooksToJson(books);
                        } 
                        // Get all books
                        else {
                            List<Book> books = bookDAO.viewAllBooks();
                            response = convertBooksToJson(books);
                        }
                    }
                    // POST request to add a new book
                    else if (method.equals("POST")) {
                        // Read request body
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody()));
                        StringBuilder requestBody = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            requestBody.append(line);
                        }
                        reader.close();
                        
                        // Parse JSON
                        String json = requestBody.toString();
                        Map<String, String> bookData = parseJson(json);
                        
                        if (bookData.get("name") == null || bookData.get("author") == null) {
                            response = "{\"status\": \"error\", \"message\": \"Book name and author are required\"}";
                            statusCode = 400; // Bad Request
                        } else {
                            // Add book to database
                            bookDAO.addBook(bookData.get("name"), bookData.get("author"));
                            
                            // Return success response
                            response = "{\"status\": \"success\", \"message\": \"Book added successfully\"}";
                            statusCode = 201; // Created
                        }
                    }
                    // Unsupported method
                    else {
                        response = "{\"status\": \"error\", \"message\": \"Method not supported\"}";
                        statusCode = 405; // Method Not Allowed
                    }
                } catch (Exception e) {
                    response = "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}";
                    statusCode = 500; // Internal Server Error
                }
                
                // Set response headers
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                
                // Send the response
                byte[] responseBytes = response.getBytes("UTF-8");
                exchange.sendResponseHeaders(statusCode, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            }
        });
        
        // Create context for specific book operations (GET, PUT, DELETE by ID)
        server.createContext("/api/books/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String response = "";
                int statusCode = 200;
                
                // Set CORS headers
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                // Handle OPTIONS request for CORS preflight
                if (method.equals("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1); // No content status code
                    exchange.close();
                    return;
                }
                
                // Extract ID from path /api/books/{id}
                String idStr = path.substring(path.lastIndexOf("/") + 1);
                
                try {
                    int id = Integer.parseInt(idStr);
                    
                    // GET request for a specific book
                    if (method.equals("GET")) {
                        Book book = bookDAO.getBookById(id);
                        if (book != null) {
                            response = convertBookToJson(book);
                        } else {
                            response = "{\"status\": \"error\", \"message\": \"Book not found\"}";
                            statusCode = 404; // Not Found
                        }
                    } 
                    // PUT request to update a book
                    else if (method.equals("PUT")) {
                        // Read request body
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody()));
                        StringBuilder requestBody = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            requestBody.append(line);
                        }
                        reader.close();
                        
                        // Parse JSON
                        String json = requestBody.toString();
                        Map<String, String> bookData = parseJson(json);
                        
                        // Validate input
                        if (bookData.get("name") == null || bookData.get("author") == null) {
                            response = "{\"status\": \"error\", \"message\": \"Book name and author are required\"}";
                            statusCode = 400; // Bad Request
                        } else {
                            // Check if book exists
                            Book existingBook = bookDAO.getBookById(id);
                            if (existingBook == null) {
                                response = "{\"status\": \"error\", \"message\": \"Book not found\"}";
                                statusCode = 404; // Not Found
                            } else {
                                // Update the book
                                bookDAO.updateBook(id, bookData.get("name"), bookData.get("author"));
                                response = "{\"status\": \"success\", \"message\": \"Book updated successfully\"}";
                            }
                        }
                    } 
                    // DELETE request to remove a book
                    else if (method.equals("DELETE")) {
                        // Check if book exists
                        Book existingBook = bookDAO.getBookById(id);
                        if (existingBook == null) {
                            response = "{\"status\": \"error\", \"message\": \"Book not found\"}";
                            statusCode = 404; // Not Found
                        } else {
                            // Delete the book
                            bookDAO.deleteBook(id);
                            response = "{\"status\": \"success\", \"message\": \"Book deleted successfully\"}";
                        }
                    }
                    // Unsupported method
                    else {
                        response = "{\"status\": \"error\", \"message\": \"Method not supported\"}";
                        statusCode = 405; // Method Not Allowed
                    }
                } catch (NumberFormatException e) {
                    response = "{\"status\": \"error\", \"message\": \"Invalid ID format\"}";
                    statusCode = 400; // Bad Request
                } catch (Exception e) {
                    response = "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}";
                    statusCode = 500; // Internal Server Error
                }
                
                // Set response headers
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                
                // Send the response
                byte[] responseBytes = response.getBytes("UTF-8");
                exchange.sendResponseHeaders(statusCode, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            }
        });
        
        // Handle root API context
        server.createContext("/api", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Set CORS headers
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                if (exchange.getRequestMethod().equals("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }
                
                String response = "{\"status\": \"success\", \"message\": \"API server is running\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                
                byte[] responseBytes = response.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            }
        });
        
        // Start the server
        server.start();
        System.out.println("Server started on port " + PORT);
        System.out.println("API endpoints available at http://localhost:" + PORT + "/api");
        System.out.println("Press Ctrl+C to stop the server");
    }
    
    // Utility method to convert a list of books to JSON
    private static String convertBooksToJson(List<Book> books) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < books.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(convertBookToJson(books.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    // Utility method to convert a single book to JSON
    private static String convertBookToJson(Book book) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"author\":\"%s\"}", 
            book.getId(), 
            escapeJson(book.getName()), 
            escapeJson(book.getAuthor())
        );
    }
    
    // Utility method to escape JSON strings
    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    
    // Simple JSON parser for books
    private static Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();
        
        // Extract name
        int nameStart = json.indexOf("\"name\"");
        if (nameStart >= 0) {
            nameStart = json.indexOf(":", nameStart) + 1;
            nameStart = json.indexOf("\"", nameStart) + 1;
            int nameEnd = json.indexOf("\"", nameStart);
            result.put("name", json.substring(nameStart, nameEnd));
        }
        
        // Extract author
        int authorStart = json.indexOf("\"author\"");
        if (authorStart >= 0) {
            authorStart = json.indexOf(":", authorStart) + 1;
            authorStart = json.indexOf("\"", authorStart) + 1;
            int authorEnd = json.indexOf("\"", authorStart);
            result.put("author", json.substring(authorStart, authorEnd));
        }
        
        return result;
    }
}