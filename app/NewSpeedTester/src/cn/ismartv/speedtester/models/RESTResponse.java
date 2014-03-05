package cn.ismartv.speedtester.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;

public class RESTResponse {
	
	public int resCode;
	
	public Throwable err;
	
	public String content;
	
	public <T> T getObject(Class<T> clazz, TypeAdapter<T> adapter) {
		Gson gson = null;
		if(adapter != null) {
			gson = new GsonBuilder().registerTypeAdapter(clazz, adapter).create();
		} else {
			gson = new Gson();
		}
		try {
			return gson.fromJson(this.content, clazz);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
}
