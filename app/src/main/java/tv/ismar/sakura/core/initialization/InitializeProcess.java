package tv.ismar.sakura.core.initialization;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.injectdb.library.query.Select;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.sakura.R;
import tv.ismar.sakura.core.client.OkHttpClientManager;

/**
 * Created by huaijie on 8/3/15.
 */
public class InitializeProcess implements Runnable {
    private static final String TAG = "InitializeProcess";

    private static final int[] PROVINCE_STRING_ARRAY_RES = {
            R.array.china_north,
            R.array.china_east,
            R.array.china_south,
            R.array.china_center,
            R.array.china_southwest,
            R.array.china_northwest,
            R.array.china_northeast
    };
    private final String[] mDistrictArray;
    private final String[] mIspArray;
    private Context mContext;


    public InitializeProcess(Context context) {
        this.mContext = context;
        mDistrictArray = mContext.getResources().getStringArray(R.array.district);
        mIspArray = mContext.getResources().getStringArray(R.array.isp);
    }

    @Override
    public void run() {
        initializeDistrict();
        initializeProvince();
        initalizeCity();
        initializeIsp();
        fetchCdnList();
        if (TextUtils.isEmpty(tv.ismar.sakura.core.preferences.AccountSharedPrefs.getInstance(mContext).getSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.CITY))) {
            fetchLocationByIP();
        }

    }


    private void initializeDistrict() {
        if (new Select().from(tv.ismar.sakura.data.table.DistrictTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {

                for (String district : mDistrictArray) {
                    tv.ismar.sakura.data.table.DistrictTable districtTable = new tv.ismar.sakura.data.table.DistrictTable();
                    districtTable.district_id = tv.ismar.sakura.utils.StringUtils.getMd5Code(district);
                    districtTable.district_name = district;
                    districtTable.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void initializeProvince() {
        if (new Select().from(tv.ismar.sakura.data.table.ProvinceTable.class).executeSingle() == null) {

            ActiveAndroid.beginTransaction();
            try {
                for (int i = 0; i < mDistrictArray.length; i++) {
                    String[] provinceArray = mContext.getResources().getStringArray(PROVINCE_STRING_ARRAY_RES[i]);
                    for (String province : provinceArray) {
                        tv.ismar.sakura.data.table.ProvinceTable provinceTable = new tv.ismar.sakura.data.table.ProvinceTable();
                        String[] strs = province.split(",");
                        String provinceName = strs[0];
                        String provincePinYin = strs[1];

                        provinceTable.province_name = provinceName;
                        provinceTable.pinyin = provincePinYin;

                        provinceTable.province_id = tv.ismar.sakura.utils.StringUtils.getMd5Code(provinceName);
                        provinceTable.district_id = tv.ismar.sakura.utils.StringUtils.getMd5Code(mDistrictArray[i]);
                        provinceTable.save();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void initalizeCity() {
        if (new Select().from(tv.ismar.sakura.data.table.CityTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                InputStream inputStream = mContext.getResources().getAssets().open("location.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (null != s && !s.equals("")) {
                        String[] strings = s.split("\\,");
                        Long geoId = Long.parseLong(strings[0]);
                        String area = strings[1];
                        String city = strings[2];
                        String province = strings[3];
                        String provinceId = tv.ismar.sakura.utils.HardwareUtils.getMd5ByString(province);

                        if (area.equals(city)) {
                            tv.ismar.sakura.data.table.CityTable cityTable = new tv.ismar.sakura.data.table.CityTable();
                            cityTable.geo_id = geoId;
                            cityTable.province_id = provinceId;
                            cityTable.city = city;
                            cityTable.save();
                        }
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }


    private void initializeIsp() {
        if (new Select().from(tv.ismar.sakura.data.table.IspTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                for (String isp : mIspArray) {
                    tv.ismar.sakura.data.table.IspTable ispTable = new tv.ismar.sakura.data.table.IspTable();
                    ispTable.isp_id = tv.ismar.sakura.utils.StringUtils.getMd5Code(isp);
                    ispTable.isp_name = isp;
                    ispTable.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void fetchCdnList() {
        String api = "http://wx.api.tvxio.com/shipinkefu/getCdninfo?actiontype=getcdnlist";
        Request request = new Request.Builder()
                .url(api)
                .build();
        Response response = null;
        try {
            response = OkHttpClientManager.getInstance().client.newCall(request).execute();
            tv.ismar.sakura.data.http.CdnListEntity cdnListEntity = new Gson().fromJson(response.body().string(), tv.ismar.sakura.data.http.CdnListEntity.class);
            initializeCdnTable(cdnListEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeCdnTable(tv.ismar.sakura.data.http.CdnListEntity cdnListEntity) {
        new Delete().from(tv.ismar.sakura.data.table.CdnTable.class).execute();
        ActiveAndroid.beginTransaction();
        try {
            for (tv.ismar.sakura.data.http.CdnListEntity.CdnEntity cdnEntity : cdnListEntity.getCdn_list()) {
                tv.ismar.sakura.data.table.CdnTable cdnTable = new tv.ismar.sakura.data.table.CdnTable();
                cdnTable.cdn_id = cdnEntity.getCdnID();
                cdnTable.cdn_name = cdnEntity.getName();
                cdnTable.cdn_nick = cdnEntity.getNick();
                cdnTable.cdn_flag = cdnEntity.getFlag();
                cdnTable.cdn_ip = cdnEntity.getUrl();
                cdnTable.district_id = getDistrictId(cdnEntity.getNick());
                cdnTable.isp_id = getIspId(cdnEntity.getNick());
                cdnTable.route_trace = cdnEntity.getRoute_trace();
                cdnTable.speed = 0;
                cdnTable.checked = false;
                cdnTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }


    private void fetchLocationByIP() {
        String api = "http://lily.tvxio.com/iplookup";
        Request request = new Request.Builder().url(api).build();
        Response response = null;
        try {
            response = OkHttpClientManager.getInstance().client.newCall(request).execute();
            tv.ismar.sakura.data.http.IpLookUpEntity ipLookUpEntity = new Gson().fromJson(response.body().string(), tv.ismar.sakura.data.http.IpLookUpEntity.class);
            initializeLocation(ipLookUpEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initializeLocation(tv.ismar.sakura.data.http.IpLookUpEntity ipLookUpEntity) {
        tv.ismar.sakura.data.table.CityTable cityTable = new Select().from(tv.ismar.sakura.data.table.CityTable.class).where(tv.ismar.sakura.data.table.CityTable.CITY + " = ?", ipLookUpEntity.getCity()).executeSingle();

        tv.ismar.sakura.core.preferences.AccountSharedPrefs accountSharedPrefs = tv.ismar.sakura.core.preferences.AccountSharedPrefs.getInstance(mContext);
        accountSharedPrefs.setSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.PROVINCE, ipLookUpEntity.getProv());
        accountSharedPrefs.setSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.CITY, ipLookUpEntity.getCity());
        accountSharedPrefs.setSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.ISP, ipLookUpEntity.getIsp());
        accountSharedPrefs.setSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.IP, ipLookUpEntity.getIp());
        if (cityTable != null) {
            accountSharedPrefs.setSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.GEO_ID, String.valueOf(cityTable.geo_id));
        }

        tv.ismar.sakura.data.table.ProvinceTable provinceTable = new Select().from(tv.ismar.sakura.data.table.ProvinceTable.class)
                .where(tv.ismar.sakura.data.table.ProvinceTable.PROVINCE_NAME + " = ?", ipLookUpEntity.getProv()).executeSingle();
        if (provinceTable != null) {
            accountSharedPrefs.setSharedPrefs(tv.ismar.sakura.core.preferences.AccountSharedPrefs.PROVINCE_PY, provinceTable.pinyin);
        }
    }

    private String getDistrictId(String cdnNick) {
        for (String district : mDistrictArray) {
            if (cdnNick.contains(district)) {
                return tv.ismar.sakura.utils.StringUtils.getMd5Code(district);
            }
        }
        // 第三方节点返回 "0"
        return "0";
    }

    private String getIspId(String cdnNick) {
        for (String isp : mIspArray) {
            if (cdnNick.contains(isp)) {
                return tv.ismar.sakura.utils.StringUtils.getMd5Code(isp);
            }
        }
        return tv.ismar.sakura.utils.StringUtils.getMd5Code(mIspArray[mIspArray.length - 1]);
    }
}
