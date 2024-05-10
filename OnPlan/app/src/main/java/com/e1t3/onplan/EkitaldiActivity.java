package com.e1t3.onplan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.e1t3.onplan.dao.DAOGertaerak;
import com.e1t3.onplan.databinding.ActivityEkitaldiaBinding;
import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.model.Gela;
import com.e1t3.onplan.model.Gertaera;
import com.e1t3.onplan.shared.EkitaldiMota;
import com.e1t3.onplan.shared.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EkitaldiActivity extends AppCompatActivity {

    //Layout Android elementuak
    private ActivityEkitaldiaBinding binding;
    private LinearLayout linearLayout;

    // Datubaserako objektuak
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FloatingActionButton fab;

    private Ekitaldia ekitaldia;

    private SharedPreferences settingssp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityEkitaldiaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

        //get String array from enum
        String[] motak = new String[EkitaldiMota.values().length];
        for (int i = 0; i < EkitaldiMota.values().length; i++) {
            motak[i] = EkitaldiMota.values()[i].toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(binding.getRoot().getContext(), android.R.layout.simple_spinner_dropdown_item, motak);
        //set the spinners adapter to the previously created one.
        //FloatingActionButton fab = binding.fab;
        linearLayout = binding.getRoot().findViewById(R.id.linearLayout);
        String id = getIntent().getExtras().getString("id");
        setUp(id);

        Button btnAtzera = binding.getRoot().findViewById(R.id.atzeraEkitaldia);
        btnAtzera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EkitaldiActivity.this, EkitaldiakIkusi.class);
                intent.putExtra("dia", getIntent().getExtras().getInt("dia"));
                intent.putExtra("mes", getIntent().getExtras().getInt("mes"));
                intent.putExtra("anio", getIntent().getExtras().getInt("anio"));
                startActivity(intent);
            }
        });

        fab = binding.getRoot().findViewById(R.id.floatingActionButton);
        fab.setCustomSize(130);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EkitaldiActivity.this, EkitaldiaEditatu.class);
                intent.putExtra("dia", getIntent().getExtras().getInt("dia"));
                intent.putExtra("mes", getIntent().getExtras().getInt("mes"));
                intent.putExtra("anio", getIntent().getExtras().getInt("anio"));
                intent.putExtra("id", ekitaldia.getId());
                startActivity(intent);
            }
        });
    }

    private void setGelaIzena(TextView gela) {
        db.collection(Values.GELAK)
                .whereIn(FieldPath.documentId(), Collections.singletonList(ekitaldia.getGela()))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Gela g = new Gela(document);
                                gela.setText(g.getIzena());
                            }
                        }
                    }
                });
    }

    public void setUp(String id){
        db.collection(Values.EKITALDIAK)
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            ekitaldia = new Ekitaldia(document);

                            TextView dataH = binding.getRoot().findViewById(R.id.ekitaldiDataHasiera);
                            dataH.setText(ekitaldia.getHasierakoDataOrdua());
                            TextView dataB = binding.getRoot().findViewById(R.id.ekitaldiDataBukaera);
                            dataB.setText(ekitaldia.getBukaerakoDataOrdua());

                            TextView izena = binding.getRoot().findViewById(R.id.ekitaldiIzena);
                            izena.setText(ekitaldia.getIzena());

                            TextView deskribapena = binding.getRoot().findViewById(R.id.ekitaldiDeskribapena);
                            deskribapena.setText(ekitaldia.getDeskribapena());

                            TextView aurrekontua = binding.getRoot().findViewById(R.id.ekitaldiAurrekontua);
                            aurrekontua.setText(String.format("%.2f",ekitaldia.getAurrekontua()));

                            TextView gela = binding.getRoot().findViewById(R.id.ekitaldiGela);
                            setGelaIzena(gela);


                            List<String> ids = ekitaldia.getGertaerak();
                             if (ids.size() > 0)lortuGertaerakIdz(ids, linearLayout);


                        } else { }
                    }
                });
    }

    public List<Gertaera> lortuGertaerakIdz(List<String> ids, LinearLayout linearLayout) {
        List<Gertaera> gertaerak= new ArrayList<>();

        db.collection(Values.GERTAERAK)
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Gertaera gertaera = new Gertaera(document);
                                LinearLayout linearLayoutGertaera = new LinearLayout(linearLayout.getContext());
                                linearLayoutGertaera.setOrientation(LinearLayout.HORIZONTAL);
                                linearLayoutGertaera.setGravity(Gravity.CENTER_VERTICAL);
                                linearLayoutGertaera.setPadding(16, 16, 16, 16);

                                TextView gertaeraOrdua = new TextView(linearLayout.getContext());
                                gertaeraOrdua.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                                gertaeraOrdua.setTextColor(Color.parseColor("#004f53"));

                                LinearLayout checkLayout = new LinearLayout(linearLayout.getContext());

                                CheckBox gertaeraEginda = new CheckBox(linearLayout.getContext());
                                if (gertaera.eginDa()) {
                                    gertaeraEginda.setChecked(true);
                                    gertaeraEginda.setClickable(false);
                                }
                                gertaeraEginda.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) {
                                        gertaera.setEginda();
                                        gertaeraEginda.setClickable(false);
                                        DAOGertaerak daoGertaerak = new DAOGertaerak();
                                        daoGertaerak.gehituEdoEguneratuGertaera(gertaera);

                                    }
                                });

                                LinearLayout textLayout = new LinearLayout(linearLayout.getContext());

                                TextView gertaeraIzena = new TextView(linearLayout.getContext());
                                gertaeraIzena.setTextSize(1,20);
                                gertaeraIzena.setTextColor(Color.parseColor("#001e20"));

                                TextView gertaeraDeskribapena = new TextView(linearLayout.getContext());
                                gertaeraDeskribapena.setTextColor(Color.parseColor("#004f53"));



                                linearLayoutGertaera.addView(gertaeraOrdua);
                                checkLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayoutGertaera.addView(checkLayout);
                                checkLayout.addView(gertaeraEginda);
                                textLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayoutGertaera.addView(textLayout);
                                textLayout.addView(gertaeraIzena);
                                textLayout.addView(gertaeraDeskribapena);

                                gertaeraOrdua.setText(gertaera.getOrdua());
                                gertaeraEginda.setChecked(gertaera.eginDa());
                                gertaeraIzena.setText(gertaera.getIzena());
                                gertaeraDeskribapena.setText(gertaera.getDeskribapena());

                                linearLayout.addView(linearLayoutGertaera);
                            }

                        } else {
                        }
                    }
                });
        return gertaerak;
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