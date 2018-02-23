package shasoft.projectplanner.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Sprint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String sprintId;
    private double effortPointsScoped; // we will only deal with days without making any assumptions
    private double actualPointsScored;
    private List<ProjectActivity> newItemsToBacklog;
    private List<ProjectActivity> updatedEstimatesActivities;
    private List<ProjectActivity> activitiesScopedIn;
    private List<ProjectActivity> activitiesCompleted;
    private double effortFromNewItems;
    private double effortFromUpdatedEstimates;
    private SprintMetaData sprintMetaData;

    public Sprint(String sprintId, SprintMetaData sprintMetaData) {
        this.sprintId = sprintId;
        this.sprintMetaData = sprintMetaData;
    }

    public void addActivityScopedIntoSprint(ProjectActivity activityScopedIn) {
        if (activitiesScopedIn == null) {
            activitiesScopedIn = new ArrayList<>();
        }
        if (activitiesScopedIn.contains(activityScopedIn)) {
            logger.info("Item already scoped in sprint. Not adding again." + activityScopedIn);
            return;
        }

        activitiesScopedIn.add(activityScopedIn);
        // using method "getEffortAsOfSprint" for an as-of-view of the effort at the time of this sprint
        Double effortAsOfThisSprint = activityScopedIn.getEffortAsOfSprint(sprintId);
        effortPointsScoped += effortAsOfThisSprint == null ? 0 : effortAsOfThisSprint;
    }

    public void addActivityCompletedInSprint(ProjectActivity activityCompleted) {
        if (activitiesCompleted == null) {
            activitiesCompleted = new ArrayList<>();
        }
        if (activitiesCompleted.contains(activityCompleted)) {
            logger.info("Item already marked complete in sprint. Not adding again." + activityCompleted);
            return;
        }

        activitiesCompleted.add(activityCompleted);
        // effort is the right field to pick here. Assumption is that activity effort is not updated
        // once item is closed. Therefore no need to use "getEffortAsOfSprint" method
        actualPointsScored += activityCompleted.getEffort() == null ? 0 : activityCompleted.getEffort();
    }

    public void addNewItemToBacklog(ProjectActivity oneNewItemToBacklog) {
        if (newItemsToBacklog == null) {
            newItemsToBacklog = new ArrayList<>();
        }
        if (newItemsToBacklog.contains(oneNewItemToBacklog)) {
            logger.info("Item already present in backlog. Not adding again: " + newItemsToBacklog);
            return;
        }

        newItemsToBacklog.add(oneNewItemToBacklog);
        effortFromNewItems += oneNewItemToBacklog.getOriginalEffort() == null ? 0
                : oneNewItemToBacklog.getOriginalEffort();
    }

    public void addUpdatedEstimatesActivity(ProjectActivity oneUpdatedItemToBacklog) {
        if (updatedEstimatesActivities == null) {
            updatedEstimatesActivities = new ArrayList<>();
        }
        if (updatedEstimatesActivities.contains(oneUpdatedItemToBacklog)) {
            logger.info("Item already present in updated list in sprint. Not adding again: " + oneUpdatedItemToBacklog);
            return;
        }

        updatedEstimatesActivities.add(oneUpdatedItemToBacklog);
        Double d = oneUpdatedItemToBacklog.getSprintVsDeltaEffortRevision().get(sprintId);
        effortFromUpdatedEstimates += d == null ? 0 : d;
    }

    public String getSprintId() {
        return sprintId;
    }

    public double getEffortPointsScoped() {
        return effortPointsScoped;
    }

    public double getActualPointsScored() {
        return actualPointsScored;
    }

    public List<ProjectActivity> getNewItemsToBacklog() {
        return newItemsToBacklog;
    }

    public List<ProjectActivity> getUpdatedEstimatesActivities() {
        return updatedEstimatesActivities;
    }

    public List<ProjectActivity> getActivitiesScopedIn() {
        return activitiesScopedIn;
    }

    public List<ProjectActivity> getActivitiesCompleted() {
        return activitiesCompleted;
    }

    public double getEffortFromNewItems() {
        return effortFromNewItems;
    }

    public double getEffortFromUpdatedEstimates() {
        return effortFromUpdatedEstimates;
    }

    public SprintMetaData getSprintMetaData() {
        return sprintMetaData;
    }

    @Override
    public String toString() {
        return "Sprint{" +
                "sprintId='" + sprintId + '\'' +
                ", effortPointsScoped=" + effortPointsScoped +
                ", actualPointsScored=" + actualPointsScored +
                ", newItemsToBacklog=" + newItemsToBacklog +
                ", updatedEstimatesActivities=" + updatedEstimatesActivities +
                ", activitiesScopedIn=" + activitiesScopedIn +
                ", activitiesCompleted=" + activitiesCompleted +
                ", effortFromNewItems=" + effortFromNewItems +
                ", effortFromUpdatedEstimates=" + effortFromUpdatedEstimates +
                ", sprintMetaData=" + sprintMetaData +
                '}';
    }
}
