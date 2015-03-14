package ru.nsmelik.newsreader.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import ru.nsmelik.newsreader.FeedLoadService;
import ru.nsmelik.newsreader.helpers.DatabaseHelper;
import ru.nsmelik.newsreader.wrapper.FeedWrapper;
import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.adapters.AddFeedListAdapter;
import ru.nsmelik.newsreader.database.DatabaseSingleton;


public class SearchFeedFragment extends Fragment implements FeedLoadService.LoadServiceCallback {

    private static final IntentFilter UPDATE_FILTER = new IntentFilter(FeedLoadService.SUCCESS);

    private ArrayList<FeedWrapper> feeds = new ArrayList<>();
    private Context context;
    private ListView result;
    private AddFeedListAdapter adapter;
    private DatabaseHelper helper;
    private FeedLoadService.LoadServiceCallback callback;
    private ProgressBar progressBar;
    private Activity activity;
    private EditText text;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter = new AddFeedListAdapter(context, feeds);
            result.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            result.setVisibility(View.VISIBLE);
        }
    };


    public SearchFeedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        activity = getActivity();
        helper = DatabaseSingleton.getInstance().getHelper();
        callback = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_feed_layout, container, false);

        text = (EditText) view.findViewById(R.id.search_box);
        Button search = (Button) view.findViewById(R.id.search_button);
        result = (ListView) view.findViewById(R.id.results);
        progressBar = (ProgressBar) view.findViewById(R.id.find_feed_progress);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String request = text.getText().toString();
                if (request.length() > 0) {
                    FeedLoadService.findFeed(context, request, callback);
                    result.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    hideSoftKeyboard(activity);
                }
            }
        });

        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FeedWrapper feed = (FeedWrapper)adapter.getItem(position);
                FeedLoadService.updateFeed(context, helper.addFeed(feed));
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void getFeeds(ArrayList<FeedWrapper> feeds) {
        this.feeds = feeds;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, UPDATE_FILTER);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //noinspection ConstantConditions
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

}
