package shasoft.projectplanner.domain;

import java.util.Date;
import java.util.Objects;

public class SprintMetaData {

    private String sprintId;
    private Date startDate;
    private Date endDate;
    private double projectActuals; // actuals from time booking system
    private double projectProjections; // projectiosn from project allocation systems

    public SprintMetaData(String sprintId, Date startDate, Date endDate
            , Double projectActuals, Double projectProjections) {
        this.sprintId = sprintId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectActuals = projectActuals == null ? 0 : projectActuals;
        this.projectProjections = projectProjections == null ? 0 : projectProjections;
    }

    public String getSprintId() {
        return sprintId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public double getProjectActuals() {
        return projectActuals;
    }

    public double getProjectProjections() {
        return projectProjections;
    }

    @Override
    public String toString() {
        return "SprintMetaData{" +
                "sprintId='" + sprintId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", projectActuals=" + projectActuals +
                ", projectProjections=" + projectProjections +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprintMetaData that = (SprintMetaData) o;
        return Double.compare(that.projectActuals, projectActuals) == 0 &&
                Double.compare(that.projectProjections, projectProjections) == 0 &&
                Objects.equals(sprintId, that.sprintId) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sprintId, startDate, endDate, projectActuals, projectProjections);
    }
}
