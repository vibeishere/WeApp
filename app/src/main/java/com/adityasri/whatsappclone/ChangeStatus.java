package com.adityasri.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

public class ChangeStatus extends AppCompatActivity {
    Toolbar toolbar;
    TextInputLayout status;
    DatabaseReference ref;
    SharedPreferences pref;
    String phoneNumberText,currentStatus;
    Query checkUser;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);
        toolbar = findViewById(R.id.statusAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        status = findViewById(R.id.newStatus);
        pref = getSharedPreferences("UID",MODE_APPEND);
        phoneNumberText = pref.getString("phoneNumber", "");
        ref = FirebaseDatabase.getInstance().getReference("users");
        ref.keepSynced(true);
        checkUser = ref.orderByChild("phoneNumber").equalTo(phoneNumberText);
        checkUser.keepSynced(true);
        showExistingStatus();

    }

    private void showExistingStatus() {
        currentStatus = getIntent().getStringExtra("status");
        status.getEditText().setText(currentStatus);
    }

    private void updateStatus(String newStatusText) {
        if(newStatusText.isEmpty()){
            status.setError("Field cannot be Empty!");
        }
        else{
            status.setError(null);
            status.setErrorEnabled(false);
            ref.child(phoneNumberText).child("status").setValue(newStatusText);
            Toast.makeText(this, "Status Updated!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangeStatus.this,Settings.class);
            startActivity(intent);
            finish();
        }
    }

    public void modifyStatus(View view){
        updateStatus(status.getEditText().getText().toString());
    }

}