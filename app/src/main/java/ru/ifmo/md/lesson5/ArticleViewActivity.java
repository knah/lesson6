package ru.ifmo.md.lesson5;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import java.net.URL;


public class ArticleViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);

        URL url = (URL) getIntent().getSerializableExtra("url");
        ((WebView) findViewById(R.id.webView)).loadUrl(url.toString());
    }

}
