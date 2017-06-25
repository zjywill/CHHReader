package com.comic.hcreader.detail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.comic.hcreader.BitmapUtils;
import com.comic.hcreader.Loge;
import com.comic.hcreader.R;
import com.comic.hcreader.component.BasicActivity;
import com.comic.hcreader.dataservice.DataService;
import com.comix.overwatch.HiveProgressView;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tbruyelle.rxpermissions.RxPermissions;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DetailActivity extends BasicActivity {

  public static final String IMAGE_BREAK_TAG = "&00000&";

  private int id;
  private String url;
  private String title;
  private String externalCacheDirPath;
  private HiveProgressView progressView;
  private WebView webView;

  public List<String> imgUrls;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    try {
      externalCacheDirPath = DetailActivity.this.getExternalCacheDir().getPath();
    } catch (Exception e) {

    }
    initData();
    initToolbar();
    initViews();
    getContent();
  }

  private void initData() {
    Intent intent = getIntent();
    if (intent != null) {
      url = intent.getStringExtra("url");
      title = intent.getStringExtra("title");
      id = intent.getIntExtra("id", 0);
    }
    Loge.d("id: " + id);
  }

  private void initToolbar() {
    TextView titleText = (TextView) findViewById(R.id.toolbar_title_text);
    if (titleText != null) {
      titleText.setText(title);
    }
    AppCompatImageButton backButton = (AppCompatImageButton) findViewById(R.id.toolbar_back_button);
    if (backButton != null) {
      backButton.setOnClickListener(view -> onBackPressed());
    }
  }

  private void initViews() {
    progressView = (HiveProgressView) findViewById(R.id.loading_progress);
    webView = (WebView) findViewById(R.id.content_web);
    if (webView != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
          WebView.setWebContentsDebuggingEnabled(true);
        }
      }
      webView.setWebViewClient(new CustomWebViewClient());
      webView.setWebChromeClient(new CustomWebViewChromeClient());
      WebSettings webSettings = webView.getSettings();
      if (webSettings != null) {
        webSettings.setSupportZoom(false);
        webSettings.setJavaScriptEnabled(true);
      }
    }
  }

  private void getContent() {
    if (!TextUtils.isEmpty(url)) {
      DataService.getInstance(DetailActivity.this)
        .getContent(61)
        .map(content -> {
          String htmlText = "";
          if (content != null) {

            String result = getHeadHtml();
            String body = content.getContent();
            result = "<html>" + result + body + "</html>";

            Document doc = null;
            doc = Jsoup.parse(result);

            if (doc == null) return null;

            Elements es = doc.select("script");
            if (es != null) {
              es.remove();
            }

            handleImageClickEvent(doc);
            removeHyperlinks(doc);
            htmlText = doc.html();

            htmlText = htmlText.replaceAll("1a1a1a", "888888");
            htmlText = htmlText.replaceAll("b8b7b7", "ffffff");
          }

          return htmlText;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(content -> {
          if (webView != null && progressView != null && !TextUtils.isEmpty(content)) {
            webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
            webView.setVisibility(View.VISIBLE);
            progressView.setVisibility(View.GONE);
            doImageDownload();
          }
        }, error -> {
          if (webView != null && progressView != null) {
            webView.loadUrl(url);
            webView.setVisibility(View.VISIBLE);
            doImageDownload();
          }
        });
    }
  }

  private String getHeadHtml() {
    String result = "";
    try {
      InputStream in = DetailActivity.this.getResources().getAssets().open("head.html");
      if (in != null) {
        int length = in.available();
        byte[] buffer = new byte[length];
        if (in.read(buffer) > 0) result = new String(buffer, "UTF-8");
        in.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private void handleImageClickEvent(Document doc) {
    Elements es = doc.getElementsByTag("img");
    getImgUrls().clear();
    for (Element e : es) {
      String imgUrl = e.attr("src");

      if (!imgUrl.startsWith("http") && imgUrl.endsWith("gif")) {
        imgUrl = "https://www.chiphell.com/" + imgUrl;
      }

      getImgUrls().add(imgUrl);

      String imgName;
      File file = new File(imgUrl);
      imgName = file.getName();
      String filePath = "file://" + externalCacheDirPath + "/" + id + "/" + imgName;
      e.attr("src", "file:///android_asset/temp_img.png");
      e.attr("src_link", filePath);
      e.attr("ori_link", imgUrl);
    }
  }

  private void removeHyperlinks(Document doc) {
    Elements hrefs = doc.getElementsByTag("a");
    for (Element href : hrefs) {
      href.removeAttr("href");
    }
  }

  public synchronized List<String> getImgUrls() {
    if (imgUrls == null) {
      imgUrls = new ArrayList<>();
    }
    return imgUrls;
  }

  public synchronized void setImgUrls(List<String> imgUrls) {
    getImgUrls().clear();
    if (imgUrls != null && imgUrls.size() > 0) {
      getImgUrls().addAll(imgUrls);
    }
  }

  private void doImageDownload() {
    if (getImgUrls().isEmpty()) {
      return;
    }
    for (String urlStr : getImgUrls()) {
      if (urlStr == null) {
        break;
      }
      Loge.i("doImageDownload urlStr: " + urlStr);
      Glide.with(getApplicationContext())
        .load(urlStr)
        .asBitmap()
        .toBytes(Bitmap.CompressFormat.PNG, 30)
        .override(180, 180)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(new SimpleTarget<byte[]>() {
          @Override
          public void onResourceReady(byte[] resource,
                                      GlideAnimation<? super byte[]> glideAnimation) {
            RxPermissions.getInstance(DetailActivity.this)
              .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                       Manifest.permission.WRITE_EXTERNAL_STORAGE)
              .map(granted -> {
                if (granted && externalCacheDirPath != null) {
                  String imgName;
                  File file = new File(urlStr);
                  imgName = file.getName();
                  String folder = externalCacheDirPath + "/" + id;
                  String path = folder + "/" + imgName;
                  BitmapUtils.saveBitmap(resource, folder, path);
                  return true;
                }
                return false;
              })
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Subscriber<Boolean>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Boolean aBoolean) {
                  if (aBoolean) {
                    if (webView != null) {
                      webView.loadUrl("javascript:(function(){"
                                        + "var objs = document.getElementsByTagName(\"img\"); "
                                        + "for(var i=0;i<objs.length;i++)  " + "{"
                                        + "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
                                        + "    objs[i].setAttribute(\"src\",imgSrc);" + "}"
                                        + "})()");
                    }
                  }
                }
              });
          }
        });
    }
  }

  private class CustomWebViewChromeClient extends WebChromeClient {
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
      Loge.d("web onProgressChanged newProgress: " + newProgress);
      if (newProgress > 50 && progressView != null) {
        progressView.setVisibility(View.GONE);
      }

      if (webView != null) {
        webView.loadUrl(
          "javascript:(function(){" + "document.getElementsByClassName(\"header\")[0].remove();"
            + "document.getElementsByClassName(\"footer\")[0].remove();" + "})()");
        webView.loadUrl(
          "javascript:(function(){" + "var objs = document.getElementsByClassName(\"orange\");"
            + " for(var i=0;i<objs.length;i++){objs[i].removeAttribute(\"href\")}" + "})()");
      }
    }
  }

  private class CustomWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
      Loge.d("web shouldOverrideUrlLoading");
      return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      Loge.d("web onPageStarted");
      super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      Loge.d("web onPageFinished");
    }
  }
}
