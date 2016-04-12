package src.domain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static src.Main.msg;

/**
 * @author Edwin
 *         Created on 3/30/2016.
 */
public class Database {

    //Fields
    String url = "jdbc:mysql://localhost:3306/yakuza_db";
    String user = "root";
    String pass = "nErv3dismal5";

    private final String SELECT_NEWSITEMS = "select * from news_item";

    private final String KEY_ID = "id";
    private final String KEY_TITLE = "title";
    private final String KEY_CONTENT = "content";
    private final String KEY_ISIMPORTANT = "is_important";
    private final String KEY_DATE = "date";

    //Constructors
    public Database() throws Exception {
        msg("Database intialized.");
    }

    //Methods
    public List<NewsItem> loadNewsItems() throws Exception {
        try (Connection c = getConnection()) {
            try (Statement s = getStatement(c)) {
                try (ResultSet rs = getResultSet(s, SELECT_NEWSITEMS)) {
                    List<NewsItem> result = new ArrayList<>();
                    while (rs.next()) {
                        boolean b = false;
                        if (rs.getInt(KEY_ISIMPORTANT) == 0) {
                            b = false;
                        } else if (rs.getInt(KEY_ISIMPORTANT) == 1){
                            b = true;
                        } else {
                            msg("Error: int from db should be 0 or 1.");
                        }
                        result.add(new NewsItem(rs.getInt(KEY_ID), rs.getString(KEY_TITLE), rs.getString(KEY_CONTENT), b, rs.getDate(KEY_DATE)));
                    }
                    msg(String.format("Returned %s newsitems.", result.size()));
                    return result;
                }
            }
        }
    }


    private ResultSet getResultSet(Statement s, String query) throws SQLException {
        return s.executeQuery(query);
    }

    private Statement getStatement(Connection c) throws SQLException {
        return c.createStatement();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}