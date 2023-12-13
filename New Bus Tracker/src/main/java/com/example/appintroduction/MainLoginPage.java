package com.example.appintroduction;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainLoginPage extends AppCompatActivity implements View.OnClickListener{
   private Button btnAdmin,btnDriver,btnPassenger,btnSignUP;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login_page);

        Button btnAdmin = findViewById(R.id.btnAdmin);
        Button btnDriver = findViewById(R.id.btnDriver);
        Button btnPassenger = findViewById(R.id.btnPassenger);
        Button btnSignUP = findViewById(R.id.btnSignUP);

        btnAdmin.setOnClickListener(this);
        btnDriver.setOnClickListener(this);
        btnPassenger.setOnClickListener(this);
        btnSignUP.setOnClickListener(this);
        }
@Override
public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnAdmin:
            case R.id.btnPassenger:
            case R.id.btnDriver:
                Intent intentAdmin= new Intent(this,Loginpage.class);
            startActivity(intentAdmin);        break;
            case R.id.btnSignUP:
            Intent intentSignUP= new Intent(this,Register.class);
            startActivity(intentSignUP);
            break;
        }
        }
}