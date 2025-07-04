package gr.network;

import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.network.client.Neo4jConnection;
import gr.network.client.SqliteConnection;
import gr.network.domain.Action;
import gr.network.load.GraphLoader;
import gr.network.load.SqliteLoader;
import gr.network.read.GraphReader;
import gr.network.read.InputReader;
import gr.network.read.SqlReader;
import gr.network.read.QueryComparison;

/**
 * Launcher of the application.
 * @version 1.1
 */
public class Launcher {

    private final static Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private static boolean shouldLoad;
    private static String fileName;

    private static boolean shouldQuery;
    private static String queryName;

    private static boolean shouldCompare;
    private static boolean shouldRunSql;

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean okInput = handleArgs(args);
        if (!okInput) return;

        try (
            Neo4jConnection neo4jConnection = new Neo4jConnection();
            SqliteConnection sqliteConnection = new SqliteConnection();
        ) {

            if (shouldLoad) {
                LOGGER.info("Graph Loading Specified");
                InputReader reader;

                if (fileName != null) {
                    LOGGER.info("Specified file name: {}", fileName);
                    reader = new InputReader(fileName);
                } else {
                    LOGGER.info("File name not specified, taking default");
                    reader = new InputReader();
                }

                Set<String> users = reader.getUserIds();
                Set<String> courses = reader.getCourseIds();
                Set<Action> actions = reader.getActions();

                LOGGER.info("Loading {} Users", users.size());
                LOGGER.info("Loading {} Courses", courses.size());
                LOGGER.info("Loading {} Actions", actions.size());

                // load in sqlite
                SqliteLoader sqliteLoader = new SqliteLoader(users, courses, actions, sqliteConnection.getConnection());
                sqliteLoader.load();
                LOGGER.info("Sqlite Loading Done");

                GraphLoader loader = new GraphLoader(users, courses, actions, neo4jConnection);
                loader.load();
                LOGGER.info("Neo4j Loading Done");
            }

            if (shouldCompare) {
                LOGGER.info("Running query comparison between Neo4j and SQLite");
                QueryComparison comparison = new QueryComparison(neo4jConnection, sqliteConnection, scanner);
                if (queryName != null) {
                    comparison.runComparison(queryName);
                } else {
                    comparison.runComparison(null);
                }
            } else if (shouldRunSql) {
                LOGGER.info("Running SQL queries only");
                SqlReader sqlReader = new SqlReader(sqliteConnection, scanner);
                if (shouldQuery) {
                    sqlReader.run(queryName);
                } else {
                    sqlReader.run(null);
                }
            } else {
                GraphReader reader = new GraphReader(neo4jConnection, scanner);

                if (shouldQuery) {
                    LOGGER.info("Graph Querying Specified: {}", queryName);
                    reader.run(queryName);
                } else {
                    // No query specified - ask user to choose one or all
                    printAvailableExecutions();

                    String choice = scanner.nextLine().trim();

                    switch (choice) {
                        case "0" -> {
                            runAllQueries(reader);
                        }
                        case "1" -> reader.run("graphsize");
                        case "2" -> reader.run("actionstargetsofuser");
                        case "3" -> reader.run("actionsperuser");
                        case "4" -> reader.run("toptargets");
                        case "5" -> reader.run("avgactions");
                        case "6" -> reader.run("positivefeature2");
                        case "7" -> reader.run("label1pertarget");
                        case "8" -> {
                            QueryComparison comparison = new QueryComparison(neo4jConnection, sqliteConnection, scanner);
                            comparison.runComparison(null);
                        }
                        case "9" -> {
                            QueryComparison comparison = new QueryComparison(neo4jConnection, sqliteConnection, scanner);
                            comparison.runAllComparisons();
                        }
                        default -> System.out.println("Invalid selection " + choice);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Connection failed.", e);
        } finally {
            scanner.close();
        }
    }

    private static boolean handleArgs(String[] args) {
        switch (args.length) {
            case 0:
                return true; // allow empty args, fall back to interactive
            case 1:
                if (args[0].equals("--compare")) {
                    shouldCompare = true;
                    return true;
                } else if (args[0].equals("--sql")) {
                    shouldRunSql = true;
                    return true;
                } else if (args[0].equals("--load")) {
                    shouldLoad = true;
                    return true;
                } else if (args[0].equals("--query")) {
                    shouldQuery = true;
                    return true;
                }
                break;
            case 2:
                if (args[0].equals("--load")) {
                    shouldLoad = true;
                    fileName = args[1];
                    return true;
                } else if (args[0].equals("--query")) {
                    shouldQuery = true;
                    queryName = args[1];
                    return true;
                } else if (args[0].equals("--compare")) {
                    shouldCompare = true;
                    queryName = args[1];
                    return true;
                } else if (args[0].equals("--sql")) {
                    shouldRunSql = true;
                    shouldQuery = true;
                    queryName = args[1];
                    return true;
                }
                break;
            case 3:
                if (args[0].equals("--load") && args[1].equals("--query")) {
                    shouldLoad = true;
                    shouldQuery = true;
                    queryName = args[2];
                    return true;
                } else if (args[0].equals("--load") && args[1].equals("--compare")) {
                    shouldLoad = true;
                    shouldCompare = true;
                    queryName = args[2];
                    return true;
                }
                break;
            case 4:
                if (args[0].equals("--load") && args[2].equals("--query")) {
                    shouldLoad = true;
                    fileName = args[1];
                    shouldQuery = true;
                    queryName = args[3];
                    return true;
                } else if (args[0].equals("--load") && args[2].equals("--compare")) {
                    shouldLoad = true;
                    fileName = args[1];
                    shouldCompare = true;
                    queryName = args[3];
                    return true;
                }
                break;
        }
        System.out.println(getUsage());
        return false;
    }

    /**
     * Return the execution usage of the application.
     */
    private static String getUsage() {
        return """
            Usage: java -jar <jar_name>.jar <args>
                --load : Load the graph from the default file (mooc_actions_merged.csv)
                --load <path_to_csv_file> : Load the graph from the given file
                --query <query_alias> : Query the graph with the given query alias
                --query : Query the graph with the given query alias (interactive)
                --sql <query_alias> : Run SQL queries only
                --sql : Run SQL queries only (interactive)
                --compare <query_alias> : Compare Neo4j vs SQLite performance for specific query
                --compare : Compare Neo4j vs SQLite performance (interactive)
                --load --query <query_name> : Load the graph and query it with the given query name
                --load --compare <query_name> : Load the graph and run comparison
                --load <path_to_csv_file> --query <query_name> : Load the graph from a specified file and query it with the given query name
                --load <path_to_csv_file> --compare <query_name> : Load the graph from a specified file and run comparison
                [NO ARGS] : Just run and choose a query interactively (includes comparison options)
        """;
    }

    /**
     * Print the running usage of the application.
     */
    private static void printAvailableExecutions() {
        System.out.println("\nAvailable Queries:");
        System.out.println(" 0) all - Run all Neo4j queries");
        System.out.println(" 1) graphsize - Count of user, courses and actions");
        System.out.println(" 2) actionstargetsofuser - Show the actions and targets of a user");
        System.out.println(" 3) actionsperuser - Count of actions per user");
        System.out.println(" 4) toptargets - Top 10 targets by distinct users");
        System.out.println(" 5) avgactions - Average actions per user");
        System.out.println(" 6) positivefeature2 - Show user/target pairs with feature2 > 0");
        System.out.println(" 7) label1pertarget - Count label=1 per target");
        System.out.println(" 8) compare - Compare single query performance (Neo4j vs SQLite)");
        System.out.println(" 9) compareall - Compare all queries performance (Neo4j vs SQLite)");
        System.out.print("\nChoose a query (0-9): ");
    }

    private static void runAllQueries(GraphReader reader) {
        reader.run("graphsize");
        reader.run("actionstargetsofuser");
        reader.run("actionsperuser");
        reader.run("toptargets");
        reader.run("avgactions");
        reader.run("positivefeature2");
        reader.run("label1pertarget");
    }
}