package ru.ifmo.md.lesson5;

import android.database.DataSetObservable;

import java.net.URL;

/**
 * Created by kna on 20.10.2014.
 */
public class RssStorage extends DataSetObservable {

    static private RssData[] feeds = null;
    static private RssStorage observerInstance = new RssStorage();

    private RssStorage() {
    }

    static public void preloadFeeds(URL[] addrs) {
        new AsyncFeedLoader(observerInstance, new AsyncFeedLoader.ResultCallback() {
            @Override
            public void run(RssData[] result) {
                feeds = result;
            }
        }).execute(addrs);
        observerInstance.notifyInvalidated();
    }

    static public RssStorage getObserverInstance() {
        return observerInstance;
    }

    static public RssData[] getFeeds() {
        return feeds;
    }

}
