import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {
    public static List<Map<String, String>> readTestData(String filePath) throws IOException {
        List<Map<String, String>> testData = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

        // Assuming the first row contains column headers
        Row headerRow = sheet.getRow(0);
        int lastColumn = headerRow.getLastCellNum();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row currentRow = sheet.getRow(i);

            if (currentRow != null) {
                Map<String, String> rowMap = new HashMap<>();

                for (int j = 0; j < lastColumn; j++) {
                    Cell currentCell = currentRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell headerCell = headerRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                    if (headerCell != null) {
                        rowMap.put(headerCell.getStringCellValue(), currentCell.toString());
                    } else {
                        // Handle null headerCell
                    }
                }

                testData.add(rowMap);
            }
        }

        workbook.close();
        inputStream.close();
        return testData;
    }
}