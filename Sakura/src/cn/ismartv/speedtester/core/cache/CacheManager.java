package cn.ismartv.speedtester.core.cache;


import android.content.Context;
import android.content.SharedPreferences;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.data.NodeEntity;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.utils.StringUtils;
import com.activeandroid.ActiveAndroid;

import java.util.ArrayList;

/**
 * Created by fenghb on 14-7-11.
 */
public class CacheManager {
    public static void updateNodeCache(int id, int cdnId, int speed) {
        NodeCacheTable nodeCacheTable = NodeCacheTable.load(NodeCacheTable.class, id);
        nodeCacheTable.cdnID = cdnId;
        nodeCacheTable.speed = speed;
        nodeCacheTable.save();
    }

    public static void updateNodeCache(Context context, ArrayList<NodeEntity> nodes) {

        ActiveAndroid.beginTransaction();
        try {
            for (NodeEntity nodeEntity : nodes) {
                NodeCacheTable nodeCacheTable = new NodeCacheTable();
                nodeCacheTable.nodeName = nodeEntity.getName();
                nodeCacheTable.cdnID = Integer.parseInt(nodeEntity.getCdnID());
                nodeCacheTable.nick = nodeEntity.getNick();
                nodeCacheTable.flag = nodeEntity.getFlag();
                nodeCacheTable.ip = nodeEntity.getUrl();
                nodeCacheTable.url = nodeEntity.getTestFile();
                nodeCacheTable.routeTrace = nodeEntity.getRoute_trace();
                int speed = 0;
                if (null != nodeEntity.getSpeed() || (!"".equals(nodeEntity.getSpeed())))
                    speed = Integer.parseInt(nodeEntity.getSpeed());
                nodeCacheTable.speed = speed;
                nodeCacheTable.updateTime = String.valueOf(System.currentTimeMillis());
                nodeCacheTable.area = StringUtils.getAreaCodeByNode(nodeEntity.getNick());
                nodeCacheTable.isp = StringUtils.getOperatorByNode(nodeEntity.getNick());
                nodeCacheTable.checked = "false";
                nodeCacheTable.running = "false";
                nodeCacheTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    //
//    public static void updateCheck(Context context, String cdnId, String checked) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(NodeCache.CHECKED, checked);
//        ContentValues clear = new ContentValues();
//        clear.put(NodeCache.CHECKED, "false");
//        context.getContentResolver().update(NodeCache.CONTENT_URI, clear, NodeCache.CHECKED, new String[]{"true"});
//        context.getContentResolver().update(NodeCache.CONTENT_URI, contentValues, NodeCache.CDN_ID, new String[]{cdnId});
//    }
//
//    //when the node speed test,update the data
//    public static void updateRunning(Context context, String cdnId, String running) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(NodeCache.RUNNING, running);
//        ContentValues clear = new ContentValues();
//        clear.put(NodeCache.RUNNING, "false");
//        context.getContentResolver().update(NodeCache.CONTENT_URI, clear, NodeCache.CDN_ID, new String[]{"true"});
//        context.getContentResolver().update(NodeCache.CONTENT_URI, contentValues, NodeCache.CDN_ID, new String[]{cdnId});
//    }
//
//
//    public static void updateSpeedLogUrl(Context context, String speedlogurl) {
//        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("speedlogurl", speedlogurl);
//        editor.apply();
//    }
//
//
//    public static void updateSelectNodeCache(Context context, int province, int netType) {
//        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt("node_province", province);
//        editor.putInt("node_netType", netType);
//        editor.apply();
//    }
//
//    public static void updateVersion(Context context, int isUpdate, String name) {
//        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt("update", isUpdate);
//        editor.putString("apk_name", name);
//        editor.apply();
//    }
//
    public static void updatFeedBack(Context context, String phoneNumber) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("feedback_phoneNumber", phoneNumber);
        editor.apply();
    }
//
//
//    public static void updatLocalIp(Context context, String localIp) {
//        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
//
//        if (!preferences.getString("local_ip", "").equals(localIp)) {
//            NetWorkUtil.getInstant().weiXinUpload(context);
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString("local_ip", localIp);
//            editor.apply();
//        }
//    }

}
