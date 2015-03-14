package ru.nsmelik.newsreader.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.database.DatabaseSingleton;
import ru.nsmelik.newsreader.database.DatabaseTable;
import ru.nsmelik.newsreader.helpers.Utils;

/**
 * Created by Smelik Nick.
 */
public class ArticlesListAdapter extends CursorAdapter {
    public static final int ID = 1;
    private SimpleDateFormat patternDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat patternTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ArticlesListAdapter(Context context, long id) {
        super(context,
                DatabaseSingleton.getInstance().getHelper().getArticlesCursor(id),
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    public static class ViewHolder {
        public int titleIndex;
        public int descriptionIndex;
        public int pubDateIndex;
        public int imageIndex;
        public int favouriteIndex;
        public int readIndex;
        public int feedNameIndex;
        public boolean isFavourite;

        public TextView title;
        public TextView description;
        public TextView pubDate;
        public TextView feedName;
        public ImageView image;
        public ImageView favourite;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_article, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.title = (TextView) view.findViewById(R.id.article_title);
        holder.description = (TextView) view.findViewById(R.id.article_description);
        holder.pubDate = (TextView) view.findViewById(R.id.article_date);
        holder.image = (ImageView) view.findViewById(R.id.article_image);
        holder.favourite = (ImageView) view.findViewById(R.id.favourite_image);
        holder.feedName = (TextView) view.findViewById(R.id.article_item_feed_name);

        holder.titleIndex = cursor.getColumnIndex(DatabaseTable.TITLE);
        holder.descriptionIndex = cursor.getColumnIndex(DatabaseTable.DESCRIPTION);
        holder.pubDateIndex = cursor.getColumnIndex(DatabaseTable.PUB_DATE);
        holder.imageIndex = cursor.getColumnIndex(DatabaseTable.IMAGE);
        holder.favouriteIndex = cursor.getColumnIndex(DatabaseTable.FAVOURITE);
        holder.readIndex = cursor.getColumnIndex(DatabaseTable.READ);
        holder.feedNameIndex = cursor.getColumnIndex(DatabaseTable.FEED_NAME_FOR_ARTICLES);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        setArticleInformation(context, cursor, (ViewHolder) view.getTag());
    }

    private ViewHolder setArticleInformation(Context context, Cursor cursor, ViewHolder holder) {
        holder.title.setText(cursor.getString(holder.titleIndex));
        holder.description.setText(Html.fromHtml(cursor.getString(holder.descriptionIndex)));
        holder.isFavourite = cursor.getInt(holder.favouriteIndex) > 0;

        Date pubDate = new Date(cursor.getLong(holder.pubDateIndex));
        if (System.currentTimeMillis() - pubDate.getTime() < 3600000) {
            holder.pubDate.setText(DateUtils.getRelativeTimeSpanString(pubDate.getTime()).toString());
        } else if (DateUtils.isToday(pubDate.getTime())) {
            holder.pubDate.setText(context.getString(R.string.today) + " " + patternTime.format(pubDate));
        } else {
            holder.pubDate.setText(patternDate.format(pubDate));
        }

        if (cursor.getInt(holder.favouriteIndex) > 0) {
            holder.favourite.setImageResource(R.drawable.ic_action_selected);
        } else {
            holder.favourite.setImageResource(R.drawable.ic_action_not_important);
        }

        if (cursor.getInt(holder.readIndex) > 0) {
            holder.title.setTextColor(Color.GRAY);
            holder.description.setTextColor(Color.GRAY);
            holder.pubDate.setTextColor(Color.GRAY);
            holder.feedName.setTextColor(Color.GRAY);
        } else {
            holder.title.setTextColor(Color.BLACK);
            holder.description.setTextColor(Color.BLACK);
            holder.pubDate.setTextColor(Color.BLACK);
            holder.feedName.setTextColor(Color.BLACK);
        }

        byte[] blob = cursor.getBlob(holder.imageIndex);
        if (blob != null) {
            holder.image.setImageBitmap(Utils.convertByteArrayToImage(blob));
        }

        if (cursor.getColumnIndex(DatabaseTable.FEED_NAME_FOR_ARTICLES) > 0)
            holder.feedName.setText(cursor.getString(holder.feedNameIndex));
        else
            holder.feedName.setText("");

        return holder;
    }
}
