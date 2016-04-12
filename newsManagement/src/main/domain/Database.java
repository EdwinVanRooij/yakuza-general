package main.domain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        testSelectQuery();
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
                    return result;
                }
            }
        }
    }

    public boolean updateNewsItem(NewsItem i) throws Exception {
        try (Connection c = getConnection()) {
            try (PreparedStatement s = c.prepareStatement("update news_item set title = ?, content = ?, is_important = ?, date = ? where id = ?;")) {
                s.setString(1, i.getTitle());
                s.setString(2, i.getContent());

                if (i.isImportant()) {
                    s.setInt(3, 1);
                } else {
                    s.setInt(3, 0);
                }

                s.setDate(4, (Date) i.getDate());
                s.setInt(5, i.getId());
                int rowsAffected = s.executeUpdate();
                if (rowsAffected > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public boolean insertNewsItem(NewsItem i) throws Exception {
        try (Connection c = getConnection()) {
            try (PreparedStatement s = c.prepareStatement("insert into news_item ("+KEY_TITLE+", "+KEY_CONTENT+", "+KEY_ISIMPORTANT+") values (?, ?, ?);")) {
                s.setString(1, i.getTitle());
                s.setString(2, i.getContent());

                if (i.isImportant()) {
                    s.setInt(3, 1);
                } else {
                    s.setInt(3, 0);
                }

                int rowsAffected = s.executeUpdate();
                if (rowsAffected > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public boolean deleteNewsItem(NewsItem i) throws Exception {
        try (Connection c = getConnection()) {
            try (PreparedStatement s = c.prepareStatement("delete from news_item where " + KEY_ID + " = ?")) {
                s.setInt(1, i.getId());

                int rowsAffected = s.executeUpdate();
                if (rowsAffected > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public void testSelectQuery() throws Exception {
        try (Connection c = getConnection()) {
            try (Statement s = getStatement(c)) {
                try (ResultSet rs = getResultSet(s, "select * from news_item")) {
                    while (rs.next()) {
                        msg(rs.getString("title") + ", " + rs.getString("content"));
                    }
                }
            }
        }
    }

    /**
     * Executes a modifying query on the database
     * @param query the query to be executed
     * @return amount of rows affected
     * @throws Exception
     */
    public int executeModQuery(String query) throws Exception {
        try (Connection c = getConnection()) {
            try (Statement s = getStatement(c)) {
                int rowsAffected = s.executeUpdate(query);
                return rowsAffected;
            }
        }
    }

    public void testUpdateQuery() throws Exception {
        try (Connection c = getConnection()) {
            try (Statement s = getStatement(c)) {
                int rowsAffected = s.executeUpdate(
                        "update employees set email = 'testemail@foo.com' where first_name = 'Edwin' and last_name = 'Van Rooij'");
                msg("Rows affected: " + rowsAffected);
            }
        }
    }

    public void testDeleteQuery() throws Exception {
        try (Connection c = getConnection()) {
            try (Statement s = getStatement(c)) {
                int rowsAffected = s.executeUpdate(
                        "delete from employees where first_name = 'Edwin' and last_name = 'Van Rooij'");
                msg("Rows affected: " + rowsAffected);
            }
        }
    }

    private ResultSet getResultSet(Statement s, String query) throws SQLException {
        return s.executeQuery(query);
    }

    private ResultSet getResultSet(PreparedStatement s) throws SQLException {
        return s.executeQuery();
    }

    private Statement getStatement(Connection c) throws SQLException {
        return c.createStatement();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }

    private void displayResultSet(ResultSet rs) throws SQLException {
        while (rs.next()) {
            msg(rs.getString("name") + ", " + rs.getString("sex"));
        }
    }

    private void msg(String msg) {
        System.out.println(msg);
    }
}