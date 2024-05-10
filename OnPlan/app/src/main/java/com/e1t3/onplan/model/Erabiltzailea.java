package com.e1t3.onplan.model;

import com.e1t3.onplan.shared.Values;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

/**
 * Klase honek Erabiltzailearen datuak gordetzeko da.
 */

public class Erabiltzailea {

    private String id;
    private String izena;
    private String abizena;
    private String email;
    private String nanIfz;
    private String telefonoa;
    private boolean enpresaDa;
    private boolean admin;

    /**
     * Metodo honek erabiltzailearen datuak gordetzeko erabiltzen da.
     * @param document DocumentSnapshot
     */

    public Erabiltzailea(DocumentSnapshot document) {
        this.id             = document.getId();
        this.izena          = document.getString(Values.ERABILTZAILEAK_IZENA);
        this.abizena        = document.getString(Values.ERABILTZAILEAK_ABIZENA);
        this.email          = document.getString(Values.ERABILTZAILEAK_EMAIL);
        this.nanIfz         = document.getString(Values.ERABILTZAILEAK_NAN_IFZ);
        this.telefonoa      = document.getString(Values.ERABILTZAILEAK_TELEFONOA);
        this.enpresaDa      = document.getBoolean(Values.ERABILTZAILEAK_ENPRESA_DA);
        this.admin          = document.getBoolean(Values.ERABILTZAILEAK_ADMIN);
    }

    //Getterrak eta seterrak

    /**
     * Metodo honek erabiltzailearen id-a bueltatzen du.
     * @return String
     */

    public String getId() { return id; }

    /**
     * Metodo honek erabiltzailea enpresa den a la ez bueltatzend u.
     * @return true enpresa bada
     */

    public boolean getEnpresaDa() { return enpresaDa; }

    /**
     * Metodo honek erabiltzailearen izena bueltatzen du.
     * @return String
     */

    public String getIzena() { return izena; }

    /**
     * Metodo honek erabiltzailearen abizena bueltatzen du.
     * @return String
     */

    public String getAbizena() { return abizena; }

    /**
     * Metodo honek erabiltzailearen email-a bueltatzen du.
     * @return String
     */

    public String getEmail() { return email; }

    /**
     * Metodo honek erabiltzailearen nan-a edo ifz-a bueltatzen du.
     * @return String
     */

    public String getNanIfz() { return nanIfz; }

    /**
     * Metodo honek erabiltzailearen telefonoa bueltatzen du.
     * @return String
     */

    public String getTelefonoa() { return telefonoa; }

    /**
     * Metodo honek erabiltzailea admin den a la ez bueltatzend u.
     * @return true admin bada
     */

    public boolean adminDa() {
        return admin;
    }

    /**
     * Metodo honek erabiltzailearen datuak Map batean gordetzen ditu datubaserako.
     * @return Map<String, Object>
     */

    public Map<String, Object> getDocument() {
        return Map.of(
                Values.ERABILTZAILEAK_IZENA, izena,
                Values.ERABILTZAILEAK_ABIZENA, abizena,
                Values.ERABILTZAILEAK_EMAIL, email,
                Values.ERABILTZAILEAK_NAN_IFZ, nanIfz,
                Values.ERABILTZAILEAK_TELEFONOA, telefonoa,
                Values.ERABILTZAILEAK_ENPRESA_DA, enpresaDa,
                Values.ERABILTZAILEAK_ADMIN, admin
        );
    }


}
