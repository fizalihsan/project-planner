package shasoft.projectplanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shasoft.projectplanner.domain.ProjectActivity;
import shasoft.projectplanner.domain.Sprint;
import shasoft.projectplanner.domain.SprintMetaData;

import java.util.*;

public class SprintReporter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private List<Sprint> sprints = new ArrayList<>();
    private List<ProjectActivity> projectActivities;
    private Map<String, SprintMetaData> sprintIdVsMetaDataMap;

    public SprintReporter(List<ProjectActivity> projectActivities, Map<String, SprintMetaData> sprintIdVsMetaDataMap) {
        this.projectActivities = projectActivities;
        this.sprintIdVsMetaDataMap = sprintIdVsMetaDataMap;
        createSprintBreakdown();
    }

    /**
     * creates sprints and puts activities into them. For each sprint, put the group of activities
     * that were: scoped in, completed, newly added to backlog, estimates updated
     */
    private void createSprintBreakdown() {
        HashMap<String, Sprint> sprintIdSprintMap = new HashMap<>();

        if (sprintIdVsMetaDataMap != null) {
            for (SprintMetaData sprintMetaData : sprintIdVsMetaDataMap.values()) {
                sprintIdSprintMap.put(sprintMetaData.getSprintId(), new Sprint(
                        sprintMetaData.getSprintId(), sprintMetaData));
            }
        }

        logger.info("SprintId map after populating metadata = " + sprintIdSprintMap);

        for (ProjectActivity projectActivity : projectActivities) {
            // activity scoped into sprint
            String sprintId = null;
            List<String> sprintIds = projectActivity.getSprintIds();
            if (sprintIds != null) {
                for (int i = 0; i < sprintIds.size(); i++) {
                    sprintId = sprintIds.get(i);

                    //spurious data
                    if (sprintId == null || sprintId.trim().equals("")) {
                        continue;
                    }
                    if (!sprintIdSprintMap.containsKey(sprintId)) {
                        sprintIdSprintMap.put(sprintId, new Sprint(sprintId, sprintIdVsMetaDataMap.get(sprintId)));
                    }
                    Sprint s = sprintIdSprintMap.get(sprintId);
                    s.addActivityScopedIntoSprint(projectActivity);
                }
            }

            // activity added to backlog during this sprint
            sprintId = projectActivity.getSprintWhereItemIsAdded();
            if (sprintId != null && !sprintId.trim().equals("")) {
                if (!sprintIdSprintMap.containsKey(sprintId)) {
                    sprintIdSprintMap.put(sprintId, new Sprint(sprintId, sprintIdVsMetaDataMap.get(sprintId)));
                }
                Sprint s = sprintIdSprintMap.get(sprintId);
                s.addNewItemToBacklog(projectActivity);
            }

            // activity completed during this sprint
            sprintId = projectActivity.getSprintWhereItemIsClosed();
            if (sprintId != null && !sprintId.trim().equals("")) {
                if (!sprintIdSprintMap.containsKey(sprintId)) {
                    sprintIdSprintMap.put(sprintId, new Sprint(sprintId, sprintIdVsMetaDataMap.get(sprintId)));
                }
                Sprint s = sprintIdSprintMap.get(sprintId);
                s.addActivityCompletedInSprint(projectActivity);
            }

            // activity estimates updated during this sprint
            Map<String, Double> sprintVsDeltaEffortRevision = projectActivity.getSprintVsDeltaEffortRevision();
            if (sprintVsDeltaEffortRevision != null) {
                for (String sprintIdKey : sprintVsDeltaEffortRevision.keySet()) {
                    if (!sprintIdSprintMap.containsKey(sprintIdKey)) {
                        sprintIdSprintMap.put(sprintIdKey, new Sprint(sprintIdKey, sprintIdVsMetaDataMap.get(sprintIdKey)));
                    }
                    Sprint s = sprintIdSprintMap.get(sprintIdKey);
                    s.addUpdatedEstimatesActivity(projectActivity);
                }
            }
        }

        sprints.addAll(sprintIdSprintMap.values());
        logger.info("Adding total sprints: " + sprints.size());
        sprints.sort((Sprint s1, Sprint s2)->new Integer(s1.getSprintId()).compareTo(new Integer(s2.getSprintId())));
    }

    /**
     * Calculated field. Takes in full list of sprints and gives teh project estimate "as of this sprint"
     * It does this by picking up all activities within sprints
     * Expected that the list of all sprints is sorted by sprintId
     *
     * @param thisSprint
     * @return
     */
    public double getTotalProjectEstimateAsOfThisSprint(Sprint thisSprint) {
       double totalEstimate = 0;
       for (Sprint nextSprint : sprints) {
           totalEstimate += nextSprint.getEffortFromNewItems();
           totalEstimate += nextSprint.getEffortFromUpdatedEstimates();

           if (nextSprint.getSprintId().equals(thisSprint.getSprintId())) {
               break;
           }
       }
       return totalEstimate;
    }

    /**
     * Calculated field. Takes in full list of sprints and gives the completed tasks "as of this sprint"
     * It does this by picking all activities within sprints
     * Expected that the list of all sprints is sorted by sprintId
     *
     * @param thisSprint
     * @return
     */
    public double getTotalCompletedPointsAsOfThisSprint(Sprint thisSprint) {
        double totalScoredPoints = 0;
        for (Sprint nextSprint : sprints) {
            totalScoredPoints += nextSprint.getActualPointsScored();

            if (nextSprint.getSprintId().equals(thisSprint.getSprintId())) {
                break;
            }
        }

        return totalScoredPoints;
    }

    /**
     * Calculated field. This gives the "ideal burndown" based on resource projections
     * Expected that the list of all sprints is sorted by SprintId
     * @param thisSprint
     * @return
     */
    public double getTotalRemainingProjectionsAsOfThisSprint(Sprint thisSprint) {
        double remainingProjections = 0;
        for (Sprint nextSprint : sprints) {
            //ignore elapsed sprints
            // for e.g. to calculate this field for sprint 4, sum up projections for sprint 5 and above
            if (Integer.parseInt(nextSprint.getSprintId()) <= Integer.parseInt(thisSprint.getSprintId())) {
                continue;
            }
            remainingProjections += nextSprint.getSprintMetaData().getProjectProjections();
        }

        return remainingProjections;
    }

    /**
     * Calculated field. This gives the full projected figure for the project
     * to act as the starting point for "ideal burndown" chart. This is sum of all resource projections
     * @param allSprintsSorted
     * @return
     */
    public static double getTotalProjections(List<Sprint> allSprintsSorted) {
        double fullProjections = 0;

        for (Sprint nextSprint : allSprintsSorted) {
            fullProjections += nextSprint.getSprintMetaData().getProjectProjections();
        }

        return fullProjections;
    }

    public List<Sprint> getSprints() {
        return sprints;
    }
}
