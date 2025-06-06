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
     */
    public void run(String queryName) {
        if (queryName == null || queryName.isBlank()) {
            printAvailableQueries();
            System.out.print("Choose one query to run: ");
            queryName = scanner.nextLine();
        }

        logger.info("Running SQL query '{}'.", queryName);

        switch (queryName.toLowerCase()) {
            case "sampledata" -> sampleData();
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
     * (1) Show a small portion of the database
     */
    private void sampleData() {
        System.out.println("Sample data from database");
        String sql = "SELECT actionId, userId, courseId, label, feature2 FROM Actions LIMIT 10";
        executeAndPrint("Sample Database Portion", sql);
    }

    /**
     * (2) Count of users, courses and actions
     */
    private void graphSize() {
        System.out.println("Database size counts");
        
        String userCountSql = "SELECT COUNT(DISTINCT userId) as userCount FROM Users";
        String courseCountSql = "SELECT COUNT(DISTINCT courseId) as courseCount FROM Courses";
        String actionCountSql = "SELECT COUNT(*) as actionCount FROM Actions";

        executeAndPrint("Total Users Count", userCountSql);
        executeAndPrint("Total Courses Count", courseCountSql);
        executeAndPrint("Total Actions Count", actionCountSql);
    }

    /**
     * (3) All actions and targets of a user
     */
    private void actionsTargetsOfUser(String userID) {
        System.out.println("Actions and targets of user " + userID);

        String sql = "SELECT actionId, courseId as targetId FROM Actions WHERE userId = ?";
        executeAndPrint("Actions and Targets of user " + userID, sql, userID);
    }

    /**
     * (4) Action counts per user
     */
    private void actionsPerUser() {
        System.out.println("Action counts per user");

        String sql = """
            SELECT userId, COUNT(*) as action_count
            FROM Actions 
            GROUP BY userId
            ORDER BY userId
            """;

        executeAndPrint("Action Counts per User", sql);
    }

    /**
     * (5) For each target, count how many users have done this target
     */
    private void topTargets() {
        String sql = """
            SELECT courseId as targetId, COUNT(DISTINCT userId) as user_count
            FROM Actions 
            GROUP BY courseId
            ORDER BY user_count DESC
            LIMIT 10
            """;

        executeAndPrint("Top targets by distinct users", sql);
    }

    /**
     * (6) Count the average number of actions per user
     */
    private void avgActionsPerUser() {
        String sql = """
            SELECT AVG(action_count) as avg_actions_per_user
            FROM (
                SELECT userId, COUNT(*) as action_count
                FROM Actions 
                GROUP BY userId
            ) user_actions
            """;

        executeAndPrint("Average actions per user", sql);
    }

    /**
     * (7) Show the userID and the targetID, if the action has positive Feature2
     */
    private void userTargetWithPositiveFeature2() {
        String sql = """
            SELECT DISTINCT userId, courseId as targetId
            FROM Actions 
            WHERE feature2 > 0
            LIMIT 10
            """;

        executeAndPrint("User/Target with Feature2 > 0", sql);
    }

    /**
     * (8) For each targetID, count the actions with label "1"
     */
    private void labelOnePerTarget() {
        String sql = """
            SELECT courseId as targetId, COUNT(*) as label_1_count
            FROM Actions 
            WHERE label = 1
            GROUP BY courseId
            ORDER BY label_1_count DESC
            LIMIT 10
            """;

        executeAndPrint("Label=1 actions per target", sql);
    }

    /**
     * Executes and prints the result of the given SQL query.
     */
    private void executeAndPrint(String label, String sql, String... params) {
        long start = System.nanoTime();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters if any
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }
            
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
        } catch (SQLException e) {
            System.err.printf("Error executing SQL query '%s': %s\n", label, e.getMessage());
            logger.error("Error executing SQL query '{}'", label, e);
        }
    }

    /**
     * Prints the available query options.
     */
    private void printAvailableQueries() {
        System.out.println("""
            Available SQL Queries:
            ───────────────────────────────
            sampledata        -> Show sample data from database
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