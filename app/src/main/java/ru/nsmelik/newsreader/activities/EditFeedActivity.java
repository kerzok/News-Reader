package ru.nsmelik.newsreader.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import ru.nsmelik.newsreader.R;
import ru.nsmelik.newsreader.fragments.AddFeedFragment;


public class EditFeedActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle data = new Bundle();
        data.putLong("id", getIntent().getLongExtra("feed_id", -1));
        AddFeedFragment fragment = new AddFeedFragment();
        fragment.setArguments(data);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }
}
