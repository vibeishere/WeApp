package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsers extends AppCompatActivity{
    Toolbar toolbar;
    RecyclerView recyclerView;
    DatabaseReference firebaseDatabase;
    SharedPreferences pref;
    String uid;
    TextInputEditText phoneNumberText;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);


        toolbar = findViewById(R.id.appBarLayout);
        recyclerView = findViewById(R.id.usersList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        firebaseDatabase.keepSynced(true);

        pref = getSharedPreferences("UID", MODE_APPEND);
        uid = pref.getString("phoneNumber", "");


        phoneNumberText = findViewById(R.id.searchText);
        phoneNumberText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, final int i, KeyEvent keyEvent) {

                if ((keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    final String searchedUserId = phoneNumberText.getText().toString();

                    firebaseDatabase.child(searchedUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Intent intentProfile = new Intent(AllUsers.this, UserProfile.class);
                                intentProfile.putExtra("userId", searchedUserId);
                                intentProfile.putExtra("UID", uid);
                                startActivity(intentProfile);
                            }else{
                                Toast.makeText(AllUsers.this, "User Does Not Exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AllUsers.this, "Profile cannot be accessed!", Toast.LENGTH_LONG).show();
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("users");



        FirebaseRecyclerOptions<UserClass> options = // create array of object holding information of everyone

                new FirebaseRecyclerOptions.Builder<UserClass>()
                        .setQuery(query, new SnapshotParser<UserClass>() {
                            @NonNull
                            @Override
                            public UserClass parseSnapshot(@NonNull DataSnapshot snapshot) {
                                    return new UserClass(snapshot.child("name").getValue(String.class),
                                        snapshot.child("status").getValue(String.class),
                                        snapshot.child("image").getValue(String.class),
                                        snapshot.child("thumbnail").getValue(String.class));}

                        })
                        .build();

        FirebaseRecyclerAdapter<UserClass, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UserClass, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserClass model) {

                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setThumbImage(model.getThumbImage());

                final String userId = getRef(position).getKey();
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intentProfile = new Intent(AllUsers.this, UserProfile.class);
                        intentProfile.putExtra("userId", userId);
                        intentProfile.putExtra("UID", uid);
                        startActivity(intentProfile);
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);
                return new UserViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public class UserViewHolder extends RecyclerView.ViewHolder {
        View view;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            TextView userSingleName = view.findViewById(R.id.userSingleDisplayName);
            userSingleName.setText(name);
        }


        public void setStatus(String status) {
            TextView userSingleStatus = view.findViewById(R.id.userSingleStatus);
            userSingleStatus.setText(status);
        }


        public void setThumbImage(final String thumbImage) {

            final CircleImageView circleImageView = view.findViewById(R.id.userSingleDisplayPicture);

            Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_dp).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(thumbImage).placeholder(R.drawable.default_dp).into(circleImageView);
                }
            });

        }
    }
}