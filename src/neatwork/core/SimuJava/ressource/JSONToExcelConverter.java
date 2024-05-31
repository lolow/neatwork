package neatwork.core.SimuJava.ressource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;

public class JSONToExcelConverter {

    public static void main(String[] args) {
        convertJSONToExcel("SimuJava/src/dat.json", "SimuJava/src/output.xlsx");
    }

    public static void convertJSONToExcel(String jsonFilePath, String excelFilePath) {
        try {
            // Lire le fichier JSON
            FileReader reader = new FileReader(jsonFilePath);
            JSONObject jsonObject = new JSONObject(reader);

            // Créer un nouveau fichier Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Data");

            int rowNum = 0;

            // Parcourir les clés du JSON
            for (String key : jsonObject.keySet()) {
                // Écrire la clé dans la première colonne de chaque ligne
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(key);

                // Récupérer les valeurs correspondantes
                Object value = jsonObject.get(key);

                // Si la valeur est un tableau, écrire chaque élément dans une colonne séparée
                if (value instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) value;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Row dataRow = sheet.createRow(rowNum++);
                        Cell dataCell = dataRow.createCell(0);
                        dataCell.setCellValue(jsonArray.getDouble(i));
                    }
                } else {
                    // Si la valeur est un scalaire, écrire dans la deuxième colonne
                    Cell valueCell = row.createCell(1);
                    if (value instanceof Double) {
                        valueCell.setCellValue((Double) value);
                    } else if (value instanceof Integer) {
                        valueCell.setCellValue((Integer) value);
                    } else if (value instanceof String) {
                        valueCell.setCellValue((String) value);
                    }
                }

                // Sauter une ligne après chaque clé
                rowNum++;
            }

            // Écrire le contenu dans un fichier Excel
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }

            // Fermer le workbook
            workbook.close();

            System.out.println("Conversion terminée avec succès.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
