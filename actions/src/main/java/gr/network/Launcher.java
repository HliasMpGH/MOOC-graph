package gr.network;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.network.client.Connection;
import gr.network.domain.Action;
import gr.network.load.GraphLoader;
import gr.network.read.InputReader;

/**
 * Launcher of the application.
 * @version 1.0
 */
public class Launcher {

    private final static Logger LOGGER = LoggerFactory.getLogger(GraphLoader.class);

    private static boolean shouldLoad;
    private static String fileName;

    private static boolean shouldQuery;
    private static String queryName;

    public static void main(String[] args) {

        boolean okInput = handleArgs(args);
        if (!okInput) {
            return;
        }

        // read data

        // load dummy data in memory (normally this would be read from a file)
        // List<String> users = List.of("0", "1", "2");
        // List<String> courses = List.of("0", "1", "2");
        // List<Action> actions = List.of(
        //     new Action("0", "0", "0", "2023-10-01T12:00:00Z", 0.1, 0.2, 0.3, 0.4, 1),
        //     new Action("1", "1", "1", "2023-10-01T12:00:00Z", 0.5, 0.6, 0.7, 0.8, 1),
        //     new Action("2", "2", "2", "2023-10-01T12:00:00Z", 0.9, 1.0, 1.1, 1.2, 1),
        //     new Action("3", "2", "2", "2023-10-01T12:00:00Z", 0.9, 1.0, 1.1, 1.2, 1),
        //     new Action("4", "2", "1", "2023-10-01T12:00:00Z", 0.9, 1.0, 1.1, 1.2, 1)
        // );

        // graph-load the data if specified by user
        try (Connection connection = new Connection()) {

            if (shouldLoad) {
                LOGGER.info("Graph Loading Specified");
                InputReader reader;

                if (fileName != null) {
                    LOGGER.info("Specified file name: {}",fileName);
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
                GraphLoader loader = new GraphLoader(users, courses, actions, connection);
                loader.load();
            }

            if (shouldQuery) {
                LOGGER.info("Graph Querying Specified: {}", queryName);
                // execute the query on graph using GraphReader
            }

        } catch (Exception e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }

    /**
     * Handle the command line arguments by setting
     * the user specifications.
     * @param args the command line arguments
     * @return true if the arguments are valid, false otherwise
     */
    private static boolean handleArgs(String[] args) {
        switch (args.length) {
            case 0:
                System.out.println(getUsage());
                return false;
            case 1:
                if (args[0].equals("--load")) {
                    shouldLoad = true;
                    return true;
                } else {
                    System.out.println(getUsage());
                    return false;
                }
            case 2:
                if (args[0].equals("--load")) {
                    shouldLoad = true;
                    fileName = args[1];
                    return true;
                } else if (args[0].equals("--query")) {
                    shouldQuery = true;
                    queryName = args[1];
                    return true;
                } else {
                    System.out.println(getUsage());
                    return false;
                }
            case 3:
                if (args[0].equals("--load")) {
                    shouldLoad = true;
                    if (args[1].equals("--query")) {
                        shouldQuery = true;
                        queryName = args[2];
                    } else {
                        System.out.println(getUsage());
                        return false;
                    }
                    return true;
                } else {
                    System.out.println(getUsage());
                    return false;
                }
            case 4:
                if (args[0].equals("--load")) {
                    shouldLoad = true;
                    fileName = args[1];
                    if (args[2].equals("--query")) {
                        shouldQuery = true;
                        queryName = args[3];
                        return true;
                    } else {
                        System.out.println(getUsage());
                        return false;
                    }
                } else {
                    System.out.println(getUsage());
                    return false;
                }
            default:
                System.out.println(getUsage());
                return false;
        }
    }

    private static String getUsage() {
        return "Usage: java -jar <jar_name>.jar <args>\n" +
            "  --load : Load the graph from the default file (mooc_actions_merged.csv)\n" +
            "  --load <path_to_csv_file> : Load the graph from the given file\n" +
            "  --query <query_alias> : Query the graph with the given query alias\n" +
            "  --load --query <query_name> : Load the graph and query it with the given query name\n" +
            "  --load <path_to_csv_file> --query <query_name> : Load the graph from a specified file and query it with the given query name\n";
    }
}
