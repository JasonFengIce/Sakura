package tv.ismar.speedtester.facilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

public class NetworkUtils {
	
	public static final String CHARSET = "UTF-8"; 

	public static final String TAG = "NetworkUtils";

	public static String getContent(String target, HashMap<String, String> params) throws IOException {
		if(params != null) {
			String queryStr = getQueryString(params);
			target = target + "?" + queryStr + "&timestamp=" + System.currentTimeMillis();
		} else {
			/* Add time-stamp to prevent cache */
			target += "?timestamp=" + System.currentTimeMillis();
		}
		HttpURLConnection connect = null;
		BufferedReader buff = null;
		try {
			URL url = new URL(target);
			connect = (HttpURLConnection) url.openConnection();
			connect.setReadTimeout(20000);
			connect.setConnectTimeout(20000);
			StringBuffer sb = new StringBuffer();
//			conn.addRequestProperty("User-Agent", UA);
//			conn.addRequestProperty("Accept", "application/json");
			
			InputStream in = connect.getInputStream();
			buff = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while((line=buff.readLine())!=null) {
				sb.append(line);
			}
			return sb.toString();
		} finally {
			if(buff!=null) {
				buff.close();
			}
			if(connect!=null) {
				connect.disconnect();
			}
		}
	}
	
	public static InputStream getInputStream(String target) {
		try {
			URL url = new URL(target);
			URLConnection conn = url.openConnection();
//			conn.addRequestProperty("User-Agent", UA);
//			conn.addRequestProperty("Accept", "application/json");
//			conn.connect();
			return conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String postContent(String target, HashMap<String, String> params) throws IOException {
		String queryStr = getQueryString(params);
		BufferedReader buff = null;
		HttpURLConnection connection = null;
		try {
			byte[] requestBody = queryStr.getBytes(CHARSET);
			URL url = new URL(target);
			connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(20000);
			connection.setConnectTimeout(20000);
			connection.setRequestProperty("Accept-Charset", CHARSET);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
			/* This will let connection use POST method. */
			connection.setDoOutput(true);
			connection.setFixedLengthStreamingMode(requestBody.length);
			OutputStream out = null;
			try {
				out = connection.getOutputStream();
				out.write(requestBody);
			} finally {
				if(out != null) {
					out.close();
				}
			}
			StringBuffer sb = new StringBuffer();
			InputStream in = connection.getInputStream();
			buff = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while((line=buff.readLine())!=null) {
				sb.append(line);
			}
			
			
			return sb.toString();
		} finally {
			if(buff!=null) {
				try {
					buff.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(connection!=null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getQueryString(HashMap<String, String> params) {
		StringBuilder strBuilder =  new StringBuilder();
		try {
			for(HashMap.Entry<String, String> entry: params.entrySet()) {
				if(entry.getValue()!=null) {
					strBuilder.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), CHARSET));
					strBuilder.append("&");
				}
			}
			if(strBuilder.length() > 0) {
				return strBuilder.deleteCharAt(strBuilder.length() - 1).toString();
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public static boolean urlEquals(String url1, String url2) {
		return removeRoot(url1).equals(removeRoot(url2));
	}
	
	public static String removeRoot(String url) {
		int start = url.indexOf("/api/");
		return url.substring(start, url.length());
	}

}
