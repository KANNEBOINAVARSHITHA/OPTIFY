import java.sql.*;
import java.util.Scanner;

public class DigitalLibraryManagement {

    // Database connection details
    static final String DB_URL = "jdbc:mysql://localhost:3306/digital_library";
    static final String USER = "root";
    static final String PASS = "password";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Welcome to the Digital Library Management System");

            while (true) {
                System.out.println("\nMenu:");
                System.out.println("1. Add a Book");
                System.out.println("2. Issue a Book");
                System.out.println("3. Return a Book");
                System.out.println("4. View All Books");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addBook(conn, scanner);
                        break;
                    case 2:
                        issueBook(conn, scanner);
                        break;
                    case 3:
                        returnBook(conn, scanner);
                        break;
                    case 4:
                        viewAllBooks(conn);
                        break;
                    case 5:
                        System.out.println("Exiting the system. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Book ID: ");
        int bookId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();

        System.out.print("Enter Author Name: ");
        String author = scanner.nextLine();

        String query = "INSERT INTO books (id, title, author, issued) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();
            System.out.println("Book added successfully!");
        }
    }

    private static void issueBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Book ID to issue: ");
        int bookId = scanner.nextInt();

        String query = "UPDATE books SET issued = ? WHERE id = ? AND issued = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, true);
            pstmt.setInt(2, bookId);
            pstmt.setBoolean(3, false);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book issued successfully!");
            } else {
                System.out.println("Book not found or already issued.");
            }
        }
    }

    private static void returnBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Book ID to return: ");
        int bookId = scanner.nextInt();

        String query = "UPDATE books SET issued = ? WHERE id = ? AND issued = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, false);
            pstmt.setInt(2, bookId);
            pstmt.setBoolean(3, true);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book returned successfully!");
            } else {
                System.out.println("Book not found or not issued.");
            }
        }
    }

    private static void viewAllBooks(Connection conn) throws SQLException {
        String query = "SELECT * FROM books";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.printf("%10s %30s %30s %10s\n", "Book ID", "Title", "Author", "Issued");
            System.out.println("------------------------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                boolean issued = rs.getBoolean("issued");
                System.out.printf("%10d %30s %30s %10s\n", id, title, author, issued ? "Yes" : "No");
            }
        }
    }
}