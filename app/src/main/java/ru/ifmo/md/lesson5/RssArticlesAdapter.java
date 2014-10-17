package ru.ifmo.md.lesson5;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by kna on 17.10.2014.
 */
public class RssArticlesAdapter implements ListAdapter {

    private Set<DataSetObserver> observers = Collections.newSetFromMap(new WeakHashMap<DataSetObserver, Boolean>()); // todo: fix this maybe
    private String rssUrl;
    private RssData rssData;
    private final Context context;

    private static class SAXRssHandler extends DefaultHandler {

        private RssData result;
        private String nameTmp;
        private String descrTmp;
        private URL urlTmp;
        private boolean isInRssTag;
        private boolean isInItemTag;
        private String someStringTmp;

        public RssData getResult() {
            return result;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            someStringTmp = "";
            if("channel".equalsIgnoreCase(qName)) {
                isInRssTag = true;
            } else if("item".equalsIgnoreCase(qName)) {
                isInItemTag = true;
                if(result == null)
                    if(isInRssTag)
                        result = new RssData(nameTmp, descrTmp);
                    else
                        throw new SAXException("Item tags out of RSS tag");
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            someStringTmp += String.copyValueOf(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if("channel".equalsIgnoreCase(qName)) {
                isInRssTag = false;
            } else if("item".equalsIgnoreCase(qName)) {
                isInItemTag = false;
                if(result != null)
                    result.addEntry(nameTmp, descrTmp, urlTmp);
                else
                    throw new SAXException("Items are in some weird place");
            }

            if("title".equalsIgnoreCase(qName)) {
                nameTmp = someStringTmp;
            } else if("link".equalsIgnoreCase(qName)) {
                try {
                    System.out.println(someStringTmp);
                    urlTmp = new URL(someStringTmp);

                } catch (MalformedURLException ex) {
                    throw new SAXException("Malformed URL", ex);
                }
            } else if("description".equalsIgnoreCase(qName)) {
                descrTmp = someStringTmp;
            }
        }
    }

    private class AsyncDataLoader extends AsyncTask<URL, Void, RssData> {

        @Override
        protected RssData doInBackground(URL... urls) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) (urls[0]).openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                try {
                    SAXParser p = SAXParserFactory.newInstance().newSAXParser();
                    SAXRssHandler hdl = new SAXRssHandler();
                    p.parse(is, hdl);
                    return hdl.getResult();
                } catch (SAXException se) {
                    se.printStackTrace();
                } catch (ParserConfigurationException pce) {
                    pce.printStackTrace();
                }

                return null;
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return null;
            } finally {
                if(conn != null)
                    conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(RssData result) {
            rssData = result;
            for(DataSetObserver obs : observers) {
                obs.onInvalidated();
            }
        }
    }

    public RssArticlesAdapter(URL rssUrl, Context context) { // nobody can see me break java
        this.rssData = null;
        this.context = context;
        new AsyncDataLoader().execute(rssUrl);
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
        observers.add(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        observers.remove(dataSetObserver);
    }

    @Override
    public int getCount() {
        if(rssData == null)
            return 0;

        return rssData.getEntryCount();
    }

    @Override
    public Object getItem(int i) {
        if(rssData == null)
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(rssData == null)
            return null;

        TextView rv = new TextView(context);
        rv.setText(((RssData.RssEntry)getItem(i)).title);
        return rv;
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
        return rssData == null || rssData.getEntryCount() == 0;
    }

}
