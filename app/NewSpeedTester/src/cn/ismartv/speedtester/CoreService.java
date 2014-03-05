package cn.ismartv.speedtester;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.Future;

import cn.ismartv.speedtester.ISpeedTestCallback;
import cn.ismartv.speedtester.ISpeedTestService;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import cn.ismartv.speedtester.R;
import cn.ismartv.speedtester.facilities.CoreUtils;
import cn.ismartv.speedtester.facilities.ExportExclusionStrategy;
import cn.ismartv.speedtester.facilities.NetworkUtils;
import cn.ismartv.speedtester.models.FeedBackInfo;
import cn.ismartv.speedtester.models.NetworkSpeedInfo;
import cn.ismartv.speedtester.models.RESTResponse;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class CoreService extends Service {
	
	private final static String TAG = "SpeedTest_CoreService";

	public final static String DEFAULT_ROOT = "http://iris.tvxio.com";
	
	public NetworkSpeedInfo[] mNetworkSpeedInfos;
	public FeedBackInfo mFeedBackInfo;
	
	private ISpeedTestCallback mCallback;
	
	private WorkerHandler mWorkerHandler = new WorkerHandler();
	
	private SparseArray<Future<?>> mFutureList;
	
	private Future<?> mSpeedTestFuture;
	
	private int mCurrentStep;
	
	private int mCurrentJob;
	
	private long mMeasureInterval = 500;
	
	private Handler mUpdateHandler = new Handler();
	
	private UpdateSpeedTask mUpdateSpeedTask;
	
	@Override
	public IBinder onBind(Intent intent) {
//		mWorkerHandler = new WorkerHandler();
		/* start self to prevent service stop when unbound. service will stop self when all jobs have been done*/
		startService(new Intent(getApplicationContext(), CoreService.class));
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "Client Unbinded!");
		/* callback object is unavailable */
		mCallback = null;
		checkIfAllJobsHaveDone(false);
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service Destoryed!");
		super.onDestroy();
	}
	
	/**
	 * Check if all jobs have done and stop the service
	 */
	private void checkIfAllJobsHaveDone(boolean mayInterruptWorkers) {
		if(mCurrentJob == AppConstant.JOB_CANCELLED || mCurrentJob == AppConstant.JOB_FINISHED) {
			stopSelf();
		} else if(mayInterruptWorkers) {
			stopAllWorkers();
			mCurrentJob = AppConstant.JOB_FINISHED;
			checkIfAllJobsHaveDone(true);
		}
	}
	
	private void stopAllWorkers() {
		SparseArray<Future<?>> futureList = mFutureList;
		for(int i=0; i< futureList.size(); i++) {
			Future<?> future = futureList.valueAt(i);
			if(!future.isDone()) {
				future.cancel(true);
			}
		}
		futureList.clear();
		if(mSpeedTestFuture != null) {
			if(!mSpeedTestFuture.isDone()) {
				mUpdateHandler.removeCallbacks(mUpdateSpeedTask);
				mSpeedTestFuture.cancel(true);
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private final class WorkerHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			int result = msg.what;
			final int job = msg.arg1;
			final int step = msg.arg2;
			Gson gson = new Gson();
			if(result == AppConstant.RESULT_SUCCESS) {
				switch(job) {
				case AppConstant.JOB_PREPARE_TEST:
					
					String data = (String) msg.obj;
					
					try {
						mNetworkSpeedInfos = gson.fromJson(data, NetworkSpeedInfo[].class);
					} catch (JsonSyntaxException e1) {
						e1.printStackTrace();
					}
					
					if(mCallback != null && mNetworkSpeedInfos!= null && mNetworkSpeedInfos.length > 0) {
						try {
							mCallback.onTestReady(mNetworkSpeedInfos.length);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
 
					break;
				case AppConstant.JOB_STEP_START:
					mCurrentJob = AppConstant.JOB_STEP_START;
					if(mCallback != null) {
						/* consider we need a mechanism to run test on the background. we need to allow callback is null*/
						try {
							mCallback.onStepStart(mNetworkSpeedInfos[step].length, step);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						/* update speed */
						mUpdateSpeedTask = new UpdateSpeedTask();
						mUpdateHandler.post(mUpdateSpeedTask);
					} 
					break;
				case AppConstant.JOB_STEP_COMPLETE:
					mCurrentJob = AppConstant.JOB_STEP_COMPLETE;
					if(msg.obj != null) {
						NetworkSpeedInfo networkspeedInfo = (NetworkSpeedInfo)msg.obj;
						networkspeedInfo.stopflag = NetworkSpeedInfo.FLAG_STOP;
						mUpdateHandler.removeCallbacks(mUpdateSpeedTask);
					}
					
					NetworkSpeedInfo networkSpeedInfo = mNetworkSpeedInfos[step];
					networkSpeedInfo.timeEscalpsed = SystemClock.uptimeMillis() - networkSpeedInfo.timeStarted;
					networkSpeedInfo.speed = (float) networkSpeedInfo.filesizeFinished / (float) networkSpeedInfo.timeEscalpsed * 1000f / 1024f; 
					
					if(mCallback != null) {
						/* consider we need a mechanism to run test on the background. we need to allow callback is null*/
						try {
							mCallback.onStepFinish(step);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					
					if(step < (mNetworkSpeedInfos.length - 1)) {
						startSpeedTest(step + 1);
					} else {
						speedTestComplete();
					}
					break;
				case AppConstant.JOB_GET_IP:
					String feedBackInfoStr = (String) msg.obj;
					mFeedBackInfo = gson.fromJson(feedBackInfoStr, FeedBackInfo.class);
					if(TextUtils.isEmpty(mFeedBackInfo.ip)) {
						mFeedBackInfo.ip = getResources().getString(R.string.default_ip);
					}
					if(TextUtils.isEmpty(mFeedBackInfo.location)) {
						mFeedBackInfo.location = getResources().getString(R.string.unknown_area);
					}
					if(TextUtils.isEmpty(mFeedBackInfo.isp)) {
						mFeedBackInfo.isp = getResources().getString(R.string.unknown_isp);
					}
					mFutureList.remove(AppConstant.JOB_GET_IP);
					break;
				case AppConstant.JOB_UPLOAD_SPEED:
					checkIfAllJobsHaveDone(true);
					break;
				}
			} else if(result == AppConstant.RESULT_FAILED) {
				switch(job) {
				case AppConstant.JOB_PREPARE_TEST:
				case AppConstant.JOB_GET_IP:
					String err = (String) msg.obj;
					if(mCallback != null) {
						try {
							mCallback.onError(job, err);
						} catch (RemoteException e) {
							e.printStackTrace();
							cancelSpeedTest();
						}
					} else {
						/* if prepare job or get IP job failed. and no callback is available. just stop and cancel */
						cancelSpeedTest();
					}
					break;
				case AppConstant.JOB_STEP_TESTING:
					String errMessage = (String) msg.obj;
					mUpdateHandler.removeCallbacks(mUpdateSpeedTask);
					if(mCallback != null) {
						try {
							mCallback.onStepInterrupted(step, AppConstant.STEP_RESULT_INVALID, errMessage);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						
					}
					if(step < (mNetworkSpeedInfos.length - 1)) {
						startSpeedTest(step + 1);
					} else {
						speedTestComplete();
					}
					break;
				case AppConstant.JOB_UPLOAD_SPEED:
					/* Just change job to cancel state */
					checkIfAllJobsHaveDone(true);
					break;
				}
			}
		}
		
	}
	
	/**
	 * Get average speed of all the steps. Notice that if {@link NetworkSpeedInfo#display} property is false. this step result will be excluded.
	 * @return an average speed of all the steps except whose {@link NetworkSpeedInfo#display} property is false step. Unit is KB/s
	 */
	private float getFinalAverageSpeed() {
		if(mNetworkSpeedInfos != null && mNetworkSpeedInfos.length > 0) {
			int totalDownloadFileSize = 0;
			long totalDownloadTime = 0;
			for(NetworkSpeedInfo networkSpeedInfo : mNetworkSpeedInfos) {
				if(networkSpeedInfo.display && networkSpeedInfo.speed > 0) {
					totalDownloadFileSize += networkSpeedInfo.filesizeFinished;
					totalDownloadTime += networkSpeedInfo.timeEscalpsed;
				}
			}
			return ((float) totalDownloadFileSize / 1024f / ((float) totalDownloadTime / 1000f));
		}
		return 0;
	}
	
	/**
	 * Main worker thread which handles all HTTP request not include the speed test request
	 * When task is done, main work handler will be notified according to the job.
	 * @author Bob
	 */
	private final class WorkerRunnable implements Runnable {
		
		private int mJob;
		private String mUrl;
		private int mMethod;
		private HashMap<String, String> mParams;
		
		/**
		 * Create a WorkerRunnable.
		 * @param job  type of the job defined in {@link AppConstant} with JOB_ prefix.  
		 * @param url  the request url.
		 * @param method  the request method, can be {@link AppConstant#HTTP_GET} or {@link AppConstant#HTTP_POST}   
		 * @param params  the request parameters, as a query string.
		 */
		public WorkerRunnable(int job, String url, int method, HashMap<String, String> params) {
			mJob = job;
			mUrl = url;
			mMethod = method;
			mParams = params;
		}

		@Override
		public void run() {
			final WorkerHandler h = mWorkerHandler;
			RESTResponse result = null;
			if(mMethod == AppConstant.HTTP_GET) {
				result = NetworkUtils.getContent(mUrl, mParams);
			} else if(mMethod == AppConstant.HTTP_POST) {
				result = NetworkUtils.postContent(mUrl, mParams);
			} else {
				result = new RESTResponse();
			}
			
			if(result.content != null) {
				h.sendMessage(Message.obtain(h, AppConstant.RESULT_SUCCESS, mJob, 0, result.content));
			} else {
				if(result.err == null) {
					h.sendMessage(Message.obtain(h, AppConstant.RESULT_FAILED, mJob, 0, "Unknown error!"));
				} else {
					h.sendMessage(Message.obtain(h, AppConstant.RESULT_FAILED, mJob, 0, result.err.getMessage()));
				}
			}
			
		}
		
	}
	
	/**
	 * execute a new request in a separated thread. When thread completed, a handler will receive a message.
	 * @param path
	 * @param job
	 * @param method
	 * @param params
	 * @return
	 */
	public Future<?> httpRequest(String path, int job, int method, HashMap<String, String> params) {
		String url = retrieveValidUrl(path);
		return CoreUtils.getExecutor(this).submit(new WorkerRunnable(job, url, method, params));
	}
	
	public void startSpeedTest(int step) {
		mCurrentStep = step;
		mSpeedTestFuture = CoreUtils.getExecutor(this).submit(new SpeedTestTask(step, mNetworkSpeedInfos[step]));
	}
	
	public void uploadTestResult() {
		ExclusionStrategy excludeStrategy = new ExportExclusionStrategy();
		Gson gson = new GsonBuilder().addSerializationExclusionStrategy(excludeStrategy).create();
		String speedArray = gson.toJson(mNetworkSpeedInfos, NetworkSpeedInfo[].class);
		String uploadStr = "{\"speed\":"+speedArray+", \"ip\":\""+mFeedBackInfo.ip
				+"\",\"location\":\""+mFeedBackInfo.location+"\",\"isp\":\""+mFeedBackInfo.isp+"\"}";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("q", uploadStr);
		Future<?> future = httpRequest("/customer/speedlogs/", AppConstant.JOB_UPLOAD_SPEED, AppConstant.HTTP_POST, params);
		mFutureList.put(AppConstant.JOB_UPLOAD_SPEED, future);
	}
	
	/**
	 * Invoke when speed test has completed!
	 */
	private void speedTestComplete() {
		if(mCallback != null) {
			try {
				mCallback.onSpeedTestFinished(getFinalAverageSpeed());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		uploadTestResult();
	}
	
	/**
	 * Use to cancel the speed test task, may invoke from client or service itself.
	 */
	private void cancelSpeedTest() {
		stopAllWorkers();
		mCurrentJob = AppConstant.JOB_CANCELLED;
		if(mCallback != null) {
			try {
				mCallback.onSpeedTestCancelled();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private final ISpeedTestService.Stub mBinder = new ISpeedTestService.Stub() {
		
		@Override
		public void stopSpeedTest() throws RemoteException {
			cancelSpeedTest();
		}
		
		@Override
		public void registerClient(ISpeedTestCallback callback)
				throws RemoteException {
			mCallback = callback;
		}
		
		@Override
		public void prepareSpeedTest() throws RemoteException {
			Log.d(TAG, "prepareSpeedTest");
			mCurrentJob = AppConstant.JOB_PREPARE_TEST;
			mFutureList.put(AppConstant.JOB_PREPARE_TEST, httpRequest("/customer/urls/", AppConstant.JOB_PREPARE_TEST, AppConstant.HTTP_GET, null));
			mFutureList.put(AppConstant.JOB_PREPARE_TEST, httpRequest("http://lily.tvxio.com/iplookup", AppConstant.JOB_GET_IP, AppConstant.HTTP_GET, null));
		}
		
		@Override
		public int getTotalSteps() throws RemoteException {
			if(mNetworkSpeedInfos != null && mNetworkSpeedInfos.length > 0) {
				return mNetworkSpeedInfos.length;
			} else {
				return 0;
			}
		}
		
		@Override
		public int getCurrentStep() throws RemoteException {
			return mCurrentStep;
		}
		
		@Override
		public float getCurrentSpeed() throws RemoteException {
			if(mNetworkSpeedInfos != null && mNetworkSpeedInfos.length > 0) {
				NetworkSpeedInfo networkSpeedInfo = mNetworkSpeedInfos[mCurrentStep];
				return networkSpeedInfo.speed;
			}
			return 0;
		}
		
		@Override
		public int getCurrentProgressOfStep() throws RemoteException {
			if(mNetworkSpeedInfos != null && mNetworkSpeedInfos.length > 0) {
				NetworkSpeedInfo networkSpeedInfo = mNetworkSpeedInfos[mCurrentStep];
				if(networkSpeedInfo.length > 0 && networkSpeedInfo.timeStarted > 0) {
					networkSpeedInfo.timeEscalpsed = SystemClock.uptimeMillis() - networkSpeedInfo.timeStarted;
					return (int)((float)networkSpeedInfo.timeEscalpsed / (float) networkSpeedInfo.length * 100f);
				}
			}
			return 0;
		}
		
		@Override
		public float getAverageSpeed() throws RemoteException {
			return getFinalAverageSpeed();
		}

		@Override
		public int getCurrentJob() throws RemoteException {
			return mCurrentJob;
		}

		@Override
		public void startTest() throws RemoteException {
			if(mNetworkSpeedInfos != null && mNetworkSpeedInfos.length > 0) {
				startSpeedTest(0);
			} else {
				throw new RemoteException();
			}
			
		}
	};
	
	
	private String retrieveValidUrl(String url) {
		if(url==null) {
			return DEFAULT_ROOT;
		} else {
			if(url.startsWith("http://")) {
				return url;
			} else if(url.matches("^/[^/].+")) {
				return DEFAULT_ROOT + url;
			} else {
				return DEFAULT_ROOT;
			}
		}
	}
	
	private class SpeedTestTask implements Runnable {
		
		private int mStep;
		private NetworkSpeedInfo mNetworkSpeedInfo;
		
		public SpeedTestTask(int step, NetworkSpeedInfo info) {
			mStep = step;
			mNetworkSpeedInfo = info;
		}

		@Override
		public void run() {
			final NetworkSpeedInfo networkSpeedInfo = mNetworkSpeedInfo;
			int result = AppConstant.STEP_RESULT_INVALID;
			Exception exception = null;
			final WorkerHandler h = mWorkerHandler;
			try {
				result = networkSpeedInfo.download(h, mStep);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				exception = e;
			} catch (IOException e) {
				e.printStackTrace();
				exception = e;
			}
			
			if(result != AppConstant.STEP_RESULT_COMPLETE) {
				h.removeMessages(AppConstant.RESULT_SUCCESS, networkSpeedInfo);
				if( exception == null) {
					h.sendMessage(Message.obtain(h, AppConstant.RESULT_FAILED, AppConstant.JOB_STEP_TESTING, mStep, "Unknown Error!"));
				} else {
					h.sendMessage(Message.obtain(h, AppConstant.RESULT_FAILED, AppConstant.JOB_STEP_TESTING, mStep, exception.getMessage()));
				}
			}
		}
	}
	
	private class UpdateSpeedTask implements Runnable {

		private int lastFinishedFileSize;
		
		@Override
		public void run() {
			if(mSpeedTestFuture != null && !mSpeedTestFuture.isDone()) {
				NetworkSpeedInfo info = mNetworkSpeedInfos[mCurrentStep];
				int finishedFileSize = info.filesizeFinished;
				int chunkSize = finishedFileSize - lastFinishedFileSize;
				lastFinishedFileSize = finishedFileSize;
				info.speed = (float)chunkSize / (float) mMeasureInterval;
				if(mCallback != null) {
					try {
						mCallback.onUpdateSpeed(info.speed);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				mUpdateHandler.postDelayed(this, mMeasureInterval);
			}
		}
		
	}

}
