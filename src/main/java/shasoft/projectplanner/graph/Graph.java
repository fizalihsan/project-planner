package shasoft.projectplanner.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shasoft.projectplanner.domain.ProjectActivity;

import java.util.Arrays;
import java.util.List;

// TODO: revisit design of this class. It's currently doing some static and some non-static stuff
// Moreover, restrictive - always requires activities to instantiate
public class Graph {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static enum TYPE {GRAPH, SUBGRAPH};

    private String name;
    private TYPE type;
    private List<ProjectActivity> projectActivities;
    private GraphStyle defaultStyle;
    private GraphStyle defaultNodeStyle;
    private String sprintId;
    private boolean isGraphAtEndOfSprint;

    private List<ProjectActivity.TYPE> nodeTypesInGraph = Arrays.asList(ProjectActivity.TYPE.EPIC
            , ProjectActivity.TYPE.STORY);

    public Graph(String name, TYPE type, List<ProjectActivity> projectActivities
            , GraphStyle defaultStyle, GraphStyle defaultNodeStyle, String sprintId, boolean isGraphAtEndOfSprint) {

        this.name = name;
        this.type = type;
        this.projectActivities = projectActivities;
        this.defaultStyle = defaultStyle;
        this.defaultNodeStyle = defaultNodeStyle;
        this.sprintId = sprintId;
        this.isGraphAtEndOfSprint = isGraphAtEndOfSprint;
    }

    public String createTopGraph(String name, GraphStyle defaultStyle, GraphStyle defaultNodeStyle
            , Graph... subGraphs) {
        StringBuffer dot = new StringBuffer();
        dot.append("digraph " + name + " {\n");
        if (defaultStyle != null) { dot.append(defaultStyle.toDotRepresentation()).append("\n"); }
        if (defaultNodeStyle != null) {
            dot.append("node [" + defaultNodeStyle.toDotRepresentationOnSameLine() + "]\n");
        }
        dot.append("\n" + getStandardLegend() + "\n");

        for (int i = 0; i < subGraphs.length; i++) {
            dot.append(subGraphs[i].generateDotGraph());
        }
        dot.append("\n}");
        return dot.toString();
    }

    public String generateDotGraph() {
        StringBuffer dot = new StringBuffer();
        if (type == TYPE.GRAPH) { dot.append("digraph " + name + " {\n"); }
        if (type == TYPE.SUBGRAPH) { dot.append("subgraph cluster_" + name + " {\n"); }
        if (defaultStyle != null) { dot.append(defaultStyle.toDotRepresentation()).append("\n"); }
        if (defaultNodeStyle != null) {
            dot.append("node [" + defaultNodeStyle.toDotRepresentationOnSameLine() + "]\n");
        }
        for (ProjectActivity activity : projectActivities) {
            // TODO: change this to GraphNode
            if (nodeTypesInGraph.contains(activity.getType())) {
                dot.append(activity.getDisplayName());
                if (activity.getUrl() != null && !activity.getUrl().trim().equals("")) {
                    dot.append("[URL=\"" + activity.getUrl() + "\"]");
                }

                GraphStyle nodeStyle = null;
                switch (activity.getType()) {
                    case EPIC:  nodeStyle = new GraphStyle.Builder().style("filled").shape("doubleoctagon").build();
                        break;

                    case STORY: nodeStyle = new GraphStyle.Builder().style("filled").shape("ellipse").build();
                        break;

                    case TASK: nodeStyle = new GraphStyle.Builder().style("diagonals").shape("ellipse").build();
                        break;
                }
                // item completed.
                if (activity.getSprintWhereItemIsClosed() != null
                        && !activity.getSprintWhereItemIsClosed().trim().equals("")) {
                    nodeStyle.setColor("turquoise"); // todo: convert to immutable nodeStyle usage
                }
                // mark task orange if this is end of sprint reporting and task did not complete
                // TODO: allow generating this after the fact - i.e. mark task orange if sprint passed is not the larges in sprints assigned for this task
                else if (sprintId != null && isGraphAtEndOfSprint && activity.getSprintIds() != null
                        && activity.getSprintIds().size() > 0 && activity.getSprintWhereItemIsClosed() == null
                        && activity.getEffort() != null && activity.getEffort() > 0){
                    nodeStyle.setColor("orange");
                } else if (activity.getSprintIds() != null && activity.getSprintIds().size() > 0) {
                    nodeStyle.setColor("palegreen");
                }

                if (nodeStyle != null) { dot.append(" [" + nodeStyle.toDotRepresentationOnSameLine() + "]"); }

                dot.append(";\n");
            }
        }

        for (ProjectActivity activity : projectActivities) {
            // if there is a predecessor, show that. That is a dependency
            // if there is no dependency, just show the parent for graph completeness

            if (nodeTypesInGraph.contains(activity.getType())) {
                if (activity.getPredecessorIds() != null && activity.getPredecessors().size() > 0) {
                    for (ProjectActivity predecessor : activity.getPredecessors()) {
                        dot.append(predecessor.getDisplayName() + "->" + activity.getDisplayName() + "\n");
                    }
                } else if (activity.getImmediateParent() != null) {
                    dot.append(activity.getImmediateParent().getDisplayName() + "->"
                            + activity.getDisplayName() + "[style=normal,color=gold4]\n");
                }
            }
        }
        dot.append("}");
        return dot.toString();
    }

    public String getStandardLegend() {
        String legend = "subgraph cluster_legend { \n"
                + "style=\"filled\";color=\"beige\";fontsize=8\n"
                + "node [style=filled];\n"
                + "InProgress [color=\"palegreen\",fontsize=8]\n"
                + "NotStarted [color=\"gray\",fontsize=8]\n"
                + "Completed     [color=\"turquoise\",fontsize=8]\n"
                + (sprintId != null && isGraphAtEndOfSprint ? "Missed     [color=\"orange\",fontsize=8]\n" : "")
                + "edge[style=invis]\n"
                + "InProgress->NotStarted->Completed"
                + (sprintId != null && isGraphAtEndOfSprint ? "->Missed\n" : "\n")
                + "label = \"Legend\";\n"
                + "}";

        return legend;
    }

    public List<ProjectActivity.TYPE> getNodeTypesInGraph() {
        return nodeTypesInGraph;
    }

    public void setNodeTypesInGraph(List<ProjectActivity.TYPE> nodeTypesInGraph) {
        this.nodeTypesInGraph = nodeTypesInGraph;
    }
}
