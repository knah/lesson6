package ru.ifmo.md.lesson5;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by kna on 17.10.2014.
 */
public class RssData {

    public final String rssName;
    public final String rssDescription;
    public final URL rssUrl;
    private ArrayList<RssEntry> entries;
    public RssData(String rssName, String rssDescription, URL rssUrl) {
        this.rssDescription = rssDescription;
        this.rssName = rssName;
        this.rssUrl = rssUrl;
        this.entries = new ArrayList<RssEntry>();
    }

    public void addEntry(String title, String description, URL url) {
        entries.add(new RssEntry(title, description, url));
    }

    public int getEntryCount() {
        return entries.size();
    }

    public RssEntry getEntry(int index) {
        return entries.get(index);
    }

    public class RssEntry {
        public final String title;
        public final String description;
        public final URL url;

        public RssEntry(String title, String description, URL url) {
            this.title = title;
            this.description = description;
            this.url = url;
        }
    }

}
