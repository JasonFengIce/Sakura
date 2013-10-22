package cn.ismartv.speedtester.models;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.ismartv.speedtester.AppConstant;
import cn.ismartv.speedtester.facilities.Export;

import com.google.gson.annotations.Expose;


import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;


/**
 * NetworkSpeedInfo encapsulates network information for downloading files
 * @author bob
 *
 */
public class NetworkSpeedInfo {
	
	public final static int FLAG_NORMAL = 0;
	public final static int FLAG_STOP = -1;
	
	public int filesize;
	public int filesizeFinished;
	@Export
	public float speed;
	public long timeStarted;
	public long timeEscalpsed;
	@Expose
	@Export
	public String pk;
	@Expose
	public String title;
	@Expose
	public String url;
	public String realUrl;
	@Expose
	public long length;
	public int stopflag = FLAG_NORMAL;
	
	@Expose
	public boolean display = true;
	@Override
	public String toString(){
		return "pk: "+pk+", title: "+title+"\nurl: "+url+"\n length: "+length+", display: "+ display;
	}
	
	public int download(Handler handler, int step) throws MalformedURLException, IOException {
		URL fileUrl = null;
		HttpURLConnection conn = null;
		InputStream stream = null;
		try {
			fileUrl = new URL(url);
			conn = (HttpURLConnection) fileUrl.openConnection();
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);
			realUrl = conn.getURL().toString();
			filesize = conn.getContentLength();
			stream = conn.getInputStream();
			timeStarted = SystemClock.uptimeMillis();
			handler.sendMessage(Message.obtain(handler, AppConstant.RESULT_SUCCESS, AppConstant.JOB_STEP_START, step));
			handler.sendMessageDelayed(Message.obtain(handler, AppConstant.RESULT_SUCCESS, AppConstant.JOB_STEP_COMPLETE, step, this), length);
			int actuallyReadCount = 0;
			byte[] buff = new byte[128];
			while((actuallyReadCount = stream.read(buff)) != -1 && stopflag == FLAG_NORMAL) {
				filesizeFinished += actuallyReadCount;
			}
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(conn != null) {
				try {
					conn.disconnect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return AppConstant.STEP_RESULT_COMPLETE;
	}
}
