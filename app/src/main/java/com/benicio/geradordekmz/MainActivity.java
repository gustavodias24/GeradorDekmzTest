package com.benicio.geradordekmz;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private ImageView imageView;
    private Bitmap capturedImage;

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
    }

    private void generateKMLFile() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        // Criar um arquivo KML com título e descrição
        String kmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "<Placemark>\n" +
                "<name>" + title + "</name>\n" +
                "<description>" + description + "</description>\n" +
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
                entry = new ZipEntry("image.jpg");
                zos.putNextEntry(entry);
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, zos);
                zos.closeEntry();
            }

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Abra o arquivo KMZ no aplicativo Google Earth
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(kmzFile), "application/vnd.google-earth.kmz");
        startActivity(intent);
    }
}