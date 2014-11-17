package cn.ismartv.speedtester.core;

import android.content.Context;
import android.os.Handler;
import cn.ismartv.speedtester.core.logger.Logger;
import cn.ismartv.speedtester.data.*;
import cn.ismartv.speedtester.ui.fragment.FragmentFeedback;
import cn.ismartv.speedtester.utils.DeviceUtils;
import com.activeandroid.util.Log;
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
    private static final String TAG = "ClientApi";

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

    public interface UnbindNode{
        public String ACTION = "unbindCdn";

        @GET("/shipinkefu/getCdninfo")
        void excute(
                @Query("actiontype") String actiontype,
                @Query("sn") String sn,
                Callback<Empty> callback
        );

    }


}
