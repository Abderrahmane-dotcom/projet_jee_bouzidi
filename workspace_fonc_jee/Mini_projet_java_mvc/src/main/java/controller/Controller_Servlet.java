package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import modele.FetchData;
import modele.GetColumns;
import modele.GetTables;
import modele.Validatedatabase;

import java.io.*;
import java.util.List;

import org.json.JSONObject;

@WebServlet("/servlet_controller")
public class Controller_Servlet extends HttpServlet {
    private static final long serialVersionUID = -1970351388049509522L;
    /*Cette servlet jouera le rôle du controlleur, elle va récupérer la requête du client et puis elle va lui fournir la réponse*/
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	/*La requête GET du client contient les paramètres suivants*/
        String action = request.getParameter("action");
        String dbName = request.getParameter("dbName");
        String tableName = request.getParameter("tableName");
        String columns = request.getParameter("columns");
        String format = request.getParameter("format");
        // Pas d'action, il faut envoyer une réponse d'erreur
        if (action == null || action.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Action non spécifiée\"}");
            return;
        }

        
        FetchData fetchData = new FetchData();
        GetColumns getColumns = new GetColumns();
        GetTables getTables = new GetTables();
        Validatedatabase validateDatabase = new Validatedatabase();
        // selon l'action spécifiée dans le requête, une fonction du package modele va être appelée
        try {
            switch (action) {
                case "fetchData":
                    if (dbName == null || tableName == null || columns == null || format == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\": \"Paramètres manquants pour l'action fetchData\"}");
                        return;
                    }
                    
                    List<ObjectNode> results = fetchData.fetchData(dbName, tableName, columns);
                    handleFileGeneration(response, results, tableName, columns, format); // fonction pour envoyer les données selon de format spécifique
                    break;

                case "loadColumns":
                    if (dbName == null || tableName == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\": \"Database or table name is missing\"}");
                        return;
                    }
                    
                    List<String> columnsList = getColumns.fetchColumns(dbName, tableName);   // pour obtenir la liste de colonnes
                    response.setContentType("application/json");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(columnsList));
                    break;

                case "loadTables":
                    if (dbName == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\": \"Database name is missing\"}");
                        return;
                    }
                    
                    List<String> tablesList = getTables.fetchTables(dbName); //pour obtenir la liste de tables
                    response.setContentType("application/json");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(tablesList));
                    break;

                case "validateDatabase":
                    if (dbName == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\": \"Database name is missing\"}");
                        return;
                    }
                    
                    JSONObject dbCheckResponse = validateDatabase.generateDatabaseCheckResponse(dbName); //pour valider l'existance ou pas de la base de données
                    response.setContentType("application/json");
                    response.getWriter().write(dbCheckResponse.toString());
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Action inconnue\"}");
                    break;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    // fonction pour générer le fichier résultat selon le format spécifié par le client
    private void handleFileGeneration(HttpServletResponse response, List<ObjectNode> results, String tableName, String columns, String format) throws IOException {
        FetchData fetchData = new FetchData(); // Instance de FetchData

        switch (format.toLowerCase()) {
            case "json":
                fetchData.generateJSONFile(response, results, tableName);
                break;
            case "xml":
                fetchData.generateXMLFile(response, results, tableName);
                break;
            case "csv":
                fetchData.generateCSVFile(response, results, columns, tableName);
                break;
            case "xls":
                fetchData.generateExcelFile(response, results, columns, tableName);
                break;
            case "pdf":
                fetchData.generatePDFFile(response, results, columns.split(","), tableName);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Unsupported format\"}");
        }
    }


}
