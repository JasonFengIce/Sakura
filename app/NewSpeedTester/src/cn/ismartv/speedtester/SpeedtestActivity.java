package cn.ismartv.speedtester;

import java.util.HashMap;

import cn.ismartv.speedtester.facilities.RESTLoader;
import cn.ismartv.speedtester.models.RESTResponse;
import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.view.Menu;

public class SpeedtestActivity extends Activity implements LoaderCallbacks<RESTResponse> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speedtest);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.speedtest, menu);
		return true;
	}

	@Override
	public Loader<RESTResponse> onCreateLoader(int id, Bundle args) {
		int method = args.getInt("method");
		String url = args.getString("url");
		Object obj = args.getSerializable("params");
		HashMap<String, String> params = null;
		if(obj != null) {
			params = (HashMap<String, String>) obj;
		}
		return new RESTLoader(this, method, url, params);
	}

	@Override
	public void onLoadFinished(Loader<RESTResponse> loader, RESTResponse data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<RESTResponse> loader) {
		// TODO Auto-generated method stub
		
	}

}
