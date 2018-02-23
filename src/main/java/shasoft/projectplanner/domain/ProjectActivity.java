package shasoft.projectplanner.domain;

import java.util.*;

public class ProjectActivity {

    public enum TYPE {EPIC, STORY, TASK};
    public enum EFFORT_UNITS {DAYS, STORYPOINTS};

    private String id;
    private TYPE type;
    private String name;
    private String displayName;
    private EFFORT_UNITS effortUnits;
    private Double effort;
    private List<String> predecessorIds;
    private List<ProjectActivity> predecessors;
    private String immediateParentId;
    private ProjectActivity immediateParent;
    private List<String> sprintIds;
    private String url;
    private String sprintWhereItemIsAdded;
    private Double originalEffort;
    private Map<String, Double> sprintVsDeltaEffortRevision;
    private String sprintWhereItemIsClosed;

    public ProjectActivity(String id, TYPE type, String name
            , String displayName, EFFORT_UNITS effortUnits, Double effort
            , String commaSeparatedPredecessorIds, String immediateParentId
            , String commaSeparatedSprintIds, String url, String sprintWhereItemIsAdded
            , Double originalEffort, String commaSeparatedEffortDeltaVsSprint
            , String sprintWhereItemIsClosed) {

        this.id = id;
        this.type = type;
        this.name = name;
        this.displayName = displayName;
        this.effortUnits = effortUnits;
        this.effort = effort;
        this.sprintIds = commaSeparatedSprintIds != null
                ? new ArrayList<String>(Arrays.asList(commaSeparatedSprintIds.split(",")))
                : null;
        this.url = url;
        this.sprintWhereItemIsAdded = sprintWhereItemIsAdded;
        this.originalEffort = originalEffort;
        this.sprintWhereItemIsClosed = sprintWhereItemIsClosed;

        predecessorIds = commaSeparatedPredecessorIds != null
                ? new ArrayList<String>(Arrays.asList(commaSeparatedPredecessorIds.split(",")))
                : null;

        this.immediateParentId = immediateParentId;

        if (commaSeparatedEffortDeltaVsSprint != null && !commaSeparatedEffortDeltaVsSprint.trim().equals("")) {

            //expected format: 3:#2,2:#5,-3:#6 (3 days during sprint 2, 2 days during sprint 5, etc.
            sprintVsDeltaEffortRevision = new HashMap<String, Double>();

            List<String> tempList = new ArrayList<String>(Arrays.asList(commaSeparatedEffortDeltaVsSprint.split(",")));
            for (String sprintAndEffort : tempList) {
                String value = sprintAndEffort.substring(0, sprintAndEffort.indexOf(":")); //effort
                String key = sprintAndEffort.substring(sprintAndEffort.indexOf("#") + 1); //sprintId
                sprintVsDeltaEffortRevision.put(key, new Double(value));
            }
        }
    }

    /**
     * Returns effort as-of a given sprint.
     * So any upates to effort estimate after the sprint are disregarded
     * @param asOfSprintId
     * @return
     */
    public Double getEffortAsOfSprint(String asOfSprintId) {
        if (effort == null) { // there is no effort against this item
            return null;
        }
        double asOfEffort = effort;

        if (sprintVsDeltaEffortRevision != null) {
            // subtract any update doen to estimate after the sprintId passed into this method
            for (String deltaSprintId : sprintVsDeltaEffortRevision.keySet()) {
                if (Integer.parseInt(deltaSprintId) > Integer.parseInt(asOfSprintId)) {
                    asOfEffort  -= sprintVsDeltaEffortRevision.get(deltaSprintId);
                }
            }
        }
        // requesting sprint is before the item was added
        if (Integer.parseInt(sprintWhereItemIsAdded) > Integer.parseInt(asOfSprintId)) {
            asOfEffort -= originalEffort;
        }
        return asOfEffort;
    }

    public String getId() {
        return id;
    }

    public TYPE getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EFFORT_UNITS getEffortUnits() {
        return effortUnits;
    }

    public Double getEffort() {
        return effort;
    }

    public List<String> getPredecessorIds() {
        return predecessorIds;
    }

    public List<ProjectActivity> getPredecessors() {
        return predecessors;
    }

    public String getImmediateParentId() {
        return immediateParentId;
    }

    public ProjectActivity getImmediateParent() {
        return immediateParent;
    }

    public List<String> getSprintIds() {
        return sprintIds;
    }

    public String getUrl() {
        return url;
    }

    public String getSprintWhereItemIsAdded() {
        return sprintWhereItemIsAdded;
    }

    public Double getOriginalEffort() {
        return originalEffort;
    }

    public Map<String, Double> getSprintVsDeltaEffortRevision() {
        return sprintVsDeltaEffortRevision;
    }

    public String getSprintWhereItemIsClosed() {
        return sprintWhereItemIsClosed;
    }

    public void setPredecessors(List<ProjectActivity> predecessors) {
        this.predecessors = predecessors;
    }

    public void setImmediateParent(ProjectActivity immediateParent) {
        this.immediateParent = immediateParent;
    }

    @Override
    public String toString() {
        return "ProjectActivity{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", effortUnits=" + effortUnits +
                ", effort=" + effort +
                ", predecessorIds=" + predecessorIds +
                ", predecessors=" + predecessors +
                ", immediateParentId='" + immediateParentId + '\'' +
                ", immediateParent=" + immediateParent +
                ", sprintIds=" + sprintIds +
                ", url='" + url + '\'' +
                ", sprintWhereItemIsAdded='" + sprintWhereItemIsAdded + '\'' +
                ", originalEffort=" + originalEffort +
                ", sprintVsDeltaEffortRevision=" + sprintVsDeltaEffortRevision +
                ", sprintWhereItemIsClosed='" + sprintWhereItemIsClosed + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectActivity that = (ProjectActivity) o;
        return Objects.equals(id, that.id) &&
                type == that.type &&
                Objects.equals(name, that.name) &&
                Objects.equals(displayName, that.displayName) &&
                effortUnits == that.effortUnits &&
                Objects.equals(effort, that.effort) &&
                Objects.equals(predecessorIds, that.predecessorIds) &&
                Objects.equals(predecessors, that.predecessors) &&
                Objects.equals(immediateParentId, that.immediateParentId) &&
                Objects.equals(immediateParent, that.immediateParent) &&
                Objects.equals(sprintIds, that.sprintIds) &&
                Objects.equals(url, that.url) &&
                Objects.equals(sprintWhereItemIsAdded, that.sprintWhereItemIsAdded) &&
                Objects.equals(originalEffort, that.originalEffort) &&
                Objects.equals(sprintVsDeltaEffortRevision, that.sprintVsDeltaEffortRevision) &&
                Objects.equals(sprintWhereItemIsClosed, that.sprintWhereItemIsClosed);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type, name, displayName, effortUnits, effort, predecessorIds, predecessors, immediateParentId, immediateParent, sprintIds, url, sprintWhereItemIsAdded, originalEffort, sprintVsDeltaEffortRevision, sprintWhereItemIsClosed);
    }
}
