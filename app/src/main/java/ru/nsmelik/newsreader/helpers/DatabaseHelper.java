package ru.nsmelik.newsreader.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.nsmelik.newsreader.database.DatabaseTable;
import ru.nsmelik.newsreader.database.DbOpenHelper;
import ru.nsmelik.newsreader.wrapper.ArticleWrapper;
import ru.nsmelik.newsreader.wrapper.FeedWrapper;

/**
 * Created by Nick.
 */
public class DatabaseHelper {
    private DatabaseTable table;
    private Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
        SQLiteOpenHelper helper = new DbOpenHelper(context);
        table = new DatabaseTable(helper.getWritableDatabase());
    }

    //region Feed
    public long addFeed(FeedWrapper feed) {
        return table.addFeed(feed);
    }

    public void updateFeedIcon(long id, Bitmap icon) {
        table.updateFeedIcon(id, icon);
    }

    public void updateFeed(long id, ArrayList<ArticleWrapper> articles, long updateTime) {
        table.updateFeed(id, articles, updateTime);
    }

    public void updateFeed(long id, String feedName, String feedUrl) {
        table.updateFeed(id, feedName, feedUrl);
    }

    public Cursor getFeedsCursor() {
        return table.getFeedsCursor();
    }

    public LinkedList<FeedWrapper> getFeedsList() {
        return table.getFeedsList();
    }

    public FeedWrapper getFeed(long id) {
        return table.getFeed(id);
    }

    public void deleteFeed(long id) {
        table.deleteFeed(id);
    }
    //endregion

    //region Articles
    public void deleteArticle(long id) {
        table.deleteArticle(id);
    }

    public void deteleArticles() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long temp = Long.parseLong(sp.getString("auto_remove_old_articled", "604800000"));
        table.deleteArticles(System.currentTimeMillis(), temp);
    }

    public void deleteReaded(long id) {
        table.deleteRead(id);
    }

    public void deleteAllReaded() {
        table.deleteAllRead();
    }

    public void deleteAllFavouriteReaded() {
        table.deleteAllFavouriteRead();
    }

    public void addToFavourite(long id, boolean check) {
        table.addToFavourite(id, check);
    }

    public void setRead(long id) {
        table.setRead(id);
    }

    public int markFeedAsRead(long id) {
        if (id < 0)
            return ((id == -1) ? table.markAllAsRead() : table.markFavouriteAsRead());
        return table.markAsRead(id);
    }

    public int getUnreadArticles(long id) {
        if (id < 0)
            return (id == -1) ? table.getAllUnreadArticles() : table.getFavouriteUnreadArticles();
        return table.getUnreadArticles(id);
    }

    public Cursor getArticlesCursor(long id) {
        if (id < 0)
            return (id == -1) ? table.getAllArticlesCursor() : table.getAllFavouriteCursor();
        return table.getArticlesCursor(id);
    }

    public ArrayList<ArticleWrapper> getArticlesList(long id) {
        if (id < 0)
            return (id == -1) ? table.getAllArticlesList() : table.getAllFavouriteList();
        return table.getArticlesList(id);
    }
    //endregion
}
