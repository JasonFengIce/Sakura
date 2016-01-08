package tv.ismar.sakura.core.client;

import java.util.concurrent.TimeUnit;

import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;

/**
 * Created by huaijie on 1/7/16.
 */
public class OkHttpClientManager {
    private static final int DEFAULT_TIMEOUT = 2;

    private static tv.ismar.sakura.core.client.OkHttpClientManager instance;

    private static OkHttpClient client;

    private OkHttpClientManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
    }


    public static OkHttpClient getClient() {
        if (instance == null) {
            instance = new tv.ismar.sakura.core.client.OkHttpClientManager();
        }
        return client;
    }
}
