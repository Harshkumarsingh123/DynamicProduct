package com.dynamic.product.product.service;

import com.dynamic.product.exception.CustomException;
import com.dynamic.product.product.dto.ProductExcelRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ExcelProductReader {

    private static final long MAX_FILE_SIZE=10*1024*1024;
    private static final List<String> expectedHeaders= List.of(
            "name",
            "description",
            "price",
            "visible",
            "imageUrl",
            "categoryId"
    );


    public List<ProductExcelRow> readExl(MultipartFile file) throws IOException {

        validateFile(file);

        try(Workbook workbook= WorkbookFactory.create(file.getInputStream())){
            if (workbook.getNumberOfSheets() == 0) {
                throw new CustomException(
                        "Excel file contains no worksheets.",
                        HttpStatus.BAD_REQUEST
                );
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new CustomException(
                        "Excel header row is missing.",
                        HttpStatus.BAD_REQUEST
                );
            }

            DataFormatter formatter = new DataFormatter();
            validateHeader(headerRow, formatter);
            List<ProductExcelRow> records = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null || isEmptyRow(row, formatter)) {
                    continue;
                }

                records.add(new ProductExcelRow(
                        i + 1,
                        getCellValue(row, 0, formatter),
                        getCellValue(row, 1, formatter),
                        getCellValue(row, 2, formatter),
                        getCellValue(row, 3, formatter),
                        getCellValue(row, 4, formatter),
                        getCellValue(row, 5, formatter)
                ));
            }

            return records;
        }

    }

    private void validateFile(MultipartFile file){
        if(file==null || file.isEmpty()){
            throw new CustomException("Please upload an Excel file.",HttpStatus.BAD_REQUEST);
        }

        if(file.getSize()>MAX_FILE_SIZE){
            throw new CustomException("File size must not exceed 10 MB.",HttpStatus.BAD_REQUEST);
        }

        String fileName=file.getOriginalFilename();
        if(fileName==null || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")){
            throw new CustomException("Only .xlsx files are supported.", HttpStatus.BAD_REQUEST);
        }
    }


    private void validateHeader(Row headerRow, DataFormatter formatter) {

        if (headerRow.getLastCellNum() != expectedHeaders.size()) {
            throw new CustomException(
                    "Excel must contain exactly these six columns: "
                            + String.join(", ", expectedHeaders),
                    HttpStatus.CONFLICT
            );
        }

        for (int i = 0; i < expectedHeaders.size(); i++) {

            String actualHeader = getCellValue(headerRow, i, formatter)
                    .replace(" ", "")
                    .toLowerCase(Locale.ROOT);

            if (!expectedHeaders.get(i).toLowerCase(Locale.ROOT)
                    .equals(actualHeader)) {

                throw new CustomException(
                        "Invalid header at column " + (i + 1)
                                + ". Expected: " + expectedHeaders.get(i),
                        HttpStatus.CONFLICT
                );
            }
        }
    }

    private String getCellValue(Row row, int column, DataFormatter dataFormatter){

        Cell cell= row.getCell(column,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        
        return cell==null?"":dataFormatter.formatCellValue(cell).trim();
    }

    private boolean isEmptyRow(Row row,DataFormatter dataFormatter){
        for(int i=0;i<expectedHeaders.size();i++){
            if(!(getCellValue(row,i,dataFormatter)).isEmpty()){
                return false;
            }
        }
        return true;
    }

}
