package ru.ifmo.md.lesson5;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends Activity {

    static URL[] addrs;

    static {
        try {
            addrs = new URL[3];
            addrs[0] = new URL("http://feeds.bbci.co.uk/news/rss.xml");
            addrs[1] = new URL("http://echo.msk.ru/interview/rss-fulltext.xml");
            addrs[2] = new URL("http://bash.im/rss/");
        } catch (MalformedURLException ignore) {
        }
    }

    class FeedListAdapter implements ListAdapter {

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            RssStorage.getObserverInstance().registerObserver(dataSetObserver);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            RssStorage.getObserverInstance().unregisterObserver(dataSetObserver);
        }

        @Override
        public int getCount() {
            RssData[] feeds = RssStorage.getFeeds();
            return feeds == null ? 0 : feeds.length;
        }

        @Override
        public Object getItem(int i) {
            RssData[] feeds = RssStorage.getFeeds();
            return feeds == null ? null : feeds[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View w = view;
            if (w == null)
                w = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_news_entry, null);
            RssData[] feeds = RssStorage.getFeeds();
            ((TextView) w.findViewById(R.id.textHeading)).setText(feeds[i].rssName);
            ((TextView) w.findViewById(R.id.textDescription)).setText(Html.fromHtml(feeds[i].rssDescription));
            w.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent it = new Intent(MainActivity.this, RssActivity.class);
                    it.putExtra("feedId", i);
                    MainActivity.this.startActivity(it);
                }
            });
            return w;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return RssStorage.getFeeds() == null || RssStorage.getFeeds().length == 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ListView) findViewById(R.id.listViewFeeds)).setAdapter(new FeedListAdapter());

        RssStorage.preloadFeeds(addrs);
    }
}
