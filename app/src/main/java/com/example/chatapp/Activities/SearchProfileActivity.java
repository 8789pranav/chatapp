package com.example.chatapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.chatapp.Models.Account;
import com.example.chatapp.Models.FriendRequest;
import com.example.chatapp.Models.User;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtPhoneNumber, txtDateOfBirth, txtAddress,txtGender, txtFullName, txtDescription;
    private Button btnAddFriend, btnRejectFriend;

    private Toolbar toolBarViewProfile;
    private ImageView btnBackSearchFriend;
    private RelativeLayout relativeLayout;
    private CircleImageView civAvatar;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference nodeRoot, nodeFriends, nodeFriendRequests;

    Bundle bundle;
    private String userEmail, userPhoneNumber, uidSearchFriend;

    private Account account = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profile);

        toolBarViewProfile = (Toolbar)findViewById(R.id.toolBarViewProfile);
        setSupportActionBar(toolBarViewProfile);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // tắt tiêu đề mặc định của toolbar
        relativeLayout = findViewById(R.id.profile_layout_search);

        btnBackSearchFriend = (ImageView)findViewById(R.id.iconBackSearchFriend);
        btnBackSearchFriend.setOnClickListener(this);
        civAvatar = findViewById(R.id.profile_search);
        btnAddFriend = (Button)findViewById(R.id.btnAddFriend);
        btnAddFriend.setOnClickListener(this);
        btnRejectFriend = (Button)findViewById(R.id.btnRejectFriend);
        btnRejectFriend.setOnClickListener(this);

        txtFullName = (TextView)findViewById(R.id.textViewFullNameSearch);
        txtGender = (TextView)findViewById(R.id.textViewGenderSearch);
        txtPhoneNumber = (TextView)findViewById(R.id.textViewPhoneNumberSearch);
        txtDateOfBirth = (TextView)findViewById(R.id.textViewDateofBirthSearch);
        txtAddress = (TextView)findViewById(R.id.textViewAddressSearch);
        txtDescription = (TextView)findViewById(R.id.textViewDescriptionSearch);

        bundle = getIntent().getExtras();
        nodeRoot = FirebaseDatabase.getInstance().getReference();
        nodeFriendRequests = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        nodeFriends = FirebaseDatabase.getInstance().getReference().child("friends");

        if(bundle != null){
            userEmail = bundle.getString("Email");
            userPhoneNumber = bundle.getString("PhoneNumber");
            uidSearchFriend = bundle.getString("UID_Friend");

            nodeRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ///Load thông tin người dùng
                    DataSnapshot nodeUsers = dataSnapshot.child("users");

                    for(DataSnapshot nodeUIDs : nodeUsers.getChildren()){
                        Account acc = nodeUIDs.getValue(Account.class);
                        Log.d("Check account:",acc.getUsername());
                        // kiem tra lai o duoi
                        if(acc.getUsername().equals(userEmail) || acc.getPhoneNumber().equals(userPhoneNumber)){
                            account = acc;
                        }
                    }

                    if (account != null) {
                        ////////Check user có uid tìm kiếm theo ///////
                        DataSnapshot nodeUserSearch = dataSnapshot.child("users");
                        for (DataSnapshot nodeUIDs : nodeUserSearch.getChildren()) {
                            User user = nodeUIDs.getValue(User.class);
                            if (user.getUsername().equals(account.getUsername())
                                    || user.getPhoneNumber().equals(account.getPhoneNumber())) {
                                uidSearchFriend = nodeUIDs.getKey();
                            }
                        }

                        txtFullName.setText(account.getFullName());
                        txtPhoneNumber.setText(account.getPhoneNumber() + "");
                        txtDateOfBirth.setText(account.getDateOfBirth());
                        txtAddress.setText(account.getAddress());
                        String gender = account.isGender() ? "Male" : "Female";
                        txtGender.setText(gender);
                        txtDescription.setText(account.getDescription());

                        getAvatar();
                        getBackground();

                        ///////Check user có trong danh sách friend_requests///////
                        DataSnapshot nodeRequests = dataSnapshot.child("friend_requests");
                        FriendRequest checkLogin = null, checkFriend = null;
                        for(DataSnapshot nodeSingleRequests : nodeRequests.getChildren()) {
                            FriendRequest temp = nodeSingleRequests.getValue(FriendRequest.class);
                            if (temp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && temp.getUidUserFriend().equals(uidSearchFriend)
                                    && temp.getStatus().equals("sent")) {
                                // gửi yêu cầu kết bạn
                                checkLogin = temp;
                            }
                            if (temp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && temp.getUidUserFriend().equals(uidSearchFriend)
                                    && temp.getStatus().equals("received")) {
                                // nhận yêu cầu kết bạn
                                checkFriend = temp;
                            }
                        }
                        /////////////////////////////////////////////////////////////

                        ////////Kiểm tra bạn có nằm trong danh sách bạn bè chưa/////
                        boolean isFriend = false;
                        DataSnapshot nodeMatchingFriends = dataSnapshot.child("friends");
                        for (DataSnapshot nodeSingleFriends : nodeMatchingFriends.getChildren()) {
                            if (nodeSingleFriends.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                                if (nodeSingleFriends.hasChild(uidSearchFriend)) {
                                    isFriend = true;
                                }
                            }
                        }
                        ////////////////////////////////////////////////////////////


                        //////Kiểm tra các trường hợp đã gửi kết bạn hay chưa////////
                        if(checkLogin == null && checkFriend == null){
                            if(isFriend){
                                btnAddFriend.setText("Unfriended");
                                btnRejectFriend.setVisibility(View.INVISIBLE);
                            }
                            else{
                                btnAddFriend.setText("Make friend");
                                btnRejectFriend.setVisibility(View.INVISIBLE);
                            }

                        }
                        else if (checkLogin != null && checkFriend == null){
                            btnAddFriend.setText("Canceling friend request");
                            btnRejectFriend.setVisibility(View.INVISIBLE);
                        }
                        else if(checkLogin == null && checkFriend != null){
                            btnAddFriend.setText("Agree to make friends");
                            btnRejectFriend.setVisibility(View.VISIBLE);
                        }
                        ////////////////////////////////////////////////////////////////

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAddFriend:
                final String txtButtonAddFriend = btnAddFriend.getText().toString().trim();

                nodeRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(txtButtonAddFriend.equals("Make friend")){
                            //uidSearchFriend
                            FriendRequest sentRequest = new FriendRequest(FirebaseAuth.getInstance().getUid(),
                                                                uidSearchFriend,"sent");
                            FriendRequest receivedRequest = new FriendRequest(uidSearchFriend,
                                                                FirebaseAuth.getInstance().getUid(),"received");
                            nodeFriendRequests.push().setValue(sentRequest);
                            nodeFriendRequests.push().setValue(receivedRequest);

                            btnAddFriend.setText("Canceling friend request");
                            btnRejectFriend.setVisibility(View.INVISIBLE);
                        }
                        else if(txtButtonAddFriend.equals(
                                "Unfriended")){
                            DataSnapshot nodeAllFriends = dataSnapshot.child("friends");
                            for(DataSnapshot nodeUIDs : nodeAllFriends.getChildren()){
                                if(nodeUIDs.getKey().equals(FirebaseAuth.getInstance().getUid()) &&
                                        nodeUIDs.child(uidSearchFriend).getKey().equals(uidSearchFriend)){
                                    nodeFriends.child(FirebaseAuth.getInstance().getUid()).child(uidSearchFriend).removeValue();
                                    nodeFriends.child(uidSearchFriend).child(FirebaseAuth.getInstance().getUid()).removeValue();
                                }
                            }

                            btnAddFriend.setText("Make friend");
                            btnRejectFriend.setVisibility(View.INVISIBLE);
                        }
                        else if(txtButtonAddFriend.equals(
                                "Canceling friend request")){
                            DataSnapshot nodeAllRequests = dataSnapshot.child("friend_requests");
                            for(DataSnapshot nodeRequest : nodeAllRequests.getChildren()){
                                FriendRequest req = nodeRequest.getValue(FriendRequest.class);
                                if(req.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                        && req.getUidUserFriend().equals(uidSearchFriend)
                                        && req.getStatus().equals("sent")){
                                    nodeFriendRequests.child(nodeRequest.getKey()).removeValue();
                                }
                                if(req.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                        && req.getUidUserLogin().equals(uidSearchFriend)
                                        && req.getStatus().equals("received")){
                                    nodeFriendRequests.child(nodeRequest.getKey()).removeValue();
                                }
                            }

                            btnAddFriend.setText("Make friend");
                            btnRejectFriend.setVisibility(View.INVISIBLE);
                        }
                        else if(txtButtonAddFriend.equals(
                                "Agree to make friends")){
                            DataSnapshot nodeAllRequests = dataSnapshot.child("friend_requests");
                            for(DataSnapshot nodeRequest : nodeAllRequests.getChildren()){
                                FriendRequest req = nodeRequest.getValue(FriendRequest.class);
                                if(req.getUidUserLogin().equals(uidSearchFriend)
                                        && req.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                        && req.getStatus().equals("sent")){
                                    nodeFriendRequests.child(nodeRequest.getKey()).removeValue();
                                }
                                if(req.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                        && req.getUidUserFriend().equals(uidSearchFriend)
                                        && req.getStatus().equals("received")){
                                    nodeFriendRequests.child(nodeRequest.getKey()).removeValue();
                                }
                            }

                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = new Date();

                            nodeFriends.child(FirebaseAuth.getInstance().getUid()).child(uidSearchFriend)
                                    .setValue(dateFormat.format(date));
                            nodeFriends.child(uidSearchFriend).child(FirebaseAuth.getInstance().getUid())
                                    .setValue(dateFormat.format(date));

                            btnAddFriend.setText("Unfriended");
                            btnRejectFriend.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.btnRejectFriend:
                nodeRoot.addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        DataSnapshot nodeAllRequests = dataSnapshot.child("friend_requests");
                        for(DataSnapshot nodeRequest : nodeAllRequests.getChildren()){
                            FriendRequest req = nodeRequest.getValue(FriendRequest.class);
                            if(req.getUidUserLogin().equals(uidSearchFriend)
                                    && req.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                    && req.getStatus().equals("sent")){
                                nodeFriendRequests.child(nodeRequest.getKey()).removeValue();
                            }
                            if(req.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && req.getUidUserFriend().equals(uidSearchFriend)
                                    && req.getStatus().equals("received")){
                                nodeFriendRequests.child(nodeRequest.getKey()).removeValue();
                            }
                        }

                        btnAddFriend.setText("Kết bạn");
                        btnRejectFriend.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;

            case R.id.iconBackSearchFriend:
                if(bundle.getString("From").equals("FindFriendActivity")){
                    Intent iSearchFriend = new Intent(SearchProfileActivity.this,FindFriendActivity.class);
                    startActivity(iSearchFriend);
                    finish();
                }
                else if(bundle.getString("From").equals("ListRequestActivity")){
                    Intent iListRequest = new Intent(SearchProfileActivity.this,ListRequestActivity.class);
                    startActivity(iListRequest);
                    finish();
                }
                else if(bundle.getString("From").equals("MoreInfoMessage")){
                    Intent iMoreInfo = new Intent(SearchProfileActivity.this,MoreInfoActivity.class);
                    String nameFriend = bundle.getString("Name_Friend");
                    iMoreInfo.putExtra("UID",uidSearchFriend);
                    iMoreInfo.putExtra("Name",nameFriend);
                    startActivity(iMoreInfo);
                    finish();
                }
                break;
        }
    }

    public void getAvatar()
    {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference ref = storageReference.child("avatar").child(uidSearchFriend+"avatar.jpg");
        try {
            final long ONE_MEGABYTE = 1024 * 1024;
            ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    civAvatar.setImageBitmap(bmp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if(((StorageException) exception).getErrorCode()==-13010) {
                        civAvatar.setImageResource(R.drawable.avatar_default);
                        return;
                    }
                    // Handle any errors/
                }
            });

        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }
    public void getBackground()
    {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference ref = storageReference.child("background").child(uidSearchFriend+"background.jpg");
        try {
            final long ONE_MEGABYTE = 1024 * 1024;
            ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap bmp2 = bmp.createScaledBitmap(bmp,300,170,false);
                    Drawable temp = new BitmapDrawable(getResources(), bmp2);
                    relativeLayout.setBackground(temp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if(((StorageException) exception).getErrorCode()==-13010)
                    {
                        relativeLayout.setBackgroundResource(R.drawable.header_default);
                        return;
                    }
                    // Handle any errors
                }
            });

        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }
}
