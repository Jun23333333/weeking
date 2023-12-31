package com.example.weeking.workers;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weeking.R;
import com.example.weeking.dataHolder.DataHolder;
import com.example.weeking.entity.EventoClass;
import com.example.weeking.workers.adaptador.GaleriaFotosAdapter;
import com.example.weeking.workers.fragmentos.camarafragmento;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class GaleriaEventos extends AppCompatActivity {
    GridView gridView;
    FirebaseStorage storage;
    //private ArrayList<DataClass> dataList;
    FloatingActionButton anadir, camara, galeria;
    GaleriaFotosAdapter adapter;
    StorageReference reference;
    String eventoID;
    String even;
    List<String> imageUrls;
    TextView textView26;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria_eventos);
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        anadir = findViewById(R.id.flatBtnAddNewPhoto);
        gridView = findViewById(R.id.gv_imagenes);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int columnWidth = screenWidth / 2;
        Log.d("zxc", String.valueOf(columnWidth));
        gridView.setColumnWidth(columnWidth);
        EventoClass eventoSeleccionado = DataHolder.getInstance().getEventoSeleccionado();
        even = (eventoSeleccionado.getNombre() != null) ? eventoSeleccionado.getNombre() : "No tiene nombre";
        eventoID = eventoSeleccionado.getEventId();
        Log.d("msg-test", eventoID);
        StorageReference eventoStorageRef = storage.getReference().child("eventos/" + eventoID+"/");
        //Log.d("msg-test", String.valueOf(eventoStorageRef));
        imageUrls = new ArrayList<>();
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Galeria de fotos: "+ even);
        }
        eventoStorageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<Task<Uri>> tasks = new ArrayList<>(); // Lista para almacenar tareas asíncronas
                    for (StorageReference item : listResult.getItems()) {
                        // Verificamos si el nombre del archivo NO es "imagen.jpg"
                        if (!item.getName().equalsIgnoreCase("imagen.jpg")) {
                            // Si no lo es, obtenemos la URL y la agregamos a la lista de tareas
                            tasks.add(item.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Agregamos cada URL a la lista imageUrls
                                imageUrls.add(uri.toString());
                            }));
                        }
                    }

                    // Esperamos a que todas las tareas se completen antes de cargar las imágenes en el GridView
                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                        cargarImagenesEnGridView(imageUrls);
                    });

                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(GaleriaEventos.this, "Error al obtener las imágenes", Toast.LENGTH_SHORT).show();
                });
        anadir.setOnClickListener(view -> {
            Intent intent = new Intent(GaleriaEventos.this, GaleriaUploadActivity.class);
            startActivity(intent);
            finish();
        });
        }


    private void cargarImagenesEnGridView(List<String> imageUrls) {
        adapter = new GaleriaFotosAdapter(GaleriaEventos.this, imageUrls);
        gridView.setAdapter(adapter);
    }

    public void borrarImagen(String urlToDelete, int position) {
        // Obtenemos la referencia de la imagen desde la URL
        StorageReference imageRef = storage.getReferenceFromUrl(urlToDelete);

        // Extraemos el nombre de la imagen de la referencia
        String imageName = imageRef.getName();

        // Comprobamos si el nombre de la imagen es "imagen.jpg"
        if(imageName.equals("imagen.jpg")) {
            // Si es "imagen.jpg", mostramos un mensaje y no borramos la imagen
            Toast.makeText(GaleriaEventos.this, "Esta imagen no se puede borrar", Toast.LENGTH_SHORT).show();
        } else {
            // Si no es "imagen.jpg", procedemos a borrar la imagen
            imageRef.delete().addOnSuccessListener(aVoid -> {
                // Código para borrar la imagen y actualizar la interfaz
                imageUrls.remove(position); // Elimina la URL de la lista
                adapter.notifyDataSetChanged(); // Actualiza el adaptador
                Toast.makeText(GaleriaEventos.this, "Imagen borrada", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(GaleriaEventos.this, "Error al borrar la imagen", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onBackPressed() {
        // Finaliza la actividad actual
        finish();
    }
}
