package com.example.weeking.workers.fragmentos;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.example.weeking.entity.Usuario;
import com.example.weeking.workers.Contrasena3Activity;
import com.example.weeking.workers.VistaPrincipal;
import com.example.weeking.workers.Yape;
import com.example.weeking.workers.viewModels.AppViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weeking.R;
import com.example.weeking.workers.AccountActivity;
import com.example.weeking.workers.StatusActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class perfil extends Fragment {
    private Button btnStatus;
    private Button btnAccount;
    private Button btnLogOut;
    private Button contrasenia;
    private FirebaseAuth mAuth;
    private AppViewModel appViewModel;
    private ImageView imageView;
    private LogoutListener logoutListener;
    private ImageButton uploadButtonPerfil;
    private TextView userName ,codigo;
    private static final int REQUEST_PICK_IMAGE = 1;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    String download_uri = "";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LogoutListener) {
            logoutListener = (LogoutListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement LogoutListener");
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        userName = view.findViewById(R.id.textNombre); // Asume que tienes un TextView con el ID "userName" en tu layout.
        codigo = view.findViewById(R.id.textCodigoPerfil);
        mAuth = FirebaseAuth.getInstance();
        uploadButtonPerfil = view.findViewById(R.id.uploadButtonPerfil);
        imageView = view.findViewById(R.id.imageView);
        uploadButtonPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        btnStatus = view.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), StatusActivity.class);
            startActivity(intent);
        });

        btnAccount = view.findViewById(R.id.btnAccount);
        btnAccount.setOnClickListener(view12 -> {
            Intent intent = new Intent(getActivity(), AccountActivity.class);
            startActivity(intent);
        });

        contrasenia = view.findViewById(R.id.button3);
        contrasenia.setOnClickListener(view112 -> {
            Intent intent = new Intent(getActivity(), Contrasena3Activity.class);
            startActivity(intent);
        });
        btnLogOut = view.findViewById(R.id.btnLogout);
        btnLogOut.setOnClickListener(view13 -> {
            // Cierra la sesión con Firebase
            mAuth.signOut();

            // Notifica al listener que el usuario ha cerrado la sesión
            if (logoutListener != null) {
                logoutListener.onLogout();
            }
        });

        appViewModel.getCurrentUser().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                // Actualizar la UI del fragmento con los datos del usuario
                actualizarUI(usuario);
            }
        });


        return view;

    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            cargaImagen(selectedImageUri);
        }
    }
    public interface LogoutListener {
        void onLogout();
    }

    private void actualizarUI(Usuario usuario) {

        Log.d("codigoalumno",usuario.getCodigo().toString());
        userName.setText(usuario.getNombre());
        codigo.setText(usuario.getCodigo());

    }


    private void cargaImagen(Uri imageUri){

        // Obtener el ID único del usuario actual.
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (imageUri != null) {
            // Generar un nombre de archivo único para la imagen.

            // Crear la ruta completa donde se guardará la imagen, e.g., "usuarios/userId/uniqueFileName"
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("usuarios/" + UUID.randomUUID().toString());
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obtiene la URL de descarga
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            download_uri = uri.toString();
                            // Aquí actualizas la base de datos con la nueva URL de la foto
                            // ...
                            guardarFirestore(download_uri);

                            // Sigue con el flujo de tu aplicación, por ejemplo, lanzar otra actividad
                        });
                    })
                    .addOnFailureListener(e -> {
                    });
        }


    }
    private void guardarFirestore(String urlImagen){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> datos = new HashMap<>();
        datos.put("urlImagen", urlImagen);
        db.collection("usuario")
                .add(datos)
                .addOnSuccessListener(documentReference -> {

                })
                .addOnFailureListener(e -> {

                });

    }
}