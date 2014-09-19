package ismartv.android.vod.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import cn.ismartv.speedtester.utils.DevicesUtilities;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by huaijie on 8/14/14.
 */
public class NetWorkUtil {
    private static final String TAG = "NetWorkUtil";
    private static final String HOST = "http://wx.api.tvxio.com";


    private static NetWorkUtil netWorkUtil = new NetWorkUtil();

    private NetWorkUtil() {
    }


    interface Client{
        @GET("/weixin4server/uploadclientip")
        void excute(
                @Query("mac_address") String mac_address,
                @Query("client_ip") String client_ip,
                @Query("sn") String sn,
                @Query("tvmode") String tvmode,
                Callback<String> callback
        );
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

    public static NetWorkUtil getInstant() {
        return netWorkUtil;
    }


    interface Ticket {
        @GET("/weixin4server/qrcodeaction")
        Observable<Response> excute(
                @Query("ipaddress") String ipaddress,
                @Query("macaddress") String macaddress
        );
    }

    public static Observable<Response> getImage(Context context) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        Ticket client = restAdapter.create(Ticket.class);
       return client.excute(DevicesUtilities.getLocalIpAddressV4(), DevicesUtilities.getLocalMacAddress(context));
    }

    public static void getTicket(final Context context) {
        Observable.from(new String[]{"getLatestAppVersion"})
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String args) {
                        return getImage(context);
                    }
                })
                .subscribe(new Action1<Response>() {
                               @Override
                               public void call(Response response) {
                                   String result = Utils.getResult(response);
                                   qrcode(result);
                               }
                           }
                );
    }


    private static void qrcode(String ticket) {
        try {
            URL url = new URL("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("GET");
            InputStream in = urlConnection.getInputStream();
            Bitmap bmp = BitmapFactory.decodeStream(in);
//            if (null != HomeActivity.handler) {
//                Message message = HomeActivity.handler.obtainMessage(HomeActivity.QR_CODE, bmp);
//                HomeActivity.handler.sendMessage(message);
//            }
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}
