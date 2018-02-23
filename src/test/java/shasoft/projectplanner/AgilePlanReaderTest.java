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

public class AgilePlanReaderTest {

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
    public void parseProjectTasksTest() throws Exception {
        AgilePlanReader agilePlanReader = AgilePlanReader.getInstance();
        List<ProjectActivity> projectActivities = agilePlanReader.parseProjectExcelFile(sampleProjectPlanInFileSystem
                , "Tasks");
        assertEquals(16, projectActivities.size());
    }

    @Test
    public void parseProjectSprintsMetaDataTest() throws Exception {
        AgilePlanReader agilePlanReader = AgilePlanReader.getInstance();
        Map<String, SprintMetaData> sprintIdVsSprintMetaData = agilePlanReader.parseSprintMetaDataExcelFile(
                sampleProjectPlanInFileSystem, "Sprints");
        assertEquals(4, sprintIdVsSprintMetaData.size());
        assertEquals(12.0, sprintIdVsSprintMetaData.get("1").getProjectActuals());
    }

}
