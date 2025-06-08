package gr.network.read;

import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.network.client.Neo4jConnection;

import java.util.Scanner;

/**
 * Communicates with the Neo4j database
 * to query data from it.
 * @version 1.1 (optimized with index-based queries)
 */
public class GraphReader {

    private final Logger logger = LoggerFactory.getLogger(GraphReader.class);
    private final Session session;

    private Scanner scanner;

    public GraphReader(Neo4jConnection connection, Scanner scanner) {
        this.session = connection.getSession();
        this.scanner = scanner;
    }

    /**
     * Executes the query specified by alias.
     * If queryName is null or empty, display menu and let user choose.
     * @return execution time in milliseconds
     */
    public double run(String queryName) {
        if (queryName == null || queryName.isBlank()) {
            printAvailableQueries();
            System.out.print("Choose one query to run: ");
            queryName = scanner.nextLine();
        }

        logger.info("Running query '{}'.", queryName);

        double totalTime = 0.0;
        switch (queryName.toLowerCase()) {
            case "graphsize" -> totalTime = graphSize();
            case "actionstargetsofuser" -> {
                // take user input for the user id
                String userId = "";
                do {
                    System.out.print("Provide a user id: ");
                    userId = scanner.nextLine().trim();
                } while (userId.isBlank());


                totalTime = actionsTargetsOfUser(userId);
            }
            case "actionsperuser" -> totalTime = actionsPerUser();
            case "toptargets" -> totalTime = topTargets();
            case "avgactions" -> totalTime = avgActionsPerUser();
            case "positivefeature2" -> totalTime = userTargetWithPositiveFeature2();
            case "label1pertarget" -> totalTime = labelOnePerTarget();
            default -> System.out.println("Unknown query: " + queryName);
        }
        return totalTime;
    }

    /**
     * (2) Count of users, courses and actions
     * @return total execution time in milliseconds
     */
    private double graphSize() {
        System.out.println("gaphsize");
        String userCountCypher = getNodeCountCypher("User");
        String courseCountCypher = getNodeCountCypher("Course");
        String actionCountCypher = getRelationshipCountCypher(
            "User",
            "ACTION" ,
            "Course"
        );

        double time1 = executeAndPrint("Total Users Count", userCountCypher);
        double time2 = executeAndPrint("Total Courses Count", courseCountCypher);
        double time3 = executeAndPrint("Total Actions Count", actionCountCypher);
        return time1 + time2 + time3;
    }

    private String getNodeCountCypher(String label) {
        return String.format(
            "MATCH (n:%s) RETURN count(n) as %sCount",
            label,
            label.toLowerCase()
        );
    }

    private String getRelationshipCountCypher(String node1Label, String relationshipLabel, String node2Label) {
        return String.format(
            "MATCH (n1:%s)-[r:%s]->(n2:%s) RETURN count(r) as %sCount",
            node1Label,
            relationshipLabel,
            node2Label,
            relationshipLabel.toLowerCase()
        );
    }

    /**
     * (3) All actions and targets of a user
     * @return execution time in milliseconds
     */
    private double actionsTargetsOfUser(String userID) {
        System.out.println("actionsTargetsOfUser of id " + userID);

        String actionsTargetCypher = String.format(
        """
            MATCH (:User {id: '%s'})
            -[action:ACTION]->
            (course:Course)
            return action.action as actionId, course.id as courseID
            ORDER BY actionId
            LIMIT 10
        """, userID);

        return executeAndPrint("Actions and Targets of user " + userID, actionsTargetCypher);
    }

    /**
     * (4) Action counts per user
     * @return execution time in milliseconds
     */
    private double actionsPerUser() {
        System.out.println("actionsPerUser");

        String actionsTargetCypher = String.format(
        """
            MATCH (user:User)-[action:ACTION]->()
            RETURN user.id as userId, count(action) as totalActions
            ORDER BY userId
            LIMIT 10
        """);

        return executeAndPrint("Action Counts per User", actionsTargetCypher);
    }

    /**
     * (5) Top 10 target courses by number of unique users who performed actions
     * @return execution time in milliseconds
     */
    private double topTargets() {
        String cypher = """
        MATCH (t:Course)
        MATCH (u:User)-[:ACTION]->(t)
        RETURN t.id AS targetID, COUNT(DISTINCT u) as userCount
        ORDER BY userCount DESC
        LIMIT 10
    """;

        return executeAndPrint("Top 10 targets by distinct users", cypher);
    }

    /**
     * (6) Count the average number of actions per user
     * @return execution time in milliseconds
     */
    private double avgActionsPerUser() {
        String cypher = """
            MATCH (u:User)-[r:ACTION]->()
            WITH u, count(r) AS total
            RETURN avg(total) AS avgActionsPerUser
            """;

        return executeAndPrint("Average actions per user", cypher);
    }

    /**
     * (7) Show the userID and the targetID, if the action has positive Feature2
     * @return execution time in milliseconds
     */
    private double userTargetWithPositiveFeature2() {
        String cypher = """
            MATCH (u:User)-[r:ACTION]->(t:Course)
            WHERE r.feature2 > 0
            RETURN DISTINCT u.id AS userID, t.id AS targetID
            ORDER BY userID, targetID
            LIMIT 10
            """;

        return executeAndPrint("User/Target with Feature2 > 0", cypher);
    }

    /**
     * (8) For each targetID, count the number of actions with label = 1
     * @return execution time in milliseconds
     */
    private double labelOnePerTarget() {
        String cypher = """
            MATCH (:User)-[:ACTION {label: 1}]->(t:Course)
            RETURN t.id AS targetID, count(*) AS labelOneCount
            ORDER BY labelOneCount DESC
            LIMIT 10
            """;

        return executeAndPrint("Label=1 actions per target", cypher);
    }

    /**
     * Executes and prints the result of the given Cypher query.
     * @return execution time in milliseconds
     */
    private double executeAndPrint(String label, String cypher) {
        try {
            long start = System.nanoTime();
            Result result = session.run(cypher);
            double duration = (System.nanoTime() - start) / 1_000_000.0;

            System.out.printf("\n> %s (%.2f ms)\n", label, duration);
            while (result.hasNext()) {
                Record r = result.next();
                for (String key : r.keys()) {
                    System.out.print(key + ": " + r.get(key) + "\t");
                }
                System.out.println();
            }
            return duration;
        } catch (Exception e) {
            System.err.printf("Error executing query '%s': %s\n", label, e.getMessage());
            logger.error("Error executing query '{}'", label, e);
            return 0.0;
        }
    }

    /**
     * Prints the available query options.
     */
    private void printAvailableQueries() {
        System.out.println("""
            Available Queries:
            ───────────────────────────────
            graphsize         -> Count of users, courses and actions
            actionstargetsofuser -> Actions and targets of a user
            actionsperuser    -> Count of actions per user
            toptargets        -> Count distinct users per course
            avgactions        -> Average number of actions per user
            positivefeature2  -> (userID, courseID) where feature2 > 0
            label1pertarget   -> Count of label=1 actions per course
        """);
    }
}