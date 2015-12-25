package cn.ismartv.iris.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.commons.lang3.StringUtils;
//import tv.ismar.daisy.exception.ItemOfflineException;
//import tv.ismar.daisy.exception.NetworkException;
//import tv.ismar.daisy.models.*;

public class SimpleRestClient {
	// public String root_url = "http://cord.tvxio.com";
	// public String root_url = "http://127.0.0.1:21098/cord";

	// public static String sRoot_url = "http://127.0.0.1:21098/cord";

	public static String root_url = "";
	// public static String sRoot_url = "http://cord.tvxio.com/v2_0/A21/dto";
	public static String sRoot_url = "http://v2.sky.tvxio.com/v2_0/SKY/dto";
	public static String ad_domain = "lilac.t.tvxio.com";
	public static String log_domain = "cord.tvxio.com";
	public static String device_token;
	public static String sn_token;
	public static String access_token = "";
	public static String mobile_number = "";
	public static int appVersion;
	public static String app = "SKY";
	public static int densityDpi;
	public static int screenWidth;
	public static int screenHeight;

	private Gson gson;
//    private GetDataTask getdatatask;

	public SimpleRestClient() {
		GsonBuilder gsonBuilder = new GsonBuilder();
//		gsonBuilder.registerTypeAdapter(Attribute.class,
//				new AttributeDeserializer());
		gson = gsonBuilder.create();
	}

//	public Item[] getItems(String str) {
//		return gson.fromJson(str, Item[].class);
//
//	}
//
//	public Item getItemRecord(String str) {
//		return gson.fromJson(str, Item.class);
//	}

	public static String readContentFromPost(String url, String sn) {
		StringBuffer response = new StringBuffer();
		try {
			URL postUrl = new URL("http://peach.tvxio.com/trust/" + url + "/");
			HttpURLConnection connection = (HttpURLConnection) postUrl
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", "application/json");
			connection.connect();
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			String content;
			if (url.equals("active"))
				content = "sn=" + sn + "&kind=a21&"
						+ "manufacture=lenovo&version=v2_0";
			else
				content = "sn=" + sn + "&kind=a21&"
						+ "manufacture=lenovo&api_version=v2_0";
			out.writeBytes(content);

			out.flush();
			out.close();
			int status = connection.getResponseCode();
			if (status == 200) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								"UTF-8"));
				out.flush();
				out.close(); // flush and close
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();
				if (url.equals("register") && line == null) {
					connection.disconnect();
					return "200";
				}
			} else {
				connection.disconnect();
				return "";
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		return response.toString();
	}

	/**
	 * Extract the item id from given url, check whether the given url is an
	 * subitem.
	 * 
	 * @param url
	 *            is the valid url contains item id(or item pk)
	 * @param isSubItem
	 *            is an boolean array with a size of one, use to gain the result
	 *            of subitem check.
	 * @return a item id.
	 */
	public static int getItemId(String url, boolean[] isSubItem) {
		int id = 0;
		try {
			if (url.contains("/item/")) {
				isSubItem[0] = false;
			} else {
				isSubItem[0] = true;
			}
			Pattern p = Pattern.compile("/(\\d+)/?$");
			Matcher m = p.matcher(url);
			if (m.find()) {
				String idStr = m.group(1);
				if (idStr != null) {
					id = Integer.parseInt(idStr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

//	public ContentModelList getContentModelLIst(String url) {
//		try {
//			String jsonStr = NetworkUtils.getJsonStr(root_url + url, "");
//			return gson.fromJson(jsonStr, ContentModelList.class);
//		} catch (JsonSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ItemOfflineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NetworkException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}

//	public ContentModelList getContentModelList(InputStream in) {
//		return gson.fromJson(new InputStreamReader(in), ContentModelList.class);
//	}
//
//	public ChannelList getChannelList() {
//		try {
//			String api = "/api/tv/channels/";
//			String jsonStr = NetworkUtils.getJsonStr(root_url + api, "");
//			return gson.fromJson(jsonStr, ChannelList.class);
//		} catch (JsonSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ItemOfflineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NetworkException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public SectionList getSectionsByChannel(String channel)
//			throws NetworkException {
//		try {
//			String url = root_url + "/api/tv/sections/" + channel + "/";
//			String jsonStr = NetworkUtils.getJsonStr(url, "");
//			SectionList list = gson.fromJson(jsonStr, SectionList.class);
//			return list;
//		} catch (JsonSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ItemOfflineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}

//	public HomePagerEntity getVaietyHome(String url) throws NetworkException {
//		HomePagerEntity entity = null;
//
//		try {
//			String jsonStr = NetworkUtils.getJsonStr(url, "");
//			entity = gson.fromJson(jsonStr, HomePagerEntity.class);
//			return entity;
//		} catch (JsonSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ItemOfflineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return entity;
//	}
//
//	public HomePagerEntity getSportHome(String url) throws NetworkException {
//		HomePagerEntity entity = null;
//		try {
//			String jsonStr = NetworkUtils.getJsonStr(url, "");
//			if(StringUtils.isEmpty(jsonStr))
//				throw new NetworkException("空数据");
//			entity = gson.fromJson(jsonStr, HomePagerEntity.class);
//			return entity;
//		} catch (JsonSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ItemOfflineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return entity;
//	}
//
//	public ArrayList<SportGame> getSportGames(String path)
//			throws NetworkException {
//		ArrayList<SportGame> arrays = new ArrayList<SportGame>();
//		try {
//			String url = root_url + path;
//			String jsonStr = NetworkUtils.getJsonStr(url, "");
//			if(StringUtils.isEmpty(jsonStr))
//				throw new NetworkException("空数据");
//			JSONObject rootObject = new JSONObject(jsonStr);
//			JSONArray livingArray = rootObject.getJSONArray("living");
//
//			for (int i = 0; i < livingArray.length(); i++) {
//				SportGame sports = new SportGame();
//				JSONObject object = livingArray.getJSONObject(i);
//				sports.setStart_time(object.getString("start_time"));
//				sports.setExpiry_date(object.getString("expiry_date"));
//				sports.setName(object.getString("name"));
//				sports.setImageurl(object.getString("poster_url"));
//				sports.setUrl(object.getString("url"));
//				sports.setIs_complex(object.getBoolean("is_complex"));
//				sports.setLiving(true);
//				arrays.add(sports);
//			}
//			if (rootObject.has("highlight")) {
//				JSONArray highlight = rootObject.getJSONArray("highlight");
//				for (int i = 0; i < highlight.length(); i++) {
//					SportGame sports = new SportGame();
//					JSONObject object = highlight.getJSONObject(i);
//					sports.setName(object.getString("title"));
//					sports.setImageurl(object.getString("image"));
//					sports.setUrl(object.getString("url"));
//					sports.setIs_complex(object.getBoolean("is_complex"));
//					sports.setLiving(false);
//					arrays.add(sports);
//				}
//			}
//		} catch (JsonSyntaxException e) {
//			e.printStackTrace();
//		} catch (ItemOfflineException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return arrays;
//	}
//
//	public SectionList getSections(String url) throws NetworkException,
//			ItemOfflineException {
//		if (!(url.contains("https") || url.contains("http"))) {
//			url = root_url + url;
//		}
//		try {
//			String jsonStr = NetworkUtils.getJsonStr(url, "");
//			SectionList list = gson.fromJson(jsonStr, SectionList.class);
//			return list;
//		} catch (JsonSyntaxException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public SectionList getsectionss(String content) {
//		SectionList list = gson.fromJson(content, SectionList.class);
//		return list;
//	}
//
//	public ItemList getItemList(String url) throws NetworkException,
//			ItemOfflineException {
//		try {
//			if (!(url.contains("https") || url.contains("http"))) {
//				url = root_url + url;
//			}
//			String jsonStr = NetworkUtils.getJsonStr(url, "");
//			ItemList list = gson.fromJson(jsonStr, ItemList.class);
//			return list;
//		} catch (JsonSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public Item getItem(String url) throws ItemOfflineException,
//			NetworkException, JsonSyntaxException {
//		if (!(url.contains("https") || url.contains("http"))) {
//			url = root_url + url;
//		}
//		String jsonStr = NetworkUtils.getJsonStr(url, "");
//		// Log.d("Item is", jsonStr);
//
//		return gson.fromJson(jsonStr, Item.class);
//	}
//
//	public Item[] getRelatedItem(String api) throws NetworkException {
//		try {
//			String jsonStr = NetworkUtils.getJsonStr(root_url + api, "");
//			return gson.fromJson(jsonStr, Item[].class);
//		} catch (JsonSyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ItemOfflineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public void doSendRequest(String url, String method, String params,
//			HttpPostRequestInterface l) {
//		// NetworkUtils.getJsonStrByPost(url, "");
//		RequestParams q = new RequestParams();
//		handler = l;
//		if (!(url.contains("https") || url.contains("http"))) {
//			q.url = root_url + url;
//		} else {
//			q.url = url;
//		}
//		q.values = params;
//		q.method = method;
//		if(getdatatask != null && getdatatask.getStatus() != AsyncTask.Status.FINISHED){
//			getdatatask.cancel(true);
//			getdatatask = null;
//		}
//		getdatatask = new GetDataTask();
//		getdatatask.execute(q);
//	}

//	public void doTopicRequest(String url, String method, String params,
//			HttpPostRequestInterface l) {
//		// NetworkUtils.getJsonStrByPost(url, "");
//		RequestParams q = new RequestParams();
//		handler = l;
//		if (!(url.contains("https") || url.contains("http"))) {
//			q.url = url;
//		} else {
//			q.url = url;
//		}
//		q.values = params;
//		q.method = method;
//		if(getdatatask != null && getdatatask.getStatus() != AsyncTask.Status.FINISHED){
//			getdatatask.cancel(true);
//			getdatatask = null;
//		}
//		getdatatask = new GetDataTask();
//		getdatatask.execute(q);
//	}

//	class GetDataTask extends AsyncTask<RequestParams, Void, String> {
//
//		@Override
//		protected void onPreExecute() {
//			// TODO Auto-generated method stub
//			super.onPreExecute();
//			if (handler != null) {
//				handler.onPrepare();
//			}
//		}
//
//		@Override
//		protected String doInBackground(RequestParams... params) {
//			String jsonStr = "";
//
//			RequestParams p = params[0];
//			String url = p.url;
//			String values = p.values;
//			String method = p.method;
//			try {
//				if ("post".equalsIgnoreCase(method)) {
//					if (url.contains("https")) {
//						jsonStr = NetworkUtils.httpsRequestHttps(url, values);
//					} else {
//						jsonStr = NetworkUtils.getJsonStrByPost(url, values);
//					}
//				} else {
//					jsonStr = NetworkUtils.getJsonStr(url, values);
//				}
//			} catch (ItemOfflineException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				jsonStr = e.getUrl();
//			} catch (NetworkException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				jsonStr = e.getUrl();
//			}
//
//			return jsonStr;
//		}

//		@Override
//		protected void onPostExecute(String result) {
//			if (handler != null && result != null) {
//				if ("".equals(result)) {
//					handler.onFailed("网络异常");
//				} else if ("200".equals(result)) {
//					handler.onSuccess(result);
//				} else if ("406".equals(result)) {
//					handler.onFailed("device_token非标准格式 ");
//				} else if ("400".equals(result)) {
//					handler.onFailed("参数不对 ");
//				} else if ("404".equals(result)) {
//					handler.onFailed("404 NOT FOUND");
//				} else if ("599".equals(result)) {
//					handler.onFailed("599 连接错误");
//				} else if (!"".equals(result)) {
//					handler.onSuccess(result);
//				}
//			}
//		}
//
//	}
//
//	public void removeAsycCallBack(){
//		if(getdatatask != null && getdatatask.getStatus() != AsyncTask.Status.FINISHED){
//			getdatatask.cancel(true);
//		}
//	}
//
//	public class RequestParams {
//		public String url;
//		public String values;
//		public String method;
//	}
//
//	public void setHttpPostRequestInterface(HttpPostRequestInterface l) {
//		handler = l;
//	}
//
//	private HttpPostRequestInterface handler;
//
//	public interface HttpPostRequestInterface {
//		public void onPrepare();
//
//		public void onSuccess(String info);
//
//		public void onFailed(String error);
//	}
//
//	public static boolean isLogin() {
//		if ("".equals(SimpleRestClient.access_token)) {
//			return false;
//		}
//		return true;
//	}
}
