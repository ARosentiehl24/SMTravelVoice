package com.unimagdalena.android.app.smtravelvoice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.fingerlinks.mobile.android.navigator.Navigator;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    @Override
    public void onBackPressed() {
        Navigator.with(this).utils().finishWithAnimation();
    }
}
