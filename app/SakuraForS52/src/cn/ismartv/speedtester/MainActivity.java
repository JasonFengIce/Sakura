package cn.ismartv.speedtester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteController;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import cn.ismartv.speedtester.domain.EngKeyEntity;
import cn.ismartv.speedtester.domain.FakeNetworkSpeedInfo;
import cn.ismartv.speedtester.domain.FeedBackEntity;
import cn.ismartv.speedtester.domain.FeedBackProblemEntity;
import cn.ismartv.speedtester.domain.LocationInfo;
import cn.ismartv.speedtester.domain.NetworkSpeedInfo;
import cn.ismartv.speedtester.domain.SpeedInfoUploadEntity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity {
	
	private static final String APP_SHARED_NAME = "SpeedTest";
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

	private RemoteController mRemoteController;
	
	private LayoutInflater mInflater;
	
//	private TextView mCurrentStepNumber;
	private RelativeLayout mGraphicHolder;
	private TextView mCurrentStateText;
	private Button mSpeedActionButton;
	private ProgressBar mCurrentProgressBar;
	private Resources mResources;
//	private TextView mStatusIndicatorText;
//	private ImageView mStatusIcon;
	private TextView mAverageSpeedShowText;
	private ProgressBar mAverageBandwidthIndicator;
	private TextView mBandwidthSuggestion;

	private TextView mSuggestionText;
	private RadioGroup mProblemOptionsRadioGroup;
	
	private TextView[] mSpeedIndicatorTextArray;
	private PointerView mDashBoardPointer;
	
	private EditText mDetailsEditText;
	private EditText mEmailEditText;
	private EditText mPhoneNumEditText;
	private EditText mAddressEditText;
	private TextView mFeedBackIpText;
	private TextView mFeedBackCityText;
	private TextView mFeedBackISPText;
	private RadioGroup mFeedBackVerifyRadioGroup;
	private Button mSubmit;
	private Button mQuit;
	
	private TextView mSnShowTextView;
	/**
	 * Detail panel is a engineer mode panel show details
	 */
	private LinearLayout mDetailpanelLayout;
	private ListView mDetailPanelListView;
	private TextView mDetailPanelAvgSpeed;
	private DetailAdapter mDetailAdapter;
	
	private String mJsonStr = null;
	private ArrayList<NetworkSpeedInfo> mNetworkSpeedInfoList = null;
	private NetworkSpeedInfo mCurrentNetworkSpeedInfo = null;
	private FakeNetworkSpeedInfo mCurrFakeNetworkSpeedInfo = null;
	private ArrayList<SpeedInfoUploadEntity> mCurrentSpeedTestResults = null;
	private ArrayList<FeedBackProblemEntity> mFeedBackProblemEntities = null;
	private FeedBackEntity mFeedBackEntity= null;
	
	private int mTestState = TEST_STATE_IDLE;
	private int mCurrentPosition = 0;
	
//	private boolean isSetting = false;
	
	private boolean isNetworkAvailable = false;
	
	public static String domain = "http://iris.tvxio.com"; 
	
	
	
	private BroadcastReceiver mCloseReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Receiver", intent.getAction());
			MainActivity.this.finish();
		}
	};
	
	
	private OnClickListener mActionButtonListener = new OnClickListener() {
		
		public void onClick(View v) {
			switch(mTestState){
			case TEST_STATE_IDLE:
				mTestState=TEST_STATE_PENDING;
//				mStatusIndicatorText.setText(mResources.getString(R.string.starting));
				mSpeedActionButton.setText(R.string.button_label_stop);
				if(!mSpeedActionButton.isFocused()){
					mSpeedActionButton.requestFocus();
				}
				if(mJsonStr!=null){
					
					if(parseJson()){
						mCurrentPosition = 0;
						mSuggestionText.setText("");
						startToTest();
					} else {
						mTestState = TEST_STATE_IDLE;
//						mStatusIndicatorText.setText(R.string.test_stopped);
						mSpeedActionButton.setText(R.string.button_label_retest);
						showDialog(DIALOG_NETWORK_EXCEPTION);
					}
				} else {
					updateJsonStr();
				}
				break;
			case TEST_STATE_PENDING:
				mGetTestUrlRunnable.isCancelled=true;
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
//				mStatusIndicatorText.setText(R.string.test_stopped);
				mSpeedActionButton.setText(R.string.button_label_retest);
				break;
			}
			
		}
	};
	
	private void updateSpeedIndicatorText(float speed){
		float speedInKB = ((float)((int)(speed*100F))/100F);
		int numK = (int)speedInKB/1000;
		mSpeedIndicatorTextArray[0].setText(String.valueOf(numK<10?numK:9));
		int numH = (int)((speedInKB-numK*1000)/100);
		mSpeedIndicatorTextArray[1].setText(String.valueOf(numH));
		int numD = (int)((speedInKB-numK*1000-numH*100)/10);
		mSpeedIndicatorTextArray[2].setText(String.valueOf(numD));
		int numN = (int)(speedInKB-numK*1000-numH*100-numD*10);
		mSpeedIndicatorTextArray[3].setText(String.valueOf(numN));
		int numP1 = (int)((float)(speedInKB-(float)((int)speedInKB))*10F);
		mSpeedIndicatorTextArray[5].setText(String.valueOf(numP1));
		int numP2 = (int)((speedInKB - ((float)((int)(speedInKB*10)) / 10F))*100F);
		mSpeedIndicatorTextArray[6].setText(String.valueOf(numP2));
//		mSpeedIndicatorText.setText(String.valueOf(numK)+String.valueOf(numH)+String.valueOf(numD)+String.valueOf(numN)+"."+String.valueOf(numP1)+String.valueOf(numP2));
	}
	
	private PointerView.OnAnimationStopListener mOnPointerStopListener = new PointerView.OnAnimationStopListener() {
		
		public void onStop() {
	    	if(mTestState==TEST_STATE_TESTING){
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
			if(mCurrentNetworkSpeedInfo.timeStarted>0){
				if(mCurrentProgressBar.getVisibility()==View.INVISIBLE){
					mCurrentProgressBar.setVisibility(View.VISIBLE);
				}
				mCurrentProgressBar.setIndeterminate(false);
				if(mCurrentNetworkSpeedInfo.timeEscalpsed>=mCurrentNetworkSpeedInfo.length){
//					mDownloadHandler.removeCallbacks(downloadFileTask);
					counter = 0;
					mDashBoardPointer.stopHanlde();
//					mSpeedIndicatorText.setText("- - - -. - -");
					for(int index=0;index<mSpeedIndicatorTextArray.length;++index){
						if(index!=4){
							mSpeedIndicatorTextArray[index].setText("0");
						}
					}
					mCurrentNetworkSpeedInfo.flagStop = 1;
					if(mCurrentPosition<mNetworkSpeedInfoList.size()-1){
						mCurrentPosition++;
						mCurrentProgressBar.setProgress(0);
						startToTest();
					} else {
						mTestState = TEST_STATE_IDLE;
						mTimingHandler.removeCallbacks(updateStatusTask);
//						mStatusIndicatorText.setText(mResources.getString(R.string.test_completion));
						mSpeedActionButton.setText(R.string.button_label_retest);
						uploadSpeedTestResult();
//						mTimingHandler.removeCallbacks(updateStatusTask);
					}
					return;
				} else {
					mCurrentNetworkSpeedInfo.timeEscalpsed = SystemClock.uptimeMillis() - mCurrentNetworkSpeedInfo.timeStarted;
					mCurrentNetworkSpeedInfo.speed = (float)mCurrentNetworkSpeedInfo.filesizeFinished / (float)mCurrentNetworkSpeedInfo.timeEscalpsed * 1000.0F / 1024.0F;
					mCurrFakeNetworkSpeedInfo.setSpeed(mCurrentNetworkSpeedInfo.speed);
					mCurrentProgressBar.setProgress((int)mCurrentNetworkSpeedInfo.timeEscalpsed);
					updateSpeedIndicatorText(mCurrFakeNetworkSpeedInfo.speed);
					mDetailAdapter.notifyDataSetChanged();
					counter++;
					if(counter%2==0){
//						Log.d("counter", ""+counter);
						float speed = mCurrFakeNetworkSpeedInfo.speed < 2000?mCurrFakeNetworkSpeedInfo.speed:2000;
						mDashBoardPointer.updatePointer(speed, 400);
					}
				}
			}
			mTimingHandler.postDelayed(updateStatusTask, 200);
		}
	};
	
	private Runnable downloadFileTask = new Runnable(){
		
		public void run() {
			int returnValue = NetworkUtils.getFileFromUrl(mCurrentNetworkSpeedInfo);
			switch(returnValue){
			case NETWORK_CONNECTION_NORMAL:
//				mCurrentProgressBar.setProgress((int)mCurrentNetworkSpeedInfo.length);
				break;
			case NETWORK_CONNECTION_TIMEOUT:
				mCurrentNetworkSpeedInfo.timeStarted = 1;
				mCurrentNetworkSpeedInfo.timeEscalpsed = mCurrentNetworkSpeedInfo.length;
				mCurrentNetworkSpeedInfo.speed = -1;
//				try {
//					Thread.sleep(400);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}

				break;
			case NETWORK_CONNECTION_UNKNOWN:
				mCurrentNetworkSpeedInfo.timeStarted = 1;
				mCurrentNetworkSpeedInfo.timeEscalpsed = mCurrentNetworkSpeedInfo.length;
				mCurrentNetworkSpeedInfo.speed = -2;
//				try {
//					Thread.sleep(400);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}

				break;
			}
		}
		
	};
	private RelativeLayout mCurrentStateShowArea;
	private InputMethodManager mImeManager;
	private LinearLayout mTestResultArea;
	private Button mResetButton;
	private RelativeLayout mFeedBackArea;
	private LinearLayout mLeftSide;
	private TextView mCSPhoneValue;
	private TextView mCSEmailValue;
	private TextView mCSWeiboValue;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mResources = getResources();
        
//        Intent incomingIntent = getIntent();
//        String incomingAction = incomingIntent.getAction();
//        if("cn.ismartv.speedtester.settings".equals(incomingAction)){
//        	isSetting = true;
//        }
        
        mLeftSide = (LinearLayout)findViewById(R.id.left_side);

        /*
         * Detail panel is an engineering mode panel;
         */
        mDetailpanelLayout = (LinearLayout)findViewById(R.id.detail_panel);
    	mDetailPanelListView = (ListView) mDetailpanelLayout.findViewById(R.id.eng_test_info_area);
    	mDetailPanelAvgSpeed = (TextView) mDetailpanelLayout.findViewById(R.id.eng_average_speed);
    	mDetailpanelLayout.setVisibility(View.INVISIBLE);
    	
//        mCurrentStepNumber = (TextView)findViewById(R.id.current_step_number);
        mCurrentStateText = (TextView)findViewById(R.id.current_state_text);
        mGraphicHolder = (RelativeLayout)findViewById(R.id.graphic_holder); 
        
        mSpeedActionButton = (Button)findViewById(R.id.speed_actionbutton);
        mSpeedActionButton.setOnClickListener(mActionButtonListener);
        mSpeedActionButton.setOnKeyListener(mButtonOnKeyListener);
        mSpeedActionButton.setOnFocusChangeListener(mFeedBackFocusListener);
        
        LinearLayout speedIndicator = (LinearLayout)findViewById(R.id.speed_indicator);
        int speedIndicatorChildCount = speedIndicator.getChildCount();
        mSpeedIndicatorTextArray = new TextView[speedIndicatorChildCount];
        for(int index=0;index<speedIndicatorChildCount;++index){
        	mSpeedIndicatorTextArray[index] = (TextView) speedIndicator.getChildAt(index);
        }
        
        mCurrentStateShowArea = (RelativeLayout)findViewById(R.id.current_state_show_area);
        mTestResultArea = (LinearLayout)findViewById(R.id.test_result_area);
        
        mResetButton = (Button)findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(mActionButtonListener);
        mResetButton.setOnFocusChangeListener(mFeedBackFocusListener);
        
        mAverageSpeedShowText = (TextView)findViewById(R.id.average_speed_text);
        mAverageBandwidthIndicator = (ProgressBar)findViewById(R.id.average_bandwidth_indicator);
        mBandwidthSuggestion = (TextView)findViewById(R.id.bandwidth_suggestion);
        mSuggestionText = (TextView)findViewById(R.id.suggestion_text);
//        if(isSetting){
//        	mSuggestionText.setVisibility(View.INVISIBLE);
//        }
        mCurrentProgressBar = (ProgressBar)findViewById(R.id.current_progress);
        mProblemOptionsRadioGroup = (RadioGroup)findViewById(R.id.problem_options);
        //set onFocusChangeListener to every radiobuttons
        for(int i=0;i<mProblemOptionsRadioGroup.getChildCount();++i){
        	mProblemOptionsRadioGroup.getChildAt(i).setOnFocusChangeListener(mOnFocusChangeListener);
        }
        
        mFeedBackArea = (RelativeLayout)findViewById(R.id.feedback_area);
        
        mDetailsEditText = (EditText)findViewById(R.id.edit_details);
        mEmailEditText = (EditText)findViewById(R.id.edit_email);
        mPhoneNumEditText = (EditText)findViewById(R.id.edit_phonenum);
        mAddressEditText = (EditText)findViewById(R.id.edit_address);
        
        mDetailsEditText.setOnEditorActionListener(mEditActionListener);
        mEmailEditText.setOnEditorActionListener(mEditActionListener);
        mPhoneNumEditText.setOnEditorActionListener(mEditActionListener);
        mAddressEditText.setOnEditorActionListener(mEditActionListener);
        
        mFeedBackIpText = (TextView)findViewById(R.id.feedback_ip);
        mFeedBackCityText = (TextView)findViewById(R.id.feedback_city);
        mFeedBackISPText = (TextView)findViewById(R.id.feedback_isp);
        mFeedBackVerifyRadioGroup = (RadioGroup)findViewById(R.id.feedback_verify_options);
        mFeedBackVerifyRadioGroup.getChildAt(0).setOnFocusChangeListener(mOnFocusChangeListener);
        mFeedBackVerifyRadioGroup.getChildAt(1).setOnFocusChangeListener(mOnFocusChangeListener);
        
        mSubmit = (Button)findViewById(R.id.button_submit);
        mQuit = (Button)findViewById(R.id.button_quit);
        mSubmit.setOnClickListener(mSubmitOnclickListener);
        mQuit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				MainActivity.this.finish();
				
			}
		});
        mSnShowTextView = (TextView)findViewById(R.id.sn_show);
        mSnShowTextView.setText("SN: "+android.os.Build.SERIAL);
        
        mCSPhoneValue = (TextView)findViewById(R.id.cs_phone_value);
        mCSEmailValue = (TextView)findViewById(R.id.cs_email_value);
        mCSWeiboValue = (TextView)findViewById(R.id.cs_weibo_value);
        mCSWeiboValue.setText("@"+mCSWeiboValue.getText());
        
        mInflater = LayoutInflater.from(MainActivity.this);
        
        mFeedBackEntity = new FeedBackEntity();
        
        mDashBoardPointer = new PointerView(this, 371F, 158F);
        mDashBoardPointer.setPointerImage(R.drawable.dashboard_pointer, 15F, 268F);
        mDashBoardPointer.setMaxDeflectionAngle(119F);
        mDashBoardPointer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mDashBoardPointer.setOnAnimationStopListener(mOnPointerStopListener);
        mGraphicHolder.addView(mDashBoardPointer);
        
        ImageView dashboardPointerOrb = new ImageView(this);
        dashboardPointerOrb.setImageResource(R.drawable.dashboard_pointer_orb);
        RelativeLayout.LayoutParams dashPointerOrbLayoutParams = new RelativeLayout.LayoutParams(57, 55);
        dashPointerOrbLayoutParams.leftMargin = (int) (mDashBoardPointer.getCenterXOffset()-26);
        dashPointerOrbLayoutParams.topMargin = (int) (mDashBoardPointer.getCenterYOffset()-24);
        dashPointerOrbLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.status_icon);
        dashPointerOrbLayoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.status_icon);
        dashboardPointerOrb.setLayoutParams(dashPointerOrbLayoutParams);
        mGraphicHolder.addView(dashboardPointerOrb);
        
        registerReceiver(mCloseReceiver, new IntentFilter(ACTION_LAUNCHER));
        registerReceiver(mCloseReceiver, new IntentFilter(ACTION_SETTING));
        mImeManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        getInfo();
    }
    
    private OnFocusChangeListener mFeedBackFocusListener = new OnFocusChangeListener() {
		
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				mFeedBackArea.setBackgroundResource(R.drawable.feedback_bg1);
			} else {
				mFeedBackArea.setBackgroundResource(R.drawable.feedback_bg2);
			}
		}
	};
    
    private OnEditorActionListener mEditActionListener = new OnEditorActionListener() {
		
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId==0){
				mImeManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
				return true;
			} else {
				return false;
			}
		}
	};
    private OnKeyListener mButtonOnKeyListener = new OnKeyListener() {
		
		public boolean onKey(View v, int keyCode, KeyEvent event) {
//			Log.d("OnKey", v.toString()+"  Code: "+keyCode+" event:"+event.toString());
			if(v==mSpeedActionButton){
				int action = event.getAction();
				int keycode = event.getKeyCode();
				if(action==KeyEvent.ACTION_UP && EngKeyEntity.checkUseful(keycode)){
					if(EngKeyEntity.pressOneKey(keycode)){
						toggleDetailPanel();
					}
					return true;
				} else {
					return false;
				}
			}
			return false;
		}
	};
    
	private void toggleDetailPanel(){
		if(mDetailpanelLayout.getVisibility()==View.VISIBLE){
			TranslateAnimation flyAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, -1, TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0);
			flyAnimation.setDuration(300);
			flyAnimation.setFillBefore(true);
			mDetailpanelLayout.startAnimation(flyAnimation);
			mDetailpanelLayout.setVisibility(View.INVISIBLE);
		} else {
			mDetailpanelLayout.setVisibility(View.VISIBLE);
			TranslateAnimation flyAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, -1, TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0);
			flyAnimation.setDuration(300);
			flyAnimation.setFillAfter(true);
			mDetailpanelLayout.startAnimation(flyAnimation);
		}
	}
	
    protected void uploadSpeedTestResult() {
    	float totalSpeed = 0;
    	int count = 0;
    	mCurrentSpeedTestResults = new ArrayList<SpeedInfoUploadEntity>();
    	for(NetworkSpeedInfo n:mNetworkSpeedInfoList){
    		SpeedInfoUploadEntity entity = new SpeedInfoUploadEntity();
    		entity.setPk(n.pk);
    		entity.setSpeed(n.speed);
    		mCurrentSpeedTestResults.add(entity);
    		if(n.speed>0 && n.display){
    			totalSpeed +=n.speed;
    			++count;
    		}
    	}
    	float averageSpeed = (float)((int)(totalSpeed/(float)count * 100F))/100F;
//    	mCurrentStateShowArea.setVisibility(View.INVISIBLE);
    	
    	/*
    	 * according to averageSpeed give advice to user. show a bandwidth indicator and tip text.
    	 */
    	float fakeAverageSpeed = averageSpeed * 0.8f;
    	showSpeedResults(fakeAverageSpeed);
    
    	mDetailPanelAvgSpeed.setText(mResources.getString(R.string.your_avg_speed_is)+" "+String.valueOf(fakeAverageSpeed)+"KB/s");
    	Gson gson = new Gson();
    	Type listType = new TypeToken<List<SpeedInfoUploadEntity>>(){}.getType();
    	try {
			String json = gson.toJson(mCurrentSpeedTestResults, listType);
			String uploadStr = "{\"speed\":"+json+", \"ip\":\""+mFeedBackEntity.ip
					+"\",\"location\":\""+mFeedBackEntity.location+"\",\"isp\":\""+mFeedBackEntity.isp+"\"}";
			new UploadTask().execute(uploadStr, domain+"/customer/speedlogs/");
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
    
    class UploadTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			return NetworkUtils.uploadString(params[0], params[1]);
		}

		@Override
		protected void onPostExecute(String result) {
			if(result!="" && !result.trim().equals("OK")){
				mSuggestionText.setText(result);
			} else if(result.trim().equals("OK")) {
//				View layout = mInflater.inflate(R.layout.submit_toast, (ViewGroup)findViewById(R.id.toast_layout_root));
//				Toast toast = new Toast(MainActivity.this);
//				toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, -100);
//				toast.setDuration(Toast.LENGTH_LONG);
//				toast.setView(layout);
//				toast.show();
			}
			super.onPostExecute(result);
		}
    	
    }

    private void showSpeedResults(float averageSpeed){
    	mCurrentStateShowArea.setVisibility(View.INVISIBLE);
    	mGraphicHolder.setVisibility(View.INVISIBLE);
    	mTestResultArea.setVisibility(View.VISIBLE);
    	mResetButton.requestFocus();
    	/*
    	 * These constant value is based on the progress bar picture dimension.
    	 */
    	final int bandwidth1m = 41;
    	final int bandwidthRange = 151;
    	final int bandwidthTotalRange = 831;
    	float bandwidth = averageSpeed * 8F;
    	int progress = 0;
    	String tips = null;
    	final String yourBandwidth = mResources.getString(R.string.your_bandwidth);
    	final String is =mResources.getString(R.string.is);
    	final String adsl1m = mResources.getString(R.string.modem);
    	String between = mResources.getString(R.string.between);
    	mAverageSpeedShowText.setVisibility(View.VISIBLE);
    	float speedShow = averageSpeed>1096?(float)((int)((averageSpeed/1024F)*10F))/10F:(float)((int)(averageSpeed*10F))/10F;
    	String speedUnit = averageSpeed>1096?mResources.getString(R.string.speed_unit_mb):mResources.getString(R.string.speed_unit_kb);
    	mAverageSpeedShowText.setText(mResources.getString(R.string.your_avg_speed_is)+" "+String.valueOf(speedShow)+speedUnit);
    	if(bandwidth>0 && bandwidth<1024+100) {
    		progress = (int)(bandwidth/1024F * bandwidth1m);
    		tips = yourBandwidth+is+adsl1m;
    		if(bandwidth<=512+50){
    			tips = yourBandwidth+is+"512K ADSL";
    		} else if(bandwidth>=512+50 && bandwidth<1024-100){
    			tips = yourBandwidth+between.replace("#speed1#", "512K ADSL").replace("#speed2#", adsl1m);
    		} else if(Math.abs(bandwidth-1024)<=100){
    			tips = yourBandwidth+is+"1M ADSL";
    		}
    	} else if(bandwidth>=1024+100 && bandwidth<2048+200) {
    		progress = (int)((bandwidth-1024)/(2048-1024) * bandwidthRange) + bandwidth1m;
    		if(Math.abs(bandwidth-2048)<200){
    			tips = yourBandwidth+is+"2M ADSL";
    		} else {
    			tips = yourBandwidth+between.replace("#speed1#", "1M ADSL").replace("#speed2#", "2M ADSL");
    		}
    	} else if(bandwidth>=2048+200 && bandwidth<4096+400) {
    		progress = (int)((bandwidth-2048)/(4096-2048) * bandwidthRange) + bandwidthRange + bandwidth1m;
    		if(Math.abs(bandwidth-4096)<400){
    			tips = yourBandwidth+is+"4M ADSL";
    		} else {
    			tips = yourBandwidth+between.replace("#speed1#", "2M ADSL").replace("#speed2#", "4M ADSL");
    		}
    	} else if(bandwidth>=4096+400 && bandwidth<8192+400) {
    		progress = (int)((bandwidth-4096)/(8192-4096) * bandwidthRange) + bandwidthRange * 2 + bandwidth1m;
    		if(Math.abs(bandwidth-8192)<400){
    			tips = yourBandwidth+is+"8M ADSL";
    		} else {
    			tips = yourBandwidth+between.replace("#speed1#", "4M ADSL").replace("#speed2#", "8M ADSL");
    		}
    	} else if(bandwidth>=8192+400 && bandwidth<10240+500){
    		progress = (int)((bandwidth-8192)/(10240-8192) * bandwidthRange) + bandwidthRange * 3 + bandwidth1m;
    		if(Math.abs(bandwidth-10240)<500){
    			tips = yourBandwidth+is+mResources.getString(R.string.adsl_10m);
    		} else {
    			tips = yourBandwidth+between.replace("#speed1#", "8M ADSL").replace("#speed2#", mResources.getString(R.string.adsl_10m));
    		}
    	} else if(bandwidth>=10240+500 && bandwidth<20480+600){
    		progress = (int)((bandwidth-10240)/(20480-10240) * bandwidthRange) + bandwidthRange * 4 + bandwidth1m;
    		if(Math.abs(bandwidth-20480)<600){
    			tips = yourBandwidth+is+mResources.getString(R.string.adsl_20m);
    		} else {
    			tips = yourBandwidth+between.replace("#speed1#", mResources.getString(R.string.adsl_10m)).replace("#speed2#", mResources.getString(R.string.adsl_20m));
    		}
    	} else if(bandwidth>=20480+600 && bandwidth < 102400+5000) {
    		progress = (int)((bandwidth-20480)/(102400-20480) * bandwidthRange) + bandwidthRange * 5 + bandwidth1m;
    		if(Math.abs(bandwidth-102400)<5000) {
    			tips = yourBandwidth+is+mResources.getString(R.string.t1);
    		} else {
    			tips = yourBandwidth+between.replace("#speed1#", mResources.getString(R.string.adsl_20m)).replace("#speed2#", mResources.getString(R.string.t1));
    		}
    	} else if(bandwidth>=102400+5000) {
    		progress = bandwidthTotalRange;
    		tips = yourBandwidth+is+mResources.getString(R.string.t1);
    	} else if(bandwidth<=0){
    		tips = mResources.getString(R.string.seems_failed);
    		progress = 0;
    	}
    	mBandwidthSuggestion.setText(tips);
    	setResultIndicatorProgress(progress, bandwidthTotalRange);
    	Log.d("result_progress", String.valueOf(progress));
    }
    
    private void setResultIndicatorProgress(final int progress, int maxProgress){
    	mAverageBandwidthIndicator.setMax(maxProgress);
    	mAverageBandwidthIndicator.setProgress(1);
    	mUpdateProgressRunnable = new UpdateProgressRunnable(progress);
    	mUpdateProgressHandler.post(mUpdateProgressRunnable);
    }
    Handler mUpdateProgressHandler = new Handler();
	
    Runnable mUpdateProgressRunnable = null;
    class UpdateProgressRunnable implements Runnable{
    	private int mProgress;
		public UpdateProgressRunnable(int progress){
    		mProgress = progress;
		}
		public void run() {
			int currentProgress = mAverageBandwidthIndicator.getProgress();
			int maxProgress = mAverageBandwidthIndicator.getMax();
			float percent = (float)currentProgress / (float)maxProgress;
			int increase = (int) (15-15*percent*percent);
			if(currentProgress>=mProgress){
				mUpdateProgressHandler.removeCallbacks(mUpdateProgressRunnable);
			} else {
				if(currentProgress+increase>mProgress){
					mAverageBandwidthIndicator.setProgress(mProgress);					
				} else {
					mAverageBandwidthIndicator.setProgress(currentProgress+increase);
				}
				mUpdateProgressHandler.postDelayed(mUpdateProgressRunnable, 20);
			}
		}
	}
    protected void startToTest() {
    	mTestState = TEST_STATE_TESTING;
    	mCurrentNetworkSpeedInfo = mNetworkSpeedInfoList.get(mCurrentPosition);
    	mCurrFakeNetworkSpeedInfo = new FakeNetworkSpeedInfo();
    	mCurrentStateShowArea.setVisibility(View.VISIBLE);
//    	mAverageSpeedShowText.setVisibility(View.INVISIBLE);
    	mTestResultArea.setVisibility(View.INVISIBLE);
    	mGraphicHolder.setVisibility(View.VISIBLE);
//    	mStatusIndicatorText.setText(mResources.getString(R.string.testing));
    	mCurrentProgressBar.setVisibility(View.VISIBLE);
    	mCurrentProgressBar.setMax((int)mCurrentNetworkSpeedInfo.length);
    	mCurrentProgressBar.setIndeterminate(true);
    	String currentStateText = mResources.getString(R.string.numbers_of_total);
    	currentStateText = currentStateText.replaceFirst("#", String.valueOf(mNetworkSpeedInfoList.size()));
    	currentStateText = currentStateText.replaceFirst("@", String.valueOf(mCurrentPosition+1));
//    	mCurrentStepNumber.setText(String.valueOf(mCurrentPosition+1));
    	mCurrentStateText.setText(currentStateText);
    	//clean the last result
    	mDetailPanelAvgSpeed.setText(" ");
    	
    	if(mDashBoardPointer.hasStopped()){
    		startDownload();
    	} else {
    		mDashBoardPointer.stopHanlde();
    	}
	}
    
    private Handler mGetTestUrlHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(msg!=null){
				int result = msg.what;
				if(result==1){
					if(mTestState==TEST_STATE_PENDING){
						if(parseJson()){
							mCurrentPosition = 0;
							startToTest();
						} else {
							showDialog(DIALOG_NETWORK_EXCEPTION);
							mTestState = TEST_STATE_IDLE;
	//						mStatusIndicatorText.setText(R.string.test_stopped);
							mSpeedActionButton.setText(R.string.button_label_retest);
						}
					}
					
				} else if(result == 2) {

					mGetTestUrlRunnable.isCancelled = false;
				} else if(result == 0 ) {
					isNetworkAvailable = false;
					//show a info about network exception.
					mTestState = TEST_STATE_IDLE;
					try {
						showDialog(DIALOG_NETWORK_EXCEPTION);
					} catch (Exception e) {
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
			StringBuffer sb = new StringBuffer();
			BufferedReader bfReader = null;
			long s = System.currentTimeMillis();
			try {
				URL url = new URL(domain+"/customer/urls/");
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(15000);
				conn.setReadTimeout(15000);
				InputStream is = conn.getInputStream();
				if(isCancelled){
					mGetTestUrlHandler.sendEmptyMessage(2);
					return;
				}
				bfReader = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while((line=bfReader.readLine())!=null){
					if(isCancelled){
						mJsonStr = null;
						mGetTestUrlHandler.sendEmptyMessage(2);
						return;
					}
					sb.append(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
				mJsonStr = null;
				if(isCancelled){
					mGetTestUrlHandler.sendEmptyMessage(2);
				} else {
					mGetTestUrlHandler.sendEmptyMessage(0);
				}
				return;
			} finally {
				if(conn!=null){
					try {
						conn.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(bfReader!=null){
					try {
						bfReader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			mJsonStr = sb.toString();
			mGetTestUrlHandler.sendEmptyMessage(1);
			return;
		}
	}
    
    private boolean updateJsonStr(){
    	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo networkInfo : networkInfos){
        	if(networkInfo.isConnected()){
        		isNetworkAvailable = true;
        		break;
        	}
        }
        if(isNetworkAvailable){
        	//if network is established, then download the info about download file url.
            mGetTestUrlRunnable = new GetTestUrlRunnable();
        	new Thread(mGetTestUrlRunnable).start();
        	
        	return true;
        } else {
        	//else remind user to setup network settings. pop up a dialog, contains two button [Network Setting], [Cancel]
        	//WIFI setting Activity: ACTION_WIFI_SETTINGS
        	mTestState = TEST_STATE_IDLE;
        	mSpeedActionButton.setText(R.string.button_label_retest);
//			mStatusIndicatorText.setText(R.string.test_stopped);
        	showDialog(DIALOG_NETWORK_UNESTABLISHED);
        	return false;
        }
    }
    
    private void buildDetailPanel(){
    	mDetailAdapter = new DetailAdapter(this, mNetworkSpeedInfoList);
    	mDetailPanelListView.setAdapter(mDetailAdapter);
    }
    
    private boolean parseJson(){
    	mNetworkSpeedInfoList = new ArrayList<NetworkSpeedInfo>();
    	Gson gson = new Gson();
    	Type listType = new TypeToken<List<NetworkSpeedInfo>>(){}.getType();
    	try {
			mNetworkSpeedInfoList = gson.fromJson(mJsonStr, listType);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    	for(NetworkSpeedInfo n:mNetworkSpeedInfoList){
    		Log.d("networkinfo", n.toString());
    	}
    	buildDetailPanel();
    	return true;
    }
    
    private void saveInfo() {
    	SharedPreferences.Editor editor = getSharedPreferences(APP_SHARED_NAME,MODE_PRIVATE).edit();
    	editor.putString("user_email", mEmailEditText.getText().toString())
    	.putString("user_phone", mPhoneNumEditText.getText().toString())
    	.putString("user_address", mAddressEditText.getText().toString()).commit();
    }
    
    private void getInfo() {
    	SharedPreferences sp = getSharedPreferences(APP_SHARED_NAME, MODE_PRIVATE);
    	String userEmail = sp.getString("user_email", null);
    	String userPhone = sp.getString("user_phone", null);
    	String userAddress = sp.getString("user_address", null);
    	if(TextUtils.isEmpty(mEmailEditText.getText()) && !TextUtils.isEmpty(userEmail)) {
    		mEmailEditText.setText(userEmail);
    	}
    	if(TextUtils.isEmpty(mPhoneNumEditText.getText()) && !TextUtils.isEmpty(userPhone)) {
    		mPhoneNumEditText.setText(userPhone);
    	}
    	if(TextUtils.isEmpty(mAddressEditText.getText()) && !TextUtils.isEmpty(userAddress)) {
    		mAddressEditText.setText(userAddress);
    	}
    }

	@Override
	protected void onDestroy() {
		unregisterReceiver(mCloseReceiver);
		Log.d("UI","Destroyed");
		System.exit(0);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		saveInfo();
		hideCursor(false);
		mTestState = TEST_STATE_IDLE;
		mDownloadHandler.removeCallbacks(downloadFileTask);
		mTimingHandler.removeCallbacks(updateStatusTask);
		this.finish();
		Log.d("UI","Paused");
		super.onPause();
	}

	@Override
	protected void onResume() {
		hideCursor(true);
		Log.d("UI","Resumed");
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo networkInfo : networkInfos){
        	if(networkInfo.isConnected()){
        		isNetworkAvailable = true;
        		break;
        	}
        }
        if(isNetworkAvailable){
			if(mFeedBackProblemEntities == null){
				new GetFeedBackInfo().execute(1);
			}
			if(mFeedBackEntity.ip==null){
				new GetFeedBackInfo().execute(2);
			}
		} else {
			//else remind user to setup network settings. pop up a dialog, contains two button [Network Setting], [Cancel]
        	//WIFI setting Activity: ACTION_WIFI_SETTINGS
        	showDialog(DIALOG_NETWORK_UNESTABLISHED);
		}
//        
//        if(isSetting && mFeedBackArea.getVisibility()!=View.GONE){
//        	mFeedBackArea.setVisibility(View.GONE);
//        	LinearLayout parentLayout = (LinearLayout)mLeftSide.getParent();
//        	parentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
//        }
        
		super.onResume();
	}
	
	public void hideCursor(boolean hide){
		if(mRemoteController==null){
			mRemoteController = (RemoteController)getSystemService(Context.REMOTECONTROLLER_SERVICE);
		}
		if(hide){
			mRemoteController.setRcGestureOnly();
			mRemoteController.displayCursor(false);
		} else {
			mRemoteController.setDefaultMode();
		}
	}
	
	class GetFeedBackInfo extends AsyncTask<Integer, Void, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			if(params[0]==1){
				HttpURLConnection conn = null;
				StringBuffer sb = new StringBuffer();
				BufferedReader bfReader = null;
				try {
					URL url = new URL(domain+"/customer/points/");
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(15000);
					String localeName = Locale.getDefault().toString();
					conn.setRequestProperty("Accept-Language", localeName);
					conn.setReadTimeout(15000);
					InputStream is = conn.getInputStream();
					bfReader = new BufferedReader(new InputStreamReader(is));
					String line = null;
					while((line=bfReader.readLine())!=null){
						sb.append(line);
					}
					Gson gson = new Gson();
					Type listType = new TypeToken<List<FeedBackProblemEntity>>(){}.getType();
					mFeedBackProblemEntities = gson.fromJson(sb.toString(), listType);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(conn!=null){
						try {
							conn.disconnect();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(bfReader!=null){
						try {
							bfReader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else if(params[0]==2){
				HttpURLConnection conn2 = null;
				BufferedReader bfReader2 = null;
				StringBuffer sb2 = new StringBuffer();
				try {
					URL url2 = new URL("http://lily.tvxio.com/iplookup");
					conn2 = (HttpURLConnection) url2.openConnection();
					conn2.setConnectTimeout(15000);
					conn2.setReadTimeout(15000);
					InputStream is2 = conn2.getInputStream();
					bfReader2 = new BufferedReader(new InputStreamReader(is2,"UTF-8"));
					String line = null;
					while((line=bfReader2.readLine())!=null){
						sb2.append(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
					
				} finally {
					if(conn2!=null){
						conn2.disconnect();
					}
					if(bfReader2!=null){
						try {
							bfReader2.close();
						} catch (IOException e) {
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
				if(locationInfo!=null){
					String ip = locationInfo.getIp();
					if(ip!=null && !"".equals(ip)){
						mFeedBackEntity.ip = ip;
					} else {
						mFeedBackEntity.ip = mResources.getString(R.string.default_ip);
					}
					String city = locationInfo.getCity();
					if(city!=null && !"".equals(city)){
						mFeedBackEntity.city = city;
					} else {
						mFeedBackEntity.city = mResources.getString(R.string.unknown_area);
					}
					String isp = locationInfo.getIsp();
					if(isp!=null && !"".equals(isp)){
						mFeedBackEntity.isp = isp;
					} else {
						mFeedBackEntity.isp = mResources.getString(R.string.unknown_isp);
					}
				}
			}
			return params[0];
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result==1){
				if(mFeedBackProblemEntities!=null){
					mProblemOptionsRadioGroup.removeAllViews();
					int i=0;
					int max = mFeedBackProblemEntities.size()-1;
					for(FeedBackProblemEntity entity:mFeedBackProblemEntities){
						RadioButton radioButton = new RadioButton(MainActivity.this);
						LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						layoutParams.setMargins(5, 0, 10, 0);
						radioButton.setTextSize(35);
						radioButton.setText(entity.point_name+"  ");
						radioButton.setId(entity.point_id);
						radioButton.setOnFocusChangeListener(mOnFocusChangeListener);
						radioButton.setLayoutParams(layoutParams);
						mProblemOptionsRadioGroup.addView(radioButton);
						if(i==max){
							radioButton.setChecked(true);
//							mProblemOptionsRadioGroup.check(entity.point_id);
							mFeedBackEntity.option = entity.point_id;
						}
						++i;
					}
				} else {
					mFeedBackEntity.option = 4;
				}
				mProblemOptionsRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} else if(result==2){
				if(mFeedBackEntity.ip!=null && mFeedBackEntity.ip!=""){
					mFeedBackIpText.setText(mFeedBackEntity.ip);
				}
				if(mFeedBackEntity.city!=null && mFeedBackEntity.city!=""){
					mFeedBackCityText.setText(mFeedBackEntity.city);
					if(TextUtils.isEmpty(mAddressEditText.getText())) {
						mAddressEditText.setText(mFeedBackEntity.city);
					}
				}
				if(mFeedBackEntity.isp!=null && mFeedBackEntity.isp!=""){
					mFeedBackISPText.setText(mFeedBackEntity.isp);
				}
			}
			super.onPostExecute(result);
		}
	};
	
	public OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				((RadioButton)v).setTextColor(0xFF55E1FF);
			} else {
				((RadioButton)v).setTextColor(0xFFFFFFFF);
			}
		}
	};
	
	public OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		
		public void onCheckedChanged(RadioGroup group, int checkedId) {
//			if (checkedId == R.id.problem_unclear) {
//				mFeedBackEntity.option = 1;
//			} else if (checkedId == R.id.problem_block) {
//				mFeedBackEntity.option = 2;
//			} else if (checkedId == R.id.unable_play) {
//				mFeedBackEntity.option = 3;
//			} else if (checkedId == R.id.problem_other) {
//				mFeedBackEntity.option = 4;
//			} else {
				mFeedBackEntity.option = checkedId;
//			}
		}
	};

	public OnClickListener mSubmitOnclickListener = new OnClickListener() {
		
		public void onClick(View v) {
			if(mCurrentNetworkSpeedInfo==null){
    			showToast(R.string.you_should_test_speed_first);
				return;
    		}
			mFeedBackEntity.description = mDetailsEditText.getText().toString();
			mFeedBackEntity.phone = mPhoneNumEditText.getText().toString();
			mFeedBackEntity.mail = mEmailEditText.getText().toString();
			mFeedBackEntity.location = mAddressEditText.getText().toString();
			if(TextUtils.equals(mFeedBackEntity.location, mFeedBackEntity.city) || TextUtils.isEmpty(mFeedBackEntity.location)) {
				showToast(R.string.please_give_a_valid_address);
				return;
			}
			if(TextUtils.isEmpty(mFeedBackEntity.phone.trim()) || mFeedBackEntity.phone.trim().length()!=11) {
				showToast(R.string.you_should_give_an_phone_number);
				return;
			}
			String defaultIp = mResources.getString(R.string.default_ip);
			if(defaultIp.equals(mFeedBackEntity.ip)){
				mFeedBackEntity.ip = "0.0.0.0";
			}
			int verifyCheckedId = mFeedBackVerifyRadioGroup.getCheckedRadioButtonId();
			if(verifyCheckedId==R.id.feedback_verify_yes){
				mFeedBackEntity.is_correct = true;
			} else if(verifyCheckedId == R.id.feedback_verify_no){
				mFeedBackEntity.is_correct = false;
			}
			Gson gson = new Gson();
	    	Type listType = new TypeToken<List<SpeedInfoUploadEntity>>(){}.getType();
	    	try {
				String speedJson = gson.toJson(mCurrentSpeedTestResults, listType);
//				mFeedBackEntity.location = NetworkUtils.charEncoder(mFeedBackEntity.location);
//				mFeedBackEntity.isp = NetworkUtils.charEncoder(mFeedBackEntity.isp);
				String entityJson = gson.toJson(mFeedBackEntity);
				String json = entityJson.substring(0, entityJson.length()-1)+",\"speed\":"+speedJson+"}";
				new UploadTask().execute(json, domain + "/customer/pointlogs/");
				View layout = mInflater.inflate(R.layout.submit_toast, (ViewGroup)findViewById(R.id.toast_layout_root));
				Toast toast = new Toast(MainActivity.this);
				toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, -100);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setView(layout);
				toast.show();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		
	    	}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		String network_conf = mResources.getString(R.string.button_label_setup_network);
		String back = mResources.getString(R.string.back);
		
		switch(id){
		case DIALOG_NETWORK_EXCEPTION:
			String except_message = mResources.getString(R.string.msg_network_exception);
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(this);
			customBuilder.setPositiveButton(network_conf, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.setAction("lenovo.intent.action.NETWORK");
					sendBroadcast(intent);
					MainActivity.this.finish();
					dismissDialog(DIALOG_NETWORK_EXCEPTION);
				}
			}).setNegativeButton(back, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
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
					dismissDialog(DIALOG_NETWORK_UNESTABLISHED);
				}
			}).setPositiveButton(network_conf, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.setAction("lenovo.intent.action.NETWORK");
					sendBroadcast(intent);
					MainActivity.this.finish();
					dismissDialog(DIALOG_NETWORK_UNESTABLISHED);
				}
			}).setMessage(unestablished_message);
			dialog = customBuilder2.create();
			break;
//		case DIALOG_PENDING_CANCEL:
//			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//			LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
//			View layout = inflater.inflate(R.layout.progress_dialog, (LinearLayout)findViewById(R.id.progress_dialog_root));
//			builder.setView(layout);
//			dialog = builder.create();
		default:
			dialog = null;
			break;
		}
		return dialog;
	}
    
	private void showToast(int textResID) {
		View layout = mInflater.inflate(R.layout.submit_toast, (ViewGroup)findViewById(R.id.toast_layout_root));
		TextView tips = (TextView) layout.findViewById(R.id.submit_success_toast_text);
		tips.setText(textResID);
		Toast toast = new Toast(MainActivity.this);
		toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, -100);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
}