package com.e1t3.onplan;


import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.e1t3.onplan.dao.DAOEkitaldiak;
import com.e1t3.onplan.dao.DAOErabiltzaileak;
import com.e1t3.onplan.dao.DAOGertaerak;
import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.model.Erabiltzailea;
import com.e1t3.onplan.shared.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ErabiltzaileAldaketak extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference dr = db.collection(Values.ERABILTZAILEAK).document();
    private DocumentReference id;

    private ImageView mSetImage;
    private Button mOptionButton,mgorde,mborrar,mgordefoto;
    private String mPath,email;
    public TextView izena,abizena,dni,emaila,telefonoa;
    private boolean empresa_da;
    private Uri path;
    private String nombre_imagen;
    private FirebaseUser user;
    private StorageReference storage;
    private SharedPreferences erabiltzaileDatuak;
    private SharedPreferences.Editor editor;
    private SharedPreferences settingssp;
    private static final int GALLERY_INTENT = 1;
    private ProgressDialog mpprogressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erabiltzaile_aldaketak);

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

         user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();
        datuak();
        mSetImage = (ImageView) findViewById(R.id.limagen);
        mOptionButton = (Button) findViewById(R.id.belegir);
        mgordefoto = (Button) findViewById(R.id.mgordefoto);
        mpprogressDialog = new ProgressDialog(this);
        mgordefoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ErabiltzaileAldaketak.this, MainActivity.class);
                startActivity(i);
            }
        });
        storage = FirebaseStorage.getInstance().getReference();

        erabiltzaileDatuak = getSharedPreferences(Values.ERABILTZAILEAK, Context.MODE_PRIVATE);
        editor = erabiltzaileDatuak.edit();
        mOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_INTENT);
            }
        });


        mborrar = (Button) findViewById(R.id.beleminar);
        mborrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEkitaldiakId();
                DAOErabiltzaileak daoErabiltzaileak = new DAOErabiltzaileak();
                daoErabiltzaileak.ezabatuErabiltzailea(erabiltzaileDatuak.getString("id", ""));
                borrarUsuario();
            }
        });

        mgorde = (Button) findViewById(R.id.bgorde);
        mgorde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean egokia[] = new boolean[3];
                egokia[0] = stringIrakurri(izena.getText().toString(),findViewById(R.id.textIzena));
                egokia[1] = zenbakiaIrakurri(telefonoa.getText().toString(),findViewById(R.id.textTelefonoa));
                if(empresa_da){
                   egokia[2]=true;
                }else{
                    egokia[2] = stringIrakurri(abizena.getText().toString(),findViewById(R.id.textIzena));
                }
                if (egokia[0] && egokia[1] && egokia[2]) {
                    datuakaldatu();
                }
            }

        });



    }

    private void datuakaldatu(){
        db.collection(Values.ERABILTZAILEAK)
                .whereEqualTo(Values.ERABILTZAILEAK_EMAIL, email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Erabiltzailea erabiltzailea = new Erabiltzailea(document);

                                DocumentReference actualizar = db.collection(Values.ERABILTZAILEAK).document(erabiltzailea.getId());
                               id = db.collection(Values.ERABILTZAILEAK).document(erabiltzailea.getId());

                                if(empresa_da){
                                    actualizar.update(Values.ERABILTZAILEAK_IZENA,izena.getText().toString(),Values.ERABILTZAILEAK_TELEFONOA,telefonoa.getText().toString());
                                }else{
                                    actualizar.update(Values.ERABILTZAILEAK_IZENA,izena.getText().toString(),Values.ERABILTZAILEAK_TELEFONOA,telefonoa.getText().toString(),Values.ERABILTZAILEAK_ABIZENA,abizena.getText().toString());
                                }
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void datuak(){
        izena = findViewById(R.id.textIzena);
        telefonoa =  findViewById(R.id.textTelefonoa);
        dni =  findViewById(R.id.textdni);
        dni.setEnabled(false);
        emaila =   findViewById(R.id.textEmaila);
        emaila.setEnabled(false);
        abizena =  findViewById(R.id.textAbizena2);
        db.collection(Values.ERABILTZAILEAK)
                .whereEqualTo(Values.ERABILTZAILEAK_EMAIL, email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Erabiltzailea erabiltzailea = new Erabiltzailea(document);
                                izena.setText(erabiltzailea.getIzena());
                                emaila.setText(erabiltzailea.getEmail());
                                dni.setText(erabiltzailea.getNanIfz());
                                telefonoa.setText(erabiltzailea.getTelefonoa());
                                if (erabiltzailea.getEnpresaDa()) {
                                    abizena.setVisibility(View.INVISIBLE);
                                } else {
                                    abizena.setVisibility(View.VISIBLE);
                                    abizena.setText(erabiltzailea.getAbizena());
                                }
                                empresa_da = erabiltzailea.getEnpresaDa();

                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == GALLERY_INTENT){
            mpprogressDialog.setTitle("Subiendo...");
            mpprogressDialog.setMessage("Subiendo foto a firebase");
            mpprogressDialog.setCancelable(false);
            mpprogressDialog.show();
              Uri uri = data.getData();
               String pan =  uri.getPath();
               String partes[] = pan.split("/");
               pan = partes[partes.length-1];
               pan = user.getUid() +"/"+ pan;
              StorageReference filePath = storage.child("avatar").child(pan);


              filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                  @Override
                  public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                      mpprogressDialog.dismiss();
                      Glide.with(ErabiltzaileAldaketak.this)
                                      .load(uri)
                                              . fitCenter().centerCrop().into(mSetImage);
                        Toast.makeText(ErabiltzaileAldaketak.this,getString(R.string.toast_ondoIgota), Toast.LENGTH_SHORT).show();
                      UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                              .setPhotoUri(uri)
                              .build();
                      FirebaseAuth.getInstance().getCurrentUser().updateProfile(userProfileChangeRequest);
                  }
              });

        }
    }

    private void getEkitaldiakId() {
        db.collection(Values.EKITALDIAK)
                .whereEqualTo(Values.EKITALDIAK_ERABILTZAILEA, erabiltzaileDatuak.getString("id", ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Ekitaldia ekitaldia = new Ekitaldia(document);

                                for (int i = 0; i<ekitaldia.getGertaerak().size(); i++) {
                                    DAOGertaerak daoGertaerak = new DAOGertaerak();
                                    daoGertaerak.ezabatuGertaeraIdz(ekitaldia.getGertaerak().get(i));
                                }
                                DAOEkitaldiak daoEkitaldiak = new DAOEkitaldiak();
                                daoEkitaldiak.ezabatuEkitaldiaId(ekitaldia.getId());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void borrarUsuario() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG,"OK! Works fine!");
                    startActivity(new Intent(ErabiltzaileAldaketak.this, Login.class));
                    finish();
                } else {
                    Log.w(TAG,"Something is wrong!");
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
