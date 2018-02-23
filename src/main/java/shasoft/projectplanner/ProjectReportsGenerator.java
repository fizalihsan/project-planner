package shasoft.projectplanner;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shasoft.projectplanner.domain.ProjectActivity;
import shasoft.projectplanner.domain.Sprint;
import shasoft.projectplanner.graph.Graph;
import shasoft.projectplanner.graph.GraphStyle;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ProjectReportsGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ProjectReportsGenerator.class);
    private SprintReporter sprintReporter;

    public ProjectReportsGenerator(SprintReporter sprintReporter) {
        this.sprintReporter = sprintReporter;
    }

    public void generateVelocityAndReleaseBurndownReports(String outputXlsFileName) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet0 = wb.createSheet("Velocity");
        HSSFSheet sheet1 = wb.createSheet("Burndown");

        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(outputXlsFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        writeVelocitySheet(sheet0);
        writeReleaseBurndownSheet(sheet1);

        try {
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeVelocitySheet(HSSFSheet sheet0) {
        // creating header
        int rowCount = 0;
        HSSFRow row = sheet0.createRow(rowCount++);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int loc = 0;
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Sprint #"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Start Date"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("End Date"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Scoped In"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Scored (Completed)"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Actuals"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Projections"));

        for (Sprint sprint : sprintReporter.getSprints()) {
            row = sheet0.createRow(rowCount++);
            loc = 0;

            logger.info("Writing velocity row for sprintId: " + sprint.getSprintId()
                    + " with meta data: " + sprint.getSprintMetaData());

            row.createCell(loc++).setCellValue(new HSSFRichTextString(sprint.getSprintId()));
            row.createCell(loc++).setCellValue(sdf.format(sprint.getSprintMetaData().getStartDate()));
            row.createCell(loc++).setCellValue(sdf.format(sprint.getSprintMetaData().getEndDate()));
            row.createCell(loc++).setCellValue(sprint.getEffortPointsScoped());
            row.createCell(loc++).setCellValue(sprint.getActualPointsScored());
            row.createCell(loc++).setCellValue(sprint.getSprintMetaData().getProjectActuals());
            row.createCell(loc++).setCellValue(sprint.getSprintMetaData().getProjectProjections());
        }
    }

    private void writeReleaseBurndownSheet(HSSFSheet sheet0) {
        //creating header
        int rowCount = 0;
        HSSFRow row = sheet0.createRow(rowCount++);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int loc = 0;
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Sprint #"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Start Date"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("End Date"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Total Estimate"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Backlog"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Burned"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Ideal Burndown"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("New Items Effort"));
        row.createCell(loc++).setCellValue(new HSSFRichTextString("Re-estimated Effort"));

        // writing starting row (pre-sprints) first to display full estimate on ideal burndown chart
        // The burndown shows burndown as-of end of each sprint. Therefore we will need to add an explicity
        // row to represent start of first sprint with full projected estimate and 0 actuals.

        row = sheet0.createRow(rowCount++);
        loc = 0;
        // no sprint-id, start-date, end-date. Therefore dummy increment
        loc++; loc++; loc++;
        double estimateFromPlan = sprintReporter.getTotalProjectEstimateAsOfThisSprint(sprintReporter.getSprints().get(0));
        row.createCell(loc++).setCellValue(estimateFromPlan);
        row.createCell(loc++).setCellValue(estimateFromPlan); // putting the full estimate as backlog
        row.createCell(loc++).setCellValue(0); // no points completed yet
        row.createCell(loc++).setCellValue(SprintReporter.getTotalProjections(sprintReporter.getSprints())); // full projecction
        row.createCell(loc++).setCellValue(0); // no new items
        row.createCell(loc++).setCellValue(0); // no re-estimated items

        for (Sprint sprint : sprintReporter.getSprints()) {
            double completedPoints = sprintReporter.getTotalCompletedPointsAsOfThisSprint(sprint);
            double totalScope = sprintReporter.getTotalProjectEstimateAsOfThisSprint(sprint);
            double backlog = totalScope - completedPoints;

            double idealBurndown = sprintReporter.getTotalRemainingProjectionsAsOfThisSprint(sprint);
            logger.info("Sprint id = " + sprint.getSprintId() + " and total projections = " + idealBurndown);

            row = sheet0.createRow(rowCount++);
            loc = 0;
            row.createCell(loc++).setCellValue(new HSSFRichTextString(sprint.getSprintId()));
            row.createCell(loc++).setCellValue(sdf.format(sprint.getSprintMetaData().getStartDate()));
            row.createCell(loc++).setCellValue(sdf.format(sprint.getSprintMetaData().getEndDate()));
            row.createCell(loc++).setCellValue(totalScope);
            row.createCell(loc++).setCellValue(backlog);
            row.createCell(loc++).setCellValue(completedPoints);
            row.createCell(loc++).setCellValue(idealBurndown);
            row.createCell(loc++).setCellValue(sprint.getEffortFromNewItems());
            row.createCell(loc++).setCellValue(sprint.getEffortFromUpdatedEstimates());
        }
    }

    /**
     * create dot file
     *
     * @param projectActivities
     * @param outputFileDirectory
     * @param outputFileNameWithoutExtension
     * @param projectActivityTypesInGraph
     * @param sprintId
     * @param isGraphAtEndOfSprint
     * @param outputFormats
     */
    public static void generateGraphs(List<ProjectActivity> projectActivities, String outputFileDirectory
            , String outputFileNameWithoutExtension, List<ProjectActivity.TYPE> projectActivityTypesInGraph
            , String sprintId, boolean isGraphAtEndOfSprint, String... outputFormats) {

        GraphStyle generalStyle = new GraphStyle.Builder().color("white").style("filled").fontSize(8).build();
        GraphStyle nodeStyle = new GraphStyle.Builder().color("gray").style("filled").fontSize(8).build();

        Graph g = new Graph("Data", Graph.TYPE.SUBGRAPH, projectActivities, generalStyle, nodeStyle, sprintId, isGraphAtEndOfSprint);
        if (projectActivityTypesInGraph != null) {
            g.setNodeTypesInGraph(projectActivityTypesInGraph);
        }

        String dotGraph = g.createTopGraph("Top", generalStyle, nodeStyle, g);
        logger.info("dot representation = \n" + dotGraph);

        String dotFileFullPath = outputFileDirectory + File.separator + outputFileNameWithoutExtension + ".dot";
        File dotFile = new File(dotFileFullPath);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dotFile));
            writer.write(dotGraph);
            writer.close();

            for (int i = 0; i < outputFormats.length; i++) {
                String executionCommand = String.format("%sdot -T%s %s -o %s.%s"
                        , Configs.GRAPHVIZ_BIN + File.separator, outputFormats[i], dotFileFullPath
                        , outputFileDirectory + File.separator + outputFileNameWithoutExtension, outputFormats[i]);

                logger.info("Executing command: " + executionCommand);
                Runtime.getRuntime().exec(executionCommand);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
