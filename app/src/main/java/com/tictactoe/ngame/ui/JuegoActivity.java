package com.tictactoe.ngame.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.tictactoe.ngame.R;
import com.tictactoe.ngame.app.Constantes;
import com.tictactoe.ngame.model.Jugada;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class JuegoActivity extends AppCompatActivity {

    List<ImageView> casillas;
    TextView tvPlayer1, tvPlayer2;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String uid, jugadaId, playerOneName = "", playerTwoName = "";
    Jugada jugada;
    ListenerRegistration listenerJugada = null;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initViews();
        initGame();
    }

    private void initGame() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        Bundle extras = getIntent().getExtras();

        jugadaId = extras.getString(Constantes.EXTRA_JUGADA_ID);
    }

    private void initViews() {
        tvPlayer1 = findViewById(R.id.textViewPlayer1);
        tvPlayer2 = findViewById(R.id.textViewPlayer2);

        casillas = new ArrayList<>();
        casillas.add((ImageView) findViewById(R.id.imageView0));
        casillas.add((ImageView) findViewById(R.id.imageView1));
        casillas.add((ImageView) findViewById(R.id.imageView2));
        casillas.add((ImageView) findViewById(R.id.imageView3));
        casillas.add((ImageView) findViewById(R.id.imageView4));
        casillas.add((ImageView) findViewById(R.id.imageView5));
        casillas.add((ImageView) findViewById(R.id.imageView6));
        casillas.add((ImageView) findViewById(R.id.imageView7));
        casillas.add((ImageView) findViewById(R.id.imageView8));

    }

    @Override
    protected void onStart() {
        super.onStart();
        jugadaListener();
    }

    private void jugadaListener() {
        Log.w("VER VARIABLE", jugadaId);
        listenerJugada = db.collection("jugadas")
                .document(jugadaId)
                .addSnapshotListener(JuegoActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(JuegoActivity.this, "Error al obtener los datos de jugadas", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String source = snapshot != null
                                && snapshot.getMetadata().hasPendingWrites() ? "Local" : "Server";

                        if (snapshot.exists() && source.equals("Server")) {
                            //Parseando documentSnaphot > Jugada
                            jugada = snapshot.toObject(Jugada.class);
                            if (playerOneName.isEmpty() || playerTwoName.isEmpty()) {
                                //Obtener nombres de los jugadores
                                getPlayerNames();
                            }

                            updateUI();
                        }

                        cambioColorJugador();
                    }
                });
    }

    private void cambioColorJugador() {
        if (jugada.isTurnoJugadorUno()) {
            tvPlayer1.setTextColor(getResources().getColor(R.color.colorAccent));
            tvPlayer2.setTextColor(getResources().getColor(R.color.colorGris));
        } else {
            tvPlayer1.setTextColor(getResources().getColor(R.color.colorGris));
            tvPlayer2.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private void updateUI() {
        for (int i = 0; i < 9; i++) {
            int casilla = jugada.getCeldasSeleccionadas().get(i);
            ImageView ivCasillaActual = casillas.get(i);
            if (casilla == 0) {
                ivCasillaActual.setImageResource(R.drawable.ic_empty_square);
            } else if (casilla == 1) {
                ivCasillaActual.setImageResource(R.drawable.ic_player_one);
            } else {
                ivCasillaActual.setImageResource(R.drawable.ic_player_two);
            }
        }
    }

    private void getPlayerNames() {
        //obtener el nombre del player 1
        db.collection("users")
                .document(jugada.getJugadorUnoId())
                .get()
                .addOnSuccessListener(JuegoActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        playerOneName = snapshot.get("name").toString();
                        tvPlayer1.setText(playerOneName);
                    }
                });

        //obtener el nombre del player 2
        db.collection("users")
                .document(jugada.getJugadorDosId())
                .get()
                .addOnSuccessListener(JuegoActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        playerTwoName = snapshot.get("name").toString();
                        tvPlayer2.setText(playerTwoName);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerJugada != null) {
            listenerJugada.remove();
        }

    }

    public void casillaSelecionada(View view) {
        if (!jugada.getGanadorId().isEmpty()) {
            Toast.makeText(this, "La Partida a terminado :)", Toast.LENGTH_SHORT).show();
        } else {
            if (jugada.isTurnoJugadorUno() && jugada.getJugadorUnoId().equals(uid)) {
                //Esta jugando el jugador 1
                actualizarJugada(view.getTag().toString());
            } else if (!jugada.isTurnoJugadorUno() && jugada.getJugadorDosId().equals(uid)) {
                //Esta Jugando el jugador 2
                actualizarJugada(view.getTag().toString());
            } else {
                Toast.makeText(this, "No es tu turno Aún", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void actualizarJugada(String numeroCasila) {
        int posicionCasilla = Integer.parseInt(numeroCasila);

        if (jugada.getCeldasSeleccionadas().get(posicionCasilla) != 0) {
            Toast.makeText(this, "Seleccione una casilla libre :)", Toast.LENGTH_SHORT).show();
        } else {
            if (jugada.isTurnoJugadorUno()) {
                casillas.get(posicionCasilla).setImageResource(R.drawable.ic_player_one);
                jugada.getCeldasSeleccionadas().set(posicionCasilla, 1);
            } else {
                casillas.get(posicionCasilla).setImageResource(R.drawable.ic_player_two);
                jugada.getCeldasSeleccionadas().set(posicionCasilla, 2);
            }

            if (existeSolucion()) {
                jugada.setGanadorId(uid);
                Toast.makeText(this, "Hay Solución", Toast.LENGTH_SHORT).show();
            } else if (existeEmpate()) {
                Toast.makeText(this, "Hay empate", Toast.LENGTH_SHORT).show();
                jugada.setGanadorId("EMPATE");
            } else {
                cambioTurno();
            }


            //Actualizar en fireBaseStore
            db.collection("jugadas")
                    .document(jugadaId)
                    .set(jugada)
                    .addOnSuccessListener(JuegoActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(JuegoActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Error", "error al guardar la jugada");
                }
            });
        }

    }

    private void cambioTurno() {
        //cambio de turno
        jugada.setTurnoJugadorUno(!jugada.isTurnoJugadorUno());
    }

    private boolean existeEmpate() {
        boolean existe = false;
        //Empate
        boolean hayCasillaLibre = false;
        for (int i = 0; i < 9; i++) {
            if (jugada.getCeldasSeleccionadas().get(i) == 0) {
                hayCasillaLibre = true;
                break;
            }
        }

        if (!hayCasillaLibre) {
            //empate
            existe = true;
        }

        return existe;
    }


    private boolean existeSolucion() {
        boolean existe = false;

        List<Integer> selectedCells = jugada.getCeldasSeleccionadas();
        if (selectedCells.get(0) == selectedCells.get(1)
                && selectedCells.get(1) == selectedCells.get(2)
                && selectedCells.get(2) != 0) { // 0 - 1 - 2
            existe = true;
        } else if (selectedCells.get(3) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(5)
                && selectedCells.get(5) != 0) { // 3 - 4 - 5
            existe = true;
        } else if (selectedCells.get(6) == selectedCells.get(7)
                && selectedCells.get(7) == selectedCells.get(8)
                && selectedCells.get(8) != 0) { // 6 - 7 - 8
            existe = true;
        } else if (selectedCells.get(0) == selectedCells.get(3)
                && selectedCells.get(3) == selectedCells.get(6)
                && selectedCells.get(6) != 0) { // 0 - 3 - 6
            existe = true;
        } else if (selectedCells.get(1) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(7)
                && selectedCells.get(7) != 0) { // 1 - 4 - 7
            existe = true;
        } else if (selectedCells.get(2) == selectedCells.get(5)
                && selectedCells.get(5) == selectedCells.get(8)
                && selectedCells.get(8) != 0) { // 2 - 5 - 8
            existe = true;
        } else if (selectedCells.get(0) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(8)
                && selectedCells.get(8) != 0) { // 0 - 4 - 8
            existe = true;
        } else if (selectedCells.get(2) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(6)
                && selectedCells.get(6) != 0) { // 2 - 4 - 6
            existe = true;
        }

        return existe;
    }

    public void mostrarDialogoGameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = getLayoutInflater().inflate(R.layout.dialogo_game_over, null);
        //Obtenemos los datos del layout
        TextView tvPuntos = v.findViewById(R.id.textViewPuntos);
        TextView tvInformacion = v.findViewById(R.id.textViewInformacion);
        LottieAnimationView gameOverAnimation = v.findViewById(R.id.animation_view);

        builder.setTitle("Game Over");
        builder.setCancelable(false);
        builder.setView(v);

        builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
