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
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * Created by <huaijiefeng@gmail.com> on 8/21/14.
 */
public class NetWorkClient extends BaseClient {

    private static final String WEIXIN_HOST = "http://192.168.1.185:8099";


    public static final String TAG = "NetWorkClient";

    interface Client {
        @GET("/shipinkefu/getCdninfo")
        Observable<Response> excute(
                @Query("actiontype") String actiontype
        );
    }

    interface WeiXin {
        @GET("/weixin/uploadclientip")
        Observable<Response> excute(
                @Query("mac_address") String mac_address,
                @Query("client_ip") String client_ip,
                @Query("sn") String sn
        );
    }


    private static Observable<Response> weiXinUpload(String mac_address, String client_ip, String sn) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(WEIXIN_HOST)
                .build();
        WeiXin client = restAdapter.create(WeiXin.class);
        return client.excute(mac_address, client_ip, sn);
    }

    private static Observable<Response> send(String actiontype) {
        Log.d(TAG, "action type  --> " + actiontype);
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();
        class MyErrorHandler implements ErrorHandler {
            @Override
            public Throwable handleError(RetrofitError cause) {
                Response r = cause.getResponse();
                if (r != null && r.getStatus() == 401) {
                    return new Exception(cause);
                }
                Log.d(TAG, cause.getMessage());
                return cause;
            }
        }
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        Client client = restAdapter.create(Client.class);
        return client.excute(actiontype);
    }


    public static void uploadWeixin(final Context context) {
        Observable.from(new String[]{DevicesUtilities.getLocalMacAddress(context)})
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String args) {
                        return NetWorkClient.weiXinUpload(args, DevicesUtilities.getLocalIpAddressV4(), DevicesUtilities.getSNCode());
                    }
                })
                .subscribe(new Action1<Response>() {
                               @Override
                               public void call(Response response) {
                                   String result = Utils.getResult(response);
                                   Log.d(TAG, result);
                               }
                           }
                );
    }

    private static void getNodeList(final Context context) {
        Observable.from(new String[]{"getcdnlist"})
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String args) {
                        return NetWorkClient.send(args);
                    }
                })
                .subscribe(new Action1<Response>() {
                               @Override
                               public void call(Response response) {
                                   String result = Utils.getResult(response);
                                   Log.d(TAG, result);
                                   HttpData httpData = new Gson().fromJson(result, HttpData.class);
                                   CacheManager.updateNodeCache(context, httpData.getCdn_list());
                               }
                           }
                );
    }

    public static void getLatestAppVersion(final Context context) {
        Observable.from(new String[]{"getLatestAppVersion"})
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String args) {
                        return NetWorkClient.send(args);
                    }
                })
                .subscribe(new Action1<Response>() {
                               @Override
                               public void call(Response response) {
                                   String result = Utils.getResult(response);
                                   Log.d(TAG, "getLatestAppVersion --->" + result);
                                   VersionInfo versionInfo = new Gson().fromJson(result, VersionInfo.class);
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
                           }
                );
    }

    public static void getTag(final Context context) {
        Observable.from(new String[]{"gettag"})
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String args) {
                        return NetWorkClient.send(args);
                    }
                })
                .subscribe(new Action1<Response>() {
                               @Override
                               public void call(Response response) {
                                   String result = Utils.getResult(response);
                                   Log.d(TAG, result);
                                   NodeTag tag = new Gson().fromJson(result, NodeTag.class);
                                   if (isFirstInstall(context)) {
                                       getNodeList(context);
                                   }
                                   if (tag.isChanged()) {
                                       getNodeList(context);
                                   }
                               }
                           }
                );
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
