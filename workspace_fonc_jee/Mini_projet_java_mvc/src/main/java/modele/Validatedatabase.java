package modele;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONException;
import org.json.JSONObject;

public class Validatedatabase {

    private static final String URL = "jdbc:mysql://localhost:3305"; 
    private static final String USER = "root";
    private static final String PASSWORD = "abderrahmane2003";

    // fonction pour vérifier si la base de données choisit existe ou pas
    public boolean checkDatabaseExists(String dbName) {
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }

        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
          
            String query = "SHOW DATABASES";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String existingDbName = resultSet.getString(1);
                    if (existingDbName.equalsIgnoreCase(dbName)) {
                        return true; 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while checking database existence", e);
        }

        return false; 
    }
    // fonction pour envoyer la réponse sous format JSONObject
    public JSONObject generateDatabaseCheckResponse(String dbName) {
        JSONObject jsonResponse = new JSONObject();

        try {
            if (dbName == null || dbName.isEmpty()) {
                jsonResponse.put("error", "Database name is missing");
            } else {
                boolean dbExists = checkDatabaseExists(dbName);

                if (dbExists) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("dbName", dbName);
                } else {
                    jsonResponse.put("error", "Base de données non trouvée");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating JSON response", e);
        }

        return jsonResponse;
    }
}
