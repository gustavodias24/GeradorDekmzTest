package com.benicio.geradordekmz.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.benicio.geradordekmz.adapter.AdapterImages;
import com.benicio.geradordekmz.adapter.AdapterPointer;
import com.benicio.geradordekmz.databinding.ActivityMainBinding;
import com.benicio.geradordekmz.model.PointerModel;
import com.benicio.geradordekmz.util.PointerStorageUtil;
import com.benicio.geradordekmz.util.RecyclerItemClickListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST_LOCATION = 102;
    double latitude, longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityMainBinding vbinding;
    private RecyclerView rPoint, rImages;
    private AdapterPointer adapter;
    private AdapterImages adapterImges;
    private List<PointerModel> pontos = new ArrayList<>();
    private List<Uri> images = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vbinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(vbinding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        vbinding.buttonCaptureImage.setOnClickListener(view ->
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if ( checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, CAMERA_REQUEST_CODE);
                }
                else {
                    // already permisson
                    captureImageFromCamera();
                }
            }
            else{
                // system < M
                captureImageFromCamera();
            }
        });

        vbinding.buttonGenerateKML.setOnClickListener(view -> generateKMLFile());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocAtt();

        vbinding.atualizarLocBtn.setOnClickListener( view -> {
            getLocAtt();
        });

        vbinding.adicionarPontoBtn.setOnClickListener(view -> {
            getLocAtt();
            String title = vbinding.editTextTitle.getEditText().getText().toString().trim();
            String description = vbinding.editTextDescription.getEditText().getText().toString().trim();
            PointerModel newPointer = new PointerModel(
                    title,
                    description,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    latitude,
                    longitude,
                    images
            );
            Toast.makeText(this, "Ponto adicionado!", Toast.LENGTH_SHORT).show();
            pontos.add(newPointer);
            PointerStorageUtil.savePointer(getApplicationContext(), pontos);
            adapter.notifyDataSetChanged();
        });

        configurarRecyclerView();
        listarPointes();
        listenerRecyclerPointer();
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

    @SuppressLint("SetTextI18n")
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

            images.add(data.getData());
            rImages.setVisibility(View.VISIBLE);
            vbinding.semImage.setVisibility(View.GONE);

            Log.d("imageBucetina", data.getData().toString());

//            capturedImage = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(capturedImage);
//            imageView.setVisibility(View.VISIBLE);
        }
        if (requestCode == PERMISSIONS_REQUEST_LOCATION  && resultCode == RESULT_OK) {
            requestLocation();
        }
    }

    private void generateKMLFile() {

        StringBuilder placeMarks = new StringBuilder();

        for ( PointerModel p : pontos){
            placeMarks.append(p.getPlaceMark());
        }

        // Criar um arquivo KML com título e descrição
        String kmlContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">" +
                    "<Document>"    +
                        placeMarks  +
                    "</Document>"   +
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

            // Adicionar a imagens ao arquivo KMZ
//            for ( PointerModel p : pontos){
//                try {
//                    entry = new ZipEntry(p.getImageName());
//                    zos.putNextEntry(entry);
//
//                    // Converter a imagem em um array de bytes
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    p.getImage().compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                    byte[] imageBytes = stream.toByteArray();
//
//                    // Escrever os bytes da imagem no arquivo KMZ
//                    zos.write(imageBytes, 0, imageBytes.length);
//                    zos.closeEntry();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

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

    private void configurarRecyclerView(){
        rPoint = vbinding.recyclerPoints;
        rPoint.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rPoint.setHasFixedSize(true);
        rPoint.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        adapter = new AdapterPointer(pontos, getApplicationContext());
        rPoint.setAdapter(adapter);

        rImages = vbinding.recyclerImagens;
        rPoint.setHasFixedSize(true);
        rPoint.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.HORIZONTAL));
        rPoint.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        adapterImges = new AdapterImages(images, getApplicationContext());
    }

    private void listarPointes(){
        pontos.clear();
        if (PointerStorageUtil.loadPointers(getApplicationContext()) != null){
            pontos.addAll(PointerStorageUtil.loadPointers(getApplicationContext()));
            Collections.reverse(pontos);
            adapter.notifyDataSetChanged();
        }
    }

    public void listenerRecyclerPointer(){
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setMessage("Remover esse ponto?");
                b.setPositiveButton("Sim", (dialogInterface, i) -> {
                    pontos.remove(i);
                    PointerStorageUtil.savePointer(getApplicationContext(), pontos);
                    adapter.notifyDataSetChanged();
                });
                b.create().show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(rPoint);
        
    }
}