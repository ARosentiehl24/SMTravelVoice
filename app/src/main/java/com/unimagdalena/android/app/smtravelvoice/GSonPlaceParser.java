package com.unimagdalena.android.app.smtravelvoice;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Alberto on 03-Nov-16.
 */

public class GSonPlaceParser {

    public ArrayList<Place> getPlaces(InputStream inputStream) throws IOException {
        ArrayList<Place> places = new ArrayList<>();

        Gson gson = new Gson();

        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

        jsonReader.beginArray();

        while (jsonReader.hasNext()) {
            Place place = gson.fromJson(jsonReader, Place.class);
            places.add(place);
        }

        jsonReader.endArray();
        jsonReader.close();

        return places;
    }
}
