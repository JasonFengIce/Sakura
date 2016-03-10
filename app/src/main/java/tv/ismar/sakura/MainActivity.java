package tv.ismar.sakura;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import tv.ismar.sakura.core.FeedbackProblem;
import tv.ismar.sakura.core.SakuraClientAPI;
import tv.ismar.sakura.core.ScreenManager;
import tv.ismar.sakura.core.client.OkHttpClientManager;
import tv.ismar.sakura.core.event.AnswerAvailableEvent;
import tv.ismar.sakura.core.update.AppUpdateUtilsV2;
import tv.ismar.sakura.data.http.ProblemEntity;
import tv.ismar.sakura.ui.activity.HomeActivity;
import tv.ismar.sakura.ui.widget.AppUpdateMessagePopWindow;
import tv.ismar.sakura.ui.widget.MessagePopWindow;

public class MainActivity extends Activity implements View.OnClickListener, OnHoverListener, OnKeyListener {
    private static final String TAG = "LauncherActivity";
    private MessagePopWindow networkErrorPopupWindow;
    private AppUpdateMessagePopWindow appUpdateMessagePopWindow;

    private ImageView indicatorNode;
    private ImageView indicatorFeedback;
    private ImageView indicatorHelp;
    private View contentView;

    private ImageView tmpImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenManager.getScreenManager().pushActivity(this);
        contentView = LayoutInflater.from(this).inflate(R.layout.sakura_activity_launch, null);
        setContentView(contentView);
        initViews();

        networkErrorPopupWindow = new MessagePopWindow(this, "网络异常，请检查网络", null);


        AppUpdateUtilsV2.getInstance(this).checkAppUpdate();
        fetchProblems();
    }

    @Override
    protected void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private void initViews() {
        indicatorNode = (ImageView) findViewById(R.id.indicator_node_image);
        indicatorFeedback = (ImageView) findViewById(R.id.indicator_feedback_image);
        indicatorHelp = (ImageView) findViewById(R.id.indicator_help_image);
        tmpImageView = (ImageView) findViewById(R.id.tmp);

        indicatorNode.setOnClickListener(this);
        indicatorFeedback.setOnClickListener(this);
        indicatorHelp.setOnClickListener(this);

        indicatorNode.setOnHoverListener(this);
        indicatorFeedback.setOnHoverListener(this);
        indicatorHelp.setOnHoverListener(this);

        indicatorNode.setOnKeyListener(this);

        indicatorNode.requestFocusFromTouch();


    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmpImageView.requestFocus();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setClass(this, HomeActivity.class);

        switch (view.getId()) {
            case R.id.indicator_node_image:
                intent.putExtra("position", 0);
                break;
            case R.id.indicator_feedback_image:
                intent.putExtra("position", 1);
                break;

            case R.id.indicator_help_image:
                intent.putExtra("position", 2);
                break;
        }
        startActivity(intent);
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.i(TAG, "key :" + keyCode);
        return false;
    }

    /**
     * fetch tv problems from http server
     */
    private void fetchProblems() {
        SakuraClientAPI.Problems client = OkHttpClientManager.getInstance().restAdapter_IRIS_TVXIO.create(SakuraClientAPI.Problems.class);
        client.excute().enqueue(new Callback<List<ProblemEntity>>() {
            @Override
            public void onResponse(Response<List<ProblemEntity>> response) {
                if (response.errorBody() == null) {
                    FeedbackProblem feedbackProblem = FeedbackProblem.getInstance();
                    feedbackProblem.saveCache(response.body());
                } else {
                    EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                EventBus.getDefault().post(new AnswerAvailableEvent(AnswerAvailableEvent.EventType.NETWORK_ERROR, AnswerAvailableEvent.NETWORK_ERROR));
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (networkErrorPopupWindow != null) {
            networkErrorPopupWindow.dismiss();
            networkErrorPopupWindow = null;
        }
        if (appUpdateMessagePopWindow != null) {
            appUpdateMessagePopWindow.dismiss();
            appUpdateMessagePopWindow = null;
        }

        ScreenManager.getScreenManager().popActivity(this);
        super.onDestroy();
    }

    @Subscribe
    public void answerAvailable(AnswerAvailableEvent event) {
        switch (event.getEventType()) {
            case NETWORK_ERROR:
                showNetworkErrorPop();
                break;
            case APP_UPDATE:
                showAppUpdatePop((Bundle) event.getMsg());
                break;
        }
    }

    private void showNetworkErrorPop() {
        networkErrorPopupWindow = new MessagePopWindow(this, "网络异常，请检查网络", null);
        contentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!networkErrorPopupWindow.isShowing()) {
                    networkErrorPopupWindow.showAtLocation(contentView, Gravity.CENTER, new MessagePopWindow.ConfirmListener() {
                                @Override
                                public void confirmClick(View view) {
                                    networkErrorPopupWindow.dismiss();
                                    ScreenManager.getScreenManager().popAllActivityExceptOne(null);
                                }
                            },
                            null
                    );
                }
            }
        }, 1000);
    }

    private void showAppUpdatePop(Bundle bundle) {
        Log.i(TAG, "app update...");
        final String path = bundle.getString("path");
        List<String> msgs = bundle.getStringArrayList("msgs");
        appUpdateMessagePopWindow = new AppUpdateMessagePopWindow(this, msgs, new AppUpdateMessagePopWindow.ConfirmListener() {
            @Override
            public void confirmClick(View view) {
                appUpdateMessagePopWindow.dismiss();
                installApk(MainActivity.this, path);
            }
        }, new AppUpdateMessagePopWindow.CancelListener() {
            @Override
            public void cancelClick(View view) {
                appUpdateMessagePopWindow.dismiss();
            }
        });

        contentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!appUpdateMessagePopWindow.isShowing()) {
                    appUpdateMessagePopWindow.setButtonText("立即升级", "下次升级");
                    appUpdateMessagePopWindow.showAtLocation(contentView, Gravity.CENTER);
                }
            }
        }, 1000);
    }

    public void installApk(Context mContext, String path) {
        Uri uri = Uri.parse("file://" + path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


}
