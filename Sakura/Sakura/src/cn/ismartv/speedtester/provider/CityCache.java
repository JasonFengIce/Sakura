package cn.ismartv.speedtester.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.HashMap;

/**
 * Created by fenghb on 14-7-15.
 */
public class CityCache {

    //table_name
    public static final String TABLE_NAME = "city_cache";
    //projectionMap
    public static HashMap<String, String> projectionMap;
    //columns
    public static final String ID = BaseColumns._ID;
    public static final String CITY_ID = "city_id";
    public static final String CITY_NAME = "city_name";
    public static final String NICK = "nick";
    public static final String AREA = "area";
    public static final String AREA_CODE = "area_code";

    public static final Uri CONTENT_URI = Uri.parse("content://"
            + SakuraProvider.AUTHORITY + "/" + TABLE_NAME);

    //create table sql
    public static final String CREATE_TABLE_SQL = "CREATE TABLE "
            + TABLE_NAME + " (" + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CITY_ID + " INTEGER UNIQUE,"
            + CITY_NAME + " VARCHAR(255),"
            + NICK + " VARCHAR(255),"
            + AREA + " VARCHAR(255),"
            + AREA_CODE + " INTEGER)";

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(ID, ID);
        projectionMap.put(CITY_ID, CITY_ID);
        projectionMap.put(CITY_NAME, CITY_NAME);
        projectionMap.put(NICK, NICK);
        projectionMap.put(AREA, AREA);
        projectionMap.put(AREA_CODE, AREA_CODE);
    }
}
