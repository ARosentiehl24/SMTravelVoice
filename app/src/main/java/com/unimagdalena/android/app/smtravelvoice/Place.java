package com.unimagdalena.android.app.smtravelvoice;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Alberto on 03-Nov-16.
 */

public class Place implements Serializable {

    private String placeId;
    private String name;
    private Coordinate coordinates;
    private Float ratio;
    private String webPage;
    private ArrayList<Photo> photos;
    private String mainDescription;
    private ArrayList<Description> descriptions;
    private String distance;

    public Place(String placeId, String name, Coordinate coordinates, Float ratio, String webPage, ArrayList<Photo> photos, String mainDescription, ArrayList<Description> descriptions) {
        this.placeId = placeId;
        this.name = name;
        this.coordinates = coordinates;
        this.ratio = ratio;
        this.webPage = webPage;
        this.photos = photos;
        this.mainDescription = mainDescription;
        this.descriptions = descriptions;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

    public Float getRatio() {
        return ratio;
    }

    public void setRatio(Float ratio) {
        this.ratio = ratio;
    }

    public String getWebPage() {
        return webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    public String getMainDescription() {
        return mainDescription;
    }

    public void setMainDescription(String mainDescription) {
        this.mainDescription = mainDescription;
    }

    public ArrayList<Description> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(ArrayList<Description> descriptions) {
        this.descriptions = descriptions;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
