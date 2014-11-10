package ru.ifmo.md.lesson5;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;


public class RssActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss);

        ListView listView = (ListView) findViewById(R.id.rssListView);
        listView.setAdapter(new RssArticlesAdapter(getIntent().getIntExtra("feedId", -1), this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            RssStorage.refreshFeeds(getApplicationContext());
        }
        return super.onOptionsItemSelected(item);
    }

}
