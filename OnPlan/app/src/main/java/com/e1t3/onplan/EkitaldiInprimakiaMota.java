package com.e1t3.onplan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.e1t3.onplan.shared.Values;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EkitaldiInprimakiaMota extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnVolverAtrasTE;
    private ListView lvEkitaldiMota;
    private List<String> llist = new ArrayList<>();
    private ArrayAdapter<Spanned> arrayAdapter;
    private SharedPreferences ekitaldi;
    private SharedPreferences.Editor editor;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SimpleDateFormat formato;
    private SharedPreferences settingssp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_tipo_de_evento);

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

        btnVolverAtrasTE = findViewById(R.id.btnVolverAtrasET);
        btnVolverAtrasTE.setOnClickListener(this);

        lvEkitaldiMota = findViewById(R.id.lvEkitaldiMota);
        lvEkitaldiMota.setOnItemClickListener(this);

        ekitaldi = getSharedPreferences("datuak", Context.MODE_PRIVATE);
        editor = ekitaldi.edit();
        formato = new SimpleDateFormat("dd/MM/yyyy hh:mm");

        ekitaldiMota();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnVolverAtrasTE.getId()) {
            this.finish();
        }
    }

    private void ekitaldiMota() {
        llist = List.of(getResources().getStringArray(R.array.ekitaldi_mota));
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, llist);
        lvEkitaldiMota.setAdapter(arrayAdapter);
    }

    private String ekitaldiMotaString(String ekitaldiMota) {
        switch (ekitaldiMota) {
            case "Ospakizuna":
                return "OSPAKIZUNA";
            case "Ikuskizuna":
                return "IKUSKIZUNA";
            case "Hitzaldia":
                return "HITZALDIA";
            case "Erakusketa":
                return "ERAKUSKETA";
            case "Beste mota":
                return "BESTE_MOTA";
            case "Celebración":
                return "OSPAKIZUNA";
            case "Espectaculo":
                return "IKUSKIZUNA";
            case "Conferencia":
                return "HITZALDIA";
            case "Esposición":
                return "ERAKUSKETA";
            case "Otro":
                return "BESTE_MOTA";
        }
        return "";
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int posizioa, long l) {
        String ekitaldiMota = llist.get(posizioa);
        String ekitaldiMotaString = ekitaldiMotaString(ekitaldiMota);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(ekitaldiMota + R.string.dialog_seguroEkitaldi)
                .setTitle(R.string.dialog_aviso)
                .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.putString(Values.EKITALDIAK_EKITALDI_MOTA, ekitaldiMotaString);
                        editor.commit();
                        gertaeraAukeartuEdoez();
                    }
                })
                .setNegativeButton(R.string.dialog_cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void gertaeraAukeartuEdoez() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_crearSucesos)
                .setTitle(R.string.dialog_aviso)
                .setPositiveButton(R.string.dialog_si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(EkitaldiInprimakiaMota.this, EkitaldiInprimakiaGertaerak.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ekitaldiDatuakGorde();
                        Intent intent = new Intent(EkitaldiInprimakiaMota.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
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
        ekitaldiak.document().set(ekitaldia);
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

    public void setDayNight() {
        boolean oscuro = settingssp.getBoolean("oscuro", false);
        if (oscuro) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}