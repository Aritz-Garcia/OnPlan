package com.e1t3.onplan.dao;

import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.shared.Values;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Klase honek Ekitaldiak gordetzeko ezabatzeko eta eguneratzeko metodoak ditu.
 */

public class DAOEkitaldiak {


    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DAOEkitaldiak() { }

    /**
     * Metodo honek ekitaldi bat gordetzen edo eguneratzen du.
     * @param ekitaldia Ekitaldia
     */

    public boolean gehituEdoEguneratuEkitaldia(Ekitaldia ekitaldia){
        Map<String, Object> ekitaldiDoc = ekitaldia.getDocument();
        db.collection(Values.EKITALDIAK)
                .document(ekitaldia.getId())
                .set(ekitaldiDoc);
        return true;
    }

    /**
     * Metodo honek ekitaldi bat ezabatzen du.
     * @param id String
     */

    public boolean ezabatuEkitaldiaId(String id){
        db.collection(Values.EKITALDIAK)
                .document(id)
                .delete();
        return true;
    }

}
