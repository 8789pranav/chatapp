package com.example.chatapp.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.example.chatapp.Adapters.MainActivity;
import com.example.chatapp.Adapters.RecyclerListMessageAdapter;
import com.example.chatapp.Controllers.MessageController;
import com.example.chatapp.Fragments.AudioMessageFragment;
import com.example.chatapp.Interfaces.GetAudioFromRecordFragment;
import com.example.chatapp.Learn.Imageee;
import com.example.chatapp.Models.Account;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.RecentlyChat;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class ChatWithFriendActivity extends AppCompatActivity implements ValueEventListener,
        View.OnClickListener, GetAudioFromRecordFragment {

    private Intent iChat;
    private String uidFriendChat, nameFriendChat;

    private TextView textViewNameFriend;
    private ImageButton btnSendMessage, btnBackButton, btnSendImage, btnMoreInfo, btnSendAudio,btnSendImageWithCamera;
    private EditText editTextMessage;
    private RecyclerView recyclerViewMessage;

    private DatabaseReference nodeRefreshMessage, nodeMessage, nodeInfoMine, nodeInfoFriend, nodeGetMyName;
    private String myName = "";

    private MessageController messageController;

    private Uri filePath;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    TextView alert;
    private Uri ImageUri;
    ArrayList ImageList = new ArrayList();
    ArrayList<com.esafirm.imagepicker.model.Image> images=new ArrayList<>();
    Image image;

    private int upload_count = 0;
    private ProgressDialog progressDialog;
    ArrayList urlStrings;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_friends);

        iChat= getIntent();
        uidFriendChat = iChat.getStringExtra("UID_Friend"); // lấy uid người bạn chat cùng
        nameFriendChat = iChat.getStringExtra("Name_Friend"); // lấy tên hiển thị trên thanh toolbars

        recyclerViewMessage = (RecyclerView)findViewById(R.id.recyclerViewMessage);


        textViewNameFriend = (TextView)findViewById(R.id.textViewNameFriend);
        textViewNameFriend.setText(nameFriendChat + "");

        btnSendMessage = (ImageButton)findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(this);

        btnSendImageWithCamera = (ImageButton)findViewById(R.id.btnOpenCamera);
        btnSendImageWithCamera.setOnClickListener(this);

        btnSendImage = (ImageButton)findViewById(R.id.btnSendImage);
        btnSendImage.setOnClickListener(this);

        btnBackButton = (ImageButton)findViewById(R.id.btnBackMessages);
        btnBackButton.setOnClickListener(this);

        btnSendAudio = (ImageButton)findViewById(R.id.btnSendAudio);
        btnSendAudio.setOnClickListener(this);

        btnMoreInfo = (ImageButton)findViewById(R.id.btnMoreInfo);
        btnMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iMoreInfo = new Intent(ChatWithFriendActivity.this,MoreInfoActivity.class);
                iMoreInfo.putExtra("UID",uidFriendChat);
                iMoreInfo.putExtra("Name",nameFriendChat);
                startActivity(iMoreInfo);
                finish();
            }
        });


        editTextMessage = (EditText)findViewById(R.id.editTextMessage);
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                messageController.scrollMessageEditText();
                if(editTextMessage.getText().toString().isEmpty()){
                    btnSendMessage.setImageResource(R.drawable.icon_message_empty);
                }
                else {
                    btnSendMessage.setImageResource(R.drawable.icon_send_message);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                messageController.scrollMessageEditText();
                if(editTextMessage.getText().toString().isEmpty()){
                    btnSendMessage.setImageResource(R.drawable.icon_message_empty);
                }
                else {
                    btnSendMessage.setImageResource(R.drawable.icon_send_message);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                messageController.scrollMessageEditText();
                if(editTextMessage.getText().toString().isEmpty()){
                    btnSendMessage.setImageResource(R.drawable.icon_message_empty);
                }
                else {
                    btnSendMessage.setImageResource(R.drawable.icon_send_message);
                }
            }
        });

        nodeGetMyName = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid());
        nodeGetMyName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Account acc = dataSnapshot.getValue(Account.class);
                myName = acc.getFullName(); // lấy tên cho việc push tin nhắn mới nhất phía dưới
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        nodeRefreshMessage = FirebaseDatabase.getInstance().getReference();
        nodeRefreshMessage.addValueEventListener(this); // lắng nghe sự kiện khi có tin nhắn mới hoặc gửi đi
    }


    @Override
    protected void onStart() {
        super.onStart();
        messageController = new MessageController(ChatWithFriendActivity.this,uidFriendChat,recyclerViewMessage);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        // lấy trang thái online
        // lấy danh sách tin nhắn
        DataSnapshot nodeMessage = dataSnapshot.child("messages");
        messageController.refreshMessage(nodeMessage,myName,nameFriendChat);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSendAudio:
                if(RecyclerListMessageAdapter.getCurrentMedia!=null && RecyclerListMessageAdapter.btnCurrentPlay!=null) {

                    MediaPlayer mediaPlayer = RecyclerListMessageAdapter.getCurrentMedia;
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        RecyclerListMessageAdapter.btnCurrentPlay.setImageResource(R.drawable.icon_pause_audio_message);
                    }
                }
                FragmentManager fm = getFragmentManager();
                AudioMessageFragment audioMessageFragment = AudioMessageFragment.newInstance(uidFriendChat);
                audioMessageFragment.show(fm, null); // show dialog
                break;
            case R.id.btnBackMessages:
                Intent iFriendFragment = new Intent(ChatWithFriendActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                if(iChat.getStringExtra("From").equals("Friend_Fragment")){
                    bundle.putInt("ReturnTab", 1);
                }
                else if (iChat.getStringExtra("From").equals("Message_Fragment")
                        || iChat.getStringExtra("From").equals("MoreInfoMessage")){
                    bundle.putInt("ReturnTab", 0);
                }
                bundle.putString("UID",FirebaseAuth.getInstance().getUid());
                iFriendFragment.putExtras(bundle);
                startActivity(iFriendFragment);
                finish();
                break;
            case R.id.btnSendImage:
               // Intent iSendPicture = new Intent();
              //  iSendPicture.setType("image/*");
              //  iSendPicture.setAction(Intent.ACTION_GET_CONTENT);
              //  startActivityForResult(Intent.createChooser(iSendPicture, "Chọn ảnh để gửi"), 1);
                ImagePicker.create(ChatWithFriendActivity.this)

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
                break;
            case R.id.btnSendMessage:
                String contentMessage = editTextMessage.getText().toString();
                if(!contentMessage.isEmpty()){
                    pushMessage("text",contentMessage);
                }
                break;
            case R.id.btnOpenCamera:
                Intent intentopencamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentopencamera,2);
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        nodeRefreshMessage.addValueEventListener(this);
    }

    public void pushMessage(String type, String contentMessage){
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        simpleDateFormat.setTimeZone(timeZone);
        String timeMsg = simpleDateFormat.format(Calendar.getInstance().getTime());

        nodeMessage = FirebaseDatabase.getInstance().getReference().child("messages");

        Message msg = null;

        switch (type){
            case "text":
                msg = new Message(FirebaseAuth.getInstance().getUid(), uidFriendChat,
                        contentMessage, false, false, timeMsg,false);
                break;
            case "image":
                msg = new Message(FirebaseAuth.getInstance().getUid(), uidFriendChat,
                        contentMessage, true, false, timeMsg,false);
                break;
            case "audio":
                msg = new Message(FirebaseAuth.getInstance().getUid(), uidFriendChat,
                        contentMessage, false, true, timeMsg,false); // co duoi .mp3 san r
                break;
        }
        nodeMessage.push().setValue(msg);

        //push thông tin cần thiết cho việc lấy danh sách gần đây
        nodeInfoMine = FirebaseDatabase.getInstance().getReference().child("more_info")
                .child(FirebaseAuth.getInstance().getUid()).child("last_messages");

        // push tin nhắn cuối để show ra khi lấy danh sách nhăn tin gần đây
        nodeInfoMine.child(uidFriendChat).setValue(new RecentlyChat(FirebaseAuth.getInstance().getUid(),uidFriendChat,nameFriendChat,
                contentMessage,type, timeMsg,false)); // gửi tin nhắn cuối mặc định là chưa xem

        nodeInfoFriend = FirebaseDatabase.getInstance().getReference().child("more_info")
                .child(uidFriendChat).child("last_messages");

        nodeInfoFriend.child(FirebaseAuth.getInstance().getUid()).setValue(new RecentlyChat(FirebaseAuth.getInstance().getUid(),
                FirebaseAuth.getInstance().getUid(), myName, contentMessage,type, timeMsg,false));

        editTextMessage.setText(""); // xóa tin nhắn trước đó
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

       // if (requestCode == 1 && resultCode == Activity.RESULT_OK
            //    && data != null && data.getData() != null) {
          //  nodeRefreshMessage.addValueEventListener(this);
            if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
                nodeRefreshMessage.addValueEventListener(this);
                // Get a list of picked images
                List<Image> images = ImagePicker.getImages(data);

                // or get a single image only
                Image image = ImagePicker.getFirstImageOrNull(data);
                // Uri urii=Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(image.getId()));
                //  uploadFile(urii);
                printImages(images);
                urlStrings = new ArrayList<>();


                StorageReference ImageFolder = FirebaseStorage.getInstance().getReference().child("ImageFolder");


                for (upload_count = 0; upload_count < ImageList.size(); upload_count++) {

                    Uri IndividualImage = (Uri) ImageList.get(upload_count);
                  //  final StorageReference ImageName = ImageFolder.child("Images" + IndividualImage.getLastPathSegment());
                  final   StorageReference ref = storageReference.child(FirebaseAuth.getInstance().getUid())
                     .child(uidFriendChat).child("Images" + IndividualImage.getLastPathSegment());

                    ref.putFile(IndividualImage).addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ref.getDownloadUrl().addOnSuccessListener(
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Toast.makeText(ChatWithFriendActivity.this, "Storage Completed" +
                                                            "", Toast.LENGTH_SHORT).show();
                                                    urlStrings.add(String.valueOf(uri));


                                                    if (urlStrings.size() == ImageList.size()) {
                                                        storeLink(urlStrings);
                                                    }

                                                }
                                            }
                                    );
                                }
                            }
                    );


                }

            }
           // filePath = data.getData();
          //  try {
               // final String nameImage = UUID.randomUUID().toString(); // tạo tên bất kì cho ảnh

              //  StorageReference ref = storageReference.child(FirebaseAuth.getInstance().getUid())
                      //  .child(uidFriendChat).child(nameImage+".jpg");

              //  ref.putFile(filePath)
                   //     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                          //  @Override
                           // public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                             //   Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                              //  while (!uriTask.isComplete()) ;
                             //   Uri urlImage = uriTask.getResult();
                            //  String  imageUrll = urlImage.toString();
                             //   pushMessage("image",imageUrll);

                           // }
                     //   })
                       // .addOnFailureListener(new OnFailureListener() {
                         //   @Override
                           // public void onFailure(@NonNull Exception e) {
                            //    Toast.makeText(ChatWithFriendActivity.this,
                                //        "Lỗi upload ảnh. Kiểm tra lại ảnh hoặc kết nội Internet của bạn",Toast.LENGTH_SHORT).show();
                          //  }
                     //   })
                     //   .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                          //  @Override
                          //  public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                          //  }
                      //  });
         //   } catch (NullPointerException e) {
            //    e.printStackTrace();
           // }
       // }
        else{
            nodeRefreshMessage.addValueEventListener(this);
        }

        if (requestCode == 2 && resultCode == Activity.RESULT_OK
               && data != null) {
           nodeRefreshMessage.addValueEventListener(this);
           Bitmap bitmap = (Bitmap) data.getExtras().get("data");
          try {
              final String nameImage = UUID.randomUUID().toString(); // tạo tên bất kì cho ảnh

             StorageReference ref = storageReference.child(FirebaseAuth.getInstance().getUid())
                      .child(uidFriendChat).child(nameImage+".jpg");
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
               bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
              byte[] data2 = baos.toByteArray();
               UploadTask uploadTask = ref.putBytes(data2);
               uploadTask.addOnFailureListener(new OnFailureListener() {
                  @Override
                   public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ChatWithFriendActivity.this,"Did not receive the picture please check again !!",Toast.LENGTH_LONG).show();
                  }
              }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                  public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                     while (!uriTask.isComplete()) ;
                      Uri urlImage = uriTask.getResult();
                     String  imageUrll = urlImage.toString();
                       pushMessage("image",imageUrll);
                    //  pushMessage("image",nameImage+".jpg");
                  }
               });
            } catch (NullPointerException e) {
            e.printStackTrace();
          }
      }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // tat nhac
        if(RecyclerListMessageAdapter.getCurrentMedia!=null) {
            MediaPlayer mediaPlayer = RecyclerListMessageAdapter.getCurrentMedia;
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }

    }
    private void storeLink(ArrayList<String> urlStrings) {
      //  imagehelper imagehelperr = new imagehelper();


        HashMap<String, String> hashMap = new HashMap<>();

        for (int i = 0; i < urlStrings.size(); i++) {
           // imagehelperr =new imagehelper(urlStrings.get(0),urlStrings.get(1),urlStrings.get(2));
            //  hashMap.put("ImgLink" + i, urlStrings.get(i));
            pushMessage("image",urlStrings.get(i));

        }
        //   Upload upload = new Upload(, imageUrll);
       // DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User");

      //  databaseReference.push().setValue(imagehelperr)
             //   .addOnCompleteListener(
                      //  new OnCompleteListener<Void>() {
                          //  @Override
                           // public void onComplete(@NonNull Task<Void> task) {
                             //   if (task.isSuccessful()) {
                                 //   Toast.makeText(Select_Image.this, "Successfully Uplosded", Toast.LENGTH_SHORT).show();
                              //  }
                          //  }
                     //   }
             //   ).addOnFailureListener(new OnFailureListener() {
           // @Override
          //  public void onFailure(@NonNull Exception e) {
               // Toast.makeText(Select_Image.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            //}
        //});
        //progressDialog.dismiss();
        //alert.setText("Uploaded Successfully");
        //uploaderBtn.setVisibility(View.GONE);

        ImageList.clear();
    }
    private void printImages(List<Image> images) {
        if (images == null) return;


        for (int i = 0, l = images.size(); i < l; i++) {

            ImageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(images.get(i).getId()));
            ImageList.add(ImageUri);
        }


    }
    private void uploadFile(Uri data) {
        // progressBar.setVisibility(View.VISIBLE);

       // StorageReference sRef = FirebaseStorage.getInstance().getReference().child(Constant.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + ".jpg");
        final   StorageReference ref = storageReference.child(FirebaseAuth.getInstance().getUid())
                .child(uidFriendChat).child("Images" + data.getLastPathSegment());

        ref.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;
                        Uri urlImage = uriTask.getResult();
                        String  imageUrll = urlImage.toString();
                        String name="read";
                        pushMessage("image",imageUrll);
                      //  Upload upload = new Upload(name, imageUrll);
                       // mDatabaseReference.child(mDatabaseReference.push().getKey()).setValue(upload);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                       //double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                       // alert.setText((int) progress + "% Uploading...");
                      //  Toast.makeText(ChatWithFriendActivity.this, (int) progress + "% Uploading...", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public void onBackPressed() {

    }

    @Override
    public void getAudioName(String audioName) {
        pushMessage("audio",audioName);
    }

}
