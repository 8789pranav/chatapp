package com.example.chatapp.Activities;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.Adapters.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scottyab.aescrypt.AESCrypt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnSwitchRegister, btnLogin;
    EditText txtUserName, txtPassWord;
    FirebaseAuth firebaseAuth;
    TextView btnSwitchForgetPassWord;
    String email,password;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtUserName = (EditText) findViewById(R.id.editTextUsername);
        txtPassWord = (EditText) findViewById(R.id.editTextPassword);
        btnSwitchRegister = (Button)findViewById(R.id.buttonSwitchRegister);
        btnSwitchRegister.setOnClickListener(this);
        btnSwitchForgetPassWord = (TextView) findViewById(R.id.buttonForgetPassword);
        btnSwitchForgetPassWord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this,ReStorePassWordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        btnLogin = (Button)findViewById(R.id.buttonLogin);

        if(readFile().equals("")) {

        }
        else
        {
            String messageAfterDecrypt="";
            try {
                messageAfterDecrypt = AESCrypt.decrypt("123", readFile());
            }catch (GeneralSecurityException e){
                //handle error - could be due to incorrect password or tampered encryptedMsg
            }
            if(messageAfterDecrypt!="") {
                String[] fulluser = messageAfterDecrypt.split("[ ]");
                email = fulluser[0].trim();
                password = fulluser[1].trim();
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    Toast.makeText(LoginActivity.this, "\n" +
                                                    "Your account information has been changed! Please check again!!",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    String uid = firebaseAuth.getCurrentUser().getUid();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("UID", uid);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                        });
            }

        }

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                 email = txtUserName.getText().toString();
                 password = txtPassWord.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "\n" +
                            "Please enter your email before logging in!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "\n" +
                            "Please enter your password before logging in!", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "\n" +
                                                    "Incorrect account information or password. Please check again!!",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (!user.isEmailVerified()) {
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(LoginActivity.this,
                                                            "The account is currently not activated. The system has sent an activation email, please activate before using your account" + user.getEmail(),
                                                            Toast.LENGTH_LONG).show();
                                                    saveFile(email,password);
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    String uid = firebaseAuth.getCurrentUser().getUid();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("UID", uid);
                                                    intent.putExtras(bundle);
                                                    overridePendingTransition(R.anim.animation_in,R.anim.animation_out);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(LoginActivity.this,
                                                            "Authentication account faulty please wait a moment!",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        return;
                                    }
                                    saveFile(email,password);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    String uid = firebaseAuth.getCurrentUser().getUid();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("UID", uid);
                                    intent.putExtras(bundle);
                                    overridePendingTransition(R.anim.animation_in,R.anim.animation_out);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonSwitchRegister:
                Intent iRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(iRegister);
        }
    }
    public  void saveFile(String email, String passWord)
    {
        try {

            // M??? m???t lu???ng ghi file.
            FileOutputStream out = this.openFileOutput("session.txt", MODE_PRIVATE);
            // Ghi d??? li???u.
            String fulluser = email +" " + passWord;
            String encryptedMsg ="";
            try {
                encryptedMsg = AESCrypt.encrypt("123",fulluser);
                System.out.println("Ma hoa: "+encryptedMsg);
            }catch (GeneralSecurityException e){
                //handle error
            }
            out.write(encryptedMsg.getBytes());
            out.close();
        } catch (Exception e) {
            Toast.makeText(this,"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    private String readFile() {
        try {
            // M??? m???t lu???ng ?????c file.
            FileInputStream in = this.openFileInput("session.txt");
            if(in==null)
                return "";
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            StringBuilder sb = new StringBuilder();
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            return "";
        }

    }
}
