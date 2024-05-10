package com.e1t3.onplan.dao;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.print.PrintAttributes;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.e1t3.onplan.R;
import com.e1t3.onplan.model.Ekitaldia;
import com.e1t3.onplan.model.Gertaera;
import com.e1t3.onplan.shared.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DAOGertaerak {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DAOGertaerak() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Metodo honek gertaera bat gehitzen edo eguneratzen du
     * @param gertaera Gertaera
     * @return true gertaera gehitu edo eguneratu bada
     */

    public boolean gehituEdoEguneratuGertaera(Gertaera gertaera) {
            Map<String, Object> ekitaldiDoc = gertaera.getDocument();
            db.collection(Values.GERTAERAK)
                    .document(gertaera.getId())
                    .set(ekitaldiDoc);
        return true;
    }

    /**
     * Metodo honek gertaera bat ezabatzen du
     * @param id String
     * @return true gertaera ezabatu bada
     */

    public boolean ezabatuGertaeraIdz(String id) {
        db.collection(Values.GERTAERAK)
                .document(id)
                .delete();
        return true;
    }

    /**
     * Metodo honek gertaerak ezabatzen ditu
     * @param ids List<String>
     * @return true gertaerak ezabatu badira
     */

    public boolean gertaerakIdzEzabatu(List<String> ids) {
        for (String id : ids) {
            db.collection(Values.GERTAERAK)
                    .document(id)
                    .delete();
        }
        return true;
    }

}
