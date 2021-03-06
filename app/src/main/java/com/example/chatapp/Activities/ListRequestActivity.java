package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.Adapters.ListRequestAdapter;
import com.example.chatapp.Models.Account;
import com.example.chatapp.Models.AccountRequest;
import com.example.chatapp.Models.FriendRequest;

import com.example.chatapp.Adapters.MainActivity;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListRequestActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener{

    private ImageView btnBackListFriend;
    private ListView listViewListRequest;

    private List<AccountRequest> listAccountRequests;
    private ListRequestAdapter listRequestAdapter;

    private DatabaseReference nodeRoot;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_request);

        btnBackListFriend = (ImageView)findViewById(R.id.iconBackListFriend);
        btnBackListFriend.setOnClickListener(this);

        nodeRoot = FirebaseDatabase.getInstance().getReference();
        nodeRoot.addValueEventListener(this);

        listAccountRequests = new ArrayList<>();

        listViewListRequest = (ListView)findViewById(R.id.listViewListRequest);
        listRequestAdapter = new ListRequestAdapter(this, R.layout.item_request_in_list_request,listAccountRequests);
        listViewListRequest.setAdapter(listRequestAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iconBackListFriend:
                Intent intent = new Intent(ListRequestActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("ReturnTab", 1);
                bundle.putString("UID", FirebaseAuth.getInstance().getUid());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        listAccountRequests.clear();
        List<String> uidTemp = new ArrayList<>(); // t???o 1 list t???m

        DataSnapshot nodeFriendRequests = dataSnapshot.child("friend_requests");
        for(DataSnapshot singleRequest : nodeFriendRequests.getChildren()){
            FriendRequest frTemp = singleRequest.getValue(FriendRequest.class);

            if(frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                    && frTemp.getStatus().equals("received")){
                uidTemp.add(frTemp.getUidUserFriend()); // l???y ra ???????c t???t c??? y??u c???u k???t b???n t???i nick ????ng nh???p
            }
        }

        DataSnapshot nodeUsers = dataSnapshot.child("users");
        for(DataSnapshot singleUser : nodeUsers.getChildren()){
            Account acc = singleUser.getValue(Account.class);
            if(uidTemp.contains(singleUser.getKey())){
                // ki???m tra n???u uid t??? list l???y ra ??? request gi???ng uid trong users th?? add v??o list account request
                listAccountRequests.add(new AccountRequest(singleUser.getKey(),acc.getFullName(),
                                                                acc.getUsername(),acc.getPhoneNumber()));
            }
        }

        listRequestAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
