package com.tictactoe.ngame.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tictactoe.ngame.R;
import com.tictactoe.ngame.model.User;

public class RegistroActivity extends AppCompatActivity {

    private EditText nombre, correo, password;
    private Button btnRegistro;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String name, email, pass;
    ProgressBar cargandoRegistro;
    ScrollView formRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.correo);
        password = findViewById(R.id.password);
        btnRegistro = findViewById(R.id.btnRegistro);
        cargandoRegistro = findViewById(R.id.cargandoRegistro);
        formRegistro = findViewById(R.id.formRegistro);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cambiarRegistroCargando(true);
        eventos();
    }

    private void eventos() {
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nombre.getText().toString();
                email = correo.getText().toString();
                pass = password.getText().toString();

                if(email.isEmpty()){
                    correo.setError("El Correo no puede estar vacio");
                }else if(name.isEmpty()){
                    nombre.setError("El nombre no puede estar vacio");
                }else if(pass.isEmpty()) {
                    password.setError("La contraseña no puede estar vacia");
                } else{
                    // realizar regisro en firebase Auth
                    registrarUser();
                }

            }
        });
    }

    private void registrarUser() {
        cambiarRegistroCargando(false);
        firebaseAuth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUi(user);
                        }else{
                            Toast.makeText(RegistroActivity.this, "Error en el registro", Toast.LENGTH_LONG).show();
                            updateUi(null);
                        }
                    }
                });
    }

    private void updateUi(FirebaseUser user) {
        if(user != null){
            //Almacenar la informacion del usuario en fireStor
            //Navegar hacia la siguiente pantalla de la aplicacion
            User nuevoUsuario = new User(name, 0, 0);

            db.collection("users")
                    .document(user.getUid())
                    .set(nuevoUsuario)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                            Intent i = new Intent(RegistroActivity.this, BuscarPartidaActivity.class);
                            startActivity(i);
                        }
                    });

        }else{
            cambiarRegistroCargando(true);
            password.setError("Nombre, Correo y/o Contraseña incorrectos ");
            password.requestFocus();
        }
    }

    private void cambiarRegistroCargando(boolean mostrar) {
        cargandoRegistro.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        formRegistro.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }
}
