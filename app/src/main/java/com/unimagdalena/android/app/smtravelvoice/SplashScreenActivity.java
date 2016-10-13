package com.unimagdalena.android.app.smtravelvoice;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.fingerlinks.mobile.android.navigator.Navigator;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Navigator.with(SplashScreenActivity.this).build().goTo(MapsActivity.class).animation(android.R.anim.fade_in, android.R.anim.fade_out).commit();
                finish();
            }
        }, 2500);
    }
}
