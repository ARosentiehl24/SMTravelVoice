package com.unimagdalena.android.app.smtravelvoice;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;

import org.fingerlinks.mobile.android.navigator.Navigator;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.mainDescription)
    TextView mainDescription;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.photos)
    ViewFlipper photos;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webPage)
    TextView webPage;

    private Boolean displayHome;
    private Place place;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        displayHome = getIntent().getExtras().getBoolean("display", true);
        place = (Place) getIntent().getExtras().getSerializable("place");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(displayHome);

        assert place != null;
        getSupportActionBar().setTitle(place.getName());

        if (place.getPhotos().size() > 1) {
            photos.setAutoStart(true);
            photos.setFlipInterval(2500);

            for (Photo photo : place.getPhotos()) {
                ImageView placePhoto = new ImageView(this);
                placePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(this).load(photo.getUrl()).into(placePhoto);

                photos.addView(placePhoto);
            }
        } else {
            ImageView placePhoto = new ImageView(this);
            placePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Glide.with(this).load(place.getPhotos().get(0).getUrl()).into(placePhoto);

            photos.addView(placePhoto);
        }

        mainDescription.setText(place.getMainDescription());

        String placeDescription = "";

        for (Description value : place.getDescriptions()) {
            placeDescription = placeDescription + (value.getMessage() + "\n");
        }

        description.setText(placeDescription);

        webPage.setText(place.getWebPage());

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(new Locale("es", "US"));

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }

                    textToSpeech.speak(place.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (displayHome) {
            Navigator.with(this).utils().finishWithAnimation();
        } else {
            Navigator.with(this).utils().finishWithAnimation(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
