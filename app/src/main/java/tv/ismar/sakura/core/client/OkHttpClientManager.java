package tv.ismar.sakura.core.client;

import java.util.concurrent.TimeUnit;

import cn.ismartv.log.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

/**
 * Created by huaijie on 1/7/16.
 */
public class OkHttpClientManager {

    public static final String API_HOST = "http://wx.api.tvxio.com/";
    public static final String IRIS_TVXIO_HOST = "http://iris.tvxio.com";
    private static final String SPEED_CALLA_TVXIO_HOST = "http://speed.calla.tvxio.com";
    private static final String LILY_TVXIO_HOST = "http://lily.tvxio.com";

    private static final int DEFAULT_TIMEOUT = 2;

    private static OkHttpClientManager instance;

    public OkHttpClient client;

    public Retrofit restAdapter_WX_API_TVXIO;
    public Retrofit restAdapter_SPEED_CALLA_TVXIO;
    public Retrofit restAdapter_LILY_TVXIO_HOST;
    public Retrofit restAdapter_IRIS_TVXIO;

    private OkHttpClientManager() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        restAdapter_WX_API_TVXIO = new Retrofit.Builder()
                .client(client)
                .baseUrl(API_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAdapter_SPEED_CALLA_TVXIO = new Retrofit.Builder()
                .client(client)
                .baseUrl(SPEED_CALLA_TVXIO_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAdapter_LILY_TVXIO_HOST = new Retrofit.Builder()
                .client(client)
                .baseUrl(LILY_TVXIO_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restAdapter_IRIS_TVXIO = new Retrofit.Builder()
                .client(new OkHttpClient())
                .baseUrl(IRIS_TVXIO_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }


    public static OkHttpClientManager getInstance() {
        if (instance == null) {
            instance = new OkHttpClientManager();
        }
        return instance;
    }
}
