package tv.ismar.speedtester;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.Future;
import tv.ismar.speedtester.facilities.CoreUtils;
import tv.ismar.speedtester.facilities.NetworkUtils;
import tv.ismar.speedtester.models.NetworkSpeedInfo;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;

public class CoreService extends Service {
	
	private final static String TAG = "SpeedTest_CoreService";

	public static String sDefaultRoot = "http://iris.tvxio.com"; 
	
	public NetworkSpeedInfo[] mNetworkSpeedInfos;
	
	private ISpeedTestCallback mCallback;
	
	private WorkerHandler mWorkerHandler = new WorkerHandler();
	
	private Future<?> mFuture;
	
	private Future<?> mSpeedTestFuture;
	
	private int mCurrentStep;
	
	private int mCurrentJob;
	
	public CoreService() {
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		mWorkerHandler = new WorkerHandler();
		return mBinder;
	}
	

	@SuppressLint("HandlerLeak")
	private final class WorkerHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			int result = msg.what;
			final int job = msg.arg1;
			final int step = msg.arg2;
			if(mCallback != null && result == AppConstant.RESULT_SUCCESS) {
				switch(job) {
				case AppConstant.JOB_PREPARE_TEST:
					mCurrentJob = AppConstant.JOB_PREPARE_TEST;
					String data = (String) msg.obj;
					Gson gson = new Gson();
					mNetworkSpeedInfos = gson.fromJson(data, NetworkSpeedInfo[].class);
					
					try {
						mCallback.onSpeedTestStart(mNetworkSpeedInfos.length);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					startSpeedTest(0);
					break;
				case AppConstant.JOB_STEP_START:
					mCurrentJob = AppConstant.JOB_STEP_START;
					try {
						mCallback.onStepStart(mNetworkSpeedInfos[step].length, step);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					break;
				case AppConstant.JOB_STEP_COMPLETE:
					mCurrentJob = AppConstant.JOB_STEP_COMPLETE;
					if(msg.obj != null) {
						NetworkSpeedInfo networkspeedInfo = (NetworkSpeedInfo)msg.obj;
						networkspeedInfo.stopflag = NetworkSpeedInfo.FLAG_STOP;
					}
					
					try {
						mCallback.onStepFinish(step);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					NetworkSpeedInfo networkSpeedInfo = mNetworkSpeedInfos[step];
					networkSpeedInfo.timeEscalpsed = SystemClock.uptimeMillis() - networkSpeedInfo.timeStarted;
					networkSpeedInfo.speed = (float) networkSpeedInfo.filesizeFinished / (float) networkSpeedInfo.timeEscalpsed * 1000f / 1024f; 
					
					if(step < (mNetworkSpeedInfos.length - 1)) {
						startSpeedTest(step + 1);
					} else {
						try {
							mCallback.onSpeedTestFinished(getFinalAverageSpeed());
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					break;
				}
			} else if(mCallback != null && result == AppConstant.RESULT_FAILED) {
				switch(job) {
				case AppConstant.JOB_PREPARE_TEST:
					String err = (String) msg.obj;
					try {
						mCallback.onError(AppConstant.JOB_PREPARE_TEST, err);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					break;
				case AppConstant.JOB_STEP_TESTING:
					String errMessage = (String) msg.obj;
					try {
						mCallback.onStepInterrupted(step, AppConstant.STEP_RESULT_INVALID, errMessage);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					if(step < (mNetworkSpeedInfos.length - 1)) {
						startSpeedTest(step + 1);
					}
					break;
				case AppConstant.JOB_CANCELLED:
					try {
						mCallback.onSpeedTestCancelled();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	private float getFinalAverageSpeed() {
		if(mNetworkSpeedInfos != null && mNetworkSpeedInfos.length > 0) {
			float averageSpeed = 0;
			for(NetworkSpeedInfo networkSpeedInfo : mNetworkSpeedInfos) {
				if(networkSpeedInfo.display && networkSpeedInfo.speed > 0) {
					averageSpeed += networkSpeedInfo.speed;
				}
			}
			return averageSpeed;
		}
		return 0;
	}
	
	private final class WorkerRunnable implements Runnable {
		
		private int mJob;
		private String mUrl;
		private int mMethod;
		private HashMap<String, String> mParams;
		
		public WorkerRunnable(int job, String url, int method, HashMap<String, String> params) {
			mJob = job;
			mUrl = url;
			mMethod = method;
			mParams = params;
		}

		@Override
		public void run() {
			Throwable throwable = null;
			String result = null;
			final WorkerHandler h = mWorkerHandler;
			if(mMethod == AppConstant.HTTP_GET) {
				try {
					result = NetworkUtils.getContent(mUrl, mParams);
				} catch (IOException e) {
					e.printStackTrace();
					throwable = e;
				}
			} else if(mMethod == AppConstant.HTTP_POST) {
				try {
					result = NetworkUtils.postContent(mUrl, mParams);
				} catch (IOException e) {
					e.printStackTrace();
					throwable = e;
				}
			}
			
			if(result == null) {
				if(throwable == null) {
					h.sendMessage(Message.obtain(h, AppConstant.RESULT_FAILED, mJob, 0, "Unknown error!"));
				} else {
					h.sendMessage(Message.obtain(h, AppConstant.RESULT_FAILED, mJob, 0, throwable.getMessage()));
				}
			} else {
				h.sendMessage(Message.obtain(h, AppConstant.RESULT_SUCCESS, mJob, 0, result));
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
		
	}
	
	private final ISpeedTestService.Stub mBinder = new ISpeedTestService.Stub() {
		
		@Override
		public void stopSpeedTest() throws RemoteException {
			if(mFuture != null) {
				if(!mFuture.isDone()) {
					mFuture.cancel(true);
				}
			}
			if(mSpeedTestFuture != null) {
				if(!mSpeedTestFuture.isDone()) {
					mSpeedTestFuture.cancel(true);
				}
			}
			mCurrentJob = AppConstant.JOB_CANCELLED;
			if(mCallback != null) {
				mCallback.onSpeedTestCancelled();
			}
		}
		
		@Override
		public void registerClient(ISpeedTestCallback callback)
				throws RemoteException {
			mCallback = callback;
		}
		
		@Override
		public void prepareSpeedTest() throws RemoteException {
			Log.d(TAG, "prepareSpeedTest");
			mFuture = httpRequest("/customer/urls/", AppConstant.JOB_PREPARE_TEST, AppConstant.HTTP_GET, null);
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
				if(networkSpeedInfo.timeStarted > 0) {
					long timeEscalpsed = SystemClock.uptimeMillis() - networkSpeedInfo.timeStarted;
					return (float)networkSpeedInfo.filesizeFinished / (float)timeEscalpsed * 1000.0f / 1024.0f;
				}
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
			if(mNetworkSpeedInfos != null && mNetworkSpeedInfos.length > 0) {
				float averageSpeed = 0;
				for(NetworkSpeedInfo networkSpeedInfo : mNetworkSpeedInfos) {
					if(networkSpeedInfo.display && networkSpeedInfo.speed > 0) {
						averageSpeed += networkSpeedInfo.speed;
					}
				}
				return averageSpeed;
			}
			return 0;
		}

		@Override
		public int getCurrentJob() throws RemoteException {
			return mCurrentJob;
		}
	};
	
	
	private String retrieveValidUrl(String url) {
		if(url==null) {
			return sDefaultRoot;
		} else {
			if(url.startsWith("http://")) {
				return url;
			} else if(url.matches("^/[^/].+")) {
				return sDefaultRoot + url;
			} else {
				return sDefaultRoot;
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
}
