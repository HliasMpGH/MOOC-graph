package gr.network.client;

import org.neo4j.driver.Driver;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Handles the connection to the Neo4j database.
 * @version 1.0
 */
public class Connection implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(Connection.class);

    /** The connection details. */
    private final String uri;
    private final String user;
    private final String password;
    private final String dbName;

    private Driver driver;
    private Session connectionSession;

    private static final Dotenv dotenv = Dotenv.configure()
        .directory("./")
        .load();

    public Connection() {
        this(
            dotenv.get("NEO4J_URI"),
            dotenv.get("NEO4J_USER"),
            dotenv.get("NEO4J_PASSWORD"),
            dotenv.get("NEO4J_DBNAME")
        );
    }

    public Connection(String uri, String user, String password, String dbName) {
        this.uri = uri;
        this.user = user;
        this.dbName = dbName;
        this.password = password;
    }

    /**
     * Retrieves the current Neo4j session.
     * if no session is open, it opens a new one.
     */
    public Session getSession() {
        if (this.connectionSession == null) {
            this.initializeSession();
        }
        return this.connectionSession;
    }

    /**
     * Retrieves the current Neo4j driver.
     * if no driver exists, it sets a new one.
     */
    public Driver getDriver() {
        if (this.driver == null) {
            this.initializeDriver();
        }
        return this.driver;
    }

    /**
     * Initializes the Neo4j session.
     */
    private void initializeSession() {
        logger.info("Opening New Neo4j Session...");

        this.connectionSession = this.getDriver().session(SessionConfig.builder()
            .withDatabase(dbName)
            .build());
    }

    /**
     * Initializes the Neo4j driver.
     */
    private void initializeDriver() {
        logger.info("Opening New Neo4j Connection...");

        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    /**
     * Closes the Neo4j session and driver.
     */
    @Override
    public void close() {
        logger.info("Closing Neo4j Session...");

        if (this.connectionSession != null) {
            this.connectionSession.close();
            logger.info("Neo4j Session closed.");
        }

        logger.info("Closing Neo4j Connection...");

        if (this.driver != null) {
            this.driver.close();
            logger.info("Neo4j Connection closed.");
        }
    }
}
