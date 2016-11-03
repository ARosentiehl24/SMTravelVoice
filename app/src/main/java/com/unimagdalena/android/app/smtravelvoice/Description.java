package com.unimagdalena.android.app.smtravelvoice;

import java.io.Serializable;

/**
 * Created by Alberto on 03-Nov-16.
 */

public class Description implements Serializable {

    private String message;

    public Description(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
