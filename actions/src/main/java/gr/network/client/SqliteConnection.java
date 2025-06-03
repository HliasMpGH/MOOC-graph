package gr.network.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class SqliteConnection implements AutoCloseable {

    private final String uri;

    private Connection connection;

    private static final Dotenv dotenv = Dotenv.configure()
        .directory("./")
        .load();

    public SqliteConnection() {
        this(dotenv.get("SQLITE_URI"));
    }

    public SqliteConnection(String uri) {
        this.uri = uri;
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null) {
            this.connection = DriverManager.getConnection(this.uri);
        }
        return this.connection;
    }

    @Override
    public void close() throws Exception {
        if (this.connection != null) {
            this.connection.close();
        }
    }
}
