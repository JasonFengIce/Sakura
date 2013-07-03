package tv.ismar.speedtester;

public interface AppConstant {
	
	public final static int JOB_PREPARE_TEST = 0x01;
	public final static int JOB_STEP_PENDING = 0x02;
	public final static int JOB_STEP_START = 0x03;
	public final static int JOB_STEP_TESTING = 0x04;
	public final static int JOB_STEP_COMPLETE = 0x05;
	public final static int JOB_CANCELLED = 0x06;
	public final static int JOB_FINISHED = 0x07;
	public final static int JOB_EXCEPTION = 0x08;

	public final static int RESULT_SUCCESS = 0x010000;
	public final static int RESULT_FAILED = 0x010001;
	
	public final static int HTTP_GET = 0x001;
	public final static int HTTP_POST = 0x002;
	
	public final static int STEP_RESULT_COMPLETE = 0x0100;
	public final static int STEP_RESULT_INVALID = 0x0101;
	
}
