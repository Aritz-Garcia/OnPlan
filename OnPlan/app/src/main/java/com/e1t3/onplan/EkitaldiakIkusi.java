package com.e1t3.onplan;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.model.Erabiltzailea;
import com.e1t3.onplan.shared.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EkitaldiakIkusi extends AppCompatActivity{


    private FirebaseUser user;
    private String email;
    private Date fecha;
    private LinearLayout lista;
    private  ArrayAdapter<Ekitaldia> adapter;
    private TextView nada;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SharedPreferences settingssp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_eventos);

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();


        Button btnAtzera = findViewById(R.id.atzeraEkitaldiakIkusi);
        btnAtzera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EkitaldiakIkusi.this, MainActivity.class);
                startActivity(intent);
            }
        });

        nada = findViewById(R.id.nada);
        nada.setVisibility(View.INVISIBLE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();
        lista = findViewById(R.id.lista);
        erabiltzaileId();
        if(lista == null){
            nada.setVisibility(View.VISIBLE);
        }



    }

    public void erabiltzaileId(){
        db.collection(Values.ERABILTZAILEAK)
                .whereEqualTo(Values.ERABILTZAILEAK_EMAIL, email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Erabiltzailea erabiltzailea = new Erabiltzailea(document);
                                ekitaldilista(erabiltzailea);
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private String dosDigitos(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }
    public void ekitaldilista(Erabiltzailea erabiltzailea){
        int dia, mes, anio;
        dia = getIntent().getExtras().getInt("dia");
        mes = getIntent().getExtras().getInt("mes");
        anio = getIntent().getExtras().getInt("anio");

        String selectedDate = anio + "/" + mes + "/" + dia;
        SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
        try {
            fecha = formato.parse(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (erabiltzailea.adminDa()) {
            getAdminEvents();
        } else {
            getEvents(erabiltzailea);
        }


    }
    private void getEvents(Erabiltzailea erabiltzailea) {
        db.collection(Values.EKITALDIAK)
                .whereEqualTo(Values.EKITALDIAK_ERABILTZAILEA,erabiltzailea.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Ekitaldia ekitaldia = new Ekitaldia(document);
                                //create button with the name of the event
                                if (ekitaldia.getDataTarteanDago(fecha)) {
                                    Button btn = new Button(EkitaldiakIkusi.this);

                                    btn.setText(ekitaldia.getIzena());
                                    btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(EkitaldiakIkusi.this, EkitaldiActivity.class);

                                            intent.putExtra("dia", getIntent().getExtras().getInt("dia"));
                                            intent.putExtra("mes", getIntent().getExtras().getInt("mes"));
                                            intent.putExtra("anio", getIntent().getExtras().getInt("anio"));
                                            intent.putExtra("id", ekitaldia.getId());
                                            startActivity(intent);
                                        }
                                    });
                                    lista.addView(btn);
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void getAdminEvents() {
        db.collection(Values.EKITALDIAK)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Ekitaldia ekitaldia = new Ekitaldia(document);
                                //create button with the name of the event
                                if (ekitaldia.getDataTarteanDago(fecha)) {
                                    Button btn = new Button(EkitaldiakIkusi.this);
                                    btn.setText(ekitaldia.getIzena());
                                    btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(EkitaldiakIkusi.this, EkitaldiActivity.class);
                                            intent.putExtra("id", ekitaldia.getId());
                                            startActivity(intent);
                                        }
                                    });
                                    lista.addView(btn);
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void setDayNight() {
        boolean oscuro = settingssp.getBoolean("oscuro", false);
        if (oscuro) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}