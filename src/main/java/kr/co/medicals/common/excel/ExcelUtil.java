package kr.co.medicals.common.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class ExcelUtil {
    public static CellStyle getHeaderCellStyle(SXSSFWorkbook workbook){
        CellStyle greyCellStyle = workbook.createCellStyle();
        greyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        greyCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        greyCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        greyCellStyle.setWrapText(true);
        return greyCellStyle;
    }

    public static CellStyle getCellDateStyle(SXSSFWorkbook workbook){
        CellStyle cellDateStyle= workbook.createCellStyle();
        CreationHelper creationHelper= workbook.getCreationHelper();
        cellDateStyle.setDataFormat(
                creationHelper.createDataFormat().getFormat("yyyy-mm-dd")
        );
        return cellDateStyle;
    }

    public static void setCellValue(String columeName, Cell cell, Object value, SXSSFWorkbook workbook){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        if(columeName.contains("date")||columeName.contains("DATE")) {
            try {
                if(columeName.equals("contractSdate") || columeName.equals("contractEdate")) {
                    Timestamp ts = new Timestamp((Long) value);
                    cell.setCellValue(sdf.parse(String.valueOf(ts)));
                }else{
                    if(((String) value).contains("/")) value=((String) value).replace("/","-");
                    cell.setCellValue(sdf.parse((String) value));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cell.setCellStyle(ExcelUtil.getCellDateStyle(workbook));
        }else{
            if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else if (value instanceof Integer) {
                cell.setCellValue(Integer.parseInt(String.valueOf(value)));
            } else if (value instanceof Double) {
                cell.setCellValue(Double.parseDouble(String.valueOf(value)));
            } else if (value instanceof String) {
                cell.setCellValue(String.valueOf(value));
            } else {
                cell.setCellValue("");
            }
        }
    }

    public static CellStyle autoLineChange(SXSSFWorkbook workbook){
        CellStyle autoLineChange = workbook.createCellStyle();
        autoLineChange.setWrapText(true);
        return autoLineChange;
    }
}