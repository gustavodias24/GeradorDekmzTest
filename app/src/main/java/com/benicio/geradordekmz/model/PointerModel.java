package com.benicio.geradordekmz.model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class PointerModel implements Serializable {
    String title, descri, imageName;
    Double lat, longi;
    Bitmap image;

    public PointerModel() {
    }

    public PointerModel(String title, String descri, String imageName, Double lat, Double longi, Bitmap image) {
        this.title = title;
        this.descri = descri;
        this.imageName = imageName;
        this.lat = lat;
        this.longi = longi;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescri() {
        return descri;
    }

    public void setDescri(String descri) {
        this.descri = descri;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLongi() {
        return longi;
    }

    public void setLongi(Double longi) {
        this.longi = longi;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getPlaceMark(){
        return
                "<Placemark>\n" +
                    "<name>" + this.title + "</name>\n" +
                    "<description>" + this.descri + "</description>\n" +
                    "<Point>\n" +
                        "<coordinates>" + this.longi + "," + this.lat + ",0</coordinates>\n" +
                    "</Point>\n" +
                    "<Style>\n" +
                        "<IconStyle>\n" +
                            "<Icon>\n" +
                                "<href>"+ this.imageName +"</href>\n" +
                            "</Icon>\n" +
                        "</IconStyle>\n" +
                    "</Style>\n" +
                "</Placemark>\n" ;
    }
}
