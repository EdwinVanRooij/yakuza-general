package main.domain;

import java.util.ArrayList;
import java.util.List;

import static main.Main.msg;

/**
 * @author Edwin
 *         Created on 3/30/2016.
 */
public class Register {

    private Database db;
    private List<NewsItem> newsItems;

    public Register() throws Exception {
        db = new Database();
        newsItems = new ArrayList<>();
        loadRegister();
    }

    public List<NewsItem> getNewsItems() {
        return newsItems;
    }

    /**
     * Clears the whole register list and loads from database
     */
    public void loadRegister() throws Exception {
        loadNewsItems();
    }

    /**
     * Saves the whole register to the database
     */
    public void saveRegisterToDb() {

    }

    /**
     * Adds a NewsItem to the list
     *
     * @param i the newsItem to be added
     */
    public boolean addNewsItem(NewsItem i) throws Exception {
        if (db.insertNewsItem(i)) {
            loadNewsItems();
            msg("Inserted a newsitem.");
            return true;
        } else {
            msg("0 rows affected, possibly failed executing the query.");
            return false;
        }
    }

    /**
     * Updates the given NewsItem in this register
     *
     * @param i the NewsItem to be updated
     */
    public boolean updateNewsItem(NewsItem i) throws Exception {
        if (db.updateNewsItem(i)) {
            loadNewsItems();
            msg("Updated a newsitem");
            return true;
        } else {
            msg("0 rows affected, possibly failed executing the query.");
            return false;
        }
    }

    /**
     * Deletes the newsitem from the register
     *
     * @param i the newsitem to be deleted
     */
    public boolean deleteNewsItem(NewsItem i) throws Exception {
        if (db.deleteNewsItem(i)) {
            loadNewsItems();
            msg("Deleted a newsitem");
            return true;
        } else {
            msg("0 rows affected, possibly failed executing the query.");
            return false;
        }
    }

    /**
     * All newsitems currently in the database
     */
    private void loadNewsItems() throws Exception {
        newsItems = db.loadNewsItems();
    }
}
