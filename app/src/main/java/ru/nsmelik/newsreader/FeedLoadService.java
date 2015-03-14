package ru.nsmelik.newsreader;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;

import ru.nsmelik.newsreader.database.DatabaseSingleton;
import ru.nsmelik.newsreader.helpers.DatabaseHelper;
import ru.nsmelik.newsreader.wrapper.ArticleWrapper;
import ru.nsmelik.newsreader.wrapper.FeedWrapper;

/**
 * Created by Smelik Nick.
 */
public class FeedLoadService extends IntentService {

    public interface LoadServiceCallback {
        void getFeeds(ArrayList<FeedWrapper> feeds);
    }

    public final static String TAG = FeedLoadService.class.toString();
    public final static String FIND = TAG.concat(":find");
    public final static String LOAD = TAG.concat(":load");
    public final static String PLANNED = TAG.concat(":planned");
    public final static String SUCCESS = TAG.concat(":success");
    public final static String FAIL = TAG.concat(":fail");
    public final static String CONNECTION = TAG.concat(":connection");

    private final static long DELAY = 300000;
    private static long SYNC_TIME = 3600000;
    private static long ARTICLES_COUNT = 50;
    private static int CONNECTION_TIMEOUT = 30000;

    private final static String apiFindServer = "https://ajax.googleapis.com/ajax/services/feed/find?";
    private final static String apiLoadServer = "https://ajax.googleapis.com/ajax/services/feed/load?";
    private final static String apiVersion = "v=1.0&q=";
    private final static String apiCount = "&num=";
    private final static String iconSearch = "http://favicon.yandex.net/favicon/";

    private DatabaseHelper helper;
    private static LoadServiceCallback invoker;

    @Override
    public void onCreate() {
        super.onCreate();
        helper = DatabaseSingleton.getInstance().getHelper();
    }

    public static void ensureUpdating(Context context, boolean now) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SYNC_TIME = Integer.parseInt(sp.getString("sync_frequency", "3600000"));
        ARTICLES_COUNT = Integer.parseInt(sp.getString("count_of_articles", "50"));

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                now ? 0 : DELAY,
                DELAY,
                PendingIntent.getService(
                        context,
                        0,
                        new Intent(context, FeedLoadService.class)
                                .setAction(PLANNED),
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
        );
    }

    public FeedLoadService() {
        super("FeedLoadService");
    }

    public static void findFeed(Context context, String name, LoadServiceCallback callback) {
        invoker = callback;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SYNC_TIME = Integer.parseInt(sp.getString("sync_frequency", "3600000"));
        ARTICLES_COUNT = Integer.parseInt(sp.getString("count_of_articles", "50"));
        context.startService(new Intent(context, FeedLoadService.class).setAction(FIND).putExtra("name", name));
    }

    public static void updateFeed(Context context, long id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SYNC_TIME = Integer.parseInt(sp.getString("sync_frequency", "3600000"));
        ARTICLES_COUNT = Integer.parseInt(sp.getString("count_of_articles", "50"));
        context.startService(new Intent(context, FeedLoadService.class).setAction(LOAD).putExtra("id", id));
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (isNetworkConnected()) {
            if (action.equals(FIND)) {
                String name = intent.getStringExtra("name");
                findFeed(name);
            } else if (action.equals(LOAD)) {
                long id = intent.getLongExtra("id", -1);
                if (id == -1) {
                    updateAllFeeds(false);
                } else {
                    loadArticles(id, false, false);
                }
            } else if (action.equals(PLANNED)) {
                updateAllFeeds(true);
            }
        } else {
            sendBroadcast(new Intent(CONNECTION));
        }
    }

    private void findFeed(String name) {
        URL url;
        try {
            url = new URL(apiFindServer + apiVersion + name);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject json = new JSONObject(builder.toString());
            if (json.getInt("responseStatus") == 200) {
                JSONObject result = json.getJSONObject("responseData");
                JSONArray array = result.getJSONArray("entries");
                ArrayList<FeedWrapper> feeds = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject resultFeed = array.getJSONObject(i);
                    String feedName = Html.fromHtml(resultFeed.getString("title")).toString();
                    String feedUrl = resultFeed.getString("url");
                    Bitmap feedIcon = getFeedIcon(feedUrl);
                    feeds.add(new FeedWrapper(feedName, feedUrl, feedIcon));
                }
                invoker.getFeeds(feeds);
            } else {
                throw new JSONException("Invalid response");
            }
        } catch (IOException | JSONException e) {
            sendBroadcast(new Intent(FAIL));
            e.printStackTrace();
        }
        sendBroadcast(new Intent(SUCCESS));
    }

    private void updateAllFeeds(boolean planned) {
        LinkedList<FeedWrapper> feeds = helper.getFeedsList();
        for (FeedWrapper feed : feeds) {
            loadArticles(feed.getId(), true, planned);
        }
        sendBroadcast(new Intent(SUCCESS));
    }

    private void loadArticles(long id, boolean massive, boolean planned) {
        URL url;
        try {
            FeedWrapper feed = helper.getFeed(id);
            if (!planned || System.currentTimeMillis() - feed.getLastUpdate() >= SYNC_TIME) {
                if (feed.getIcon() == null) {
                    Bitmap icon = getFeedIcon(feed.getUrl());
                    helper.updateFeedIcon(id, icon);
                }
                url = new URL(apiLoadServer + apiVersion + feed.getUrl() + apiCount + ARTICLES_COUNT);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                JSONObject json = new JSONObject(builder.toString());
                if (json.getInt("responseStatus") == 200) {
                    JSONObject result = json.getJSONObject("responseData").getJSONObject("feed");
                    JSONArray array = result.getJSONArray("entries");
                    ArrayList<ArticleWrapper> articlesList = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject article = array.getJSONObject(i);
                        String title = article.getString("title");
                        String link = article.getString("link");
                        String date = article.getString("publishedDate");
                        String description = article.getString("content");
                        articlesList.add(new ArticleWrapper(title, description, date, link));
                    }
                    helper.updateFeed(id, articlesList, System.currentTimeMillis());
                } else {
                    throw new JSONException("Invalid response");
                }
            }
        } catch (IOException | JSONException e) {
            sendBroadcast(new Intent(FAIL));
            e.printStackTrace();
        }
        if (!massive) {
            sendBroadcast(new Intent(SUCCESS));
        }
    }

    private Bitmap getFeedIcon(String feedUrl) {
        try {
            URL iconUrl = new URL(iconSearch + feedUrl.substring(7));
            HttpURLConnection httpConnection = (HttpURLConnection) iconUrl.openConnection();
            httpConnection.setConnectTimeout(15000);
            if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Some problem with Http Connection");
            }
            InputStream inputStream = httpConnection.getInputStream();
            return cropBitmap(BitmapFactory.decodeStream(inputStream));
        } catch (Exception e) {
            sendBroadcast(new Intent(FAIL));
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap cropBitmap(Bitmap image)
    {
        return Bitmap.createBitmap(image, 0, 0, 16, 16);
    }

    private void loadFullArticle() {

    }
}
