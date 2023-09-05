package com.benicio.geradordekmz.model;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;


import java.io.Serializable;
import java.util.List;

public class PointerModel implements Serializable {
    String title, descri, imageName, placemarkId, imageId;
    Double lat, longi;
    List<Uri> images;



    public PointerModel() {
    }

    public PointerModel(String title, String descri, String imageName, String placemarkId, String imageId, Double lat, Double longi, List<Uri> images) {
        this.title = title;
        this.descri = descri;
        this.imageName = imageName;
        this.placemarkId = placemarkId;
        this.imageId = imageId;
        this.lat = lat;
        this.longi = longi;
        this.images = images;
    }

    public String getPlacemarkId() {
        return placemarkId;
    }

    public void setPlacemarkId(String placemarkId) {
        this.placemarkId = placemarkId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public List<Uri> getImages() {
        return images;
    }

    public void setImages(List<Uri> images) {
        this.images = images;
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


    public String getPlaceMark() {
        String id = this.placemarkId;
        String name = this.title;
        String description = String.format("<div>%s</div>", this.descri);
        String longitude = this.longi.toString();
        String latitude = this.lat.toString();
        String altitude = "581.8185049830403";
        String heading = "331.2084133091871";
        String tilt = "0";
        String fovy = "35";
        String range = "869.6198195626494";
        String altitudeMode = "absolute";
        String styleUrl = "#__managed_style_08AA3D77B82B9B11FE7B";
        String imageId = this.imageId;
        @SuppressLint("DefaultLocale") String coordinates = String.format("%f,%f,596.8981538367215", this.longi.toString(),this.lat.toString() );

        StringBuilder imagesTagKml = new StringBuilder();
        for (Uri imageUri : images) {
            String template = "<gx:Image kml:id=\"%s\">\n" +
                    "            <gx:ImageUrl>%s</gx:ImageUrl>\n" +
                    "        </gx:Image>\n";
            String imageTag = String.format(template, imageId, imageUri.toString());
            imagesTagKml.append(imageTag);
        }

        String placemarkXml = "<Placemark id=\"%s\">\n" +
                "    <name>%s</name>\n" +
                "    <description><![CDATA[%s]]></description>\n" +
                "    <LookAt>\n" +
                "        <longitude>%s</longitude>\n" +
                "        <latitude>%s</latitude>\n" +
                "        <altitude>%s</altitude>\n" +
                "        <heading>%s</heading>\n" +
                "        <tilt>%s</tilt>\n" +
                "        <gx:fovy>%s</gx:fovy>\n" +
                "        <range>%s</range>\n" +
                "        <altitudeMode>%s</altitudeMode>\n" +
                "    </LookAt>\n" +
                "    <styleUrl>%s</styleUrl>\n" +
                "    <gx:Carousel>\n" + imagesTagKml.toString() +
                "    </gx:Carousel>\n" +
                "    <Point>\n" +
                "        <coordinates>%s</coordinates>\n" +
                "    </Point>\n" +
                "</Placemark>";

        return String.format(placemarkXml, id, name, description, longitude, latitude, altitude, heading, tilt, fovy, range, altitudeMode, styleUrl, coordinates);
    }

}
