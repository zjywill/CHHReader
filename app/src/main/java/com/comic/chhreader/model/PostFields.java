package com.comic.chhreader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class PostFields extends RealmObject{

    @SerializedName("postdate")
    @Expose
    private String postdate;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("imageurl")
    @Expose
    private String imageurl;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("item")
    @Expose
    private Integer item;
    @SerializedName("is_valid")
    @Expose
    private Boolean isValid;
    @SerializedName("link")
    @Expose
    private String link;

    /**
     * @return The postdate
     */
    public String getPostdate() {
        return postdate;
    }

    /**
     * @param postdate The postdate
     */
    public void setPostdate(String postdate) {
        this.postdate = postdate;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The imageurl
     */
    public String getImageurl() {
        return imageurl;
    }

    /**
     * @param imageurl The imageurl
     */
    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
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

    /**
     * @return The item
     */
    public Integer getItem() {
        return item;
    }

    /**
     * @param item The item
     */
    public void setItem(Integer item) {
        this.item = item;
    }

    /**
     * @return The isValid
     */
    public Boolean getIsValid() {
        return isValid;
    }

    /**
     * @param isValid The is_valid
     */
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * @return The link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    public void setLink(String link) {
        this.link = link;
    }

}
