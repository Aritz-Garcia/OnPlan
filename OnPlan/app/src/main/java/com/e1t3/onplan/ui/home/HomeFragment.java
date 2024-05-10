package com.e1t3.onplan.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.e1t3.onplan.EkitaldiInprimakia;
import com.e1t3.onplan.R;
import com.e1t3.onplan.EkitaldiakIkusi;
import com.e1t3.onplan.databinding.FragmentHomeBinding;
import com.e1t3.onplan.model.Ekitaldia;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment implements CalendarView.OnDateChangeListener {

    private FragmentHomeBinding binding;
    private CalendarView calendarview;
    public Ekitaldia ekitaldia;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        calendarview = root.findViewById(R.id.calendario);
        calendarview.setOnDateChangeListener(this);

        return root;
    }

    public void onSelectedDayChange (CalendarView calendarview, int i3, int i1, int i2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence []items = new CharSequence[3];
        items[0] = getString(R.string.dialog_tarea_agregar);
        items[1] = getString(R.string.dialog_tarea_ver);
        items[2] = getString(R.string.dialog_cancelar);

        Calendar calendar = Calendar.getInstance();
        Date date1,date2;
        int monthDay =calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String day = String.valueOf(monthDay);
        String mes = String.valueOf(month+1);
        String anio = String.valueOf(year);

        i1++;//se le suma uno porque empieza desde el mes 0
        String dayC = String.valueOf(i2);
        String mesC = String.valueOf(i1);
        String anioC = String.valueOf(i3);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
           date1= sdf.parse(day + "-" + mes + "-" + anio);
           date2 = sdf.parse(dayC + "-" + mesC + "-" + anioC);

        if(date2.before(date1)){
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
            builder2.setMessage(R.string.dialog_fechaPasado)
                    .setTitle(R.string.dialog_error)
                    .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            AlertDialog dialog = builder2.create();
            dialog.show();
        }else{
                int finalMes = i1;
                builder.setTitle(R.string.dialog_seleccinarTarea)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    //agregar evento
                                    Intent intent = new Intent(getActivity(), EkitaldiInprimakia.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("dia", i2);
                                    bundle.putInt("mes", finalMes);
                                    bundle.putInt("anio", i3);

                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                } else if (i == 1) {
                                    //ver evento
                                    Intent intent = new Intent(getActivity(), EkitaldiakIkusi.class);
                                    intent.putExtra("dia", i2);
                                    intent.putExtra("mes", finalMes);
                                    intent.putExtra("anio", i3);
                                    startActivity(intent);
                                } else {
                                    return;
                                }
                            }
                        });
            AlertDialog dialog = builder.create();
            dialog.show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}