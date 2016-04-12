package src.domain;

import java.util.ArrayList;
import java.util.List;

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
     * All newsitems currently in the database
     */
    private void loadNewsItems() throws Exception {
        newsItems = db.loadNewsItems();
    }
}
