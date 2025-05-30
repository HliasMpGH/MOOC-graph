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
 * @version 1.0
 */
public class GraphReader {

    private final Logger logger = LoggerFactory.getLogger(GraphReader.class);
    private final Session session;

    public GraphReader(Connection connection) {
        this.session = connection.getSession();
    }

    /**
     * Executes the query specified by alias.
     * If queryName is null or empty, display menu and let user choose.
     */
    public void run(String queryName) {
        if (queryName == null || queryName.isBlank()) {
            printAvailableQueries();
            System.out.print("Choose one query to run: ");
            Scanner scanner = new Scanner(System.in);
            queryName = scanner.nextLine();
        }

        logger.info("Running query '{}'.", queryName);

        switch (queryName.toLowerCase()) {
            case "toptargets" -> topTargets();
            case "avgactions" -> avgActionsPerUser();
            case "positivefeature2" -> userTargetWithPositiveFeature2();
            case "label1pertarget" -> labelOnePerTarget();
            default -> System.out.println("Unknown query: " + queryName);
        }
    }

    /**
     * (5) For each target, count how many distinct users performed an action
     */
    private void topTargets() {
        String cypher = """
            MATCH (u:User)-[:ACTION]->(t:Course)
            RETURN t.id AS targetID, count(DISTINCT u) AS userCount
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
            RETURN u.id AS userID, t.id AS targetID
            LIMIT 10
            """;

        executeAndPrint("User/Target with Feature2 > 0", cypher);
    }

    /**
     * (8) For each targetID, count the number of actions with label = 1
     */
    private void labelOnePerTarget() {
        String cypher = """
            MATCH (:User)-[r:ACTION]->(t:Course)
            WHERE r.label = 1
            RETURN t.id AS targetID, count(r) AS labelOneCount
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
        Result result = session.run(cypher);
        double duration = (System.nanoTime() - start) / 1_000_000.0;

        System.out.printf("\n▶ %s (%.2f ms)\n", label, duration);
        while (result.hasNext()) {
            Record r = result.next();
            for (String key : r.keys()) {
                System.out.print(key + ": " + r.get(key) + "\t");
            }
            System.out.println();
        }
    }

    /**
     * Prints the available query options.
     */
    private void printAvailableQueries() {
        System.out.println("""
            Available Queries:
            ───────────────────────────────
            1. toptargets        → Count distinct users per course
            2. avgactions        → Average number of actions per user
            3. positivefeature2  → (userID, courseID) where feature2 > 0
            4. label1pertarget   → Count of label=1 actions per course
        """);
    }
}
