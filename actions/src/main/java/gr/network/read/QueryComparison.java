package gr.network.read;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.network.client.Neo4jConnection;
import gr.network.client.SqliteConnection;

import java.util.Scanner;

/**
 * Compares query performance between Neo4j and SQLite databases
 * @version 1.0
 */
public class QueryComparison {

    private final Logger logger = LoggerFactory.getLogger(QueryComparison.class);
    private final Neo4jConnection neo4jConnection;
    private final SqliteConnection sqliteConnection;
    private Scanner scanner;

    public QueryComparison(Neo4jConnection neo4jConnection, SqliteConnection sqliteConnection, Scanner scanner) {
        this.neo4jConnection = neo4jConnection;
        this.sqliteConnection = sqliteConnection;
        this.scanner = scanner;
    }

    /**
     * Runs comparison between Neo4j and SQL queries
     */
    public void runComparison(String queryName) {
        if (queryName == null || queryName.isBlank()) {
            printAvailableQueries();
            System.out.print("Choose one query to compare: ");
            queryName = scanner.nextLine();
        }

        logger.info("Running comparison for query '{}'.", queryName);

        System.out.println("\n" + "=".repeat(60));
        System.out.printf("COMPARISON: %s\n", queryName.toUpperCase());
        System.out.println("=".repeat(60));

        // Get user input once if needed
        String userId = null;
        if ("actionstargetsofuser".equals(queryName.toLowerCase())) {
            do {
                System.out.print("Provide a user id for comparison: ");
                userId = scanner.nextLine().trim();
            } while (userId.isBlank());
        }

        System.out.println("\n--- NEO4J RESULTS ---");
        double neo4jMs;
        if (userId != null) {
            Scanner tempScanner = new Scanner(userId + "\n");
            GraphReader tempGraphReader = new GraphReader(neo4jConnection, tempScanner);
            neo4jMs = tempGraphReader.run(queryName);
        } else {
            GraphReader graphReader = new GraphReader(neo4jConnection, scanner);
            neo4jMs = graphReader.run(queryName);
        }

        System.out.println("\n--- SQLITE RESULTS ---");
        double sqliteMs;
        if (userId != null) {
            Scanner tempScanner = new Scanner(userId + "\n");
            SqlReader tempSqlReader = new SqlReader(sqliteConnection, tempScanner);
            sqliteMs = tempSqlReader.run(queryName);
        } else {
            SqlReader sqlReader = new SqlReader(sqliteConnection, scanner);
            sqliteMs = sqlReader.run(queryName);
        }

        System.out.println("\n--- PERFORMANCE COMPARISON ---");
        System.out.printf("Neo4j execution time:   %.2f ms\n", neo4jMs);
        System.out.printf("SQLite execution time:  %.2f ms\n", sqliteMs);
        System.out.printf("Time difference:        %.2f ms\n", Math.abs(neo4jMs - sqliteMs));
        
        if (neo4jMs < sqliteMs) {
            System.out.printf("Neo4j is %.2fx faster\n", sqliteMs / neo4jMs);
        } else if (sqliteMs < neo4jMs) {
            System.out.printf("SQLite is %.2fx faster\n", neo4jMs / sqliteMs);
        } else {
            System.out.println("Both databases performed equally");
        }
        
        System.out.println("=".repeat(60));
    }

    /**
     * Runs all available queries for comparison
     */
    public void runAllComparisons() {
        String[] queries = {
            "graphsize", 
            "actionsperuser",
            "toptargets",
            "avgactions",
            "positivefeature2",
            "label1pertarget"
        };

        System.out.println("\nRunning complete performance comparison...\n");
        
        for (String query : queries) {
            runComparison(query);
            System.out.println("\nPress Enter to continue to next query...");
            scanner.nextLine();
        }

        // Special case for user-specific query
        System.out.println("Testing user-specific query with sample user...");
        runUserSpecificComparison("0");
    }

    /**
     * Runs user-specific query comparison with a predetermined user ID
     */
    private void runUserSpecificComparison(String userId) {
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("COMPARISON: ACTIONSTARGETSOFUSER (User: %s)\n", userId);
        System.out.println("=".repeat(60));

        System.out.println("\n--- NEO4J RESULTS ---");
        long neo4jStart = System.nanoTime();
        Scanner tempScanner = new Scanner(userId + "\n");
        GraphReader tempGraphReader = new GraphReader(neo4jConnection, tempScanner);
        tempGraphReader.run("actionstargetsofuser");
        long neo4jTime = System.nanoTime() - neo4jStart;

        System.out.println("\n--- SQLITE RESULTS ---");
        long sqliteStart = System.nanoTime();
        Scanner tempSqlScanner = new Scanner(userId + "\n");
        SqlReader tempSqlReader = new SqlReader(sqliteConnection, tempSqlScanner);
        tempSqlReader.run("actionstargetsofuser");
        long sqliteTime = System.nanoTime() - sqliteStart;

        System.out.println("\n--- PERFORMANCE COMPARISON ---");
        double neo4jMs = neo4jTime / 1_000_000.0;
        double sqliteMs = sqliteTime / 1_000_000.0;
        
        System.out.printf("Neo4j execution time:   %.2f ms\n", neo4jMs);
        System.out.printf("SQLite execution time:  %.2f ms\n", sqliteMs);
        
        if (neo4jMs < sqliteMs) {
            System.out.printf("Neo4j is %.2fx faster\n", sqliteMs / neo4jMs);
        } else if (sqliteMs < neo4jMs) {
            System.out.printf("SQLite is %.2fx faster\n", neo4jMs / sqliteMs);
        } else {
            System.out.println("Both databases performed equally");
        }
        
        System.out.println("=".repeat(60));
    }

    /**
     * Prints the available query options for comparison.
     */
    private void printAvailableQueries() {
        System.out.println("""
            Available Queries for Comparison:
            ───────────────────────────────
            graphsize         -> Count of users, courses and actions
            actionstargetsofuser -> Actions and targets of a user
            actionsperuser    -> Count of actions per user
            toptargets        -> Count distinct users per course
            avgactions        -> Average number of actions per user
            positivefeature2  -> (userID, courseID) where feature2 > 0
            label1pertarget   -> Count of label=1 actions per course
            all              -> Run all queries for complete comparison
        """);
    }
}