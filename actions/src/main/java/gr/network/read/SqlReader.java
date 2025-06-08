package gr.network.read;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.network.client.SqliteConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Communicates with the SQLite database
 * to query data from it.
 * @version 1.0
 */
public class SqlReader {

    private final Logger logger = LoggerFactory.getLogger(SqlReader.class);
    private final Connection connection;
    private Scanner scanner;

    public SqlReader(SqliteConnection sqliteConnection, Scanner scanner) {
        try {
            this.connection = sqliteConnection.getConnection();
        } catch (SQLException e) {
            logger.error("Failed to establish SQLite connection", e);
            throw new RuntimeException("Failed to establish SQLite connection", e);
        }
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

        logger.info("Running SQL query '{}'.", queryName);

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
        System.out.println("Database size counts");

        String userCountSql = "SELECT COUNT(*) as userCount FROM Users";
        String courseCountSql = "SELECT COUNT(*) as courseCount FROM Courses";
        String actionCountSql = "SELECT COUNT(*) as actionCount FROM Actions";

        double time1 = executeAndPrint("Total Users Count", userCountSql);
        double time2 = executeAndPrint("Total Courses Count", courseCountSql);
        double time3 = executeAndPrint("Total Actions Count", actionCountSql);
        return time1 + time2 + time3;
    }

    /**
     * (3) All actions and targets of a user
     * @return execution time in milliseconds
     */
    private double actionsTargetsOfUser(String userID) {
        System.out.println("Actions and targets of user " + userID);

        String sql = """
        SELECT actionId, courseId as targetId
        FROM Actions WHERE userId = ?
        ORDER BY actionId
        LIMIT 10
        """;
        return executeAndPrint("Actions and Targets of user " + userID, sql, userID);
    }

    /**
     * (4) Action counts per user
     * @return execution time in milliseconds
     */
    private double actionsPerUser() {
        System.out.println("Action counts per user");

        String sql = """
            SELECT userId, COUNT(*) as action_count
            FROM Actions
            GROUP BY userId
            ORDER BY userId
            LIMIT 10
            """;

        return executeAndPrint("Action Counts per User", sql);
    }

    /**
     * (5) For each target, count how many users have done this target
     * @return execution time in milliseconds
     */
    private double topTargets() {
        String sql = """
            SELECT courseId as targetId, COUNT(DISTINCT userId) as user_count
            FROM Actions
            GROUP BY courseId
            ORDER BY user_count DESC
            LIMIT 10
            """;

        return executeAndPrint("Top targets by distinct users", sql);
    }

    /**
     * (6) Count the average number of actions per user
     * @return execution time in milliseconds
     */
    private double avgActionsPerUser() {
        String sql = """
            SELECT AVG(action_count) as avg_actions_per_user
            FROM (
                SELECT userId, COUNT(*) as action_count
                FROM Actions
                GROUP BY userId
            ) user_actions
            """;

        return executeAndPrint("Average actions per user", sql);
    }

    /**
     * (7) Show the userID and the targetID, if the action has positive Feature2
     * @return execution time in milliseconds
     */
    private double userTargetWithPositiveFeature2() {
        String sql = """
            SELECT DISTINCT userId, courseId as targetId
            FROM Actions
            WHERE feature2 > 0
            ORDER BY userId, targetId
            LIMIT 10
            """;

        return executeAndPrint("User/Target with Feature2 > 0", sql);
    }

    /**
     * (8) For each targetID, count the actions with label "1"
     * @return execution time in milliseconds
     */
    private double labelOnePerTarget() {
        String sql = """
            SELECT courseId as targetId, COUNT(*) as label_1_count
            FROM Actions
            WHERE label = 1
            GROUP BY courseId
            ORDER BY label_1_count DESC
            LIMIT 10
            """;

        return executeAndPrint("Label=1 actions per target", sql);
    }

    /**
     * Executes and prints the result of the given SQL query.
     * @return execution time in milliseconds
     */
    private double executeAndPrint(String label, String sql, String... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters if any
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }

            long start = System.nanoTime();
            ResultSet result = stmt.executeQuery();
            double duration = (System.nanoTime() - start) / 1_000_000.0;

            System.out.printf("\n> %s (%.2f ms)\n", label, duration);

            // Print column headers
            int columnCount = result.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(result.getMetaData().getColumnName(i) + "\t");
            }
            System.out.println();

            // Print results
            while (result.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(result.getObject(i) + "\t");
                }
                System.out.println();
            }
            return duration;
        } catch (SQLException e) {
            System.err.printf("Error executing SQL query '%s': %s\n", label, e.getMessage());
            logger.error("Error executing SQL query '{}'", label, e);
            return 0.0;
        }
    }

    /**
     * Prints the available query options.
     */
    private void printAvailableQueries() {
        System.out.println("""
            Available SQL Queries:
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