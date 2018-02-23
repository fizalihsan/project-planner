package shasoft.projectplanner;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) {

        // pull project plan from test resources. This will in reality be from local filesystem
        // Copy project plan from classpath into temp directory.
        String sampleProjectPlanInClasspath = "samples/SampleProjectPlan.xlsx";
        String tempDir = System.getProperty("java.io.tmpdir");
        URL projectPlanURL = App.class.getClassLoader().getResource(sampleProjectPlanInClasspath);
        String sampleProjectPlanInFileSystem = tempDir + "temp_plan.xlsx";
        logger.info("Copying file from resource: " + projectPlanURL
                + " into filesystem path: " + sampleProjectPlanInFileSystem);
        File tempFile = new File(sampleProjectPlanInFileSystem);
        try {
            FileUtils.copyURLToFile(projectPlanURL, tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ProjectBoard projectBoard = new ProjectBoard(sampleProjectPlanInFileSystem
                ,  tempDir, "ProjectGraph"
                , "2", "Task", true);
        try {
            projectBoard.buildProjectBoard();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
