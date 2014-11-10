package ru.ifmo.md.lesson5;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class RssContentProvider extends ContentProvider {
    public static final String AUTHORITY = "ru.ifmo.md.lesson6.rss";
    public static final String BASE_URI = "feeds";
    public static final Uri URI_FEED_DIR = Uri.parse("content://" + AUTHORITY + "/" + BASE_URI);
    public static final int URITYPE_FEED_DIR = 1;
    public static final int URITYPE_FEED_ARTICLES = 2;
    public static final int URITYPE_ARTICLE = 3;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_URI, URITYPE_FEED_DIR);
        uriMatcher.addURI(AUTHORITY, BASE_URI + "/#", URITYPE_FEED_ARTICLES);
        uriMatcher.addURI(AUTHORITY, BASE_URI + "/#/#", URITYPE_ARTICLE);
    }
    private RssDatabase database;

    public RssContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        switch (uriType) {
            case URITYPE_FEED_ARTICLES:
                break;
            case URITYPE_FEED_DIR:
            case URITYPE_ARTICLE:
            default:
                throw new UnsupportedOperationException("Unsupported delete type");
        }

        SQLiteDatabase db = database.getWritableDatabase();
        return db.delete(RssDatabase.Structure.FEEDS_TABLE, "rowid = ?", new String[]{uri.getLastPathSegment()});
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("This operation is unsupported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        String tableName = null;
        switch (uriType) {
            case URITYPE_FEED_DIR:
                tableName = RssDatabase.Structure.FEEDS_TABLE;
                break;
            case URITYPE_FEED_ARTICLES:
                tableName = RssDatabase.Structure.ARTICLES_TABLE;
                break;
            case URITYPE_ARTICLE:
                throw new UnsupportedOperationException("Unsupported insert type");
        }

        SQLiteDatabase db = database.getWritableDatabase();
        long rowid = db.insert(tableName, null, values);

        return uri.buildUpon().appendPath("" + rowid).build();
    }

    @Override
    public boolean onCreate() {
        database = new RssDatabase(getContext(), null);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        if (uriType == URITYPE_FEED_DIR) {
            builder.setTables(RssDatabase.Structure.FEEDS_TABLE);
        } else {
            builder.setTables(RssDatabase.Structure.ARTICLES_TABLE);
        }

        switch (uriType) {
            case URITYPE_FEED_DIR:
                break; // select *
            case URITYPE_FEED_ARTICLES:
                builder.appendWhere(RssDatabase.Structure.ARTICLES_COLUMN_FEED_ID + "=" + uri.getLastPathSegment());
                break;
            case URITYPE_ARTICLE:
                break; // nothing again
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cr = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cr.setNotificationUri(getContext().getContentResolver(), uri);

        return cr;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        switch (uriType) {
            case URITYPE_FEED_ARTICLES:
                break;
            case URITYPE_FEED_DIR:
            case URITYPE_ARTICLE:
            default:
                throw new UnsupportedOperationException("Unsupported update type");
        }

        SQLiteDatabase db = database.getWritableDatabase();
        return db.update(RssDatabase.Structure.FEEDS_TABLE, values, "rowid = ?", new String[]{uri.getLastPathSegment()});
    }
}
