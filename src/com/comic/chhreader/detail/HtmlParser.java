package com.comic.chhreader.detail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.webkit.WebView;

import com.comic.chhreader.Loge;

public abstract class HtmlParser extends AsyncTask<Void, Void, String> {

	private String mData;
	private WebView webView;
	private Context mContext;
	public static String Js2JavaInterfaceName = "JsUseJava";
	public List<String> imgUrls = new ArrayList<String>();

	public HtmlParser(WebView wevView, String data, Context context) {
		this.webView = wevView;
		mData = data;
		mContext = context;
	}

	@Override
	protected String doInBackground(Void... params) {

		Document doc = null;
		imgUrls.clear();

		doc = Jsoup.parse(mData);

		if (doc == null)
			return null;

		Elements es = doc.select("script");
		if (es != null) {
			es.remove();
		}
		handleImageClickEvent(doc);
		removeHyperlinks(doc);
		String htmlText = handleDocument(doc);

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
				String filePath = "file://" + Environment.getExternalStorageDirectory().getPath()
						+ "/ChhReader/Cache/SUB/" + imgName;
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

	@Override
	protected void onPostExecute(String result) {
		webView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);
		super.onPostExecute(result);
	}

}
