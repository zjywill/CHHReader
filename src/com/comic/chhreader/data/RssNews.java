package com.comic.chhreader.data;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.comic.chhreader.Loge;

public class RssNews {

	public String title;
	public String link;
	public String imageurl;
	public String description;

	public static List<RssNews> getRssNews(String data) {
		List<RssNews> newsList = new ArrayList<RssNews>();
		try {
			XmlPullParserFactory factory;

			factory = XmlPullParserFactory.newInstance();

			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(new StringReader(data));

			parser.nextTag();
			parser.nextTag();

			while (parser.nextTag() == XmlPullParser.START_TAG) {
				String name1 = parser.getName();
				if (name1.equals("item")) {
					RssNews item = new RssNews();
					while (parser.nextTag() == XmlPullParser.START_TAG) {
						String name2 = parser.getName();
						if (name2.equals("title")) {
							item.title = parser.nextText();
							Loge.i("RssNews title: " + item.title);
						} else if (name2.equals("link")) {
							item.link = parser.nextText();
							Loge.d("RssNews link: " + item.link);
						} else if (name2.equals("description")) {
							item.description = parser.nextText();
							item.imageurl = item.description.substring(0, item.description.lastIndexOf("\"/>"));
							item.description = item.description.substring(item.description.lastIndexOf("/>") + 2);
							item.imageurl = item.imageurl.replace("<img src=\"", "");
							Loge.d("RssNews description: " + item.description);
							Loge.d("RssNews imageurl: " + item.imageurl);
						} else {
							skipUnknownTag(parser);
						}
					}
					newsList.add(item);
				} else {
					skipUnknownTag(parser);
				}
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newsList;
	}

	private static void skipUnknownTag(XmlPullParser parser) {
		try {
			String name1 = parser.getName();
			while (parser.next() > 0) {
				if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(name1)) {
					break;
				}
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
