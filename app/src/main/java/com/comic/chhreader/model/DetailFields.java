package com.comic.chhreader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class DetailFields {

    @SerializedName("rootlink")
    @Expose
    private String rootlink;
    @SerializedName("content")
    @Expose
    private String content;

    /**
     * @return The rootlink
     */
    public String getRootlink() {
        return rootlink;
    }

    /**
     * @param rootlink The rootlink
     */
    public void setRootlink(String rootlink) {
        this.rootlink = rootlink;
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
