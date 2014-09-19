package com.huaijie.tools.database.nosql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huaijie.tools.database.DataDeserializer;
import com.huaijie.tools.database.DataFilter;
import com.huaijie.tools.database.DataSerializer;
import com.huaijie.tools.database.NoSQLEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * The NoSQL datastore is in fact backed by SQL. This might seem counter to the ideals of the project at first. However,
 * the framework prevents the user from having to interact with SQL directly and deals purely with documents.
 * The database is still useful in implementation however for it's indexing retrieval and storage options.
 */
public class SimpleNoSQLDBHelper extends SQLiteOpenHelper {

    private DataSerializer serializer;
    private DataDeserializer deserializer;

    public static int DATABASE_VERSION = 2;
    public static String DATABASE_NAME = "simplenosql.db";

    // DB Creation
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SimpleNoSQLContract.EntityEntry.TABLE_NAME + " (" +
            SimpleNoSQLContract.EntityEntry._ID + " INTEGER PRIMARY KEY," +
            SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID + TEXT_TYPE + COMMA_SEP +
            SimpleNoSQLContract.EntityEntry.COLUMN_NAME_ENTITY_ID + TEXT_TYPE + COMMA_SEP +
            SimpleNoSQLContract.EntityEntry.COLUMN_NAME_DATA + TEXT_TYPE + COMMA_SEP +
            " UNIQUE(" + SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID + COMMA_SEP + SimpleNoSQLContract.EntityEntry.COLUMN_NAME_ENTITY_ID + ") ON CONFLICT REPLACE"
            + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE " + SimpleNoSQLContract.EntityEntry.TABLE_NAME;


    public SimpleNoSQLDBHelper(Context context, DataSerializer serializer, DataDeserializer deserializer) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Be non-destructive when doing a real upgrade.
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public <T> void saveEntity(NoSQLEntity<T> entity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID, entity.getBucket());
        values.put(SimpleNoSQLContract.EntityEntry.COLUMN_NAME_ENTITY_ID, entity.getId());
        values.put(SimpleNoSQLContract.EntityEntry.COLUMN_NAME_DATA, serializer.serialize(entity.getData()));
        db.insertWithOnConflict(SimpleNoSQLContract.EntityEntry.TABLE_NAME, SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteEntity(String bucket, String entityId) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {bucket, entityId};
        db.delete(SimpleNoSQLContract.EntityEntry.TABLE_NAME, SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID + "=? and " + SimpleNoSQLContract.EntityEntry.COLUMN_NAME_ENTITY_ID + "=?", args);
    }

    public void deleteBucket(String bucket) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {bucket};
        db.delete(SimpleNoSQLContract.EntityEntry.TABLE_NAME, SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID + "=?", args);
    }

    public <T> List<NoSQLEntity<T>> getEntities(String bucket, String entityId, Class<T> clazz, DataFilter<T> filter) {
        if (bucket == null || entityId == null) {
            return new ArrayList<NoSQLEntity<T>>(0);
        }
        String selection = SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID + "=? AND " + SimpleNoSQLContract.EntityEntry.COLUMN_NAME_ENTITY_ID + "=?";
        String[] selectionArgs = {bucket, entityId};
        return getEntities(selection, selectionArgs, clazz, filter);
    }

    public <T> List<NoSQLEntity<T>> getEntities(String bucket, String entityId, Class<T> clazz) {
        return getEntities(bucket, entityId, clazz, null);
    }

    public <T> List<NoSQLEntity<T>> getEntities(String bucket, Class<T> clazz, DataFilter<T> filter) {
        if (bucket == null) {
            return new ArrayList<NoSQLEntity<T>>(0);
        }
        String selection = SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID + "=?";
        String[] selectionArgs = {bucket};
        return getEntities(selection, selectionArgs, clazz, filter);
    }

    public <T> List<NoSQLEntity<T>> getEntities(String bucket, Class<T> clazz) {
        return getEntities(bucket, clazz, null);
    }

    private <T> List<NoSQLEntity<T>> getEntities(String selection, String[] selectionArgs, Class<T> clazz, DataFilter<T> filter) {
        List<NoSQLEntity<T>> results = new ArrayList<NoSQLEntity<T>>();
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID, SimpleNoSQLContract.EntityEntry.COLUMN_NAME_ENTITY_ID, SimpleNoSQLContract.EntityEntry.COLUMN_NAME_DATA};

        Cursor cursor = db.query(SimpleNoSQLContract.EntityEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        try {
            while (cursor.moveToNext()) {
                String bucketId = cursor.getString(cursor.getColumnIndex(SimpleNoSQLContract.EntityEntry.COLUMN_NAME_BUCKET_ID));
                String entityId = cursor.getString(cursor.getColumnIndex(SimpleNoSQLContract.EntityEntry.COLUMN_NAME_ENTITY_ID));
                String data = cursor.getString(cursor.getColumnIndex(SimpleNoSQLContract.EntityEntry.COLUMN_NAME_DATA));

                NoSQLEntity<T> entity = new NoSQLEntity<T>(bucketId, entityId);
                entity.setData(deserializer.deserialize(data, clazz));
                if (filter != null && !filter.isIncluded(entity)) {
                    // skip this item, it's been filtered out.
                    continue;
                }
                results.add(entity);
            }
        } finally {
            cursor.close();
        }
        return results;
    }
}
