package com.e1t3.onplan;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.e1t3.onplan.dao.DAOEkitaldiak;
import com.e1t3.onplan.dao.DAOGertaerak;
import com.e1t3.onplan.databinding.ActivityEkitaldiaEditatuBinding;
import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.model.Gela;
import com.e1t3.onplan.model.Gertaera;
import com.e1t3.onplan.shared.Values;
import com.e1t3.onplan.ui.dialog.DatePickerFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EkitaldiaEditatu extends AppCompatActivity {
    //Layout Android elementuak
    private ActivityEkitaldiaEditatuBinding binding;
    private LinearLayout linearLayout;

    // Datubaserako objektuak
    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DAOEkitaldiak daoEkitaldiak = new DAOEkitaldiak();

    private Ekitaldia ekitaldia;

    private EditText izenaText;
    private EditText deskribapenaText;
    private EditText dataText;
    private EditText orduaText;

    private SharedPreferences settingssp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEkitaldiaEditatuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

        //get String array from enum

        //set the spinners adapter to the previously created one.
        //FloatingActionButton fab = binding.fab;
        linearLayout = binding.getRoot().findViewById(R.id.linearLayout);
        String id = getIntent().getExtras().getString("id");
        setUp(id);

        Button btnAtzera = findViewById(R.id.atzeraEdit);
        btnAtzera.setOnClickListener(v -> {
            Intent intent = new Intent(EkitaldiaEditatu.this, EkitaldiActivity.class);
            intent.putExtra("dia", getIntent().getExtras().getInt("dia"));
            intent.putExtra("mes", getIntent().getExtras().getInt("mes"));
            intent.putExtra("anio", getIntent().getExtras().getInt("anio"));
            intent.putExtra("id", ekitaldia.getId());
            startActivity(intent);
        });

        Button btnBorrar = findViewById(R.id.btnBorrar);
        btnBorrar.setOnClickListener(v -> {
            DAOEkitaldiak daoEkitaldiak = new DAOEkitaldiak();
            DAOGertaerak daoGertaerak = new DAOGertaerak();
            daoGertaerak.gertaerakIdzEzabatu(ekitaldia.getGertaerak());
            daoEkitaldiak.ezabatuEkitaldiaId(ekitaldia.getId());
        });

        Button btnGorde = findViewById(R.id.btnGorde);
        btnGorde.setOnClickListener(v -> {
            ekitaldia.setIzena(Objects.requireNonNull(binding.ekitaldiIzena.getText()).toString());
            ekitaldia.setDeskribapena(Objects.requireNonNull(binding.ekitaldiDeskribapena.getText()).toString());
            daoEkitaldiak.gehituEdoEguneratuEkitaldia(ekitaldia);
            Intent intent = new Intent(EkitaldiaEditatu.this, EkitaldiActivity.class);
            intent.putExtra("id", ekitaldia.getId());
            startActivity(intent);
        });

    }

    @SuppressLint("DefaultLocale")
    public void setUp(String id){
        db.collection(Values.EKITALDIAK)
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
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

                        List<String> ids;
                        ids = (List<String>) document.get(Values.EKITALDIAK_GERTAERAK);
                        assert ids != null;
                        lortuGertaerakIdzEdit(ids, linearLayout, ekitaldia);

                    }
                });
    }

    private void setGelaIzena(TextView gela) {
        db.collection(Values.GELAK)
                .whereIn(FieldPath.documentId(), Collections.singletonList(ekitaldia.getGela()))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Gela g = new Gela(document);
                            gela.setText(g.getIzena());
                        }
                    }
                });
    }

    @SuppressWarnings("deprecation")
    public void lortuGertaerakIdzEdit(List<String> ids, LinearLayout linearLayout, Ekitaldia ekitaldia) {
        if (ids.size() > 0) {
            db.collection(Values.GERTAERAK)
                    .whereIn(FieldPath.documentId(), ids)
                    .get()
                    .addOnCompleteListener(task -> {
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
                                }

                                gertaeraEginda.setClickable(false);

                                LinearLayout textLayout = new LinearLayout(linearLayout.getContext());

                                WindowManager mWinMgr = (WindowManager) linearLayout.getContext().getSystemService(Context.WINDOW_SERVICE);
                                int displayWidth = mWinMgr.getDefaultDisplay().getWidth();

                                textLayout.setMinimumWidth(3*displayWidth/5);

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


                                LinearLayout buttonLayout = new LinearLayout(linearLayout.getContext());
                                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                                FloatingActionButton ezabatuBotoia = new FloatingActionButton(linearLayout.getContext());
                                ezabatuBotoia.setImageResource(android.R.drawable.ic_delete);
                                ezabatuBotoia.setCustomSize(100);
                                ezabatuBotoia.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FE4444")));

                                ezabatuBotoia.setOnClickListener(v -> {
                                    linearLayout.removeView(linearLayoutGertaera);
                                    DAOGertaerak daoGertaerak = new DAOGertaerak();
                                    daoGertaerak.ezabatuGertaeraIdz(gertaera.getId());
                                    ekitaldia.ezabatuGertaera(gertaera.getId());
                                    DAOEkitaldiak daoEkitaldiak = new DAOEkitaldiak();
                                    daoEkitaldiak.gehituEdoEguneratuEkitaldia(ekitaldia);
                                });

                                buttonLayout.addView(ezabatuBotoia);

                                linearLayoutGertaera.addView(buttonLayout);
                                linearLayout.addView(linearLayoutGertaera);



                            }

                            //ADD button
                            LinearLayout layoutButton = new LinearLayout(linearLayout.getContext());
                            layoutButton.setOrientation(LinearLayout.HORIZONTAL);
                            layoutButton.setPadding(16, 16, 16, 16);

                            FloatingActionButton gehituBotoia = new FloatingActionButton(linearLayout.getContext());
                            gehituBotoia.setImageResource(android.R.drawable.ic_input_add);
                            gehituBotoia.setCustomSize(100);

                            gehituBotoia.setOnClickListener(v -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(linearLayout.getContext());
                                builder.setTitle(R.string.title_gertaera_popup);
                                // I'm using fragment here so I'm using getView() to provide ViewGroup
                                // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                                View viewInflated = LayoutInflater.from(linearLayout.getContext()).inflate(R.layout.gehitu_gertera_popup, (ViewGroup) null, false);
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                builder.setView(viewInflated);

                                izenaText = (EditText) viewInflated.findViewById(R.id.popupIzena);
                                deskribapenaText = (EditText) viewInflated.findViewById(R.id.popupDeskribapena);
                                dataText = (EditText) viewInflated.findViewById(R.id.popupData);
                                dataText.setText("");

                                dataText.setOnClickListener(v15 -> showDatePickerDialogFin());
                                orduaText = (EditText) viewInflated.findViewById(R.id.popupOrdua);
                                String ordua = "00:00";
                                orduaText.setText(ordua);
                                orduaText.setOnClickListener(v16 -> showTimePickerDialog());

                                Button gehitu = (Button) viewInflated.findViewById(R.id.sortu);
                                Button atzera = (Button) viewInflated.findViewById(R.id.atzeraEkitaldiak);
                                AlertDialog alertDialog = builder.create();
                                // Set up the buttons
                                gehitu.setOnClickListener(v17 -> {
                                    if(validate()) {
                                        // get miliseconds from string date
                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                        Date date = null;
                                        try {
                                            date = sdf.parse(dataText.getText().toString() + " " + orduaText.getText().toString());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        assert date != null;
                                        Gertaera g = new Gertaera(ekitaldia.getId() + "G" +ekitaldia.getGertaerak().size()+1 ,izenaText.getText().toString(), deskribapenaText.getText().toString(), false, new Timestamp(date));
                                        DAOGertaerak daoGertaerak = new DAOGertaerak();
                                        ekitaldia.gehituGertaera(g);
                                        daoGertaerak.gehituEdoEguneratuGertaera(g);
                                        //go to EkitaldiaEditatu
                                        Intent intent = new Intent(linearLayout.getContext(), EkitaldiaEditatu.class);
                                        intent.putExtra("id", ekitaldia.getId());
                                        startActivity(intent);
                                        alertDialog.dismiss();
                                    }
                                });
                                atzera.setOnClickListener(v13 -> alertDialog.dismiss());
                                builder.show();
                            });

                            layoutButton.addView(gehituBotoia);
                            linearLayout.addView(layoutButton);

                        } else {
                            LinearLayout linearLayoutGertaera = new LinearLayout(linearLayout.getContext());
                            linearLayoutGertaera.setOrientation(LinearLayout.HORIZONTAL);
                            linearLayoutGertaera.setPadding(16, 16, 16, 16);

                            FloatingActionButton gehituBotoia = new FloatingActionButton(linearLayout.getContext());
                            gehituBotoia.setImageResource(R.drawable.gertaera_add);
                            gehituBotoia.setCustomSize(100);

                            gehituBotoia.setOnClickListener(v -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(linearLayout.getContext());
                                builder.setTitle(R.string.title_gertaera_popup);
                                // I'm using fragment here so I'm using getView() to provide ViewGroup
                                // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                                View viewInflated = LayoutInflater.from(linearLayout.getContext()).inflate(R.layout.gehitu_gertera_popup, (ViewGroup) null, false);
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                builder.setView(viewInflated);

                                izenaText = (EditText) viewInflated.findViewById(R.id.popupIzena);
                                deskribapenaText = (EditText) viewInflated.findViewById(R.id.popupDeskribapena);
                                dataText = (EditText) viewInflated.findViewById(R.id.popupData);
                                dataText.setText("");

                                dataText.setOnClickListener(v14 -> showDatePickerDialogFin());
                                orduaText = (EditText) viewInflated.findViewById(R.id.popupOrdua);
                                String ordua = "00:00";
                                orduaText.setText(ordua);
                                orduaText.setOnClickListener(v18 -> showTimePickerDialog());

                                Button gehitu = (Button) viewInflated.findViewById(R.id.sortu);
                                Button atzera = (Button) viewInflated.findViewById(R.id.atzeraEkitaldiak);
                                AlertDialog alertDialog = builder.create();
                                // Set up the buttons
                                gehitu.setOnClickListener(v12 -> {
                                    if(validate()) {
                                        // get miliseconds from string date
                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                        Date date = null;
                                        try {
                                            date = sdf.parse(dataText.getText().toString() + " " + orduaText.getText().toString());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        assert date != null;
                                        Gertaera g = new Gertaera(ekitaldia.getId() + "G" +ekitaldia.getGertaerak().size()+1 ,izenaText.getText().toString(), deskribapenaText.getText().toString(), false, new Timestamp(date));
                                        DAOGertaerak daoGertaerak = new DAOGertaerak();
                                        ekitaldia.gehituGertaera(g);
                                        daoGertaerak.gehituEdoEguneratuGertaera(g);
                                        //go to EkitaldiaEditatu
                                        Intent intent = new Intent(linearLayout.getContext(), EkitaldiaEditatu.class);
                                        intent.putExtra("id", ekitaldia.getId());
                                        startActivity(intent);
                                        alertDialog.dismiss();
                                    }
                                });
                                atzera.setOnClickListener(v1 -> {
                                    Intent intent = new Intent(linearLayout.getContext(), EkitaldiaEditatu.class);
                                    intent.putExtra("id", ekitaldia.getId());
                                    startActivity(intent);
                                    alertDialog.dismiss();
                                });
                                builder.show();
                            });

                            linearLayoutGertaera.addView(gehituBotoia);
                            linearLayout.addView(linearLayoutGertaera);
                        }

                    });
        }

    }

    private void showDatePickerDialogFin() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance((datePicker, anio, mes, dia) -> {
            final String selectedDate = dosDigitos(dia) + "/" + dosDigitos(mes+1) + "/" + anio;
            dataText.setText(selectedDate);
        });
        newFragment.show(this.getSupportFragmentManager(), "datePicker");
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hourOfDay, minutes) -> {
            String denbora = dosDigitos(hourOfDay) + ":" + dosDigitos(minutes);
            orduaText.setText(denbora);
        }, 0, 0, true);
        timePickerDialog.show();
    }

    private boolean validate(){
        if (this.izenaText.getText().toString().isEmpty()) {
            this.izenaText.setError(getString(R.string.error_beharreskoa));
            return false;
        }
        if (this.dataText.getText().toString().isEmpty()) {
            this.dataText.setError(getString(R.string.error_beharreskoa));
            return false;
        }
        if (this.orduaText.getText().toString().isEmpty()) {
            this.orduaText.setError(getString(R.string.error_beharreskoa));
            return false;
        }
        return egunaKonprobatu();
    }

    private String dosDigitos(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    //check if date and time is correct
    private boolean egunaKonprobatu() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date;
        try {
            date = sdf.parse(dataText.getText().toString() + " " + orduaText.getText().toString());
        } catch (ParseException e) {

            return false;
        }
        assert date != null;
        if (ekitaldia.getDataTarteanDago(date)) return true;
        this.dataText.setError(getString(R.string.error_data));
        return false;
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
