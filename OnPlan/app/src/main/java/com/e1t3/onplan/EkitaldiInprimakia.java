package com.e1t3.onplan;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.model.Erabiltzailea;
import com.e1t3.onplan.shared.Values;
import com.e1t3.onplan.ui.dialog.DatePickerFragment;
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

public class EkitaldiInprimakia extends AppCompatActivity implements View.OnClickListener {

    private EditText etNombreEvento, etFechaIn, etHoraIn, etFechaFin, etHoraFin, etAforo, etPresupuesto, etDescripcion;
    private Button btnSiguiente, btnVolverAgenda;
    private SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    public Ekitaldia ekitaldia = new Ekitaldia();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences ekitaldiDatuak;
    private SharedPreferences.Editor editor;
    private SharedPreferences settingssp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_evento);

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

        etNombreEvento = findViewById(R.id.etNombreEvento);
        etFechaIn = findViewById(R.id.etFechaInicio);
        etHoraIn = findViewById(R.id.etHoraInicio);
        etFechaFin = findViewById(R.id.etFechaFin);
        etHoraFin = findViewById(R.id.etHoraFin);
        etAforo = findViewById(R.id.etAforo);
        etPresupuesto = findViewById(R.id.etPresupuesto);
        etDescripcion = findViewById(R.id.etDeskribapena);

        Bundle bundle = getIntent().getExtras();
        int dia, mes, anio;
        dia = bundle.getInt("dia");
        mes = bundle.getInt("mes");
        anio = bundle.getInt("anio");

        String selectedDate = dosDigitos(dia) + "/" + dosDigitos(mes) + "/" + anio;
        etFechaIn.setText(selectedDate);
        etFechaIn.setOnClickListener(this);
        etFechaFin.setOnClickListener(this);

        String ordua = "00:00";
        etHoraIn.setText(ordua);
        etHoraIn.setOnClickListener(this);
        etHoraFin.setText(ordua);
        etHoraFin.setOnClickListener(this);

        btnVolverAgenda = findViewById(R.id.btnVolverAgenda);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnVolverAgenda.setOnClickListener(this);
        btnSiguiente.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnSiguiente.getId()) {
            //guardar datos para crear evento
            Date comprovacion = comprovacion();
            if (comprovacionTiempo2().equals(comprovacion)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_datosMetidos)
                        .setTitle(R.string.dialog_error)
                        .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                etFechaFin.setText("");
                                String ordua = "00:00";
                                etHoraFin.setText(ordua);
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (comprovacionTiempo1().after(comprovacionTiempo2()) || comprovacionTiempo1().equals(comprovacionTiempo2())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_fechaIgualOAnterior)
                        .setTitle(R.string.dialog_error)
                        .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                etFechaFin.setText("");
                                String ordua = "00:00";
                                etHoraFin.setText(ordua);
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else if(!stringIrakurri(etNombreEvento.getText().toString(),findViewById(R.id.etNombreEvento)) || !zenbakiaIrakurri(etAforo.getText().toString(),findViewById(R.id.etAforo)) || !zenbakiaIrakurri(etPresupuesto.getText().toString(),findViewById(R.id.etPresupuesto)) ){

            }else{
                String ekitaldiIzena = etNombreEvento.getText().toString();
                int edukiera = Integer.parseInt(etAforo.getText().toString());
                Double ekitaldiAurrekontua = Double.parseDouble(etPresupuesto.getText().toString());
                String ekitaldiDeskribapena = etDescripcion.getText().toString();

                String hasieraDataOrdua = etFechaIn.getText().toString() + " " + etHoraIn.getText().toString();
                String bukaeraDataOrdua = etFechaFin.getText().toString() + " " + etHoraFin.getText().toString();

                ekitaldia.setIzena(ekitaldiIzena);
                ekitaldia.setAurrekontua(ekitaldiAurrekontua);
                ekitaldia.setDeskribapena(ekitaldiDeskribapena);

                ekitaldiDatuak = getSharedPreferences("datuak", Context.MODE_PRIVATE);
                editor = ekitaldiDatuak.edit();
                editor.clear().apply();
                editor.commit();
                getEranbilytzaileaId();
                editor.putString(Values.EKITALDIAK_IZENA, ekitaldiIzena);
                editor.putString(Values.EKITALDIAK_HASIERAKO_DATA_ORDUA, hasieraDataOrdua);
                editor.putString(Values.EKITALDIAK_BUKAERAKO_DATA_ORDUA, bukaeraDataOrdua);
                editor.putInt("edukiera", edukiera);
                editor.putFloat(Values.EKITALDIAK_AURREKONTUA, ekitaldiAurrekontua.floatValue());
                editor.putString(Values.EKITALDIAK_DESKRIBAPENA, ekitaldiDeskribapena);
                editor.commit();


                Intent i = new Intent(this, EkitaldiInprimakiaGelak.class);
                startActivity(i);
            }
        } else if (view.getId() == btnVolverAgenda.getId()) {
            this.finish();
        } else if (view.getId() == etFechaIn.getId()) {
            showDatePickerDialogInicio();
        } else if (view.getId() == etFechaFin.getId()) {
            showDatePickerDialogFin();
        } else if (view.getId() == etHoraIn.getId()) {
            showTimePickerDialog(etHoraIn);
        } else if (view.getId() == etHoraFin.getId()) {
            showTimePickerDialog(etHoraFin);
        }
    }

    private void showDatePickerDialogInicio() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int anio, int mes, int dia) {
                final String selectedDate = dosDigitos(dia) + "/" + dosDigitos(mes+1) + "/" + anio;
                etFechaIn.setText(selectedDate);
            }
        });
        newFragment.show(this.getSupportFragmentManager(), "datePicker");
    }

    private void showDatePickerDialogFin() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int anio, int mes, int dia) {
                final String selectedDate = dosDigitos(dia) + "/" + dosDigitos(mes+1) + "/" + anio;
                etFechaFin.setText(selectedDate);
            }
        });
        newFragment.show(this.getSupportFragmentManager(), "datePicker");
    }

    private String dosDigitos(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    private Date comprovacionTiempo1() {
        Date inicio = null;
        try {
            inicio = formato.parse(etFechaIn.getText().toString() + " " + etHoraIn.getText().toString());
            return inicio;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return inicio;
    }

    private Date comprovacionTiempo2() {
        Date fin = null;
        try {
            fin = formato.parse(etFechaFin.getText().toString() + " " + etHoraFin.getText().toString());
            return fin;
        } catch (ParseException e) {
            if (fin == null) {
                try {
                    fin = formato.parse("01/01/1800 00:00");
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return fin;
    }

    private Date comprovacion() {
        Date fin = null;
        try {
            fin = formato.parse("01/01/1800 00:00");

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return fin;
    }

    private void showTimePickerDialog(EditText editText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                String denbora = dosDigitos(hourOfDay) + ":" + dosDigitos(minutes);
                editText.setText(denbora);
            }
        }, 0, 0, true);
        timePickerDialog.show();
    }

    public boolean stringIrakurri(String textua, EditText text){
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
    public boolean zenbakiaIrakurri(String textua, EditText text){
        if( textua.length()==0 ) {
            text.setError(getString(R.string.error_campoNecesario));
            return false;
        }else if(Integer.parseInt(textua)<0) {
            text.setError(getString(R.string.error_nadaNegativo));
            return false;
        }else if((!textua.matches("[0-9]+\\.?")) ){
            text.setError(getString(R.string.error_soloNumeros));
            return false;
        }else if (textua.length()>6) {
            text.setError(getString(R.string.error_numeroGrande));
            return false;
        }else{
            return true;
        }
    }

    private void getEranbilytzaileaId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();
        db.collection(Values.ERABILTZAILEAK)
                .whereEqualTo(Values.ERABILTZAILEAK_EMAIL, email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Erabiltzailea erabiltzailea = new Erabiltzailea(document);
                                String erabiltzaileaId = erabiltzailea.getId();
                                editor.putString(Values.EKITALDIAK_ERABILTZAILEA, erabiltzaileaId);
                                editor.commit();
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