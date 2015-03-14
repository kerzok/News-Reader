package ru.nsmelik.newsreader.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import ru.nsmelik.newsreader.helpers.DatabaseHelper;
import ru.nsmelik.newsreader.wrapper.ArticleWrapper;
import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.database.DatabaseSingleton;


public class ArticleActivity extends ActionBarActivity {
    private ViewPager mViewPager;
    private DatabaseHelper helper;
    private ArrayList<ArticleWrapper> articles;
    private MenuItem menuItem;

    //region initialization
    private void initialize() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        helper = DatabaseSingleton.getInstance().getHelper();
        long feedId = getIntent().getExtras().getLong("feedId");
        articles = helper.getArticlesList(feedId);
        PageFragment.setArticlesList(articles);
        initViewPager();
    }

    private void initViewPager() {
        int articleId = getIntent().getExtras().getInt("articleId");
        PagerAdapter mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.article_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(articleId);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                helper.setRead(articles.get(position).getId());
                menuItem.setIcon(articles.get(position).isFavourite() ? (R.drawable.ic_bookmark_white_36dp) :
                        R.drawable.ic_bookmark_outline_white_36dp);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        initialize();
    }

    //region menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(getApplication()).inflate(R.menu.article, menu);
        menuItem = menu.findItem(R.id.menu_add_to_favourite);
        ArticleWrapper temp = articles.get(mViewPager.getCurrentItem());
        menuItem.setIcon(temp.isFavourite() ? (R.drawable.ic_bookmark_white_36dp) :
                R.drawable.ic_bookmark_outline_white_36dp);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add_to_favourite) {
            articles.get(mViewPager.getCurrentItem()).
                    setFavourite(!articles.get(mViewPager.getCurrentItem()).isFavourite());
            ArticleWrapper temp = articles.get(mViewPager.getCurrentItem());
            helper.addToFavourite(temp.getId(), temp.isFavourite());
            item.setIcon(temp.isFavourite() ? (R.drawable.ic_bookmark_white_36dp) :
                    R.drawable.ic_bookmark_outline_white_36dp);
        }
        if (id == R.id.menu_item_share){
            startActivity(Intent.createChooser(createShareIntent(), getResources().getString(R.string.share_via)));
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref = sp.getBoolean("volume_button_move", true);
        if (pref) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (action == KeyEvent.ACTION_DOWN) {
                        mViewPager.setCurrentItem((mViewPager.getCurrentItem() - 1 == 0) ?
                                0 : mViewPager.getCurrentItem() - 1);
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (action == KeyEvent.ACTION_DOWN) {
                        mViewPager.setCurrentItem((mViewPager.getCurrentItem() + 1 == articles.size()) ?
                                articles.size() : mViewPager.getCurrentItem() + 1);
                    }
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String text = Html.fromHtml(articles.get(mViewPager.getCurrentItem()).getDescription()).toString();
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        return shareIntent;
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return articles.size();
        }

    }

    public static class PageFragment extends Fragment {

        private SimpleDateFormat patternDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        private SimpleDateFormat patternTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

        private static final String ARG_POSITION = "section_number";
        static private ArrayList<ArticleWrapper> articles;

        public static PageFragment newInstance(int position) {
            PageFragment pageFragment = new PageFragment();
            Bundle arguments = new Bundle();
            arguments.putInt(ARG_POSITION, position);
            pageFragment.setArguments(arguments);
            return pageFragment;
        }

        public static void setArticlesList(ArrayList<ArticleWrapper> articlesList) {
            articles = articlesList;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getActivity().setTitle("");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_page, null);
            WebView webView = (WebView) view.findViewById(R.id.webView);
            int position = getArguments().getInt(ARG_POSITION);

            ArticleWrapper article = articles.get(position);
            long pubDate = article.getPubDate();
            String time;
            if (System.currentTimeMillis() - pubDate < 3600000) {
                time = DateUtils.getRelativeTimeSpanString(pubDate).toString();
            } else if (DateUtils.isToday(pubDate)) {
                time = getString(R.string.today) + " " + patternTime.format(pubDate);
            } else {
                time = patternDate.format(pubDate);
            }
            String content = getString(R.string.detail, article.getTitle(), time, article.getDescription(), article.getLink());

            webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);
            return view;
        }
    }

}
