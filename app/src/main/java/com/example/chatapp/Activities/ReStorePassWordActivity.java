package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ReStorePassWordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtEmail;
    private Button btnBack,btnReStore;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        txtEmail = findViewById(R.id.editTextEmailNeedRestore);
        btnBack = findViewById(R.id.buttonBack);
        btnReStore = findViewById(R.id.buttonRestorePassWord);
        btnBack.setOnClickListener(this);
        btnReStore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final Intent intent = new Intent(ReStorePassWordActivity.this,LoginActivity.class);
        if(v.getId() == R.id.buttonBack)
        {
            startActivity(intent);

        }
        if(v.getId()==R.id.buttonRestorePassWord)
        {
            final String email = txtEmail.getText().toString().trim();

            firebaseAuth = FirebaseAuth.getInstance();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "\n" +
                        "Please enter your registered email!", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(), "We have sent the verification to your email, please check your email!", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "\n" +
                                "Invalid email. Please check your Email!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}
