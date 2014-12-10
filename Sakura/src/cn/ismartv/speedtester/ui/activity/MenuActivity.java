package cn.ismartv.speedtester.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.core.ClientApi;
import cn.ismartv.speedtester.core.cache.CacheManager;
import cn.ismartv.speedtester.data.IpLookUpEntity;
import com.ismartv.android.vod.core.install.BootInstallTask;
import com.ismartv.android.vod.service.HttpProxyService;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.File;
import java.util.List;

/**
 * Created by huaijie on 14-11-12.
 */
public class MenuActivity extends BaseActivity {
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
        deleteApk();
        setContentView(R.layout.activity_menu);
        ButterKnife.inject(this);

        SharedPreferences sharedPreferences = this.getSharedPreferences(AppConstant.APP_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getInt(CacheManager.IpLookUp.USER_PROVINCE, -1) == -1 || sharedPreferences.getInt(CacheManager.IpLookUp.USER_ISP, -1) == -1) {
            fetchIpLookup();
        }

        ///////////////////////////////////////////////////////////////
        //Add Showcase View
        ///////////////////////////////////////////////////////////////
//        showcaseView = new ShowcaseView.Builder(this)
//                .setTarget(new ViewTarget(findViewById(R.id.tab_speed)))
//                .setOnClickListener(this)
//                .build();
//        showcaseView.setButtonText(getString(R.string.next));
    }


    @OnClick({R.id.tab_speed, R.id.tab_help, R.id.tab_feedback})
    public void pickTab(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        switch (view.getId()) {
            case R.id.tab_speed:
                intent.putExtra(TAB_FLAG, TAB_SPEED);
                break;
            case R.id.tab_help:
                intent.putExtra(TAB_FLAG, TAB_HELP);
                break;
            case R.id.tab_feedback:
                intent.putExtra(TAB_FLAG, TAB_FEEDBACK);
                break;
            default:
                break;
        }
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

    /**
     * delete sakura apk, delete vod server apk
     */
    private void deleteApk() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(20000);
                    File file = new File(MenuActivity.this.getFilesDir(), BootInstallTask.SELF_APP_NAME);
                    if (null != file)
                        file.delete();
                    file = new File(MenuActivity.this.getFilesDir(), BootInstallTask.VOD_APP_NAME);
                    if(null!= file)
                        file.delete();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

}
