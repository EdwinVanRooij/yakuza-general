package tools;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import constants.ServerConstants;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster (some modifications to this beautiful code)
 */
public class DatabaseConnection {

    public static final int RETURN_GENERATED_KEYS = 1;

    private static ThreadLocal<Connection> con = new ThreadLocalConnection();

    public static Connection getConnection() {
        Connection c = con.get();
        try {
            c.getMetaData();
        } catch (SQLException e) { // connection is dead, therefore discard old object 5ever
            con.remove();
            c = con.get();
        }
        return c;
    }

    private static class ThreadLocalConnection extends ThreadLocal<Connection> {

        @Override
        protected Connection initialValue() {
            try {
                Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
            } catch (ClassNotFoundException e) {
                System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
                e.printStackTrace();
                return null;
            }
            try {
                Properties p = new Properties();
                p.load(new FileInputStream("configuration.ini"));

                //SQL DATABASE
                ServerConstants.DB_URL = p.getProperty("URL");
                ServerConstants.DB_USER = p.getProperty("DB_USER");
                ServerConstants.DB_PASS = p.getProperty("DB_PASS");

                return DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);

            } catch (SQLException e) {
                System.out.println("[SEVERE] Unable to make database connection.");
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                System.out.println("Failed to load configuration.ini.");
                System.exit(0);
                return null;
            }
        }
    }
}
