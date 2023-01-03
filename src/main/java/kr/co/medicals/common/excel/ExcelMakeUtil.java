package kr.co.medicals.common.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ExcelMakeUtil {
    public static SXSSFWorkbook makeSalesExcel(Object searchList, String[] keys, String[] width) {
        int sheetRowCount = ExcelConstants.sheetRowCnt;
        int prevDataRow = 0;
        List<?> list;
        if (searchList instanceof PageImpl<?>) {
            list = ((PageImpl<?>) searchList).getContent();
        } else {
            list = (List<?>) searchList;
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        double headRowCnt = (double) list.size() / (double) sheetRowCount;
        for (int s = 0; s < Math.ceil(headRowCnt); s++) {
            SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(ExcelConstants.sheetName + s);

            int nowSheetRowCount = prevDataRow + sheetRowCount;
            if (nowSheetRowCount > list.size()) {
                nowSheetRowCount = list.size();
            }
            int v = 0;
            for (int j = prevDataRow; j < nowSheetRowCount; j++) {
                if (v == 0) {
                    Row row = sheet.createRow(v++);
                    for (int l = 0; l < keys.length; l++) {
                        Cell cell = row.createCell(l);
                        cell.setCellStyle(ExcelUtil.getHeaderCellStyle(workbook));
                        sheet.setColumnWidth(l, Integer.parseInt(width[l]));
                        cell.setCellValue(keys[l]);
                    }
                }
                Row row = sheet.createRow(v++);
                Map<?, ?> cellValues = (Map<?, ?>) list.get(j);
                for (int l = 0; l < keys.length; l++) {
                    Cell cell = row.createCell(l);
                    Object value = null;
                    if (cellValues.get(keys[l]) != null) {
                        value = cellValues.get(keys[l]);
                    }
                    cell.setCellStyle(ExcelUtil.autoLineChange(workbook));
                    sheet.setColumnWidth(l, Integer.parseInt(width[l]));
                    ExcelUtil.setCellValue("", cell, value, workbook);
                }
            }
            prevDataRow += sheetRowCount;
            try {
                sheet.flushRows(sheetRowCount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return workbook;
    }
}
