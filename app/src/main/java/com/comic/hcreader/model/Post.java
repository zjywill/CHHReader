package com.comic.hcreader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class Post extends RealmObject {
    @PrimaryKey
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("postdate")
    @Expose
    private String postdate;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("topic")
    @Expose
    private String topic;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("link")
    @Expose
    private String link;


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPostdate() {
        return postdate;
    }

    public void setPostdate(String postdate) {
        this.postdate = postdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
