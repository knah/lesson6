package ru.ifmo.md.lesson5;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kna on 10.11.2014.
 */
public class RssDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "rssArticles.db";

    ;
    private static final int DB_VERSION = 5;
    public RssDatabase(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Structure.FEEDS_TABLE +
                "(" +
                Structure.COLUMN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // why is sqlite so stupid?
                Structure.FEEDS_COLUMN_NAME + " TEXT NOT NULL, " +
                Structure.FEEDS_COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                Structure.FEEDS_COLUMN_URL + " TEXT NOT NULL UNIQUE " +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Structure.ARTICLES_TABLE +
                "(" +
                Structure.ARTICLES_COLUMN_NAME + " TEXT NOT NULL, \n" +
                Structure.ARTICLES_COLUMN_DESCRIPTION + " TEXT NOT NULL, \n" +
                Structure.ARTICLES_COLUMN_URL + " TEXT NOT NULL UNIQUE, \n" +
                Structure.ARTICLES_COLUMN_FEED_ID + " INTEGER REFERENCES \n" +
                Structure.FEEDS_TABLE + "(" + Structure.COLUMN_ROWID + ") ON DELETE CASCADE\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE " + Structure.FEEDS_TABLE + ";");
            db.execSQL("DROP TABLE " + Structure.ARTICLES_TABLE + ";");
            onCreate(db);
        }
    }

    public static class Structure {
        public static final String FEEDS_TABLE = "feeds";
        public static final String ARTICLES_TABLE = "articles";

        public static final String COLUMN_ROWID_QUERY = "rowid as _id";
        public static final String COLUMN_ROWID = "rowid";
        public static final String COLUMN_ROWID_AFTER_QUERY = "_id";

        public static final String FEEDS_COLUMN_NAME = "name";
        public static final String FEEDS_COLUMN_DESCRIPTION = "description";
        public static final String FEEDS_COLUMN_URL = "url";
        public static final String FEEDS_COLUMN_LAST_UPDATE = "last_update";

        public static final String ARTICLES_COLUMN_NAME = "name";
        public static final String ARTICLES_COLUMN_DESCRIPTION = "description";
        public static final String ARTICLES_COLUMN_FEED_ID = "feed_id";
        public static final String ARTICLES_COLUMN_URL = "url";
    }
}
