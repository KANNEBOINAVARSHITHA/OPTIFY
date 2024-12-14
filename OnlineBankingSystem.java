import java.sql.*;
import java.util.Scanner;

public class OnlineBankingSystem {

    // Database connection details
    static final String DB_URL = "jdbc:mysql://localhost:3306/online_banking";
    static final String USER = "root";
    static final String PASS = "password";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Welcome to the Online Banking System");

            while (true) {
                System.out.println("\nMenu:");
                System.out.println("1. Create Account");
                System.out.println("2. View Account Details");
                System.out.println("3. Deposit Money");
                System.out.println("4. Withdraw Money");
                System.out.println("5. Transfer Money");
                System.out.println("6. View Transaction History");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        createAccount(conn, scanner);
                        break;
                    case 2:
                        viewAccountDetails(conn, scanner);
                        break;
                    case 3:
                        depositMoney(conn, scanner);
                        break;
                    case 4:
                        withdrawMoney(conn, scanner);
                        break;
                    case 5:
                        transferMoney(conn, scanner);
                        break;
                    case 6:
                        viewTransactionHistory(conn, scanner);
                        break;
                    case 7:
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

    private static void createAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Account Holder Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Initial Deposit: ");
        double balance = scanner.nextDouble();

        String query = "INSERT INTO accounts (name, balance) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, balance);
            pstmt.executeUpdate();
            System.out.println("Account created successfully!");
        }
    }

    private static void viewAccountDetails(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Account ID: ");
        int accountId = scanner.nextInt();

        String query = "SELECT * FROM accounts WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Account ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Balance: " + rs.getDouble("balance"));
            } else {
                System.out.println("Account not found.");
            }
        }
    }

    private static void depositMoney(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Account ID: ");
        int accountId = scanner.nextInt();

        System.out.print("Enter Amount to Deposit: ");
        double amount = scanner.nextDouble();

        String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        String insertTransaction = "INSERT INTO transactions (account_id, type, amount) VALUES (?, 'DEPOSIT', ?)";

        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
             PreparedStatement transactionStmt = conn.prepareStatement(insertTransaction)) {
            updateStmt.setDouble(1, amount);
            updateStmt.setInt(2, accountId);
            updateStmt.executeUpdate();

            transactionStmt.setInt(1, accountId);
            transactionStmt.setDouble(2, amount);
            transactionStmt.executeUpdate();

            System.out.println("Money deposited successfully!");
        }
    }

    private static void withdrawMoney(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Account ID: ");
        int accountId = scanner.nextInt();

        System.out.print("Enter Amount to Withdraw: ");
        double amount = scanner.nextDouble();

        String checkBalanceQuery = "SELECT balance FROM accounts WHERE id = ?";
        String updateQuery = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        String insertTransaction = "INSERT INTO transactions (account_id, type, amount) VALUES (?, 'WITHDRAW', ?)";

        try (PreparedStatement checkBalanceStmt = conn.prepareStatement(checkBalanceQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
             PreparedStatement transactionStmt = conn.prepareStatement(insertTransaction)) {

            checkBalanceStmt.setInt(1, accountId);
            ResultSet rs = checkBalanceStmt.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amount) {
                updateStmt.setDouble(1, amount);
                updateStmt.setInt(2, accountId);
                updateStmt.executeUpdate();

                transactionStmt.setInt(1, accountId);
                transactionStmt.setDouble(2, amount);
                transactionStmt.executeUpdate();

                System.out.println("Money withdrawn successfully!");
            } else {
                System.out.println("Insufficient funds or account not found.");
            }
        }
    }

    private static void transferMoney(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Source Account ID: ");
        int sourceAccountId = scanner.nextInt();

        System.out.print("Enter Target Account ID: ");
        int targetAccountId = scanner.nextInt();

        System.out.print("Enter Amount to Transfer: ");
        double amount = scanner.nextDouble();

        conn.setAutoCommit(false);
        String withdrawQuery = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        String depositQuery = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        String insertTransaction = "INSERT INTO transactions (account_id, type, amount, target_account) VALUES (?, 'TRANSFER', ?, ?)";

        try (PreparedStatement withdrawStmt = conn.prepareStatement(withdrawQuery);
             PreparedStatement depositStmt = conn.prepareStatement(depositQuery);
             PreparedStatement transactionStmt = conn.prepareStatement(insertTransaction)) {

            withdrawStmt.setDouble(1, amount);
            withdrawStmt.setInt(2, sourceAccountId);
            withdrawStmt.executeUpdate();

            depositStmt.setDouble(1, amount);
            depositStmt.setInt(2, targetAccountId);
            depositStmt.executeUpdate();

            transactionStmt.setInt(1, sourceAccountId);
            transactionStmt.setDouble(2, amount);
            transactionStmt.setInt(3, targetAccountId);
            transactionStmt.executeUpdate();

            conn.commit();
            System.out.println("Money transferred successfully!");
        } catch (SQLException e) {
            conn.rollback();
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void viewTransactionHistory(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Account ID: ");
        int accountId = scanner.nextInt();

        String query = "SELECT * FROM transactions WHERE account_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            System.out.printf("%10s %20s %10s %10s\n", "Transaction ID", "Type", "Amount", "Target Account");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                int transactionId = rs.getInt("id");
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                int targetAccount = rs.getInt("target_account");
                System.out.printf("%10d %20s %10.2f %10d\n", transactionId, type, amount, targetAccount);
            }

        }
    }
}