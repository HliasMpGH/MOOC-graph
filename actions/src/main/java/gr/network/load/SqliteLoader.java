package gr.network.load;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import gr.network.domain.Action;


public class SqliteLoader {

    private final Connection connection;

    private final Set<String> users;
    private final Set<String> courses;
    private final Set<Action> actions;

    private static final int BATCH_SIZE = 10000;

    public SqliteLoader(Set<String> users, Set<String> courses, Set<Action> actions, Connection connection) {
        this.connection = connection;
        this.users = users;
        this.courses = courses;
        this.actions = actions;
    }

    public void load() throws SQLException {

        connection.setAutoCommit(false);

        try {
            // Create tables first
            createTables();

            // insert users
            bulkInsertUsers(users);

            // insert courses
            bulkInsertCourses(courses);

            // insert actions
            bulkInsertActions(actions);

            connection.commit();

            System.out.println("Successfully inserted:");
            System.out.println("- " + users.size() + " users");
            System.out.println("- " + courses.size() + " courses");
            System.out.println("- " + actions.size() + " actions");

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS Users(
                userId TEXT PRIMARY KEY
            )
            """;

        String createCoursesTable = """
            CREATE TABLE IF NOT EXISTS Courses(
                courseId TEXT PRIMARY KEY
            )
            """;

        String createActionsTable = """
            CREATE TABLE IF NOT EXISTS Actions(
                actionId TEXT PRIMARY KEY,
                userId TEXT,
                courseId TEXT,
                tmsmp DATETIME,
                label INTEGER,
                feature0 REAL,
                feature1 REAL,
                feature2 REAL,
                feature3 REAL,
                FOREIGN KEY (userId) REFERENCES Users(userId),
                FOREIGN KEY (courseId) REFERENCES Courses(courseId)
            )
            """;

        try (var stmt1 = connection.prepareStatement(createUsersTable);
             var stmt2 = connection.prepareStatement(createCoursesTable);
             var stmt3 = connection.prepareStatement(createActionsTable)) {

            stmt1.execute();
            stmt2.execute();
            stmt3.execute();

            System.out.println("Tables created successfully");
        }
    }

    private void bulkInsertUsers(Set<String> userIds) throws SQLException {
        String insertSQL = "INSERT OR IGNORE INTO Users (userId) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            int count = 0;

            for (String userId : userIds) {
                pstmt.setString(1, userId);
                pstmt.addBatch();
                count++;

                if (count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }

            // execute remaining batch
            if (count % BATCH_SIZE != 0) {
                pstmt.executeBatch();
            }
        }
    }

    private void bulkInsertCourses(Set<String> courseIds) throws SQLException {
        String insertSQL = "INSERT OR IGNORE INTO Courses (courseId) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            int count = 0;

            for (String courseId : courseIds) {
                pstmt.setString(1, courseId);
                pstmt.addBatch();
                count++;

                if (count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }

            // execute remaining batch
            if (count % BATCH_SIZE != 0) {
                pstmt.executeBatch();
            }
        }
    }

    private void bulkInsertActions(Set<Action> actions) throws SQLException {
        String insertSQL = """
            INSERT OR IGNORE INTO Actions (actionId, userId, courseID, tmsmp, label, feature0, feature1, feature2, feature3)
            SELECT ?, u.userId, c.courseId, ?, ?, ?, ?, ?, ?
            FROM users u, courses c
            WHERE u.userId = ? AND c.courseId = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            int count = 0;

            for (Action action : actions) {
                pstmt.setString(1, action.getAction());
                pstmt.setString(2, action.getTimestamp());
                pstmt.setInt(3, action.getLabel());
                pstmt.setDouble(4, action.getFeature0());
                pstmt.setDouble(5, action.getFeature1());
                pstmt.setDouble(6, action.getFeature2());
                pstmt.setDouble(7, action.getFeature3());
                pstmt.setString(8, action.getUser());
                pstmt.setString(9, action.getCourse());
                pstmt.addBatch();
                count++;

                if (count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }

            // execute remaining batch
            if (count % BATCH_SIZE != 0) {
                pstmt.executeBatch();
            }
        }
    }
}
