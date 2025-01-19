package modele;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class GetTables {

    private static final String URL_TEMPLATE = "jdbc:mysql://localhost:3305/";
    private static final String USER = "root";
    private static final String PASSWORD = "abderrahmane2003";

    // Fonction pour afficher les tables d'une base de données
    public List<String> fetchTables(String dbName) {
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name is required");
        }

        try {            
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        List<String> tables = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL_TEMPLATE + dbName, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES")) {

            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch tables", e);
        }

        return tables;
    }

    public JSONArray generateTablesJson(String dbName) {
        try {
            List<String> tables = fetchTables(dbName);
            return new JSONArray(tables);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating JSON response", e);
        }
    }
}
