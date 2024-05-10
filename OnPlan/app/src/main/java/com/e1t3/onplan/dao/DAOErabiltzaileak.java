package com.e1t3.onplan.dao;

import com.e1t3.onplan.model.Erabiltzailea;
import com.e1t3.onplan.shared.Values;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Klase honek Erabiltzaileak gordetzeko ezabatzeko eta eguneratzeko metodoak ditu.
 */

public class DAOErabiltzaileak {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DAOErabiltzaileak() { }

    /**
     * Metodo honek erabiltzailea gordetzen edo eguneratzen du.
     * @param erabiltzailea Erabiltzailea
     */

    public boolean gehituEdoEguneratuErabiltzailea(Erabiltzailea erabiltzailea) {
        Map<String, Object> erabiltzaileDoc = erabiltzailea.getDocument();
        db.collection(Values.ERABILTZAILEAK)
                .document(erabiltzailea.getId())
                .set(erabiltzaileDoc);
        return true;
    }

    /**
     * Metodo honek erabiltzailea ezabatzen du.
     * @param id String
     */

    public boolean ezabatuErabiltzailea(String id) {
        db.collection(Values.ERABILTZAILEAK)
                .document(id)
                .delete();
        return true;
    }


}
