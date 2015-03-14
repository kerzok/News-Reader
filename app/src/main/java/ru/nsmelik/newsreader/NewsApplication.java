package ru.nsmelik.newsreader;

import android.app.Application;
import android.content.Context;

/**
 * Created by Smelik Nick.
 */
public class NewsApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        FeedLoadService.ensureUpdating(context, false);
    }
}
