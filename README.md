# A lightweight Scrum management tool

## Motivation and Features
* Keeps backlogs simple, clean and maintainable. Combines ideas from Scrum and Pert analysis.
    * Decouple project management from tools meant for issue tracking / code commits (e.g. Jira).
    * Manage documentation links (wiki, Jira, etc.) for stories / tasks within the plan
    * Maintain hierarchy of tasks (epic, story, task) as well as dependencies.
* View overall project story map including dependencies and hierarchy. Create this map at the level of epics and stories for large projects.
* Project documentation is embedded in the story map (nodes are hyperlinks).
* View tasks per sprint and highlight tasks planned but not completed for past sprints.
* Measure, measure, measure...
    * Standard velocity and burndown
    * Changes to project scope per sprint. Projected burndown beyond current sprint.
    * Velocity not just planned vs. completed but also against metadata from time booking systems.

### Story Map
![](src/main/resources/samples/ProjectGraph.png?raw=true)
### Sprint Map
![](src/main/resources/samples/ProjectGraph_Sprint2.png?raw=true)
### Burndown
![](src/main/resources/samples/Burndown.png?raw=true)
### Velocity
![](src/main/resources/samples/Velocity.png?raw=true)
![](src/main/resources/samples/Velocity_detailed.png?raw=true)

## Getting Started

The project runs as a standalone Java application. Requires project plan to be provided as an Excel spreadsheet.
A sample plan is under resources/samples

### Prerequisites

[Graphviz](https://www.graphviz.org) installed locally.

