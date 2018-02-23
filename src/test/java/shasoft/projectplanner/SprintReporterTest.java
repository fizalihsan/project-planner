package shasoft.projectplanner;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shasoft.projectplanner.domain.ProjectActivity;
import shasoft.projectplanner.domain.SprintMetaData;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class SprintReporterTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String SAMPLE_PROJECT_PLAN_IN_CLASSPATH = "samples/SampleProjectPlan.xlsx";
    private static String sampleProjectPlanInFileSystem = null;

    @Before
    public void setUp() throws Exception {

        // Copy project plan from classpath into temp directory.
        URL projectPlanURL = this.getClass().getClassLoader().getResource(SAMPLE_PROJECT_PLAN_IN_CLASSPATH);
        sampleProjectPlanInFileSystem = System.getProperty("java.io.tmpdir") + "temp_plan.xlsx";
        logger.info("Copying file from resource: " + projectPlanURL
                + " into filesystem path: " + sampleProjectPlanInFileSystem);
        File tempFile = new File(sampleProjectPlanInFileSystem);
        FileUtils.copyURLToFile(projectPlanURL, tempFile);
    }

    @After
    public void tearDown() throws Exception {
        logger.info("Deleting temporary file: " + sampleProjectPlanInFileSystem);
        new File(sampleProjectPlanInFileSystem).delete();
    }

    @Test
    public void createSprintBreakdownTest() throws Exception {
        AgilePlanReader agilePlanReader = AgilePlanReader.getInstance();
        List<ProjectActivity> projectActivities = agilePlanReader.parseProjectExcelFile(sampleProjectPlanInFileSystem
                , "Tasks");
        Map<String, SprintMetaData> sprintIdVsMetaDataMap = agilePlanReader.parseSprintMetaDataExcelFile(
                sampleProjectPlanInFileSystem, "Sprints");
        SprintReporter sprintReporter = new SprintReporter(projectActivities, sprintIdVsMetaDataMap);
        assertEquals(40.0, sprintReporter.getTotalProjections(sprintReporter.getSprints()));
        // as of sprint 0
        assertEquals(60.0, sprintReporter.getTotalProjectEstimateAsOfThisSprint(sprintReporter.getSprints().get(0)));
        assertEquals(10.0, sprintReporter.getTotalCompletedPointsAsOfThisSprint(sprintReporter.getSprints().get(0)));
        assertEquals(30.0, sprintReporter.getTotalRemainingProjectionsAsOfThisSprint(sprintReporter.getSprints().get(0)));
        // as of sprint 1
        assertEquals(66.0, sprintReporter.getTotalProjectEstimateAsOfThisSprint(sprintReporter.getSprints().get(1)));
        assertEquals(18.0, sprintReporter.getTotalCompletedPointsAsOfThisSprint(sprintReporter.getSprints().get(1)));
        assertEquals(20.0, sprintReporter.getTotalRemainingProjectionsAsOfThisSprint(sprintReporter.getSprints().get(1)));
        // as of sprint 2
        assertEquals(66.0, sprintReporter.getTotalProjectEstimateAsOfThisSprint(sprintReporter.getSprints().get(2)));
        assertEquals(33.0, sprintReporter.getTotalCompletedPointsAsOfThisSprint(sprintReporter.getSprints().get(2)));
        assertEquals(10.0, sprintReporter.getTotalRemainingProjectionsAsOfThisSprint(sprintReporter.getSprints().get(2)));
    }
}
