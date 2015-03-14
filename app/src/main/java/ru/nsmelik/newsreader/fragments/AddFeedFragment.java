package ru.nsmelik.newsreader.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import ru.nsmelik.newsreader.FeedLoadService;
import ru.nsmelik.newsreader.helpers.DatabaseHelper;
import ru.nsmelik.newsreader.wrapper.FeedWrapper;
import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.database.DatabaseSingleton;


public class AddFeedFragment extends Fragment {

    private static final String HTTP = "http://";

    private EditText url;
    private EditText name;
    private DatabaseHelper helper;
    private long id = 1;
    private Context context;
    private boolean edit = false;

    public AddFeedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        helper = DatabaseSingleton.getInstance().getHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_feed_layout, container, false);
        url = (EditText) view.findViewById(R.id.edit_address);
        name = (EditText) view.findViewById(R.id.edit_name);
        Button addButton = (Button) view.findViewById(R.id.add_button);

        Bundle arguments = getArguments();
        if (arguments != null) {
            id = arguments.getLong("id", -1);
            edit = true;
            FeedWrapper feed = helper.getFeed(id);
            url.setText(feed.getUrl());
            name.setText(feed.getName());
            addButton.setText(getResources().getString(R.string.edit_button));
        }
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url.getText() != null) {
                    String _url = url.getText().toString();
                    if (!_url.substring(0, 7).equals(HTTP))
                        _url = HTTP + _url;
                    String _name = name.getText().toString();
                    if (_name.equals("")) {
                        _name = _url;
                    }
                    if (edit)
                        helper.updateFeed(id, _name, _url);
                    else
                        FeedLoadService.updateFeed(context, helper.addFeed(new FeedWrapper(_name, _url, null)));
                    getActivity().finish();
                }
            }
        });
        return view;
    }
}
