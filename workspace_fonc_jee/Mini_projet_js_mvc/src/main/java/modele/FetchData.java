package modele;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


import org.json.JSONObject;

@WebServlet("/GetDataServlet")
public class FetchData extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dbName = request.getParameter("dbName");
        String tableName = request.getParameter("tableName");
        String columns = request.getParameter("columns");
        
        
        response.setCharacterEncoding("UTF-8");
        
        System.out.println("Received dbName: " + dbName);

        // Validate inputs
        if (dbName == null || dbName.isEmpty() || tableName == null || tableName.isEmpty()
                || columns == null || columns.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or invalid parameters\"}");
            return;
        }

        // Validate table and column names
        if (!tableName.matches("^[a-zA-Z0-9_]+$") || !columns.matches("^[a-zA-Z0-9_,]+$")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid table or column names\"}");
            return;
        }
        
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3305/" + dbName, "root", "abderrahmane2003")) {

            String query = "SELECT " + columns + " FROM " + tableName;
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                List<JSONObject> results = new ArrayList<>();
                while (resultSet.next()) {
                    JSONObject row = new JSONObject();
                    for (String column : columns.split(",")) {
                        row.put(column, resultSet.getObject(column));
                    }
                    results.add(row);
                }

                response.setContentType("application/json");
                response.getWriter().write(new org.json.JSONArray(results).toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unexpected server error: " + e.getMessage() + "\"}");
        }
    }
}

