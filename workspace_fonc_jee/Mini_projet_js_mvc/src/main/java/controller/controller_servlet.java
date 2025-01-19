package controller;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;

@WebServlet("/servlet_controller")
public class controller_servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Récupérer l'action depuis les paramètres
        String action = request.getParameter("action");

        if (action == null || action.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Action non spécifiée\"}");
            return;
        }

        // Utiliser RequestDispatcher pour déléguer en fonction de l'action
        String destination = null;

        switch (action) {
            case "validateDatabase":
                destination = "/ValidateDatabaseServlet";
                break;
            case "loadTables":
                destination = "/GetTablesServlet";
                break;
            case "loadColumns":
                destination = "/GetColumnsServlet";
                break;
            case "fetchData":
                destination = "/GetDataServlet";
                break;
            case "getDatabases":
                destination = "/GetDatabasesServlet";
                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Action inconnue\"}");
                return;
        }

        // Dispatcher la requête
        RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
