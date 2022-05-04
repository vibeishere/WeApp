package com.adityasri.whatsappclone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.ChildEventListener;
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

public class
RequestFragment extends Fragment {
    private RecyclerView requestList;
    private DatabaseReference friendsRef,usersRef;
    private View mainView;
    SharedPreferences pref;
    String uid;

    public RequestFragment() {
        // Required empty public constructor
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
        mainView = inflater.inflate(R.layout.fragment_request, container, false);
        requestList = mainView.findViewById(R.id.requestView);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("friendsRequest").child(uid);
        friendsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        requestList.setHasFixedSize(true);
        requestList.setLayoutManager(linearLayoutManager);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance().getReference().child("friendsRequest").child(uid).orderByChild("requestType").equalTo("received");
        query.keepSynced(true);

        final FirebaseRecyclerOptions<RequestClass> options =
                new FirebaseRecyclerOptions.Builder<RequestClass>()
                        .setQuery(query, new SnapshotParser<RequestClass>() {
                            @NonNull
                            @Override
                            public RequestClass parseSnapshot(@NonNull DataSnapshot snapshot) {
                                    return new RequestClass(snapshot.child("requestType").getValue(String.class));
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter<RequestClass, RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<RequestClass, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, final int position, @NonNull final RequestClass model) {
                final String userId = getRef(position).getKey();
                //holder.setType(model.getType());
                    usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.child("name").getValue(String.class);
                            String thumbnailImage = snapshot.child("thumbnail").getValue(String.class);

                            holder.setName(name);
                            holder.setImage(thumbnailImage);
                            holder.setNumber(userId);

                            holder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intentProfile = new Intent(getContext(), UserProfile.class);
                                    intentProfile.putExtra("userId", userId);
                                    intentProfile.putExtra("UID", uid);
                                    startActivity(intentProfile);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Not able to retrieve data from Database", Toast.LENGTH_LONG).show();
                        }
                    });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_single_layout, parent, false);
                return new RequestViewHolder(view);
                }
        };

        requestList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    private class RequestViewHolder extends RecyclerView.ViewHolder{
        View view;

        public RequestViewHolder(View itemView){
            super(itemView);
            view = itemView;
        }
        public void setName(String name) {
            TextView nameView = view.findViewById(R.id.requestSingleDisplayName);
            nameView.setText(name);
        }
        public void setImage(final String thumbnailImage) {
            final CircleImageView circleImageView = view.findViewById(R.id.requestSingleDisplayPicture);
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
        public void setNumber(String number) {
            TextView numberView = view.findViewById(R.id.requestSingleStatus);
            numberView.setText(number);
        }

        /*public void setType(String type) {
            TextView typeView = view.findViewById(R.id.reqType);
            type = type.toUpperCase();
            typeView.setText(type);
        }*/
    }
}

