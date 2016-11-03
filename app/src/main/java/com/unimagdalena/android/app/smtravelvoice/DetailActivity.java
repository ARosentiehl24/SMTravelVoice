package com.unimagdalena.android.app.smtravelvoice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;

import org.fingerlinks.mobile.android.navigator.Navigator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    private Place place;

    @BindView(R.id.photos)
    ViewFlipper photos;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        place = (Place) getIntent().getExtras().getSerializable("place");

        photos.setAutoStart(true);
        photos.setFlipInterval(2500);

        for (Photo photo : place.getPhotos()) {
            ImageView placePhoto = new ImageView(this);
            placePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Glide.with(this).load(photo.getUrl()).into(placePhoto);

            photos.addView(placePhoto);
        }
    }

    @Override
    public void onBackPressed() {
        Navigator.with(this).utils().finishWithAnimation();
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
