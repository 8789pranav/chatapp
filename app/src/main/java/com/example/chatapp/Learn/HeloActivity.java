package com.example.chatapp.Learn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.chatapp.R;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HeloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helo);
    }
        public void hloaa(View view) {
            if (!checkPermission()) {
                openActivity();
            } else {
                if (checkPermission()) {
                    requestPermissionAndContinue();
                } else {
                    openActivity();
                }
            }


        }
        private static final int PERMISSION_REQUEST_CODE = 200;
        private boolean checkPermission() {

            return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ;
        }

        private void requestPermissionAndContinue() {
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(getString(R.string.permission_necessary));
                    alertBuilder.setMessage(R.string.storage_permission_is_encessary_to_wrote_event);
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HeloActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                    , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                    Log.e("", "permission denied, show dialog");
                } else {
                    ActivityCompat.requestPermissions(HeloActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
            } else {
                openActivity();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (permissions.length > 0 && grantResults.length > 0) {

                    boolean flag = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            flag = false;
                        }
                    }
                    if (flag) {
                        openActivity();
                    } else {
                        finish();
                    }

                } else {
                    finish();
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

        private void openActivity() {
            Intent intent =new Intent(HeloActivity.this, Imageee.class);
            startActivity(intent);
        }

}