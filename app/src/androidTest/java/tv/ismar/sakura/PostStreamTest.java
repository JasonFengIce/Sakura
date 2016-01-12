package tv.ismar.sakura;

import android.test.AndroidTestCase;
import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.core.client.OkHttpClientManager;
import tv.ismar.sakura.data.http.ChatMsgEntity;
import tv.ismar.sakura.utils.DeviceUtils;

/**
 * Created by huaijie on 1/12/16.
 */
public class PostStreamTest extends AndroidTestCase {
    private static final String TAG = "PostStreamTest";

    public void testPostStream() {

        OkHttpClientManager clientManager = OkHttpClientManager.getInstance();
        SakuraClientAPI.UploadFeedback client = clientManager.restAdapter_IRIS_TVXIO.create(SakuraClientAPI.UploadFeedback.class);
        String userAgent = android.os.Build.MODEL.replaceAll(" ", "_") + "/" + android.os.Build.ID + " " + DeviceUtils.getSnToken();

        String json = "{\"city\":\"上海\",\"description\":\"方法关于\",\"ip\":\"116.228.50.238\",\"isp\":\"电信\",\"location\":\"上海\",\"phone\":\"15370770695\",\"option\":3}";
        try {
            Response<ResponseBody> response = client.excute(userAgent, (json)).execute();
            Log.d(TAG, "test");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void testFetchFeedback() {
        Retrofit retrofit = OkHttpClientManager.getInstance().restAdapter_IRIS_TVXIO;
        SakuraClientAPI.Feedback feedback = retrofit.create(SakuraClientAPI.Feedback.class);
        feedback.excute("1", "10").enqueue(new Callback<ChatMsgEntity>() {
            @Override
            public void onResponse(Response<ChatMsgEntity> response) {
                ChatMsgEntity chatMsgEntity = response.body();

                for (ChatMsgEntity.Data data : chatMsgEntity.getData()) {
                    Log.i(TAG, data.getCommont());
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

}
