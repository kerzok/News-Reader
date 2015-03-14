package ru.nsmelik.newsreader.wrapper;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * Created by Smelik Nick.
 */
public class FeedWrapper {
    private long id;
    private String name;
    private String url;
    private Bitmap icon;
    private long lastUpdate;
    private Cursor articles;

    public FeedWrapper(long id, String name, String url, Bitmap icon, long lastUpdate, Cursor articles) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.lastUpdate = lastUpdate;
        this.articles = articles;
    }

    public FeedWrapper(String name, String url, Bitmap icon) {
        this.name = name;
        this.url = url;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public Cursor getArticles() {
        return articles;
    }

    public long getId() {
        return id;
    }
}
