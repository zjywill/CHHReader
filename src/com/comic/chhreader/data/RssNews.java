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
	public long posttime;

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

			int itemId = 1;
			while (parser.nextTag() == XmlPullParser.START_TAG) {
				String name1 = parser.getName();
				if (name1.equals("item")) {
					RssNews item = new RssNews();
					item.posttime = itemId;
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
							Loge.d("RssNews item.description: " + item.description);
							String tempdes = item.description;
							int startpos = tempdes.indexOf("<img");
							if (startpos >= 0) {
								if (startpos + 4 < tempdes.length())
									tempdes = tempdes.substring(startpos + 4, tempdes.length());
								int startpos2 = tempdes.indexOf("src=");
								if (startpos2 >= 0) {
									if (startpos2 + 4 < tempdes.length())
										tempdes = tempdes.substring(startpos2 + 4, tempdes.length());
									int jpgindex = tempdes.indexOf(".jpg");
									if (jpgindex > 0) {
										if (jpgindex + 4 < tempdes.length())
											tempdes = tempdes.substring(0, jpgindex + 4);
									} else {
										int gifindex = tempdes.indexOf(".gif");
										if (gifindex > 0) {
											if (gifindex + 4 < tempdes.length())
												tempdes = tempdes.substring(0, gifindex + 4);
										}
									}
									item.imageurl = tempdes.replaceAll("\"", "");
								}
							}
							Loge.d("RssNews imageurl: " + item.imageurl);
							itemId++;
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
