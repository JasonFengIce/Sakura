package cn.ismartv.speedtester.core;

import cn.ismartv.speedtester.data.ProblemEntity;
import cn.ismartv.speedtester.data.TicketEntity;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import java.util.List;

/**
 * Created by huaijie on 14-10-30.
 */
public class ClientApi {
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

}
