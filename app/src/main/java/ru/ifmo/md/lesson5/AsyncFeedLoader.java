package ru.ifmo.md.lesson5;

import android.content.Context;
import android.database.DataSetObservable;
import android.os.AsyncTask;
import android.widget.Toast;

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
 * Created by kna on 20.10.2014.
 */
public class AsyncFeedLoader extends AsyncTask<URL, Void, RssData[]> {

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

    @Override
    protected RssData[] doInBackground(URL... urls) {
        RssData[] result = new RssData[urls.length];
        for (int i = 0; i < urls.length; i++) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) urls[i].openConnection();
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
                    SAXRssHandler hdl = new SAXRssHandler(urls[i]);
                    p.parse(new InputSource(isr), hdl);
                    result[i] = hdl.getResult();
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
        return result;
    }

    @Override
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

        callback.run(realResult);
        observable.notifyInvalidated();
    }

    public AsyncFeedLoader(DataSetObservable observable, ResultCallback callback, Context context) {
        this.observable = observable;
        this.callback = callback;
        this.context = context;
    }

    private DataSetObservable observable;
    private ResultCallback callback;
    private Context context;

    public interface ResultCallback {
        public void run(RssData[] result);
    }
}
