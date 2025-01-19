package modele;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class GetColumns {

    private static final String URL_TEMPLATE = "jdbc:mysql://localhost:3305/";
    private static final String USER = "root";
    private static final String PASSWORD = "abderrahmane2003";
    // fonction pour récupérer les colonnes d'une table d'une base de données choisie
    public List<String> fetchColumns(String dbName, String tableName) {
        if (dbName == null || dbName.isEmpty() || tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Database and table names are required");
        }

        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        List<String> columns = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL_TEMPLATE + dbName, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW COLUMNS FROM " + tableName)) {

            while (resultSet.next()) {
                columns.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch columns", e);
        }

        return columns;
    }
    // renvoie un JSONArray contenant les différentes colonnes existantes
    public JSONArray generateColumnsJson(String dbName, String tableName) {
        try {
            List<String> columns = fetchColumns(dbName, tableName);
            return new JSONArray(columns);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating JSON response", e);
        }
    }
}
