package cn.ismartv.speedtester.core.cache;


import android.content.Context;
import android.content.SharedPreferences;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.NodeEntity;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.TableInfo;
import com.activeandroid.query.Select;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import java.util.ArrayList;
import java.util.Arrays;

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

    public static void updateCheck(String cdnId, boolean checked) {

        NodeCacheTable checkedItem = new Select().from(NodeCacheTable.class).where(NodeCacheTable.CHECKED + "=?", true).executeSingle();
        if (null != checkedItem) {
            checkedItem.checked = false;
            checkedItem.save();
        }
        NodeCacheTable nodeCacheTable = NodeCacheTable.loadByCdnId(NodeCacheTable.class, Long.parseLong(cdnId));
        nodeCacheTable.checked = checked;
        nodeCacheTable.save();
    }

    public static void clearCheck() {
        NodeCacheTable checkedItem = new Select().from(NodeCacheTable.class).where(NodeCacheTable.CHECKED + "=?", true).executeSingle();
        if (null != checkedItem) {
            checkedItem.checked = false;
            checkedItem.save();
        }
    }

    public static NodeCacheTable fetchCheck() {
        return new Select()
                .from(NodeCacheTable.class)
                .where("checked = ?", true)
                .executeSingle();
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
                nodeCacheTable.speed = nodeEntity.getSpeed();
                nodeCacheTable.updateTime = String.valueOf(System.currentTimeMillis());
                nodeCacheTable.area = StringUtils.getAreaCodeByNode(nodeEntity.getNick());
                nodeCacheTable.isp = StringUtils.getOperatorByNode(nodeEntity.getNick());
                nodeCacheTable.checked = false;
                nodeCacheTable.running = "false";
                nodeCacheTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    public static void updateLocationCache(Context context, String city, String isp) {
        String[] mCity = context.getResources().getStringArray(R.array.citys);
        String[] mIsp = context.getResources().getStringArray(R.array.isps);
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int ispPosition = Arrays.asList(mIsp).indexOf(isp);
        int cityPositon = Arrays.asList(mCity).indexOf(city);
        editor.putInt("city_position", cityPositon == -1 ? 0 : cityPositon);
        editor.putInt("isp_position", ispPosition == -1 ? 0 : ispPosition);

        editor.apply();
    }

    public static void updateSpeedLogUrl(Context context, String speedlogurl) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("speedlogurl", speedlogurl);
        editor.apply();
    }

    public static void updatFeedBack(Context context, String phoneNumber) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("feedback_phoneNumber", phoneNumber);
        editor.apply();
    }

    public static void updatLocalIp(Context context, String localIp) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);

        if (!preferences.getString("local_ip", "").equals(localIp)) {
            weiXinUpload(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("local_ip", localIp);
            editor.apply();
        }
    }

    public static void updateLaunched(Context context, boolean b) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("launched", b);
        editor.apply();
    }


    public static void updateNodePosition(Context context, int cityPostion, int ispPosition) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("l_city_position", cityPostion);
        editor.putInt("l_isp_position", ispPosition);
        editor.apply();
    }

    private static void weiXinUpload(Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.UploadClientIp client = restAdapter.create(ClientApi.UploadClientIp.class);
        client.excute(DeviceUtils.getLocalMacAddress(context), DeviceUtils.getLocalIpAddressV4(), DeviceUtils.getSnCode(),
                DeviceUtils.getModel(), new Callback<Empty>() {
                    @Override
                    public void success(Empty o, retrofit.client.Response response) {
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                    }
                }
        );
    }
}
