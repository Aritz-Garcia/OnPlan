package com.e1t3.onplan;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.e1t3.onplan.databinding.ActivityMainBinding;
import com.e1t3.onplan.model.Erabiltzailea;
import com.e1t3.onplan.shared.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView tvNombreUsuario, tvEmailUsuario;
    private ImageView imagenUser;
    private Button btnCerrarSesion,btnEditarUsuario;
    private SharedPreferences erabiltzaileDatuak;
    private SharedPreferences.Editor editor;
    private SharedPreferences settingssp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_agenda, R.id.nav_salas, R.id.nav_kontaktua)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

        tvEmailUsuario = headerView.findViewById(R.id.tvUserEmail);
        tvNombreUsuario = headerView.findViewById(R.id.tvUserName);
        imagenUser = headerView.findViewById(R.id.ivUserImage);
        btnCerrarSesion = headerView.findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(this);
        btnEditarUsuario = headerView.findViewById(R.id.btnEditarUsuario);
        btnEditarUsuario.setOnClickListener(this);

        erabiltzaileDatuak = getSharedPreferences(Values.ERABILTZAILEAK, Context.MODE_PRIVATE);
        editor = erabiltzaileDatuak.edit();
        editor.commit();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();
        Uri photoUrl =user.getPhotoUrl();
        this.setup(email, headerView);
        tvEmailUsuario.setText(email);
        if (photoUrl != null) {
            Picasso.get()
                    .load(photoUrl)
                    .into(imagenUser);
        }
        this.sicambios(email,headerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, EzarpenakActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnCerrarSesion.getId()) {
            signOut();
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        } else if (view.getId() == btnEditarUsuario.getId()) {
            Intent i = new Intent(this, ErabiltzaileAldaketak.class);
            startActivity(i);
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private void setup(String email, View headerView) {
        db.collection(Values.ERABILTZAILEAK)
                .whereEqualTo(Values.ERABILTZAILEAK_EMAIL, email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Erabiltzailea erabiltzailea = new Erabiltzailea(document);
                                TextView tvNombreUsuario = headerView.findViewById(R.id.tvUserName);
                                String nombre;
                                if (erabiltzailea.getEnpresaDa()) {
                                    nombre = erabiltzailea.getIzena();
                                } else {
                                    nombre = erabiltzailea.getIzena() + " "  + erabiltzailea.getAbizena();
                                }
                                tvNombreUsuario.setText(nombre);
                                editor.putString(Values.ERABILTZAILEAK_EMAIL, erabiltzailea.getEmail());
                                editor.putString("id", erabiltzailea.getId());
                                editor.commit();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void sicambios(String email, View headerView){
        db.collection(Values.ERABILTZAILEAK)
                .whereEqualTo(Values.ERABILTZAILEAK_EMAIL, email)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(TAG, "New city: " + dc.getDocument().getData());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                    break;
                            }
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