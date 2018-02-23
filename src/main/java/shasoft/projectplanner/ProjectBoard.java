package shasoft.projectplanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shasoft.projectplanner.domain.ProjectActivity;
import shasoft.projectplanner.domain.Sprint;
import shasoft.projectplanner.domain.SprintMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProjectBoard {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String taskListExcelPath;
    private String outputDirectory;
    private String outputFileNamePrefix;
    private String currentSprint;
    private String displayProjectGraphLevel;
    private boolean isEndOfSprint;

    public ProjectBoard(String taskListExcelPath, String outputDirectory, String outputFileNamePrefix
            , String currentSprint, String displayProjectGraphLevel, boolean isEndOfSprint) {
        this.taskListExcelPath = taskListExcelPath;
        this.outputDirectory = outputDirectory;
        this.outputFileNamePrefix = outputFileNamePrefix;
        this.currentSprint = currentSprint;
        this.displayProjectGraphLevel = displayProjectGraphLevel;
        this.isEndOfSprint = isEndOfSprint;
    }

    public void buildProjectBoard() throws Exception {
        if (taskListExcelPath == null) {
            throw new IllegalArgumentException("Project file location not provided");
        }

        List<ProjectActivity> projectActivities;
        Map<String, SprintMetaData> sprintIdVsMetaDataMap;
        projectActivities = AgilePlanReader.getInstance().parseProjectExcelFile(taskListExcelPath, "Tasks");
        sprintIdVsMetaDataMap = AgilePlanReader.getInstance().parseSprintMetaDataExcelFile(taskListExcelPath, "Sprints");

        SprintReporter sprintReporter = new SprintReporter(projectActivities, sprintIdVsMetaDataMap);
        logger.info("Total sprints detected = " + sprintReporter.getSprints().size());

        ProjectReportsGenerator reportsGenerator = new ProjectReportsGenerator(sprintReporter);
        reportsGenerator.generateVelocityAndReleaseBurndownReports(outputDirectory + File.separator + outputFileNamePrefix + "_metrics.xls");
        if (currentSprint != null && !currentSprint.trim().equals("")) {
            Sprint chosenSprint = null;
            for (Sprint s : sprintReporter.getSprints()) {
                if (s.getSprintId().equals(currentSprint)) {
                    chosenSprint = s;
                }
            }
            if (chosenSprint != null) {
                ProjectReportsGenerator.generateGraphs(
                        chosenSprint.getActivitiesScopedIn(), outputDirectory, outputFileNamePrefix + "_Sprint" + currentSprint
                        , Arrays.asList(ProjectActivity.TYPE.EPIC, ProjectActivity.TYPE.STORY, ProjectActivity.TYPE.TASK)
                        , chosenSprint.getSprintId(), isEndOfSprint, "svg", "png");
            }
        }
        ProjectActivity.TYPE projectGraphLevel = ProjectActivity.TYPE.valueOf(displayProjectGraphLevel.toUpperCase());
        List<ProjectActivity.TYPE> allChosenLevels = new ArrayList<>();
        for (ProjectActivity.TYPE l : new ProjectActivity.TYPE[]{ProjectActivity.TYPE.EPIC, ProjectActivity.TYPE.STORY, ProjectActivity.TYPE.TASK}) {
            // add all levels that are greater than or equal to hierarchy of chosen level for display
            // for instance, if chosen level is story, pick up epic and story
            allChosenLevels.add(l);
            if (projectGraphLevel == l) {
                break;
            }
        }
        ProjectReportsGenerator.generateGraphs(projectActivities, outputDirectory, outputFileNamePrefix
                , allChosenLevels, null, false, "svg", "png");
    }
}
