package ru.nsmelik.newsreader.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.database.DatabaseSingleton;
import ru.nsmelik.newsreader.database.DatabaseTable;
import ru.nsmelik.newsreader.helpers.DatabaseHelper;
import ru.nsmelik.newsreader.helpers.Utils;

/**
 * Created by Nick.
 */
public class FeedListAdapter extends CursorAdapter {
    public static int ID = 0;
    private DatabaseHelper helper;

    public FeedListAdapter(Context context, int flags) {
        super(context, DatabaseSingleton.getInstance().getHelper().getFeedsCursor(), flags);
        helper = DatabaseSingleton.getInstance().getHelper();
    }

    static class ViewHolder {
        public int iconIndex;
        public int titleIndex;

        public ImageView iconView;
        public TextView  titleView;
        public TextView  countView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_feed, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.iconView = (ImageView) view.findViewById(R.id.feed_icon);
        holder.titleView = (TextView) view.findViewById(R.id.feed_name);
        holder.countView = (TextView) view.findViewById(R.id.feed_count);

        holder.iconIndex = cursor.getColumnIndex(DatabaseTable.ICON);
        holder.titleIndex = cursor.getColumnIndex(DatabaseTable.NAME);
        view.setTag(holder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        setFeedInformation(context, cursor, (ViewHolder) view.getTag());
    }

    private void setFeedInformation(Context context, Cursor cursor, ViewHolder holder) {
        holder.titleView.setText(cursor.getString(holder.titleIndex));
        int count = helper.getUnreadArticles(cursor.getLong(0));
        if (count == 0) {
            holder.countView.setBackground(context.getResources().getDrawable(R.drawable.transparent_square));
            holder.countView.setText("");
        } else {
            holder.countView.setBackground(context.getResources().getDrawable(R.drawable.square));
            holder.countView.setText(Integer.toString(count));
        }
        byte[] blob = cursor.getBlob(holder.iconIndex);
        if (blob != null) {
            holder.iconView.setImageBitmap(Utils.convertByteArrayToImage(blob));
        }
    }
}
