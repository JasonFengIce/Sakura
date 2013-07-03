package tv.ismar.speedtester.facilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Application;

public class CoreApplication extends Application {
	
	private final static int POOL_SIZE = 10;
	
	private ExecutorService mExecutorService;
	
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "RestClient thread #" + mCount.getAndIncrement());
        }
    };
    
    /**
     * Return an ExecutorService (global to the entire application) that may be
     * used by clients when running long tasks in the background.
     * 
     * @return An ExecutorService to used when processing long running tasks
     */
    public ExecutorService getExecutor() {
		if(mExecutorService == null) {
			mExecutorService = Executors.newFixedThreadPool(POOL_SIZE, sThreadFactory);
		}
		return mExecutorService;
	}
}
