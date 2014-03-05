package cn.ismartv.speedtester;

public interface AppConstant {
	/**
	 * Prepare data for speed test. if this job failed, test should be cancel. 
	 */
	public final static int JOB_PREPARE_TEST = 0x01;
	/**
	 * Pending to start current step. NOT USED.
	 */
	@Deprecated
	public final static int JOB_STEP_PENDING = 0x02;
	/**
	 * A step has started! when in this job, speed is available.
	 */
	public final static int JOB_STEP_START = 0x03;
	/**
	 * A step is testing.
	 */
	public final static int JOB_STEP_TESTING = 0x04;
	/**
	 * A step has finished. No exception occurs.
	 */
	public final static int JOB_STEP_COMPLETE = 0x05;
	/**
	 * When job has been cancelled manually or unexpectedly, stop service if necessary.
	 */
	public final static int JOB_CANCELLED = 0x06;
	/**
	 * When job has been finished, stop service if necessary.
	 */
	public final static int JOB_FINISHED = 0x07;
	/**
	 * Something goes wrong, NOT USED 
	 */
	@Deprecated
	public final static int JOB_EXCEPTION = 0x08;
	/**
	 * Get IP address to indicate the user identity.
	 */
	public final static int JOB_GET_IP = 0x09;
	/**
	 * Upload the result of test. This is the final job.
	 */
	public final static int JOB_UPLOAD_SPEED = 0x0a;

	
	/**
	 * A worker thread has finished its job successfully.
	 */
	public final static int RESULT_SUCCESS = 0x010000;
	/**
	 * A worker thread has ended unexpectedly. Notice that when cancel a future of a thread don't propagate any message.
	 */
	public final static int RESULT_FAILED = 0x010001;
	
	/**
	 * HTTP method: GET
	 */
	public final static int HTTP_GET = 0x001;
	/**
	 * HTTP method: POST
	 */
	public final static int HTTP_POST = 0x002;
	
	
	/**
	 * A step has completed. May be ended by a exception of network issue.
	 */
	public final static int STEP_RESULT_COMPLETE = 0x0100;
	/**
	 * A step is not finished yet or cancelled
	 */
	public final static int STEP_RESULT_INVALID = 0x0101;
	
}
