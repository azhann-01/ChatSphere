package main;

import database.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {

    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Database connected successfully.");
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
