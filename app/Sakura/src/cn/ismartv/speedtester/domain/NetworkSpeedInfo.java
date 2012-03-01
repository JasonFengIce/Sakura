package cn.ismartv.speedtester.domain;

import android.view.View;

/**
 * NetworkSpeedInfo encapsulates network information for downloading files
 * @author bob
 *
 */
public class NetworkSpeedInfo {
	public int filesize = 0;
	public int filesizeFinished = 0;
	public float speed = 0;
	public long timeStarted = 0;
	public long timeEscalpsed = 0;
	public String pk = null;
	public String title = null;
	public String url = null;
	public String realUrl = null;
	public long length;
	public int flagStop = 0;
	public boolean display = true;
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public boolean isDisplay() {
		return display;
	}
	public void setDisplay(boolean display) {
		this.display = display;
	}
	@Override
	public String toString(){
		return "pk: "+pk+", title: "+title+"\nurl: "+url+"\n length: "+length+", display: "+ display;
	}
}
