package com.example.chatapp.Learn;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.example.chatapp.R;


import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Imageee extends AppCompatActivity {
    Button chose,Show;
    LinearLayout linearLayout;
    RecyclerView recyclerView;
    Uri ImageUri;
    ArrayList ImageList = new ArrayList();
    ArrayList<image_help> image_helps;
    private int upload_count = 0;
    myimageadapter myimageadapterr;
    private ProgressDialog progressDialog;
    ArrayList urlStrings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageee);

        chose=findViewById(R.id.button_chose);
        Show=findViewById(R.id.button_show);
        image_helps=new ArrayList<>();
        Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_helps.clear();
                myimageadapterr=new myimageadapter(Imageee.this,image_helps);
                recyclerView.setAdapter(myimageadapterr);
                myimageadapterr.notifyDataSetChanged();


            }
        });

        recyclerView=findViewById(R.id.recyclerView_image);
        recyclerView.setLayoutManager(new LinearLayoutManager(Imageee.this));


        chose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.create(Imageee.this)

                        .returnMode(ReturnMode.NONE) // set whether pick and / or camera action should return immediate result or not.
                        .folderMode(true) // folder mode (false by default)
                        .toolbarFolderTitle("Folder") // folder selection title
                        .toolbarImageTitle("Tap to select") // image selection title
                        .toolbarArrowColor(Color.BLACK) // Toolbar 'up' arrow color
                        .includeVideo(true) // Show video on image picker
                        //  .onlyVideo(onlyVideo) // include video (false by default)
                        // .single() // single mode
                        .multi() // multi mode (default mode)
                        .limit(10) // max images can be selected (99 by default)
                        .showCamera(true) // show camera or not (true by default)
                        .imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
                        //  .origin(images) // original selected images, used in multi mode
                        //  .exclude(images) // exclude anything that in image.getPath()
                        //  .excludeFiles(files) // same as exclude but using ArrayList<File>
                        // .theme(R.style.CustomImagePickerTheme) // must inherit ef_BaseTheme. please refer to sample
                        .enableLog(false) // disabling log
                        .start(); // start image picker activity with request code
                image_helps.clear();
                myimageadapterr=new myimageadapter(Imageee.this,getdata());
                recyclerView.setAdapter(myimageadapterr);
                myimageadapterr.notifyDataSetChanged();






            }
        });









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
                        ActivityCompat.requestPermissions(Imageee.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(Imageee.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            List<Image> images = ImagePicker.getImages(data);

            // or get a single image only
            Image image = ImagePicker.getFirstImageOrNull(data);
            // Uri urii=Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(image.getId()));
            //  uploadFile(urii);
            printImages(images);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void printImages(List<Image> images) {
        ImageList.clear();



        if (images == null) return ;

        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0, l = images.size(); i < l; i++) {
            stringBuffer.append(images.get(i).getPath()).append("\n");
            ImageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(images.get(i).getId()));
            ImageList.add(ImageUri);

        }


    }
    private ArrayList<image_help> getdata(){

        image_helps=new ArrayList<>();
        image_helps.clear();

        urlStrings = new ArrayList<>();



        for (upload_count = 0; upload_count < ImageList.size(); upload_count++) {

            //Uri IndividualImage = (Uri) ImageList.get(upload_count);
            // File downloadflder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            image_help s;

            // File[] files=downloadflder.listFiles();
            //  for(int i=0;i<files.length;i++){
            //  File file= files[i];
            s=new image_help();
            s.setName("heloo");
            s.setUri((Uri) ImageList.get(upload_count));
            image_helps.add(s);
            // recyclerView.setAdapter(myimageadapterr);
            /// myimageadapterr.notifyDataSetChanged();




        }


        return image_helps;
    }
}