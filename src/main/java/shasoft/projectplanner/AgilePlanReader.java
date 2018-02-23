package shasoft.projectplanner;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shasoft.projectplanner.domain.ProjectActivity;
import shasoft.projectplanner.domain.SprintMetaData;

import java.io.FileInputStream;
import java.util.*;

public class AgilePlanReader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static AgilePlanReader instance = null;

    private AgilePlanReader(){};

    public static AgilePlanReader getInstance() {
        if (instance == null) {
            instance = new AgilePlanReader();
        }
        return instance;
    }

    public List<ProjectActivity> parseProjectExcelFile(String taskListExcelPath, String sheetName) throws Exception {
        List<ProjectActivity> projectActivities = new ArrayList<ProjectActivity>();

        FileInputStream fis = new FileInputStream(taskListExcelPath);
        Workbook workbook = null;
        if (taskListExcelPath == null || sheetName == null) {
            logger.error("Parameters cannot be null: taskListExcelPath="
                    + taskListExcelPath + "; sheetName=" + sheetName);
            throw new RuntimeException("Invalid parameters. Cannot be null");
        }
        if (taskListExcelPath.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(fis);
        } else if (taskListExcelPath.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(fis);
        } else {
            throw new RuntimeException("Unexpected file type. Only xls or xlsx are supported");
        }

        Sheet sheet = workbook.getSheet(sheetName);
        int firstRowNumber = sheet.getFirstRowNum();
        ExcelCellValueRetriever excelCellValueRetriever = new ExcelCellValueRetriever();

        logger.info("Total rows in excel file sheet " + sheetName + "=" + sheet.getLastRowNum());
        for (int rowNum = firstRowNumber + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            logger.info("Reading row: " + rowNum);

            int i = 0;
            String id = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            ProjectActivity.TYPE type = ProjectActivity.TYPE.valueOf(
                    excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue().toUpperCase());
            String name = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            String displayName = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            ProjectActivity.EFFORT_UNITS effortUnits = ProjectActivity.EFFORT_UNITS.valueOf(
                    excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue().toUpperCase());
            Double effort = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getDoubleValue();
            String commaSeparatedPredecessorIds = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            String immediateParentIds = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            String commaSeparatedSprintIds = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            String url = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            String sprintWhereItemIsAdded = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            Double originalEffort = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getDoubleValue();
            String commaSeparatedEffortDeltaVsSprint = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            String sprintWhereItemIsClosed = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();

            ProjectActivity projectActivity = new ProjectActivity(id, type, name, displayName, effortUnits, effort
                    , commaSeparatedPredecessorIds, immediateParentIds, commaSeparatedSprintIds, url
                    , sprintWhereItemIsAdded, originalEffort, commaSeparatedEffortDeltaVsSprint
                    , sprintWhereItemIsClosed);
            projectActivities.add(projectActivity);
        }

        return updatePredecessors(projectActivities);
    }

    public Map<String, SprintMetaData> parseSprintMetaDataExcelFile(String taskListExcelPath, String sheetName) throws Exception {
        Map<String, SprintMetaData> sprintIdVsSprintMetaData = new HashMap<>();
        FileInputStream fis = new FileInputStream(taskListExcelPath);
        Workbook workbook = null;
        if (taskListExcelPath == null || sheetName == null) {
            logger.error("Parameters cannot be null: taskListExcelPath="
                    + taskListExcelPath + "; sheetName=" + sheetName);
            throw new RuntimeException("Invalid parameters. Cannot be null");
        }
        if (taskListExcelPath.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(fis);
        } else if (taskListExcelPath.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(fis);
        } else {
            throw new RuntimeException("Unexpected file type. Only xls or xlsx are supported");
        }

        Sheet sheet = workbook.getSheet(sheetName);
        int firstRowNumber = sheet.getFirstRowNum();
        ExcelCellValueRetriever excelCellValueRetriever = new ExcelCellValueRetriever();

        logger.info("Total rows in excel file sheet " + sheetName + "=" + sheet.getLastRowNum());
        for (int rowNum = firstRowNumber + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            logger.info("Reading row: " + rowNum);

            int i = 0;
            String sprintId = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getStringValue();
            logger.info("Reading meta data for sprintId: " + sprintId);
            Date startDate = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getDateValue();
            Date endDate = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getDateValue();
            Double projectActuals = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getDoubleValue();
            Double projectProjections = excelCellValueRetriever.refreshWithCell(row.getCell(i++)).getDoubleValue();

            SprintMetaData metaData = new SprintMetaData(sprintId, startDate, endDate, projectActuals
                    , projectProjections);
            sprintIdVsSprintMetaData.put(sprintId, metaData);
        }

        return sprintIdVsSprintMetaData;
    }

    private List<ProjectActivity> updatePredecessors(List<ProjectActivity> projectActivities) {
        for (int i = 0; i < projectActivities.size(); i++) {
            if (projectActivities.get(i).getImmediateParentId() != null) {
                for (int j = 0; j < projectActivities.size(); j++) {
                    logger.info("Immediate parent id = " + projectActivities.get(i).getImmediateParentId());
                    logger.info("Id of this activity = " + projectActivities.get(j).getId());
                    if (projectActivities.get(i).getImmediateParentId().equals(projectActivities.get(j).getId())) {
                        ProjectActivity immediateParent = projectActivities.get(j);
                        projectActivities.get(i).setImmediateParent(immediateParent);
                        break;
                    }
                }
            }

            List<ProjectActivity> predecessors = new ArrayList<>();
            if (projectActivities.get(i).getPredecessorIds() != null) {
                for (int j = 0; j < projectActivities.size(); j++) {
                    logger.info("Immediate parent id = " + projectActivities.get(i).getImmediateParentId());
                    logger.info("Id of this activity = " + projectActivities.get(j).getId());
                    if (projectActivities.get(i).getPredecessorIds().contains(projectActivities.get(j).getId())) {
                        predecessors.add(projectActivities.get(j));
                    }
                }
            }

            projectActivities.get(i).setPredecessors(predecessors);
        }
        return projectActivities;
    }

    // todo: remove data type check repetitions
    class ExcelCellValueRetriever {
        private Cell cell;
        private Integer cellType;

        public ExcelCellValueRetriever() {}

        public ExcelCellValueRetriever(Cell cell) {
            this.cell = cell;
            cellType = cell == null ? null : cell.getCellType();
        }
        public ExcelCellValueRetriever refreshWithCell(Cell newCell) {
            cell = newCell;
            cellType = cell == null ? null : cell.getCellType();
            return this;
        }
        public String getStringValue() {
            if (cellType == null) {
                return null;
            }
            String value = null;
            switch (cellType) {
                case Cell.CELL_TYPE_BLANK:
                    value = null; //originally set to ""
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    value = new Double(cell.getNumericCellValue()).toString();
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    throw new IllegalArgumentException("Unexpected value in cell. Formula not expected");
                case Cell.CELL_TYPE_BOOLEAN:
                    value = new Boolean(cell.getBooleanCellValue()).toString();
                    break;
                case Cell.CELL_TYPE_ERROR:
                    throw new IllegalArgumentException("Unexpected value in cell. ERROR in excel cell");
            }

            return value;
        }
        public Double getDoubleValue() {
            if (cellType == null) {
                return null;
            }
            Double value = null;
            switch (cellType) {
                case Cell.CELL_TYPE_BLANK:
                    value = null; //originally set to ""
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    value = cell.getNumericCellValue();
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = Double.parseDouble(cell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    throw new IllegalArgumentException("Unexpected value in cell. Formula not expected");
                case Cell.CELL_TYPE_BOOLEAN:
                    throw new IllegalArgumentException("Unexpected value in cell. Boolean not expected");
                case Cell.CELL_TYPE_ERROR:
                    throw new IllegalArgumentException("Unexpected value in cell. ERROR in excel cell");
            }

            return value;
        }

        // todo: implement fully
        public Date getDateValue() {
            return cell.getDateCellValue();
        }
    }
}
