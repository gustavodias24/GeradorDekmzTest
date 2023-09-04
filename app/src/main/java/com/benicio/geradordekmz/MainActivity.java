package com.benicio.geradordekmz;

import static android.location.LocationManager.GPS_PROVIDER;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.benicio.geradordekmz.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST_LOCATION = 102;
    double latitude, longitude;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private ImageView imageView;
    private Bitmap capturedImage;

    private TextView textView;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityMainBinding vbinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vbinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(vbinding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.loc_text);

        Button buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        buttonCaptureImage.setOnClickListener(view -> captureImageFromCamera());

        Button buttonGenerateKML = findViewById(R.id.buttonGenerateKML);
        buttonGenerateKML.setOnClickListener(view -> generateKMLFile());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocAtt();

        vbinding.atualizarLocBtn.setOnClickListener( view -> {
            getLocAtt();
        });
    }

    private void getLocAtt(){
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
                         attLocText(latitude, longitude);
                    }
                });
    }

    public void attLocText(Double lat, Double longi){
        Toast.makeText(this, "Atualizando...", Toast.LENGTH_SHORT).show();
        vbinding.locText.setTextColor(Color.BLACK);
        vbinding.locText.setText("Lat: " + lat + "\n" + "Long: " + longi );
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

        // Criar um arquivo KML com título e descrição
        String kmlContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                    "<Placemark>\n" +
                        "<name>" + title + "</name>\n" +
                        "<description>" + description + "</description>\n" +
                        "<Point>\n" +
                            "<coordinates>" + longitude + "," + latitude + ",0</coordinates>\n" + // Adicione ",0" à coordenada Z
                        "</Point>\n" +
                        "<Style>\n" +
                            "<IconStyle>\n" +
                                "<Icon>\n" +
                                    "<href>image.jpg</href>\n" + // Referência à imagem
                                "</Icon>\n" +
                            "</IconStyle>\n" +
                        "</Style>\n" +
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
        File kmzFile = new File(getExternalFilesDir(null), "marker.kmz");

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
                try {
                    entry = new ZipEntry("image.jpg");
                    zos.putNextEntry(entry);

                    // Converter a imagem em um array de bytes
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageBytes = stream.toByteArray();

                    // Escrever os bytes da imagem no arquivo KMZ
                    zos.write(imageBytes, 0, imageBytes.length);
                    zos.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Abra o arquivo KMZ no aplicativo Google Earth
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, "com.benicio.geradordekmz.fileprovider", kmzFile);
        intent.setDataAndType(uri, "application/vnd.google-earth.kmz");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}