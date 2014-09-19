package cn.ismartv.speedtester.core.httpclient;

import android.util.Log;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.*;
import rx.Observable;

/**
 * Created by <huaijiefeng@gmail.com> on 8/22/14.
 */
public class CdnClient extends BaseClient {
    public static final String TAG = "CdnClient";

    interface GetBindCdn {
        @GET("/shipinkefu/getCdninfo")
        Observable<Response> excute(
                @Query("actiontype") String actiontype,
                @Query("sn") String snCode
        );
    }

    interface BindCdn {
        @POST("/shipinkefu/getCdninfo")
        Observable<Response> excute(
                @Query("actiontype") String actiontype,
                @Query("sn") String snCode,
                @Query("cdn") String cdnNumber
        );
    }


    public static Observable<Response> get(String actiontype, String sn) {
        Log.d(TAG, "action type  --> " + actiontype);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        GetBindCdn client = restAdapter.create(GetBindCdn.class);
        return client.excute(actiontype, sn);
    }

    public static Observable<Response> bind(String actiontype, String snCode, String cdnNumber) {
        Log.d(TAG, "action type  --> " + actiontype);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        BindCdn client = restAdapter.create(BindCdn.class);
        return client.excute(actiontype, snCode, cdnNumber);
    }

    interface UploadResult {
        @FormUrlEncoded
        @POST("/shipinkefu/getCdninfo")
        Observable<Response> excute(
                @Field("actiontype") String actionType,
                @Field("snCode") String snCode,
                @Field("nodeId") String nodeId,
                @Field("nodeSpeed") String nodeSpeed
        );
    }



    public static Observable<Response> uploadResult(String actiontype, String snCode, String nodeId, String nodeSpeed) {
        Log.d(TAG, "action type  --> " + actiontype);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(HOST)
                .build();
        UploadResult client = restAdapter.create(UploadResult.class);
        return client.excute(actiontype, snCode, nodeId, nodeSpeed);
    }
}
