package ui;

import java.util.Scanner;
import java.util.List;
import dao.BookDAO;
import model.Book;

public class LibraryApp {
    // ANSI color codes for terminal
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String BOLD = "\u001B[1m";
    
    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback if clearing doesn't work
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }
    
    private static void printHeader() {
        System.out.println(BLUE + BOLD + "╔══════════════════════════════════════╗" + RESET);
        System.out.println(BLUE + BOLD + "║      LIBRARY MANAGEMENT SYSTEM       ║" + RESET);
        System.out.println(BLUE + BOLD + "╚══════════════════════════════════════╝" + RESET);
    }
    
    private static void displayMenu() {
        System.out.println(CYAN + "\n┌─────────── MENU OPTIONS ───────────┐" + RESET);
        System.out.println(CYAN + "│ " + YELLOW + "1" + RESET + ". Add a New Book               " + CYAN + "│" + RESET);
        System.out.println(CYAN + "│ " + YELLOW + "2" + RESET + ". View All Books               " + CYAN + "│" + RESET);
        System.out.println(CYAN + "│ " + YELLOW + "3" + RESET + ". Update Book Details          " + CYAN + "│" + RESET);
        System.out.println(CYAN + "│ " + YELLOW + "4" + RESET + ". Delete a Book                " + CYAN + "│" + RESET);
        System.out.println(CYAN + "│ " + YELLOW + "5" + RESET + ". Search for Books             " + CYAN + "│" + RESET);
        System.out.println(CYAN + "│ " + YELLOW + "6" + RESET + ". Exit Application             " + CYAN + "│" + RESET);
        System.out.println(CYAN + "└────────────────────────────────────┘" + RESET);
    }

    private static void waitForEnter(Scanner scanner) {
        System.out.println(YELLOW + "\nPress Enter to continue..." + RESET);
        scanner.nextLine();
    }
    
    private static void displayBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println(RED + "No books found!" + RESET);
            return;
        }
        
        System.out.println(PURPLE + "┌───────┬────────────────────────────┬────────────────────────────┐" + RESET);
        System.out.println(PURPLE + "│  " + BOLD + "ID" + RESET + PURPLE + "   │        " + BOLD + "BOOK NAME" + RESET + PURPLE + "          │         " + BOLD + "AUTHOR" + RESET + PURPLE + "            │" + RESET);
        System.out.println(PURPLE + "├───────┼────────────────────────────┼────────────────────────────┤" + RESET);
        
        for (Book book : books) {
            System.out.printf(PURPLE + "│" + RESET + " %-5d " + PURPLE + "│" + RESET + " %-26s " + PURPLE + "│" + RESET + " %-26s " + PURPLE + "│\n" + RESET, 
                            book.getId(), 
                            truncateString(book.getName(), 26), 
                            truncateString(book.getAuthor(), 26));
        }
        
        System.out.println(PURPLE + "└───────┴────────────────────────────┴────────────────────────────┘" + RESET);
    }
    
    private static String truncateString(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BookDAO bookDAO = new BookDAO();
        
        while (true) {
            clearScreen();
            printHeader();
            displayMenu();

            System.out.print(GREEN + "\nEnter your choice [1-6]: " + RESET);
            
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println(RED + "Please enter a valid number!" + RESET);
                waitForEnter(scanner);
                continue;
            }

            switch (choice) {
                case 1:
                    clearScreen();
                    printHeader();
                    System.out.println(GREEN + BOLD + "\n=== ADD A NEW BOOK ===" + RESET);
                    System.out.print("Enter Book Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Author Name: ");
                    String author = scanner.nextLine();
                    bookDAO.addBook(name, author);
                    waitForEnter(scanner);
                    break;
                case 2:
                    clearScreen();
                    printHeader();
                    System.out.println(GREEN + BOLD + "\n=== VIEW ALL BOOKS ===" + RESET);
                    List<Book> allBooks = bookDAO.viewAllBooks();
                    displayBooks(allBooks);
                    waitForEnter(scanner);
                    break;
                case 3:
                    clearScreen();
                    printHeader();
                    System.out.println(GREEN + BOLD + "\n=== UPDATE BOOK DETAILS ===" + RESET);
                    
                    try {
                        System.out.print("Enter Book ID to Update: ");
                        int updateId = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter New Book Name: ");
                        String newName = scanner.nextLine();
                        System.out.print("Enter New Author Name: ");
                        String newAuthor = scanner.nextLine();
                        bookDAO.updateBook(updateId, newName, newAuthor);
                    } catch (NumberFormatException e) {
                        System.out.println(RED + "Invalid ID format. Please enter a valid number!" + RESET);
                    }
                    
                    waitForEnter(scanner);
                    break;
                case 4:
                    clearScreen();
                    printHeader();
                    System.out.println(GREEN + BOLD + "\n=== DELETE A BOOK ===" + RESET);
                    
                    try {
                        System.out.print("Enter Book ID to Delete: ");
                        int deleteId = Integer.parseInt(scanner.nextLine());
                        
                        System.out.print(RED + "Are you sure you want to delete this book? (y/n): " + RESET);
                        String confirm = scanner.nextLine().trim().toLowerCase();
                        
                        if (confirm.equals("y") || confirm.equals("yes")) {
                            bookDAO.deleteBook(deleteId);
                        } else {
                            System.out.println(YELLOW + "Deletion cancelled." + RESET);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(RED + "Invalid ID format. Please enter a valid number!" + RESET);
                    }
                    
                    waitForEnter(scanner);
                    break;
                case 5:
                    clearScreen();
                    printHeader();
                    System.out.println(GREEN + BOLD + "\n=== SEARCH FOR BOOKS ===" + RESET);
                    System.out.print("Enter Name or Author to Search: ");
                    String searchQuery = scanner.nextLine();
                    List<Book> searchResults = bookDAO.searchBook(searchQuery);
                    
                    System.out.println(YELLOW + "\nSearch Results for: \"" + searchQuery + "\"" + RESET);
                    displayBooks(searchResults);
                    
                    waitForEnter(scanner);
                    break;
                case 6:
                    clearScreen();
                    System.out.println(GREEN + BOLD + "Thank you for using Library Management System!" + RESET);
                    System.out.println(YELLOW + "Exiting program..." + RESET);
                    scanner.close();
                    return;
                default:
                    System.out.println(RED + "Invalid choice, please try again." + RESET);
                    waitForEnter(scanner);
            }
        }
    }
}
