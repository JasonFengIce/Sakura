package cn.ismartv.speedtester.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.*;
import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.setting.domain.*;
import cn.ismartv.speedtester.ui.activity.BaseActivity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OobeActivity extends BaseActivity implements OnKeyListener {
    private static final String TAG = "OobeActivity";

    private static final int TEST_STATE_IDLE = 0;
    private static final int TEST_STATE_PENDING = 1;
    private static final int TEST_STATE_TESTING = 2;

    private static final int DIALOG_NETWORK_UNESTABLISHED = 1;
    private static final int DIALOG_NETWORK_EXCEPTION = 2;
    private static final int DIALOG_PENDING_CANCEL = 3;

    public static final int NETWORK_CONNECTION_NORMAL = 0;
    public static final int NETWORK_CONNECTION_TIMEOUT = 1;
    public static final int NETWORK_CONNECTION_UNKNOWN = 2;

    /**
     * The system broadcast intent string
     */
    public static final String ACTION_LAUNCHER = "com.lenovo.dll.nebula.launcher.home";
    public static final String ACTION_SETTING = "com.lenovo.nebula.settings.action.launch";

    /**
     * mHandler is used to update ui and control file download progress.
     */
    private Handler mTimingHandler = new Handler();
    private Handler mDownloadHandler = new Handler();

    private Thread mDownloaderThread = null;
    private GetTestUrlRunnable mGetTestUrlRunnable = null;

    private RelativeLayout mGraphicHolder;
    private TextView mCurrentStateText;
    private Button mSpeedActionButton;
    private ProgressBar mCurrentProgressBar;
    private Resources mResources;
    private TextView mAverageSpeedShowText;
    private ProgressBar mAverageBandwidthIndicator;
    private TextView mBandwidthSuggestion;

    private TextView mSuggestionText;

    private TextView[] mSpeedIndicatorTextArray;
    private PointerView mDashBoardPointer;

    private String mJsonStr = null;
    private ArrayList<NetworkSpeedInfo> mNetworkSpeedInfoList = null;
    private NetworkSpeedInfo mCurrentNetworkSpeedInfo = null;
    private FakeNetWorkSpeedInfo mCurrFakeNetWorkSpeedInfo = null;
    private ArrayList<SpeedInfoUploadEntity> mCurrentSpeedTestResults = null;
    private FeedBackEntity mFeedBackEntity = null;

    private int mTestState = TEST_STATE_IDLE;
    private int mCurrentPosition = 0;

    private boolean isNetworkAvailable = false;

    public int mDisappearTime = 0;

    public static String domain = "http://iris.tvxio.com";

    private RelativeLayout mCurrentStateShowArea;
    private LinearLayout mTestResultArea;

    public boolean isSetting = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setView();
        layoutView();
        initViews();
        mResources = getResources();
        mFeedBackEntity = new FeedBackEntity();
    }

    public void setView() {
        switch (tvModel) {
            case S51:
                setContentView(R.layout.activity_setting_for_s51);
                break;
            case S52:
                setContentView(R.layout.activity_setting_for_s52);
                break;
            case S9i:
                setContentView(R.layout.activity_setting_for_s9i);
                break;
            default:
                break;
        }
    }

    private void layoutView() {
        switch (tvModel) {
            case S51:
                break;
            case S52:
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.x = 228;
                params.y = -1;
                params.width = 1110;
                params.height = 971;
                getWindow().setAttributes(params);
                break;
            case S9i:
                WindowManager.LayoutParams S9iparams = getWindow().getAttributes();
//                S9iparams.x = 248;
//                S9iparams.y = -1;
                S9iparams.width = WindowManager.LayoutParams.MATCH_PARENT;
                S9iparams.height = WindowManager.LayoutParams.MATCH_PARENT;
                S9iparams.gravity = Gravity.CENTER;
                getWindow().setAttributes(S9iparams);
                break;
            default:
                break;
        }
    }

    public void setTheme() {
        switch (tvModel) {
            case S51:
                setTheme(R.style.oobe_for_s51_style);
                break;
            case S52:
                setTheme(R.style.oobe_for_s52_style);
                break;
            case S9i:
                setTheme(R.style.oobe_for_s9i_style);
                break;
            default:
                break;
        }
    }

    private void initViews() {
        mCurrentStateText = (TextView) findViewById(R.id.current_state_text);
        mGraphicHolder = (RelativeLayout) findViewById(R.id.graphic_holder);
        mSpeedActionButton = (Button) findViewById(R.id.oobe_action_btn);
        mSpeedActionButton.setOnClickListener(mActionButtonListener);
        mCurrentStateShowArea = (RelativeLayout) findViewById(R.id.current_state_show_area);
        mTestResultArea = (LinearLayout) findViewById(R.id.test_result_area);
        mAverageSpeedShowText = (TextView) findViewById(R.id.average_speed_text);
        mAverageBandwidthIndicator = (ProgressBar) findViewById(R.id.average_bandwidth_indicator);
        mBandwidthSuggestion = (TextView) findViewById(R.id.bandwidth_suggestion);
        mSuggestionText = (TextView) findViewById(R.id.suggestion_text);
        mCurrentProgressBar = (ProgressBar) findViewById(R.id.current_progress);

        LinearLayout speedIndicator = (LinearLayout) findViewById(R.id.speed_indicator);
        speedIndicator.setFocusable(true);
        speedIndicator.setFocusableInTouchMode(true);
        speedIndicator.requestFocus();
        speedIndicator.requestFocusFromTouch();
        int speedIndicatorChildCount = speedIndicator.getChildCount();
        mSpeedIndicatorTextArray = new TextView[speedIndicatorChildCount];

        for (int index = 0; index < speedIndicatorChildCount; ++index) {
            mSpeedIndicatorTextArray[index] = (TextView) speedIndicator.getChildAt(index);
        }

        mDashBoardPointer = new PointerView(this, 269F, 40F);
        mDashBoardPointer.setPointerImage(R.drawable.oobe_dashboard_pointer, 14F, 230F);
        mDashBoardPointer.setMaxDeflectionAngle(119F);
        mDashBoardPointer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mDashBoardPointer.setOnAnimationStopListener(mOnPointerStopListener);
        mGraphicHolder.addView(mDashBoardPointer);
        ImageView dashboardPointerOrb = new ImageView(this);
        dashboardPointerOrb.setImageResource(R.drawable.dashboard_pointer_orb);
        RelativeLayout.LayoutParams dashPointerOrbLayoutParams = new RelativeLayout.LayoutParams(57, 55);
        dashPointerOrbLayoutParams.leftMargin = (int) (mDashBoardPointer.getCenterXOffset() - 26);
        dashPointerOrbLayoutParams.topMargin = (int) (mDashBoardPointer.getCenterYOffset() - 24);
        dashPointerOrbLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.status_icon);
        dashPointerOrbLayoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.status_icon);
        dashboardPointerOrb.setLayoutParams(dashPointerOrbLayoutParams);
        mGraphicHolder.addView(dashboardPointerOrb);
    }


    protected void uploadSpeedTestResult() {
        float totalSpeed = 0;
        int count = 0;
        mCurrentSpeedTestResults = new ArrayList<SpeedInfoUploadEntity>();
        for (NetworkSpeedInfo n : mNetworkSpeedInfoList) {
            SpeedInfoUploadEntity entity = new SpeedInfoUploadEntity();
            entity.setPk(n.pk);
            entity.setSpeed(n.speed);
            mCurrentSpeedTestResults.add(entity);
            if (n.speed > 0 && n.display) {
                totalSpeed += n.speed;
                ++count;
            }
        }
        float averageSpeed = (float) ((int) (totalSpeed / (float) count * 100F)) / 100F;
        float fakeAverageSpeed = averageSpeed * 0.8f;
        if (isSetting) {
            showSpeedResults(fakeAverageSpeed, 41, 151, 831);
        } else {
            showSpeedResults(fakeAverageSpeed, 71, 193, 1109);
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<SpeedInfoUploadEntity>>() {
        }.getType();
        try {
            String json = gson.toJson(mCurrentSpeedTestResults, listType);
            String uploadStr = "{\"speed\":" + json + ", \"ip\":\"" + mFeedBackEntity.ip
                    + "\",\"location\":\"" + mFeedBackEntity.location + "\",\"isp\":\"" + mFeedBackEntity.isp + "\"}";
            new UploadTask().execute(uploadStr, domain + "/customer/speedlogs/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class UploadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return NetworkUtils.uploadString(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != "" && !result.trim().equals("OK")) {
                mSuggestionText.setText(result);
            } else if (result.trim().equals("OK")) {
            }
            super.onPostExecute(result);
        }

    }

    private void showSpeedResults(float averageSpeed, int bandwidth1m, int bandwidthRange, int bandwidthTotalRange) {
        mCurrentStateShowArea.setVisibility(View.INVISIBLE);
        mGraphicHolder.setVisibility(View.INVISIBLE);
        mTestResultArea.setVisibility(View.VISIBLE);

        float bandwidth = averageSpeed * 8F;
        int progress = 0;
        String tips = null;
        final String yourBandwidth = mResources.getString(R.string.your_bandwidth);
        final String is = mResources.getString(R.string.is);
        final String adsl1m = mResources.getString(R.string.adsl_1m);
        String between = mResources.getString(R.string.between);
        mAverageSpeedShowText.setVisibility(View.VISIBLE);
        float speedShow = averageSpeed > 1096 ? (float) ((int) ((averageSpeed / 1024F) * 10F)) / 10F : (float) ((int) (averageSpeed * 10F)) / 10F;
        String speedUnit = averageSpeed > 1096 ? mResources.getString(R.string.speed_unit_mb) : mResources.getString(R.string.speed_unit_kb);
        mAverageSpeedShowText.setText(mResources.getString(R.string.your_avg_speed_is) + " " + String.valueOf(speedShow) + speedUnit);
        if (bandwidth > 0 && bandwidth < 1024 + 100) {
            progress = (int) (bandwidth / 1024F * bandwidth1m);
            tips = yourBandwidth + is + adsl1m;
            if (bandwidth <= 512 + 50) {
                tips = yourBandwidth + is + "512K ADSL";
            } else if (bandwidth >= 512 + 50 && bandwidth < 1024 - 100) {
                tips = yourBandwidth + between.replace("#speed1#", "512K ADSL").replace("#speed2#", adsl1m);
            } else if (Math.abs(bandwidth - 1024) < 100) {
                tips = yourBandwidth + is + "1M ADSL";
            }
        } else if (bandwidth >= 1024 + 100 && bandwidth < 2048 + 200) {
            progress = (int) ((bandwidth - 1024) / (2048 - 1024) * bandwidthRange) + bandwidth1m;
            if (Math.abs(bandwidth - 2048) < 200) {
                tips = yourBandwidth + is + "2M ADSL";
            } else {
                tips = yourBandwidth + between.replace("#speed1#", "1M ADSL").replace("#speed2#", "2M ADSL");
            }
        } else if (bandwidth >= 2048 + 200 && bandwidth < 4096 + 400) {
            progress = (int) ((bandwidth - 2048) / (4096 - 2048) * bandwidthRange) + bandwidthRange + bandwidth1m;
            if (Math.abs(bandwidth - 4096) < 400) {
                tips = yourBandwidth + is + "4M ADSL";
            } else {
                tips = yourBandwidth + between.replace("#speed1#", "2M ADSL").replace("#speed2#", "4M ADSL");
            }
        } else if (bandwidth >= 4096 + 400 && bandwidth < 8192 + 400) {
            progress = (int) ((bandwidth - 4096) / (8192 - 4096) * bandwidthRange) + bandwidthRange * 2 + bandwidth1m;
            if (Math.abs(bandwidth - 8192) < 400) {
                tips = yourBandwidth + is + "8M ADSL";
            } else {
                tips = yourBandwidth + between.replace("#speed1#", "4M ADSL").replace("#speed2#", "8M ADSL");
            }
        } else if (bandwidth >= 8192 + 400 && bandwidth < 10240 + 500) {
            progress = (int) ((bandwidth - 8192) / (10240 - 8192) * bandwidthRange) + bandwidthRange * 3 + bandwidth1m;
            if (Math.abs(bandwidth - 10240) < 500) {
                tips = yourBandwidth + is + mResources.getString(R.string.adsl_10m);
            } else {
                tips = yourBandwidth + between.replace("#speed1#", "8M ADSL").replace("#speed2#", mResources.getString(R.string.adsl_10m));
            }
        } else if (bandwidth >= 10240 + 500 && bandwidth < 20480 + 600) {
            progress = (int) ((bandwidth - 10240) / (20480 - 10240) * bandwidthRange) + bandwidthRange * 4 + bandwidth1m;
            if (Math.abs(bandwidth - 20480) < 600) {
                tips = yourBandwidth + is + mResources.getString(R.string.adsl_20m);
            } else {
                tips = yourBandwidth + between.replace("#speed1#", mResources.getString(R.string.adsl_10m)).replace("#speed2#", mResources.getString(R.string.adsl_20m));
            }
        } else if (bandwidth >= 20480 + 600 && bandwidth < 102400 + 5000) {
            progress = (int) ((bandwidth - 20480) / (102400 - 20480) * bandwidthRange) + bandwidthRange * 5 + bandwidth1m;
            if (Math.abs(bandwidth - 102400) < 5000) {
                tips = yourBandwidth + is + mResources.getString(R.string.t1);
            } else {
                tips = yourBandwidth + between.replace("#speed1#", mResources.getString(R.string.adsl_20m)).replace("#speed2#", mResources.getString(R.string.t1));
            }
        } else if (bandwidth >= 102400 + 5000) {
            progress = bandwidthTotalRange;
            tips = yourBandwidth + is + mResources.getString(R.string.t1);
        } else if (bandwidth <= 0) {
            tips = mResources.getString(R.string.seems_failed);
            progress = 0;
        }
        mBandwidthSuggestion.setText(tips);
        setResultIndicatorProgress(progress, bandwidthTotalRange);
        Log.d("result_progress", String.valueOf(progress));
    }

    private void setResultIndicatorProgress(final int progress, int maxProgress) {
        mAverageBandwidthIndicator.setMax(maxProgress);
        mAverageBandwidthIndicator.setProgress(1);
        mUpdateProgressRunnable = new UpdateProgressRunnable(progress);
        mUpdateProgressHandler.post(mUpdateProgressRunnable);
    }

    Handler mUpdateProgressHandler = new Handler();

    Runnable mUpdateProgressRunnable = null;

    class UpdateProgressRunnable implements Runnable {
        private int mProgress;

        public UpdateProgressRunnable(int progress) {
            mProgress = progress;
        }

        public void run() {
            int currentProgress = mAverageBandwidthIndicator.getProgress();
            int maxProgress = mAverageBandwidthIndicator.getMax();
            float percent = (float) currentProgress / (float) maxProgress;
            int increase = (int) (20 - 20 * percent * percent);

            if (currentProgress >= mProgress) {
                mUpdateProgressHandler.removeCallbacks(mUpdateProgressRunnable);
            } else {
                if (currentProgress + increase > mProgress) {
                    mAverageBandwidthIndicator.setProgress(mProgress);
                } else {
                    mAverageBandwidthIndicator.setProgress(currentProgress + increase);
                }
                mUpdateProgressHandler.postDelayed(mUpdateProgressRunnable, 20);
            }
        }
    }

    protected void startToTest() {
        mTestState = TEST_STATE_TESTING;
        mCurrentNetworkSpeedInfo = mNetworkSpeedInfoList.get(mCurrentPosition);
        mCurrFakeNetWorkSpeedInfo = new FakeNetWorkSpeedInfo();
        mCurrentStateShowArea.setVisibility(View.VISIBLE);
//    	mAverageSpeedShowText.setVisibility(View.INVISIBLE);
        mTestResultArea.setVisibility(View.INVISIBLE);
        mGraphicHolder.setVisibility(View.VISIBLE);
//    	mStatusIndicatorText.setText(mResources.getString(R.string.testing));
        mCurrentProgressBar.setVisibility(View.VISIBLE);
        mCurrentProgressBar.setMax((int) mCurrentNetworkSpeedInfo.length);
        mCurrentProgressBar.setIndeterminate(true);
        String currentStateText = mResources.getString(R.string.numbers_of_total);
        currentStateText = currentStateText.replaceFirst("#", String.valueOf(mNetworkSpeedInfoList.size()));
        currentStateText = currentStateText.replaceFirst("@", String.valueOf(mCurrentPosition + 1));
//    	mCurrentStepNumber.setText(String.valueOf(mCurrentPosition+1));
        mCurrentStateText.setText(currentStateText);
        //clean the last result
        if (mDashBoardPointer.hasStopped()) {
            startDownload();
        } else {
            mDashBoardPointer.stopHanlde();
        }
    }


    private Handler mGetTestUrlHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                int result = msg.what;
                if (result == 1) {
                    //TODO:continue to run test.
                    if (mTestState == TEST_STATE_PENDING) {
                        if (parseJson()) {
                            mCurrentPosition = 0;
                            startToTest();
                        } else {
                            showDialog(DIALOG_NETWORK_EXCEPTION);
                            mTestState = TEST_STATE_IDLE;
                            //						mStatusIndicatorText.setText(R.string.test_stopped);
                            mSpeedActionButton.setText(R.string.button_label_retest);
                        }
                    }

                } else if (result == 2) {

                    mGetTestUrlRunnable.isCancelled = false;
                } else if (result == 0) {
                    isNetworkAvailable = false;
                    //show a info about network exception.
                    mTestState = TEST_STATE_IDLE;
                    try {
                        showDialog(DIALOG_NETWORK_EXCEPTION);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //				mStatusIndicatorText.setText(R.string.test_stopped);
                    mSpeedActionButton.setText(R.string.button_label_retest);
                }
            }
        }

    };

    class GetTestUrlRunnable implements Runnable {
        private HttpURLConnection conn = null;
        public boolean isCancelled = false;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            StringBuffer sb = new StringBuffer();
            BufferedReader bfReader = null;
            long s = System.currentTimeMillis();
            try {
                //TODO:This url needs to be supplied
                URL url = new URL(domain + "/customer/urls/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                InputStream is = conn.getInputStream();
                if (isCancelled) {
                    mGetTestUrlHandler.sendEmptyMessage(2);
                    return;
                }
                bfReader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = bfReader.readLine()) != null) {
                    if (isCancelled) {
                        mJsonStr = null;
                        mGetTestUrlHandler.sendEmptyMessage(2);
                        return;
                    }
                    sb.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mJsonStr = null;
                if (isCancelled) {
                    mGetTestUrlHandler.sendEmptyMessage(2);
                } else {
                    mGetTestUrlHandler.sendEmptyMessage(0);
                }
                return;
            } finally {
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (bfReader != null) {
                    try {
                        bfReader.close();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            mJsonStr = sb.toString();
            mGetTestUrlHandler.sendEmptyMessage(1);
            return;
        }
    }


    private boolean updateJsonStr() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo networkInfo : networkInfos) {
            if (networkInfo.isConnected()) {
                isNetworkAvailable = true;
                break;
            }
        }
        if (isNetworkAvailable) {
            //if network is established, then download the info about download file url.
            mGetTestUrlRunnable = new GetTestUrlRunnable();
            new Thread(mGetTestUrlRunnable).start();

            return true;
        } else {
            mTestState = TEST_STATE_IDLE;
            mSpeedActionButton.setText(R.string.button_label_retest);
            showDialog(DIALOG_NETWORK_UNESTABLISHED);
            return false;
        }
    }


    private boolean parseJson() {
        mNetworkSpeedInfoList = new ArrayList<NetworkSpeedInfo>();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<NetworkSpeedInfo>>() {
        }.getType();
        try {
            mNetworkSpeedInfoList = gson.fromJson(mJsonStr, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        for (NetworkSpeedInfo n : mNetworkSpeedInfoList) {
            Log.d("networkinfo", n.toString());
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mTestState = TEST_STATE_IDLE;
        mDownloadHandler.removeCallbacks(downloadFileTask);
        mTimingHandler.removeCallbacks(updateStatusTask);
        this.finish();
        super.onPause();
    }

    @Override
    protected void onResume() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo networkInfo : networkInfos) {
            if (networkInfo.isConnected()) {
                isNetworkAvailable = true;
                break;
            }
        }
        if (isNetworkAvailable) {
            if (mFeedBackEntity.ip == null) {
                new GetFeedBackInfo().execute();
            }
        } else {
            //else remind user to setup network settings. pop up a dialog, contains two button [Network Setting], [Cancel]
            //WIFI setting Activity: ACTION_WIFI_SETTINGS
            showDialog(DIALOG_NETWORK_UNESTABLISHED);
        }
        super.onResume();
    }

    class GetFeedBackInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection conn2 = null;
            BufferedReader bfReader2 = null;
            StringBuffer sb2 = new StringBuffer();
            try {
                URL url2 = new URL("http://lily.tvxio.com/iplookup");
                conn2 = (HttpURLConnection) url2.openConnection();
                conn2.setConnectTimeout(15000);
                conn2.setReadTimeout(15000);
                InputStream is2 = conn2.getInputStream();
                bfReader2 = new BufferedReader(new InputStreamReader(is2, "UTF-8"));
                String line = null;
                while ((line = bfReader2.readLine()) != null) {
                    sb2.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (conn2 != null) {
                    conn2.disconnect();
                }
                if (bfReader2 != null) {
                    try {
                        bfReader2.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            String feedBackstr = sb2.toString();
            Log.d("feedbackstr", feedBackstr);
            Gson gson = new Gson();
            LocationInfo locationInfo = new LocationInfo();
            try {
                locationInfo = gson.fromJson(feedBackstr, LocationInfo.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            if (locationInfo != null) {
                String ip = locationInfo.getIp();
                if (ip != null && !"".equals(ip)) {
                    mFeedBackEntity.ip = ip;
                } else {
                    mFeedBackEntity.ip = mResources.getString(R.string.default_ip);
                }
                String city = locationInfo.getCity();
                if (city != null && !"".equals(city)) {
                    mFeedBackEntity.location = city;
                } else {
                    mFeedBackEntity.location = mResources.getString(R.string.unknown_area);
                }
                String isp = locationInfo.getIsp();
                if (isp != null && !"".equals(isp)) {
                    mFeedBackEntity.isp = isp;
                } else {
                    mFeedBackEntity.isp = mResources.getString(R.string.unknown_isp);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

//			if(mFeedBackEntity.ip!=null && mFeedBackEntity.ip!=""){
//				mFeedBackIpText.setText(mFeedBackEntity.ip);
//			}
//			if(mFeedBackEntity.location!=null && mFeedBackEntity.location!=""){
//				mFeedBackCityText.setText(mFeedBackEntity.location);
//			}
//			if(mFeedBackEntity.isp!=null && mFeedBackEntity.isp!=""){
//				mFeedBackISPText.setText(mFeedBackEntity.isp);
//			}
            super.onPostExecute(result);
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        String quit = mResources.getString(R.string.quit);
        String back = mResources.getString(R.string.back);
        switch (id) {
            case DIALOG_NETWORK_EXCEPTION:
                String except_message = mResources.getString(R.string.msg_network_exception);
                CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
                customBuilder.setPositiveButton(quit, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        OobeActivity.this.finish();
                        dismissDialog(DIALOG_NETWORK_EXCEPTION);
                    }
                }).setNegativeButton(back, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dismissDialog(DIALOG_NETWORK_EXCEPTION);
                    }
                }).setMessage(except_message);
                dialog = customBuilder.create();
                break;
            case DIALOG_NETWORK_UNESTABLISHED:
                String unestablished_message = mResources.getString(R.string.msg_network_unestablished);
                CustomDialog.Builder customBuilder2 = new CustomDialog.Builder(this);
                customBuilder2.setMessage(unestablished_message).setNegativeButton(back, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dismissDialog(DIALOG_NETWORK_UNESTABLISHED);
                    }
                }).setPositiveButton(quit, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        OobeActivity.this.finish();
                        dismissDialog(DIALOG_NETWORK_UNESTABLISHED);
                    }
                }).setMessage(unestablished_message);
                dialog = customBuilder2.create();
                break;
            default:
                dialog = null;
                break;
        }
        return dialog;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (isSetting) {
            return false;
        } else {
            return validateKeyCode(keyCode);
        }
    }

    private boolean validateKeyCode(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_ENTER) {
            return false;
        } else {
            return true;
        }
    }

    private Runnable mExpiredTimer = new Runnable() {

        private int mTimeEscaped = 0;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (mTestState == TEST_STATE_IDLE) {
                if (mTimeEscaped >= mDisappearTime * 10) {
                    Intent newIntent = new Intent("lenovo.settings.action.finish");
                    OobeActivity.this.sendBroadcast(newIntent);
                    Log.d("Expired", "quit");
                    OobeActivity.this.finish();
                } else {
                    mTimeEscaped += 5;
                    mExpiredHandler.postDelayed(mExpiredTimer, 5000);
                }
            } else {
                mTimeEscaped = 0;
                mExpiredHandler.postDelayed(mExpiredTimer, 5000);
            }
        }
    };

    private Handler mExpiredHandler = new Handler();


    private OnClickListener mActionButtonListener = new OnClickListener() {

        public void onClick(View v) {
            switch (mTestState) {
                case TEST_STATE_IDLE:
                    mTestState = TEST_STATE_PENDING;
                    mSpeedActionButton.setText(R.string.button_label_stop);
                    if (mJsonStr != null) {

                        if (parseJson()) {
                            mCurrentPosition = 0;
                            mSuggestionText.setText("");
                            startToTest();
                        } else {
                            mTestState = TEST_STATE_IDLE;
                            mSpeedActionButton.setText(R.string.button_label_retest);
                            showDialog(DIALOG_NETWORK_EXCEPTION);
                        }
                    } else {
                        updateJsonStr();
                    }
                    break;
                case TEST_STATE_PENDING:
                    mGetTestUrlRunnable.isCancelled = true;
                    mSpeedActionButton.setText(R.string.button_label_retest);
                    mTestState = TEST_STATE_IDLE;
                    break;
                case TEST_STATE_TESTING:
                    mTimingHandler.removeCallbacks(updateStatusTask);
                    mDownloadHandler.removeCallbacks(mDownloaderThread);
                    mCurrentNetworkSpeedInfo.flagStop = 1;
                    mTestState = TEST_STATE_IDLE;
                    mCurrentProgressBar.setVisibility(View.INVISIBLE);
                    mCurrentStateShowArea.setVisibility(View.INVISIBLE);
                    mSpeedActionButton.setText(R.string.button_label_retest);
                    break;
            }

        }
    };


    private void updateSpeedIndicatorText(float speed) {
        float speedInKB = ((float) ((int) (speed * 100F)) / 100F);
        int numK = (int) speedInKB / 1000;
        mSpeedIndicatorTextArray[0].setText(String.valueOf(numK < 10 ? numK : 9));
        int numH = (int) ((speedInKB - numK * 1000) / 100);
        mSpeedIndicatorTextArray[1].setText(String.valueOf(numH));
        int numD = (int) ((speedInKB - numK * 1000 - numH * 100) / 10);
        mSpeedIndicatorTextArray[2].setText(String.valueOf(numD));
        int numN = (int) (speedInKB - numK * 1000 - numH * 100 - numD * 10);
        mSpeedIndicatorTextArray[3].setText(String.valueOf(numN));
        int numP1 = (int) ((float) (speedInKB - (float) ((int) speedInKB)) * 10F);
        mSpeedIndicatorTextArray[5].setText(String.valueOf(numP1));
        int numP2 = (int) ((speedInKB - ((float) ((int) (speedInKB * 10)) / 10F)) * 100F);
        mSpeedIndicatorTextArray[6].setText(String.valueOf(numP2));
    }

    private PointerView.OnAnimationStopListener mOnPointerStopListener = new PointerView.OnAnimationStopListener() {

        public void onStop() {
            if (mTestState == TEST_STATE_TESTING) {
                startDownload();
            }
        }

        public void onSpeedChange(float currentSpeed) {
            updateSpeedIndicatorText(currentSpeed);
        }
    };

    private void startDownload() {
        mDownloaderThread = new Thread(downloadFileTask);
        mDownloaderThread.start();
        mTimingHandler.post(updateStatusTask);
        mDashBoardPointer.startHandle();
    }

    private Runnable updateStatusTask = new Runnable() {
        private int counter = 0;

        public void run() {
            if (mCurrentNetworkSpeedInfo.timeStarted > 0) {
                if (mCurrentProgressBar.getVisibility() == View.INVISIBLE) {
                    mCurrentProgressBar.setVisibility(View.VISIBLE);
                }
                mCurrentProgressBar.setIndeterminate(false);
                if (mCurrentNetworkSpeedInfo.timeEscalpsed >= mCurrentNetworkSpeedInfo.length) {
                    counter = 0;
                    for (int index = 0; index < mSpeedIndicatorTextArray.length; ++index) {
                        if (index != 4) {
                            mSpeedIndicatorTextArray[index].setText("0");
                        }
                    }
                    mCurrentNetworkSpeedInfo.flagStop = 1;
                    if (mCurrentPosition < mNetworkSpeedInfoList.size() - 1) {
                        mCurrentPosition++;
                        mCurrentProgressBar.setProgress(0);
                        startToTest();
                    } else {
                        mTestState = TEST_STATE_IDLE;
                        mTimingHandler.removeCallbacks(updateStatusTask);
                        mSpeedActionButton.setText(R.string.button_label_retest);
                        uploadSpeedTestResult();
                    }
                    return;
                } else {
                    mCurrentNetworkSpeedInfo.timeEscalpsed = SystemClock.uptimeMillis() - mCurrentNetworkSpeedInfo.timeStarted;
                    mCurrentNetworkSpeedInfo.speed = (float) mCurrentNetworkSpeedInfo.filesizeFinished / (float) mCurrentNetworkSpeedInfo.timeEscalpsed * 1000.0F / 1024.0F;
                    mCurrFakeNetWorkSpeedInfo.setSpeed(mCurrentNetworkSpeedInfo.speed);
                    mCurrentProgressBar.setProgress((int) mCurrentNetworkSpeedInfo.timeEscalpsed);
                    updateSpeedIndicatorText(mCurrFakeNetWorkSpeedInfo.speed);
                    counter++;
                    if (counter % 2 == 0) {
                        float speed = mCurrFakeNetWorkSpeedInfo.speed < 2000 ? mCurrFakeNetWorkSpeedInfo.speed : 2000;
                        mDashBoardPointer.updatePointer(speed, 400);
                    }
                }
            }
            mTimingHandler.postDelayed(updateStatusTask, 200);
        }
    };

    private Runnable downloadFileTask = new Runnable() {

        public void run() {
            int returnValue = NetworkUtils.getFileFromUrl(mCurrentNetworkSpeedInfo);
            switch (returnValue) {
                case NETWORK_CONNECTION_NORMAL:
//				mCurrentProgressBar.setProgress((int)mCurrentNetworkSpeedInfo.length);
                    break;
                case NETWORK_CONNECTION_TIMEOUT:
                    mCurrentNetworkSpeedInfo.timeStarted = 1;
                    mCurrentNetworkSpeedInfo.timeEscalpsed = mCurrentNetworkSpeedInfo.length;
                    mCurrentNetworkSpeedInfo.speed = -1;
                    break;
                case NETWORK_CONNECTION_UNKNOWN:
                    mCurrentNetworkSpeedInfo.timeStarted = 1;
                    mCurrentNetworkSpeedInfo.timeEscalpsed = mCurrentNetworkSpeedInfo.length;
                    mCurrentNetworkSpeedInfo.speed = -2;
                    break;
            }
        }
    };
}
