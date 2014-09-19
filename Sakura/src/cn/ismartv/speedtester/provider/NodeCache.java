package cn.ismartv.speedtester.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.HashMap;

/**
 * Created by fenghb on 14-7-11.
 */
public class NodeCache {
    //table_name
    public static final String TABLE_NAME = "node_cache";
    //projectionMap
    public static HashMap<String, String> projectionMap;
    //columns
    public static final String ID = BaseColumns._ID;
    public static final String CDN_ID = "cdn_id";
    public static final String NODE = "node";
    public static final String NICK = "nick";
    public static final String FLAG = "flag";
    public static final String AREA = "area";
    public static final String OPERATOR = "operator";
    public static final String IP = "ip";
    public static final String URL = "url";
    public static final String ROUTE_TRACE = "route_trace";
    public static final String SPEED = "speed";
    public static final String UPDATE_TIME = "update_time";
    public static final String CHECKED = "checked";
    public static final String RUNNING = "running";

    public static final Uri CONTENT_URI = Uri.parse("content://"
            + SakuraProvider.AUTHORITY + "/" + TABLE_NAME);

    //create table sql
    public static final String CREATE_TABLE_SQL = "CREATE TABLE "
            + TABLE_NAME + " (" + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//            + CDN_ID + " INTEGER ,"
            + CDN_ID + " INTEGER UNIQUE,"
            + NODE + " VARCHAR(255),"
            + NICK + " VARCHAR(255),"
            + FLAG + " VARCHAR(255),"
            + AREA + " INTEGER,"
            + OPERATOR + " INTEGER,"
            + IP + " VARCHAR(255),"
            + URL + " VARCHAR(255),"
            + ROUTE_TRACE + " VARCHAR(255),"
            + SPEED + " VARCHAR(255),"
            + CHECKED + " VARCHAR(255),"
            + RUNNING + " VARCHAR(255),"
            + UPDATE_TIME + " VARCHAR(255))";


    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(ID, ID);
        projectionMap.put(CDN_ID, CDN_ID);
        projectionMap.put(NODE, NODE);
        projectionMap.put(NICK, NICK);
        projectionMap.put(FLAG, FLAG);
        projectionMap.put(AREA, AREA);
        projectionMap.put(OPERATOR, OPERATOR);
        projectionMap.put(IP, IP);
        projectionMap.put(URL, URL);
        projectionMap.put(ROUTE_TRACE, ROUTE_TRACE);
        projectionMap.put(SPEED, SPEED);
        projectionMap.put(CHECKED, CHECKED);
        projectionMap.put(RUNNING, RUNNING);
        projectionMap.put(UPDATE_TIME, UPDATE_TIME);

    }
}
