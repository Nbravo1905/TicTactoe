package com.tictactoe.ngame.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tictactoe.ngame.R;

public class LoginActivity extends AppCompatActivity {

    private EditText correo, password;
    private Button btnLogin;
    private ScrollView formLogin;
    private ProgressBar cargandoLogin;
    private Button irRegistro;
    private FirebaseAuth firebaseAuth;
    private String email,pass;
    boolean tryLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        correo = findViewById(R.id.correo);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnRegistro);
        formLogin = findViewById(R.id.formRegistro);
        cargandoLogin = findViewById(R.id.cargandoRegistro);
        irRegistro = findViewById(R.id.irRegistro);

        firebaseAuth = FirebaseAuth.getInstance();

        cambiarLoginCargando(true);
        eventos();
    }

    private void eventos() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = correo.getText().toString();
                pass = password.getText().toString();

                if (email.isEmpty()){
                    correo.setError("El correo es obligatorio");
                }else if (pass.isEmpty()){
                    password.setError("La contraseña es incorrecta");
                }else{
                    //realizar login en Firebase Auth
                    cambiarLoginCargando(false);
                    loginUser();
                }
            }
        });

        irRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(i);
            }
        });
    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        tryLogin = true;
                        if (task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUi(user);
                        }else{
                            Log.w("TAG", "Error al login", task.getException());
                            updateUi(null);
                        }
                    }
                });
    }

    private void updateUi(FirebaseUser user) {
        if(user != null){
            //Almacenar la informacion del usuario en fireStor
            //Navegar hacia la siguiente pantalla de la aplicacion

            Intent i = new Intent(LoginActivity.this, BuscarPartidaActivity.class);
            startActivity(i);

        }else{
            cambiarLoginCargando(true);
            if (tryLogin) {
                password.setError("Nombre, Correo y/o Contraseña incorrectos ");
                password.requestFocus();
            }
        }
    }

    private void cambiarLoginCargando(boolean mostrar) {
        cargandoLogin.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        formLogin.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Comprobamos si ya el usuario a iniciado sesion
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUi(currentUser);
    }
}
