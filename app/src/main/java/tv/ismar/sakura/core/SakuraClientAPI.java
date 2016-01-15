package tv.ismar.sakura.core;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import tv.ismar.sakura.data.http.BindedCdnEntity;
import tv.ismar.sakura.data.http.ChatMsgEntity;
import tv.ismar.sakura.data.http.ProblemEntity;
import tv.ismar.sakura.data.http.TeleEntity;

/**
 * Created by huaijie on 2015/4/7.
 */
public class SakuraClientAPI {

    public interface Problems {
        @GET("/customer/points/")
        Call<List<ProblemEntity>> excute(

        );
    }

    public interface Feedback {
        @GET("/customer/getfeedback/")
        Call<ChatMsgEntity> excute(
                @Query("sn") String sn,
                @Query("topn") String topn

        );
    }


    public interface GetBindCdn {
        @GET("/shipinkefu/getCdninfo?actiontype=getBindcdn")
        Call<BindedCdnEntity> excute(
                @Query("sn") String snCode
        );
    }

    public interface BindCdn {
        @GET("/shipinkefu/getCdninfo?actiontype=bindecdn")
        Call<ResponseBody> excute(
                @Query("sn") String snCode,
                @Query("cdn") int cdnId
        );
    }

    /**
     * UnbindNode
     */
    public interface UnbindNode {
        @GET("/shipinkefu/getCdninfo?actiontype=unbindCdn")
        Call<ResponseBody> excute(
                @Query("sn") String sn

        );
    }


    public interface FetchTel {
        String ACTION = "getContact";

        @GET("/shipinkefu/getCdninfo")
        Call<List<TeleEntity>> excute(
                @Query("actiontype") String actiontype,
                @Query("ModeName") String modeName,
                @Query("sn") String sn

        );
    }


    public interface DeviceLog {
        @GET("/log")
        Call<ResponseBody> execute(
                @Query("data") String data,
                @Query("sn") String sn,
                @Query("modelname") String modelName

        );
    }

    public interface UploadResult {
        String ACTION_TYPE = "submitTestData";

        @FormUrlEncoded
        @POST("/shipinkefu/getCdninfo")
        Call<ResponseBody> excute(
                @Field("actiontype") String actionType,
                @Field("snCode") String snCode,
                @Field("nodeId") String nodeId,
                @Field("nodeSpeed") String nodeSpeed
        );
    }

    public interface UploadFeedback {
        @FormUrlEncoded
        @POST("/customer/pointlogs/")
        Call<ResponseBody> excute(
                @Header("User-Agent") String userAgent,
                @Field("q") String q
        );
    }

}
