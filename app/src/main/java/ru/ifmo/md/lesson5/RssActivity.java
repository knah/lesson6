package ru.ifmo.md.lesson5;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;


public class RssActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss);

        ListView listView = (ListView) findViewById(R.id.rssListView);
        listView.setAdapter(new RssArticlesAdapter(getIntent().getIntExtra("feedId", -1), this));
    }

}
