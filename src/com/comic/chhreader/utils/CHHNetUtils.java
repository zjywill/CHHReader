package com.comic.chhreader.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.comic.chhreader.Loge;
import com.comic.chhreader.data.ContentData;
import com.comic.chhreader.data.SubItemData;
import com.comic.chhreader.data.TopicData;

public class CHHNetUtils {

	private static HttpClient sHttpClient = null;

	public static final int HTTP_TIMEOUT = 60000;

	public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android " + android.os.Build.VERSION.RELEASE + ";" + Locale.getDefault().toString() + "; " + android.os.Build.DEVICE + "/" + android.os.Build.ID + ")";

	public static Object postResult(String api_url,
			HashMap<String, Object> params, String access_token) {
		final HttpPost post = new HttpPost(api_url);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		if (access_token != null)
			params.put("access_token", access_token);
		String queryParams = buildParams(params, "&");
		final String entityString = queryParams;
		Loge.d("postResult entityString: " + entityString);
		if (sHttpClient == null) {
			initHttpClient();
		}
		HttpEntity entity;
		try {
			entity = new StringEntity(entityString);
			post.setEntity(entity);
			final HttpResponse r = sHttpClient.execute(post);
			return getParseResult(r);
		} catch (UnknownHostException e) {
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (ConcurrentModificationException e) {
		} catch (JSONException e) {
		}
		return null;
	}

	public static String buildParams(HashMap<String, Object> params,
			String splitter) {
		StringBuffer buf = new StringBuffer();
		if (params == null) {
			params = new HashMap<String, Object>();
		}
		Iterator<String> itrs = params.keySet().iterator();
		List<String> sortKeyList = new ArrayList<String>();
		while (itrs.hasNext()) {
			sortKeyList.add(itrs.next());
		}
		Collections.sort(sortKeyList);
		for (String key : sortKeyList) {
			if (buf.length() != 0) {
				buf.append(splitter);
			}
			buf.append(key).append("=");
			buf.append(params.get(key));
		}
		return buf.toString();
	}

	private static void initHttpClient() {
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setUserAgent(params, USER_AGENT);

		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);

		final SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory.setHostnameVerifier(new AllowAllHostnameVerifier());
		registry.register(new Scheme("https", sslSocketFactory, 443));

		final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);

		sHttpClient = new DefaultHttpClient(manager, params);
	}

	public static Object getParseResult(HttpResponse r) throws JSONException,
			IOException {
		Header header = r.getFirstHeader("WWW-Authenticate");
		if (header != null) {
			String value = header.getValue();
			Loge.e("header value=" + value);
			if (value.contains("expired_token")) {
				Loge.e("expired_token");
			}
			if (value.contains("invalid_token")) {
				Loge.e("invalid_token");
			}
		}
		final int status = r.getStatusLine().getStatusCode();
		Loge.d("status=" + status);

		if (status != HttpURLConnection.HTTP_OK && status != HttpURLConnection.HTTP_CREATED) {
			String content = getResponse(r.getEntity());
			if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
				if (isRefreshTokenExpired(content)) {
					Loge.e("SESSION_ESPIRED");
					return null;
				}
			}
			Loge.e("SERVER_ERROR");
			return null;
		}
		String content = getResponse(r.getEntity());

		// Loge.d("getParseResult " + content);

		if ("{}".equals(content)) {
			return null;
		} else if (content.startsWith("{") && content.endsWith("}")) {
			Loge.d("return JSONObject");
			final JSONObject obj = new JSONObject(content);
			return obj;
		} else if (content.startsWith("[") && content.endsWith("]")) {
			Loge.d("return JSONArray");
			return new JSONArray(content);
		} else if (content.startsWith("<") && content.endsWith(">")) {
			return content;
		} else if (content.startsWith("\"") && content.endsWith("\"")) {
			Loge.d("return String");
			content = content.substring(1, content.length() - 1);
			return content;
		} else {
			return content;
		}
	}

	private static String getResponse(HttpEntity entity)
			throws UnsupportedEncodingException, IllegalStateException,
			IOException {
		String response = "";

		int length = (int) entity.getContentLength();

		if (length < 1) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			entity.writeTo(out);
			return out.toString();
		}
		StringBuffer sb = new StringBuffer(length);
		InputStreamReader isr;

		if (entity.getContentEncoding() != null && entity.getContentEncoding().getValue().equals("gzip")) {
			isr = new InputStreamReader(new GZIPInputStream(entity.getContent()), "UTF-8");
		} else {
			isr = new InputStreamReader(entity.getContent(), "UTF-8");
		}

		char buff[] = new char[length];
		int cnt;
		while ((cnt = isr.read(buff, 0, length - 1)) > 0) {
			sb.append(buff, 0, cnt);
		}

		response = sb.toString();
		isr.close();
		sb = null;
		buff = null;

		return response;
	}

	private static boolean isRefreshTokenExpired(String content)
			throws JSONException {
		if (content.startsWith("{") && content.endsWith("}")) {
			final JSONObject obj = new JSONObject(content);
			if (!obj.isNull("error")) {
				String error = obj.getString("error");
				if ("invalid_grant".equals(error)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Object getResult(String api_url,
			HashMap<String, Object> params, String access_token) {

		Loge.d("api_url = " + api_url);

		if (params == null) {
			params = new HashMap<String, Object>();
		}
		if (access_token != null) {
			params.put("access_token", access_token);
		}

		String queryParams = buildParams(params, "&");
		final String entityString = queryParams;

		StringBuffer requestUrl = new StringBuffer(api_url);
		if (entityString != null && !entityString.isEmpty()) {
			requestUrl.append("?");
			requestUrl.append(entityString);
		}

		Loge.d("requestUrl = " + requestUrl.toString());

		HttpGet get = new HttpGet(requestUrl.toString());

		if (sHttpClient == null) {
			initHttpClient();
		}
		try {
			final HttpResponse r = sHttpClient.execute(get);
			return getParseResult(r);
		} catch (UnknownHostException e) {
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (ConcurrentModificationException e) {
		} catch (JSONException e) {
		}
		return null;
	}

	// TODO
	// ----------------------------DIVIDER---------------------------------------------

	public static ArrayList<TopicData> getTopicsDate(Context context) {
		ArrayList<TopicData> topicsData = null;
		Object obj = getResult("http://chiphell.sinaapp.com/chiphell/topics", null, null);
		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject itemObject = jsonArray.getJSONObject(i);
					String pk = itemObject.getString("pk");
					JSONObject fieldsObject = itemObject.getJSONObject("fields");
					String name = fieldsObject.getString("name");
					if (topicsData == null) {
						topicsData = new ArrayList<TopicData>();
					}
					TopicData itemData = new TopicData();
					itemData.mName = name;
					itemData.mPk = Integer.parseInt(pk);
					itemData.mSelected = false;
					topicsData.add(itemData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (topicsData != null && topicsData.size() > 0) {
			Loge.d("topicsData fetch ok");
		}
		return topicsData;
	}

	public static ArrayList<SubItemData> getAllSubItemsDate(Context context) {
		Object obj = getResult("http://chiphell.sinaapp.com/chiphell/allsubitem", null, null);
		ArrayList<SubItemData> subItemDatas = null;
		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject itemObject = jsonArray.getJSONObject(i);
					String pk = itemObject.getString("pk");
					JSONObject fieldsObject = itemObject.getJSONObject("fields");
					String name = fieldsObject.getString("name");
					String sourcelink = fieldsObject.getString("sourcelink");
					String topic = fieldsObject.getString("topic");
					if (subItemDatas == null) {
						subItemDatas = new ArrayList<SubItemData>();
					}
					SubItemData itemData = new SubItemData();
					itemData.mName = name;
					itemData.mUrl = sourcelink;
					itemData.mPk = Integer.parseInt(pk);
					itemData.mTopic = Integer.parseInt(topic);
					subItemDatas.add(itemData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (subItemDatas != null && subItemDatas.size() > 0) {
			Loge.d("subItemsDate fetch ok");
		}
		return subItemDatas;
	}

	public static ArrayList<SubItemData> getSubItemsDate(Context context,
			int pknum) {

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("t", pknum);
		Object obj = getResult("http://chiphell.sinaapp.com/chiphell/items", params, null);
		ArrayList<SubItemData> subItemDatas = null;
		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject itemObject = jsonArray.getJSONObject(i);
					String pk = itemObject.getString("pk");
					JSONObject fieldsObject = itemObject.getJSONObject("fields");
					String name = fieldsObject.getString("name");
					String sourcelink = fieldsObject.getString("sourcelink");
					String topic = fieldsObject.getString("topic");
					if (subItemDatas == null) {
						subItemDatas = new ArrayList<SubItemData>();
					}
					SubItemData itemData = new SubItemData();
					itemData.mName = name;
					itemData.mUrl = sourcelink;
					itemData.mPk = Integer.parseInt(pk);
					itemData.mTopic = Integer.parseInt(topic);
					subItemDatas.add(itemData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (subItemDatas != null && subItemDatas.size() > 0) {
			Loge.d("subItemsDate fetch ok");
		}
		return subItemDatas;
	}

	public static ArrayList<ContentData> getMainContentItemsDate(Context context) {
		Object obj = getResult("http://chiphell.sinaapp.com/chiphell/maindatas", null, null);
		ArrayList<ContentData> contentDatas = null;
		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject itemObject = jsonArray.getJSONObject(i);
					JSONObject fieldsObject = itemObject.getJSONObject("fields");
					String title = fieldsObject.getString("name");
					String imageurl = fieldsObject.getString("imageurl");
					String link = fieldsObject.getString("link");
					String pk = fieldsObject.getString("item");
					String content = fieldsObject.getString("content");
					String date = fieldsObject.getString("postdate");
					boolean valid = fieldsObject.getBoolean("is_valid");

					ContentData itemData = new ContentData();
					itemData.mTitle = title;
					itemData.mLink = link;
					itemData.mImageUrl = imageurl;
					itemData.mContent = content;
					itemData.mSubItemType = Integer.parseInt(pk);
					itemData.mValid = valid;
					itemData.mPostDate = Long.parseLong(date);
					if (contentDatas == null) {
						contentDatas = new ArrayList<ContentData>();
					}
					contentDatas.add(itemData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (contentDatas != null && contentDatas.size() > 0) {
			Loge.d("contentDatas fetch ok");
		}
		return contentDatas;
	}

	public static ArrayList<ContentData> getContentItemsDate(Context context,
			int topic, int pknum, int page) {

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("i", pknum);
		params.put("p", page);
		Object obj = getResult("http://chiphell.sinaapp.com/chiphell/datas", params, null);
		ArrayList<ContentData> contentDatas = null;
		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject itemObject = jsonArray.getJSONObject(i);
					JSONObject fieldsObject = itemObject.getJSONObject("fields");
					String title = fieldsObject.getString("name");
					String imageurl = fieldsObject.getString("imageurl");
					String link = fieldsObject.getString("link");
					String pk = fieldsObject.getString("item");
					String content = fieldsObject.getString("content");
					String date = fieldsObject.getString("postdate");
					boolean valid = fieldsObject.getBoolean("is_valid");

					ContentData itemData = new ContentData();
					itemData.mTitle = title;
					itemData.mLink = link;
					itemData.mImageUrl = imageurl;
					itemData.mContent = content;
					itemData.mSubItemType = Integer.parseInt(pk);
					itemData.mTopicType = topic;
					itemData.mValid = valid;
					itemData.mPostDate = Long.parseLong(date);
					if (contentDatas == null) {
						contentDatas = new ArrayList<ContentData>();
					}
					contentDatas.add(itemData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (contentDatas != null && contentDatas.size() > 0) {
			Loge.d("contentDatas fetch ok");
		}
		return contentDatas;
	}
	
	public static ArrayList<ContentData> getDatasbypage(Context context, int page) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("p", page);
		Object obj = getResult("http://chiphell.sinaapp.com/chiphell/getdatasbypage", params, null);
		Loge.d("getDatasbypage: " + obj.toString());
		ArrayList<ContentData> contentDatas = null;
		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject itemObject = jsonArray.getJSONObject(i);
					JSONObject fieldsObject = itemObject.getJSONObject("fields");
					String title = fieldsObject.getString("name");
					String imageurl = fieldsObject.getString("imageurl");
					String link = fieldsObject.getString("link");
					String pk = fieldsObject.getString("item");
					String content = fieldsObject.getString("content");
					String date = fieldsObject.getString("postdate");
					boolean valid = fieldsObject.getBoolean("is_valid");

					ContentData itemData = new ContentData();
					itemData.mTitle = title;
					itemData.mLink = link;
					itemData.mImageUrl = imageurl;
					itemData.mContent = content;
					itemData.mSubItemType = Integer.parseInt(pk);
					itemData.mValid = valid;
					itemData.mPostDate = Long.parseLong(date);
					if (contentDatas == null) {
						contentDatas = new ArrayList<ContentData>();
					}
					contentDatas.add(itemData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (contentDatas != null && contentDatas.size() > 0) {
			Loge.d("contentDatas fetch ok");
		}
		return contentDatas;
	}

	public static String getContentBody(Context context, String url) {
		String body = "";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("link", url);
		Object obj = getResult("http://chiphell.sinaapp.com/chiphell/getcontent", params, null);
		if (obj instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) obj;
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					JSONObject itemObject = jsonArray.getJSONObject(i);
					JSONObject fieldsObject = itemObject.getJSONObject("fields");
					body = fieldsObject.getString("content");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return body;
	}

}
