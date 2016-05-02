package com.comic.chhreader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class Detail {
    @SerializedName("pk")
    @Expose
    private Integer pk;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("fields")
    @Expose
    private DetailFields fields;

    /**
     * @return The pk
     */
    public Integer getPk() {
        return pk;
    }

    /**
     * @param pk The pk
     */
    public void setPk(Integer pk) {
        this.pk = pk;
    }

    /**
     * @return The model
     */
    public String getModel() {
        return model;
    }

    /**
     * @param model The model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return The fields
     */
    public DetailFields getFields() {
        return fields;
    }

    /**
     * @param fields The fields
     */
    public void setFields(DetailFields fields) {
        this.fields = fields;
    }

}
