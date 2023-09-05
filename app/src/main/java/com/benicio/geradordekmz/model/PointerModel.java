package com.benicio.geradordekmz.model;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;


import java.io.Serializable;
import java.util.List;

public class PointerModel implements Serializable {
    String title, descri, placemarkId, imageId;
    Double lat, longi;
    List<Uri> images;

    public PointerModel(String title, String descri, String placemarkId, String imageId, Double lat, Double longi, List<Uri> images) {
        this.title = title;
        this.descri = descri;
        this.placemarkId = placemarkId;
        this.imageId = imageId;
        this.lat = lat;
        this.longi = longi;
        this.images = images;
    }

    public String getPlacemarkId() {
        return placemarkId;
    }
    public void setImages(List<Uri> images) {
        this.images = images;
    }
    public String getTitle() {
        return title;
    }
    public Double getLat() {
        return lat;
    }
    public Double getLongi() {
        return longi;
    }

    private String getStyleIcon(){

        String xml = "<gx:CascadingStyle kml:id=\"%s\">\n" +
                "    <Style>\n" +
                "        <IconStyle>\n" +
                "            <scale>1.2</scale>\n" +
                "            <Icon>\n" +
                "                <href>%s</href>\n" +
                "            </Icon>\n" +
                "        </IconStyle>\n" +
                "        <LabelStyle>\n" +
                "        </LabelStyle>\n" +
                "        <LineStyle>\n" +
                "            <color>%s</color>\n" +
                "            <width>%s</width>\n" +
                "        </LineStyle>\n" +
                "        <PolyStyle>\n" +
                "            <color>%s</color>\n" +
                "        </PolyStyle>\n" +
                "        <BalloonStyle>\n" +
                "            <displayMode>%s</displayMode>\n" +
                "        </BalloonStyle>\n" +
                "    </Style>\n" +
                "</gx:CascadingStyle>";

        String id = this.placemarkId;
        String href = images.get(0).toString();
        String color = "ff2dc0fb";
        String width = "4.8";
        String polyColor = "40ffffff";
        String displayMode = "hide";

        return String.format(xml, id, href, color, width, polyColor, displayMode);
    }
    private String getStyleMap(){
        String styleMapId = this.placemarkId;
        String key = "highlight";
        String styleUrl = this.placemarkId;

       return String.format(
                "<StyleMap id=\"%s\">\n" +
                        "    <Pair>\n" +
                        "        <key>%s</key>\n" +
                        "        <styleUrl>%s</styleUrl>\n" +
                        "    </Pair>\n" +
                        "</StyleMap>",
                styleMapId, key, styleUrl
        );
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
        String styleUrl = this.placemarkId;
        String imageId = this.imageId;
        @SuppressLint("DefaultLocale") String coordinates = String.format("%f,%f,596.8981538367215", this.longi.toString(),this.lat.toString() );

        StringBuilder imagesTagKml = new StringBuilder();
        for (Uri imageUri : images) {
            String template =
                    "<gx:Image kml:id=\"%s\">\n" +
                    "    <gx:ImageUrl>%s</gx:ImageUrl>\n" +
                    "</gx:Image>\n";
            String imageTag = String.format(template, imageId, imageUri.toString());
            imagesTagKml.append(imageTag);
        }

        String placemarkXml =
                getStyleIcon() +
                getStyleMap() +
                "<Placemark id=\"%s\">\n" +
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
