package com.comic.chhreader.detail;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.comic.chhreader.Loge;
import com.comic.chhreader.utils.CHHNetUtils;
import com.comic.chhreader.utils.DataBaseUtils;

public abstract class HtmlParser extends AsyncTask<Void, Void, String> {

	private String mUrl;
	private Context mContext;
	public static String Js2JavaInterfaceName = "JsUseJava";
	public List<String> imgUrls = new ArrayList<String>();
	public String mImageSet;

	public HtmlParser(Context context, String url) {
		mUrl = url;
		mContext = context;
	}

	@Override
	protected String doInBackground(Void... params) {

		String body = CHHNetUtils.getContentBody(mContext, mUrl);

		if (body.isEmpty()) {
			return "";
		}

		String result = "";
		try {
			InputStream in = mContext.getResources().getAssets().open("head.html");
			int lenght = in.available();
			byte[] buffer = new byte[lenght];
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = result + body;

		Document doc = null;
		imgUrls.clear();

		doc = Jsoup.parse(result);

		if (doc == null)
			return null;

		Elements es = doc.select("script");
		if (es != null) {
			es.remove();
		}
		handleImageClickEvent(doc);
		removeHyperlinks(doc);
		String htmlText = handleDocument(doc);

		StringBuilder imageSetBuilder = new StringBuilder("");
		for (int i = 0; i < imgUrls.size(); i++) {
			imageSetBuilder.append(imgUrls.get(i));
			if (i < (imgUrls.size() - 1)) {
				imageSetBuilder.append("&000&");
			}
		}
		Loge.d("image set: " + imageSetBuilder.toString());

		DataBaseUtils.updateContentData(mContext, mUrl, htmlText, imageSetBuilder.toString());

		return htmlText;
	}

	public List<String> getImgUrls() {
		return imgUrls;
	}

	private void handleImageClickEvent(Document doc) {

		Elements es = doc.getElementsByTag("img");

		for (Element e : es) {
			String imgUrl = e.attr("src");
			imgUrls.add(imgUrl);
			String imgName;
			File file = new File(imgUrl);
			imgName = file.getName();
			if (imgName.endsWith(".gif")) {
				e.remove();
			} else {
				String filePath = "file://" + Environment.getExternalStorageDirectory().getPath() + "/ChhReader/Cache/SUB/" + imgName;
				e.attr("src", "file:///android_asset/temp_img.png");
				e.attr("src_link", filePath);
				e.attr("ori_link", imgUrl);
				String str = "window." + Js2JavaInterfaceName + ".setImgSrc('" + filePath + "')";
				e.attr("onclick", str);
			}
		}
	}

	private void removeHyperlinks(Document doc) {
		Elements hrefs = doc.getElementsByTag("a");
		for (Element href : hrefs) {
			href.removeAttr("href");
		}
	}

	protected abstract String handleDocument(Document doc);

	protected abstract void excuteEnd(String result);

	@Override
	protected void onPostExecute(String result) {
		excuteEnd(result);
		super.onPostExecute(result);
	}

}
