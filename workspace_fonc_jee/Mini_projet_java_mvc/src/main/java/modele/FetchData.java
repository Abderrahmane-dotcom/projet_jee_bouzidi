package modele;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import com.thoughtworks.xstream.XStream;

import jakarta.servlet.http.HttpServletResponse;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FetchData {
	// cette classe du modele contient l'implémentation des fonctions de :  récupération de données et formatage selon le type de fichier choisit
	public List<ObjectNode> fetchData(String dbName, String tableName, String columns) throws SQLException, ClassNotFoundException {
		// fonction pour récupérer les données sachant les colonnes , la table et la base de données choisie
	    if (dbName == null || dbName.isEmpty() || tableName == null || tableName.isEmpty()
	            || columns == null || columns.isEmpty()) {
	        throw new IllegalArgumentException("Missing or invalid parameters");
	    }

	    if (!tableName.matches("^[a-zA-Z0-9_]+$") || !columns.matches("^[a-zA-Z0-9_,]+$")) {
	        throw new IllegalArgumentException("Invalid table or column names");
	    }

	    Class.forName("com.mysql.cj.jdbc.Driver");

	    String query = "SELECT " + columns + " FROM " + tableName;

	    try (Connection connection = DriverManager.getConnection(
	            "jdbc:mysql://localhost:3305/" + dbName, "root", "abderrahmane2003");
	         PreparedStatement preparedStatement = connection.prepareStatement(query)) {

	        List<ObjectNode> results = new ArrayList<>();
	        ObjectMapper mapper = new ObjectMapper();

	        try (ResultSet resultSet = preparedStatement.executeQuery()) {
	            while (resultSet.next()) {
	                ObjectNode row = mapper.createObjectNode();
	                for (String column : columns.split(",")) {
	                    row.put(column.trim(), resultSet.getString(column.trim()));
	                }
	                results.add(row);
	            }
	        }

	        return results;
	    }
	}
    // fonction pour la génaration du fichier JSON
    public void generateJSONFile(HttpServletResponse response, List<ObjectNode> results, String tableName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=" + tableName + "_data.json");
        try (PrintWriter out = response.getWriter()) {
            out.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));
        }
    }
    // fonction pour la génération du fichier XML 
    public void generateXMLFile(HttpServletResponse response, List<ObjectNode> results, String tableName) throws IOException {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Le nom de la table ne peut pas être vide.");
        }
        XStream xstream = new XStream();
        xstream.alias(tableName, List.class); 

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlBuilder.append("<").append(tableName).append(">\n");

        int lineNumber = 1;
        for (ObjectNode row : results) {
            xmlBuilder.append("  <ligne").append(lineNumber).append(">\n"); 
            row.fieldNames().forEachRemaining(field -> {
                String value = row.get(field).asText();
                xmlBuilder.append("    <").append(field).append(">")
                          .append(value)
                          .append("</").append(field).append(">\n");
            });
            xmlBuilder.append("  </ligne").append(lineNumber).append(">\n");
            lineNumber++;
        }

        xmlBuilder.append("</").append(tableName).append(">\n");

        
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment; filename=" + tableName + "_data.xml");
        try (PrintWriter out = response.getWriter()) {
            out.write(xmlBuilder.toString());
        }
    }
    // pour la génération du fichier csv
    public void generateCSVFile(HttpServletResponse response, List<ObjectNode> results, String columns, String tableName) throws IOException {
        String[] columnArray = columns.split(",");
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + tableName + "_data.csv");

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"))) {
            writer.writeNext(columnArray);

            for (ObjectNode row : results) {
                String[] rowData = new String[columnArray.length];
                for (int i = 0; i < columnArray.length; i++) {
                    rowData[i] = row.get(columnArray[i]).asText();
                }
                writer.writeNext(rowData);
            }
        }
    }
    // pour la génération du fichier .xls
    public void generateExcelFile(HttpServletResponse response, List<ObjectNode> results, String columns, String tableName) throws IOException {
        String[] columnArray = columns.split(",");

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + tableName + "_data.xls");
        try (OutputStream out = response.getOutputStream()) {
            WritableWorkbook workbook = Workbook.createWorkbook(out);
            WritableSheet sheet = workbook.createSheet("Data", 0);

            for (int i = 0; i < columnArray.length; i++) {
                sheet.addCell(new Label(i, 0, columnArray[i]));
            }

            int rowNum = 1;
            for (ObjectNode row : results) {
                for (int i = 0; i < columnArray.length; i++) {
                    String value = row.get(columnArray[i]).asText("");
                    sheet.addCell(new Label(i, rowNum, value));
                }
                rowNum++;
            }

            workbook.write();
            workbook.close();
        } catch (Exception e) {
            throw new IOException("Error generating Excel file", e);
        }
    }
    // pour la génération du fichier pdf
    public void generatePDFFile(HttpServletResponse response, List<ObjectNode> results, String[] columns, String tableName) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + tableName + "_data.pdf");
        try (OutputStream out = response.getOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            PdfPTable table = new PdfPTable(columns.length);
            for (String column : columns) {
                table.addCell(new Phrase(column));
            }

            for (ObjectNode row : results) {
                for (String column : columns) {
                    table.addCell(new Phrase(row.get(column).asText()));
                }
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        }
    }
    
    
    
    
}
