package ru.ifmo.md.lesson5;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kna on 17.10.2014.
 */
public class RssArticlesAdapter extends CursorAdapter {

    public RssArticlesAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, ViewGroup viewGroup) {
        View w = LayoutInflater.from(context).inflate(R.layout.layout_news_entry, null);
        ((TextView) w.findViewById(R.id.textHeading)).setText(cursor.getString(cursor.getColumnIndex(RssDatabase.Structure.ARTICLES_COLUMN_NAME)));
        ((TextView) w.findViewById(R.id.textDescription)).setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(RssDatabase.Structure.ARTICLES_COLUMN_DESCRIPTION))));
        final String us = cursor.getString(cursor.getColumnIndex(RssDatabase.Structure.ARTICLES_COLUMN_URL));
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(context, ArticleViewActivity.class);
                try {
                    it.putExtra("url", new URL(us));
                } catch (MalformedURLException e) {
                    Toast.makeText(context, context.getString(R.string.article_invalid_url), Toast.LENGTH_SHORT).show();
                }
                context.startActivity(it);
            }
        });
        return w;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        View w = view;
        ((TextView) w.findViewById(R.id.textHeading)).setText(cursor.getString(cursor.getColumnIndex(RssDatabase.Structure.ARTICLES_COLUMN_NAME)));
        ((TextView) w.findViewById(R.id.textDescription)).setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(RssDatabase.Structure.ARTICLES_COLUMN_DESCRIPTION))));
        final String us = cursor.getString(cursor.getColumnIndex(RssDatabase.Structure.ARTICLES_COLUMN_URL));
        w.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(context, ArticleViewActivity.class);
                try {
                    it.putExtra("url", new URL(us));
                } catch (MalformedURLException e) {
                    Toast.makeText(context, context.getString(R.string.article_invalid_url), Toast.LENGTH_SHORT).show();
                }
                context.startActivity(it);
            }
        });
    }
}
