package com.comic.hcreader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class Detail {

    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("content")
    @Expose
    private String content;

    /**
     * @return The rootlink
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link The rootlink
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content The content
     */
    public void setContent(String content) {
        this.content = content;
    }

}
