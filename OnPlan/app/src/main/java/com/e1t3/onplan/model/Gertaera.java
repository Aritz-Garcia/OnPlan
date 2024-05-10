package com.e1t3.onplan.model;

import com.e1t3.onplan.shared.Values;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

public class Gertaera {

    private String id;
    private String izena;
    private String deskribapena;
    private boolean eginDa;
    private Timestamp ordua;

    public Gertaera(String id, String izena, String deskribapena, boolean eginDa, Timestamp ordua) {
        this.id = id;
        this.izena = izena;
        this.deskribapena = deskribapena;
        this.eginDa = eginDa;
        this.ordua = ordua;
    }

    public Gertaera(DocumentSnapshot document) {
        this.id             = document.getId();
        this.izena          = document.getString(Values.GERTAERAK_IZENA);
        this.deskribapena   = document.getString(Values.GERTAERAK_DESKRIBAPENA);
        this.eginDa         = Boolean.TRUE.equals(document.getBoolean(Values.GERTAERAK_EGIN_DA));
        this.ordua          = document.getTimestamp(Values.GERTAERAK_ORDUA);
    }

    /**
     * Metodo honek gertaera baten datuak datubaseak ulertzen duen datu mota batean gordetzen ditu.
     * @return String
     */

    public Map<String, Object> getDocument() {
        return Map.of(
                Values.GERTAERAK_IZENA, izena,
                Values.GERTAERAK_DESKRIBAPENA, deskribapena,
                Values.GERTAERAK_EGIN_DA, eginDa,
                Values.GERTAERAK_ORDUA, ordua
        );
    }

    /**
     * Metodo honek gertaera baten id-a itzultzen du.
     * @return String
     */

    public String getId() {
        return id;
    }

    /**
     * Metodo honek gertaera baten izena itzultzen du.
     * @return String
     */

    public String getIzena() {
        return izena;
    }

    /**
     * Metodo honek gertaera hastenden ordua itzultzen du.
     * @return String
     */

    public String getOrdua() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        return sdf.format(this.ordua.toDate());
    }

    /**
     * Metodo honek gertaera egin den itzultzen du.
     * @return String
     */

    public boolean eginDa() {
        return this.eginDa;
    }

    /**
     * Metodo honek gertaera baten deskribapena itzultzen du.
     * @return String
     */

    public String getDeskribapena() {
        return deskribapena;
    }

    /**
     * Metodo honek gertaera egin dela ezartzen du.
     */

    public void setEginda() {
        this.eginDa = true;
    }
}
