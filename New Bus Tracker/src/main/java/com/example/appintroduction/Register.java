package com.example.appintroduction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText PersonName, EmailAddress, TextPassword, TextCode;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        PersonName = findViewById(R.id.editName);
        EmailAddress = findViewById(R.id.editEmailAddress);
        TextPassword = findViewById(R.id.editPassword);
        TextCode = findViewById(R.id.editCode);
        buttonReg = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.prograssBar);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);

                String editName = String.valueOf(PersonName.getText());
                String editEmailAddress = String.valueOf(EmailAddress.getText());
                String editPassword = String.valueOf(TextPassword.getText());
                String editCode = String.valueOf(TextCode.getText());

                if (TextUtils.isEmpty(editName)) {
                    Toast.makeText(Register.this, "Enter the name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(editEmailAddress)) {
                    Toast.makeText(Register.this, "Enter the Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(editPassword)) {
                    Toast.makeText(Register.this, "Enter the password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(editCode)) {
                    Toast.makeText(Register.this, "Enter Your NBT Code or Zero", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Email pattern validation
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!editEmailAddress.matches(emailPattern)) {
                    Toast.makeText(Register.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Password pattern validation
                String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
                if (!editPassword.matches(passwordPattern)) {
                    Toast.makeText(Register.this, "Invalid password format\n\n" +
                            "Password must contain at least:\n" +
                            "- 8 characters\n" +
                            "- 1 uppercase letter\n" +
                            "- 1 lowercase letter\n" +
                            "- 1 digit\n" +
                            "- 1 special character (@#$%^&+=)", Toast.LENGTH_LONG).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(editEmailAddress, editPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account Created", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
