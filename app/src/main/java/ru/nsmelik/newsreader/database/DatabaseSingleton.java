package ru.nsmelik.newsreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import ru.nsmelik.newsreader.NewsApplication;
import ru.nsmelik.newsreader.helpers.DatabaseHelper;

/**
 * Created by Nick on 29.11.2014.
 */
public class DatabaseSingleton {

    private static DatabaseSingleton instance;
    public DatabaseHelper helper;

    private DatabaseSingleton(Context context) {
        helper = new DatabaseHelper(context);
    }

    public static DatabaseSingleton getInstance() {
        if (instance == null) {
            instance = new DatabaseSingleton(NewsApplication.context);
        }
        return instance;
    }

    public DatabaseHelper getHelper() {
        return helper;
    }
}
