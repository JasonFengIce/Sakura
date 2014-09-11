package cn.ismartv.speedtester.core.cache;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import cn.ismartv.speedtester.data.Node;
import cn.ismartv.speedtester.provider.NodeCache;
import cn.ismartv.speedtester.utils.StringUtilities;

import java.util.ArrayList;

/**
 * Created by fenghb on 14-7-11.
 */
public class CacheManager {
    public static void updateNodeCache(Context context, String cdnId, String speed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NodeCache.SPEED, speed);
        context.getContentResolver().update(NodeCache.CONTENT_URI, contentValues, NodeCache.CDN_ID, new String[]{cdnId});
    }

    public static void updateNodeCache(Context context, ArrayList<Node> nodes) {
        ContentValues contentValues = new ContentValues();
        for (Node node : nodes) {
            contentValues.put(NodeCache.NODE, node.getName());
            contentValues.put(NodeCache.CDN_ID, node.getCdnID());
            contentValues.put(NodeCache.NICK, node.getNick());
            contentValues.put(NodeCache.FLAG, node.getFlag());
            contentValues.put(NodeCache.IP, node.getUrl());
            contentValues.put(NodeCache.URL, node.getTestFile());
            contentValues.put(NodeCache.ROUTE_TRACE, node.getRoute_trace());
            contentValues.put(NodeCache.SPEED, node.getSpeed());
            contentValues.put(NodeCache.UPDATE_TIME, System.currentTimeMillis());
            contentValues.put(NodeCache.AREA, StringUtilities.getAreaCodeByNode(node.getNick()));
            contentValues.put(NodeCache.OPERATOR, StringUtilities.getOperatorByNode(node.getNick()));
            contentValues.put(NodeCache.CHECKED, "false");
            contentValues.put(NodeCache.RUNNING, "false");
            context.getContentResolver().insert(NodeCache.CONTENT_URI, contentValues);
        }
    }


    public static void updateCheck(Context context, String cdnId, String checked) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NodeCache.CHECKED, checked);
        ContentValues clear = new ContentValues();
        clear.put(NodeCache.CHECKED, "false");
        context.getContentResolver().update(NodeCache.CONTENT_URI, clear, NodeCache.CHECKED, new String[]{"true"});
        context.getContentResolver().update(NodeCache.CONTENT_URI, contentValues, NodeCache.CDN_ID, new String[]{cdnId});
    }

    //when the node speed test,update the data
    public static void updateRunning(Context context, String cdnId, String running) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NodeCache.RUNNING, running);
        ContentValues clear = new ContentValues();
        clear.put(NodeCache.RUNNING, "false");
        context.getContentResolver().update(NodeCache.CONTENT_URI, clear, NodeCache.CDN_ID, new String[]{"true"});
        context.getContentResolver().update(NodeCache.CONTENT_URI, contentValues, NodeCache.CDN_ID, new String[]{cdnId});
    }


    public static void updateSpeedLogUrl(Context context, String speedlogurl) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("speedlogurl", speedlogurl);
        editor.apply();
    }

    public static void updateFeedBack(Context context, String province, String netType, String netWidth, String phoneNumber) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("feedback_province", province);
        editor.putString("feedback_netType", netType);
        editor.putString("feedback_netWidth", netWidth);
        editor.putString("feedback_phoneNumber", phoneNumber);
        editor.apply();
    }


    public static void updateSelectNodeCache(Context context, int province, int netType) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("node_province", province);
        editor.putInt("node_netType", netType);
        editor.apply();
    }

    public static void updateVersion(Context context, int isUpdate, String name) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("update", isUpdate);
        editor.putString("apk_name", name);
        editor.apply();
    }

}