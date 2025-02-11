package com.debatetimer.view.exporter;

import com.debatetimer.domain.Stance;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTableResponse;
import com.debatetimer.dto.parliamentary.response.TableInfoResponse;
import com.debatetimer.dto.parliamentary.response.TimeBoxResponse;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParliamentaryTableExcelExporter {

    private static Font NORMAL;
    private static Font BOLD;
    private static Font BOLD_AND_WHITE;

    private static CellStyle HEADER_STYLE;
    private static CellStyle HEADER_CELL_STYLE;
    private static CellStyle PROS_HEADER_STYLE;
    private static CellStyle CONS_HEADER_STYLE;
    private static CellStyle PROS_STYLE;
    private static CellStyle CONS_STYLE;
    private static CellStyle NEUTRAL_STYLE;

    private static final String NAME_HEADER = "테이블 이름";
    private static final String TYPE_HEADER = "형식";
    private static final String PARLIAMENTARY_HEADER_BODY = "의회식 토론";
    private static final String AGENDA_HEADER = "토론 주제";
    private static final String PROS_HEADER = "찬성";
    private static final String CONS_HEADER = "반대";

    private static final int PROS_COLUMN_NUMBER = 0;
    private static final int CONS_COLUMN_NUMBER = 1;
    private static final int NAME_HEADER_ROW_NUMBER = 1;
    private static final int TYPE_HEADER_ROW_NUMBER = 2;
    private static final int AGENDA_HEADER_ROW_NUMBER = 3;
    private static final int TABLE_HEADER_ROW_NUMBER = 5;
    private static final int TIME_BOX_FIRST_ROW_NUMBER = 6;
    private static final int END_COLUMN_NUMBER = 3;
    private static final int WIDTH_SIZE = 10000;

    private final ParliamentaryTableExportMessageResolver messageResolver;

    private static Font createFont(Workbook workbook, boolean bold, IndexedColors fontColor) {
        Font font = workbook.createFont();
        font.setColor(fontColor.getIndex());
        font.setBold(bold);
        return font;
    }

    private static CellStyle createGroundColor(
            Workbook workBook,
            IndexedColors color,
            Font cellFont,
            HorizontalAlignment align
    ) {
        CellStyle style = workBook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(cellFont);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static void initializeFont(Workbook workbook) {
        NORMAL = createFont(workbook, false, IndexedColors.BLACK);
        BOLD = createFont(workbook, true, IndexedColors.BLACK);
        BOLD_AND_WHITE = createFont(workbook, true, IndexedColors.WHITE);
    }

    private static void initializeStyle(Workbook workbook) {
        HEADER_STYLE = createGroundColor(workbook, IndexedColors.LIGHT_YELLOW, BOLD, HorizontalAlignment.LEFT);
        HEADER_CELL_STYLE = createGroundColor(workbook, IndexedColors.LIGHT_YELLOW, NORMAL, HorizontalAlignment.LEFT);
        PROS_HEADER_STYLE = createGroundColor(workbook, IndexedColors.CORNFLOWER_BLUE, BOLD,
                HorizontalAlignment.CENTER);
        CONS_HEADER_STYLE = createGroundColor(workbook, IndexedColors.CORAL, BOLD, HorizontalAlignment.CENTER);
        PROS_STYLE = createGroundColor(workbook, IndexedColors.LIGHT_CORNFLOWER_BLUE, NORMAL,
                HorizontalAlignment.CENTER);
        CONS_STYLE = createGroundColor(workbook, IndexedColors.ROSE, NORMAL, HorizontalAlignment.CENTER);
        NEUTRAL_STYLE = createGroundColor(workbook, IndexedColors.GREY_50_PERCENT, BOLD_AND_WHITE,
                HorizontalAlignment.CENTER);
    }

    public ByteArrayOutputStream export(
            ParliamentaryTableResponse parliamentaryTableResponse,
            ByteArrayOutputStream outputStream
    ) {
        TableInfoResponse tableInfo = parliamentaryTableResponse.info();
        List<TimeBoxResponse> timeBoxes = parliamentaryTableResponse.table();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(tableInfo.name());
        initializeFont(workbook);
        initializeStyle(workbook);

        createHeader(sheet, NAME_HEADER_ROW_NUMBER, NAME_HEADER, tableInfo.name());
        createHeader(sheet, TYPE_HEADER_ROW_NUMBER, TYPE_HEADER, PARLIAMENTARY_HEADER_BODY);
        createHeader(sheet, AGENDA_HEADER_ROW_NUMBER, AGENDA_HEADER, tableInfo.agenda());

        createTableHeader(sheet);
        createTimeBoxRows(timeBoxes, sheet);
        setColumnWidth(sheet);
        writeToStream(outputStream, workbook);
        return outputStream;
    }

    private void writeToStream(ByteArrayOutputStream outputStream, Workbook workbook) {
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new DTServerErrorException(ServerErrorCode.EXCEL_EXPORT_ERROR);
        }
    }

    private void createHeader(
            Sheet sheet,
            int rowNumber,
            String header,
            String headerBody
    ) {
        Row row = sheet.createRow(rowNumber);
        createCell(row, 0, header, HEADER_STYLE);
        createCell(row, 1, headerBody, HEADER_CELL_STYLE);
    }

    private void createTableHeader(Sheet sheet) {
        Row row = sheet.createRow(TABLE_HEADER_ROW_NUMBER);
        createCell(row, PROS_COLUMN_NUMBER, PROS_HEADER, PROS_HEADER_STYLE);
        createCell(row, CONS_COLUMN_NUMBER, CONS_HEADER, CONS_HEADER_STYLE);
    }

    private void createCell(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    private void setColumnWidth(Sheet sheet) {
        for (int i = 0; i < END_COLUMN_NUMBER; i++) {
            sheet.setColumnWidth(i, WIDTH_SIZE);
        }
    }

    private void createTimeBoxRows(List<TimeBoxResponse> timeBoxes, Sheet sheet) {
        for (int i = 0; i < timeBoxes.size(); i++) {
            createTimeBoxRow(sheet, i + TIME_BOX_FIRST_ROW_NUMBER, timeBoxes.get(i));
        }
    }

    private void createTimeBoxRow(Sheet sheet, int rowNumber, TimeBoxResponse timeBox) {
        Row row = sheet.createRow(rowNumber);
        String timeBoxMessage = messageResolver.resolveBoxMessage(timeBox);
        Stance stance = timeBox.stance();
        createRowByStance(sheet, row, stance, rowNumber, timeBoxMessage);
    }

    private void createRowByStance(Sheet sheet, Row row, Stance stance, int index, String message) {
        switch (stance) {
            case NEUTRAL:
                Cell neturalCell = row.createCell(0);
                neturalCell.setCellStyle(NEUTRAL_STYLE);
                neturalCell.setCellValue(message);
                sheet.addMergedRegion(new CellRangeAddress(index, index, 0, 1)); // A1:B1 셀 병합
                break;

            case PROS:
                setProsAndConsCellstyle(row);
                Cell prosCell = row.getCell(PROS_COLUMN_NUMBER);
                prosCell.setCellValue(message);
                break;

            case CONS:
                setProsAndConsCellstyle(row);
                Cell consCell = row.getCell(CONS_COLUMN_NUMBER);
                consCell.setCellValue(message);
                break;
        }
    }

    private void setProsAndConsCellstyle(Row row) {
        Cell prosCell = row.createCell(PROS_COLUMN_NUMBER);
        prosCell.setCellStyle(PROS_STYLE);
        Cell consCell = row.createCell(CONS_COLUMN_NUMBER);
        consCell.setCellStyle(CONS_STYLE);
    }
}
