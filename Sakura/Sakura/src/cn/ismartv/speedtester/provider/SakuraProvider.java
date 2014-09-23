package cn.ismartv.speedtester.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by fenghb on 14-7-11.
 */
public class SakuraProvider extends ContentProvider {

    //UriMatcher
    private static UriMatcher matcher;
    private DatabaseHelper dbHelper;
    public static final String AUTHORITY = "cn.ismartv.sakura";

    private static final int CACHE = 1;
    private static final int CACHE_ID = 2;
    private static final int CITY_CACHE = 3;
    private static final int CITY_CACHE_ID = 4;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, NodeCache.TABLE_NAME, CACHE);
        matcher.addURI(AUTHORITY, NodeCache.TABLE_NAME + "/#", CACHE_ID);
        matcher.addURI(AUTHORITY, CityCache.TABLE_NAME, CITY_CACHE);
        matcher.addURI(AUTHORITY, CityCache.TABLE_NAME + "/#", CITY_CACHE_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return (dbHelper == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (matcher.match(uri)) {
            case CACHE:
                qb.setTables(NodeCache.TABLE_NAME);
                qb.setProjectionMap(NodeCache.projectionMap);
                break;
            case CITY_CACHE:
                qb.setTables(CityCache.TABLE_NAME);
                qb.setProjectionMap(CityCache.projectionMap);
                break;
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String tableName;
        switch (matcher.match(uri)) {
            case CACHE:
                tableName = NodeCache.TABLE_NAME;
                break;
            case CITY_CACHE:
                tableName = CityCache.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }

        // make a copy of the values
        ContentValues v;
        if (contentValues != null) {
            v = new ContentValues(contentValues);
        } else {
            v = new ContentValues();
        }

        // store the data
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long rowId = db.insert(tableName, " ", v);

        if (rowId > 0) {
            Uri catUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return catUri;
        }
        throw new RuntimeException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int count = 0;
        long id = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (matcher.match(uri)) {
            case CACHE_ID:
                id = ContentUris.parseId(uri);
//                selection = selection == null ? "id = " + id : "id = " + id + " AND " + selection;
                break;
            case CACHE:
                count = db.update(NodeCache.TABLE_NAME, contentValues, selection + " = ?", selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknow uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int length = values.length;
        if (length < 10) {
            for (ContentValues contentValues : values) {
                insert(uri, contentValues);
            }
            return length;
        } else {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            switch (matcher.match(uri)) {
                case CACHE:
                    break;
                case CITY_CACHE:
                    break;
                default:
                    throw new IllegalArgumentException("Unknow uri: " + uri);
            }
        }
        return 0;
    }
}
