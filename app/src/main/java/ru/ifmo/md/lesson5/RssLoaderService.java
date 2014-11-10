package ru.ifmo.md.lesson5;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class RssLoaderService extends IntentService {
    private static final String ACTION_GET_ALL = "ru.ifmo.md.lesson5.action.GET_ALL";
    private static final String ACTION_GET_SINGLE = "ru.ifmo.md.lesson5.action.GET_SINGLE";

    // TODO: Rename parameters
    private static final String EXTRA_SINGLE_ID = "ru.ifmo.md.lesson5.extra.SINGLE_ID";

    public RssLoaderService() {
        super("RssLoaderService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetAll(Context context) {
        Intent intent = new Intent(context, RssLoaderService.class);
        intent.setAction(ACTION_GET_ALL);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionGetSingle(Context context, int rowid) {
        Intent intent = new Intent(context, RssLoaderService.class);
        intent.setAction(ACTION_GET_SINGLE);
        intent.putExtra(EXTRA_SINGLE_ID, rowid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_ALL.equals(action)) {
                handleActionGetAll();
            } else if (ACTION_GET_SINGLE.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_SINGLE_ID, -1);
                handleActionGetSingle(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetAll() {
        URL[] urls;
        int[] rowids;
        ContentResolver resolver = getContentResolver();
        Cursor cr = resolver.query(RssContentProvider.URI_FEED_DIR, new String[]{RssDatabase.Structure.FEEDS_COLUMN_URL, RssDatabase.Structure.COLUMN_ROWID}, null, null, null);

        urls = new URL[cr.getCount()];
        rowids = new int[cr.getCount()];

        int ui = 0;
        cr.moveToNext();
        while (!cr.isAfterLast()) {
            String urlS = cr.getString(0);
            rowids[ui] = cr.getInt(1);
            try {
                urls[ui] = new URL(urlS);
            } catch (MalformedURLException ignore) {

            } finally {
                ui++;
                cr.moveToNext();
            }
        }
        cr.close();

        for (int i = 0; i < urls.length; i++) {
            loadSingleFeed(urls[i], rowids[i]);
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetSingle(int rowid) {
        ContentResolver resolver = getContentResolver();
        Cursor cr = resolver.query(
                RssContentProvider.URI_FEED_DIR,
                new String[]{RssDatabase.Structure.FEEDS_COLUMN_URL, RssDatabase.Structure.COLUMN_ROWID},
                "rowid = ?",
                new String[]{"" + rowid},
                null);


        URL url = null;
        cr.moveToNext();
        if(cr.isAfterLast())
            return;
        try {
            url = new URL(cr.getString(0));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        cr.close();

        loadSingleFeed(url, rowid);
    }

    private void loadSingleFeed(URL url, int feedRowid) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            String ct = conn.getHeaderField("Content-Type");
            String encoding = "utf-8"; // there is no encoding except for utf-8, and utf-8 is the encoding
            if (ct != null && ct.contains("charset=")) {
                Matcher mt = Pattern.compile("charset=([^\\s]+)").matcher(ct);
                mt.find();
                encoding = mt.group(1);
            }
            InputStreamReader isr = new InputStreamReader(is, encoding);
            try {
                SAXParser p = SAXParserFactory.newInstance().newSAXParser();
                SAXRssHandler hdl = new SAXRssHandler(url);
                p.parse(new InputSource(isr), hdl);
                RssData rsd = hdl.getResult();

                ContentValues valuesFeed = new ContentValues();
                valuesFeed.put(RssDatabase.Structure.FEEDS_COLUMN_NAME, rsd.rssName);
                valuesFeed.put(RssDatabase.Structure.FEEDS_COLUMN_DESCRIPTION, rsd.rssDescription);

                ContentResolver resolver = getContentResolver();

                Uri targetURI = RssContentProvider.URI_FEED_DIR.buildUpon().appendPath(feedRowid + "").build();

                ContentValues values;
                resolver.update(targetURI, valuesFeed, null, null);

                outer:
                for (int i = 0; i < rsd.getEntryCount(); i++) {
                    values = new ContentValues();
                    values.put(RssDatabase.Structure.ARTICLES_COLUMN_NAME, rsd.getEntry(i).title);
                    values.put(RssDatabase.Structure.ARTICLES_COLUMN_DESCRIPTION, rsd.getEntry(i).description);
                    values.put(RssDatabase.Structure.ARTICLES_COLUMN_URL, rsd.getEntry(i).url.toString());
                    values.put(RssDatabase.Structure.ARTICLES_COLUMN_FEED_ID, feedRowid);

                    Cursor cr = resolver.query(targetURI, new String[]{RssDatabase.Structure.FEEDS_COLUMN_URL}, RssDatabase.Structure.FEEDS_COLUMN_URL + " = ?", new String[]{rsd.getEntry(i).url.toString()}, null);
                    if (cr.getCount() > 0) {
                        cr.close();
                        break; // we have all the feeds
                    }

                    cr.close();

                    resolver.insert(targetURI, values);

                }

                resolver.notifyChange(RssContentProvider.URI_FEED_DIR, null);
                resolver.notifyChange(targetURI, null);

            } catch (SAXException se) {
                se.printStackTrace();
            } catch (ParserConfigurationException pce) {
                pce.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

    private static class SAXRssHandler extends DefaultHandler {

        private RssData result;
        private String nameTmp;
        private String descrTmp;
        private URL urlTmp;
        private boolean isInRssTag;
        private String someStringTmp;

        private URL wholeFeedUrl;

        public SAXRssHandler(URL wholeFeedUrl) {
            this.wholeFeedUrl = wholeFeedUrl;
        }

        public RssData getResult() {
            return result;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            someStringTmp = "";
            if ("channel".equalsIgnoreCase(qName)) {
                isInRssTag = true;
            } else if ("item".equalsIgnoreCase(qName)) {
                if (result == null)
                    if (isInRssTag)
                        result = new RssData(nameTmp, descrTmp, wholeFeedUrl);
                    else
                        throw new SAXException("Item tags out of RSS tag");
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            someStringTmp += String.copyValueOf(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("channel".equalsIgnoreCase(qName)) {
                isInRssTag = false;
            } else if ("item".equalsIgnoreCase(qName)) {
                if (result != null)
                    result.addEntry(nameTmp, descrTmp, urlTmp);
                else
                    throw new SAXException("Items are in some weird place");
            }

            if ("title".equalsIgnoreCase(qName)) {
                nameTmp = someStringTmp;
            } else if ("link".equalsIgnoreCase(qName)) {
                try {
                    urlTmp = new URL(someStringTmp);

                } catch (MalformedURLException ex) {
                    throw new SAXException("Malformed URL", ex);
                }
            } else if ("description".equalsIgnoreCase(qName)) {
                descrTmp = someStringTmp;
            }
        }
    }

    /*@Override
    protected void onPostExecute(RssData[] result) {
        int notNull = 0;
        for (RssData d : result) {
            if (d != null)
                notNull++;
        }
        RssData[] realResult = null;
        if (notNull > 0) {
            realResult = new RssData[notNull];
            int cnt = 0;
            for (RssData d : result)
                if (d != null)
                    realResult[cnt++] = d;
        } else {
            Toast.makeText(context, R.string.internet_problems, Toast.LENGTH_SHORT).show();
        }
    }*/
}
