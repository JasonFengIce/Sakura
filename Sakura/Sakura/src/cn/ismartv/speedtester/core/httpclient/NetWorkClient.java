package cn.ismartv.speedtester.core.httpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.data.HttpData;
import cn.ismartv.speedtester.data.NodeTag;
import cn.ismartv.speedtester.data.VersionInfo;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import cn.ismartv.speedtester.utils.Utilities;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by <huaijiefeng@gmail.com> on 8/21/14.
 */
public class NetWorkClient extends BaseClient {


    public static final String TAG = "NetWorkClient";

    interface NodeList {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<HttpData> callback
        );
    }



    private static void getNodeList(final  Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        NodeList client = restAdapter.create(NodeList.class);
         client.excute("getcdnlist", new Callback<HttpData>() {
             @Override
             public void success(HttpData httpData, Response response) {
                 CacheManager.updateNodeCache(context, httpData.getCdn_list());
             }

             @Override
             public void failure(RetrofitError retrofitError) {

             }
         });
    }

    interface AppVersionInfo {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<VersionInfo> callback
        );
    }


    public static void getLatestAppVersion(final  Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        AppVersionInfo client = restAdapter.create(AppVersionInfo.class);
        client.excute("getLatestAppVersion", new Callback<VersionInfo>() {
            @Override
            public void success(VersionInfo versionInfo, Response response) {
                CacheManager.updateSpeedLogUrl(context, versionInfo.getSpeedlogurl());
                PackageInfo packageInfo = null;
                try {
                    packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (packageInfo.versionCode < Integer.parseInt(versionInfo.getVersion())) {
                    Log.d(TAG, "update url is --> " + versionInfo.getDownloadurl());
                    downloadAPK(context, versionInfo.getDownloadurl(), versionInfo.getVersion(), versionInfo.getMd5(), 1);
                } else {
                    CacheManager.updateVersion(context, 0, "");
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    interface Tag {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<NodeTag> callback
        );
    }


    public static void getTag(final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        Tag client = restAdapter.create(Tag.class);
        client.excute("gettag", new Callback<NodeTag>() {
            @Override
            public void success(NodeTag tag, Response response) {

                if (isFirstInstall(context)) {
                    getNodeList(context);
                }
                if (tag.isChanged()) {
                    getNodeList(context);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }





    private static void downloadAPK(Context context, String downloadurl, String version, String md5, int count) {
        if (count > 3)
            return;
        File fileName = null;
        String apkName = null;
        try {
            int byteread;
            URL url = new URL(downloadurl);
            apkName = "Sakura_" + version + ".apk";
            fileName = new File(DevicesUtilities.getUpdateDirectory(), apkName);
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(fileName);
            byte[] buffer = new byte[1024];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            fs.flush();
            fs.close();
            inStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String MD5Value = Utilities.getMd5ByFile(fileName);
        Log.d(TAG, "local apk md5 code is : " + MD5Value + " --> " + md5);
        if (md5.equals(MD5Value)) {
            CacheManager.updateVersion(context, 1, apkName);
        } else {
            downloadAPK(context, downloadurl, version, md5, count + 1);
        }
    }

    private static boolean isFirstInstall(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        if (preferences.getBoolean("first_install", true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("first_install", false);
            editor.apply();
            return true;
        }
        return false;
    }
}
