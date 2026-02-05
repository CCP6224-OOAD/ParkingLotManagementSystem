package parkinglotmanagementsystem.dao;

import parkinglotmanagementsystem.util.Constants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class for managing database connections and initialization
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            // Establish connection
            connection = DriverManager.getConnection(Constants.DB_URL);
            System.out.println("Database connection established: " + Constants.DB_FILE);
            // Initialize database schema
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // Check if connection is closed and reconnect if necessary
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(Constants.DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get connection!");
            e.printStackTrace();
        }
        return connection;
    }

    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {

            // Enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Table 1: parking_spots
            String createSpotsTable = """
                        CREATE TABLE IF NOT EXISTS parking_spots (
                            spot_id TEXT PRIMARY KEY,
                            floor_number INTEGER NOT NULL,
                            row_number INTEGER NOT NULL,
                            spot_number INTEGER NOT NULL,
                            spot_type TEXT NOT NULL CHECK(spot_type IN ('COMPACT', 'REGULAR', 'HANDICAPPED', 'RESERVED')),
                            hourly_rate REAL NOT NULL,
                            is_occupied INTEGER DEFAULT 0 CHECK(is_occupied IN (0, 1)),
                            current_plate TEXT
                        );
                    """;
            stmt.execute(createSpotsTable);

            // Table 2: vehicles
            String createVehiclesTable = """
                        CREATE TABLE IF NOT EXISTS vehicles (
                            plate_number TEXT PRIMARY KEY,
                            vehicle_type TEXT NOT NULL CHECK(vehicle_type IN ('MOTORCYCLE', 'CAR', 'SUV', 'HANDICAPPED'))
                        );
                    """;
            stmt.execute(createVehiclesTable);

            // Table 3: tickets
            String createTicketsTable = """
                        CREATE TABLE IF NOT EXISTS tickets (
                            ticket_id TEXT PRIMARY KEY,
                            plate_number TEXT NOT NULL,
                            spot_id TEXT NOT NULL,
                            entry_time TEXT NOT NULL,
                            exit_time TEXT,
                            fine_scheme TEXT NOT NULL CHECK(fine_scheme IN ('FIXED', 'PROGRESSIVE', 'HOURLY')),
                            FOREIGN KEY(plate_number) REFERENCES vehicles(plate_number),
                            FOREIGN KEY(spot_id) REFERENCES parking_spots(spot_id)
                        );
                    """;
            stmt.execute(createTicketsTable);

            // Table 4: fines
            String createFinesTable = """
                        CREATE TABLE IF NOT EXISTS fines (
                            fine_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            plate_number TEXT NOT NULL,
                            ticket_id TEXT NOT NULL,
                            fine_type TEXT NOT NULL CHECK(fine_type IN ('OVERSTAY', 'RESERVED_MISUSE')),
                            fine_amount REAL NOT NULL,
                            fine_scheme TEXT NOT NULL,
                            is_paid INTEGER DEFAULT 0 CHECK(is_paid IN (0, 1)),
                            created_at TEXT NOT NULL,
                            FOREIGN KEY(plate_number) REFERENCES vehicles(plate_number),
                            FOREIGN KEY(ticket_id) REFERENCES tickets(ticket_id)
                        );
                    """;
            stmt.execute(createFinesTable);

            // Table 5: payments
            String createPaymentsTable = """
                        CREATE TABLE IF NOT EXISTS payments (
                            payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            ticket_id TEXT NOT NULL,
                            parking_fee REAL NOT NULL,
                            fine_amount REAL DEFAULT 0.0,
                            total_amount REAL NOT NULL,
                            payment_method TEXT NOT NULL CHECK(payment_method IN ('CASH', 'CARD')),
                            payment_time TEXT NOT NULL,
                            FOREIGN KEY(ticket_id) REFERENCES tickets(ticket_id)
                        );
                    """;
            stmt.execute(createPaymentsTable);

            // // Table 6: reservations
            String createReservationsTable = """
                        CREATE TABLE IF NOT EXISTS reservations (
                            reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            spot_id TEXT NOT NULL,
                            plate_number TEXT,
                            reserved_at TEXT NOT NULL,
                            is_active INTEGER DEFAULT 1 CHECK(is_active IN (0, 1)),
                            FOREIGN KEY(spot_id) REFERENCES parking_spots(spot_id)
                        );
                    """;
            stmt.execute(createReservationsTable);

            // Table 7: system_config
            String createConfigTable = """
                        CREATE TABLE IF NOT EXISTS system_config (
                            config_key TEXT PRIMARY KEY,
                            config_value TEXT NOT NULL
                        );
                    """;
            stmt.execute(createConfigTable);

            // Initialize default fine scheme if not exists
            String initConfig = """
                        INSERT OR IGNORE INTO system_config (config_key, config_value)
                        VALUES ('FINE_SCHEME', 'FIXED');
                    """;
            stmt.execute(initConfig);

            System.out.println("Database schema initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Failed to initialize database!");
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close connection!");
            e.printStackTrace();
        }
    }

    public void resetDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS payments;");
            stmt.execute("DROP TABLE IF EXISTS fines;");
            stmt.execute("DROP TABLE IF EXISTS tickets;");
            // stmt.execute("DROP TABLE IF EXISTS reservations;");
            stmt.execute("DROP TABLE IF EXISTS vehicles;");
            stmt.execute("DROP TABLE IF EXISTS parking_spots;");
            stmt.execute("DROP TABLE IF EXISTS system_config;");
            System.out.println("Database reset completed.");
            // Reinitialize
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Failed to reset database!");
            e.printStackTrace();
        }
    }
}
