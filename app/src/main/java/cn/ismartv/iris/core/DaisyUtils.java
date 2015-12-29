package cn.ismartv.iris.core;

import android.content.Context;
import cn.ismartv.iris.VodApplication;

import java.util.concurrent.ExecutorService;

public class DaisyUtils {

	private DaisyUtils() {
	}

	/**
	 * Return the current {@link VodApplication}
	 * 
	 * @param context
	 *            The calling context
	 * @return The {@link VodApplication} the given context is linked to.
	 */
	public static VodApplication getVodApplication(Context context) {
		return (VodApplication) context.getApplicationContext();
	}


	/**
	 * Return the {@link VodApplication} executors pool.
	 * 
	 * @param context
	 *            The calling context
	 * @return The executors pool of the current {@link VodApplication}
	 */
	public static ExecutorService getExecutor(Context context) {
		return getVodApplication(context).getExecutor();
	}

}
