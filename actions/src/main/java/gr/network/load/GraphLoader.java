package gr.network.load;

import org.neo4j.cypherdsl.core.*;
import static org.neo4j.cypherdsl.core.Cypher.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.network.client.Connection;
import gr.network.domain.Action;

/**
 * Communicates with the Neo4j database
 * to load the starting data to it,
 * provided by the InputReader.
 * @version 1.0
 */
public class GraphLoader {

    private final Logger logger = LoggerFactory.getLogger(GraphLoader.class);

    /** Connection to neo4j */
    private Connection connection;

    /** The user nodes to be loaded */
    private List<String> users;

    /** The course nodes to be loaded */
    private List<String> courses;

    /** The action (user-course) edges to be loaded */
    private List<Action> actions;

    public GraphLoader(List<String> users, List<String> courses, List<Action> actions) {
        // receive the graph to load
        this(users, courses, actions, new Connection());
    }

    public GraphLoader(
        List<String> users,
        List<String> courses,
        List<Action> actions,
        Connection connection
    ) {
        this.users = users;
        this.courses = courses;
        this.actions = actions;
        this.connection = connection;
    }

    /**
     * Load the graph to the database..
     */
    public void load() {
        logger.info("Loading graph...");
        loadNodes();
        loadEdges();
    }

    /**
     * Load the nodes to the database.
     */
    private void loadNodes() {
        logger.info("Loading nodes...");

        String userCreate = toCreateCypher(users, "User");
        String courseCreate = toCreateCypher(courses, "Course");

        logger.info("User Create query: {}", userCreate);
        logger.info("Course Create query: {}", courseCreate);

        connection.getSession().run(userCreate);
        connection.getSession().run(courseCreate);

        logger.info("loaded nodes");
    }

    /**
     * Create a node with the given label and ID.
     * @param label The label of the node.
     * @param id The ID of the node.
     * @return The created node.
     */
    private Node toNode(String label, String id) {
        return node(label).withProperties(
            "id", literalOf(id)
        );
    }

    /**
     * Create a cypher query to create nodes with the given label and IDs.
     * @param nodes The IDs of the nodes.
     * @param nodeLabel The label of the nodes.
     * @return The cypher query to create the nodes.
     */
    private String toCreateCypher(List<String> nodes, String nodeLabel) {
        return Cypher.create(
                nodes.stream()
                .map(nodeId -> toNode(nodeLabel, nodeId))
                .toList()
            ).build().getCypher();
    }

    /**
     * Load the edges to the database.
     * This method assumes that the nodes have already been loaded
     * and it creates the edges between them.
     */
    private void loadEdges() {
        logger.info("Loading edges...");
        for (Action action : actions) {
            loadAction(action);
        }
    }

    /**
     * Load an action to the database.
     * @param action The action to load.
     */
    public void loadAction(Action action) {
        // match the user and course nodes by ID
        Node userNode = node("User").named("u");
        Node courseNode = node("Course").named("c");

        // define the match conditions
        Condition userMatch = userNode.property("id").isEqualTo(literalOf(action.user()));
        Condition courseMatch = courseNode.property("id").isEqualTo(literalOf(action.course()));

        // define the edge properties
        Relationship relationship = userNode
            .relationshipTo(courseNode, action.action())
            .named("r")
            .withProperties(Map.of(
                "timestamp", literalOf(action.timestamp()),
                "feature0", literalOf(action.feature0()),
                "feature1", literalOf(action.feature1()),
                "feature2", literalOf(action.feature2()),
                "feature3", literalOf(action.feature3()),
                "label", literalOf(action.label())
            ));

        // create the cypher query
        String create = Cypher.match(userNode, courseNode)
            .where(userMatch.and(courseMatch))
            .create(relationship)
            .build().getCypher();

        // execute the cypher query
        connection.getSession().run(create);
    }


}
