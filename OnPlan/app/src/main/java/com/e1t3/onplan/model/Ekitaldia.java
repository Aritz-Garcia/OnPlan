package com.e1t3.onplan.model;

import com.e1t3.onplan.dao.DAOEkitaldiak;
import com.e1t3.onplan.shared.EkitaldiMota;
import com.e1t3.onplan.shared.Values;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Klase honek ekitaldi baten datuak gordetzen ditu.
 */

public class Ekitaldia {

    private String id;
    private String izena;
    private String deskribapena;
    private Timestamp hasierakoDataOrdua;
    private Timestamp bukaerakoDataOrdua;
    private String gela;
    private double aurrekontua;
    private EkitaldiMota ekitaldiMota;
    private String usuario;
    private List<String> gerataerak;

    public Ekitaldia() {}

    /**
     * Metodo honek ekitaldi bat sortzen du.
     * @param document DocumentSnapshot
     */

    public Ekitaldia(DocumentSnapshot document) {
        this.id                 = document.getId();
        this.izena              = document.getString(Values.EKITALDIAK_IZENA);
        this.deskribapena       = document.getString(Values.EKITALDIAK_DESKRIBAPENA);
        this.hasierakoDataOrdua = document.getTimestamp(Values.EKITALDIAK_HASIERAKO_DATA_ORDUA);
        this.bukaerakoDataOrdua = document.getTimestamp(Values.EKITALDIAK_BUKAERAKO_DATA_ORDUA);
        this.gela               = document.getString(Values.EKITALDIAK_GELA);
        this.aurrekontua        = document.getDouble(Values.EKITALDIAK_AURREKONTUA);
        this.ekitaldiMota       = EkitaldiMota.valueOf(document.getString(Values.EKITALDIAK_EKITALDI_MOTA));
        this.usuario            = document.getString(Values.EKITALDIAK_ERABILTZAILEA);
        this.gerataerak         = (List<String>) document.get(Values.EKITALDIAK_GERTAERAK);
    }

    /**
     * Metodo honek ekitaldi bat sortzen du.
     * @param izena String
     * @param deskribapena String
     * @param hasierakoDataOrdua Timestamp
     * @param bukaerakoDataOrdua Timestamp
     * @param gela String
     * @param aurrekontua double
     * @param ekitaldiMota EkitaldiMota
     * @param usuario String
     * @param gerataerak List
     */

    public Ekitaldia(String id, String izena, String deskribapena, Timestamp hasierakoDataOrdua, Timestamp bukaerakoDataOrdua, String gela, double aurrekontua, EkitaldiMota ekitaldiMota, String usuario, List<Gertaera> gerataerak) {
        this.id = id;
        this.izena = izena;
        this.deskribapena = deskribapena;
        this.hasierakoDataOrdua = hasierakoDataOrdua;
        this.bukaerakoDataOrdua = bukaerakoDataOrdua;
        this.gela = gela;
        this.aurrekontua = aurrekontua;
        this.ekitaldiMota = ekitaldiMota;
        this.usuario = usuario;
        this.gerataerak = new ArrayList<>();
    }

    // Getterrak eta Setterrak

    /**
     * Metodo honek ekitaldiaren id-a itzultzen du.
     * @return String
     */

    public String getId() {
        return id;
    }

    /**
     * Metodo honek firebasek ulertzen duen dokumentu bat bueltatzen du.
     * @return Map<String, Object>
     */

    public Map<String, Object> getDocument() {
        return Map.of(
                Values.EKITALDIAK_IZENA, izena,
                Values.EKITALDIAK_DESKRIBAPENA, deskribapena,
                Values.EKITALDIAK_HASIERAKO_DATA_ORDUA, hasierakoDataOrdua,
                Values.EKITALDIAK_BUKAERAKO_DATA_ORDUA, bukaerakoDataOrdua,
                Values.EKITALDIAK_GELA, gela,
                Values.EKITALDIAK_AURREKONTUA, aurrekontua,
                Values.EKITALDIAK_EKITALDI_MOTA, ekitaldiMota,
                Values.EKITALDIAK_ERABILTZAILEA, usuario,
                Values.EKITALDIAK_GERTAERAK, gerataerak
        );
    }

    /**
     * Metodo honek hasierako data eta ordua itzultzen ditu.
     * @return String
     */

    public String getHasierakoDataOrdua() {
        return this.getDataString(this.hasierakoDataOrdua.getSeconds()*1000);
    }

    /**
     * Metodo honek bukaerako data eta ordua itzultzen ditu.
     * @return String
     */

    public String getBukaerakoDataOrdua() {
        return this.getDataString(this.bukaerakoDataOrdua.getSeconds()*1000);
    }

    /**
     * Metodo honek data eta ordua itzultzen du.
     * @param miliseconds long
     * @return String
     */

    private String getDataString(long miliseconds) {
        Date date = new Date(miliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+2"));
        String formattedDate = sdf.format(date);
        return formattedDate; // Tuesday,November 1,2011 12:00,AM
    }

    /**
     * Metodo honek ekitaldiaren izena itzultzen du.
     * @return String
     */

    public String getIzena() {
        return izena;
    }

    /**
     * Metodo honek ekitaldiaren deskribapena itzultzen du.
     * @return String
     */

    public String getDeskribapena() {
        return deskribapena;
    }

    /**
     * Metodo honek ekitaldiaren gela itzultzen du.
     * @return String
     */

    public String getGela() {
        return gela;
    }

    /**
     * Metodo honek ekitaldiaren aurrekontua itzultzen du.
     * @return double
     */

    public double getAurrekontua() {
        return aurrekontua;
    }

    /**
     * Metodo honek ekitaldiaren izena ezartzen du.
     * @param izena String
     */

    /**
     * Metodo honek ekitaldiaren gertaerak itzultzen ditu du.
     * @return  List<String>
     */

    public List<String> getGertaerak() {
        return this.gerataerak;
    }

    /**
     * Metodo honek data ekitaldiaren denbora tartearen barruan dagoen bueltatzen du.
     * @preturn true barruan badago bestela false
     */

    public boolean getDataTarteanDago(Date fecha){
        Date date= hasierakoDataOrdua.toDate();
        Date date2= bukaerakoDataOrdua.toDate();
        if(fecha.compareTo(date) >= 0 && fecha.compareTo(date2) <= 0){
            return true;
        }else{
            return false;
        }
    }

    public void setIzena(String izena){
        this.izena = izena;
    }

    /**
     * Metodo honek ekitaldiaren aurrekontua ezartzen du.
     * @param aurrekontua double
     */

    public void setAurrekontua(double aurrekontua){
        this.aurrekontua = aurrekontua;
    }

    /**
     * Metodo honek ekitaldiaren deskribapena ezartzen du.
     * @param deskribapena String
     */

    public void setDeskribapena(String deskribapena){
        this.deskribapena = deskribapena;
    }

    /**
     * Metodo honek gertaera baten id-s bidalita ekitaldiatik kentzen du du.
     * @param id String
     */

    public void ezabatuGertaera(String id) {
        this.gerataerak.remove(id);
    }

    /**
     * Metodo honek ekitaldiari gertaera gehitzen dio.
     * @param gertaera Gertaera
     */

    public void gehituGertaera(Gertaera gertaera) {
        this.gerataerak.add(gertaera.getId());
        DAOEkitaldiak dao = new DAOEkitaldiak();
        dao.gehituEdoEguneratuEkitaldia(this);
    }

    public String toString() {
        return  izena ;
    }

}
