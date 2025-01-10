package com.debatetimer.domain.exporter;

import static org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT;
import static org.apache.poi.ss.usermodel.IndexedColors.BLACK;
import static org.apache.poi.ss.usermodel.IndexedColors.CORAL;
import static org.apache.poi.ss.usermodel.IndexedColors.CORNFLOWER_BLUE;
import static org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_CORNFLOWER_BLUE;
import static org.apache.poi.ss.usermodel.IndexedColors.LIGHT_YELLOW;
import static org.apache.poi.ss.usermodel.IndexedColors.ROSE;
import static org.apache.poi.ss.usermodel.IndexedColors.WHITE;

import com.debatetimer.domain.Stance;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBoxes;
import com.debatetimer.view.exporter.ParliamentaryTableExportMessageResolver;
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
        NORMAL = createFont(workbook, false, BLACK);
        BOLD = createFont(workbook, true, BLACK);
        BOLD_AND_WHITE = createFont(workbook, true, WHITE);
    }

    private static void initializeStyle(Workbook workbook) {
        HEADER_STYLE = createGroundColor(workbook, LIGHT_YELLOW, BOLD, LEFT);
        HEADER_CELL_STYLE = createGroundColor(workbook, LIGHT_YELLOW, NORMAL, LEFT);
        PROS_HEADER_STYLE = createGroundColor(workbook, CORNFLOWER_BLUE, BOLD, CENTER);
        CONS_HEADER_STYLE = createGroundColor(workbook, CORAL, BOLD, CENTER);
        PROS_STYLE = createGroundColor(workbook, LIGHT_CORNFLOWER_BLUE, NORMAL, CENTER);
        CONS_STYLE = createGroundColor(workbook, ROSE, NORMAL, CENTER);
        NEUTRAL_STYLE = createGroundColor(workbook, GREY_50_PERCENT, BOLD_AND_WHITE, CENTER);
    }

    public Workbook export(ParliamentaryTable table, ParliamentaryTimeBoxes timeBoxes) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(table.getName());
        initializeFont(workbook);
        initializeStyle(workbook);

        createHeader(sheet, NAME_HEADER_ROW_NUMBER, NAME_HEADER, table.getName());
        createHeader(sheet, TYPE_HEADER_ROW_NUMBER, TYPE_HEADER, PARLIAMENTARY_HEADER_BODY);
        createHeader(sheet, AGENDA_HEADER_ROW_NUMBER, AGENDA_HEADER, table.getAgenda());

        createTableHeader(sheet, TABLE_HEADER_ROW_NUMBER);
        createTimeBoxRows(timeBoxes, sheet);
        setColumnWidth(sheet);
        return workbook;
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

    private void createTableHeader(Sheet sheet, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        createCell(row, 0, PROS_HEADER, PROS_HEADER_STYLE);
        createCell(row, 1, CONS_HEADER, CONS_HEADER_STYLE);
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

    private void createTimeBoxRows(ParliamentaryTimeBoxes timeBoxes, Sheet sheet) {
        List<ParliamentaryTimeBox> timeBoxeList = timeBoxes.getTimeBoxes();
        for (int i = 0; i < timeBoxeList.size(); i++) {
            createTimeBoxRow(sheet, i + TIME_BOX_FIRST_ROW_NUMBER, timeBoxeList.get(i));
        }
    }

    private void createTimeBoxRow(Sheet sheet, int rowNumber, ParliamentaryTimeBox timeBox) {
        Row row = sheet.createRow(rowNumber);
        String timeBoxMessage = messageResolver.resolveBoxMessage(timeBox);
        if (timeBox.getStance() == Stance.NEUTRAL) {
            createNeutralRow(sheet, row, rowNumber, timeBoxMessage);
            return;
        }
        createProsOrConsRow(row, timeBox.getStance(), timeBoxMessage);
    }

    private void createNeutralRow(Sheet sheet, Row row, int index, String message) {
        Cell neturalCell = row.createCell(0);
        neturalCell.setCellStyle(NEUTRAL_STYLE);
        neturalCell.setCellValue(message);
        sheet.addMergedRegion(new CellRangeAddress(index, index, 0, 1)); // A1:B1 셀 병합
    }

    private void createProsOrConsRow(Row row, Stance stance, String message) {
        Cell prosCell = row.createCell(0);
        prosCell.setCellStyle(PROS_STYLE);

        Cell consCell = row.createCell(1);
        consCell.setCellStyle(CONS_STYLE);

        if (stance == Stance.PROS) {
            prosCell.setCellValue(message);
        }

        if (stance == Stance.CONS) {
            consCell.setCellValue(message);
        }
    }
}
