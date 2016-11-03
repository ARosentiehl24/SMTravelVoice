package com.unimagdalena.android.app.smtravelvoice;

import java.io.Serializable;

/**
 * Created by Alberto on 03-Nov-16.
 */

public class Photo implements Serializable {

    private String url;

    public Photo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
