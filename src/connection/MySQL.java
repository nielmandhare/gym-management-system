package connection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import util.LoggerUtil;
import util.TimeUtil;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MySQL {

    private static final String HOST;
    private static final String PORT;
    private static final String DB_NAME;
    private static final String USER;
    private static final String PASS;
    private static final String DUMP_PATH;
    private static final String MYSQL_PATH;

    private static final Properties PROPS;

    private static Connection connection;

    private static int affectedRows;

    private static final Logger logger = LoggerUtil.getLogger(MySQL.class);

    static {
        PROPS = new Properties();
        try {
            InputStream input = new java.io.File("src/resources/db.properties").isFile()
                    ? new java.io.FileInputStream("src/resources/db.properties")
                    : MySQL.class.getClassLoader().getResourceAsStream("resources/db.properties");

            if (input == null) {
                throw new RuntimeException("db.properties file not found in classpath");
            }
            try (input) { PROPS.load(input); }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "initializing MySQL caused an error!", e);
        }

        HOST = PROPS.getProperty("db.host");
        PORT = PROPS.getProperty("db.port");
        DB_NAME = PROPS.getProperty("db.name");
        USER = PROPS.getProperty("db.user");
        PASS = PROPS.getProperty("db.password");

        MYSQL_PATH = PROPS.getProperty("db.mysql_path");
        DUMP_PATH = PROPS.getProperty("db.mysqldump_path");

        try {
            String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useAffectedRows=true";

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            logger.log(Level.SEVERE, "initializing MySQL caused an error!", e);
        }
    }

    // get connection
    public static Connection getConnection() {
        return connection;
    }

    // execute search query
    private static void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            connection = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
                    + "?useAffectedRows=true", USER, PASS);
        }
    }

    public static ResultSet executeSearch(String query, Object... params) throws SQLException {
        security.Session.requireAdmin();
        ensureConnection();
        PreparedStatement stmt = connection.prepareStatement(query);
        try {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            stmt.closeOnCompletion();
            return stmt.executeQuery();
        } catch (SQLException e) {
            stmt.close();
            throw e;
        }
    }

    // execute insert, update and delete query
    public static boolean executeIUD(String query, Object... params) {
        security.Session.requireAdmin();
        affectedRows = 0;
        try {
            ensureConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
                affectedRows = stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "executing IUD operations caused an error!", e);
            return false;
        }
    }

    // get affected rows count
    public static int getAffectedRows() {
        return affectedRows;
    }

    // backup database
    public static boolean backupDatabase() {
        security.Session.requireAdmin();
        try {
            String backupDir = "backups";
            new File(backupDir).mkdirs();

            String backupStringFileName = backupDir + File.separator
                    + DB_NAME + "_"
                    + TimeUtil.getFormattedDateTime("yyyyMMdd_HHmmss")
                    + ".sql";

            List<String> command = Arrays.asList(
                    DUMP_PATH,
                    "--defaults-extra-file=src/resources/mysql.cnf",
                    "--no-tablespaces",
                    "--single-transaction",
                    DB_NAME
            );

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectOutput(new File(backupStringFileName));
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = processBuilder.start();

            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "backing-up database caused an error!", e);
        }
        return false;
    }

    // restore databse backup
    public static void restoreDatabaseBackup() {

    }

    // close database connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "closing database connection caused an error!", e);
        }
    }
}
