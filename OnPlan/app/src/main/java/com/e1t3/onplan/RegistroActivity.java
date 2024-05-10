package com.e1t3.onplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import com.e1t3.onplan.dao.DAOErabiltzaileak;
import com.e1t3.onplan.shared.Values;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    /*Hemen sortu egingo ditugu programa osoan erabiliko ditugun aldagaiak.Adibidez labelak, botoiak*/
    private static final String TAG = "GoogleSignIn";
    public TextView izena,abizena,dni,telefonoa,emaila,pasahitza1,pasahitza2, radioerror;
    public  RadioButton aukeratuta, aukeratuta2;
    /*Firebase aldagaiak */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private SharedPreferences settingssp;
    /*Hau izango da  balidazioen erantzunak hartzeko*/
    public boolean[] egokia = new boolean[7];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();
        
        mAuth = FirebaseAuth.getInstance();

        aukeratuta = findViewById(R.id.RBPersona);
        aukeratuta2= findViewById(R.id.RBEmpresa);
        radioerror = findViewById(R.id.radioerror);

        Button gorde = findViewById(R.id.GordeBotoia);
        gorde.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(aukeratuta.isChecked() ||  aukeratuta2.isChecked()) {
                    radioerror.setVisibility(View.INVISIBLE);
                    datuakhartu();
                    egokia[0] = stringIrakurri(izena.getText().toString(), findViewById(R.id.izenaTextua));
                    if (aukeratuta.isChecked()) {
                        abizena =  findViewById(R.id.AbizenaTextua);
                        egokia[1] = stringIrakurri(abizena.getText().toString(), findViewById(R.id.AbizenaTextua));
                    } else {
                        egokia[1] = true;
                    }
                    egokia[2] = dnikonprobatu(dni.getText().toString(), findViewById(R.id.DNITextua));
                    egokia[3] = zenbakiaIrakurri(telefonoa.getText().toString(), findViewById(R.id.TelefonoTextua));
                    egokia[4] = emailkonprobatu(emaila.getText().toString(), findViewById(R.id.EmailTextua));
                    egokia[5] = pasahitzaIrakurri(pasahitza1.getText().toString(), findViewById(R.id.Pasahitza1Textua));
                    egokia[6] = pasahitzaKonfirmatu(pasahitza1.getText().toString(), pasahitza2.getText().toString(), findViewById(R.id.Pasahitza2Textua));
                    if (egokia[0] && egokia[1] && egokia[2] && egokia[3] && egokia[4] && egokia[5] && egokia[6]) {
                        datuakbidali();
                    }
                }else{
                    radioerror.setVisibility(View.VISIBLE);
                    }
            }
        });
        /*Ez du datuak gordetzen eta login activity hasi egiten da*/
        Button ezeztatu = findViewById(R.id.EzeztatuBotoia);
        ezeztatu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Aukeratzen badu Pertsona radiobutton Abizena jartzeko laukia agertuko da
    public void radiobuttonkonporbatu(View v){
        if(aukeratuta.isChecked()){
            findViewById(R.id.AbizenaTextua).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.AbizenaTextua).setVisibility(View.INVISIBLE);
        }
    }

    //datuak hartu egiten ditugu activity_registro geroago konprobatzeko bere egitura
    public void datuakhartu(){
        izena = findViewById(R.id.izenaTextua);
        dni = findViewById(R.id.DNITextua);
        telefonoa = findViewById(R.id.TelefonoTextua);
        emaila = findViewById(R.id.EmailTextua);
        pasahitza1 = findViewById(R.id.Pasahitza1Textua);
        pasahitza2 = findViewById(R.id.Pasahitza2Textua);
    }

    /*Sortu egiten ditugu documentu berri bat firebase data basean eta firebase autenthification erabiltzailearen datuekin.
    *Ondoren MainActivity hasiko da bere seisioarekin hasita*/
    public void datuakbidali(){
            mAuth.createUserWithEmailAndPassword(emaila.getText().toString(), pasahitza1.getText().toString()).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    CollectionReference erbiltzaileak = db.collection(Values.ERABILTZAILEAK);
                    Map<String, Object> erbiltzailea = new HashMap();
                    erbiltzailea.put(Values.ERABILTZAILEAK_IZENA,izena.getText().toString());
                    if (aukeratuta.isChecked()) {
                        erbiltzailea.put(Values.ERABILTZAILEAK_ABIZENA, abizena.getText().toString());
                        erbiltzailea.put(Values.ERABILTZAILEAK_ENPRESA_DA,false);
                    }else{
                        erbiltzailea.put(Values.ERABILTZAILEAK_ENPRESA_DA,true);
                    }
                    erbiltzailea.put(Values.ERABILTZAILEAK_TELEFONOA,telefonoa.getText().toString());
                    erbiltzailea.put(Values.ERABILTZAILEAK_EMAIL,emaila.getText().toString());
                    erbiltzailea.put(Values.ERABILTZAILEAK_NAN_IFZ,dni.getText().toString());
                    erbiltzailea.put(Values.ERABILTZAILEAK_ADMIN,false);
                    erbiltzaileak.document().set(erbiltzailea);
                    Log.d(TAG, "signInWithCredential:success");
                    Intent i = new Intent(RegistroActivity.this, MainActivity.class);
                    startActivity(i);
                    RegistroActivity.this.finish();
                } else {
                    emaila.setError(getString(R.string.error_repetirEmail));
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                }
            });


    }


    /*Balidazio metodo honek erabiltzen da bakarrik textua sartu ahal izateko*/
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

    /*Balidazio metodo honek erabiltzen da bakarrik zenbakiak eta 9 luzeera sartu ahal izateko*/
    public boolean zenbakiaIrakurri(String textua, EditText text){
        if( textua.length()==0 ) {
            text.setError(getString(R.string.error_campoNecesario));
            return false;
        }else if(textua.length()!=9) {
            text.setError(getString(R.string.error_maximoNueve));
            return false;
        }else if((!textua.matches("[0-9]+\\.?")) ){
            text.setError(getString(R.string.error_soloNumeros));
            return false;
        }else{
            return true;
        }
    }

    /*Balidazio metodo honek erabiltzen da bakarrik dni formatua sartu ahal izateko*/
    public boolean dnikonprobatu(String textua, EditText text){
            if( textua.length()==0 )  {
                text.setError(getString(R.string.error_campoNecesario));
                return false;
            }else if(!nif(textua, text)){
                return false;
            }else{
                return true;
            }
    }

    public boolean nif(String textua, EditText text) {
        int numero;
        String numero1;
        String letr;
        String letra;

        if(textua.matches("[0-9]{8}[A-Z]")){
            numero1 = textua.substring(0,textua.length()-1);
            letr = textua.substring(textua.length()-1,textua.length());
            numero = Integer.parseInt(numero1) % 23;
            letra= "TRWAGMYFPDXBNJZSQVHLCKET";
            letra=letra.substring(numero,numero+1);
            if (!letra.equals(letr.toUpperCase())) {
                text.setError(getString(R.string.error_letra));
                return false;
            }else{
                return true;
            }
        }else{
            text.setError(getString(R.string.error_formatoDNI));
           return false;
        }
    }

    /*Balidazio metodo honek erabiltzen da bakarrik emaila formatua sartu ahal izateko*/
    public boolean emailkonprobatu(String textua, EditText text){
            if( textua.length()==0 )  {
                text.setError(getString(R.string.error_campoNecesario));
                return false;
            }else if((!textua.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"))){
                text.setError(getString(R.string.error_formatoEmail));
                return false;
            }else{
                return true;
            }
    }

    /*Balidazio metodo honek erabiltzen da bakarrik textua eta gutxienez 6 karaktere sartu ahal izateko*/
    public boolean pasahitzaIrakurri(String cadena, EditText text){
        if(cadena.length()==0){
            text.setError(getString(R.string.error_campoNecesario));
            return false;
        }else if(cadena.length()<6) {
            text.setError(getString(R.string.error_caracteres));
            return false;
        }else {
            return true;
        }
    }

    //TRUE O FALSE itzultzen du lehenengo pasahitza eta bigarrena berdinak badira
    public boolean pasahitzaKonfirmatu(String pasahitza1, String pasahitza2, EditText text) {
        if(pasahitza2.length()==0) {
            text.setError(getString(R.string.error_campoNecesario));
            return false;
        }else if(!pasahitza1.equals(pasahitza2)){
            text.setError(getString(R.string.error_compPass));
            return false;
        }else {
            return true;
        }
    }
    //Pantaila osoa beltza moduan jartzen du
    public void setDayNight() {
        boolean oscuro = settingssp.getBoolean("oscuro", false);
        if (oscuro) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}