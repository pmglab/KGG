/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.text;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author mxli
 */
public class LocalExcelFile {

    public static boolean WriteArray2XLSXFile(String outFileName, List<String[]> dateList, boolean hasHead, int indexKey) throws
            Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 = wb.createSheet("Data");
        if (dateList.isEmpty()) {
            return false;
        }
        int listSize = dateList.size();
        String[] cells = dateList.get(0);
        int columnNum = cells.length;
        for (int i = 0; i < columnNum; i++) {
            sheet1.setColumnWidth(i, (short) ((30 * 6) / ((double) 1 / 20)));
        }

        XSSFCellStyle headStyle = wb.createCellStyle();
        //apply custom font to the text in the comment
        XSSFFont font = wb.createFont();
        font.setFontName("Courier New");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.RED.index);

        headStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        headStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        headStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        headStyle.setLocked(true);
        headStyle.setFont(font);

        XSSFCellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        XSSFCellStyle markedBodyStyle = wb.createCellStyle();
        markedBodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        markedBodyStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        markedBodyStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        //create titile row
        XSSFRow row = sheet1.createRow(rowIndex);

        String lastKey = null;
        int switcher = -1;
        XSSFCell cell = null;

        if (hasHead) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(cells[i]);
                cell.setCellStyle(headStyle);
            }
        } else {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(cells[i]);
                cell.setCellStyle(bodyStyle);
            }
        }
        rowIndex++;
        for (; rowIndex < listSize; rowIndex++) {
            String[] cells1 = dateList.get(rowIndex);
            row = sheet1.createRow((rowIndex));
            columnNum = cells1.length;
            if (indexKey >= 0) {
                if (lastKey == null && cells1[indexKey] != null) {
                    lastKey = cells1[indexKey];
                    switcher *= -1;
                } else if (lastKey != null && cells1[indexKey] == null) {
                    lastKey = cells1[indexKey];
                    switcher *= -1;
                } else if (lastKey == null && cells1[indexKey] == null) {
                } else {
                    if (!lastKey.equals(cells1[indexKey])) {
                        switcher *= -1;
                        lastKey = cells1[indexKey];
                    }
                }
            } else {
                switcher = 1;
            }
            // System.out.println(cells1[0]);
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (switcher > 0) {
                    cell.setCellStyle(bodyStyle);
                } else {
                    cell.setCellStyle(markedBodyStyle);
                }

                if (cells1[j] != null) {
                    if (Util.isNumeric(cells1[j])) {
                        //org.?apache.?poi.?XSSF.?usermodel.?XSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(cells1[j]));
                    } else {
                        cell.setCellValue(cells1[j]);
                    }
                } else {
                    cell.setCellValue(".");
                }
            }

        }

        // Write the output to a inFile
        FileOutputStream fileOut = new FileOutputStream(outFileName);
        wb.write(fileOut);
        fileOut.close();

        return true;
    }

    public static boolean WriteArray2XLSXFile(String fileName, List<String[]> arry, boolean hasHead) throws IOException {
        int rowNum = arry.size();
        if (rowNum == 0) {
            System.err.println("No input data!");
            return false;
        }

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 = wb.createSheet("Data");
        String[] titleNames = null;
        if (hasHead) {
            titleNames = arry.get(0);
        }
        int columnNum = arry.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet1.setColumnWidth(i, (int) ((30 * 6) / ((double) 1 / 20)));
        }

        XSSFCellStyle headStyle = wb.createCellStyle();
        //apply custom font to the text in the comment
        XSSFFont font = wb.createFont();
        font.setFontName("Courier New");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(new XSSFColor(Color.RED));

        headStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        headStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        headStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        headStyle.setLocked(true);
        headStyle.setFont(font);

        XSSFCellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        XSSFCellStyle markedBodyStyle = wb.createCellStyle();
        markedBodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        markedBodyStyle.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
        markedBodyStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        //create titile row
        XSSFRow row = sheet1.createRow(rowIndex);

        XSSFCell cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet1.createRow(i);
            String[] line = arry.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?XSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }
            }

            if (i >= 1048576) {
                String strInfo = "Due to limited capacity of MS Excel 2007, only the first 1048576 rows are exported!. ";
                JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
                break;
            }
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(fileName);
        wb.write(fileOut);
        fileOut.close();

        return true;

    }

    public static boolean WriteArray2XLSXFile(String filePath, String[] titleNames, List<String[]> arry) throws IOException {

        int rowNum = arry.size();
        if (rowNum == 0) {
            System.err.println("No input data!");
            return false;
        }

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 = wb.createSheet("Data");
        if (titleNames == null) {
            titleNames = arry.get(0);
        }
        int columnNum = arry.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet1.setColumnWidth(i, (int) ((30 * 6) / ((double) 1 / 20)));
        }

        XSSFCellStyle headStyle = wb.createCellStyle();
        //apply custom font to the text in the comment
        XSSFFont font = wb.createFont();
        font.setFontName("Courier New");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(new XSSFColor(Color.RED));
        headStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);

        headStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        headStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        headStyle.setLocked(true);
        headStyle.setFont(font);

        XSSFCellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        XSSFCellStyle markedBodyStyle = wb.createCellStyle();
        markedBodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        markedBodyStyle.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
        markedBodyStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        //create titile row
        XSSFRow row = sheet1.createRow(rowIndex);

        XSSFCell cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet1.createRow(i);
            String[] line = arry.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?XSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }
            }

            if (i >= 1048576) {
                String strInfo = "Due to limited capacity of MS Excel 2007, only the first 1048576 rows are exported!. ";
                JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
                break;
            }
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(filePath);
        wb.write(fileOut);
        fileOut.close();

        return true;

    }

    public static boolean WriteArray2XLSXFile(String filePath, String[] titleNames, List<String[]> arry, List<String[]> arry2) throws IOException {

        int rowNum = arry.size();
        if (rowNum == 0) {
            System.err.println("No input data!");
            return false;
        }

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet1 = wb.createSheet("InsideGenes");

        if (titleNames == null) {
            titleNames = arry.get(0);
        }
        int columnNum = arry.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet1.setColumnWidth(i, (int) ((30 * 6) / ((double) 1 / 20)));
        }

        XSSFCellStyle headStyle = wb.createCellStyle();
        //apply custom font to the text in the comment
        XSSFFont font = wb.createFont();
        font.setFontName("Courier New");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(new XSSFColor(Color.RED));

        headStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        headStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        headStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        headStyle.setLocked(true);
        headStyle.setFont(font);

        XSSFCellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        bodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        XSSFCellStyle markedBodyStyle = wb.createCellStyle();
        markedBodyStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        markedBodyStyle.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
        markedBodyStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        //create titile row
        XSSFRow row = sheet1.createRow(rowIndex);

        XSSFCell cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet1.createRow(i);
            String[] line = arry.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?XSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }
            }
            if (i >= 1048576) {
                String strInfo = "Due to limited capacity of MS Excel 2007, only the first 1048576 rows are exported!. ";
                JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
                break;
            }
        }

        rowNum = arry2.size();
        XSSFSheet sheet2 = wb.createSheet("OutsideGenes");
        if (titleNames != null) {
            titleNames = arry2.get(0);
        }
        columnNum = arry2.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet2.setColumnWidth(i, (int) ((30 * 6) / ((double) 1 / 20)));
        }
        rowIndex = 0;
        row = sheet2.createRow(rowIndex);
        cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet2.createRow(i);
            String[] line = arry2.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?XSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }

            }
//            if (i >= 32765) {
//                String strInfo = "The number of lines has overflowed, so parts of the data are omitted!. ";
//                JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
//                break;
//            }//To adapt to the limitation. This problem should be solved sooner. 
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(filePath);
        wb.write(fileOut);
        fileOut.close();

        return true;
    }

    /**
     * Creates a new instance of LocalExcelFile
     */
    public LocalExcelFile() {
    }

//    public static void test() throws Exception {
//
//        OutputStream os = new FileOutputStream("test.xlsx");
//
//        XSSFWorkbook wb = new XSSFWorkbook();
//
//        XSSFSheet sheet = wb.createSheet("test");
//
//        XSSFRow row = sheet.createRow(0);
//
//        row.createCell(0).setCellValue("column1");
//
//        row.createCell(1).setCellValue("column2");
//
//        wb.write(os);
//
//        os.close();
//
//        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream("test.xlsx"));
//
//        int sheetIndex = 0;
//        XSSFSheet sheet1 = workbook.getSheetAt(sheetIndex);
//
//        for (Row tempRow : sheet1) {
//
//            // print out the first three columns
//            for (int column = 0; column < 3; column++) {
//                Cell tempCell = tempRow.getCell(column);
///*
//                if (tempCell.getCellType() == Cell.CELL_TYPE_STRING) {
//                    System.out.print(tempCell.getStringCellValue() + "  ");
//                }
//        */
//            }
//            System.out.println();
//        }
//    }    
    public static boolean writeArray2ExcelFile(String fileName, List<String[]> arry, boolean hasHead) throws
            Exception {
        int rowNum = arry.size();
        if (rowNum == 0) {
            System.err.println("No input data!");
            return false;
        }

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Data");
        String[] titleNames = null;
        if (hasHead) {
            titleNames = arry.get(0);
        }
        int columnNum = arry.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet1.setColumnWidth(i, (int) ((30 * 6) / ((double) 1 / 20)));
        }

        HSSFCellStyle headStyle = wb.createCellStyle();
        //apply custom font to the text in the comment
        HSSFFont font = wb.createFont();
        font.setFontName("Courier New");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.RED.index);

        headStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        headStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        headStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        headStyle.setLocked(true);
        headStyle.setFont(font);

        HSSFCellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        HSSFCellStyle markedBodyStyle = wb.createCellStyle();
        markedBodyStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        markedBodyStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
        markedBodyStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        //create titile row
        HSSFRow row = sheet1.createRow(rowIndex);

        HSSFCell cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet1.createRow((i));
            String[] line = arry.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?HSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }

                if (i >= 65536) {
                    String strInfo = "Due to limited capacity of MS Excel 2003, only the first 65536 rows are exported!. ";
                    JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(fileName);
        wb.write(fileOut);
        fileOut.close();

        return true;
    }

    public static boolean writeArray2ExcelFile(String filePath, String[] titleNames, List<String[]> arry) throws
            Exception {
        int rowNum = arry.size();
        if (rowNum == 0) {
            System.err.println("No input data!");
            return false;
        }

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Data");
        if (titleNames == null) {
            titleNames = arry.get(0);
        }
        int columnNum = arry.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet1.setColumnWidth((short) i, (int) ((30 * 6) / ((double) 1 / 20)));
        }

        HSSFCellStyle headStyle = wb.createCellStyle();
        //apply custom font to the text in the comment
        HSSFFont font = wb.createFont();
        font.setFontName("Courier New");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.RED.index);

        headStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        headStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        headStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        headStyle.setLocked(true);
        headStyle.setFont(font);

        HSSFCellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        HSSFCellStyle markedBodyStyle = wb.createCellStyle();
        markedBodyStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        markedBodyStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
        markedBodyStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        //create titile row
        HSSFRow row = sheet1.createRow(rowIndex);

        HSSFCell cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet1.createRow((i));
            String[] line = arry.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?HSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }
            }

            if (i >= 65536) {
                String strInfo = "Due to limited capacity of MS Excel 2003, only the first 65536 rows are exported!. ";
                JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
                break;
            }
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(filePath);
        wb.write(fileOut);
        fileOut.close();

        return true;
    }

    public static boolean writeArray2ExcelFile(String filePath, String[] titleNames, List<String[]> arry, List<String[]> arry2) throws FileNotFoundException, IOException {

        int rowNum = arry.size();
        if (rowNum == 0) {
            System.err.println("No input data!");
            return false;
        }

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("InsideGenes");

        if (titleNames == null) {
            titleNames = arry.get(0);
        }
        int columnNum = arry.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet1.setColumnWidth((short) i, (int) ((30 * 6) / ((double) 1 / 20)));
        }

        HSSFCellStyle headStyle = wb.createCellStyle();
        //apply custom font to the text in the comment
        HSSFFont font = wb.createFont();
        font.setFontName("Courier New");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.RED.index);

        headStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        headStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        headStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        headStyle.setLocked(true);
        headStyle.setFont(font);

        HSSFCellStyle bodyStyle = wb.createCellStyle();
        bodyStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        HSSFCellStyle markedBodyStyle = wb.createCellStyle();
        markedBodyStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        markedBodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        markedBodyStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
        markedBodyStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        //create titile row
        HSSFRow row = sheet1.createRow(rowIndex);

        HSSFCell cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet1.createRow(i);
            String[] line = arry.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?HSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }

            }
            if (i >= 65536) {
                String strInfo = "Due to limited capacity of MS Excel 2003, only the first 65536 rows are exported!. ";
                JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
                break;
            }
        }

        rowNum = arry2.size();
        HSSFSheet sheet2 = wb.createSheet("OutsideGenes");
        if (titleNames != null) {
            titleNames = arry2.get(0);
        }
        columnNum = arry2.get(0).length;

        for (int i = 0; i < columnNum; i++) {
            sheet2.setColumnWidth((short) i, (int) ((30 * 6) / ((double) 1 / 20)));
        }
        rowIndex = 0;
        row = sheet2.createRow(rowIndex);
        cell = null;
        if (titleNames != null) {
            for (int i = 0; i < columnNum; i++) {
                cell = row.createCell(i);
                cell.setCellValue(titleNames[i]);
                cell.setCellStyle(headStyle);
            }
            rowIndex++;
        }

        for (int i = rowIndex; i < rowNum; i++) {
            row = sheet2.createRow((i));
            String[] line = arry2.get(i);
            columnNum = line.length;
            for (int j = 0; j < columnNum; j++) {
                cell = row.createCell(j);
                if (line[0] != null) {
                    cell.setCellStyle(markedBodyStyle);
                } else {
                    cell.setCellStyle(bodyStyle);
                }
                if (line[j] != null) {
                    if (LocalString.isNumeric(line[j])) {
                        //org.?apache.?poi.?hssf.?usermodel.?HSSFCell.CELL_TYPE_NUMERIC
                        cell.setCellType(0);
                        cell.setCellValue(Double.parseDouble(line[j]));
                    } else {
                        cell.setCellValue(line[j]);
                    }
                } else {
                    cell.setCellValue("-");
                }

            }
            if (i >= 65536) {
                String strInfo = "The number of lines has overflowed, so parts of the data are omitted!. ";
                JOptionPane.showMessageDialog(null, strInfo, "Warning!", JOptionPane.WARNING_MESSAGE);
                break;
            }//To adapt to the limitation. This problem should be solved sooner. 
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(filePath);
        wb.write(fileOut);
        fileOut.close();
        return true;
    }

}
