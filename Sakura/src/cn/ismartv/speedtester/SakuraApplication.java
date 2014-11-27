package cn.ismartv.speedtester;

import android.content.Context;
import android.content.SharedPreferences;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.logger.Logger;
import cn.ismartv.speedtester.data.HttpDataEntity;
import cn.ismartv.speedtester.data.LocationEntity;
import cn.ismartv.speedtester.data.NodeTagEntity;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.app.Application;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by huaijie on 14-10-30.
 */
public class SakuraApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        CacheManager.getInstance(this).initializeCityTable();
        getTag(this);
    }


    public void getTag(final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.Tag client = restAdapter.create(ClientApi.Tag.class);
        client.excute("gettag", new Callback<NodeTagEntity>() {
            @Override
            public void success(NodeTagEntity tag, Response response) {
                if (isFirstInstall(context) || tag.isChanged()) {
                    getNodeList(context);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    private static void getNodeList(final Context context) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(AppConstant.API_HOST)
                .build();
        ClientApi.NodeList client = restAdapter.create(ClientApi.NodeList.class);
        client.excute("getcdnlist", new Callback<HttpDataEntity>() {
            @Override
            public void success(HttpDataEntity httpData, Response response) {
                CacheManager.updateNodeCache(context, httpData.getCdn_list());
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    private boolean isFirstInstall(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        if (preferences.getBoolean("first_install", true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("first_install", false);
            editor.apply();
            return true;
        }
        return false;
    }
}
