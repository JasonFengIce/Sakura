package com.ismartv.android.vod.core;

import android.content.Context;
import android.util.Log;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by huaijie on 8/14/14.
 */
public class NetWorkUtil {
    private static final String TAG = "NetWorkUtil";
    private static final String HOST = "http://wx.api.tvxio.com";


    private static NetWorkUtil netWorkUtil = new NetWorkUtil();

    private NetWorkUtil() {
    }

    public static NetWorkUtil getInstant() {
        return netWorkUtil;
    }

    public void weiXinUpload(Context context) {


        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        Client client = restAdapter.create(Client.class);
        client.excute(DevicesUtilities.getLocalMacAddress(context), DevicesUtilities.getLocalIpAddressV4(), DevicesUtilities.getSNCode(),
                DevicesUtilities.getModeName(), new Callback<String>() {
                    @Override
                    public void success(String o, retrofit.client.Response response) {
//                        Log.d(TAG, o.toString());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.e(TAG, retrofitError.getMessage());
                    }
                }
        );
    }

    interface Client {
        @GET("/weixin4server/uploadclientip")
        void excute(
                @Query("mac_address") String mac_address,
                @Query("client_ip") String client_ip,
                @Query("sn") String sn,
                @Query("tvmode") String tvmode,
                Callback<String> callback
        );
    }



}
