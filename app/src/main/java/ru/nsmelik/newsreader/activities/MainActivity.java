package ru.nsmelik.newsreader.activities;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import ru.nsmelik.newsreader.FeedLoadService;
import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.adapters.ArticlesListAdapter;
import ru.nsmelik.newsreader.adapters.ArticlesListAdapter.ViewHolder;
import ru.nsmelik.newsreader.database.DatabaseSingleton;
import ru.nsmelik.newsreader.database.DbCursorLoader;
import ru.nsmelik.newsreader.fragments.NavigationDrawerFragment;
import ru.nsmelik.newsreader.helpers.DatabaseHelper;
import ru.nsmelik.newsreader.pulltorefresh.PullToRefresh;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LoaderManager.LoaderCallbacks<Cursor> {

    private LoaderManager.LoaderCallbacks<Cursor> callback;
    private ArticlesListAdapter articlesAdapter;
    private PullToRefresh mPullToRefreshLayout;
    private String mTitle;
    private DatabaseHelper table;
    private Context context;
    private long feedId = -1;

    //region initialization
    private void initialize() {
        mTitle = getResources().getString(R.string.all_name);
        context = this;
        callback = this;
        table = DatabaseSingleton.getInstance().getHelper();
        table.deteleArticles();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        restoreActionBar();
        ((NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer))
                .setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
        articlesAdapter = new ArticlesListAdapter(context, feedId);
        pullToRefreshInit();
        articleListInit();
    }

    private void pullToRefreshInit() {
        mPullToRefreshLayout = (PullToRefresh) findViewById(R.id.container);
        mPullToRefreshLayout.setup(this);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (feedId != -2) {
                    mPullToRefreshLayout.setRefreshing(true);
                    FeedLoadService.updateFeed(context, feedId);
                } else {
                    mPullToRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void articleListInit() {
        final ListView articlesList = (ListView) findViewById(R.id.article_list);
        articlesList.setAdapter(articlesAdapter);



        articlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, ArticleActivity.class);
                Bundle b = new Bundle();
                b.putLong("feedId", feedId);
                table.setRead(id);
                intent.putExtra("articleId", position);
                intent.putExtra("feedId", feedId);
                getLoaderManager().restartLoader(ArticlesListAdapter.ID, b, callback);
                startActivity(intent);
            }
        });

        articlesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder holder = (ViewHolder) view.getTag();
                if (holder.isFavourite) {
                    table.addToFavourite(id, false);
                } else {
                    table.addToFavourite(id, true);
                }
                holder.isFavourite = !holder.isFavourite;
                Bundle b = new Bundle();
                b.putLong("feedId", feedId);
                getLoaderManager().restartLoader(ArticlesListAdapter.ID, b, callback);
                return true;
            }
        });
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    @Override
    public void onNavigationDrawerItemSelected(long id, String feed_name) {
        feedId = id;
        Bundle b = new Bundle();
        b.putLong("feedId", id);
        getLoaderManager().restartLoader(ArticlesListAdapter.ID, b, this);
        mTitle = feed_name;
        restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    //region OptionMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(getApplication()).inflate(R.menu.global, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_new_feed:
                intent = new Intent(this, AddFeedActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_mark_all_as_read:
                table.markFeedAsRead(feedId);
                getLoaderManager().restartLoader(ArticlesListAdapter.ID, null, this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region CursorLoader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new DbCursorLoader(context, args.getLong("feedId"));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        articlesAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        articlesAdapter.changeCursor(null);
    }
    //endregion

    //region SaveAndRestoreState
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putLong("feedId", feedId);
        savedInstanceState.putCharSequence("title", mTitle);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        feedId = savedInstanceState.getLong("feedId", -1);
        mTitle = savedInstanceState.getString("title", getResources().getString(R.string.all_name));
        onNavigationDrawerItemSelected(feedId, mTitle);
        super.onRestoreInstanceState(savedInstanceState);
    }
    //endregion

    //region BroadcastReceivers
    private static final IntentFilter UPDATE_FILTER = new IntentFilter(FeedLoadService.SUCCESS);
    private static final IntentFilter FAILED_FILTER = new IntentFilter(FeedLoadService.FAIL);

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPullToRefreshLayout.setRefreshing(false);
            Bundle b = new Bundle();
            b.putLong("feedId", feedId);
            getLoaderManager().restartLoader(ArticlesListAdapter.ID, b, callback);
        }
    };

    private final BroadcastReceiver connectionFailedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPullToRefreshLayout.setRefreshing(false);
            Toast.makeText(context, getResources().getString(R.string.bad_internet_connection), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, UPDATE_FILTER);
        registerReceiver(connectionFailedReceiver, FAILED_FILTER);
    }

    @Override
    public void onPause() {
        unregisterReceiver(receiver);
        unregisterReceiver(connectionFailedReceiver);
        super.onPause();
    }
    //endregion
}
