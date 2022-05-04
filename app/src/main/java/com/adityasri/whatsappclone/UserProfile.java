package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adityasri.whatsappclone.sendNotificationCode.ApiService;
import com.adityasri.whatsappclone.sendNotificationCode.Client;
import com.adityasri.whatsappclone.sendNotificationCode.Data;
import com.adityasri.whatsappclone.sendNotificationCode.NotificationSender;
import com.adityasri.whatsappclone.sendNotificationCode.Response;
import com.adityasri.whatsappclone.sendNotificationCode.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.internal.util.HashMapSupplier;
import retrofit2.Call;
import retrofit2.Callback;

public class UserProfile extends AppCompatActivity {

TextView name,status,friendsStatus;
ImageView displayPicture;
String userId;
Button sendFriendRequestButton,declineFriendRequestButton;
DatabaseReference ref,sendFriendRequestRef,friendRef,notificationRef,rootRef;
ProgressBar progressBar;
private String currentState;
String uid;
SharedPreferences pref;
private ApiService apiService;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        name = findViewById(R.id.userDisplayName);
        displayPicture = findViewById(R.id.userDisplayPicture);
        status = findViewById(R.id.userStatus);
        friendsStatus = findViewById(R.id.userFriendsStatus);
        sendFriendRequestButton = findViewById(R.id.sendFriendRequest);
        progressBar = findViewById(R.id.userProgress);
        declineFriendRequestButton = findViewById(R.id.declineFriendRequest);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);

        currentState = "not_friends";
        userId = getIntent().getStringExtra("userId");
        uid = getIntent().getStringExtra("UID");

        ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
        ref.keepSynced(true);
        friendsStatus.setText("");

        sendFriendRequestRef = FirebaseDatabase.getInstance().getReference("friendsRequest");
        sendFriendRequestRef.keepSynced(true);
        friendRef = FirebaseDatabase.getInstance().getReference("friends");
        friendRef.keepSynced(true);
        notificationRef = FirebaseDatabase.getInstance().getReference("notification");
        notificationRef.keepSynced(true);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);
        declineFriendRequestButton.setEnabled(false);
        declineFriendRequestButton.setVisibility(View.INVISIBLE);

        if(userId.equals(uid)) {
            sendFriendRequestButton.setVisibility(View.INVISIBLE);
            declineFriendRequestButton.setVisibility(View.INVISIBLE);
        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nameText = snapshot.child("name").getValue().toString();
                    String statusText = snapshot.child("status").getValue().toString();
                    final String image = snapshot.child("image").getValue().toString();

                    name.setText(nameText);
                    status.setText(statusText);

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_dp).into(displayPicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_dp).into(displayPicture);
                        }
                    });
                    progressBar.setVisibility(View.INVISIBLE);

                    sendFriendRequestRef.child(uid).child(userId).child("requestType").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                            String req_type = snapshot.getValue().toString();
                            sendFriendRequestButton.setEnabled(true);
                            if(req_type.equals("received")){
                                currentState = "req_received";
                                sendFriendRequestButton.setText("ACCEPT FRIEND REQUEST");
                                declineFriendRequestButton.setEnabled(true);
                                declineFriendRequestButton.setVisibility(View.VISIBLE);
                            }else if(req_type.equals("sent")){
                                currentState = "req_sent";
                                sendFriendRequestButton.setText("CANCEL FRIEND REQUEST");
                                declineFriendRequestButton.setEnabled(false);
                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                            }

                        }else{
                                friendRef.child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild(userId)){
                                            currentState = "friends";
                                            sendFriendRequestButton.setText("UNFRIEND "+ name.getText().toString());
                                            String date = snapshot.child(userId).child("Date").getValue(String.class);
                                            friendsStatus.setText("Friends Since: " + date);
                                            declineFriendRequestButton.setEnabled(false);
                                            declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(UserProfile.this, "Error! Try Again!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UserProfile.this, "Error! Try Again!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfile.this, "Error in retrieving the information!", Toast.LENGTH_LONG).show();
            }
        });


    }
    public void sendFriendRequest(View view){

        sendFriendRequestButton.setEnabled(false);

        //---------------------NOT FRIENDS STATE---------------------
            if(currentState.equals("not_friends")){

                Map requestMap = new HashMap();
                requestMap.put( uid + "/" + userId + "/requestType","sent");
                requestMap.put(userId + "/" + uid + "/requestType","received");


                sendFriendRequestRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                        updateToken();
                        notificationRef.child(uid).child(userId).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    final String token = snapshot.getValue(String.class);
                                    FirebaseDatabase.getInstance().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                String nameAccount = snapshot.child("name").getValue(String.class);
                                                sendNotification(token, "New Friend Request", nameAccount + " has sent you a friend request!");
                                                sendFriendRequestButton.setEnabled(true);
                                                currentState = "req_sent";
                                                sendFriendRequestButton.setText("CANCEL FRIEND REQUEST");
                                                declineFriendRequestButton.setEnabled(false);
                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(UserProfile.this, "Error in retrieving the information!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }else{
                                    Toast.makeText(UserProfile.this, "Error in sending the notification!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UserProfile.this, "Error in retrieving the information!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            //------------------------CANCEL FRIEND REQUEST-------------------------------------
            if(currentState.equals("req_sent")){

                Map removeMap = new HashMap();
                removeMap.put( uid + "/" + userId,null);
                removeMap.put(userId + "/" + uid,null);

                sendFriendRequestRef.updateChildren(removeMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error==null) {
                            sendFriendRequestButton.setEnabled(true);
                            currentState = "not_friends";
                            sendFriendRequestButton.setText("SEND FRIEND REQUEST");
                            declineFriendRequestButton.setEnabled(false);
                            declineFriendRequestButton.setVisibility(View.INVISIBLE);
                        }
                        else{
                            sendFriendRequestButton.setEnabled(true);
                            Toast.makeText(UserProfile.this, "Error! Not able to cancel the request!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            //---------------------------REQ_RECEIVED_STATE----------------------
            if(currentState.equals("req_received")){

                final String currentDate = DateFormat.getDateInstance().format(new Date());

                Map friendMap = new HashMap();
                friendMap.put("friends/" + uid + "/" + userId + "/Date",currentDate);
                friendMap.put("friends/" + userId + "/" + uid + "/Date",currentDate);
                friendMap.put("friendsRequest/" + userId + "/" + uid,null);
                friendMap.put("friendsRequest/" + uid + "/" + userId,null);

                rootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Toast.makeText(UserProfile.this, "Error in updating profile!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            sendFriendRequestButton.setEnabled(true);
                            currentState = "friends";
                            sendFriendRequestButton.setText("UNFRIEND "+ name.getText().toString());
                            declineFriendRequestButton.setEnabled(false);
                            declineFriendRequestButton.setVisibility(View.INVISIBLE);
                        }

                    }
                });
            }

            //-------------------UNFRIEND----------------
        if(currentState.equals("friends")){
            Map removeMap = new HashMap();
            removeMap.put( uid + "/" + userId,null);
            removeMap.put(userId + "/" + uid,null);

            friendRef.updateChildren(removeMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if(error!=null){
                        Toast.makeText(UserProfile.this, "Error in updating profile!", Toast.LENGTH_SHORT).show();
                    }else{
                        friendsStatus.setText("");
                        sendFriendRequestButton.setEnabled(true);
                        currentState = "not_friends";
                        sendFriendRequestButton.setText("SEND FRIEND REQUEST");
                        declineFriendRequestButton.setEnabled(false);
                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                    }

                }
            });

            Map removeChat = new HashMap();
            removeChat.put("chat/"+uid+"/"+userId,null);
            removeChat.put("chat/"+userId+"/"+uid,null);
            rootRef.updateChildren(removeChat);
        }
    }

    public void updateToken() {
        ref.child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.getValue(String.class);
                notificationRef.child(uid).child(userId).child("token").setValue(token);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfile.this, "Error! Cannot Update Token!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void sendNotification(String userToken,String title,String message){
        Data data = new Data(title,message,uid,userId);
        NotificationSender sender = new NotificationSender(data,userToken);

        apiService.sendNotification(sender).enqueue(new Callback<Response>() { // used google api to send notifications for frnd rquest
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if(response.code()==200){
                    if(response.body().success!=1){
                        Toast.makeText(UserProfile.this, "Sending Notification Failed", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Toast.makeText(UserProfile.this, "Sending Notification Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void declineFriendRequest(View view){

        Map removeMap = new HashMap();
        removeMap.put( uid + "/" + userId,null);
        removeMap.put(userId + "/" + uid,null);

        sendFriendRequestRef.updateChildren(removeMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error==null) {
                    sendFriendRequestButton.setEnabled(true);
                    currentState = "not_friends";
                    sendFriendRequestButton.setText("SEND FRIEND REQUEST");
                    declineFriendRequestButton.setEnabled(false);
                    declineFriendRequestButton.setVisibility(View.INVISIBLE);
                }
                else{
                    sendFriendRequestButton.setEnabled(true);
                    Toast.makeText(UserProfile.this, "Error! Not able to cancel the request!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}