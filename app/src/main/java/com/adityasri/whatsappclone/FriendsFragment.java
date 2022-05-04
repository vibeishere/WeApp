package com.adityasri.whatsappclone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
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

import static android.content.Context.MODE_APPEND;


public class FriendsFragment extends Fragment {
private RecyclerView friendsView;
private DatabaseReference databaseRef;
private String uid;
private View mainView;
SharedPreferences pref;

    public FriendsFragment() {
    }


    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = this.getActivity().getSharedPreferences("UID",MODE_APPEND);
        uid = pref.getString("phoneNumber","");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView =  inflater.inflate(R.layout.fragment_friends, container, false);

        friendsView = mainView.findViewById(R.id.friendsView);
        friendsView.setHasFixedSize(true);
        friendsView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        databaseRef.keepSynced(true);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("friends").child(uid);

        query.keepSynced(true);

        final FirebaseRecyclerOptions<FriendsClass> options =
                new FirebaseRecyclerOptions.Builder<FriendsClass>()
                        .setQuery(query, new SnapshotParser<FriendsClass>() {
                            @NonNull
                            @Override
                            public FriendsClass parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new FriendsClass(snapshot.child("Date").getValue(String.class));
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter<FriendsClass, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendsClass, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull FriendsClass model) {


                final String userId = getRef(position).getKey();
                databaseRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String userName = snapshot.child("name").getValue(String.class);
                        final String thumbnailImage = snapshot.child("thumbnail").getValue(String.class);
                        String status = snapshot.child("status").getValue(String.class);

                        holder.setStatus(status);
                        GetTimeAgo getTimeAgo = new GetTimeAgo();
                        long lastSeenTime =  snapshot.child("lastSeen").getValue(Long.class);
                        String lastSeenString = getTimeAgo.getTimeAgo(lastSeenTime);
                        holder.setOnline(lastSeenString);


                        holder.setName(userName);
                        holder.setImage(thumbnailImage);

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence option[] = new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select an Option");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(i==0){
                                            Intent intentProfile = new Intent(getContext(), UserProfile.class);
                                            intentProfile.putExtra("userId", userId);
                                            intentProfile.putExtra("UID", uid);
                                            startActivity(intentProfile);
                                        }
                                        if(i==1){
                                            Intent intentChat = new Intent(getContext(), ChatActivity.class);
                                            intentChat.putExtra("userId", userId);
                                            intentChat.putExtra("UID", uid);
                                            intentChat.putExtra("name", userName);
                                            startActivity(intentChat);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error! Not able to show your friends!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);
                return new FriendsViewHolder(view);
            }
        };

        friendsView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    private class FriendsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public FriendsViewHolder(View itemView){
            super(itemView);
            view = itemView;
        }

        public void setStatus(String status){

            TextView userDate = view.findViewById(R.id.userSingleStatus);
            userDate.setText(status);
        }

        public void setName(String name){
            TextView userName = view.findViewById(R.id.userSingleDisplayName);
            userName.setText(name);
        }


        public void setImage(final String thumbnailImage) {
            final CircleImageView circleImageView = view.findViewById(R.id.userSingleDisplayPicture);
            Picasso.get().load(thumbnailImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_dp).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(thumbnailImage).placeholder(R.drawable.default_dp).into(circleImageView);
                }
            });
        }

        public void setOnline(final String onlineStatus) {
            final ImageView imageView = view.findViewById(R.id.onlineStatus);
            if(onlineStatus.equals("Online")){
                imageView.setVisibility(View.VISIBLE);
            }
            else{
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}