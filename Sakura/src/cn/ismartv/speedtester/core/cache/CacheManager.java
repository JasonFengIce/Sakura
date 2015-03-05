package cn.ismartv.speedtester.core.cache;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.IpLookUpEntity;
import cn.ismartv.speedtester.data.NodeEntity;
import cn.ismartv.speedtester.data.table.CityTable;
import cn.ismartv.speedtester.provider.NodeCacheTable;
import cn.ismartv.speedtester.utils.DeviceUtils;
import cn.ismartv.speedtester.utils.StringUtils;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import java.util.ArrayList;

/**
 * Created by fenghb on 14-7-11.
 */
public class CacheManager {
    private static final String TAG = "CacheManager";

    private static CacheManager instance;

    private static Context mContext;

    private CacheManager(Context context) {
        mContext = context;
    }

    public static CacheManager getInstance(Context context) {
        if (null == instance) {
            instance = new CacheManager(context);
        }
        return instance;
    }

    /**
     * insert data to city table
     */
    public void initializeCityTable() {
        String[] cities = mContext.getResources().getStringArray(R.array.citys);
        String[] cityNicks = mContext.getResources().getStringArray(R.array.city_nicks);
        int[] flags = mContext.getResources().getIntArray(R.array.city_flag);


        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < cities.length; ++i) {
                CityTable cityTable = new CityTable();
                cityTable.flag = flags[i];
                cityTable.name = cities[i];
                cityTable.nick = cityNicks[i];
                cityTable.areaName = StringUtils.getAreaNameByProvince(cities[i]);
                cityTable.areaFlag = StringUtils.getAreaCodeByProvince(cities[i]);
                cityTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }


    public void updatePosition(int provincePosition, int ispPosition) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(IpLookUp.USER_PROVINCE, provincePosition);
        editor.putInt(IpLookUp.USER_ISP, ispPosition);
        editor.apply();
    }


    /**
     * get
     */

    /**
     * IpLookUp
     */
    public class IpLookUp {
        public static final String USER_PROVINCE = "user_province";
        public static final String USER_IP = "user_ip";
        public static final String USER_ISP = "user_isp";
        public static final String USER_CITY = "user_city";


        public String getUserProvince() {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
            String[] array = mContext.getResources().getStringArray(R.array.citys);
            int position = sharedPreferences.getInt(USER_PROVINCE, 0);

            if (position == -1)
                position = 0;

            return array[position];
        }

        public String getUserIp() {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);

            return sharedPreferences.getString(USER_IP, "0.0.0.0");
        }

        public String getUserIsp() {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
            String[] array = mContext.getResources().getStringArray(R.array.isps);
            int position = sharedPreferences.getInt(USER_ISP, 0);

            if (position == -1)
                position = 0;
            return array[position];
        }

        public String getUserCity() {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(USER_CITY, "");
        }

        public void updateIpLookUpCache(IpLookUpEntity ipLookUpEntity) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            int provincePosition = getProvincePositionByName(ipLookUpEntity.getProv());
            int ispPosition = getIspPositionByName(ipLookUpEntity.getIsp());
            String ipAddress = ipLookUpEntity.getIp();

            if (AppConstant.DEBUG) {
                Log.d(TAG, "ipAddress is --->  " + ipAddress);
            }
            editor.putString(USER_CITY, ipLookUpEntity.getCity());
            editor.putInt(USER_PROVINCE, provincePosition);
            editor.putInt(USER_ISP, ispPosition);
            editor.putString(USER_IP, ipAddress);
            editor.apply();

        }

        public int getProvincePositionByName(String provinceName) {
            CityTable cityTable = new Select()
                    .from(CityTable.class)
                    .where(CityTable.NICK + " = ? ", provinceName)
                    .executeSingle();
            if (AppConstant.DEBUG)
                Log.d(TAG, "city flag ---> " + (cityTable.flag - 1));
            return cityTable.flag - 1;
        }

        public int getIspPositionByName(String ispName) {
            String[] isps = mContext.getResources().getStringArray(R.array.isps);
            for (int i = 0; i < isps.length; ++i) {
                if (ispName.equals(isps[i])) {
                    return i;
                }
            }
            return -1;
        }
    }


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
        NodeCacheTable nodeCacheTable = NodeCacheTable.loadByCdnId(NodeCacheTable.class, Integer.parseInt(cdnId));

        if (null != nodeCacheTable) {
            nodeCacheTable.checked = checked;
            nodeCacheTable.save();
        }
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
        new Delete().from(NodeCacheTable.class).execute();
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

    public static void updatLocalIp(Context context) {
        weiXinUpload(context);
    }

    public static void updateLaunched(Context context, boolean b) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("launched", b);
        editor.apply();
    }


    private static void weiXinUpload(final Context context) {
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
