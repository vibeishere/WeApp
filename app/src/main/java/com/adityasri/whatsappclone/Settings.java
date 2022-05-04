package com.adityasri.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Settings extends AppCompatActivity {
DatabaseReference ref;
SharedPreferences preferences;
Query checkUser;
String phoneNumberText,statusText,imageText,nameText;
TextView name,status;
CircleImageView displayPicture;
StorageReference mStorageRef;
ProgressBar progressBar;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        name = findViewById(R.id.displayName);
        displayPicture = findViewById(R.id.userSingleDisplayPicture);
        status = findViewById(R.id.status);
        progressBar = findViewById(R.id.Progress);
        progressBar.setVisibility(View.INVISIBLE);

        preferences = getSharedPreferences("UID",MODE_APPEND);
        phoneNumberText = preferences.getString("phoneNumber","");

        mStorageRef = FirebaseStorage.getInstance().getReference("DP");
        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.keepSynced(true);

        checkUser = ref.orderByChild("phoneNumber").equalTo(phoneNumberText);
        checkUser.keepSynced(true);


        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ref.child(phoneNumberText).child("online").setValue(true);
                    imageText = snapshot.child(phoneNumberText).child("image").getValue(String.class);
                    statusText = snapshot.child(phoneNumberText).child("status").getValue(String.class);
                    nameText = snapshot.child(phoneNumberText).child("name").getValue(String.class);
                    name.setText(nameText);
                    if(!imageText.equals("default_dp.png")){
                        setImage(imageText);
                    }
                    setStatus(statusText);
                }
                else{
                    Toast.makeText(Settings.this, "Error in retrieving the Data!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Settings.this, "Error! Try Again!", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setImage(final String image) {
        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_dp).into(displayPicture, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(image).placeholder(R.drawable.default_dp).into(displayPicture);
            }
        });
    }

    private void setStatus(String statusText) {
        status.setText(statusText);
    }

    public void changeStatus(View view){
        Intent intentStatus = new Intent(Settings.this,ChangeStatus.class);
        intentStatus.putExtra("status",statusText);
        startActivity(intentStatus);
    }

    public void changeImage(View view){
                    Intent intentImage = new Intent();
                    intentImage.setType("image/*");
                    intentImage.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intentImage,"Select Image"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(Settings.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumbnailFilePath = new File(resultUri.getPath());
                Bitmap thumbnailBitmap = null;
                try {
                    thumbnailBitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumbnailFilePath);
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbnailByte = baos.toByteArray();

                progressBar.setVisibility(View.VISIBLE);
                final StorageReference riversRef = mStorageRef.child(phoneNumberText+".jpg");
                final StorageReference thumbnailRef = mStorageRef.child("Thumbnail").child(phoneNumberText+".jpg");
                riversRef.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        thumbnailRef.putBytes(thumbnailByte)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        thumbnailRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri thumbnail_uri) {
                                                                String thumbnailUrl = thumbnail_uri.toString();
                                                                Map updateHashMap = new HashMap<>();
                                                                updateHashMap.put("image",downloadUrl);
                                                                updateHashMap.put("thumbnail",thumbnailUrl);
                                                                ref.child(phoneNumberText).updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            progressBar.setVisibility(View.INVISIBLE);
                                                                            setImage(downloadUrl);
                                                                        }
                                                                    }
                                                                });


                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                });
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Settings.this, "Image could not be uploaded!", Toast.LENGTH_LONG).show();
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

}