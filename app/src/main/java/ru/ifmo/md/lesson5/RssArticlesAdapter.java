package ru.ifmo.md.lesson5;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by kna on 17.10.2014.
 */
public class RssArticlesAdapter implements ListAdapter {

    private Set<DataSetObserver> observers = Collections.newSetFromMap(new WeakHashMap<DataSetObserver, Boolean>()); // todo: fix this maybe
    private final Context context;
    private final int feedId;


    public RssArticlesAdapter(int feedId, Context context) {
        this.context = context;
        this.feedId = feedId;
    }

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
        RssStorage.getObserverInstance().registerObserver(dataSetObserver); // this generally has to be fixed
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        RssStorage.getObserverInstance().unregisterObserver(dataSetObserver);
    }

    @Override
    public int getCount() {
        RssData rssData = RssStorage.getFeeds()[feedId];
        if (rssData == null)
            return 0;

        return rssData.getEntryCount();
    }

    @Override
    public Object getItem(int i) {
        RssData rssData = RssStorage.getFeeds()[feedId];
        if (rssData == null)
            return null;

        return rssData.getEntry(i);
    }

    @Override
    public long getItemId(int i) {
        return 0; // no idea
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        RssData rssData = RssStorage.getFeeds()[feedId];
        if (rssData == null)
            return null;

        View w = view;
        if (w == null)
            w = LayoutInflater.from(context).inflate(R.layout.layout_news_entry, null);
        final RssData.RssEntry entry = (RssData.RssEntry) getItem(i);
        ((TextView) w.findViewById(R.id.textHeading)).setText(entry.title);
        ((TextView) w.findViewById(R.id.textDescription)).setText(Html.fromHtml(entry.description));
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(context, ArticleViewActivity.class);
                it.putExtra("url", entry.url);
                context.startActivity(it);
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
        RssData rssData = RssStorage.getFeeds()[feedId];
        return rssData == null || rssData.getEntryCount() == 0;
    }

}
