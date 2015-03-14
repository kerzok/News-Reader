package ru.nsmelik.newsreader.adapters;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.nsmelik.newsreader.wrapper.FeedWrapper;
import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.database.DatabaseTable;
import ru.nsmelik.newsreader.database.DbOpenHelper;

/**
 * Created by Smelik Nick on 11.07.2014.
 */
public class AddFeedListAdapter extends BaseAdapter {

    ArrayList<FeedWrapper> currentState;
    DatabaseTable table;
    Context context;

    public AddFeedListAdapter(Context context, ArrayList<FeedWrapper> feeds) {
        this.context = context;
        SQLiteOpenHelper helper = new DbOpenHelper(context);
        table = new DatabaseTable(helper.getWritableDatabase());
        currentState = feeds;
    }

    @Override
    public int getCount() {
        return currentState.size();
    }

    @Override
    public Object getItem(int position) {
        return currentState.get(position);
    }

    @Override
    public long getItemId(int position) {
        return currentState.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.item_add_feed, parent, false);

        TextView feedName = (TextView) convertView.findViewById(R.id.feedName);
        TextView feedUrl = (TextView) convertView.findViewById(R.id.feedUrl);
        ImageView feedIcon = (ImageView) convertView.findViewById(R.id.feedIcon);
        FeedWrapper feed = currentState.get(position);
        feedName.setText(feed.getName());
        feedUrl.setText(feed.getUrl());
        feedIcon.setImageBitmap(feed.getIcon());
        return convertView;
    }
}
