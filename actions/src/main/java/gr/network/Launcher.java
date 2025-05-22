package gr.network;

import java.util.List;

import gr.network.client.Connection;
import gr.network.domain.Action;
import gr.network.load.GraphLoader;

/**
 * Launcher of the application.
 * @version 1.0
 */
public class Launcher {
    public static void main(String[] args) {
        // read data

        // load dummy data in memory (normally this would be read from a file)
        List<String> users = List.of("0", "1", "2");
        List<String> courses = List.of("0", "1", "2");
        List<Action> actions = List.of(
            new Action("0", "0", "0", "2023-10-01T12:00:00Z", 0.1, 0.2, 0.3, 0.4, 1),
            new Action("1", "1", "1", "2023-10-01T12:00:00Z", 0.5, 0.6, 0.7, 0.8, 1),
            new Action("2", "2", "2", "2023-10-01T12:00:00Z", 0.9, 1.0, 1.1, 1.2, 1),
            new Action("3", "2", "2", "2023-10-01T12:00:00Z", 0.9, 1.0, 1.1, 1.2, 1),
            new Action("4", "2", "1", "2023-10-01T12:00:00Z", 0.9, 1.0, 1.1, 1.2, 1)
        );

        // graph-load the data
        try (Connection connection = new Connection()) {
            GraphLoader loader = new GraphLoader(users, courses, actions, connection);
            loader.load();
        } catch (Exception e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }
}
