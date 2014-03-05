package cn.ismartv.speedtester.facilities;

import java.util.concurrent.ExecutorService;

import android.content.Context;

public class CoreUtils {
	
	public static CoreApplication getCoreApplication(Context context) {
		return (CoreApplication) context.getApplicationContext();
	}
	
	public static ExecutorService getExecutor(Context context) {
		return getCoreApplication(context).getExecutor();
	}
	
}
