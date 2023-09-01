package com.benicio.geradordekmz;

import static android.location.LocationManager.GPS_PROVIDER;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {
    private Uri uri;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST_LOCATION = 102;
    private Location currentLocation;
    double latitude, longitude;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private ImageView imageView;
    private Bitmap capturedImage;

    private FusedLocationProviderClient fusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        imageView = findViewById(R.id.imageView);

        Button buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        buttonCaptureImage.setOnClickListener(view -> captureImageFromCamera());

        Button buttonGenerateKML = findViewById(R.id.buttonGenerateKML);
        buttonGenerateKML.setOnClickListener(view -> generateKMLFile());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                         latitude = location.getLatitude();
                         longitude = location.getLongitude();

                    }
                });
    }


    private void captureImageFromCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            capturedImage = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(capturedImage);
            imageView.setVisibility(View.VISIBLE);
        }
        if (requestCode == PERMISSIONS_REQUEST_LOCATION  && resultCode == RESULT_OK) {
            requestLocation();
        }
    }

    private void generateKMLFile() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        Log.d("bucetinha", "generateKMLFile: " + latitude + "\n" + longitude);

        // Criar um arquivo KML com título e descrição
        String kmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "<Placemark>\n" +
                "<name>" + title + "</name>\n" +
                "<description>" + description + "</description>\n" +
                "<Point>\n" +
                "<coordinates>" + longitude + "," + latitude + "</coordinates>\n" +
                "</Point>\n" +
                "</Placemark>\n" +
                "</kml>";

        // Salvar o arquivo KML no armazenamento externo
        File kmlFile = new File(getExternalFilesDir(null), "marker.kml");
        try {
            FileOutputStream fos = new FileOutputStream(kmlFile);
            fos.write(kmlContent.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Comprimir o arquivo KML em um arquivo KMZ
        File kmzFile = new File(getExternalFilesDir(null), Environment.getExternalStorageDirectory().toString() + "marker.kmz");

        try {
            FileOutputStream fos = new FileOutputStream(kmzFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // Adicionar o arquivo KML ao arquivo KMZ
            ZipEntry entry = new ZipEntry("marker.kml");
            zos.putNextEntry(entry);
            byte[] kmlBytes = kmlContent.getBytes();
            zos.write(kmlBytes, 0, kmlBytes.length);
            zos.closeEntry();

            // Adicionar a imagem ao arquivo KMZ
            if (capturedImage != null) {
                entry = new ZipEntry("image.jpg");
                zos.putNextEntry(entry);
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, zos);
                zos.closeEntry();
            }

            if (Build.VERSION.SDK_INT < 24) {
                uri = Uri.fromFile(kmzFile);
            } else {
                uri = Uri.parse(kmzFile.getPath()); // My work-around for SDKs up to 29.
            }

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Abra o arquivo KMZ no aplicativo Google Earth
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.google-earth.kmz");
        startActivity(intent);
    }
}