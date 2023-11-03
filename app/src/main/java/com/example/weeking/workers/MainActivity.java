package com.example.weeking.workers;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.weeking.R;
import com.example.weeking.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;

    FirebaseAuth auth;

    String canal1 = "importanteDefault";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflar el layout aquí antes de llamar a setContentView()
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();
        /*ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Log.d("msg-test", "Firebase uid: " + user.getUid());
                    } else {
                        Log.d("msg-test", "Canceló el Log-in");
                    }
                }
        );

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                ))
                .build();

        signInLauncher.launch(intent);*/
        // Verificar si el usuario ya ha iniciado sesión
        if (auth.getCurrentUser() != null) {
            // Si el usuario ya está autenticado, navega a VistaPrincipal y termina MainActivity
            startActivity(new Intent(MainActivity.this, VistaPrincipal.class));
            finish();
            return; // No procesar más el método onCreate
        }


        crearCanalesNotificacion();
        binding.iniciarSesion.setOnClickListener(v -> {
            String correo = binding.correo.getText().toString();
            String contrasena = binding.password.getText().toString();

            if (!correo.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                if (!contrasena.isEmpty()) {
                    auth.signInWithEmailAndPassword(correo, contrasena)
                            .addOnSuccessListener(authResult -> {
                                notificarImportanceDefault();
                                startActivity(new Intent(MainActivity.this, VistaPrincipal.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Error en el inicio de sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    binding.password.setError("No se permiten campos vacíos");
                }
            } else if (correo.isEmpty()) {
                binding.correo.setError("No se permiten campos vacíos");
            } else {
                binding.correo.setError("Por favor, introduce un correo electrónico válido");
            }
        });


        binding.recuperarContrasena.setOnClickListener(v -> navigateToActivity(ContrasenaRecuperacion_Activity.class));
        binding.registrate.setOnClickListener(v -> navigateToActivity(RegistroActivity.class));
        binding.imageView.setOnClickListener(v -> navigateToActivity(ActividadesActivity.class));
        binding.bienvenidos.setOnClickListener(v -> navigateToActivity(Lista_don.class));
    }

    private void navigateToActivity(Class<?> destinationClass) {
        Intent intent = new Intent(MainActivity.this, destinationClass);
        startActivity(intent);
    }

    public void crearCanalesNotificacion() {

        NotificationChannel channel = new NotificationChannel(canal1,
                "Canal notificaciones default",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Canal para notificaciones con prioridad default");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        pedirPermisos();
    }
    public void pedirPermisos() {
        // TIRAMISU = 33
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
        }
    }

    private void notificarImportanceDefault() {
        // Crear notificación
        Intent intent = new Intent(this, VistaPrincipal.class); // Cambia esto según donde quieras que vaya el usuario al hacer clic en la notificación
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, canal1)
                .setSmallIcon(R.drawable.logo_48) // Cambia esto por tu propio icono
                .setContentTitle("Bienvenidos a Weeking")  // Título modificado
                .setContentText("Gracias por unirte a nosotros") // Texto modificado
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        Notification notification = builder.build();

        // Lanzar notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, notification);
        }

    }




}