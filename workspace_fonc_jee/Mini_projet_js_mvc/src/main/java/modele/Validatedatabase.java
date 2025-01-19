package modele;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.json.JSONException;
import org.json.JSONObject;


@WebServlet("/ValidateDatabaseServlet")

public class Validatedatabase extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String URL = "jdbc:mysql://localhost:3305"; // Example with MySQL
    private static final String USER = "root";
    private static final String PASSWORD = "abderrahmane2003";

    private boolean checkDatabaseExists(String dbName) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Query to list all databases
            String query = "SHOW DATABASES";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String existingDbName = resultSet.getString(1);
                    if (existingDbName.equalsIgnoreCase(dbName)) {
                        return true; // Database found
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Database does not exist
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String dbName = request.getParameter("dbName"); // Fetch the database name from the request
        JSONObject jsonResponse = new JSONObject();

        if (dbName == null || dbName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                jsonResponse.put("error", "Database name is missing");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            boolean dbExists = checkDatabaseExists(dbName);

            if (dbExists) {
                try {
                    jsonResponse.put("success", true);
                    jsonResponse.put("dbName", dbName); // Return the valid database name
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                try {
                    jsonResponse.put("error", "Database not found");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(jsonResponse.toString()); // Send JSON response
    }
}

