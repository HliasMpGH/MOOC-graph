package gr.network.load;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<String> users;

    /** The course nodes to be loaded */
    private Set<String> courses;

    /** The action (user-course) edges to be loaded */
    private Set<Action> actions;

    public GraphLoader(Set<String> users, Set<String> courses, Set<Action> actions) {
        // receive the graph to load
        this(users, courses, actions, new Connection());
    }

    public GraphLoader(
        Set<String> users,
        Set<String> courses,
        Set<Action> actions,
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

        // load users in batches
        loadNodesInBatches(users, "User", 5000);
        // load courses in batches
        loadNodesInBatches(courses, "Course", 5000);

        logger.info("loaded nodes");
    }

    /**
     * Load the a set of nodes into the database
     * as batches. Loading them all by once would be inefficient.
     */
    private void loadNodesInBatches(Set<String> nodeIds, String nodeLabel, int batchSize) {
        // convert to list to split into batches
        List<String> nodeList = new ArrayList<>(nodeIds);

        for (int i = 0; i < nodeList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, nodeList.size());
            List<String> batch = nodeList.subList(i, endIndex);

            String cypher = String.format("UNWIND $nodeIds AS nodeId MERGE (:%s {id: nodeId})", nodeLabel);
            connection.getSession().run(cypher, Map.of("nodeIds", batch));

            logger.info("Loaded {} {} nodes ({}/{})",
                    batch.size(), nodeLabel, endIndex, nodeList.size());
        }
    }

    /**
     * Load the edges to the database.
     * This method assumes that the nodes have already been loaded
     * and it creates the edges between them.
     */
    private void loadEdges() {
        logger.info("Loading edges...");

        // load actions in btatches
        loadEdgesInBatches(actions, 5000);

        logger.info("loaded edges");
    }

    /**
     * Load the a set of edges into the database
     * as batches. Loading them all by once would be inefficient.
     */
    private void loadEdgesInBatches(Set<Action> actions, int batchSize) {
        List<Action> actionList = new ArrayList<>(actions);

        for (int i = 0; i < actionList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, actionList.size());
            List<Action> batch = actionList.subList(i, endIndex);

            // Convert to maps for parameterized query
            List<Map<String, Object>> actionMaps = batch.stream()
                .map(this::actionToMap)
                .collect(Collectors.toList());

            String cypher = """
                    UNWIND $actions AS action
                    MATCH (u:User {id: action.user})
                    MATCH (c:Course {id: action.course})
                    CREATE (u)-[:ACTION {
                        action: action.action,
                        timestamp: action.timestamp,
                        feature0: action.feature0,
                        feature1: action.feature1,
                        feature2: action.feature2,
                        feature3: action.feature3,
                        label: action.label
                    }]->(c)
                    """;

            connection.getSession().run(cypher, Map.of("actions", actionMaps));

            logger.info("Loaded {} edges ({}/{})",
                    batch.size(), endIndex, actionList.size());
        }
    }

    /**
     * Transform an action to a map
     * to be used in the cypher query.
     */
    private Map<String, Object> actionToMap(Action action) {
        return Map.of(
            "user", action.getUser(),
            "course", action.getCourse(),
            "action", action.getAction(),
            "timestamp", action.getTimestamp(),
            "feature0", action.getFeature0(),
            "feature1", action.getFeature1(),
            "feature2", action.getFeature2(),
            "feature3", action.getFeature3(),
            "label", action.getLabel()
        );
    }
}
