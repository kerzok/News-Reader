package ru.nsmelik.newsreader.database;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.util.Log;

import ru.nsmelik.newsreader.helpers.DatabaseHelper;

/**
 * Created by Nick on 18.08.2014.
 */
public class DbCursorLoader extends CursorLoader {

    private DatabaseHelper helper;
    private long id;

    public DbCursorLoader(Context context, long id) {
        super(context);
        this.helper = DatabaseSingleton.getInstance().getHelper();
        this.id = id;
    }

    public DbCursorLoader(Context context) {
        super(context);
        this.helper = DatabaseSingleton.getInstance().getHelper();
        this.id = 0;
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cursor = null;
        try {
            if (id == 0) {
                cursor = helper.getFeedsCursor();
            } else {
                cursor = helper.getArticlesCursor(id);
            }
        } catch (Exception e) {
            Log.e("CursorLoader", "Something goes wrong while loading cursor");
        }
        return cursor;
    }
}
