package com.example.appintroduction;

import android.content.Intent;
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

public class Loginpage extends AppCompatActivity {
    EditText EmailAddress,TextPassword,TextCode;
    Button buttonlog;
    FirebaseAuth mAuth;
    ProgressBar prograssBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        mAuth=FirebaseAuth.getInstance();
        EmailAddress=findViewById(R.id.editEmailAddress);
        TextPassword=findViewById(R.id.editPassword);
        TextCode=findViewById(R.id.editCode);
        buttonlog=findViewById(R.id.btnLogin);
        prograssBar=findViewById(R.id.prograssBar);

        buttonlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prograssBar.setVisibility(View.VISIBLE);
                String editEmailAddress,editPassword;
                editEmailAddress=String.valueOf(EmailAddress.getText());
                editPassword=String.valueOf(TextPassword.getText());
                // set alert for empty field in login page
                if(TextUtils.isEmpty(editEmailAddress)){
                    Toast.makeText(Loginpage.this, "Enter the Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(editPassword)){
                    Toast.makeText(Loginpage.this, "Enter the password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(editEmailAddress,editPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                prograssBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Loginpage.this, "Login Successful ", Toast.LENGTH_SHORT).show();
                                    String code= TextCode.getText().toString();

                                  // checking private code and based on that allow to suitable dashboard(NBT Code)
                                    if(code.equals("1")) {
                                        Intent intent = new Intent(getApplicationContext(), AdminDashboard.class);
                                        startActivity(intent);
                                        finish();
                                    }else if(code.equals("2")) {
                                        Intent intent = new Intent(getApplicationContext(), DriverDashboard.class);
                                        startActivity(intent);
                                        finish();
                                    } else if ((code.equals("0"))||(code.equals(""))){
                                        Intent intent = new Intent(getApplicationContext(), PassengerDashboard.class);
                                        startActivity(intent);
                                        finish();
                                    } else{
                                        Toast.makeText(Loginpage.this, "check your code, if you don't have code put zero ", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Loginpage.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        };
}
