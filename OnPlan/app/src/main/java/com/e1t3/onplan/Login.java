package com.e1t3.onplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmail, etPassword;
    private Button btnErregistratu, btnSartu;
    private FirebaseAuth mAuth;
    private SharedPreferences settingssp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        settingssp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        setDayNight();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnErregistratu = findViewById(R.id.btnErregistratu);
        btnSartu = findViewById(R.id.btnLogin);

        btnErregistratu.setOnClickListener(this);
        btnSartu.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnErregistratu.getId()) {
            //ventana erregistratu
            erregistratu();
        } else if (view.getId() == btnSartu.getId()) {
            //validation
            sesioaHasi();
        }
    }

    @Override
    protected void onStart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            Intent i = new Intent(Login.this, MainActivity.class);
            startActivity(i);
        }
        super.onStart();
    }

    private void sesioaHasi() {
        if (!etEmail.getText().toString().equals("") && !etPassword.getText().toString().equals("")) {
            mAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Intent i = new Intent(Login.this, MainActivity.class);
                    startActivity(i);
                    Login.this.finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.dialog_cuentaNoExiste)
                            .setTitle(R.string.dialog_error)
                            .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    etPassword.setText("");
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_emailVacio)
                    .setTitle(R.string.dialog_error)
                    .setPositiveButton(R.string.dialog_aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            etPassword.setText("");
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void erregistratu() {
        Intent i = new Intent(this, RegistroActivity.class);
        startActivity(i);
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