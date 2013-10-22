package cn.ismartv.speedtester.facilities;

import java.util.HashMap;

import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.models.RESTResponse;
import android.content.AsyncTaskLoader;
import android.content.Context;

public class RESTLoader extends AsyncTaskLoader<RESTResponse> {
	
	public static final int GET_URL_LOADER_ID = 0;
	public static final int POINT_LOADER_ID = 1;
	public static final int FEEDBACK_LOADER_ID = 2;
	
	private String[] mUrls = {"/customer/urls/", "/customer/points/","/customer/pointlogs/"};
	private int[] mMethods = {AppConstant.HTTP_GET, AppConstant.HTTP_GET, AppConstant.HTTP_POST};
	
	public int mId;
	
	public HashMap<String, String> mParams;
	
	public RESTLoader(Context context, int id, HashMap<String, String> params) {
		super(context);
		mId = id;
		mParams = params;
	}

	@Override
	public RESTResponse loadInBackground() {
		RESTResponse result = null;
		if( mMethods[mId] == AppConstant.HTTP_GET) {
			result = NetworkUtils.getContent(mUrls[mId], mParams);
		} else if(mMethods[mId] == AppConstant.HTTP_POST){
			result = NetworkUtils.postContent(mUrls[mId], mParams);
		}
		return result;
	}

}
