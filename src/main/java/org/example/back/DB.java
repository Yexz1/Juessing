
package org.example.back;

/**
 * create database projet;
 * grant all privileges on test.* to foo@localhost identified by "bar";
 * flush privileges;
 */

 import java.sql.*;

public class DB {
    private static final String driverClassName = "org.mariadb.jdbc.Driver";
    private static final String url = "jdbc:mariadb://localhost/test";
    private static final String username = "foo";
    private static final String password = "bar";
    private static Connection connection;


    static {
        try {
            loadDrive();
            getConnection();
            createTable();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadDrive() throws ClassNotFoundException {
        Class.forName(driverClassName);
        System.out.println("Driver loaded");
    }

    private static void getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(url, username, password);
        }
    }

    private static void createTable() throws SQLException {
        String table = "CREATE TABLE IF NOT EXISTS words ( id int NOT NULL AUTO_INCREMENT PRIMARY KEY, word varchar(30))";
        try (Statement statement = connection.createStatement()) {
            statement.execute(table);
        }
    }

    public static void addWord(String s) throws SQLException {
        String insert = "INSERT INTO words(word) VALUES(?)";
        try {
            PreparedStatement statement = connection.prepareStatement(insert);
            statement.setString(1, s);
            statement.executeUpdate();
        } catch (Exception _) {}
    }

    public static boolean searchWord(String s) throws SQLException {
        String select = "SELECT * FROM words WHERE word = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(select);
            statement.setString(1, s);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (Exception _) {}
        return false;
    }

    public static String getWord(int i) throws SQLException {
        String select = "SELECT * FROM words WHERE id = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(select);
            statement.setInt(1, i);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return resultSet.getString("word");
        } catch (SQLException _) {}
        return null;
    }
}
