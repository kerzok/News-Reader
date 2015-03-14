package ru.nsmelik.newsreader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.nsmelik.newsreader.helpers.Utils;
import ru.nsmelik.newsreader.wrapper.ArticleWrapper;
import ru.nsmelik.newsreader.wrapper.FeedWrapper;

/**
 * Created by Nick Smelik.
 */
public class DatabaseTable {
    //region Rows
    /**
     * Names of database table
     */
    public static final String TABLE_FEED_NAME = "feeds";
    public static final String TABLE_ARTICLE_NAME = "articles";

    /**
     * Rows of feed table
     */
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String ICON = "icon";
    public static final String LAST_UPDATE = "last_update";

    /**
     * Rows of article table
     */
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String PUB_DATE = "pub_date";
    public static final String LINK = "link";
    public static final String READ = "read";
    public static final String FEED_ID = "feed_id";
    public static final String FAVOURITE = "favourite";
    public static final String IMAGE = "image";
    public static final String FEED_NAME_FOR_ARTICLES = "feed_name";


    private static final String CREATE_FEED_TABLE = String.format(
            "create table %s (" +
                    "_id integer not null primary key autoincrement," +
                    "%s text not null," +
                    "%s text default null," +
                    "%s blob default null," +
                    "%s integer default 0" +
                    ")",
            TABLE_FEED_NAME,
            NAME,
            URL,
            ICON,
            LAST_UPDATE
    );

    private static final String CREATE_ARTICLE_TABLE = String.format(
            "create table %s (" +
                    "_id integer not null primary key autoincrement," +
                    "%s integer not null," +
                    "%s text not null," +
                    "%s text not null," +
                    "%s text default null," +
                    "%s integer not null," +
                    "%s text not null," +
                    "%s boolean default false," +
                    "%s integer default 0," +
                    "%s blob default null" +
                    ")",
            TABLE_ARTICLE_NAME,
            FEED_ID,
            TITLE,
            DESCRIPTION,
            NAME,
            PUB_DATE,
            LINK,
            READ,
            FAVOURITE,
            IMAGE
    );
    //endregion

    //region Queries
    /**
     * Queries for tables
     */
    private static final String DROP_TABLE_QUERY = "drop table if exists " + TABLE_FEED_NAME;
    private static final String SELECT_ALL_FEEDS_QUERY = "select * from " + TABLE_FEED_NAME;
    private static final String SELECT_FEED_QUERY = "select * from " + TABLE_FEED_NAME + " todo " +
            "where _id = ?";
    private static final String SELECT_UNREAD_ARTICLE_QUERY = "select * from " + TABLE_ARTICLE_NAME + " todo " +
            "where " + FEED_ID + " = ? and " + READ + " = 0";
    private static final String SELECT_FAVOURITE_UNREAD_ARTICLE_QUERY = "select * from " + TABLE_ARTICLE_NAME + " todo " +
            "where " + FAVOURITE + " = 1 and " + READ + " = 0";
    private static final String SELECT_ALL_UNREAD_ARTICLE_QUERY = "select * from " + TABLE_ARTICLE_NAME + " todo " +
            "where " + READ + " = 0";
    private static final String SELECT_ARTICLE_TO_UPDATE_QUERY = "select * from " + TABLE_ARTICLE_NAME + " todo " +
            "where " + TITLE + " = ?";
    private static final String SELECT_ALL_ARTICLES = "select " + TABLE_ARTICLE_NAME + ".*, " + TABLE_FEED_NAME + "." + NAME + " as " + FEED_NAME_FOR_ARTICLES +
            " from " + TABLE_ARTICLE_NAME + " inner join " + TABLE_FEED_NAME +
            " on articles." + FEED_ID + " = feeds._id order by " + PUB_DATE + " desc";
    private static final String SELECT_FAVOURITE_ARTICLES = "select " + TABLE_ARTICLE_NAME + ".*, " + TABLE_FEED_NAME + "." + NAME + " as " + FEED_NAME_FOR_ARTICLES +
            " from " + TABLE_ARTICLE_NAME + " inner join " + TABLE_FEED_NAME +
            " on articles." + FEED_ID + " = feeds._id where " + FAVOURITE + " = 1 order by " + PUB_DATE + " desc";
    //endregion

    //region Initial
    private final SQLiteDatabase db;

    public static void init(SQLiteDatabase db) {
        db.execSQL(CREATE_FEED_TABLE);
        db.execSQL(CREATE_ARTICLE_TABLE);
    }

    public static void drop(SQLiteDatabase db) {
        db.execSQL(DROP_TABLE_QUERY);
    }

    public DatabaseTable(SQLiteDatabase db) {
        this.db = db;
    }
    //endregion

    //region Feed
    public long addFeed(FeedWrapper feed) {
        ContentValues values = new ContentValues(3);
        values.put(NAME, feed.getName());
        values.put(URL, feed.getUrl());
        values.put(ICON, Utils.convertImageToByteArray(feed.getIcon()));
        return db.insertOrThrow(TABLE_FEED_NAME, null, values);
    }

    public void updateFeedIcon(long id, Bitmap icon) {
        ContentValues values = new ContentValues(1);
        values.put(ICON, Utils.convertImageToByteArray(icon));
        db.update(TABLE_FEED_NAME, values, "_id = ?", new String[]{Long.toString(id)});
    }

    public void updateFeed(long id, ArrayList<ArticleWrapper> articles, long updateTime) {
        ContentValues values = new ContentValues(1);
        values.put(LAST_UPDATE, updateTime);
        db.update(TABLE_FEED_NAME, values, "_id = ?", new String[]{Long.toString(id)});
        values = new ContentValues(8);
        for (ArticleWrapper article : articles) {
            if (!getArticleByTitle(article.getTitle())) {
                values.put(FEED_ID, id);
                values.put(TITLE, article.getTitle());
                values.put(DESCRIPTION, article.getDescription());
                values.put(NAME, article.getFeedName());
                values.put(PUB_DATE, article.getPubDate());
                values.put(LINK, article.getLink());
                values.put(READ, article.isRead());
                values.put(IMAGE, Utils.convertImageToByteArray(article.getImage()));
                db.insertOrThrow(TABLE_ARTICLE_NAME, null, values);
            }
        }
    }

    public void updateFeed(long id, String feedName, String feedUrl) {
        ContentValues values = new ContentValues(2);
        values.put(NAME, feedName);
        values.put(URL, feedUrl);
        db.update(TABLE_FEED_NAME, values, "_id = ?", new String[]{Long.toString(id)});
    }

    public Cursor getFeedsCursor() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_ALL_FEEDS_QUERY, null);
            return cursor;
        } catch (Exception e) {
            Log.e("getFeedsList", e.toString());
        }
        return null;
    }

    public LinkedList<FeedWrapper> getFeedsList() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_ALL_FEEDS_QUERY, null);
            LinkedList<FeedWrapper> result = new LinkedList<>();
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                String url = cursor.getString(2);
                Bitmap icon = Utils.convertByteArrayToImage(cursor.getBlob(3));
                long updated = cursor.getLong(4);
                result.addLast(new FeedWrapper(
                        id,
                        name,
                        url,
                        icon,
                        updated,
                        getArticlesCursor(id)
                ));
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public FeedWrapper getFeed(long rowId) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_FEED_QUERY, new String[]{Long.toString(rowId)});
            FeedWrapper result = null;
            if (cursor != null) {
                cursor.moveToFirst();
                long id = cursor.getLong(0);
                String feedName = cursor.getString(1);
                String feedUrl = cursor.getString(2);
                Bitmap feedIcon = Utils.convertByteArrayToImage(cursor.getBlob(3));
                long updated = cursor.getLong(4);

                result = new FeedWrapper(
                        id,
                        feedName,
                        feedUrl,
                        feedIcon,
                        updated,
                        getArticlesCursor(id)
                );
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void deleteFeed(long id) {
        db.delete(TABLE_FEED_NAME, "_id = ?", new String[]{Long.toString(id)});
        db.delete(TABLE_ARTICLE_NAME, FEED_ID + " = ?", new String[]{Long.toString(id)});
    }
    //endregion

    //region Articles
    public void deleteArticle(long id) {
        db.delete(TABLE_ARTICLE_NAME, "_id = ?", new String[]{Long.toString(id)});
    }

    public void deleteArticles(long currentTime, long period) {
        db.delete(TABLE_ARTICLE_NAME, "? - " + PUB_DATE + " > " + period + " and " + FAVOURITE + " = 0", new String[]{Long.toString(currentTime)});
    }

    public void deleteRead(long id) {
        db.delete(TABLE_ARTICLE_NAME, FEED_ID + " = ? AND " + READ + " = 1 AND " + FAVOURITE + " = 0", new String[]{Long.toString(id)});
    }

    public void deleteAllRead() {
        db.delete(TABLE_ARTICLE_NAME, FAVOURITE + " = 0 AND " + READ + " = 1", null);
    }

    public void deleteAllFavouriteRead() {
        db.delete(TABLE_ARTICLE_NAME, READ + " = 1 AND " + FAVOURITE + " = 1", null);
    }

    private boolean getArticleByTitle(String title) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_ARTICLE_TO_UPDATE_QUERY, new String[]{title});
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void addToFavourite(long id, boolean check) {
        ContentValues value = new ContentValues(1);
        value.put(FAVOURITE, check ? 1 : 0);
        db.update(TABLE_ARTICLE_NAME, value, "_id = ?", new String[]{Long.toString(id)});
    }

    public void setRead(long id) {
        ContentValues values = new ContentValues(1);
        values.put(READ, 1);
        db.update(TABLE_ARTICLE_NAME, values, "_id = ?", new String[]{Long.toString(id)});
    }

    public int markAsRead(long id) {
        ContentValues values = new ContentValues(1);
        values.put(READ, 1);
        return db.update(TABLE_ARTICLE_NAME, values, FEED_ID + " = ?", new String[]{Long.toString(id)});
    }

    public int markAllAsRead() {
        ContentValues values = new ContentValues(1);
        values.put(READ, 1);
        return db.update(TABLE_ARTICLE_NAME, values, null, null);
    }

    public int markFavouriteAsRead() {
        ContentValues values = new ContentValues(1);
        values.put(READ, 1);
        return db.update(TABLE_ARTICLE_NAME, values, FAVOURITE + " = 1", null);
    }

    public int getUnreadArticles(long id) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_UNREAD_ARTICLE_QUERY, new String[]{Long.toString(id)});
            return cursor.getCount();
        } finally {
            if (cursor != null) {
                //cursor.close();
            }
        }
    }

    public int getAllUnreadArticles() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_ALL_UNREAD_ARTICLE_QUERY, null);
            return cursor.getCount();
        } finally {
            if (cursor != null) {
                //cursor.close();
            }
        }
    }

    public int getFavouriteUnreadArticles() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_FAVOURITE_UNREAD_ARTICLE_QUERY, null);
            return cursor.getCount();
        } finally {
            if (cursor != null) {
                //cursor.close();
            }
        }
    }

    public Cursor getArticlesCursor(long id) {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_ARTICLE_NAME, new String[]{"_id", FEED_ID, TITLE, DESCRIPTION, NAME, PUB_DATE, LINK, READ, FAVOURITE, IMAGE},
                    FEED_ID + " = ?", new String[]{Long.toString(id)}, null, null, PUB_DATE + " DESC", null);
            return cursor;
        } finally {
            if (cursor != null) {
                //cursor.close();
            }
        }
    }

    public Cursor getAllFavouriteCursor() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_FAVOURITE_ARTICLES, null);
            return cursor;
        } finally {
            if (cursor != null) {
                //cursor.close();
            }
        }
    }

    public Cursor getAllArticlesCursor() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SELECT_ALL_ARTICLES, null);
            return cursor;
        } finally {
            if (cursor != null) {
                //cursor.close();
            }
        }
    }

    public ArrayList<ArticleWrapper> getArticlesList(long id) {Cursor cursor = null;
        try {
            cursor = db.query(TABLE_ARTICLE_NAME, new String[]{"_id", FEED_ID, TITLE, DESCRIPTION, NAME, PUB_DATE, LINK, READ, FAVOURITE, IMAGE},
                    FEED_ID + " = ?", new String[]{Long.toString(id)}, null, null, PUB_DATE + " DESC", null);
            ArrayList<ArticleWrapper> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                long articleId = cursor.getLong(0);
                long feedId = cursor.getLong(1);
                String title = cursor.getString(2);
                String description = cursor.getString(3);
                String name = cursor.getString(4);
                long pubDate = cursor.getLong(5);
                String link = cursor.getString(6);
                Boolean read = cursor.getInt(7) > 0;
                Boolean favourite = cursor.getInt(8) > 0;
                Bitmap image = Utils.convertByteArrayToImage(cursor.getBlob(9));
                result.add(new ArticleWrapper(title, description, pubDate, link, articleId, feedId, name, read, favourite, image));
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public ArrayList<ArticleWrapper> getAllFavouriteList() {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_ARTICLE_NAME, new String[]{"_id", FEED_ID, TITLE, DESCRIPTION, NAME, PUB_DATE, LINK, READ, FAVOURITE, IMAGE},
                    FAVOURITE + " = 1", null, null, null, PUB_DATE + " DESC", null);
            ArrayList<ArticleWrapper> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                long articleId = cursor.getLong(0);
                long feedId = cursor.getLong(1);
                String title = cursor.getString(2);
                String description = cursor.getString(3);
                String name = cursor.getString(4);
                long pubDate = cursor.getLong(5);
                String link = cursor.getString(6);
                Boolean read = cursor.getInt(7) > 0;
                Boolean favourite = cursor.getInt(8) > 0;
                Bitmap image = Utils.convertByteArrayToImage(cursor.getBlob(9));
                result.add(new ArticleWrapper(title, description, pubDate, link, articleId, feedId, name, read, favourite, image));
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public ArrayList<ArticleWrapper> getAllArticlesList() {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_ARTICLE_NAME, new String[]{"_id", FEED_ID, TITLE, DESCRIPTION, NAME, PUB_DATE, LINK, READ, FAVOURITE, IMAGE},
                    null, null, null, null, PUB_DATE + " DESC", null);
            ArrayList<ArticleWrapper> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                long articleId = cursor.getLong(0);
                long feedId = cursor.getLong(1);
                String title = cursor.getString(2);
                String description = cursor.getString(3);
                String name = cursor.getString(4);
                long pubDate = cursor.getLong(5);
                String link = cursor.getString(6);
                Boolean read = cursor.getInt(7) > 0;
                Boolean favourite = cursor.getInt(8) > 0;
                Bitmap image = Utils.convertByteArrayToImage(cursor.getBlob(9));
                result.add(new ArticleWrapper(title, description, pubDate, link, articleId, feedId, name, read, favourite, image));
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    //endregion
}

