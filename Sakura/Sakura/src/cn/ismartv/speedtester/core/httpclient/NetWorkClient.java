package cn.ismartv.speedtester.core.httpclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.data.HttpData;
import cn.ismartv.speedtester.data.NodeTag;
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
