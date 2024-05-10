package com.e1t3.onplan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.model.Gertaera;
import com.e1t3.onplan.shared.Values;
import com.e1t3.onplan.ui.dialog.DatePickerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EkitaldiInprimakiaGertaerak extends AppCompatActivity implements View.OnClickListener {

    private EditText etGertaeraIzena, etGertaeraDeskribapena, etGertaeraEguna, etGertaeraOrdua;
    private Button btnVolverSuceso, btnCrearSuceso;
    private SharedPreferences ekitaldi;
    private SharedPreferences.Editor editor;
    private SimpleDateFormat formato;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static int sartu = 0;
    private SharedPreferences settingssp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ekitaldi_inprimakia_gertaerak);

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

        etGertaeraIzena = findViewById(R.id.etGertaeraIzena);
        etGertaeraDeskribapena = findViewById(R.id.etGertaeraDeskribapena);
        etGertaeraEguna = findViewById(R.id.etGertaeraEguna);
        etGertaeraOrdua = findViewById(R.id.etGertaeraOrdua);
        btnVolverSuceso = findViewById(R.id.btnVolverSuceso);
        btnCrearSuceso = findViewById(R.id.btnCrearSuceso);

        etGertaeraEguna.setOnClickListener(this);
        etGertaeraOrdua.setOnClickListener(this);
        btnVolverSuceso.setOnClickListener(this);
        btnCrearSuceso.setOnClickListener(this);

        ekitaldi = getSharedPreferences("datuak", Context.MODE_PRIVATE);
        editor = ekitaldi.edit();
        formato = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == etGertaeraEguna.getId()) {
            showDatePickerDialog();
        } else if (view.getId() == etGertaeraOrdua.getId()) {
            showTimePickerDialog();
        } else if (view.getId() == btnVolverSuceso.getId() && sartu == 0) {
            finish();
        } else if (view.getId() == btnVolverSuceso.getId() && sartu != 0) {
            Intent intent = new Intent(EkitaldiInprimakiaGertaerak.this, MainActivity.class);
            startActivity(intent);
        } else if (view.getId() == btnCrearSuceso.getId()) {
            if (egunaKonprobatu()) {
                if(stringIrakurri(etGertaeraIzena.getText().toString(),findViewById(R.id.etGertaeraIzena))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.dialog_masSucesos)
                            .setTitle(R.string.dialog_aviso)
                            .setPositiveButton(R.string.dialog_si, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (sartu != 0) {
                                        ekitaldiaEzabatu();
                                    }
                                    ekitaldiDatuakGorde();
                                    gertaeraDatuakGorde();
                                    gertaeraEkitaldianSartu();
                                    etGertaeraIzena.setText(null);
                                    etGertaeraDeskribapena.setText(null);
                                    etGertaeraEguna.setText(null);
                                    etGertaeraOrdua.setText(null);
                                    btnVolverSuceso.setText(R.string.dialog_salir);
                                    sartu++;
                                }
                            })
                            .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (sartu != 0) {
                                        ekitaldiaEzabatu();
                                    }
                                    ekitaldiDatuakGorde();
                                    gertaeraDatuakGorde();
                                    gertaeraEkitaldianSartu();
                                    Intent intent = new Intent(EkitaldiInprimakiaGertaerak.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int anio, int mes, int dia) {
                final String selectedDate = dosDigitos(dia) + "/" + dosDigitos(mes+1) + "/" + anio;
                etGertaeraEguna.setText(selectedDate);
            }
        });
        newFragment.show(this.getSupportFragmentManager(), "datePicker");
    }

    private String dosDigitos(int n) { return (n<=9) ? ("0"+n) : String.valueOf(n); }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                String denbora = dosDigitos(hourOfDay) + ":" + dosDigitos(minutes);
                etGertaeraOrdua.setText(denbora);
            }
        }, 0, 0, true);
        timePickerDialog.show();
    }

    private boolean egunaKonprobatu() {
        if (egunKonprobaketa2().equals(konprobaketa())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_fechaYHoraSucVacio)
                    .setTitle(R.string.dialog_error)
                    .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            etGertaeraEguna.setText("");
                            String ordua = "00:00";
                            etGertaeraOrdua.setText(ordua);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else if (egunKonprobaketa1().after(egunKonprobaketa2()) && egunKonprobaketa3().after(egunKonprobaketa2())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_fechaAnterior)
                    .setTitle(R.string.dialog_error)
                    .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            etGertaeraEguna.setText("");
                            String ordua = "00:00";
                            etGertaeraOrdua.setText(ordua);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else if (egunKonprobaketa3().before(egunKonprobaketa2()) && egunKonprobaketa1().before(egunKonprobaketa2())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_fechaPosterior)
                    .setTitle(R.string.dialog_error)
                    .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            etGertaeraEguna.setText("");
                            String ordua = "00:00";
                            etGertaeraOrdua.setText(ordua);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else {
            return true;
        }
    }

    private Date konprobaketa() {
        Date eguna = null;
        try {
            eguna = formato.parse("01/01/1700 00:00");

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return eguna;
    }

    private Date egunKonprobaketa1() {
        Date hasieraEguna = null;
        try {
            hasieraEguna = formato.parse(ekitaldi.getString(Values.EKITALDIAK_HASIERAKO_DATA_ORDUA, "01/01/1800 00:00"));
            return hasieraEguna;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return hasieraEguna;
    }

    private Date egunKonprobaketa2() {
        Date sartutakoEguna = null;
        try {
            sartutakoEguna = formato.parse(etGertaeraEguna.getText().toString() + " " + etGertaeraOrdua.getText().toString());
            return sartutakoEguna;
        } catch (ParseException e) {
            if (sartutakoEguna == null) {
                try {
                    sartutakoEguna = formato.parse("01/01/1700 00:00");
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return sartutakoEguna;
    }

    private Date egunKonprobaketa3() {
        Date bukaeraEguna = null;
        try {
            bukaeraEguna = formato.parse(ekitaldi.getString(Values.EKITALDIAK_BUKAERAKO_DATA_ORDUA, "01/01/1800 00:00"));
            return bukaeraEguna;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bukaeraEguna;
    }

    private boolean stringIrakurri(String textua, EditText text){
        if( textua.length()==0 )  {
            text.setError(getString(R.string.error_campoNecesario));
            return false;
        }else if((!textua.matches("[a-zA-Z ]+\\.?"))){
            text.setError(getString(R.string.error_soloLetras));
            return false;
        }else{
            return true;
        }
    }

    private Date dataAldatu (String data) {
        Date gertaeraDataOrdua = null;
        try {
            gertaeraDataOrdua =  formato.parse(data);
            return gertaeraDataOrdua;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return gertaeraDataOrdua;
    }

    private void ekitaldiDatuakGorde() {
        Timestamp hasieraDataOrdua = new Timestamp(dataAldatu(ekitaldi.getString(Values.EKITALDIAK_HASIERAKO_DATA_ORDUA, "")));
        Timestamp bukareaDataOrdua = new Timestamp(dataAldatu(ekitaldi.getString(Values.EKITALDIAK_BUKAERAKO_DATA_ORDUA, "")));

        CollectionReference ekitaldiak = db.collection(Values.EKITALDIAK);
        Map<String, Object> ekitaldia = new HashMap();
        ekitaldia.put(Values.EKITALDIAK_IZENA,ekitaldi.getString(Values.EKITALDIAK_IZENA, ""));
        ekitaldia.put(Values.EKITALDIAK_EKITALDI_MOTA,ekitaldi.getString(Values.EKITALDIAK_EKITALDI_MOTA, ""));
        ekitaldia.put(Values.EKITALDIAK_AURREKONTUA,ekitaldi.getFloat(Values.EKITALDIAK_AURREKONTUA, 0));
        ekitaldia.put(Values.EKITALDIAK_HASIERAKO_DATA_ORDUA, hasieraDataOrdua);
        ekitaldia.put(Values.EKITALDIAK_BUKAERAKO_DATA_ORDUA, bukareaDataOrdua);
        ekitaldia.put(Values.EKITALDIAK_DESKRIBAPENA,ekitaldi.getString(Values.EKITALDIAK_DESKRIBAPENA, ""));
        ekitaldia.put(Values.EKITALDIAK_ERABILTZAILEA,ekitaldi.getString(Values.EKITALDIAK_ERABILTZAILEA, ""));
        ekitaldia.put(Values.EKITALDIAK_GELA,ekitaldi.getString(Values.EKITALDIAK_GELA, ""));
        ekitaldia.put(Values.EKITALDIAK_GERTAERAK, FieldValue.arrayUnion(ekitaldi.getString(Values.EKITALDIAK_GERTAERAK, "")));
        ekitaldiak.document().set(ekitaldia);
    }

    private void gertaeraDatuakGorde() {
        String gertaeraEguna = etGertaeraEguna.getText().toString() + " " + etGertaeraOrdua.getText().toString();
        Timestamp gertaeraDataOrdua = new Timestamp(dataAldatu(gertaeraEguna));

        CollectionReference gertaerak = db.collection(Values.GERTAERAK);
        Map<String, Object> gertaera = new HashMap();
        gertaera.put(Values.GERTAERAK_IZENA,etGertaeraIzena.getText().toString());
        gertaera.put(Values.GERTAERAK_EGIN_DA,false);
        gertaera.put(Values.GERTAERAK_DESKRIBAPENA,etGertaeraDeskribapena.getText().toString());
        gertaera.put(Values.GERTAERAK_ORDUA, gertaeraDataOrdua);
        gertaerak.document().set(gertaera);
    }

    private void getIdGertaera() {
        db.collection(Values.GERTAERAK)
                .whereEqualTo(Values.GERTAERAK_IZENA, etGertaeraIzena.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Gertaera gertaera =  new Gertaera(document);
                                String gertaeraId = gertaera.getId();
                                editor.putString(Values.EKITALDIAK_GERTAERAK, gertaeraId);
                                editor.commit();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void gertaeraEkitaldianSartu() {
        getIdGertaera();
        db.collection(Values.EKITALDIAK)
                .whereEqualTo(Values.EKITALDIAK_IZENA, ekitaldi.getString(Values.EKITALDIAK_IZENA, ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Ekitaldia ekitaldia =  new Ekitaldia(document);
                                String ekitaldiaId = ekitaldia.getId();
                                editor.putString("id", ekitaldiaId);
                                editor.commit();
                                DocumentReference ekitaldiak = db.collection(Values.EKITALDIAK).document(ekitaldiaId);
                                ekitaldiak.update(Values.EKITALDIAK_GERTAERAK, FieldValue.arrayRemove(""));
                                ekitaldiak.update(Values.EKITALDIAK_GERTAERAK, FieldValue.arrayUnion(ekitaldi.getString(Values.EKITALDIAK_GERTAERAK, "")));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void ekitaldiaEzabatu() {
        db.collection(Values.EKITALDIAK).document(ekitaldi.getString( "id", ""))
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
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