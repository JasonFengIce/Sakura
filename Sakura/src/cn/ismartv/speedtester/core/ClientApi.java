package cn.ismartv.speedtester.core;

import android.content.Context;
import android.os.Handler;
import cn.ismartv.speedtester.data.*;
import cn.ismartv.speedtester.ui.fragment.FragmentFeedback;
import cn.ismartv.speedtester.utils.DeviceUtils;
import com.google.gson.Gson;
import retrofit.Callback;
import retrofit.http.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * Created by huaijie on 14-10-30.
 */
public class ClientApi {
    public static final String HOST = "http://wx.api.tvxio.com";

    public interface Problems {
        String HOST = "http://iris.tvxio.com";

        @GET("/customer/points/")
        void excute(
                Callback<List<ProblemEntity>> callback
        );
    }


    public interface Ticket {
        public static final String HOST = "http://wx.api.tvxio.com";

        @GET("/weixin4server/qrcodeaction")
        void excute(
                @Query("ipaddress") String ipaddress,
                @Query("macaddress") String macaddress,
                Callback<TicketEntity> callback
        );
    }

    public interface Tag {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<NodeTagEntity> callback
        );
    }

    public interface NodeList {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<HttpDataEntity> callback
        );
    }

    public interface Feedback {
        String HOST = "http://iris.tvxio.com";

        @GET("/customer/getfeedback/")
        void excute(
                @Query("sn") String sn,
                @Query("topn") String topn,
                Callback<ChatMsgEntity> callback
        );
    }

    public interface UploadResult {
        @FormUrlEncoded
        @POST("/shipinkefu/getCdninfo")
        void excute(
                @Field("actiontype") String actionType,
                @Field("snCode") String snCode,
                @Field("nodeId") String nodeId,
                @Field("nodeSpeed") String nodeSpeed,
                Callback<Empty> callback
        );
    }


    public interface BindCdn {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                @Query("sn") String snCode,
                @Query("cdn") String cdnNumber,
                Callback<Empty> callback

        );
    }

    public interface GetBindCdn {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                @Query("sn") String snCode,
                Callback<HttpDataEntity> callback
        );
    }


    public interface Location {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<LocationEntity> callback

        );
    }


    public interface AppVersionInfo {
        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                Callback<VersionInfoEntity> callback
        );
    }

    public interface UploadClientIp {
        @GET("/weixin4server/uploadclientip")
        void excute(
                @Query("mac_address") String mac_address,
                @Query("client_ip") String client_ip,
                @Query("sn") String sn,
                @Query("tvmode") String tvmode,
                Callback<Empty> callback
        );
    }

    public interface FetchTel {
        public String ACTION = "getContact";

        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                @Query("ModeName") String modeName,
                @Query("sn") String sn,
                Callback<List<TeleEntity>> callback
        );
    }


    public static void uploadFeedback(Context context, FeedBackEntity feedBack, final Handler handler) {

        final String str = new Gson().toJson(feedBack);
        new Thread() {
            @Override
            public void run() {
                String localeName = Locale.getDefault().toString();
                URL url;
                HttpURLConnection conn = null;
                InputStream inputStream;
                BufferedReader reader = null;
                StringBuffer sb = new StringBuffer();
                OutputStream outputStream;
                BufferedWriter writer = null;
                try {
                    url = new URL("http://iris.tvxio.com/customer/pointlogs/");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("content-type", "text/json");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", android.os.Build.MODEL.replaceAll(" ", "_") + "/" + android.os.Build.ID + " " + DeviceUtils.getSnCode());
                    conn.setRequestProperty("Accept-Language", localeName);
                    outputStream = conn.getOutputStream();
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write("q=" + str);
                    writer.flush();
                    int statusCode = conn.getResponseCode();
                    if (statusCode == 200) {
                        inputStream = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if ("OK".equals(sb.toString()) && null != handler) {
                    handler.sendEmptyMessage(FragmentFeedback.UPLAOD_FEEDBACK_COMPLETE);
                } else {
                    handler.sendEmptyMessage(FragmentFeedback.UPLAOD_FEEDBACK_FAILED);
                }
            }
        }.start();
    }
}
