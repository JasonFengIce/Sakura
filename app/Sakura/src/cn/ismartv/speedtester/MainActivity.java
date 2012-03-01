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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.ismartv.speedtester.domain.FeedBackEntity;
import cn.ismartv.speedtester.domain.FeedBackProblemEntity;
import cn.ismartv.speedtester.domain.NetworkSpeedInfo;
import cn.ismartv.speedtester.domain.SpeedInfoUploadEntity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity {
	
	private static final int TEST_STATE_IDLE = 0;
	private static final int TEST_STATE_PENDING = 1;
	private static final int TEST_STATE_TESTING = 2;
	
	private static final int DIALOG_NETWORK_UNESTABLISHED = 1;
	private static final int DIALOG_NETWORK_EXCEPTION = 2;
	
	public static final int NETWORK_CONNECTION_NORMAL = 0;
	public static final int NETWORK_CONNECTION_TIMEOUT = 1;
	public static final int NETWORK_CONNECTION_UNKNOWN = 2;
	
	/**
	 * mHandler is used to update ui and control file download progress.
	 */
	private Handler mTimingHandler = new Handler();
	private Handler mDownloadHandler = new Handler();
	
	private Thread mDownloaderThread = null;
	
	private GetFileUrlTask mGetFileUrlTask = null;
	
	private LayoutInflater mInflater;
	
	private LinearLayout mLeftSide;
	private RelativeLayout mGraphicHolder;

	private TextView mSpeedShowText;
	private Button mSpeedActionButton;
	private ProgressBar mCurrentProgressBar;
	private Resources mResources;
	private TextView mStatusIndicatorText;
//	private ImageView mStatusIcon;
	private LinearLayout mTestAearLayout;
	private TextView mSuggestionText;
	private RadioGroup mProblemOptionsRadioGroup;
	
	private TextView mSpeedIndicatorText;
	private PointerView mDashBoardPointer;
	
	private EditText mDetailsEditText;
	private EditText mEmailEditText;
	private EditText mPhoneNumEditText;
	private TextView mFeedBackIpText;
	private TextView mFeedBackCityText;
	private TextView mFeedBackISPText;
	private RadioGroup mFeedBackVerifyRadioGroup;
	private Button mSubmit;
	
	private TextView mSnShowTextView;
	private TextView mModelShowTextView;
	
	
	private String mJsonStr = null;
	private List<NetworkSpeedInfo> mNetworkSpeedInfoList = null;
	private NetworkSpeedInfo mCurrentNetworkSpeedInfo = null;
	private ArrayList<SpeedInfoUploadEntity> mCurrentSpeedTestResults = null;
	private ArrayList<FeedBackProblemEntity> mFeedBackProblemEntities = null;
	private FeedBackEntity mFeedBackEntity= null;
	
	private int mTestState = TEST_STATE_IDLE;
	private int mCurrentPosition = 0;
	
	private boolean isNetworkAvailable = false;
	
	public static String domain = "http://iris.tvxio.com"; 
	
	
	private OnClickListener mActionButtonListener = new OnClickListener() {
		
		public void onClick(View v) {
			switch(mTestState){
			case TEST_STATE_IDLE:
				mTestState=TEST_STATE_PENDING;
				mStatusIndicatorText.setText(mResources.getString(R.string.starting));
				mSpeedActionButton.setText(R.string.button_label_stop);
				if(mJsonStr!=null){
					
					if(parseJson()){
						mCurrentPosition = 0;
						startToTest();
					}
				} else {
					updateJsonStr();
				}
				break;
			case TEST_STATE_PENDING:
				mGetFileUrlTask.cancel(true);
				break;
			case TEST_STATE_TESTING:
				mTimingHandler.removeCallbacks(updateStatusTask);
				mDownloadHandler.removeCallbacks(mDownloaderThread);
				mTestState = TEST_STATE_IDLE;
				mCurrentProgressBar.setVisibility(View.INVISIBLE);
				mStatusIndicatorText.setText(R.string.test_stopped);
				mSpeedActionButton.setText(R.string.button_label_retest);
				break;
			}
			
		}
	};
	
	private void updateSpeedIndicatorText(float speed){
		float speedInKB = ((float)((int)(speed*100F))/100F);
		int numK = (int)speedInKB/1000;
		int numH = (int)((speedInKB-numK*1000)/100);
		int numD = (int)((speedInKB-numK*1000-numH*100)/10);
		int numN = (int)(speedInKB-numK*1000-numH*100-numD*10);
		int numP1 = (int)((float)(speedInKB-(float)((int)speedInKB))*10F);
		int numP2 = (int)((speedInKB - ((float)((int)(speedInKB*10)) / 10F))*100F);
		mSpeedIndicatorText.setText(String.valueOf(numK)+String.valueOf(numH)+String.valueOf(numD)+String.valueOf(numN)+"."+String.valueOf(numP1)+String.valueOf(numP2));
	}
	
	private Runnable updateStatusTask = new Runnable() {
		private int counter = 0;
		public void run() {
			if(mCurrentNetworkSpeedInfo.timeStarted>0){
				if(mCurrentProgressBar.getVisibility()==View.INVISIBLE){
					mCurrentProgressBar.setVisibility(View.VISIBLE);
				}
				if(mCurrentNetworkSpeedInfo.timeEscalpsed>=mCurrentNetworkSpeedInfo.length){
//					mDownloadHandler.removeCallbacks(downloadFileTask);
					counter = 0;
					if(mCurrentNetworkSpeedInfo.speed == -1){
						mSpeedShowText.setText(R.string.exception_time_out);
					} else if(mCurrentNetworkSpeedInfo.speed == -2){
						mSpeedShowText.setText(R.string.exception_unknown);
					}
					mDashBoardPointer.stopHanlde();
					mSpeedIndicatorText.setText("- - - -. - -");
					mCurrentNetworkSpeedInfo.flagStop = 1;
					if(mCurrentPosition<mNetworkSpeedInfoList.size()-1){
						mCurrentPosition++;
						startToTest();
					} else {
						//TODO:show the result to user
						mTestState = TEST_STATE_IDLE;
						mTimingHandler.removeCallbacks(updateStatusTask);
						mStatusIndicatorText.setText(mResources.getString(R.string.test_completion));
						mSpeedActionButton.setText(R.string.button_label_retest);
						uploadSpeedTestResult();
						mTimingHandler.removeCallbacks(updateStatusTask);
						return;
					}
				} else {
					mCurrentNetworkSpeedInfo.timeEscalpsed = SystemClock.uptimeMillis() - mCurrentNetworkSpeedInfo.timeStarted;
					mCurrentNetworkSpeedInfo.speed = (float)mCurrentNetworkSpeedInfo.filesizeFinished / (float)mCurrentNetworkSpeedInfo.timeEscalpsed * 1000.0F / 1024.0F;
					float currentSpeed = mCurrentNetworkSpeedInfo.speed;
					String currentUnit = mResources.getString(R.string.speed_unit_kb); 
					if(currentSpeed > 1024){
						currentSpeed = currentSpeed / 1024.0F;
						currentUnit = mResources.getString(R.string.speed_unit_mb);
					} else if( currentSpeed < 1) {
						currentSpeed = currentSpeed * 1024.0F;
						currentUnit = mResources.getString(R.string.speed_unit_byte);
					}
					currentSpeed = (float)((int)(currentSpeed * 100F))/100F;
					mSpeedShowText.setText(currentSpeed + " " + currentUnit);
					mCurrentProgressBar.setProgress((int)mCurrentNetworkSpeedInfo.timeEscalpsed);
					updateSpeedIndicatorText(mCurrentNetworkSpeedInfo.speed);
					counter++;
					if(counter%2==0){
//						Log.d("counter", ""+counter);
						float speed = mCurrentNetworkSpeedInfo.speed < 2000?mCurrentNetworkSpeedInfo.speed:2000;
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
				mCurrentProgressBar.setProgress((int)mCurrentNetworkSpeedInfo.length);
				break;
			case NETWORK_CONNECTION_TIMEOUT:
				mCurrentNetworkSpeedInfo.timeStarted = 1;
				mCurrentNetworkSpeedInfo.timeEscalpsed = mCurrentNetworkSpeedInfo.length;
				mCurrentNetworkSpeedInfo.speed = -1;
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case NETWORK_CONNECTION_UNKNOWN:
				mCurrentNetworkSpeedInfo.timeStarted = 1;
				mCurrentNetworkSpeedInfo.timeEscalpsed = mCurrentNetworkSpeedInfo.length;
				mCurrentNetworkSpeedInfo.speed = -2;
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
		}
		
	};

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mResources = getResources();
        
        mLeftSide = (LinearLayout)findViewById(R.id.left_side);
        
        mGraphicHolder = (RelativeLayout)findViewById(R.id.graphic_holder); 
        
        mSpeedActionButton = (Button)findViewById(R.id.speed_actionbutton);
        mSpeedActionButton.setOnClickListener(mActionButtonListener);
        mStatusIndicatorText = (TextView)findViewById(R.id.status_tips);
//        mStatusIcon = (ImageView)findViewById(R.id.status_icon);
        mSuggestionText = (TextView)findViewById(R.id.suggestion_text);
        mCurrentProgressBar = (ProgressBar)findViewById(R.id.current_progress);
        mProblemOptionsRadioGroup = (RadioGroup)findViewById(R.id.problem_options);
        
        mDetailsEditText = (EditText)findViewById(R.id.edit_details);
        mEmailEditText = (EditText)findViewById(R.id.edit_email);
        mPhoneNumEditText = (EditText)findViewById(R.id.edit_phonenum);
        mFeedBackIpText = (TextView)findViewById(R.id.feedback_ip);
        mFeedBackCityText = (TextView)findViewById(R.id.feedback_city);
        mFeedBackISPText = (TextView)findViewById(R.id.feedback_isp);
        mFeedBackVerifyRadioGroup = (RadioGroup)findViewById(R.id.feedback_verify_options);
        mSubmit = (Button)findViewById(R.id.button_submit);
        
        mSnShowTextView = (TextView)findViewById(R.id.sn_show);
        mModelShowTextView = (TextView)findViewById(R.id.model_show);
        mSnShowTextView.setText("SN: "+android.os.Build.SERIAL);
        mModelShowTextView.setText(mResources.getString(R.string.model)+" " +android.os.Build.MODEL);
        
        mTestAearLayout = (LinearLayout)findViewById(R.id.speedtest_area);
        
        mInflater = LayoutInflater.from(MainActivity.this);
        
        mFeedBackEntity = new FeedBackEntity();
        
        mDashBoardPointer = new PointerView(this);
//        mDashBoardPointer.setImageDrawable(mResources.getDrawable(R.drawable.dashboard_pointer));
        mDashBoardPointer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mGraphicHolder.addView(mDashBoardPointer);
        mSpeedIndicatorText = (TextView)findViewById(R.id.speed_indicator_text);
    }
    
    
    protected void uploadSpeedTestResult() {
    	mCurrentSpeedTestResults = new ArrayList<SpeedInfoUploadEntity>();
    	for(NetworkSpeedInfo n:mNetworkSpeedInfoList){
    		SpeedInfoUploadEntity entity = new SpeedInfoUploadEntity();
    		entity.setPk(n.pk);
    		entity.setSpeed(n.speed);
    		mCurrentSpeedTestResults.add(entity);
    	}
    	Gson gson = new Gson();
    	Type listType = new TypeToken<List<SpeedInfoUploadEntity>>(){}.getType();
    	try {
			String json = gson.toJson(mCurrentSpeedTestResults, listType);
			String uploadStr = "{\"speed\":"+json+", \"ip\":\""+mFeedBackEntity.ip+"\",\"location\":\""+mFeedBackEntity.location+"\",\"isp\":\""+mFeedBackEntity.isp+"\"}";
			new UploadTask().execute(uploadStr, domain+"/customer/speedlogs/");
			Log.d("uploadStr",uploadStr);
			
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
				View layout = mInflater.inflate(R.layout.submit_toast, (ViewGroup)findViewById(R.id.toast_layout_root));
//				TextView textView = (TextView)layout.findViewById(R.id.submit_success_toast_text);
				Toast toast = new Toast(MainActivity.this);
				toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, -100);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setView(layout);
				toast.show();
			}
			super.onPostExecute(result);
		}
    	
    }


	
    protected void startToTest() {
    	mTestState = TEST_STATE_TESTING;
    	mCurrentNetworkSpeedInfo = mNetworkSpeedInfoList.get(mCurrentPosition);
    	mStatusIndicatorText.setText(mResources.getString(R.string.testing));
    	View currentProgressLayout = mTestAearLayout.getChildAt(mCurrentPosition);
    	mCurrentProgressBar.setVisibility(View.VISIBLE);
    	mCurrentProgressBar.setMax((int)mCurrentNetworkSpeedInfo.length);
    	mSpeedShowText = (TextView)currentProgressLayout.findViewById(R.id.speed_show_text);
    	
    	mDownloaderThread = new Thread(downloadFileTask);
    	mDownloaderThread.start();
    	mTimingHandler.postDelayed(updateStatusTask, mCurrentPosition==0?50:400);
    	mDashBoardPointer.startHandle();
	}
    
    private void buildTestLayout(){
    	mTestAearLayout.removeAllViews();
    	for(int i=0;i<mNetworkSpeedInfoList.size();i++){
    		NetworkSpeedInfo networkSpeedInfo = mNetworkSpeedInfoList.get(i);
    		LinearLayout layout =  (LinearLayout) mInflater.inflate(R.layout.listitem, null);
    		TextView providerShow = (TextView) layout.findViewById(R.id.provider_show);
    		providerShow.setText(networkSpeedInfo.title);
    		mTestAearLayout.addView(layout, i);
    	}
    }
    
	class GetFileUrlTask extends AsyncTask<Void, Void, Integer>{

		@Override
		protected Integer doInBackground(Void... params) {
			StringBuffer sb = new StringBuffer();
			try {
				//TODO:This url needs to be supplied
				URL url = new URL(domain+"/customer/urls/");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(20000);
				InputStream is = conn.getInputStream();
				BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while((line=bfReader.readLine())!=null){
					if(isCancelled()){
						mJsonStr = null;
						return 2;
					}
					sb.append(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
				mJsonStr = null;
				return 0;
			}
			mJsonStr = sb.toString();
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result==1){
				//TODO:continue to run test.
				if(mTestState==TEST_STATE_PENDING){
					if(parseJson()){
						mCurrentPosition = 0;
						startToTest();
					}
				}
				
			} else {
				isNetworkAvailable = false;
				//show a info about network exception.
				showDialog(DIALOG_NETWORK_EXCEPTION);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled(Integer result) {
			if(result==2){
				Log.d("onCancel","Cancelled");
				mSpeedActionButton.setText(R.string.button_label_retest);
				mTestState = TEST_STATE_IDLE;
				mStatusIndicatorText.setText(R.string.test_stopped);
			}
			super.onCancelled(result);
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
        	if(mGetFileUrlTask==null){
        		mGetFileUrlTask = new GetFileUrlTask();
        		mGetFileUrlTask.execute(null, null, null);
        	} else {
        		if(mGetFileUrlTask.getStatus()!=AsyncTask.Status.RUNNING){
        			mGetFileUrlTask = new GetFileUrlTask();
            		mGetFileUrlTask.execute(null, null, null);
        		}
        	}
        	
        	return true;
        } else {
        	//else remind user to setup network settings. pop up a dialog, contains two button [Network Setting], [Cancel]
        	//WIFI setting Activity: ACTION_WIFI_SETTINGS
        	showDialog(DIALOG_NETWORK_UNESTABLISHED);
        	return false;
        }
    }
    
    private boolean parseJson(){
    	mNetworkSpeedInfoList = new ArrayList<NetworkSpeedInfo>();
    	Log.d("json", mJsonStr);
    	Gson gson = new Gson();
    	Type listType = new TypeToken<List<NetworkSpeedInfo>>(){}.getType();
    	try {
			mNetworkSpeedInfoList = gson.fromJson(mJsonStr, listType);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return false;
		}
    	for(NetworkSpeedInfo n:mNetworkSpeedInfoList){
    		Log.d("networkinfo", n.toString());
    	}
    	buildTestLayout();
    	return true;
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mTestState = TEST_STATE_IDLE;
		mDownloadHandler.removeCallbacks(downloadFileTask);
		mTimingHandler.removeCallbacks(updateStatusTask);
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d("ui","resume");
		if(mFeedBackProblemEntities == null){
			new GetFeedBackInfo().execute(1);
		}
		if(mFeedBackEntity.ip==null){
			new GetFeedBackInfo().execute(2);
		}
		super.onResume();
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
					conn.setConnectTimeout(20000);
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
						conn.disconnect();
					}
					if(bfReader!=null){
						try {
							bfReader.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else if(params[0]==2){
				HttpURLConnection conn2 = null;
				BufferedReader bfReader2 = null;
				StringBuffer sb2 = new StringBuffer();
				try {
					URL url2 = new URL("http://counter.sina.com.cn/ip");
					conn2 = (HttpURLConnection) url2.openConnection();
					conn2.setConnectTimeout(20000);
					InputStream is2 = conn2.getInputStream();
					bfReader2 = new BufferedReader(new InputStreamReader(is2,"GBK"));
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				String returnValue = sb2.toString();
				Pattern p = Pattern.compile("(Array\\((.+?)\\))");
				Matcher matcher = p.matcher(returnValue);
				matcher.find();
				String result = matcher.group(2);
				Log.d("location: ", result);
				String[] ipInfos = result.split(",");
				if(ipInfos.length>0) {
					String ip = ipInfos[0].trim();
					mFeedBackEntity.ip = ip != "\"\"" ? ip.substring(1, ip.length()-1) : "";
				}
				if(ipInfos.length>2) {
					String location = ipInfos[2].trim();
					mFeedBackEntity.location = location != "\"\"" ? location.substring(1, location.length()-1): "";
				}
				if(ipInfos.length>4){
					String isp = ipInfos[4].trim();
					mFeedBackEntity.isp = isp != "\"\"" ? isp.substring(1, isp.length()-1): "";
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
						ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						layoutParams.setMargins(5, 0, 10, 0);
						radioButton.setTextSize(26);
						radioButton.setText(entity.point_name);
						radioButton.setId(entity.point_id);
						mProblemOptionsRadioGroup.addView(radioButton, layoutParams);
						if(i==max){
							radioButton.setChecked(true);
							mFeedBackEntity.option = entity.point_id;
						}
					}
				}
				mProblemOptionsRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
				mFeedBackEntity.option = mFeedBackProblemEntities!=null ? mFeedBackProblemEntities.size()-1 :4;
			} else if(result==2){
				if(mFeedBackEntity.ip!=null && mFeedBackEntity.ip!=""){
					mFeedBackIpText.setText(mFeedBackEntity.ip);
				}
				if(mFeedBackEntity.location!=null && mFeedBackEntity.location!=""){
					mFeedBackCityText.setText(mFeedBackEntity.location);
				}
				if(mFeedBackEntity.isp!=null && mFeedBackEntity.isp!=""){
					mFeedBackISPText.setText(mFeedBackEntity.isp);
				}
				mSubmit.setOnClickListener(mSubmitOnclickListener);
			}
			super.onPostExecute(result);
		}
	};
	
	public OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch(checkedId){
			case R.id.problem_unclear:
				mFeedBackEntity.option = 1;
				break;
			case R.id.problem_block:
				mFeedBackEntity.option = 2;
				break;
			case R.id.problem_hardware:
				mFeedBackEntity.option = 3;
				break;
			case R.id.problem_other:
				mFeedBackEntity.option = 4;
				break;
			default:
				mFeedBackEntity.option = checkedId;
			}
		}
	};

	public OnClickListener mSubmitOnclickListener = new OnClickListener() {
		
		public void onClick(View v) {
			mFeedBackEntity.description = mDetailsEditText.getText().toString();
			mFeedBackEntity.phone = mPhoneNumEditText.getText().toString();
			mFeedBackEntity.mail = mEmailEditText.getText().toString();
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
				String entityJson = gson.toJson(mFeedBackEntity);
				String json = entityJson.substring(0, entityJson.length()-1)+",\"speed\":"+speedJson+"}";
				new UploadTask().execute(json, domain + "/customer/pointlogs/");
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		
	    	}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
//		String setup_network = mResources.getString(R.string.button_label_setup_network);
		switch(id){
		case DIALOG_NETWORK_EXCEPTION:
			AlertDialog.Builder exceptDialogBuilder = new AlertDialog.Builder(this);
			String except_message = mResources.getString(R.string.msg_network_exception);
			exceptDialogBuilder.setMessage(except_message)
			.setCancelable(true)
//			.setPositiveButton(setup_network, new DialogInterface.OnClickListener() {
//				
//				public void onClick(DialogInterface dialog, int which) {
//					Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
//					startActivity(intent);
//					dismissDialog(DIALOG_NETWORK_EXCEPTION);
//				}
//			})
			.setNeutralButton(R.string.button_label_OK, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(DIALOG_NETWORK_EXCEPTION);
				}
			});
			dialog = exceptDialogBuilder.create();
			break;
		case DIALOG_NETWORK_UNESTABLISHED:
			AlertDialog.Builder noNetworkDialogBuilder = new AlertDialog.Builder(this);
			String no_network_message = mResources.getString(R.string.msg_network_unestablished);
			noNetworkDialogBuilder.setMessage(no_network_message)
			.setCancelable(true)
//			.setPositiveButton(setup_network, new DialogInterface.OnClickListener() {
//				
//				public void onClick(DialogInterface dialog, int which) {
//					Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
//					startActivity(intent);
//					dismissDialog(DIALOG_NETWORK_UNESTABLISHED);
//				}
//			})
			.setNeutralButton(R.string.button_label_OK, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(DIALOG_NETWORK_UNESTABLISHED);
				}
			});
			dialog = noNetworkDialogBuilder.create();
			break;
		default:
			dialog = null;
			break;
		}
		return dialog;
	}
    
}