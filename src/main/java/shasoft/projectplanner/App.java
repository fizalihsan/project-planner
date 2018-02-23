package shasoft.projectplanner;

import shasoft.projectplanner.ProjectBoard;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        ProjectBoard projectBoard = new ProjectBoard("C:\\Users\\shash\\projects\\projectmanagement\\SampleProjectPlan.xlsx"
                ,  "C:\\Users\\shash\\projects\\projectmanagement", "ProjectGraph_temp"
                , "2", "Task", true);

        try {
            projectBoard.buildProjectBoard();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
