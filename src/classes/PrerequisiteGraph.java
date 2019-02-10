package classes;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class PrerequisiteGraph {

    DefaultDirectedGraph graph;

    public PrerequisiteGraph() {
        graph = new DefaultDirectedGraph<Course, Course>(Course.class);
    }

}
