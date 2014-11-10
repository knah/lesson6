package ru.ifmo.md.lesson5;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class RssActivity extends Activity {

    private int feedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss);

        feedId = getIntent().getIntExtra("feedId", -1);

        if (feedId == -1) {
            Log.e("FEED", "Invalid Feed ID of -1");
        }

        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                return new CursorLoader(
                        getApplicationContext(),
                        RssContentProvider.URI_FEED_DIR.buildUpon().appendPath("" + feedId).build(),
                        new String[]{RssDatabase.Structure.ARTICLES_COLUMN_NAME, RssDatabase.Structure.ARTICLES_COLUMN_DESCRIPTION, RssDatabase.Structure.ARTICLES_COLUMN_URL, RssDatabase.Structure.COLUMN_ROWID_QUERY},
                        null,
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> objectLoader, Cursor o) {
                ((CursorAdapter) ((ListView) findViewById(R.id.rssListView)).getAdapter()).swapCursor(o);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> objectLoader) {

            }
        });

        ListView listView = (ListView) findViewById(R.id.rssListView);
        listView.setAdapter(new RssArticlesAdapter(this, null, true));
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
            RssLoaderService.startActionGetSingle(getApplicationContext(), feedId);
        } else if (id == R.id.action_delete) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            TextView text = new TextView(this);
            text.setText(R.string.delete_confirm);
            alert.setView(text);
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onBackPressed();
                    getContentResolver().delete(RssContentProvider.URI_FEED_DIR.buildUpon().appendPath("" + feedId).build(), null, null);
                    getContentResolver().notifyChange(RssContentProvider.URI_FEED_DIR, null);
                }
            });
            alert.setNegativeButton(R.string.cancel, null);
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

}
