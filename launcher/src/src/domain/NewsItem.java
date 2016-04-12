package src.domain;

import java.util.Date;

/**
 * @author Edwin
 *         Created on 3/30/2016.
 */
public class NewsItem {
    public static final String NAME_ID = "Id";
    public static final String NAME_TITLE = "Title";
    public static final String NAME_CONTENT = "Content";
    public static final String NAME_ISIMPORTANT = "Is important";
    public static final String NAME_DATE = "Date";

    // These keys must equal the field names or else they won't
    // be bound to the table
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_ISIMPORTANT = "importantDescription";
    public static final String KEY_DATE = "date";

    private int id;
    private String title;
    private String content;
    private boolean isImportant;
    private Date date;


    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Date getDate() {
        return date;
    }

    public NewsItem(int id, String title, String content, boolean isImportant, Date date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isImportant= isImportant;
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("%s, %s: %s", getDate(), getTitle(), getContent());
//        return "Test";
    }
}
