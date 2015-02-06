package cn.ismartv.speedtester.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.core.preference.FeedbackProblem;
import cn.ismartv.speedtester.data.Empty;
import cn.ismartv.speedtester.data.IpLookUpEntity;
import cn.ismartv.speedtester.data.ProblemEntity;
import cn.ismartv.speedtester.data.http.EventInfoEntity;
import cn.ismartv.speedtester.utils.DeviceUtils;
import com.google.gson.Gson;
import com.ismartv.android.vod.service.HttpProxyService;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.HashMap;
import java.util.List;

/**
 * Created by huaijie on 14-11-12.
 */
public class MenuActivity extends BaseActivity implements View.OnHoverListener {
    private static final String TAG = "MenuActivity";

    public static final String TAB_FLAG = "TAB_FLAG";
    public static final int TAB_SPEED = 0;
    public static final int TAB_HELP = 1;
    public static final int TAB_FEEDBACK = 2;

//    private ShowcaseView showcaseView;

    @InjectViews({R.id.tab_speed, R.id.tab_help, R.id.tab_feedback})
    List<ImageView> tabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent ootStartIntent = new Intent(this, HttpProxyService.class);
        this.startService(ootStartIntent);
        setContentView(R.layout.activity_menu);
        ButterKnife.inject(this);


        for (ImageView tab : tabs)
            tab.setOnHoverListener(this);

        SharedPreferences sharedPreferences = this.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getInt(CacheManager.IpLookUp.USER_PROVINCE, -1) == -1 || sharedPreferences.getInt(CacheManager.IpLookUp.USER_ISP, -1) == -1) {
            fetchIpLookup();
        }


        fetchProblems();
    }


    @OnClick({R.id.tab_speed, R.id.tab_help, R.id.tab_feedback})
    public void pickTab(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        Gson gson = new Gson();
        HashMap<String, String> map = new HashMap<String, String>();

        map.put("time", String.valueOf(System.currentTimeMillis()));
        EventInfoEntity infoEntity = new EventInfoEntity();
        infoEntity.setEvent("speed_app_click");
        infoEntity.setProperties(map);


        switch (view.getId()) {


            case R.id.tab_speed:
                map.put("event", "TAB_SPEED");
                intent.putExtra(TAB_FLAG, TAB_SPEED);

                break;
            case R.id.tab_help:
                map.put("event", "TAB_HELP");
                intent.putExtra(TAB_FLAG, TAB_HELP);

                break;
            case R.id.tab_feedback:
                map.put("event", "TAB_FEEDBACK");
                intent.putExtra(TAB_FLAG, TAB_FEEDBACK);
                break;
            default:
                break;
        }
        uploadDeviceLog(Base64.encodeToString(gson.toJson(infoEntity, EventInfoEntity.class).getBytes(), Base64.DEFAULT));
        startActivity(intent);

    }


    private void fetchIpLookup() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.LILY_HOST)
                .build();

        ClientApi.IpLookUp client = restAdapter.create(ClientApi.IpLookUp.class);
        client.execute(new Callback<IpLookUpEntity>() {
            @Override
            public void success(IpLookUpEntity ipLookUpEntity, Response response) {
                CacheManager cacheManager = CacheManager.getInstance(MenuActivity.this);
                CacheManager.IpLookUp ipLookUp = cacheManager.new IpLookUp();
                ipLookUp.updateIpLookUpCache(ipLookUpEntity);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "fetchIpLookup failed!!!");
            }
        });
    }

    @Override
    public boolean onHover(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
                view.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                break;
            default:
                break;
        }
        return true;
    }

    private void uploadDeviceLog(String data) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(AppConstant.LOG_LEVEL)
                .setEndpoint(ClientApi.LOG_HOST)
                .build();
        ClientApi.DeviceLog client = restAdapter.create(ClientApi.DeviceLog.class);
        String sn = DeviceUtils.getSnCode();
        String modelName = DeviceUtils.getModel();
        client.execute(data, sn, modelName, new Callback<Empty>() {
            @Override
            public void success(Empty empty, Response response) {

            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }


    /**
     * fetch tv problems from http server
     */
    private void fetchProblems() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.NONE)
                .setEndpoint(ClientApi.Problems.HOST)
                .build();
        ClientApi.Problems client = restAdapter.create(ClientApi.Problems.class);
        client.excute(new Callback<List<ProblemEntity>>() {
            @Override
            public void success(List<ProblemEntity> problemEntities, retrofit.client.Response response) {
                FeedbackProblem feedbackProblem = FeedbackProblem.getInstance();
                feedbackProblem.saveCache(problemEntities);
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

}
