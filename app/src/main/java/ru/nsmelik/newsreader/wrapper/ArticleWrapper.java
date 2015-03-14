package ru.nsmelik.newsreader.wrapper;

import android.graphics.Bitmap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Smelik Nick.
 * This class provides wrapper for feed's articles
 */
public class ArticleWrapper {

    private String title;
    private String description;
    private Date pubDate;
    private String link;

    private long id;
    private long feedId;
    private String feedName;
    private boolean read;
    private boolean favourite;
    private boolean hasImage;
    private Bitmap image;

    /**
     * Default constructor
     * Construct wrapper if we have only base information about article
     * @param title article's title
     * @param description description of article
     * @param pubDate the date of article publication
     * @param link link to the article
     */
    public ArticleWrapper(String title, String description, String pubDate, String link) {
        this.title = title;
        this.description = description;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            this.pubDate = sdf.parse(pubDate);
        } catch (ParseException e) {
            this.pubDate = new Date(System.currentTimeMillis());
        }
        this.link = link;
    }

    /**
     * Additional constructor
     * Construct wrapper if we have full information about article
     * @param title article's title
     * @param description description of article
     * @param pubDate the date of article publication
     * @param link link to the article
     * @param id article's id in database
     * @param feedId id of the feed, witch is article from
     * @param feedName name of the fees, witch is article from
     * @param read checker, if the article was read
     * @param favourite checker, if the article add to favourite
     * @param image image of article, if it has
     */
    public ArticleWrapper(String title, String description, long pubDate, String link,
                          long id, long feedId, String feedName, boolean read, boolean favourite, Bitmap image) {
        this.title = title;
        this.description = description;
        this.feedName = feedName;
        this.pubDate = new Date(pubDate);
        this.link = link;
        this.id = id;
        this.feedId = feedId;
        this.read = read;
        this.favourite = favourite;
        if (image != null) {
            this.image = image;
            hasImage = true;
        } else {
            hasImage = false;
        }
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getPubDate() {
        return pubDate.getTime();
    }

    public String getLink() {
        return link;
    }

    public long getId() {
        return id;
    }

    public boolean isRead() {
        return read;
    }

    public boolean hasImage() {
        return hasImage;
    }

    public Bitmap getImage() {
        return image;
    }

    public long getFeedId() {
        return feedId;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
