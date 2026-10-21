import java.sql.*;
import java.util.Scanner;

public class BikeRentalSystem {

    static final String URL = "jdbc:mysql://localhost:3306/bike_rental";
    static final String USER = "root";
    static final String PASS = "";

    static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("Connected to database successfully!");
            while (true) {
                printMenu();
                int choice = readInt("Enter choice: ");
                try {
                    switch (choice) {
                        case 1 -> addBike(con);
                        case 2 -> viewBikes(con, false);
                        case 3 -> viewBikes(con, true);
                        case 4 -> addCustomer(con);
                        case 5 -> viewCustomers(con);
                        case 6 -> rentBike(con);
                        case 7 -> returnBike(con);
                        case 8 -> viewRentals(con);
                        case 0 -> {
                            System.out.println("Thank you! Goodbye.");
                            return;
                        }
                        default -> System.out.println("Invalid choice, try again.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not connect to database: " + e.getMessage());
            System.out.println("Check: XAMPP MySQL is running, database 'bike_rental' exists, connector JAR is added.");
        }
    }

    static void printMenu() {
        System.out.println("\n===== BIKE RENTAL SYSTEM =====");
        System.out.println("1. Add Bike");
        System.out.println("2. View All Bikes");
        System.out.println("3. View Available Bikes");
        System.out.println("4. Add Customer");
        System.out.println("5. View Customers");
        System.out.println("6. Rent a Bike");
        System.out.println("7. Return a Bike");
        System.out.println("8. View Rental History");
        System.out.println("0. Exit");
    }

    static void addBike(Connection con) throws SQLException {
        System.out.print("Model: ");
        String model = sc.nextLine().trim();
        System.out.print("Type (Scooter/Sports/Cruiser/Electric): ");
        String type = sc.nextLine().trim();
        double rate = readDouble("Rate per hour: ");

        String sql = "INSERT INTO bikes (model, bike_type, rate_per_hour) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, model);
            ps.setString(2, type);
            ps.setDouble(3, rate);
            ps.executeUpdate();
            System.out.println("Bike added successfully!");
        }
    }

    static void viewBikes(Connection con, boolean onlyAvailable) throws SQLException {
        String sql = "SELECT * FROM bikes" + (onlyAvailable ? " WHERE status='Available'" : "");
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%n%-5s %-28s %-12s %-10s %-10s%n", "ID", "Model", "Type", "Rate/hr", "Status");
            System.out.println("-------------------------------------------------------------------");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("%-5d %-28s %-12s %-10.2f %-10s%n",
                        rs.getInt("bike_id"), rs.getString("model"), rs.getString("bike_type"),
                        rs.getDouble("rate_per_hour"), rs.getString("status"));
            }
            if (!any)
                System.out.println("No bikes found.");
        }
    }

    static void addCustomer(Connection con) throws SQLException {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Phone: ");
        String phone = sc.nextLine().trim();
        System.out.print("Driving License No: ");
        String lic = sc.nextLine().trim();

        String sql = "INSERT INTO customers (name, phone, license_no) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, lic);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    System.out.println("Customer added! Customer ID = " + keys.getInt(1));
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("A customer with this license number already exists.");
        }
    }

    static void viewCustomers(Connection con) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM customers")) {
            System.out.printf("%n%-5s %-20s %-14s %-20s%n", "ID", "Name", "Phone", "License");
            System.out.println("---------------------------------------------------------");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("%-5d %-20s %-14s %-20s%n",
                        rs.getInt("customer_id"), rs.getString("name"),
                        rs.getString("phone"), rs.getString("license_no"));
            }
            if (!any)
                System.out.println("No customers found.");
        }
    }

    static void rentBike(Connection con) throws SQLException {
        viewBikes(con, true);
        int bikeId = readInt("Enter Bike ID to rent: ");
        int custId = readInt("Enter Customer ID: ");

        try (PreparedStatement ps = con.prepareStatement("SELECT status FROM bikes WHERE bike_id=?")) {
            ps.setInt(1, bikeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Bike not found.");
                    return;
                }
                if (!rs.getString("status").equals("Available")) {
                    System.out.println("Sorry, this bike is already rented.");
                    return;
                }
            }
        }

        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM customers WHERE customer_id=?")) {
            ps.setInt(1, custId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Customer not found.");
                    return;
                }
            }
        }

        con.setAutoCommit(false);
        try (PreparedStatement ins = con.prepareStatement(
                "INSERT INTO rentals (bike_id, customer_id, rent_time) VALUES (?, ?, NOW())",
                Statement.RETURN_GENERATED_KEYS);
                PreparedStatement upd = con.prepareStatement(
                        "UPDATE bikes SET status='Rented' WHERE bike_id=?")) {
            ins.setInt(1, bikeId);
            ins.setInt(2, custId);
            ins.executeUpdate();
            upd.setInt(1, bikeId);
            upd.executeUpdate();
            con.commit();
            try (ResultSet keys = ins.getGeneratedKeys()) {
                if (keys.next())
                    System.out.println("Bike rented! Your Rental ID = " + keys.getInt(1));
            }
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    static void returnBike(Connection con) throws SQLException {
        int rentalId = readInt("Enter Rental ID: ");

        String sql = "SELECT r.bike_id, r.rent_time, b.rate_per_hour, " +
                "TIMESTAMPDIFF(MINUTE, r.rent_time, NOW()) AS mins " +
                "FROM rentals r JOIN bikes b ON r.bike_id = b.bike_id " +
                "WHERE r.rental_id=? AND r.status='Active'";
        int bikeId;
        long mins;
        double rate;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No active rental found with this ID.");
                    return;
                }
                bikeId = rs.getInt("bike_id");
                mins = rs.getLong("mins");
                rate = rs.getDouble("rate_per_hour");
            }
        }

        long hours = Math.max(1, (long) Math.ceil(mins / 60.0));
        double total = hours * rate;

        con.setAutoCommit(false);
        try (PreparedStatement upR = con.prepareStatement(
                "UPDATE rentals SET return_time=NOW(), total_amount=?, status='Returned' WHERE rental_id=?");
                PreparedStatement upB = con.prepareStatement(
                        "UPDATE bikes SET status='Available' WHERE bike_id=?")) {
            upR.setDouble(1, total);
            upR.setInt(2, rentalId);
            upR.executeUpdate();
            upB.setInt(1, bikeId);
            upB.executeUpdate();
            con.commit();
            System.out.println("\n----- BILL -----");
            System.out.println("Rental ID : " + rentalId);
            System.out.println("Duration  : " + mins + " min (billed " + hours + " hr)");
            System.out.println("Rate/hr   : " + rate);
            System.out.println("TOTAL     : Rs. " + total);
            System.out.println("Bike returned successfully!");
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    static void viewRentals(Connection con) throws SQLException {
        String sql = "SELECT r.rental_id, c.name, b.model, r.rent_time, r.return_time, r.total_amount, r.status " +
                "FROM rentals r JOIN customers c ON r.customer_id=c.customer_id " +
                "JOIN bikes b ON r.bike_id=b.bike_id ORDER BY r.rental_id DESC";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%n%-5s %-15s %-26s %-20s %-20s %-9s %-9s%n",
                    "ID", "Customer", "Bike", "Rent Time", "Return Time", "Amount", "Status");
            System.out.println(
                    "-----------------------------------------------------------------------------------------------------------");
            boolean any = false;
            while (rs.next()) {
                any = true;
                Timestamp rt = rs.getTimestamp("return_time");
                System.out.printf("%-5d %-15s %-26s %-20s %-20s %-9.2f %-9s%n",
                        rs.getInt("rental_id"), rs.getString("name"), rs.getString("model"),
                        rs.getTimestamp("rent_time"), rt == null ? "-" : rt.toString(),
                        rs.getDouble("total_amount"), rs.getString("status"));
            }
            if (!any)
                System.out.println("No rentals yet.");
        }
    }

    static int readInt(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static double readDouble(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
}