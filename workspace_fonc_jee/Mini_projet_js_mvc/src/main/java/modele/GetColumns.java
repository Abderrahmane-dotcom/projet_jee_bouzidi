package modele;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetColumnsServlet")
public class GetColumns extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dbName = request.getParameter("dbName");
        String tableName = request.getParameter("tableName");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (dbName == null || dbName.isEmpty() || tableName == null || tableName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Database and table names are required\"}");
            return;
        }
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3305/" + dbName, "root", "abderrahmane2003")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW COLUMNS FROM " + tableName);
            List<String> columns = new ArrayList<>();
            while (resultSet.next()) {
                columns.add(resultSet.getString(1));
            }
            response.getWriter().write(new org.json.JSONArray(columns).toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Failed to fetch columns\"}");
        }
    }
}
