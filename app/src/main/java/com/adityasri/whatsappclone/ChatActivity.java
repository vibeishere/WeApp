package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
private Toolbar toolbar;
String userId;
String uid,nameText,thumbnailImage;
TextView displayNameView,displayLastSeenView;
CircleImageView displayPictureView;
RecyclerView messageView;
ImageButton changeForm,sendButton;
EditText textMessage;
private final List<Messages> messagesList = new ArrayList<>();
private LinearLayoutManager linearLayoutManager;
private DatabaseReference rootRef;
private StorageReference imageStorage;
private MessageAdapter messageAdapter;
private static final int TOTAL_ITEMS_TO_LOAD = 10;
private int currentPage = 1;
SwipeRefreshLayout refreshLayout;
private int itemPos = 0;
private String lastKey = "";
private String prevKey = "";
private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        userId = getIntent().getStringExtra("userId");
        uid = getIntent().getStringExtra("UID");
        nameText = getIntent().getStringExtra("name");
        thumbnailImage = getIntent().getStringExtra("thumbnail");

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);
        imageStorage = FirebaseStorage.getInstance().getReference();

        toolbar = findViewById(R.id.chatAppBar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_app_layout,null);
        actionBar.setCustomView(actionBarView);

        displayNameView = findViewById(R.id.chatDisplayName);
        displayLastSeenView = findViewById(R.id.chatLastSeen);
        displayPictureView = findViewById(R.id.chatDisplayPicture);

        messageAdapter = new MessageAdapter(messagesList,uid,getApplicationContext()); // has all the messages

        messageView = findViewById(R.id.messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        messageView.setHasFixedSize(true);
        messageView.setLayoutManager(linearLayoutManager);
        messageView.setAdapter(messageAdapter);

        loadMessages();

        changeForm = findViewById(R.id.changeFormButton);
        sendButton = findViewById(R.id.sendButton);
        textMessage = findViewById(R.id.messageText);

        refreshLayout = findViewById(R.id.swipeRefreshLayout);

        displayNameView.setText(nameText);

        rootRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    final String thumbnailImage = snapshot.child("thumbnail").getValue(String.class);

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastSeenTime =  snapshot.child("lastSeen").getValue(Long.class);
                    String lastSeenString = getTimeAgo.getTimeAgo(lastSeenTime);
                    displayLastSeenView.setText(lastSeenString);

                    Picasso.get().load(thumbnailImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_dp).into(displayPictureView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumbnailImage).placeholder(R.drawable.default_dp).into(displayPictureView);
                        }
                    });
                }else{
                    Toast.makeText(ChatActivity.this, "Error in finding messages from the database!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Error in retrieving messages from the database!", Toast.LENGTH_LONG).show();
            }
        });


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                loadMoreMessages();
            }
        });

    }

    public void sendImageMessage(View view){
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"SELECT A IMAGE"),GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final String currentUserRef = "messages/"+uid+"/"+userId;
            final String chatUserRef = "messages/"+userId+"/"+uid;

            DatabaseReference userMessagePush = rootRef.child("messages").child(uid).child(userId).push();
            userMessagePush.keepSynced(true);

            final String pushID = userMessagePush.getKey();

            final StorageReference filePath = imageStorage.child("messageImages").child(pushID+".jpg");
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();

                            Map messageMapFrom = new HashMap();
                            messageMapFrom.put("message", downloadUrl);
                            messageMapFrom.put("type", "image");
                            messageMapFrom.put("time", ServerValue.TIMESTAMP);
                            messageMapFrom.put("from", uid);

                            Map messageMapTo = new HashMap();
                            messageMapTo.put("message", downloadUrl);
                            messageMapTo.put("type", "image");
                            messageMapTo.put("time", ServerValue.TIMESTAMP);
                            messageMapTo.put("from", uid);

                            Map userMessageMap = new HashMap();
                            userMessageMap.put(currentUserRef + "/" + pushID, messageMapFrom);
                            userMessageMap.put(chatUserRef + "/" + pushID, messageMapTo);

                            rootRef.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    if (error != null) {
                                        Toast.makeText(ChatActivity.this, "Error in sending the message!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, "Error in uploading the image!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loadMoreMessages() {

        itemPos = 0;
        final DatabaseReference messageRef = rootRef.child("messages").child(uid).child(userId);
        messageRef.keepSynced(true);

        Query query = messageRef.orderByKey().endAt(lastKey).limitToLast(TOTAL_ITEMS_TO_LOAD); // after loading keeps it as precise location

        query.keepSynced(true);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                final String messageKey = snapshot.getKey();

                if(!messageKey.equals(prevKey)){
                    messagesList.add(itemPos,messages);
                    itemPos++;
                }else{
                    prevKey = lastKey;
                }

                if(itemPos==1){
                    lastKey = messageKey;
                }

                Log.d("KEY",prevKey + "|" + lastKey + "|" + messageKey);
                messageAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                linearLayoutManager.scrollToPosition(TOTAL_ITEMS_TO_LOAD-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Cannot load more messages!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMessages() {

        final DatabaseReference messageRef = rootRef.child("messages").child(uid).child(userId);
        messageRef.keepSynced(true);
        final Query query = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);
        query.keepSynced(true);
        query.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                final String messageKey = snapshot.getKey();

                itemPos++;

                 if(itemPos==1){
                     lastKey = messageKey;
                     prevKey = messageKey;
                 }

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged(); // this gives the final signal so as update chat activity

                messageView.smoothScrollToPosition(messagesList.size()-1);
                refreshLayout.setRefreshing(false);
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

             }

             @Override
             public void onChildRemoved(@NonNull DataSnapshot snapshot) {

             }

             @Override
             public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                 Toast.makeText(ChatActivity.this, "Error in loading the messages!", Toast.LENGTH_SHORT).show();
             }

         });
    }

    public void sendMessage(View view){

        String message = textMessage.getText().toString();

        if(!TextUtils.isEmpty(message)){
            String currentUserRef = "messages/"+uid+"/"+userId;
            String chatUserRef = "messages/"+userId+"/"+uid;

            DatabaseReference userMessagePush = rootRef.child("messages").child(uid).child(userId).push();
            userMessagePush.keepSynced(true);
            String pushID = userMessagePush.getKey();

            Map messageMapFrom = new HashMap();
            messageMapFrom.put("message",message);
            messageMapFrom.put("type","text");
            messageMapFrom.put("time",ServerValue.TIMESTAMP);
            messageMapFrom.put("from",uid);

            Map messageMapTo = new HashMap();
            messageMapTo.put("message",message);
            messageMapTo.put("type","text");
            messageMapTo.put("time",ServerValue.TIMESTAMP);
            messageMapTo.put("from",uid);

            Map userMessageMap = new HashMap();
            userMessageMap.put(currentUserRef+"/"+pushID,messageMapFrom);
            userMessageMap.put(chatUserRef+"/"+pushID,messageMapTo);

            messageView.smoothScrollToPosition(messagesList.size()-1);

            rootRef.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                    if(error!=null){
                        Toast.makeText(ChatActivity.this, "Error in sending the message!", Toast.LENGTH_SHORT).show();
                    }else{
                        textMessage.setText("");
                    }

                }
            });
            rootRef.child("chat").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.hasChild(userId)){
                        Map chatAddMap = new HashMap();
                        chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);

                        Map chatUserMap = new HashMap();
                        chatUserMap.put("chat/"+uid+"/"+userId,chatAddMap);
                        chatUserMap.put("chat/"+userId+"/"+uid,chatAddMap);

                        rootRef.updateChildren(chatUserMap);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChatActivity.this,"Error when updating the chat!",Toast.LENGTH_LONG).show();
                }
            });
        }else{
            Toast.makeText(this, "Type A Message!", Toast.LENGTH_SHORT).show();;
        }
    }

}