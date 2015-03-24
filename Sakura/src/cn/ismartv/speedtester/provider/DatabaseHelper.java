package cn.ismartv.speedtester.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by huaijie on 2015/3/24.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BaseSqlite {
    private static final String DATABASE_NAME = "sakura.db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase sqLiteDatabase;


    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        createNodeTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropNodeTable = new DropBuilder().create("node");
        db.execSQL(dropNodeTable);
        onCreate(db);
    }


    private void createNodeTable(SQLiteDatabase sqLiteDatabase) {
        String node = new TableBuilder()
                .setName("node")
                .setRow("_id", INTEGER, PRIMARY_KEY + AUTO_INCREMENT)
                .create();
        sqLiteDatabase.execSQL(node);
    }
}
