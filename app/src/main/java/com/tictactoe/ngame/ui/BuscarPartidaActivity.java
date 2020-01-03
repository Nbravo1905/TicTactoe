package com.tictactoe.ngame.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.tictactoe.ngame.R;
import com.tictactoe.ngame.app.Constantes;
import com.tictactoe.ngame.model.Jugada;

import javax.annotation.Nullable;

public class BuscarPartidaActivity extends AppCompatActivity {

    private TextView textCargando;
    private ProgressBar cargandoPartida;
    private ScrollView capaCargando,menuJuego;
    private Button btnJugar, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String uid, jugadaId;
    private ListenerRegistration listenerRegistration = null;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_partida);

        capaCargando = findViewById(R.id.capaCargando);
        menuJuego = findViewById(R.id.menuJuego);
        btnJugar = findViewById(R.id.btnJugar);
        btnRanking = findViewById(R.id.btnRanking);

        iniciarCargando();
    }

    private void iniciarCargando() {
        animationView = findViewById(R.id.animation_view);
        textCargando = findViewById(R.id.textCargando);
        cargandoPartida = findViewById(R.id.cargandoPartida);

        cargandoPartida.setIndeterminate(true);
        textCargando.setText("Cargando ...");

        cambiarMenu(true);
        iniciarFirebase();
        eventos();
    }

    private void iniciarFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
    }

    private void eventos() {
        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarMenu(false);
                buscarJugadaLibre();
            }
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void buscarJugadaLibre() {
        textCargando.setText("Buscando una partida...");
        animationView.playAnimation();

        db.collection("jugadas")
                .whereEqualTo("jugadorDosId", "")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0){
                            //No existen partidas libres, crear una
                            crearNuevaPartida();
                        }else{
                            boolean encontrado = false;

                            for(DocumentSnapshot docJugada : task.getResult().getDocuments()){

                                if(!docJugada.get("jugadorUnoId").equals(uid)) {
                                    encontrado = true;
                                    jugadaId = docJugada.getId();
                                    Jugada jugada = docJugada.toObject(Jugada.class);
                                    jugada.setJugadorDosId(uid);

                                    db.collection("jugadas")
                                            .document(jugadaId)
                                            .set(jugada)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    textCargando.setText("Partida encontrada, a Jugar");
                                                    animationView.setRepeatCount(0);
                                                    animationView.setAnimation("check.json");
                                                    animationView.playAnimation();

                                                    final Handler handler = new Handler();
                                                    final Runnable r = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            iniciarJuego();
                                                        }
                                                    };

                                                    handler.postDelayed(r, 1500);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            cambiarMenu(true);
                                            Toast.makeText(BuscarPartidaActivity.this, "Huno algun problema", Toast.LENGTH_LONG).show();
                                        }
                                    });

                                    break;
                                }

                                if(!encontrado) {
                                    crearNuevaPartida();
                                }

                            }


                        }
                    }
                });
    }

    private void crearNuevaPartida() {
        textCargando.setText("Creando Una Partida...");
        Jugada nuevaJugada = new Jugada(uid);

        db.collection("jugadas")
                .add(nuevaJugada)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        jugadaId = documentReference.getId();
                        //DEBEMOS ESPERAR A OTRO JUGADOR
                        esperarOtroJugador();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                cambiarMenu(true);
                Toast.makeText(BuscarPartidaActivity.this, "Huno algun problema al crear tu partida :(", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void esperarOtroJugador() {
        textCargando.setText("Esperando a otro Jugador...");

        listenerRegistration = db.collection("jugadas")
                .document(jugadaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(!documentSnapshot.get("jugadorDosId").equals("")){

                            textCargando.setText("!Ya Encontramos un jugador para esta partida¡");
                            animationView.setRepeatCount(0);
                            animationView.setAnimation("check.json");
                            animationView.playAnimation();

                            final Handler handler = new Handler();
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    iniciarJuego();
                                }
                            };

                            handler.postDelayed(r,1500);

                        }
                    }
                });
    }

    private void iniciarJuego() {
        if(listenerRegistration != null){
            listenerRegistration.remove();
        }
        Intent i = new Intent(BuscarPartidaActivity.this, JuegoActivity.class);
        i.putExtra(Constantes.EXTRA_JUGADA_ID, jugadaId);
        startActivity(i);
        jugadaId = "";
    }

    private void cambiarMenu(boolean showMenu) {
        capaCargando.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        menuJuego.setVisibility(showMenu ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (jugadaId != null){
            cambiarMenu(false);
            esperarOtroJugador();
        }else {
            cambiarMenu(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (listenerRegistration != null){
            listenerRegistration.remove();
        }
        if (jugadaId != "") {
            db.collection("jugadas")
                    .document(jugadaId)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            jugadaId = "";
                        }
                    });
        }
    }
}
