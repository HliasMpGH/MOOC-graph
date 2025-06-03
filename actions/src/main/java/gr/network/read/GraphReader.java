package gr.network.read;

import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.network.client.Connection;

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

    public GraphReader(Connection connection, Scanner scanner) {
        this.session = connection.getSession();
        this.scanner = scanner;
    }

    /**
     * Executes the query specified by alias.
     * If queryName is null or empty, display menu and let user choose.
     */
    public void run(String queryName) {
        if (queryName == null || queryName.isBlank()) {
            printAvailableQueries();
            System.out.print("Choose one query to run: ");
            queryName = scanner.nextLine();
        }

        logger.info("Running query '{}'.", queryName);

        switch (queryName.toLowerCase()) {
            case "graphsize" -> graphSize();
            case "actionstargetsofuser" -> {
                // take user input for the user id
                String userId = "";
                do {
                    System.out.print("Provide a user id: ");
                    userId = scanner.nextLine().trim();
                } while (userId.isBlank());


                actionsTargetsOfUser(userId);
            }
            case "actionsperuser" -> actionsPerUser();
            case "toptargets" -> topTargets();
            case "avgactions" -> avgActionsPerUser();
            case "positivefeature2" -> userTargetWithPositiveFeature2();
            case "label1pertarget" -> labelOnePerTarget();
            default -> System.out.println("Unknown query: " + queryName);
        }
    }

    /**
     * (2) Count of users, courses and actions
     */
    private void graphSize() {
        System.out.println("gaphsize");
        String userCountCypher = getNodeCountCypher("User");
        String courseCountCypher = getNodeCountCypher("Course");
        String actionCountCypher = getRelationshipCountCypher(
            "User",
            "ACTION" ,
            "Course"
        );

        executeAndPrint("Total Users Count", userCountCypher);
        executeAndPrint("Total Courses Count", courseCountCypher);
        executeAndPrint("Total Actions Count", actionCountCypher);
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
     */
    private void actionsTargetsOfUser(String userID) {
        System.out.println("actionsTargetsOfUser of id " + userID);

        String actionsTargetCypher = String.format(
        """
            MATCH (:User {id: '%s'})
            -[action:ACTION]->
            (course:Course)
            return action.action as actionId, course.id as courseID
        """, userID);

        executeAndPrint("Actions and Targets of user " + userID, actionsTargetCypher);
    }

    /**
     * (4) Action counts per user
     */
    private void actionsPerUser() {
        System.out.println("actionsPerUser");

        String actionsTargetCypher = String.format(
        """
            MATCH (user:User)
            -[action:ACTION]->()
            return user.id as userId, count(action) as totalActions
        """);

        executeAndPrint("Action Counts per User", actionsTargetCypher);
    }

    /**
     * (5) Top 10 target courses by number of unique users who performed actions
     */
    private void topTargets() {
        session.run("""
        MATCH (t:Course)
        WITH t, COUNT { (:User)-[:ACTION]->(t) } AS count
        SET t.userCount = count
    """);

        String cypher = """
        MATCH (t:Course)
        RETURN t.id AS targetID, t.userCount AS userCount
        ORDER BY userCount DESC
        LIMIT 10
    """;

        executeAndPrint("Top 10 targets by distinct users", cypher);
    }

    /**
     * (6) Count the average number of actions per user
     */
    private void avgActionsPerUser() {
        String cypher = """
            MATCH (u:User)-[r:ACTION]->()
            WITH u, count(r) AS total
            RETURN avg(total) AS avgActionsPerUser
            """;

        executeAndPrint("Average actions per user", cypher);
    }

    /**
     * (7) Show the userID and the targetID, if the action has positive Feature2
     */
    private void userTargetWithPositiveFeature2() {
        String cypher = """
            MATCH (u:User)-[r:ACTION]->(t:Course)
            WHERE r.feature2 > 0
            RETURN DISTINCT u.id AS userID, t.id AS targetID
            LIMIT 10
            """;

        executeAndPrint("User/Target with Feature2 > 0", cypher);
    }

    /**
     * (8) For each targetID, count the number of actions with label = 1
     */
    private void labelOnePerTarget() {
        String cypher = """
            MATCH (:User)-[:ACTION {label: 1}]->(t:Course)
            RETURN t.id AS targetID, count(*) AS labelOneCount
            ORDER BY labelOneCount DESC
            LIMIT 10
            """;

        executeAndPrint("Label=1 actions per target", cypher);
    }

    /**
     * Executes and prints the result of the given Cypher query.
     */
    private void executeAndPrint(String label, String cypher) {
        long start = System.nanoTime();
        try {
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
        } catch (Exception e) {
            System.err.printf("Error executing query '%s': %s\n", label, e.getMessage());
            logger.error("Error executing query '{}'", label, e);
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