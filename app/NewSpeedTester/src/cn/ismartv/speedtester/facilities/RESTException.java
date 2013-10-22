package cn.ismartv.speedtester.facilities;

public class RESTException extends Exception {
	
	private int mStatusCode;

	public RESTException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RESTException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}
	
	public RESTException(String detailMessage, int statusCode) {
		super(detailMessage);
		mStatusCode = statusCode;
	}

	public RESTException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public RESTException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
